package com.rodertech.businessscan.screens

import android.widget.Toast
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Business
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.rodertech.business_scan.R
import com.rodertech.businessscan.auth.AuthManager
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    webClientId: String = "198083510769-287vessuvr02ggtmun2f02r2m5bnmunh.apps.googleusercontent.com",
    onLoginSuccess: (rememberMe: Boolean, email: String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var rememberMe by remember { mutableStateOf(false) }
    var isLoading by remember { mutableStateOf(false) }

    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val focusManager = LocalFocusManager.current
    val keyboardController = LocalSoftwareKeyboardController.current

    val authManager = remember { AuthManager(context) }

    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()
    val isPasswordValid = password.length >= 6

    fun realizarLogin() {
        keyboardController?.hide()
        if (!isEmailValid) {
            Toast.makeText(context, "Insira um e-mail válido.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isPasswordValid) {
            Toast.makeText(context, "A senha deve ter no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        coroutineScope.launch {
            authManager.signInWithEmail(
                email = email,
                pass = password,
                onSuccess = { user ->
                    isLoading = false
                    Toast.makeText(context, "Bem-vindo de volta!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess(rememberMe, user.email ?: "")
                },
                onError = { erro ->
                    isLoading = false
                    Toast.makeText(context, erro, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    fun realizarCadastro() {
        keyboardController?.hide()
        if (!isEmailValid) {
            Toast.makeText(context, "Insira um e-mail válido para cadastrar.", Toast.LENGTH_SHORT).show()
            return
        }
        if (!isPasswordValid) {
            Toast.makeText(context, "A senha precisa de no mínimo 6 caracteres.", Toast.LENGTH_SHORT).show()
            return
        }

        isLoading = true
        coroutineScope.launch {
            authManager.signUpWithEmail(
                email = email,
                pass = password,
                onSuccess = { user ->
                    isLoading = false
                    Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess(rememberMe, user.email ?: "")
                },
                onError = { erro ->
                    isLoading = false
                    Toast.makeText(context, erro, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    fun redefinirSenha() {
        if (!isEmailValid) {
            Toast.makeText(context, "Digite seu e-mail para redefinir a senha.", Toast.LENGTH_SHORT).show()
            return
        }
        coroutineScope.launch {
            authManager.resetPassword(
                email = email,
                onSuccess = {
                    Toast.makeText(context, "E-mail de redefinição enviado!", Toast.LENGTH_LONG).show()
                },
                onError = { erro ->
                    Toast.makeText(context, erro, Toast.LENGTH_SHORT).show()
                }
            )
        }
    }

    fun realizarLoginGoogle() {
        isLoading = true
        coroutineScope.launch {
            authManager.signInWithGoogle(
                webClientId = webClientId,
                onSuccess = { user ->
                    isLoading = false
                    Toast.makeText(context, "Bem-vindo!", Toast.LENGTH_SHORT).show()
                    onLoginSuccess(rememberMe, user.email ?: "")
                },
                onError = { erro ->
                    isLoading = false
                    Toast.makeText(context, erro, Toast.LENGTH_LONG).show()
                }
            )
        }
    }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(
                Brush.verticalGradient(
                    colors = listOf(
                        Color(0xFF0F172A),
                        Color(0xFF1E1B4B)
                    )
                )
            ),
        contentAlignment = Alignment.Center
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(24.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center
        ) {
            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier.size(80.dp)
            ) {
                Surface(
                    modifier = Modifier.size(70.dp),
                    shape = RoundedCornerShape(20.dp),
                    color = Color(0xFF6366F1).copy(alpha = 0.2f)
                ) {}

                Surface(
                    modifier = Modifier.size(60.dp),
                    shape = RoundedCornerShape(16.dp),
                    color = Color.Transparent
                ) {
                    Box(
                        modifier = Modifier
                            .fillMaxSize()
                            .background(
                                Brush.linearGradient(
                                    colors = listOf(Color(0xFF818CF8), Color(0xFF4F46E5))
                                )
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Icon(
                            imageVector = Icons.Default.Business,
                            contentDescription = "Business Icon",
                            tint = Color.White,
                            modifier = Modifier.size(32.dp)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("Business", fontSize = 28.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Text("Scan", fontSize = 28.sp, fontWeight = FontWeight.ExtraBold, color = Color(0xFF818CF8))
            }

            Text(
                text = "Inteligência & Consulta Cadastral",
                fontSize = 13.sp,
                color = Color.LightGray
            )

            Spacer(modifier = Modifier.height(28.dp))

            Card(
                shape = RoundedCornerShape(24.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                elevation = CardDefaults.cardElevation(defaultElevation = 8.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(
                    modifier = Modifier.padding(20.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    OutlinedTextField(
                        value = email,
                        onValueChange = { email = it },
                        placeholder = { Text("EMAIL", color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Email, contentDescription = null, tint = Color(0xFF818CF8)) },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Email,
                            imeAction = ImeAction.Next
                        ),
                        keyboardActions = KeyboardActions(onNext = { focusManager.moveFocus(FocusDirection.Down) }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    OutlinedTextField(
                        value = password,
                        onValueChange = { password = it },
                        placeholder = { Text("PASSWORD", color = Color.Gray, fontSize = 14.sp) },
                        leadingIcon = { Icon(Icons.Default.Lock, contentDescription = null, tint = Color(0xFF818CF8)) },
                        visualTransformation = PasswordVisualTransformation(),
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(
                            keyboardType = KeyboardType.Password,
                            imeAction = ImeAction.Done
                        ),
                        keyboardActions = KeyboardActions(onDone = { realizarLogin() }),
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFF6366F1),
                            unfocusedBorderColor = Color(0xFF475569),
                            focusedTextColor = Color.White,
                            unfocusedTextColor = Color.White
                        ),
                        modifier = Modifier.fillMaxWidth(),
                        shape = RoundedCornerShape(16.dp)
                    )

                    Spacer(modifier = Modifier.height(8.dp))

                    Row(
                        verticalAlignment = Alignment.CenterVertically,
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable { rememberMe = !rememberMe }
                    ) {
                        Checkbox(
                            checked = rememberMe,
                            onCheckedChange = { rememberMe = it },
                            colors = CheckboxDefaults.colors(
                                checkedColor = Color(0xFF6366F1),
                                uncheckedColor = Color(0xFF475569),
                                checkmarkColor = Color.White
                            )
                        )
                        Text(
                            text = "Lembrar meu login",
                            color = Color.LightGray,
                            fontSize = 14.sp
                        )
                    }

                    Spacer(modifier = Modifier.height(12.dp))

                    if (isLoading) {
                        CircularProgressIndicator(color = Color(0xFF818CF8))
                    } else {
                        Button(
                            onClick = { realizarLogin() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6366F1))
                        ) {
                            Text("LOGIN", fontWeight = FontWeight.Bold, fontSize = 16.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(14.dp))

                        Text(
                            text = "Esqueceu a senha?",
                            color = Color(0xFF818CF8),
                            fontSize = 14.sp,
                            fontWeight = FontWeight.Medium,
                            modifier = Modifier.clickable { redefinirSenha() }
                        )

                        Spacer(modifier = Modifier.height(16.dp))

                        Button(
                            onClick = { realizarCadastro() },
                            modifier = Modifier
                                .fillMaxWidth()
                                .height(50.dp),
                            shape = RoundedCornerShape(16.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF334155))
                        ) {
                            Text("Criar conta", fontWeight = FontWeight.SemiBold, fontSize = 15.sp, color = Color.White)
                        }

                        Spacer(modifier = Modifier.height(20.dp))

                        HorizontalDivider(
                            color = Color(0xFF475569),
                            thickness = 1.dp,
                            modifier = Modifier.fillMaxWidth(0.9f)
                        )

                        Spacer(modifier = Modifier.height(20.dp))

                        Surface(
                            shape = RoundedCornerShape(12.dp),
                            color = Color.White,
                            modifier = Modifier
                                .size(52.dp)
                                .clickable { realizarLoginGoogle() }
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Image(
                                    painter = painterResource(id = R.drawable.ic_google),
                                    contentDescription = "Login com Google",
                                    modifier = Modifier.size(26.dp)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}