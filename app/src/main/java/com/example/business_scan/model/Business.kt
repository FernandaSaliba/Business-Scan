package com.example.business_scan.model

import com.google.gson.annotations.SerializedName
import java.text.NumberFormat
import java.util.Locale

data class Socio(
    @SerializedName("nome_socio_razao_social", alternate = ["nome_socio"])
    val nome: String = "",

    @SerializedName("qualificacao_socio", alternate = ["cargo"])
    val cargo: String = ""
)

data class Business(
    @SerializedName("cnpj")
    val cnpj: String = "",

    @SerializedName("razao_social")
    val razaoSocial: String = "",

    @SerializedName("nome_fantasia")
    val nomeFantasia: String = "",

    @SerializedName("descricao_situacao_cadastral")
    val situacaoCadastral: String = "",

    @SerializedName("cnae_fiscal_descricao")
    val cnae: String = "",

    @SerializedName("capital_social")
    val capitalSocial: Double = 0.0,

    @SerializedName("logradouro")
    val endereco: String = "",

    @SerializedName("uf")
    val uf: String = "",

    @SerializedName("municipio")
    val municipio: String = "",

    @SerializedName("qsa")
    val qsa: List<Socio> = emptyList()
) {
    val name: String
        get() = razaoSocial.ifEmpty { nomeFantasia }

    val motivoAlerta: String
        get() = situacaoCadastral

    val capitalSocialFormatado: String
        get() {
            val format = NumberFormat.getCurrencyInstance(Locale.forLanguageTag("pt-BR"))
            return format.format(capitalSocial)
        }

    val estimativaFaturamento: String
        get() = when {
            capitalSocial == 0.0 -> "Não informado"
            capitalSocial <= 81000 -> "MEI (Até R$ 81 mil/ano)"
            capitalSocial <= 360000 -> "Microempresa - ME (Até R$ 360 mil/ano)"
            capitalSocial <= 4800000 -> "Pequeno Porte - EPP (Até R$ 4,8 mi/ano)"
            else -> "Médio / Grande Porte (+ R$ 4,8 mi/ano)"
        }

    @get:Suppress("Unused")
    val scoreRisco: String
        get() = if (situacaoCadastral.equals("ATIVA", ignoreCase = true)) {
            "Baixo Risco (Situação Cadastral Regular)"
        } else {
            "Alto Risco / Alerta Cadastral (${situacaoCadastral.uppercase()})"
        }
}

