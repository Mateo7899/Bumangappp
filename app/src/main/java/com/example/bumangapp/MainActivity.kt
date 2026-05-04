package com.example.bumangapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.bumangapp.data.SessionManager
import com.example.bumangapp.ui.theme.BumangappTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            BumangappTheme {
                BumangAppNavigation()
            }
        }
    }
}

@Composable
fun BumangAppNavigation() {
    val navController = rememberNavController()
    // 1. Leemos el estado
    val sessionManager = SessionManager(context = LocalContext.current)
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = null)

// 2. Esperamos a saber el estado antes de dibujar
    if (isLoggedIn != null) {
        // 3. UN SOLO NavHost que contiene TODAS tus pantallas
        NavHost(
            navController = navController,
            // Si está logueado va al mapa, si no, lo mandamos a la pantalla de bienvenida
            startDestination = if (isLoggedIn == true) "main_menu" else "welcome"
        ) {
            composable(route = "welcome") { WelcomeScreen(navController) }
            composable(route = "login") { LoginScreen(navController) }
            composable(route = "register") { RegisterScreen(navController) }
            composable(route = "main_menu") { MainMenuScreen(navController) }
            composable(route = "settings") { SettingsScreen(navController) }
        }
    }
}
