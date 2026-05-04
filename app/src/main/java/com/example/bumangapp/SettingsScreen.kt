package com.example.bumangapp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(navController: NavController) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Ajustes") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Volver")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .padding(innerPadding)
                .fillMaxSize()
                .padding(16.dp)
        ) {
            Text(text = "Opciones de configuración", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(16.dp))
            ListItem(
                headlineContent = { Text("Cambiar Contraseña") },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            ListItem(
                headlineContent = { Text("Tamaño de Texto") },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            ListItem(
                headlineContent = { Text("Privacidad") },
                modifier = Modifier.padding(vertical = 4.dp)
            )
            Spacer(modifier = Modifier.weight(1f))
            Button(
                onClick = {
                    // Cerrar sesión simulado
                    navController.navigate("welcome") {
                        popUpTo("main_menu") { inclusive = true }
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text("Cerrar Sesión")
            }
        }
    }
}
