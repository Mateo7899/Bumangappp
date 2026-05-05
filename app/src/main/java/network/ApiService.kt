package com.example.bumangapp.network

import retrofit2.http.Body
import retrofit2.http.GET
import retrofit2.http.POST

interface ApiService {

    // La que ya teníamos
    @GET("api/rutas")
    suspend fun getRutas(): ApiResponse

    // --- NUEVAS RUTAS DE AUTENTICACIÓN ---

    // Le enviamos un LoginRequest y nos devuelve un AuthResponse
    @POST("api/login")
    suspend fun login(@Body request: Map<String, String>): AuthResponse

    // Le enviamos un RegisterRequest y nos devuelve un AuthResponse
    @POST("api/register")
    suspend fun register(@Body request: RegisterRequest): AuthResponse

    @POST("api/update-password")
    suspend fun updatePassword(@Body request: Map<String, String>): AuthResponse
}