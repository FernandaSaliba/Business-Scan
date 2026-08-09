package com.example.business_scan.network



import retrofit2.Retrofit

import retrofit2.converter.gson.GsonConverterFactory



object RetrofitClient {

    private const val BASE_URL = "https://brasilapi.com.br/"



// Alterado de ApiService para BrasilApiService

    val apiService: BrasilApiService by lazy {

        Retrofit.Builder()

            .baseUrl(BASE_URL)

            .addConverterFactory(GsonConverterFactory.create())

            .build()

            .create(BrasilApiService::class.java)

    }

}

