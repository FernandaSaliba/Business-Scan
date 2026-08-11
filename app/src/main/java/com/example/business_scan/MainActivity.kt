package com.example.business_scan

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
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
            val scope = rememberCoroutineScope()
            val rememberMeState by userPreferences.rememberMeFlow.collectAsState(initial = false)

            val isLoggedIn = remember { mutableStateOf(false) }
            val showSplash = remember { mutableStateOf(true) }

            val currentScreen = remember { mutableStateOf("search") }
            val selectedBusinessForPro = remember { mutableStateOf<Business?>(null) }

            LaunchedEffect(rememberMeState) {
                if (rememberMeState || FirebaseAuth.getInstance().currentUser != null) {
                    isLoggedIn.value = true
                }
            }

            if (showSplash.value) {
                SplashScreen(onTimeout = {
                    showSplash.value = false
                })
            } else if (!isLoggedIn.value) {
                LoginScreen(
                    webClientId = googleWebClientId,
                    onLoginSuccess = { rememberUser, email ->
                        scope.launch {
                            userPreferences.saveUserSession(rememberUser, email)
                            isLoggedIn.value = true
                        }
                    }
                )
            } else {
                when (currentScreen.value) {
                    "search" -> SearchScreen(
                        onLogout = {
                            scope.launch {
                                userPreferences.clearSession()
                                FirebaseAuth.getInstance().signOut()
                                isLoggedIn.value = false
                                showSplash.value = true
                            }
                        },
                        onOpenPremium = { business ->
                            selectedBusinessForPro.value = business
                            currentScreen.value = "premium"
                        }
                    )
                    "premium" -> PremiumScreen(
                        business = selectedBusinessForPro.value,
                        onBackClick = {
                            currentScreen.value = "search"
                        },
                        onSubscribeSuccess = {
                            currentScreen.value = "search"
                        }
                    )
                }
            }
        }
    }
}