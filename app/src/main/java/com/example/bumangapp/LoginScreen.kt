package com.example.bumangapp

import android.util.Log
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Visibility
import androidx.compose.material.icons.filled.VisibilityOff
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import com.example.bumangapp.network.LoginRequest
import com.example.bumangapp.network.RetrofitClient
import com.example.bumangapp.data.SessionManager
import kotlinx.coroutines.launch

// Asegúrate de tener importados tus archivos de Retrofit y SessionManager
// import com.tu.paquete.SessionManager
// import com.tu.paquete.RetrofitClient
// import com.tu.paquete.LoginRequest

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(navController: NavController) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var passwordVisible by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()
    val context = LocalContext.current
    val sessionManager = remember { SessionManager(context) }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6)) // Fondo gris claro
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo de Bus
        Icon(
            imageVector = Icons.Default.DirectionsBus,
            contentDescription = "Logo",
            modifier = Modifier.size(80.dp),
            tint = Color.Gray
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Títulos
        Text(
            text = "BumangApp",
            fontSize = 28.sp,
            fontWeight = FontWeight.Bold,
            color = Color.Black
        )
        Text(
            text = "INICIO DE SESIÓN",
            fontSize = 14.sp,
            color = Color.Gray,
            letterSpacing = 2.sp
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Campo Correo
        TextField(
            value = email,
            onValueChange = { email = it },
            placeholder = { Text("Correo electrónico") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE5E7EB),
                unfocusedContainerColor = Color(0xFFE5E7EB),
                focusedIndicatorColor = Color.Transparent, // Quita la línea de abajo al escribir
                unfocusedIndicatorColor = Color.Transparent // Quita la línea de abajo inactiva
            )
        )

        Spacer(modifier = Modifier.height(16.dp))

        // Campo Contraseña con OJO
        TextField(
            value = password,
            onValueChange = { password = it },
            placeholder = { Text("Contraseña") },
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(12.dp),
            visualTransformation = if (passwordVisible) VisualTransformation.None else PasswordVisualTransformation(),
            trailingIcon = {
                val image = if (passwordVisible) Icons.Filled.Visibility else Icons.Filled.VisibilityOff
                IconButton(onClick = { passwordVisible = !passwordVisible }) {
                    Icon(imageVector = image, contentDescription = "Mostrar contraseña")
                }
            },
            colors = TextFieldDefaults.colors(
                focusedContainerColor = Color(0xFFE5E7EB),
                unfocusedContainerColor = Color(0xFFE5E7EB),
                focusedIndicatorColor = Color.Transparent,
                unfocusedIndicatorColor = Color.Transparent
            )
        )

        Spacer(modifier = Modifier.height(32.dp))

        // Botón Iniciar Sesión
        Button(
            onClick = {
                coroutineScope.launch {
                    try {
                        // Cambia "LoginRequest" si el tuyo se llama diferente
                        val response = RetrofitClient.apiService.login(
                            LoginRequest(
                                email,
                                password
                            )
                        )
                        if (response.success) {
                            val saveLoginState = sessionManager.saveLoginState(true)
                            navController.navigate("main_menu") {
                                popUpTo("login") { inclusive = true }
                            }
                        }
                    } catch (e: Exception) {
                        Log.e("BUMANGAPP_LOGIN", "Error: ${e.message}")
                    }
                }
            },
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)) // Rojo vibrante
        ) {
            Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold, color = Color.White)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón hacia Registro
        TextButton(onClick = { navController.navigate("register") }) {
            Text("¿No tienes una cuenta? Regístrate", color = Color.DarkGray)
        }
    }
}
