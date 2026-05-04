package com.example.bumangapp

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.navigation.NavController
import android.util.Log
import androidx.compose.foundation.gestures.snapping.SnapPosition.Center.position
import androidx.compose.runtime.LaunchedEffect
import com.example.bumangapp.network.RetrofitClient
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.GoogleMap
import com.google.maps.android.compose.rememberCameraPositionState
import androidx.compose.runtime.*
import androidx.compose.ui.graphics.Color
import com.example.bumangapp.network.BusRoute
import com.google.maps.android.compose.Polyline

// ... (dentro de tu @Composable MainMenuScreen) ...



// ... (aquí sigue el resto de tu código visual de la pantalla) ...
// ... (Tus imports arriba, asegúrate de tener estos)
// ... (Tus otros imports arriba)
import android.Manifest
import android.content.pm.PackageManager
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.material.icons.filled.DirectionsBus
import androidx.compose.material.icons.filled.Map
import androidx.compose.material.icons.filled.Star
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import com.google.maps.android.compose.MapProperties
import com.google.maps.android.compose.MapUiSettings
import com.google.maps.android.compose.Marker
import com.google.maps.android.compose.MarkerState


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavController) {
    val context = LocalContext.current

    // 1. ESTADOS PARA LA NAVEGACIÓN
    var selectedIndex by remember { mutableIntStateOf(0) }
    val items = listOf("Mapa", "Rutas", "Premium", "Ajustes")
    val icons = listOf(Icons.Default.Map, Icons.Default.DirectionsBus, Icons.Default.Star, Icons.Default.Settings)

    // 2. ESTADOS PARA EL MAPA Y PERMISOS
    var hasLocationPermission by remember {
        mutableStateOf(
            ContextCompat.checkSelfPermission(
                context,
                Manifest.permission.ACCESS_FINE_LOCATION
            ) == PackageManager.PERMISSION_GRANTED
        )
    }

    val launcher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.RequestPermission(),
        onResult = { granted -> hasLocationPermission = granted }
    )

    LaunchedEffect(Unit) {
        if (!hasLocationPermission) {
            launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    // 3. CONFIGURACIÓN DEL MAPA (Bucaramanga como centro)
    val bucaramanga = LatLng(7.1193, -73.1227)
    val cameraPositionState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(bucaramanga, 13f)
    }

    // 4. ESTRUCTURA PRINCIPAL (SCAFFOLD)
    Scaffold(
        bottomBar = {
            NavigationBar(
                containerColor = Color.White,
                tonalElevation = 8.dp
            ) {
                items.forEachIndexed { index, item ->
                    NavigationBarItem(
                        icon = { Icon(icons[index], contentDescription = item) },
                        label = { Text(item) },
                        selected = selectedIndex == index,
                        onClick = { selectedIndex = index },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = Color.White,
                            selectedTextColor = Color(0xFFDC2626), // Tu rojo visual
                            indicatorColor = Color(0xFFDC2626),
                            unselectedIconColor = Color.Gray,
                            unselectedTextColor = Color.Gray
                        )
                    )
                }
            }
        }
    ) { paddingValues ->
        // Contenedor que cambia según la pestaña
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when (selectedIndex) {
                0 -> { // PESTAÑA: MAPA
                    if (hasLocationPermission) {
                        GoogleMap(
                            modifier = Modifier.fillMaxSize(),
                            cameraPositionState = cameraPositionState,
                            properties = MapProperties(isMyLocationEnabled = true),
                            uiSettings = MapUiSettings(myLocationButtonEnabled = true)
                        ) {
                            // Aquí puedes añadir tus Markers o Polylines que ya tenías
                            Marker(
                                state = MarkerState(position = bucaramanga),
                                title = "Bucaramanga",
                                snippet = "Centro de la ciudad"
                            )
                        }
                    } else {
                        // Mensaje si no hay permisos
                        Column(
                            modifier = Modifier.fillMaxSize(),
                            verticalArrangement = Arrangement.Center,
                            horizontalAlignment = Alignment.CenterHorizontally
                        ) {
                            Text("Necesitamos permiso de ubicación para mostrar el mapa")
                            Button(onClick = { launcher.launch(Manifest.permission.ACCESS_FINE_LOCATION) }) {
                                Text("Dar permiso")
                            }
                        }
                    }
                }

                1 -> { // PESTAÑA: RUTAS
                    PantallaProvisional("Explora las Rutas de Bus", "Próximamente: Horarios y recorridos detallados.")
                }

                2 -> { // PESTAÑA: PREMIUM
                    PantallaProvisional("Hazte Premium", "Disfruta de BumangApp sin anuncios y con rutas offline.")
                }

                3 -> { // PESTAÑA: AJUSTES
                    Column(
                        modifier = Modifier.fillMaxSize().padding(16.dp),
                        horizontalAlignment = Alignment.CenterHorizontally,
                        verticalArrangement = Arrangement.Center
                    ) {
                        Text("Ajustes", fontSize = 24.sp, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(24.dp))

                        // Botón de Cerrar Sesión con tu estilo rojo
                        Button(
                            onClick = {
                                // Aquí podrías limpiar el SessionManager antes de salir
                                navController.navigate("login") {
                                    popUpTo("main_menu") { inclusive = true }
                                }
                            },
                            modifier = Modifier.fillMaxWidth().height(50.dp),
                            colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                            shape = androidx.compose.foundation.shape.RoundedCornerShape(25.dp)
                        ) {
                            Text("CERRAR SESIÓN", color = Color.White, fontWeight = FontWeight.Bold)
                        }
                    }
                }
            }
        }
    }
}

// Un pequeño componente para no dejar las pestañas vacías mientras las creamos
@Composable
fun PantallaProvisional(titulo: String, subtitulo: String) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(titulo, fontSize = 22.sp, fontWeight = FontWeight.Bold, color = Color.Black)
        Spacer(modifier = Modifier.height(8.dp))
        Text(subtitulo, fontSize = 16.sp, color = Color.Gray, textAlign = androidx.compose.ui.text.style.TextAlign.Center)
    }
}
