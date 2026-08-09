package com.example.business_scan


import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.lifecycle.lifecycleScope
import com.example.business_scan.data.UserPreferences
import com.example.business_scan.model.Business
import com.example.business_scan.screens.LoginScreen
import com.example.business_scan.screens.PremiumScreen
import com.example.business_scan.screens.SearchScreen
import com.example.business_scan.screens.SplashScreen
import com.google.firebase.auth.FirebaseAuth
import kotlinx.coroutines.launch



class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val userPreferences = UserPreferences(applicationContext)
        val googleWebClientId = "198083510769-287vessuvr02ggtmun2f02r2m5bnmunh.apps.googleusercontent.com"

        setContent {
            val rememberMeState by userPreferences.rememberMeFlow.collectAsState(initial = false)
            var isLoggedIn by remember { mutableStateOf(false) }
            var showSplash by remember { mutableStateOf(true) }

            var currentScreen by remember { mutableStateOf("search") }
            var selectedBusinessForPro by remember { mutableStateOf<Business?>(null) }

            LaunchedEffect(rememberMeState) {
                if (rememberMeState || FirebaseAuth.getInstance().currentUser != null) {
                    isLoggedIn = true
                }
            }

            if (showSplash) {
                SplashScreen(onTimeout = { showSplash = false })
            } else if (!isLoggedIn) {
                LoginScreen(
                    webClientId = googleWebClientId,
                    onLoginSuccess = { remember: Boolean, email: String ->
                        lifecycleScope.launch {
                            userPreferences.saveUserSession(remember, email)
                            isLoggedIn = true
                        }
                    }
                )
            } else {
                when (currentScreen) {
                    "search" -> SearchScreen(
                        onLogout = {
                            lifecycleScope.launch {
                                userPreferences.clearSession()
                                FirebaseAuth.getInstance().signOut()
                                isLoggedIn = false
                                showSplash = true
                            }
                        },
                        onOpenPremium = { business ->
                            selectedBusinessForPro = business
                            currentScreen = "premium"
                        }
                    )
                    "premium" -> PremiumScreen(
                        business = selectedBusinessForPro, // 👈 PASSANDO O NEGÓCIO PESQUISADO AQUI!
                        onBackClick = { currentScreen = "search" },
                        onSubscribeSuccess = {
                            currentScreen = "search"
                        }
                    )
                }
            }
        }
    }
}
