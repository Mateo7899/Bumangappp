package com.example.bumangapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.platform.LocalContext
import androidx.lifecycle.viewmodel.compose.viewModel
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
    val appViewModel: AppStateViewModel = viewModel()

    val sessionManager = SessionManager(context = LocalContext.current)
    val isLoggedIn by sessionManager.isLoggedIn.collectAsState(initial = null)
    val userEmail by sessionManager.userEmail.collectAsState(initial = null) // null = aún cargando

    // Cargar email en el viewModel cuando esté disponible
    LaunchedEffect(userEmail) {
        if (!userEmail.isNullOrEmpty()) {
            appViewModel.emailUsuario = userEmail!!
        }
    }

    // Esperamos AMBOS valores antes de dibujar
    if (isLoggedIn != null && userEmail != null) {
        NavHost(
            navController = navController,
            startDestination = if (isLoggedIn == true) "main_menu" else "welcome"
        ) {
            composable(route = "welcome") { WelcomeScreen(navController) }
            composable(route = "login") { LoginScreen(navController, appViewModel) }
            composable(route = "register") { RegisterScreen(navController) }
            composable(route = "main_menu") { MainMenuScreen(navController, appViewModel) }
            composable(route = "settings") { SettingsScreen(navController) }
        }
    }
}
