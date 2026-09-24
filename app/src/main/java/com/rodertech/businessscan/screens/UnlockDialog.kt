package com.rodertech.businessscan.screens

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Lock
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.WorkspacePremium
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog

private val dialogBg = Color(0xFF1E293B)
private val darkBg = Color(0xFF0F172A)
private val accentColor = Color(0xFF818CF8)
private val buttonGold = Color(0xFFD97706)

@Composable
fun UnlockDialog(
    featureTitle: String,
    onDismiss: () -> Unit,
    onWatchAd: () -> Unit,
    onGoPremium: () -> Unit
) {
    Dialog(onDismissRequest = onDismiss) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp),
            shape = RoundedCornerShape(20.dp),
            colors = CardDefaults.cardColors(containerColor = dialogBg)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.spacedBy(16.dp)
            ) {
                // Ícone de Cadeado/Bloqueio Estilizado
                Box(
                    modifier = Modifier
                        .size(56.dp)
                        .background(darkBg, RoundedCornerShape(16.dp)),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.Default.Lock,
                        contentDescription = null,
                        tint = buttonGold,
                        modifier = Modifier.size(28.dp)
                    )
                }

                Text(
                    text = "Desbloquear $featureTitle",
                    fontSize = 18.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.White,
                    textAlign = TextAlign.Center
                )

                Text(
                    text = "Escolha como deseja prosseguir para utilizar esta funcionalidade Pro:",
                    fontSize = 13.sp,
                    color = Color.LightGray,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(4.dp))

                // Botão para Assistir Anúncio
                Button(
                    onClick = {
                        onDismiss()
                        onWatchAd()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = accentColor)
                ) {
                    Icon(
                        imageVector = Icons.Default.PlayCircle,
                        contentDescription = null,
                        tint = Color.White
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Assistir Anúncio (Grátis)",
                        fontWeight = FontWeight.Bold,
                        color = Color.White,
                        fontSize = 14.sp
                    )
                }

                // Botão para Ser Premium
                OutlinedButton(
                    onClick = {
                        onDismiss()
                        onGoPremium()
                    },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = buttonGold),
                    border = androidx.compose.foundation.BorderStroke(1.dp, buttonGold)
                ) {
                    Icon(
                        imageVector = Icons.Default.WorkspacePremium,
                        contentDescription = null,
                        tint = buttonGold
                    )
                    Spacer(modifier = Modifier.width(8.dp))
                    Text(
                        text = "Seja Premium (Ilimitado)",
                        fontWeight = FontWeight.Bold,
                        color = buttonGold,
                        fontSize = 14.sp
                    )
                }

                TextButton(onClick = onDismiss) {
                    Text(text = "Cancelar", color = Color.Gray, fontSize = 12.sp)
                }
            }
        }
    }
}

