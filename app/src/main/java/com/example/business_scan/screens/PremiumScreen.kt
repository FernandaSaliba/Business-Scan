package com.example.business_scan.screens

import android.widget.Toast
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.business_scan.model.Business

enum class PlanoType { MENSAL, ANUAL }

@Composable
fun PremiumScreen(
    business: Business? = null, // 👈 Parâmetro adicionado para receber a empresa selecionada
    onBackClick: () -> Unit = {},
    onSubscribeSuccess: () -> Unit = {}
) {
    val context = LocalContext.current

    // 1. Estado para controlar qual plano está selecionado (Anual por padrão)
    var planoSelecionado by remember { mutableStateOf(PlanoType.ANUAL) }
    var isLoading by remember { mutableStateOf(false) }

    Box(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFF0F172A))
    ) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(20.dp)
                .verticalScroll(rememberScrollState())
        ) {
            Spacer(modifier = Modifier.height(12.dp))

            // Botão de Voltar (Seta)
            IconButton(
                onClick = onBackClick,
                modifier = Modifier
                    .size(42.dp)
                    .background(Color(0xFF1E293B), RoundedCornerShape(12.dp))
            ) {
                Icon(
                    imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                    contentDescription = "Voltar",
                    tint = Color.White
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // Título
            Text(
                text = "Desbloqueie o BusinessScan Pro 👑",
                fontSize = 24.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
            )

            Spacer(modifier = Modifier.height(20.dp))


// 👇 CÓDIGO DA OPÇÃO 1 APLICADO AQUI
            business?.let { emp ->
                Spacer(modifier = Modifier.height(8.dp))
                Surface(
                    color = Color(0xFF1E293B),
                    shape = RoundedCornerShape(8.dp),
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Text(
                            text = "Relatório para: ${emp.name}",
                            fontSize = 13.sp,
                            fontWeight = FontWeight.Bold,
                            color = Color(0xFF818CF8)
                        )
                        if (emp.cnpj.isNotEmpty()) {
                            Text(
                                text = "CNPJ: ${emp.cnpj}",
                                fontSize = 11.sp,
                                color = Color.Gray
                            )
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(20.dp))

            // --- PLANO MENSAL ---
            PlanOptionCard(
                title = "Plano Mensal",
                subtitle = "Cancele a qualquer momento",
                price = "R$ 29,90/mês",
                isSelected = planoSelecionado == PlanoType.MENSAL,
                onClick = { planoSelecionado = PlanoType.MENSAL }
            )

            Spacer(modifier = Modifier.height(12.dp))

            // --- PLANO ANUAL ---
            PlanOptionCard(
                title = "Plano Anual (Economize 35%)",
                subtitle = "Faturado R$ 238,80 anualmente",
                price = "R$ 19,90/mês",
                badgeText = "MAIS POPULAR",
                isSelected = planoSelecionado == PlanoType.ANUAL,
                onClick = { planoSelecionado = PlanoType.ANUAL }
            )

            Spacer(modifier = Modifier.height(24.dp))

            // Card de Vantagens
            Card(
                shape = RoundedCornerShape(16.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1E293B)),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(18.dp)) {
                    Text(
                        text = "Tudo o que você terá acesso:",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                    Spacer(modifier = Modifier.height(12.dp))

                    BenefitRow(text = "Quadro de Sócios e Administradores (QSA)")
                    BenefitRow(text = "Capital Social e Faturamento Estimado")
                    BenefitRow(text = "Score de Risco Fiscal e Histórico de Alertas")
                    BenefitRow(text = "Exportação de relatórios em PDF")
                    BenefitRow(text = "Consultas ilimitadas sem restrição diária")
                }
            }

            Spacer(modifier = Modifier.height(28.dp))

            // --- BOTÃO DE ASSINATURA (SANDBOX / PRODUÇÃO) ---
            Button(
                onClick = {
                    isLoading = true
                    // Simulação do checkout da Sandbox (Mercado Pago / Stripe / Play Billing)
                    Toast.makeText(
                        context,
                        "Processando assinatura ${planoSelecionado.name}...",
                        Toast.LENGTH_SHORT
                    ).show()

                    // Simula resposta da API de pagamento com atraso
                    android.os.Handler(android.os.Looper.getMainLooper()).postDelayed({
                        isLoading = false
                        Toast.makeText(context, "🎉 Assinatura Pro ativada com sucesso!", Toast.LENGTH_LONG).show()
                        onSubscribeSuccess() // Executa a navegação de sucesso (Ativa o isPremium = true)
                    }, 1500)
                },
                enabled = !isLoading,
                shape = RoundedCornerShape(14.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color.Transparent),
                contentPadding = PaddingValues(),
                modifier = Modifier
                    .fillMaxWidth()
                    .height(54.dp)
                    .background(
                        brush = Brush.horizontalGradient(
                            colors = listOf(
                                Color(0xFFD97706),
                                Color(0xFFB45309)
                            )
                        ),
                        shape = RoundedCornerShape(14.dp)
                    )
            ) {
                if (isLoading) {
                    CircularProgressIndicator(color = Color.White, modifier = Modifier.size(24.dp))
                } else {
                    Text(
                        text = "ASSINAR AGORA (TESTE SANDBOX)",
                        fontWeight = FontWeight.Bold,
                        fontSize = 14.sp,
                        color = Color.White
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            // Rodapé informativo de segurança
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.Center,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(text = "🔒 ", fontSize = 12.sp)
                Text(
                    text = "Pagamento 100% seguro via Sandbox Mercado Pago/Stripe",
                    fontSize = 11.sp,
                    color = Color.Gray
                )
            }
        }
    }
}

// ====================================================================
// COMPONENTES AUXILIARES
// ====================================================================

@Composable
fun PlanOptionCard(
    title: String,
    subtitle: String,
    price: String,
    badgeText: String? = null,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    val borderColor = if (isSelected) Color(0xFF818CF8) else Color.Transparent
    val backgroundColor = if (isSelected) Color(0xFF312E81).copy(alpha = 0.5f) else Color(0xFF1E293B)

    Card(
        shape = RoundedCornerShape(16.dp),
        colors = CardDefaults.cardColors(containerColor = backgroundColor),
        modifier = Modifier
            .fillMaxWidth()
            .border(width = if (isSelected) 2.dp else 0.dp, color = borderColor, shape = RoundedCornerShape(16.dp))
            .clickable { onClick() }
    ) {
        Column(modifier = Modifier.padding(16.dp)) {
            if (badgeText != null) {
                Surface(
                    color = Color(0xFFD97706),
                    shape = RoundedCornerShape(6.dp),
                    modifier = Modifier.padding(bottom = 6.dp)
                ) {
                    Text(
                        text = badgeText,
                        color = Color.White,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold,
                        modifier = Modifier.padding(horizontal = 6.dp, vertical = 2.dp)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = title,
                        fontWeight = FontWeight.Bold,
                        fontSize = 15.sp,
                        color = Color.White
                    )
                    Text(
                        text = subtitle,
                        fontSize = 11.sp,
                        color = Color.LightGray
                    )
                }

                Text(
                    text = price,
                    fontWeight = FontWeight.ExtraBold,
                    fontSize = 15.sp,
                    color = Color(0xFFFBBF24)
                )
            }
        }
    }
}

@Composable
fun BenefitRow(text: String) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier.padding(vertical = 4.dp)
    ) {
        Icon(
            imageVector = Icons.Default.CheckCircle,
            contentDescription = null,
            tint = Color(0xFF10B981),
            modifier = Modifier.size(18.dp)
        )
        Spacer(modifier = Modifier.width(10.dp))
        Text(
            text = text,
            fontSize = 12.sp,
            color = Color.LightGray
        )
    }
}

