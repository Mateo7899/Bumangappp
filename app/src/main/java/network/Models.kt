package com.example.bumangapp.network

// Representa la respuesta general {"success": true, "data": [...]}
data class ApiResponse(
    val success: Boolean,
    val data: List<BusRoute>
)

// Representa cada ruta de bus
data class BusRoute(
    val id: Int,
    val name: String,
    val color_hex: String?,
    val coordinates: List<Coordinate>
)

// Representa cada punto en el mapa de esa ruta
data class Coordinate(
    val id: Int,
    val latitude: Double,
    val longitude: Double,
    val order_index: Int
)

// --- ESTRUCTURAS PARA AUTENTICACIÓN ---

// Lo que enviamos desde Android a Laravel
data class LoginRequest(val email: String, val password: String)

data class RegisterRequest(val name: String, val email: String, val password: String)

// Lo que Laravel nos responde
data class AuthResponse(
    val success: Boolean,
    val message: String,
    val user: UserData? = null // Puede ser null si falla el login
)

// Los datos del usuario que vienen en la respuesta
data class UserData(
    val name: String,
    val email: String,
    val is_premium: Int
)