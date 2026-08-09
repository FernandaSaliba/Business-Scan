package com.example.business_scan.model

import com.google.gson.annotations.SerializedName

data class EmpresaResponse(
    @SerializedName("cnpj") val cnpj: String? = "",
    @SerializedName("razao_social") val razaoSocial: String? = "",
    @SerializedName("nome_fantasia") val nomeFantasia: String? = "",
    @SerializedName("descricao_situacao_cadastral") val descricaoSituacaoCadastral: String? = "",
    @SerializedName("cnae_fiscal_descricao") val cnaeFiscalDescricao: String? = "",
    @SerializedName("capital_social") val capitalSocial: Double? = 0.0,
    @SerializedName("logradouro") val logradouro: String? = "",
    @SerializedName("numero") val numero: String? = "",
    @SerializedName("uf") val uf: String? = "",
    @SerializedName("municipio") val municipio: String? = "",

    // 🟢 Mapeamento do Quadro de Sócios
    @SerializedName("qsa") val qsa: List<SocioResponse>? = emptyList()
) {
    // Função de conversão para o modelo Business
    fun toBusiness(): Business {
        val enderecoFormatado = listOfNotNull(logradouro, numero)
            .filter { it.isNotBlank() }
            .joinToString(", ")

        return Business(
            cnpj = cnpj ?: "",
            razaoSocial = razaoSocial ?: "",
            nomeFantasia = nomeFantasia ?: "",
            situacaoCadastral = descricaoSituacaoCadastral ?: "",
            cnae = cnaeFiscalDescricao ?: "",
            capitalSocial = capitalSocial ?: 0.0,
            endereco = enderecoFormatado,
            uf = uf ?: "",
            municipio = municipio ?: "",
            qsa = qsa?.map {
                Socio(
                    nome = it.nomeSocio ?: "Não informado",
                    cargo = it.qualificacaoSocio ?: "Não informado"
                )
            } ?: emptyList()
        )
    }
}

// 🟢 Classe auxiliar para representar cada sócio retornado pela Brasil API
data class SocioResponse(
    @SerializedName("nome_socio_razao_social", alternate = ["nome_socio"])
    val nomeSocio: String? = "",

    @SerializedName("qualificacao_socio")
    val qualificacaoSocio: String? = ""
)
