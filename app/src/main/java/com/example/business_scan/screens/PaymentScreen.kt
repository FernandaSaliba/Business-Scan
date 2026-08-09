package com.example.businessscan.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.business_scan.model.Business
import java.text.NumberFormat
import java.util.Locale

@Composable
fun PaymentScreen(
    empresaDetalhada: Business? = null,
    onBack: () -> Unit = {}
) {
    val scrollState = rememberScrollState()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp)
            .verticalScroll(scrollState),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            TextButton(onClick = onBack) {
                Text("← Voltar")
            }
            Surface(
                color = Color(0xFFFFD700),
                shape = MaterialTheme.shapes.small
            ) {
                Text(
                    text = "PREMIUM",
                    color = Color.Black,
                    fontWeight = FontWeight.Bold,
                    fontSize = 12.sp,
                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp)
                )
            }
        }

        Spacer(modifier = Modifier.height(16.dp))

        Text(
            text = "Relatório Avançado",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold
        )

        Spacer(modifier = Modifier.height(24.dp))

        // Se houver uma empresa selecionada/buscada, mostra o cartão completo
        if (empresaDetalhada != null && empresaDetalhada.cnpj.isNotEmpty() && empresaDetalhada.cnpj != "N/A") {
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
                border = BorderStroke(1.dp, MaterialTheme.colorScheme.primary),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = empresaDetalhada.name,
                        style = MaterialTheme.typography.titleLarge,
                        fontWeight = FontWeight.Bold
                    )
                    if (empresaDetalhada.nomeFantasia.isNotEmpty()) {
                        Text(
                            text = "Nome Fantasia: ${empresaDetalhada.nomeFantasia}",
                            style = MaterialTheme.typography.bodyMedium,
                            color = Color.Gray
                        )
                    }

                    HorizontalDivider(modifier = Modifier.padding(vertical = 12.dp))

                    ItemDetalhe(rotulo = "CNPJ", valor = empresaDetalhada.cnpj)
                    ItemDetalhe(rotulo = "Situação Cadastral", valor = empresaDetalhada.motivoAlerta)

                    if (empresaDetalhada.cnae.isNotEmpty()) {
                        ItemDetalhe(
                            rotulo = "Atividade Principal (CNAE)",
                            valor = empresaDetalhada.cnae
                        )
                    }

                    if (empresaDetalhada.capitalSocial > 0.0) {
                        val formatadorMoeda = NumberFormat.getCurrencyInstance(Locale("pt", "BR"))
                        ItemDetalhe(
                            rotulo = "Capital Social",
                            valor = formatadorMoeda.format(empresaDetalhada.capitalSocial)
                        )
                    }

                    if (empresaDetalhada.endereco.isNotEmpty()) {
                        ItemDetalhe(
                            rotulo = "Localização",
                            valor = "${empresaDetalhada.endereco} - ${empresaDetalhada.municipio}/${empresaDetalhada.uf}"
                        )
                    }
                }
            }
        } else {
            // Cartão de demonstração dos benefícios Premium
            Card(
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.primaryContainer),
                modifier = Modifier.fillMaxWidth()
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        text = "🔓 Com o Premium você libera:",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    Text("• Busca por Nome / Razão Social")
                    Text("• Capital Social e Saúde Financeira")
                    Text("• Endereço Completo e Sócios (CNAE)")
                    Text("• Alertas de Encerramento e Fraudes")
                }
            }
        }

        Spacer(modifier = Modifier.height(32.dp))

        // Card de Assinatura / Plano
        Card(
            border = BorderStroke(2.dp, MaterialTheme.colorScheme.primary),
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(20.dp),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Plano Pro Anual", fontWeight = FontWeight.Bold, fontSize = 18.sp)
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    "R$ 19,90 / mês",
                    style = MaterialTheme.typography.headlineSmall,
                    fontWeight = FontWeight.ExtraBold,
                    color = MaterialTheme.colorScheme.primary
                )

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { /* Lógica de checkout/pagamento */ },
                    modifier = Modifier.fillMaxWidth().height(50.dp)
                ) {
                    Text("Assinar Agora e Liberar TUDO")
                }
            }
        }
    }
}

@Composable
fun ItemDetalhe(
    rotulo: String,
    valor: String
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 4.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(text = rotulo, style = MaterialTheme.typography.bodyMedium, color = Color.Gray)
        Text(text = valor, style = MaterialTheme.typography.bodyMedium, fontWeight = FontWeight.SemiBold)
    }
}

