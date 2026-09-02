package com.apkinves.toolbox.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Button
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.currentBackStackEntryAsState
import androidx.navigation.compose.rememberNavController
import com.apkinves.toolbox.BuildConfig
import com.apkinves.toolbox.R
import com.apkinves.toolbox.core.remote.AppStatus
import com.apkinves.toolbox.core.remote.AppStatusClient
import com.apkinves.toolbox.features.apkanalyzer.ApkAnalyzerScreen
import com.apkinves.toolbox.features.atmfinder.AtmFinderScreen
import com.apkinves.toolbox.features.batchquery.BatchQueryScreen
import com.apkinves.toolbox.features.btscan.BtScanScreen
import com.apkinves.toolbox.features.cidr.CidrScreen
import com.apkinves.toolbox.features.currency.CurrencyScreen
import com.apkinves.toolbox.features.cve.CveScreen
import com.apkinves.toolbox.features.dorks.DorkScreen
import com.apkinves.toolbox.features.emailverify.EmailVerifyScreen
import com.apkinves.toolbox.features.encoder.EncoderScreen
import com.apkinves.toolbox.features.exif.ExifScreen
import com.apkinves.toolbox.features.filehash.FileHashScreen
import com.apkinves.toolbox.features.filetype.FileTypeScreen
import com.apkinves.toolbox.features.freqbands.FreqBandsScreen
import com.apkinves.toolbox.features.geoutils.GeoUtilsScreen
import com.apkinves.toolbox.features.hash.HashScreen
import com.apkinves.toolbox.features.httpcodes.HttpCodesScreen
import com.apkinves.toolbox.features.history.HistoryScreen
import com.apkinves.toolbox.features.iban.ValidatorsScreen
import com.apkinves.toolbox.features.localnet.LocalNetScreen
import com.apkinves.toolbox.features.nettools.NetToolsScreen
import com.apkinves.toolbox.features.nfc.NfcScreen
import com.apkinves.toolbox.features.password.PasswordScreen
import com.apkinves.toolbox.features.pdfmeta.PdfMetaScreen
import com.apkinves.toolbox.features.phoneprefix.PhonePrefixScreen
import com.apkinves.toolbox.features.privacycheck.PrivacyCheckScreen
import com.apkinves.toolbox.features.qrscan.QrScanScreen
import com.apkinves.toolbox.features.reverseimg.ReverseImageScreen
import com.apkinves.toolbox.features.rss.RssScreen
import com.apkinves.toolbox.features.scamcheck.ScamCheckScreen
import com.apkinves.toolbox.features.stego.StegoScreen
import com.apkinves.toolbox.features.traceroute.TracerouteScreen
import com.apkinves.toolbox.features.typosquat.TyposquatScreen
import com.apkinves.toolbox.features.unified.UnifiedQueryScreen
import com.apkinves.toolbox.features.username.UsernameCheckScreen
import com.apkinves.toolbox.features.watchlist.WatchlistScreen
import com.apkinves.toolbox.features.wifirange.WifiRangeScreen
import com.apkinves.toolbox.features.wifiscan.WifiScanScreen

object Routes {
    const val HOME = "home"
    const val UNIFIED = "unified"
    const val BATCH_QUERY = "batch_query"
    const val TRACEROUTE = "traceroute"
    const val CIDR = "cidr"
    const val HASH = "hash"
    const val ENCODER = "encoder"
    const val PASSWORD = "password"
    const val HISTORY = "history"

    const val TYPOSQUAT = "typosquat"
    const val RSS = "rss"
    const val VALIDATORS = "validators"
    const val PHONE_PREFIX = "phone_prefix"
    const val SCAM_CHECK = "scam_check"
    const val ATM_FINDER = "atm_finder"
    const val EMAIL_VERIFY = "email_verify"

    const val FILE_TYPE = "file_type"
    const val FILE_HASH = "file_hash"
    const val EXIF = "exif"
    const val PDF_META = "pdf_meta"

    const val LOCAL_NET = "local_net"
    const val WIFI_SCAN = "wifi_scan"
    const val BT_SCAN = "bt_scan"
    const val NFC = "nfc"
    const val APK_ANALYZER = "apk_analyzer"

    const val GEO_UTILS = "geo_utils"
    const val HTTP_CODES = "http_codes"
    const val FREQ_BANDS = "freq_bands"
    const val WIFI_RANGE = "wifi_range"
    const val REVERSE_IMG = "reverse_img"
    const val STEGO = "stego"
    const val NET_TOOLS = "net_tools"
    const val PRIVACY_CHECK = "privacy_check"
    const val WATCHLIST = "watchlist"
    const val QR_SCAN = "qr_scan"
    const val CVE = "cve"
    const val DORKS = "dorks"
    const val USERNAME_CHECK = "username_check"
    const val CURRENCY = "currency"
}

data class ToolEntry(val route: String, val title: String, val description: String, val category: String)

private const val CAT_GENERAL = "General"
private const val CAT_RED = "Red"
private const val CAT_WEB = "Web y dominios"
private const val CAT_FINANZAS = "Finanzas / identidad"
private const val CAT_SEGURIDAD = "Seguridad"
private const val CAT_ARCHIVOS = "Archivos"
private const val CAT_HARDWARE = "Hardware local (permisos especiales)"
private const val CAT_EXTRAS = "Extras"

data class CategoryStyle(val emoji: String, val color: androidx.compose.ui.graphics.Color)

val CATEGORY_STYLES = mapOf(
    CAT_GENERAL to CategoryStyle("🧭", com.apkinves.toolbox.ui.theme.CyberColors.NeonCyan),
    CAT_RED to CategoryStyle("🌐", com.apkinves.toolbox.ui.theme.CyberColors.NeonGreen),
    CAT_WEB to CategoryStyle("🔗", com.apkinves.toolbox.ui.theme.CyberColors.NeonTeal),
    CAT_FINANZAS to CategoryStyle("💳", com.apkinves.toolbox.ui.theme.CyberColors.NeonAmber),
    CAT_SEGURIDAD to CategoryStyle("🔐", com.apkinves.toolbox.ui.theme.CyberColors.NeonPink),
    CAT_ARCHIVOS to CategoryStyle("📁", com.apkinves.toolbox.ui.theme.CyberColors.NeonPurple),
    CAT_HARDWARE to CategoryStyle("📡", com.apkinves.toolbox.ui.theme.CyberColors.NeonOrange),
    CAT_EXTRAS to CategoryStyle("✨", com.apkinves.toolbox.ui.theme.CyberColors.NeonCyan),
)

val TOOLS = listOf(
    ToolEntry(Routes.UNIFIED, "Consulta única", "WHOIS, DNS, hosting, puertos, SSL, subdominios, tecnologías, email, listas negras y más", CAT_GENERAL),
    ToolEntry(Routes.HISTORY, "Historial / Caso", "Consultas guardadas", CAT_GENERAL),
    ToolEntry(Routes.WATCHLIST, "Vigilancia", "Avisos si cambia una web o aparecen subdominios nuevos", CAT_GENERAL),
    ToolEntry(Routes.BATCH_QUERY, "Consulta por lotes", "Varios dominios/IPs a la vez, informe combinado", CAT_GENERAL),

    ToolEntry(Routes.CVE, "Vulnerabilidades (CVE)", "Busca por tecnología/versión en la NVD del NIST", CAT_WEB),
    ToolEntry(Routes.TYPOSQUAT, "Typosquatting", "Dominios parecidos ya registrados", CAT_WEB),
    ToolEntry(Routes.RSS, "Lector RSS/Atom", "Feeds de un sitio", CAT_WEB),
    ToolEntry(Routes.DORKS, "Google/Bing Dorks", "Reconocimiento pasivo sobre un dominio", CAT_WEB),
    ToolEntry(Routes.USERNAME_CHECK, "Buscador de username", "¿Existe ese usuario en otras plataformas?", CAT_WEB),

    ToolEntry(Routes.TRACEROUTE, "Conectividad y latencia", "¿Está el destino alcanzable? ¿Con qué latencia?", CAT_RED),
    ToolEntry(Routes.CIDR, "Calculadora CIDR", "Rango, máscara, hosts usables", CAT_RED),

    ToolEntry(Routes.VALIDATORS, "Validadores", "Tarjeta (Luhn+BIN), IBAN (+lista de países), NIF/NIE/CIF", CAT_FINANZAS),
    ToolEntry(Routes.PHONE_PREFIX, "Prefijo telefónico", "País por prefijo internacional", CAT_FINANZAS),
    ToolEntry(Routes.ATM_FINDER, "Cajeros cercanos", "Mapa explorable + proximidad (OpenStreetMap)", CAT_FINANZAS),
    ToolEntry(Routes.SCAM_CHECK, "Verificar fraude/scam", "Abre ScamAdviser y otros con el dominio puesto", CAT_FINANZAS),
    ToolEntry(Routes.EMAIL_VERIFY, "Verificador de email", "Sintaxis + registros MX del dominio", CAT_FINANZAS),
    ToolEntry(Routes.CURRENCY, "Divisas y cripto", "EUR→USD/USDT/USDC (con fecha) + ticker BTC/ETH", CAT_FINANZAS),

    ToolEntry(Routes.HASH, "Hashes", "MD5, SHA-1, SHA-256, SHA-512", CAT_SEGURIDAD),
    ToolEntry(Routes.ENCODER, "Encoder/Decoder", "Base64, Hex, URL, JWT", CAT_SEGURIDAD),
    ToolEntry(Routes.PASSWORD, "Contraseñas", "Generador + medidor de fortaleza + filtraciones (HIBP)", CAT_SEGURIDAD),
    ToolEntry(Routes.QR_SCAN, "Escáner QR", "Lee códigos QR/barras y analiza el contenido", CAT_SEGURIDAD),

    ToolEntry(Routes.FILE_TYPE, "Tipo de archivo real", "Detecta por cabecera binaria", CAT_ARCHIVOS),
    ToolEntry(Routes.FILE_HASH, "Comparar archivos", "¿Son dos archivos idénticos?", CAT_ARCHIVOS),
    ToolEntry(Routes.EXIF, "Metadatos EXIF", "Cámara, fecha, ubicación GPS", CAT_ARCHIVOS),
    ToolEntry(Routes.PDF_META, "Metadatos PDF", "Autor, creador, fechas ocultas", CAT_ARCHIVOS),

    ToolEntry(Routes.LOCAL_NET, "Red local", "Dispositivos activos en tu WiFi", CAT_HARDWARE),
    ToolEntry(Routes.WIFI_SCAN, "WiFi cercanas", "Redes visibles + detector de evil twin (pide ubicación)", CAT_HARDWARE),
    ToolEntry(Routes.BT_SCAN, "Bluetooth cercano", "Dispositivos BT alrededor (pide permiso)", CAT_HARDWARE),
    ToolEntry(Routes.NFC, "Lector NFC", "Lee tags/tarjetas NFC", CAT_HARDWARE),
    ToolEntry(Routes.APK_ANALYZER, "Analizador de APK", "Permisos y firma de un .apk elegido", CAT_HARDWARE),

    ToolEntry(Routes.GEO_UTILS, "Utilidades geográficas", "Decimal↔DMS, país por TLD", CAT_EXTRAS),
    ToolEntry(Routes.HTTP_CODES, "Códigos HTTP", "Diccionario buscable", CAT_EXTRAS),
    ToolEntry(Routes.FREQ_BANDS, "Bandas de frecuencia", "Qué servicio usa cada banda", CAT_EXTRAS),
    ToolEntry(Routes.WIFI_RANGE, "Alcance WiFi", "Estimación teórica en espacio libre", CAT_EXTRAS),
    ToolEntry(Routes.REVERSE_IMG, "Búsqueda inversa de imágenes", "Abre Lens/Yandex/TinEye", CAT_EXTRAS),
    ToolEntry(Routes.STEGO, "Esteganografía (básico)", "Heurística sobre el bit menos significativo", CAT_EXTRAS),
    ToolEntry(Routes.NET_TOOLS, "Ping / Conexión TCP", "Ping y conexión TCP en crudo", CAT_EXTRAS),
    ToolEntry(Routes.PRIVACY_CHECK, "Qué expone tu conexión", "IP pública, ISP, DNS en uso", CAT_EXTRAS),
)

@Composable
fun ToolboxApp() {
    var status by remember { mutableStateOf<AppStatus?>(null) }

    LaunchedEffect(Unit) {
        status = AppStatusClient.fetch()
    }

    val current = status
    val blocked = current != null && (!current.enabled || (current.minVersionCode > 0 && BuildConfig.VERSION_CODE < current.minVersionCode))

    if (blocked) {
        BlockedScreen(current!!.message.ifBlank { "Esta versión de Indaga ya no está disponible. Actualiza a la última versión." })
    } else {
        ToolboxNavigation()
    }
}

@Composable
private fun BlockedScreen(message: String) {
    val context = androidx.compose.ui.platform.LocalContext.current
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        Text("▸ INDAGA", style = MaterialTheme.typography.headlineSmall, color = MaterialTheme.colorScheme.primary)
        Text(
            message,
            style = MaterialTheme.typography.bodyMedium,
            textAlign = androidx.compose.ui.text.style.TextAlign.Center,
            modifier = Modifier.padding(top = 16.dp, bottom = 16.dp),
        )
        Button(onClick = {
            val intent = android.content.Intent(android.content.Intent.ACTION_VIEW, android.net.Uri.parse("https://github.com/JSimal/indaga/releases"))
            runCatching { context.startActivity(intent) }
        }) { Text("Ir a la página de descargas") }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ToolboxNavigation() {
    val navController = rememberNavController()
    val backStackEntry by navController.currentBackStackEntryAsState()
    val currentRoute = backStackEntry?.destination?.route ?: Routes.HOME
    val currentTitle = TOOLS.firstOrNull { it.route == currentRoute }?.title ?: "Indaga"

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (currentRoute == Routes.HOME) "▸ INDAGA" else currentTitle,
                        fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    )
                },
                navigationIcon = {
                    if (currentRoute != Routes.HOME) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(painterResource(R.drawable.ic_back), contentDescription = "Volver")
                        }
                    }
                },
                colors = androidx.compose.material3.TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface,
                    titleContentColor = MaterialTheme.colorScheme.primary,
                ),
            )
        },
    ) { padding ->
        NavHost(
            navController = navController,
            startDestination = Routes.HOME,
            modifier = Modifier.padding(padding),
        ) {
            composable(Routes.HOME) { HomeScreen(navController) }
            composable(Routes.UNIFIED) { UnifiedQueryScreen() }
            composable(Routes.BATCH_QUERY) { BatchQueryScreen() }
            composable(Routes.TRACEROUTE) { TracerouteScreen() }
            composable(Routes.CIDR) { CidrScreen() }
            composable(Routes.HASH) { HashScreen() }
            composable(Routes.ENCODER) { EncoderScreen() }
            composable(Routes.PASSWORD) { PasswordScreen() }
            composable(Routes.HISTORY) { HistoryScreen() }

            composable(Routes.CVE) { CveScreen() }
            composable(Routes.TYPOSQUAT) { TyposquatScreen() }
            composable(Routes.RSS) { RssScreen() }
            composable(Routes.DORKS) { DorkScreen() }
            composable(Routes.USERNAME_CHECK) { UsernameCheckScreen() }
            composable(Routes.VALIDATORS) { ValidatorsScreen() }
            composable(Routes.PHONE_PREFIX) { PhonePrefixScreen() }
            composable(Routes.ATM_FINDER) { AtmFinderScreen() }
            composable(Routes.SCAM_CHECK) { ScamCheckScreen() }
            composable(Routes.EMAIL_VERIFY) { EmailVerifyScreen() }
            composable(Routes.CURRENCY) { CurrencyScreen() }

            composable(Routes.FILE_TYPE) { FileTypeScreen() }
            composable(Routes.FILE_HASH) { FileHashScreen() }
            composable(Routes.EXIF) { ExifScreen() }
            composable(Routes.PDF_META) { PdfMetaScreen() }

            composable(Routes.LOCAL_NET) { LocalNetScreen() }
            composable(Routes.WIFI_SCAN) { WifiScanScreen() }
            composable(Routes.BT_SCAN) { BtScanScreen() }
            composable(Routes.NFC) { NfcScreen() }
            composable(Routes.APK_ANALYZER) { ApkAnalyzerScreen() }

            composable(Routes.GEO_UTILS) { GeoUtilsScreen() }
            composable(Routes.HTTP_CODES) { HttpCodesScreen() }
            composable(Routes.FREQ_BANDS) { FreqBandsScreen() }
            composable(Routes.WIFI_RANGE) { WifiRangeScreen() }
            composable(Routes.REVERSE_IMG) { ReverseImageScreen() }
            composable(Routes.STEGO) { StegoScreen() }
            composable(Routes.NET_TOOLS) { NetToolsScreen() }
            composable(Routes.PRIVACY_CHECK) { PrivacyCheckScreen() }
            composable(Routes.WATCHLIST) { WatchlistScreen() }
            composable(Routes.QR_SCAN) { QrScanScreen() }
        }
    }
}

@Composable
private fun HomeScreen(navController: NavHostController) {
    HomeScreenContent(
        tools = TOOLS,
        onToolClick = { route -> navController.navigate(route) },
    )
}
