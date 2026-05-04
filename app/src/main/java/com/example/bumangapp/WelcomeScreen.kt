package com.example.bumangapp

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import androidx.compose.ui.text.TextStyle


@Composable
fun WelcomeScreen(navController: NavController) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .background(Color(0xFFF3F4F6)) // Fondo gris claro
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        // Logo circular con icono de bus
        Surface(
            modifier = Modifier.size(120.dp),
            shape = CircleShape,
            color = Color(0xFF6B7280), // Gris del logo
            shadowElevation = 8.dp
        ) {
            Icon(
                imageVector = Icons.Default.DirectionsBus,
                contentDescription = null,
                modifier = Modifier.padding(20.dp).fillMaxSize(),
                tint = Color.White
            )
        }

        Spacer(modifier = Modifier.height(32.dp))

        Text(
            text = "BumangApp",
            style = TextStyle(
                fontSize = 32.sp,
                fontWeight = FontWeight.Bold,
                fontFamily = FontFamily.SansSerif
            )
        )
        Text(
            text = "BIENVENIDO",
            style = TextStyle(fontSize = 18.sp, color = Color.Gray, letterSpacing = 2.sp)
        )

        Spacer(modifier = Modifier.height(48.dp))

        // Botón Iniciar Sesión
        Button(
            onClick = { navController.navigate("login") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
            Text("INICIAR SESIÓN", fontWeight = FontWeight.Bold)
        }

        Spacer(modifier = Modifier.height(16.dp))

        // Botón Registrarse
        Button(
            onClick = { navController.navigate("register") },
            modifier = Modifier.fillMaxWidth().height(56.dp),
            shape = RoundedCornerShape(28.dp),
            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
        ) {
            Text("REGISTRARSE", fontWeight = FontWeight.Bold)
        }
    }
}
