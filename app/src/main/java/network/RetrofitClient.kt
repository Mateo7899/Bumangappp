package com.example.bumangapp // Asegúrate de que el package coincida con tus otros archivos

import com.example.bumangapp.network.ApiService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import kotlin.jvm.java

object RetrofitClient {
    private const val BASE_URL = "http://10.0.2.2:8000/"

    // DEBE LLAMARSE 'instance' Y TENER EL TIPO 'ApiService'
    val instance: ApiService by lazy {
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
            .create(ApiService::class.java)
    }
}