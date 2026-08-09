package com.example.business_scan.network



import com.example.business_scan.model.EmpresaResponse // ou o modelo DTO que você usa para a resposta da BrasilAPI

import retrofit2.Response

import retrofit2.http.GET

import retrofit2.http.Path



interface BrasilApiService {

    @GET("api/cnpj/v1/{cnpj}")

    suspend fun buscarCnpj(@Path("cnpj") cnpj: String): Response<EmpresaResponse>

}

