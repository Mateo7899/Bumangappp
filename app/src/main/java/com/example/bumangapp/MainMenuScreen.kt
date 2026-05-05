package com.example.bumangapp

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavController
import com.google.android.gms.maps.model.BitmapDescriptor
import com.google.android.gms.maps.model.BitmapDescriptorFactory
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

// --- CEREBRO DE LA APP ---
class AppStateViewModel : ViewModel() {
    var isPremium by mutableStateOf(false)
    var textScale by mutableFloatStateOf(16f)
    var emailUsuario by mutableStateOf("")
}

// --- MODELOS ---
data class RutaBus(val id: String, val destino: String, val color: Color, val puntos: List<LatLng>, val horario: String)
data class BusReal(val id: String, val rutaId: String, val cond: String, val placa: String, var pos: LatLng, var ind: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MainMenuScreen(navController: NavController, viewModel: AppStateViewModel) {
    var tab by remember { mutableIntStateOf(0) }
    var rutaSeleccionada by remember { mutableStateOf<RutaBus?>(null) }

    Scaffold(
        bottomBar = {
            NavigationBar(containerColor = Color.White, tonalElevation = 8.dp) {
                val menus = listOf("Mapa", "Rutas", "Premium", "Ajustes")
                val icons = listOf(Icons.Default.Map, Icons.Default.DirectionsBus, Icons.Default.Star, Icons.Default.Settings)
                menus.forEachIndexed { i, label ->
                    NavigationBarItem(
                        selected = tab == i,
                        onClick = { tab = i; rutaSeleccionada = null },
                        icon = { Icon(icons[i], contentDescription = null, tint = if (tab == i) Color(0xFFDC2626) else Color.Gray) },
                        label = { Text(label, fontSize = 11.sp, fontWeight = FontWeight.Medium) }
                    )
                }
            }
        }
    ) { p ->
        Box(Modifier.padding(p).background(Color(0xFFF3F4F6))) {
            when (tab) {
                0 -> VistaMapa(viewModel)
                1 -> if (rutaSeleccionada == null) VistaListaRutas(viewModel) { rutaSeleccionada = it }
                else VistaDetalleRuta(rutaSeleccionada!!) { rutaSeleccionada = null }
                2 -> VistaPremium(viewModel)
                3 -> VistaAjustes(navController, viewModel)
            }
        }
    }
}

// ===================== 10 RUTAS REALES AMB BUCARAMANGA =====================
fun getRutasAMB(): List<RutaBus> = listOf(

    // RUTA 1 - GALÁN MODELO ESTADIO UIS
    RutaBus("R1", "Galán - Modelo - Estadio - UIS", Color(0xFFE53935), listOf(
        LatLng(7.1190, -73.1280), // Galán - Chimitá
        LatLng(7.1100, -73.1180), // Campo Hermoso
        LatLng(7.0980, -73.1100), // Carrera 15
        LatLng(7.0900, -73.1050), // Calle 33 - Carrera 19
        LatLng(7.0820, -73.1010), // Centro - Calle 8
        LatLng(7.0760, -73.0980), // Carrera 24
        LatLng(7.0720, -73.0960), // Parque Estación UIS
        LatLng(7.0780, -73.0990), // Carrera 26
        LatLng(7.0850, -73.1020), // Calle 34 - Carrera 33
        LatLng(7.1000, -73.1100), // Abastos
        LatLng(7.1190, -73.1280)  // Galán regreso
    ), "5:00 AM - 8:00 PM"),

    // RUTA 2 - CRISTAL BAJO - INEM - MORRORICO - BUENAVISTA
    RutaBus("R2", "Cristal Bajo - INEM - Morrorico - Buenavista", Color(0xFF8E24AA), listOf(
        LatLng(7.1320, -73.1240), // Cristal Bajo
        LatLng(7.1250, -73.1200), // Carrera 22
        LatLng(7.1150, -73.1160), // Terminal de Transportes
        LatLng(7.0950, -73.1050), // Puente El Bueno - Transversal Met.
        LatLng(7.0800, -73.0980), // Canelos - Carrera 8
        LatLng(7.0720, -73.0940), // Diagonal 15 - Carrera 15
        LatLng(7.0650, -73.0910), // Calle 33
        LatLng(7.0580, -73.0870), // Acueducto - La Flora
        LatLng(7.0520, -73.0840), // Buenavista
        LatLng(7.0560, -73.0860), // Morrorico
        LatLng(7.0700, -73.0930), // Carrera 33 - Zona Transferencia
        LatLng(7.1000, -73.1080), // Terminal regreso
        LatLng(7.1320, -73.1240)  // Cristal Bajo regreso
    ), "5:00 AM - 8:00 PM"),

    // RUTA 3 - CRISTAL BAJO - DIAMANTE II - ESTACIÓN PROVENZA - BUENAVISTA
    RutaBus("R3", "Cristal Bajo - Provenza - Cacique - Buenavista", Color(0xFF039BE5), listOf(
        LatLng(7.1320, -73.1240), // Cristal Bajo
        LatLng(7.1200, -73.1180), // Carrera 22 - Calle 100
        LatLng(7.1050, -73.1120), // Puente Fontana - Carrera 19
        LatLng(7.0950, -73.1060), // Calle 90 - Carrera 17
        LatLng(7.0870, -73.1010), // Paralela Provenza
        LatLng(7.0810, -73.0990), // Estación Provenza Occ.
        LatLng(7.0780, -73.0960), // Puente Provenza
        LatLng(7.0740, -73.0940), // Estación Provenza Oriental
        LatLng(7.0690, -73.0910), // CC Cacique
        LatLng(7.0620, -73.0870), // Carrera 33 - Megamall
        LatLng(7.0560, -73.0850), // Morrorico - Buenavista
        LatLng(7.0620, -73.0870), // Albania - Álvarez regreso
        LatLng(7.0730, -73.0930), // Viaducto La Flora
        LatLng(7.1050, -73.1120), // San Luis
        LatLng(7.1320, -73.1240)  // Cristal Bajo
    ), "5:00 AM - 8:00 PM"),

    // RUTA 5 - CARRIZAL - CABECERA - PORTÓN DEL TEJAR - CAMPANAZO
    RutaBus("R5", "Carrizal - Cabecera - Portón Tejar - Campanazo", Color(0xFFFF6F00), listOf(
        LatLng(7.1380, -73.1300), // Carrizal - San Antonio
        LatLng(7.1270, -73.1240), // Chimitá - Calle 45
        LatLng(7.1150, -73.1160), // Carrera 9 - Calle 37
        LatLng(7.1050, -73.1100), // Carrera 22 - Calle 45
        LatLng(7.0950, -73.1050), // Carrera 33 - Conucos
        LatLng(7.0870, -73.0990), // Portón del Tejar
        LatLng(7.0820, -73.0950), // Transversal Oriental
        LatLng(7.0760, -73.0910), // El Campanazo
        LatLng(7.0820, -73.0950), // Calle 61 - Carrera 60
        LatLng(7.0900, -73.1010), // CC Cacique regreso
        LatLng(7.1150, -73.1160), // Carrera 33 regreso
        LatLng(7.1380, -73.1300)  // Carrizal
    ), "5:00 AM - 8:00 PM"),

    // RUTA 9 - TRINIDAD - TERRAZAS - AV. QUEBRADASECA - SAN MIGUEL
    RutaBus("R9", "Trinidad - Terrazas - Av. Quebradaseca - San Miguel", Color(0xFF00897B), listOf(
        LatLng(7.0830, -73.0960), // Parqueadero Unitransa - Trans. Oriental
        LatLng(7.0900, -73.0990), // Calle 48 El Carmen
        LatLng(7.0960, -73.1010), // Villa Luz - Diagonal 17
        LatLng(7.1020, -73.1040), // Calle 53 - Cancha Alares
        LatLng(7.1080, -73.1070), // Calle 58 - Carrera 14C
        LatLng(7.1140, -73.1110), // El Campanazo
        LatLng(7.1080, -73.1060), // Hacienda San Juan
        LatLng(7.1010, -73.1020), // Lagos del Cacique
        LatLng(7.0950, -73.0990), // Terrazas - Carrera 44
        LatLng(7.0880, -73.0960), // Diagonal 56 - Carrera 36
        LatLng(7.0800, -73.0930), // Av. Quebradaseca
        LatLng(7.0740, -73.0900), // Calle 45 - Carrera 8
        LatLng(7.0800, -73.0930), // Diagonal 15 - Carrera 16
        LatLng(7.0850, -73.0960), // Carrera 33 - Calle 57
        LatLng(7.0830, -73.0960)  // Regreso Parqueadero
    ), "5:00 AM - 8:00 PM"),

    // RUTA 11 - MIRADOR DE ARENALES - BAHONDO - CARRERA 33 - UIS
    RutaBus("R11", "Mirador Arenales - Bahondo - Carrera 33 - UIS", Color(0xFFC0392B), listOf(
        LatLng(7.0300, -73.0700), // Mirador de Arenales
        LatLng(7.0380, -73.0760), // Bahondo - Carrera 26
        LatLng(7.0450, -73.0800), // Av. Los Caneyes - Rincón de Girón
        LatLng(7.0550, -73.0850), // Vía Girón-Bucaramanga
        LatLng(7.0640, -73.0890), // Calle 70 - Calle 67
        LatLng(7.0700, -73.0930), // Carrera 33 - Calle 14
        LatLng(7.0750, -73.0950), // Carrera 30 - Calle 10
        LatLng(7.0720, -73.0940), // Glorieta Caballo de Bolívar
        LatLng(7.0700, -73.0930), // Estación Parque UIS
        LatLng(7.0720, -73.0940), // Carrera 25 - Calle 9
        LatLng(7.0640, -73.0890), // Regreso Carrera 33
        LatLng(7.0450, -73.0800), // Vía Girón
        LatLng(7.0300, -73.0700)  // Mirador Arenales
    ), "5:00 AM - 8:00 PM"),

    // RUTA 16 - HAMACAS - CARRERA 33 - REPOSO
    RutaBus("R16", "Hamacas - Carrera 33 - Reposo", Color(0xFF6D4C41), listOf(
        LatLng(7.1450, -73.1350), // Betania Etapa 11
        LatLng(7.1380, -73.1300), // Carrera 10 - Café Madrid
        LatLng(7.1300, -73.1250), // Hamacas - Kennedy
        LatLng(7.1200, -73.1190), // Hospital del Norte
        LatLng(7.1100, -73.1140), // Los Cuyos - Carrera 15
        LatLng(7.0950, -73.1060), // Av. Quebradaseca
        LatLng(7.0850, -73.1010), // Carrera 33
        LatLng(7.0720, -73.0940), // Viaducto La Flora - CC Cacique
        LatLng(7.0780, -73.0960), // Transversal Oriental - Campanazo
        LatLng(7.0840, -73.0970), // Cancha Alares
        LatLng(7.0780, -73.0960), // Regreso CC Cacique
        LatLng(7.0850, -73.1010), // Carrera 33
        LatLng(7.1100, -73.1140), // Av. Quebradaseca regreso
        LatLng(7.1450, -73.1350)  // Betania
    ), "5:00 AM - 8:00 PM"),

    // RUTA 21 - NORTE CAFÉ - ESTORAQUES
    RutaBus("R21", "Norte Café - Estoraques - Ciudad Norte", Color(0xFF1565C0), listOf(
        LatLng(7.1500, -73.1380), // Colorados
        LatLng(7.1420, -73.1340), // Café Madrid - Kennedy
        LatLng(7.1300, -73.1260), // Hospital del Norte - Portal del Norte
        LatLng(7.1150, -73.1170), // Carrera 15 - Calle 11
        LatLng(7.0920, -73.1040), // Parque Estación UIS
        LatLng(7.0850, -73.1010), // Carrera 30 - Av. Quebradaseca
        LatLng(7.0780, -73.0960), // Carrera 33 - Calle 56
        LatLng(7.0700, -73.0920), // Plaza Mayor
        LatLng(7.0640, -73.0880), // Marsella - Carrera 3
        LatLng(7.0610, -73.0860), // Estoraques - Carrera 8W
        LatLng(7.0660, -73.0890), // Regreso - Plaza Mayor
        LatLng(7.0750, -73.0940), // Hospital Universitario
        LatLng(7.0920, -73.1040), // Carrera 25 - Calle 9
        LatLng(7.1300, -73.1260), // Kennedy
        LatLng(7.1500, -73.1380)  // Colorados
    ), "4:40 AM - 6:20 PM"),

    // RUTA 26 - CUMBRE - CARRERA 33 - SAN TOTO
    RutaBus("R26", "Cumbre - El Carmen - Carrera 33 - San Toto", Color(0xFF558B2F), listOf(
        LatLng(7.0820, -73.0920), // Villa Helena Sur
        LatLng(7.0770, -73.0940), // La Cumbre
        LatLng(7.0720, -73.0930), // Transversal Oriental - El Carmen
        LatLng(7.0700, -73.0930), // Carrera 33
        LatLng(7.0720, -73.0940), // Carrera 33A - Calle 14
        LatLng(7.0750, -73.0950), // Carrera 25 - Calle 9
        LatLng(7.0780, -73.0960), // Carrera 18 - Calle 14
        LatLng(7.0720, -73.0940), // Viaducto La Flora
        LatLng(7.0680, -73.0910), // CC Cacique
        LatLng(7.0720, -73.0930), // Transversal Oriental regreso
        LatLng(7.0820, -73.0920)  // Villa Helena Sur
    ), "5:00 AM - 8:30 PM"),

    // RUTA 30 - MIRADOR DE ARENALES - POBLADO - CENTRO - CARRERA 22
    RutaBus("R30", "Mirador Arenales - Poblado - Centro - Cra 22", Color(0xFFAD1457), listOf(
        LatLng(7.0300, -73.0700), // Ciudadela Nuevo Girón
        LatLng(7.0380, -73.0760), // Mirador de Arenales
        LatLng(7.0420, -73.0790), // Carrera 26 - Puente Lenguerke
        LatLng(7.0480, -73.0820), // Carrera 23 - Calle 44
        LatLng(7.0530, -73.0840), // Glorieta El Poblado
        LatLng(7.0600, -73.0870), // Vía Girón-Bucaramanga
        LatLng(7.0660, -73.0900), // Intercambiador Puerta del Sol
        LatLng(7.0720, -73.0930), // Diagonal 15 - Carrera 21
        LatLng(7.0790, -73.0960), // Carrera 15 - Boulevard Santander
        LatLng(7.0840, -73.0980), // Carrera 17 - Calle 8
        LatLng(7.0870, -73.0990), // Carrera 22 - Calle 57
        LatLng(7.0840, -73.0980), // Regreso Intercambiador
        LatLng(7.0530, -73.0840), // El Poblado
        LatLng(7.0380, -73.0760), // Mirador Arenales
        LatLng(7.0300, -73.0700)  // Ciudadela Girón
    ), "5:00 AM - 11:00 PM")
)

@Composable
fun VistaMapa(vm: AppStateViewModel) {
    val ctx = LocalContext.current
    var icon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var sel by remember { mutableStateOf<BusReal?>(null) }

    val rutas = remember { getRutasAMB() }

    // CLAVE: los estados de marcador deben vivir fuera del LaunchedEffect
    // y actualizarse directamente para que Compose los detecte
    val busStates = remember {
        rutas.mapIndexed { i, r ->
            BusReal("B${r.id}", r.id, "Conductor ${i + 1}", "BUM-${200 + i}", r.puntos[0], 0)
        }.toMutableStateList()
    }

    val camState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(7.0800, -73.1000), 12f)
    }

    // Mapa de MarkerState por bus, para poder moverlos reactivamente
    val markerStates = remember {
        busStates.associate { b -> b.id to MarkerState(position = b.pos) }
    }

    LaunchedEffect(Unit) {
        val bm = Bitmap.createBitmap(90, 90, Bitmap.Config.ARGB_8888)
        val can = Canvas(bm)
        ContextCompat.getDrawable(ctx, R.drawable.autobus)?.apply {
            setBounds(0, 0, 90, 90)
            draw(can)
        }
        icon = BitmapDescriptorFactory.fromBitmap(bm)

        while (true) {
            delay(10_000)
            busStates.forEachIndexed { idx, b ->
                val r = rutas.find { it.id == b.rutaId }!!
                val nextInd = (b.ind + 1) % r.puntos.size
                val nextPos = r.puntos[nextInd]
                // Actualizar el MarkerState directamente → Compose lo detecta
                markerStates[b.id]?.position = nextPos
                busStates[idx] = b.copy(pos = nextPos, ind = nextInd)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = camState
        ) {
            // SIN polilíneas, SIN leyenda — solo buses
            busStates.forEach { b ->
                markerStates[b.id]?.let { markerState ->
                    Marker(
                        state = markerState,
                        icon = icon,
                        onClick = { sel = b; true }
                    )
                }
            }
        }
    }

    if (sel != null) {
        val rutaInfo = rutas.find { it.id == sel!!.rutaId }
        AlertDialog(
            onDismissRequest = { sel = null },
            confirmButton = {
                TextButton(onClick = { sel = null }) {
                    Text("ENTENDIDO", color = Color(0xFFDC2626))
                }
            },
            icon = { Icon(Icons.Default.DirectionsBus, null, tint = Color(0xFFDC2626)) },
            title = { Text("Bus en Movimiento - ${sel!!.rutaId}") },
            text = {
                Column {
                    Text("Placa: ${sel!!.placa}", fontWeight = FontWeight.Bold)
                    Text("Conductor: ${sel!!.cond}")
                    Text("Ruta: ${rutaInfo?.destino ?: ""}", fontSize = 13.sp)
                    Text("Horario: ${rutaInfo?.horario ?: ""}", color = Color.Gray, fontSize = 12.sp)
                    Text("Velocidad estimada: 35 km/h", color = Color.Gray, fontSize = 12.sp)
                }
            }
        )
    }
}

@Composable
fun VistaDetalleRuta(ruta: RutaBus, onBack: () -> Unit) {
    // Marcadores A y B con íconos de colores distintos
    val puntoA = ruta.puntos.first()
    val puntoB = ruta.puntos.last()

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(puntoA, 13f)
            }
        ) {
            // Línea de la ruta
            Polyline(points = ruta.puntos, color = ruta.color, width = 12f)

            // Punto A — Inicio (verde)
            Marker(
                state = rememberMarkerState(position = puntoA),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                title = "A - Inicio",
                snippet = ruta.destino.substringBefore(" - ")
            )

            // Punto B — Final (rojo)
            Marker(
                state = rememberMarkerState(position = puntoB),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                title = "B - Final",
                snippet = ruta.destino.substringAfterLast(" - ")
            )
        }

        // Botón volver
        Button(
            onClick = onBack,
            modifier = Modifier.padding(16.dp).align(Alignment.TopStart),
            colors = ButtonDefaults.buttonColors(containerColor = Color.White),
            elevation = ButtonDefaults.buttonElevation(6.dp)
        ) {
            Icon(Icons.Default.ArrowBack, null, tint = Color.Black)
            Spacer(Modifier.width(5.dp))
            Text("VOLVER", color = Color.Black, fontWeight = FontWeight.Bold)
        }

        // Card info ruta con leyenda A y B
        Card(
            modifier = Modifier
                .align(Alignment.BottomCenter)
                .padding(16.dp)
                .fillMaxWidth(),
            colors = CardDefaults.cardColors(containerColor = Color.White),
            elevation = CardDefaults.cardElevation(6.dp)
        ) {
            Column(Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Box(
                        Modifier.size(40.dp).background(ruta.color, RoundedCornerShape(8.dp)),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(ruta.id, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
                    }
                    Spacer(Modifier.width(12.dp))
                    Column {
                        Text(ruta.destino, fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        Text("Horario: ${ruta.horario}", color = Color.Gray, fontSize = 12.sp)
                    }
                }
                Spacer(Modifier.height(10.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                    // Etiqueta A
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(24.dp).background(Color(0xFF43A047), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("A", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(6.dp))
                        Text("Inicio", fontSize = 13.sp, color = Color.DarkGray)
                    }
                    // Etiqueta B
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(24.dp).background(Color(0xFFDC2626), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("B", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(6.dp))
                        Text("Final", fontSize = 13.sp, color = Color.DarkGray)
                    }
                }
            }
        }
    }
}

@Composable
fun VistaAjustes(navController: NavController, vm: AppStateViewModel) {
    var dialogPw by remember { mutableStateOf(false) }
    var nuevaPass by remember { mutableStateOf("") }
    var mensajePw by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier
                .fillMaxWidth()
                .height(180.dp)
                .background(
                    Brush.verticalGradient(
                        listOf(Color(0xFFDC2626), Color(0xFFB91C1C))
                    )
                )
        )

        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier
                    .fillMaxWidth()
                    .height(180.dp)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ajustes", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(
                    vm.emailUsuario,
                    color = Color.White.copy(alpha = 0.8f),
                    fontSize = 13.sp
                )
            }

            Column(
                Modifier
                    .fillMaxSize()
                    .background(Color(0xFFF3F4F6))
                    .padding(16.dp)
            ) {
                Spacer(Modifier.height(8.dp))

                Text(
                    "SEGURIDAD",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    Modifier.fillMaxWidth().clickable { dialogPw = true },
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(40.dp).background(Color(0xFFFFEBEB), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Lock, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Cambiar contraseña", fontWeight = FontWeight.SemiBold, fontSize = vm.textScale.sp)
                            Text("Actualiza tu clave de acceso", color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                    }
                }

                Spacer(Modifier.height(24.dp))

                Text(
                    "INTERFAZ",
                    fontSize = 11.sp,
                    fontWeight = FontWeight.Bold,
                    color = Color.Gray,
                    letterSpacing = 1.5.sp,
                    modifier = Modifier.padding(start = 4.dp, bottom = 8.dp)
                )

                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White),
                    elevation = CardDefaults.cardElevation(2.dp)
                ) {
                    Column(Modifier.padding(16.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Box(
                                Modifier.size(40.dp).background(Color(0xFFFFEBEB), RoundedCornerShape(10.dp)),
                                contentAlignment = Alignment.Center
                            ) {
                                Icon(Icons.Default.TextFields, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp))
                            }
                            Spacer(Modifier.width(14.dp))
                            Column {
                                Text("Tamaño de texto", fontWeight = FontWeight.SemiBold, fontSize = vm.textScale.sp)
                                Text("Ajusta el tamaño de la interfaz", color = Color.Gray, fontSize = 12.sp)
                            }
                        }
                        Spacer(Modifier.height(14.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            listOf("Pequeño" to 13f, "Mediano" to 17f, "Grande" to 22f).forEach { (label, size) ->
                                Button(
                                    onClick = { vm.textScale = size },
                                    modifier = Modifier.weight(1f).height(38.dp),
                                    shape = RoundedCornerShape(10.dp),
                                    colors = ButtonDefaults.buttonColors(
                                        containerColor = if (vm.textScale == size) Color(0xFFDC2626) else Color(0xFFF3F4F6)
                                    )
                                ) {
                                    Text(label, fontSize = 12.sp, color = if (vm.textScale == size) Color.White else Color.DarkGray)
                                }
                            }
                        }
                    }
                }

                Spacer(Modifier.weight(1f))

                Button(
                    onClick = {
                        vm.emailUsuario = "" // ← CORRECTO
                        navController.navigate("login") {
                            popUpTo("main_menu") { inclusive = true }
                        }
                    },
                    modifier = Modifier.fillMaxWidth().height(54.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.ExitToApp, null, tint = Color.White)
                    Spacer(Modifier.width(10.dp))
                    Text("CERRAR SESIÓN", fontWeight = FontWeight.Bold, color = Color.White)
                }

                Spacer(Modifier.height(8.dp))
            }
        }
    }

    if (dialogPw) {
        AlertDialog(
            onDismissRequest = {
                if (!cargando) {
                    dialogPw = false
                    nuevaPass = ""
                    mensajePw = ""
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevaPass.length < 6) {
                            mensajePw = "Mínimo 6 caracteres"
                            return@Button
                        }
                        cargando = true
                        coroutineScope.launch {
                            android.util.Log.e("BUMANGAPP_PW", "Email enviado: '${vm.emailUsuario}'")
                            try {
                                val response = RetrofitClient.instance.updatePassword(
                                    mapOf(
                                        "email" to vm.emailUsuario, // AHORA USA EL EMAIL REAL
                                        "new_password" to nuevaPass
                                    )
                                )
                                mensajePw = if (response.success) "✓ Contraseña actualizada" else response.message
                            } catch (e: Exception) {
                                mensajePw = "Error de conexión"
                                android.util.Log.e("BUMANGAPP_PW", "Error: ${e.message} - ${e.cause}")
                            } finally {
                                cargando = false
                            }
                        }
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    if (cargando) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("GUARDAR")
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    dialogPw = false
                    nuevaPass = ""
                    mensajePw = ""
                }) { Text("CANCELAR", color = Color.Gray) }
            },
            title = { Text("Cambiar contraseña", fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    OutlinedTextField(
                        value = nuevaPass,
                        onValueChange = { nuevaPass = it; mensajePw = "" },
                        label = { Text("Nueva contraseña") },
                        visualTransformation = androidx.compose.ui.text.input.PasswordVisualTransformation(),
                        modifier = Modifier.fillMaxWidth(),
                        singleLine = true,
                        colors = OutlinedTextFieldDefaults.colors(
                            focusedBorderColor = Color(0xFFDC2626),
                            focusedLabelColor = Color(0xFFDC2626)
                        )
                    )
                    if (mensajePw.isNotEmpty()) {
                        Text(
                            mensajePw,
                            color = if (mensajePw.startsWith("✓")) Color(0xFF16A34A) else Color.Red,
                            fontSize = 13.sp,
                            modifier = Modifier.padding(top = 6.dp)
                        )
                    }
                }
            }
        )
    }
}

@Composable
fun VistaPremium(vm: AppStateViewModel) {
    val bgGradient = Brush.verticalGradient(listOf(Color(0xFF1F2937), Color(0xFF111827)))

    Column(Modifier.fillMaxSize().background(bgGradient).padding(30.dp), Arrangement.Center, Alignment.CenterHorizontally) {
        Icon(Icons.Default.Star, null, tint = Color(0xFFFFD700), modifier = Modifier.size(120.dp))
        Text(
            if (vm.isPremium) "¡ERES MIEMBRO GOLD!" else "MEJORA A PREMIUM",
            color = Color.White, fontSize = (vm.textScale + 6).sp, fontWeight = FontWeight.ExtraBold, textAlign = TextAlign.Center
        )
        Text(
            "Acceso ilimitado a rutas HD, reportes de tráfico en tiempo real y cero anuncios.",
            color = Color.LightGray, fontSize = 14.sp, textAlign = TextAlign.Center, modifier = Modifier.padding(vertical = 15.dp)
        )

        if (!vm.isPremium) {
            Button(
                onClick = { vm.isPremium = true },
                modifier = Modifier.fillMaxWidth().height(55.dp),
                colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626)),
                shape = RoundedCornerShape(12.dp)
            ) {
                Text("ADQUIRIR POR $9.900/mes", fontWeight = FontWeight.Bold)
            }
        } else {
            Surface(color = Color(0xFF10B981), shape = RoundedCornerShape(8.dp)) {
                Text("SUSCRIPCIÓN ACTIVA", color = Color.White, modifier = Modifier.padding(horizontal = 15.dp, vertical = 5.dp), fontWeight = FontWeight.Bold)
            }
        }
    }
}

@Composable
fun VistaListaRutas(vm: AppStateViewModel, onSel: (RutaBus) -> Unit) {
    val rutas = remember { getRutasAMB() }
    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Rutas Disponibles", fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.padding(bottom = 15.dp))
        LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
            items(rutas) { ruta ->
                Card(
                    Modifier.fillMaxWidth().clickable { onSel(ruta) },
                    elevation = CardDefaults.cardElevation(4.dp),
                    colors = CardDefaults.cardColors(containerColor = Color.White)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Surface(color = ruta.color, shape = RoundedCornerShape(8.dp)) {
                            Text(ruta.id, color = Color.White, modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp), fontWeight = FontWeight.Bold)
                        }
                        Spacer(Modifier.width(15.dp))
                        Column {
                            Text(ruta.destino, fontWeight = FontWeight.Bold, fontSize = vm.textScale.sp)
                            Text("Horario: ${ruta.horario}", color = Color.Gray, fontSize = 12.sp)
                        }
                        Spacer(Modifier.weight(1f))
                        Icon(Icons.Default.ChevronRight, null, tint = Color.Gray)
                    }
                }
            }
        }
    }
}
