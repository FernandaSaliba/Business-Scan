package com.example.business_scan.screens



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

import androidx.compose.ui.res.stringResource

import androidx.compose.ui.text.font.FontWeight

import androidx.compose.ui.text.input.ImeAction

import androidx.compose.ui.text.input.KeyboardType

import androidx.compose.ui.text.input.PasswordVisualTransformation

import androidx.compose.ui.unit.dp

import androidx.compose.ui.unit.sp

import androidx.credentials.CredentialManager

import androidx.credentials.CustomCredential

import androidx.credentials.GetCredentialRequest

import com.example.business_scan.R

import com.google.android.libraries.identity.googleid.GetGoogleIdOption

import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

import com.google.firebase.auth.FirebaseAuth

import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException

import com.google.firebase.auth.FirebaseAuthInvalidUserException

import com.google.firebase.auth.FirebaseAuthUserCollisionException

import com.google.firebase.auth.GoogleAuthProvider

import kotlinx.coroutines.launch



@OptIn(ExperimentalMaterial3Api::class)

@Composable

fun LoginScreen(

    webClientId: String = "",

    onLoginSuccess: (rememberMe: Boolean, email: String) -> Unit

) {

    var email by remember { mutableStateOf("") }

    var password by remember { mutableStateOf("") }

    var rememberMe by remember { mutableStateOf(false) }

    var isLoading by remember { mutableStateOf(false) }



    val context = LocalContext.current

    val coroutineScope = rememberCoroutineScope()

    val auth = remember { FirebaseAuth.getInstance() }

    val focusManager = LocalFocusManager.current

    val keyboardController = LocalSoftwareKeyboardController.current



// Resgate seguro do ID do cliente utilizando o Context do Android em vez de envolver uma chamada @Composable no runCatching

    val fallbackClientId = remember(context) {

        try {

            context.getString(R.string.default_web_client_id)

        } catch (e: Exception) {

            ""

        }

    }



    val isEmailValid = android.util.Patterns.EMAIL_ADDRESS.matcher(email.trim()).matches()

    val isPasswordValid = password.length >= 6



    fun traduzirErroFirebase(exception: Exception?): String {

        return when (exception) {

            is FirebaseAuthInvalidCredentialsException -> "E-mail ou senha incorretos."

            is FirebaseAuthInvalidUserException -> "Conta não encontrada. Crie uma nova conta."

            is FirebaseAuthUserCollisionException -> "Este e-mail já está cadastrado."

            else -> exception?.localizedMessage ?: "Ocorreu um erro inesperado. Tente novamente."

        }

    }



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

        auth.signInWithEmailAndPassword(email.trim(), password)

            .addOnCompleteListener { task ->

                isLoading = false

                if (task.isSuccessful) {

                    Toast.makeText(context, "Bem-vindo de volta!", Toast.LENGTH_SHORT).show()

                    onLoginSuccess(rememberMe, email.trim())

                } else {

                    val erroFormatado = traduzirErroFirebase(task.exception)

                    Toast.makeText(context, erroFormatado, Toast.LENGTH_LONG).show()

                }

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

        auth.createUserWithEmailAndPassword(email.trim(), password)

            .addOnCompleteListener { task ->

                isLoading = false

                if (task.isSuccessful) {

                    Toast.makeText(context, "Conta criada com sucesso!", Toast.LENGTH_SHORT).show()

                    onLoginSuccess(rememberMe, email.trim())

                } else {

                    val erroFormatado = traduzirErroFirebase(task.exception)

                    Toast.makeText(context, erroFormatado, Toast.LENGTH_LONG).show()

                }

            }

    }



    fun redefinirSenha() {

        if (!isEmailValid) {

            Toast.makeText(context, "Digite seu e-mail para redefinir a senha.", Toast.LENGTH_SHORT).show()

            return

        }

        auth.sendPasswordResetEmail(email.trim())

            .addOnCompleteListener { task ->

                if (task.isSuccessful) {

                    Toast.makeText(context, "E-mail de redefinição enviado!", Toast.LENGTH_LONG).show()

                } else {

                    Toast.makeText(context, "Erro ao enviar e-mail.", Toast.LENGTH_SHORT).show()

                }

            }

    }



    fun realizarLoginGoogle() {

        val clientId = webClientId.ifEmpty { fallbackClientId }



        if (clientId.isEmpty()) {

            Toast.makeText(context, "webClientId do Google não configurado.", Toast.LENGTH_SHORT).show()

            return

        }



        isLoading = true

        coroutineScope.launch {

            try {

                val credentialManager = CredentialManager.create(context)

                val googleIdOption = GetGoogleIdOption.Builder()

                    .setFilterByAuthorizedAccounts(false)

                    .setServerClientId(clientId)

                    .setAutoSelectEnabled(false)

                    .build()



                val request = GetCredentialRequest.Builder()

                    .addCredentialOption(googleIdOption)

                    .build()



                val result = credentialManager.getCredential(request = request, context = context)

                val credential = result.credential



                if (credential is CustomCredential && credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL) {

                    val googleIdTokenCredential = GoogleIdTokenCredential.createFrom(credential.data)

                    val firebaseCredential = GoogleAuthProvider.getCredential(googleIdTokenCredential.idToken, null)



                    auth.signInWithCredential(firebaseCredential).addOnCompleteListener { task ->

                        isLoading = false

                        if (task.isSuccessful) {

                            val userEmail = auth.currentUser?.email ?: ""

                            Toast.makeText(context, "Bem-vindo!", Toast.LENGTH_SHORT).show()

                            onLoginSuccess(rememberMe, userEmail)

                        } else {

                            Toast.makeText(context, traduzirErroFirebase(task.exception), Toast.LENGTH_LONG).show()

                        }

                    }

                } else {

                    isLoading = false

                }

            } catch (e: Exception) {

                isLoading = false

                Toast.makeText(context, "Erro no Google: ${e.localizedMessage}", Toast.LENGTH_LONG).show()

            }

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



// Marcador Lembrar de Mim

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

