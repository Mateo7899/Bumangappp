package com.example.bumangapp

import android.graphics.Bitmap
import android.graphics.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.navigation.NavController
import com.example.bumangapp.data.SessionManager
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

fun parseColor(hex: String?): Color {
    return try {
        Color(android.graphics.Color.parseColor(hex ?: "#FF0000"))
    } catch (e: Exception) {
        Color(0xFFDC2626)
    }
}

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

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun VistaMapa(vm: AppStateViewModel) {
    val ctx = LocalContext.current
    var icon by remember { mutableStateOf<BitmapDescriptor?>(null) }
    var sel by remember { mutableStateOf<BusReal?>(null) }
    var rutas by remember { mutableStateOf<List<RutaBus>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    val busStates = remember { mutableStateListOf<BusReal>() }
    val markerStates = remember { mutableMapOf<String, MarkerState>() }

    val camState = rememberCameraPositionState {
        position = CameraPosition.fromLatLngZoom(LatLng(7.0800, -73.1000), 12f)
    }

    LaunchedEffect(Unit) {
        val bm = Bitmap.createBitmap(90, 90, Bitmap.Config.ARGB_8888)
        val can = Canvas(bm)
        ContextCompat.getDrawable(ctx, R.drawable.autobus)?.apply {
            setBounds(0, 0, 90, 90)
            draw(can)
        }
        icon = BitmapDescriptorFactory.fromBitmap(bm)

        try {
            val response = RetrofitClient.instance.getRutas()
            if (response.success) {
                val rutasConvertidas = response.data.map { busRoute ->
                    RutaBus(
                        id = "R${busRoute.id}",
                        destino = busRoute.name,
                        color = parseColor(busRoute.color_hex),
                        puntos = busRoute.coordinates
                            .sortedBy { it.order_index }
                            .map { LatLng(it.latitude, it.longitude) },
                        horario = "5:00 AM - 8:00 PM"
                    )
                }
                rutas = rutasConvertidas
                rutasConvertidas.forEachIndexed { i, r ->
                    if (r.puntos.isNotEmpty()) {
                        val bus = BusReal("B${r.id}", r.id, "Conductor ${i + 1}", "BUM-${200 + i}", r.puntos[0], 0)
                        busStates.add(bus)
                        markerStates[bus.id] = MarkerState(position = r.puntos[0])
                    }
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("BUMANGAPP_RUTAS", "Error cargando rutas: ${e.message}")
        }
        cargando = false

        while (true) {
            delay(10_000)
            busStates.forEachIndexed { idx, b ->
                val r = rutas.find { it.id == b.rutaId } ?: return@forEachIndexed
                if (r.puntos.isEmpty()) return@forEachIndexed
                val nextInd = (b.ind + 1) % r.puntos.size
                val nextPos = r.puntos[nextInd]
                markerStates[b.id]?.position = nextPos
                busStates[idx] = b.copy(pos = nextPos, ind = nextInd)
            }
        }
    }

    Box(Modifier.fillMaxSize()) {
        GoogleMap(modifier = Modifier.fillMaxSize(), cameraPositionState = camState) {
            busStates.forEach { b ->
                markerStates[b.id]?.let { markerState ->
                    Marker(state = markerState, icon = icon, onClick = { sel = b; true })
                }
            }
        }

        if (cargando) {
            Box(
                Modifier.fillMaxSize().background(Color.Black.copy(alpha = 0.3f)),
                contentAlignment = Alignment.Center
            ) {
                Card(shape = RoundedCornerShape(12.dp), colors = CardDefaults.cardColors(containerColor = Color.White)) {
                    Row(Modifier.padding(20.dp), verticalAlignment = Alignment.CenterVertically) {
                        CircularProgressIndicator(color = Color(0xFFDC2626), modifier = Modifier.size(24.dp), strokeWidth = 3.dp)
                        Spacer(Modifier.width(12.dp))
                        Text("Cargando rutas...", fontWeight = FontWeight.Medium)
                    }
                }
            }
        }
    }

    if (sel != null) {
        val rutaInfo = rutas.find { it.id == sel!!.rutaId }
        val sheetState = rememberModalBottomSheetState(skipPartiallyExpanded = true)

        ModalBottomSheet(
            onDismissRequest = { sel = null },
            sheetState = sheetState,
            containerColor = Color.White,
            shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp)
        ) {
            Column(Modifier.fillMaxWidth().padding(horizontal = 24.dp).padding(bottom = 32.dp)) {
                Box(
                    Modifier.fillMaxWidth().clip(RoundedCornerShape(16.dp))
                        .background(
                            Brush.horizontalGradient(
                                listOf(
                                    rutaInfo?.color ?: Color(0xFFDC2626),
                                    (rutaInfo?.color ?: Color(0xFFDC2626)).copy(alpha = 0.7f)
                                )
                            )
                        ).padding(20.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(56.dp).background(Color.White.copy(alpha = 0.2f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.DirectionsBus, null, tint = Color.White, modifier = Modifier.size(32.dp))
                        }
                        Spacer(Modifier.width(16.dp))
                        Column {
                            Text("Bus ${sel!!.rutaId}", color = Color.White, fontSize = 20.sp, fontWeight = FontWeight.Bold)
                            Text(rutaInfo?.destino ?: "", color = Color.White.copy(alpha = 0.85f), fontSize = 12.sp, maxLines = 1)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(10.dp)) {
                    listOf(
                        Triple(Icons.Default.Badge, "Placa", sel!!.placa),
                        Triple(Icons.Default.Person, "Conductor", sel!!.cond),
                        Triple(Icons.Default.Speed, "Velocidad", "35 km/h")
                    ).forEach { (icono, label, valor) ->
                        Card(
                            Modifier.weight(1f),
                            shape = RoundedCornerShape(12.dp),
                            colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                            elevation = CardDefaults.cardElevation(0.dp)
                        ) {
                            Column(Modifier.padding(14.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                                Icon(icono, null, tint = Color(0xFFDC2626), modifier = Modifier.size(22.dp))
                                Spacer(Modifier.height(6.dp))
                                Text(label, color = Color.Gray, fontSize = 11.sp)
                                Text(valor, fontWeight = FontWeight.Bold, fontSize = 13.sp)
                            }
                        }
                    }
                }

                Spacer(Modifier.height(12.dp))

                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFFF3F4F6)),
                    elevation = CardDefaults.cardElevation(0.dp)
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(40.dp).background(Color(0xFFFFEBEB), RoundedCornerShape(10.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(Icons.Default.Schedule, null, tint = Color(0xFFDC2626), modifier = Modifier.size(22.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text("Horario de operación", color = Color.Gray, fontSize = 12.sp)
                            Text(rutaInfo?.horario ?: "5:00 AM - 8:00 PM", fontWeight = FontWeight.Bold, fontSize = 14.sp)
                        }
                    }
                }

                Spacer(Modifier.height(20.dp))

                Button(
                    onClick = { sel = null },
                    modifier = Modifier.fillMaxWidth().height(50.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Text("CERRAR", fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}

@Composable
fun VistaDetalleRuta(ruta: RutaBus, onBack: () -> Unit) {
    val puntoA = ruta.puntos.first()
    val puntoB = ruta.puntos.last()

    Box(Modifier.fillMaxSize()) {
        GoogleMap(
            modifier = Modifier.fillMaxSize(),
            cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(puntoA, 13f)
            }
        ) {
            Polyline(points = ruta.puntos, color = ruta.color, width = 12f)
            Marker(
                state = rememberMarkerState(position = puntoA),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN),
                title = "A - Inicio",
                snippet = ruta.destino.substringBefore(" - ")
            )
            Marker(
                state = rememberMarkerState(position = puntoB),
                icon = BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_RED),
                title = "B - Final",
                snippet = ruta.destino.substringAfterLast(" - ")
            )
        }

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

        Card(
            modifier = Modifier.align(Alignment.BottomCenter).padding(16.dp).fillMaxWidth(),
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
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(24.dp).background(Color(0xFF43A047), RoundedCornerShape(4.dp)),
                            contentAlignment = Alignment.Center
                        ) { Text("A", color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.Bold) }
                        Spacer(Modifier.width(6.dp))
                        Text("Inicio", fontSize = 13.sp, color = Color.DarkGray)
                    }
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
    val ctx = LocalContext.current
    var dialogPw by remember { mutableStateOf(false) }
    var nuevaPass by remember { mutableStateOf("") }
    var mensajePw by remember { mutableStateOf("") }
    var cargando by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Box(Modifier.fillMaxSize()) {
        Box(
            Modifier.fillMaxWidth().height(180.dp).background(
                Brush.verticalGradient(listOf(Color(0xFFDC2626), Color(0xFFB91C1C)))
            )
        )
        Column(Modifier.fillMaxSize()) {
            Column(
                Modifier.fillMaxWidth().height(180.dp).padding(24.dp),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                Text("Ajustes", color = Color.White, fontSize = 26.sp, fontWeight = FontWeight.Bold)
                Text(vm.emailUsuario, color = Color.White.copy(alpha = 0.8f), fontSize = 13.sp)
            }
            Column(Modifier.fillMaxSize().background(Color(0xFFF3F4F6)).padding(16.dp)) {
                Spacer(Modifier.height(8.dp))
                Text("SEGURIDAD", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray,
                    letterSpacing = 1.5.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
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
                        ) { Icon(Icons.Default.Lock, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp)) }
                        Spacer(Modifier.width(14.dp))
                        Column(Modifier.weight(1f)) {
                            Text("Cambiar contraseña", fontWeight = FontWeight.SemiBold, fontSize = vm.textScale.sp)
                            Text("Actualiza tu clave de acceso", color = Color.Gray, fontSize = 12.sp)
                        }
                        Icon(Icons.Default.ChevronRight, null, tint = Color.LightGray)
                    }
                }
                Spacer(Modifier.height(24.dp))
                Text("INTERFAZ", fontSize = 11.sp, fontWeight = FontWeight.Bold, color = Color.Gray,
                    letterSpacing = 1.5.sp, modifier = Modifier.padding(start = 4.dp, bottom = 8.dp))
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
                            ) { Icon(Icons.Default.TextFields, null, tint = Color(0xFFDC2626), modifier = Modifier.size(20.dp)) }
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
                                ) { Text(label, fontSize = 12.sp, color = if (vm.textScale == size) Color.White else Color.DarkGray) }
                            }
                        }
                    }
                }
                Spacer(Modifier.weight(1f))
                Button(
                    onClick = {
                        vm.emailUsuario = ""
                        vm.isPremium = false
                        coroutineScope.launch {
                            SessionManager(ctx).saveLoginState(false, "", false)
                        }
                        navController.navigate("login") { popUpTo("main_menu") { inclusive = true } }
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
            onDismissRequest = { if (!cargando) { dialogPw = false; nuevaPass = ""; mensajePw = "" } },
            confirmButton = {
                Button(
                    onClick = {
                        if (nuevaPass.length < 6) { mensajePw = "Mínimo 6 caracteres"; return@Button }
                        cargando = true
                        coroutineScope.launch {
                            try {
                                val response = RetrofitClient.instance.updatePassword(
                                    mapOf("email" to vm.emailUsuario, "new_password" to nuevaPass)
                                )
                                mensajePw = if (response.success) "✓ Contraseña actualizada" else response.message
                            } catch (e: Exception) {
                                mensajePw = "Error de conexión"
                                android.util.Log.e("BUMANGAPP_PW", "Error: ${e.message} - ${e.cause}")
                            } finally { cargando = false }
                        }
                    },
                    enabled = !cargando,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    if (cargando) CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    else Text("GUARDAR")
                }
            },
            dismissButton = {
                TextButton(onClick = { dialogPw = false; nuevaPass = ""; mensajePw = "" }) {
                    Text("CANCELAR", color = Color.Gray)
                }
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
    val ctx = LocalContext.current
    var planSeleccionado by remember { mutableStateOf("mensual") }
    var mostrarDialogoPago by remember { mutableStateOf(false) }
    var cargandoPago by remember { mutableStateOf(false) }
    val coroutineScope = rememberCoroutineScope()

    Column(
        Modifier
            .fillMaxSize()
            .background(Color(0xFF111827))
            .verticalScroll(rememberScrollState())
    ) {
        // ENCABEZADO
        Box(
            Modifier
                .fillMaxWidth()
                .background(Brush.verticalGradient(listOf(Color(0xFF1F2937), Color(0xFF111827))))
                .padding(32.dp),
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Box(
                    Modifier
                        .size(90.dp)
                        .background(
                            Brush.radialGradient(listOf(Color(0xFFFFD700), Color(0xFFFF8C00))),
                            RoundedCornerShape(24.dp)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(50.dp))
                }
                Spacer(Modifier.height(16.dp))
                Text("BumangApp Premium", color = Color.White, fontSize = 24.sp, fontWeight = FontWeight.ExtraBold)
                Spacer(Modifier.height(6.dp))
                Text("Viaja más inteligente por Bucaramanga", color = Color.Gray, fontSize = 14.sp, textAlign = TextAlign.Center)
            }
        }

        Column(Modifier.padding(20.dp)) {

            // PLANES
            Text("Elige tu plan", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp))

            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                // PLAN MENSUAL
                Card(
                    Modifier.weight(1f).clickable { planSeleccionado = "mensual" }
                        .border(
                            width = if (planSeleccionado == "mensual") 2.dp else 1.dp,
                            color = if (planSeleccionado == "mensual") Color(0xFFDC2626) else Color(0xFF374151),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (planSeleccionado == "mensual") Color(0xFF1F2937) else Color(0xFF1A1A2E)
                    )
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        if (planSeleccionado == "mensual") {
                            Surface(color = Color(0xFFDC2626), shape = RoundedCornerShape(6.dp)) {
                                Text("POPULAR", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                    modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                            }
                            Spacer(Modifier.height(8.dp))
                        } else {
                            Spacer(Modifier.height(24.dp))
                        }
                        Text("Mensual", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("$9.900", color = if (planSeleccionado == "mensual") Color(0xFFDC2626) else Color.White,
                            fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Text("/mes", color = Color.Gray, fontSize = 12.sp)
                    }
                }

                // PLAN ANUAL
                Card(
                    Modifier.weight(1f).clickable { planSeleccionado = "anual" }
                        .border(
                            width = if (planSeleccionado == "anual") 2.dp else 1.dp,
                            color = if (planSeleccionado == "anual") Color(0xFFDC2626) else Color(0xFF374151),
                            shape = RoundedCornerShape(16.dp)
                        ),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(
                        containerColor = if (planSeleccionado == "anual") Color(0xFF1F2937) else Color(0xFF1A1A2E)
                    )
                ) {
                    Column(Modifier.padding(16.dp), horizontalAlignment = Alignment.CenterHorizontally) {
                        Surface(color = Color(0xFF10B981), shape = RoundedCornerShape(6.dp)) {
                            Text("AHORRA 25%", color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Bold,
                                modifier = Modifier.padding(horizontal = 8.dp, vertical = 3.dp))
                        }
                        Spacer(Modifier.height(8.dp))
                        Text("Anual", color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 15.sp)
                        Spacer(Modifier.height(6.dp))
                        Text("$89.900", color = if (planSeleccionado == "anual") Color(0xFFDC2626) else Color.White,
                            fontSize = 26.sp, fontWeight = FontWeight.ExtraBold)
                        Text("/año", color = Color.Gray, fontSize = 12.sp)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // BENEFICIOS
            Text("¿Qué incluye Premium?", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp))

            listOf(
                Triple(Icons.Default.Notifications, "Alertas de llegada", "Recibe notificaciones cuando tu bus está cerca"),
                Triple(Icons.Default.History, "Historial de rutas", "Consulta las rutas que has usado anteriormente"),
                Triple(Icons.Default.Block, "Sin anuncios", "Disfruta la app sin interrupciones publicitarias"),
                Triple(Icons.Default.SupportAgent, "Soporte prioritario", "Atención preferencial ante cualquier inconveniente")
            ).forEach { (icono, titulo, desc) ->
                Card(
                    Modifier.fillMaxWidth().padding(bottom = 10.dp),
                    shape = RoundedCornerShape(14.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937))
                ) {
                    Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            Modifier.size(44.dp).background(Color(0xFFDC2626).copy(alpha = 0.15f), RoundedCornerShape(12.dp)),
                            contentAlignment = Alignment.Center
                        ) {
                            Icon(icono, null, tint = Color(0xFFDC2626), modifier = Modifier.size(24.dp))
                        }
                        Spacer(Modifier.width(14.dp))
                        Column {
                            Text(titulo, color = Color.White, fontWeight = FontWeight.SemiBold, fontSize = 14.sp)
                            Text(desc, color = Color.Gray, fontSize = 12.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // MEDIOS DE PAGO
            Text("Medios de pago", color = Color.White, fontWeight = FontWeight.Bold, fontSize = 18.sp,
                modifier = Modifier.padding(bottom = 12.dp))

            Card(
                Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(14.dp),
                colors = CardDefaults.cardColors(containerColor = Color(0xFF1F2937))
            ) {
                Column(Modifier.padding(16.dp)) {
                    listOf(
                        Pair(Icons.Default.AccountBalance, "PSE"),
                        Pair(Icons.Default.PhoneAndroid, "Nequi"),
                        Pair(Icons.Default.PhoneAndroid, "Daviplata"),
                        Pair(Icons.Default.CreditCard, "Tarjeta crédito / débito")
                    ).forEachIndexed { index, (icono, nombre) ->
                        Row(
                            Modifier.fillMaxWidth().padding(vertical = 10.dp),
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Icon(icono, null, tint = Color(0xFFDC2626), modifier = Modifier.size(22.dp))
                            Spacer(Modifier.width(14.dp))
                            Text(nombre, color = Color.White, fontSize = 14.sp)
                            Spacer(Modifier.weight(1f))
                            Icon(Icons.Default.Check, null, tint = Color(0xFF10B981), modifier = Modifier.size(18.dp))
                        }
                        if (index < 3) HorizontalDivider(color = Color(0xFF374151), thickness = 0.5.dp)
                    }
                }
            }

            Spacer(Modifier.height(28.dp))

            // BOTÓN COMPRAR O ESTADO PREMIUM
            if (!vm.isPremium) {
                Button(
                    onClick = { mostrarDialogoPago = true },
                    modifier = Modifier.fillMaxWidth().height(56.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    Icon(Icons.Default.Star, null, tint = Color.White, modifier = Modifier.size(20.dp))
                    Spacer(Modifier.width(10.dp))
                    Text(
                        if (planSeleccionado == "mensual") "OBTENER POR $9.900/mes" else "OBTENER POR $89.900/año",
                        fontWeight = FontWeight.ExtraBold, fontSize = 15.sp
                    )
                }
                Spacer(Modifier.height(12.dp))
                Text(
                    "Pago procesado de forma segura. Cancela cuando quieras.",
                    color = Color.Gray, fontSize = 11.sp,
                    textAlign = TextAlign.Center, modifier = Modifier.fillMaxWidth()
                )
            } else {
                Card(
                    Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(16.dp),
                    colors = CardDefaults.cardColors(containerColor = Color(0xFF10B981).copy(alpha = 0.15f))
                ) {
                    Row(
                        Modifier.padding(20.dp),
                        verticalAlignment = Alignment.CenterVertically,
                        horizontalArrangement = Arrangement.Center
                    ) {
                        Icon(Icons.Default.CheckCircle, null, tint = Color(0xFF10B981), modifier = Modifier.size(28.dp))
                        Spacer(Modifier.width(12.dp))
                        Column {
                            Text("¡Eres miembro Premium!", color = Color(0xFF10B981), fontWeight = FontWeight.Bold, fontSize = 16.sp)
                            Text("Disfruta todos los beneficios", color = Color.Gray, fontSize = 13.sp)
                        }
                    }
                }
            }

            Spacer(Modifier.height(24.dp))
        }
    }

    // DIÁLOGO DE CONFIRMACIÓN DE PAGO
    if (mostrarDialogoPago) {
        AlertDialog(
            onDismissRequest = { if (!cargandoPago) mostrarDialogoPago = false },
            containerColor = Color(0xFF1F2937),
            title = { Text("Confirmar suscripción", color = Color.White, fontWeight = FontWeight.Bold) },
            text = {
                Column {
                    Text(
                        "Plan: ${if (planSeleccionado == "mensual") "Mensual - $9.900/mes" else "Anual - $89.900/año"}",
                        color = Color.White, fontSize = 14.sp
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "Al confirmar aceptas los términos y condiciones de BumangApp Premium.",
                        color = Color.Gray, fontSize = 12.sp
                    )
                }
            },
            confirmButton = {
                Button(
                    onClick = {
                        cargandoPago = true
                        coroutineScope.launch {
                            try {
                                val response = RetrofitClient.instance.upgradePremium(
                                    mapOf("email" to vm.emailUsuario)
                                )
                                if (response.success) {
                                    vm.isPremium = true
                                    SessionManager(ctx).saveLoginState(true, vm.emailUsuario, true)
                                }
                            } catch (e: Exception) {
                                android.util.Log.e("BUMANGAPP_PREMIUM", "Error: ${e.message}")
                            } finally {
                                cargandoPago = false
                                mostrarDialogoPago = false
                            }
                        }
                    },
                    enabled = !cargandoPago,
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFDC2626))
                ) {
                    if (cargandoPago) {
                        CircularProgressIndicator(color = Color.White, modifier = Modifier.size(18.dp), strokeWidth = 2.dp)
                    } else {
                        Text("CONFIRMAR", fontWeight = FontWeight.Bold)
                    }
                }
            },
            dismissButton = {
                TextButton(onClick = { mostrarDialogoPago = false }) {
                    Text("CANCELAR", color = Color.Gray)
                }
            }
        )
    }
}

@Composable
fun VistaListaRutas(vm: AppStateViewModel, onSel: (RutaBus) -> Unit) {
    var rutas by remember { mutableStateOf<List<RutaBus>>(emptyList()) }
    var cargando by remember { mutableStateOf(true) }

    LaunchedEffect(Unit) {
        try {
            val response = RetrofitClient.instance.getRutas()
            if (response.success) {
                rutas = response.data.map { busRoute ->
                    RutaBus(
                        id = "R${busRoute.id}",
                        destino = busRoute.name,
                        color = parseColor(busRoute.color_hex),
                        puntos = busRoute.coordinates.sortedBy { it.order_index }.map { LatLng(it.latitude, it.longitude) },
                        horario = "5:00 AM - 8:00 PM"
                    )
                }
            }
        } catch (e: Exception) {
            android.util.Log.e("BUMANGAPP_RUTAS", "Error: ${e.message}")
        }
        cargando = false
    }

    Column(Modifier.fillMaxSize().padding(16.dp)) {
        Text("Rutas Disponibles", fontWeight = FontWeight.Bold, fontSize = 22.sp, modifier = Modifier.padding(bottom = 15.dp))
        if (cargando) {
            Box(Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = Color(0xFFDC2626))
            }
        } else {
            LazyColumn(verticalArrangement = Arrangement.spacedBy(10.dp)) {
                items(rutas) { ruta ->
                    Card(
                        Modifier.fillMaxWidth().clickable { onSel(ruta) },
                        elevation = CardDefaults.cardElevation(4.dp),
                        colors = CardDefaults.cardColors(containerColor = Color.White)
                    ) {
                        Row(Modifier.padding(16.dp), verticalAlignment = Alignment.CenterVertically) {
                            Surface(color = ruta.color, shape = RoundedCornerShape(8.dp)) {
                                Text(ruta.id, color = Color.White,
                                    modifier = Modifier.padding(horizontal = 10.dp, vertical = 5.dp),
                                    fontWeight = FontWeight.Bold)
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
}

