package com.rodertech.businessscan.screens

import android.content.Intent
import androidx.core.net.toUri
import android.widget.Toast
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Email
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.Star
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.firebase.auth.FirebaseAuth
import com.rodertech.businessscan.auth.AuthManager
import com.rodertech.businessscan.data.UserPreferences
import kotlinx.coroutines.launch
import androidx.compose.foundation.background

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    onBackClick: () -> Unit,
    onAccountDeleted: () -> Unit,
    onOpenPremium: () -> Unit = {} // <--- Callback para abrir a tela de compra caso seja Free
) {
    val context = LocalContext.current
    val coroutineScope = rememberCoroutineScope()
    val authManager = remember { AuthManager(context) }
    val userPreferences = remember { UserPreferences(context) }

    // Observa o estado real de assinatura do usuário
    val isPremium by userPreferences.isPremiumFlow.collectAsState(initial = false)

    var showDeleteDialog by remember { mutableStateOf(false) }

    val backgroundColor = Color(0xFF1B1F38)
    val cardBackgroundColor = Color(0xFF282D4F)
    val goldColor = Color(0xFFFFC107)
    val orangeButtonColor = Color(0xFFE67E22)
    val currentUserEmail = FirebaseAuth.getInstance().currentUser?.email ?: "Usuário Conectado"

    Scaffold(
        topBar = {
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 20.dp, vertical = 16.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Botão de voltar estilizado
                IconButton(
                    onClick = onBackClick,
                    modifier = Modifier
                        .size(40.dp)
                        .background(cardBackgroundColor, RoundedCornerShape(12.dp))
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = "Voltar",
                        tint = Color.White,
                        modifier = Modifier.size(20.dp)
                    )
                }

                Spacer(modifier = Modifier.width(16.dp)) // Espaçamento idêntico ao da tela de CNPJ

                Text(
                    text = "Configurações",
                    fontSize = 20.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White
                )
            }
        },
        containerColor = backgroundColor
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 20.dp)
                .verticalScroll(rememberScrollState()),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            Spacer(modifier = Modifier.height(4.dp))

            // --- SEÇÃO DE PERFIL / CONTA ---
            Text(
                text = "CONTA",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = goldColor
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Row(
                    modifier = Modifier
                        .padding(16.dp)
                        .fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Surface(
                        color = goldColor.copy(alpha = 0.2f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.size(44.dp)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(imageVector = Icons.Default.Email, contentDescription = null, tint = goldColor)
                        }
                    }
                    Spacer(modifier = Modifier.width(14.dp))
                    Column(modifier = Modifier.weight(1f)) {
                        Text(text = "E-mail Conectado", fontSize = 11.sp, color = Color.LightGray)
                        Text(text = currentUserEmail, fontSize = 13.sp, fontWeight = FontWeight.SemiBold, color = Color.White)
                    }
                }
            }

            // --- SEÇÃO DE PREFERÊNCIAS E PLANO (DINÂMICO) ---
            Text(
                text = "ASSINATURA E RECURSOS",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = goldColor
            )

            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Row(
                        modifier = Modifier.fillMaxWidth(),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Surface(
                            color = goldColor.copy(alpha = 0.2f),
                            shape = RoundedCornerShape(12.dp),
                            modifier = Modifier.size(44.dp)
                        ) {
                            Box(contentAlignment = Alignment.Center) {
                                Icon(imageVector = Icons.Default.Star, contentDescription = null, tint = goldColor)
                            }
                        }
                        Spacer(modifier = Modifier.width(14.dp))
                        Column(modifier = Modifier.weight(1f)) {
                            Text(text = "Plano Atual", fontSize = 11.sp, color = Color.LightGray)
                            Text(
                                text = if (isPremium) "BusinessScan PRO ⭐" else "Plano Gratuito (Free)",
                                fontSize = 14.sp,
                                fontWeight = FontWeight.Bold,
                                color = if (isPremium) goldColor else Color.White
                            )
                        }
                    }

                    Spacer(modifier = Modifier.height(14.dp))

                    // Botão dinâmico dependendo se é Premium ou Free
                    if (isPremium) {
                        OutlinedButton(
                            onClick = {
                                // Redireciona o usuário para gerenciar a assinatura na Play Store (padrão recomendado pelo Google)
                                val intent = Intent(Intent.ACTION_VIEW, "https://play.google.com/store/account/subscriptions".toUri())
                                try {
                                    context.startActivity(intent)
                                } catch (_: Exception) {
                                    Toast.makeText(context, "Não foi possível abrir a Google Play.", Toast.LENGTH_SHORT).show()
                                }
                            },
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.Red),
                            border = androidx.compose.foundation.BorderStroke(1.dp, Color.Red.copy(alpha = 0.5f))
                        ) {
                            Text("Cancelar ou Gerenciar Assinatura", fontSize = 12.sp, fontWeight = FontWeight.Bold)
                        }
                    } else {
                        Button(
                            onClick = onOpenPremium,
                            modifier = Modifier.fillMaxWidth(),
                            shape = RoundedCornerShape(10.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = orangeButtonColor)
                        ) {
                            Text("👑 ASSINAR O PLANO PREMIUM", fontSize = 12.sp, fontWeight = FontWeight.Bold, color = Color.White)
                        }
                    }
                }
            }

            // --- SEÇÃO LEGAL E SUPORTE ---
            Text(
                text = "SUPORTE E LEGAL",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = goldColor
            )

            SettingsItemCard(
                icon = Icons.Default.Lock,
                iconTint = Color(0xFF3498DB),
                title = "Política de Privacidade e Termos",
                subtitle = "Consulte nossos compromissos de segurança",
                cardBackgroundColor = cardBackgroundColor,
                onClick = {
                    val url = "https://fernandasaliba.github.io/privacy-policy/"
                    val intent = Intent(Intent.ACTION_VIEW, url.toUri())
                    context.startActivity(intent)
                }
            )

            SettingsItemCard(
                icon = Icons.Default.Info,
                iconTint = Color(0xFF2ECC71),
                title = "Central de Ajuda / Suporte",
                subtitle = "Fale conosco ou tire dúvidas frequentes",
                cardBackgroundColor = cardBackgroundColor,
                onClick = {
                    val intent = Intent(Intent.ACTION_SENDTO).apply {
                        data = "mailto:suporte@rodertech.com?subject=Suporte%20BusinessScan".toUri()
                    }
                    try {
                        context.startActivity(intent)
                    } catch (_: Exception) {
                        Toast.makeText(context, "Nenhum aplicativo de e-mail encontrado.", Toast.LENGTH_SHORT).show()
                    }
                }
            )

            Spacer(modifier = Modifier.height(8.dp))

            // --- SEÇÃO DE ZONA DE PERIGO (EXCLUSÃO DE CONTA) ---
            Text(
                text = "Excluir conta",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFE74C3C)
            )

            OutlinedButton(
                onClick = { showDeleteDialog = true },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color(0xFFE74C3C)),
                border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFE74C3C).copy(alpha = 0.5f))
            ) {
                Icon(imageVector = Icons.Default.Delete, contentDescription = null, modifier = Modifier.size(18.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Text("Excluir Minha Conta Permanentemente", fontWeight = FontWeight.Bold, fontSize = 13.sp)
            }

            Spacer(modifier = Modifier.height(16.dp))

            // Rodapé com versão
            Text(
                text = "BusinessScan • Versão 1.0.0 (Build 1)",
                color = Color.Gray,
                fontSize = 11.sp,
                modifier = Modifier.align(Alignment.CenterHorizontally)
            )

            Spacer(modifier = Modifier.height(24.dp))
        }
    }

    // Diálogo de Confirmação de Exclusão
    if (showDeleteDialog) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("Excluir Conta", fontWeight = FontWeight.Bold) },
            text = { Text("Tem certeza absoluta que deseja excluir sua conta permanentemente? Todos os dados associados serão apagados e esta ação não poderá ser desfeita.") },
            confirmButton = {
                TextButton(
                    onClick = {
                        showDeleteDialog = false
                        coroutineScope.launch {
                            authManager.deleteAccount(
                                onSuccess = {
                                    Toast.makeText(context, "Conta excluída com sucesso.", Toast.LENGTH_SHORT).show()
                                    onAccountDeleted()
                                },
                                onError = { errorMessage ->
                                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                                }
                            )
                        }
                    }
                ) {
                    Text("Sim, Excluir", color = Color(0xFFE74C3C), fontWeight = FontWeight.Bold)
                }
            },
            dismissButton = {
                TextButton(onClick = { showDeleteDialog = false }) {
                    Text("Cancelar", color = Color.White)
                }
            },
            containerColor = cardBackgroundColor,
            titleContentColor = Color.White,
            textContentColor = Color.LightGray
        )
    }
}

@Composable
fun SettingsItemCard(
    icon: ImageVector,
    iconTint: Color,
    title: String,
    subtitle: String,
    cardBackgroundColor: Color,
    onClick: () -> Unit
) {
    Card(
        onClick = onClick,
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = cardBackgroundColor),
        modifier = Modifier.fillMaxWidth()
    ) {
        Row(
            modifier = Modifier
                .padding(16.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Surface(
                color = iconTint.copy(alpha = 0.2f),
                shape = RoundedCornerShape(12.dp),
                modifier = Modifier.size(44.dp)
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(imageVector = icon, contentDescription = null, tint = iconTint)
                }
            }
            Spacer(modifier = Modifier.width(14.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(text = title, fontSize = 14.sp, fontWeight = FontWeight.Bold, color = Color.White)
                Spacer(modifier = Modifier.height(2.dp))
                Text(text = subtitle, fontSize = 11.sp, color = Color.LightGray)
            }
        }
    }
}