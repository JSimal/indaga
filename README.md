# Indaga — Fase 1

App Android (Kotlin + Jetpack Compose) con herramientas de investigación tecnológica centralizadas.

## Herramientas incluidas

**General**: Consulta única (autodetección IP/dominio → todo en paralelo), Historial/Caso de investigación.

**Red**: WHOIS (TCP/43 puro), DNS Lookup (UDP/53 propio), Conectividad y latencia, Port Scanner, IP Info (ip-api.com), Calculadora CIDR, Listas negras (DNSBL), ¿Está caído?

**Web y dominios**: Subdominios (Certificate Transparency vía crt.sh), Certificado SSL, Detector de tecnologías, Seguridad de email (SPF/DMARC), robots.txt/sitemap, Redirecciones, Typosquatting, Wayback Machine, Meta tags/Open Graph, Lector RSS/Atom.

**Finanzas/identidad**: BIN Checker (binlist.net), Validadores (Luhn, IBAN, NIF/NIE/CIF), Prefijo telefónico.

**Seguridad**: Hashes, Encoder/Decoder (Base64/Hex/URL/JWT), Generador de contraseñas.

**Archivos**: Tipo de archivo real (magic bytes), Comparador de archivos, Metadatos EXIF, Metadatos PDF.

**Hardware local (permisos especiales)**: Red local (barrido de conectividad, sin permisos peligrosos), WiFi cercanas + detector de evil twin (pide ubicación, con pantalla de justificación antes), Bluetooth cercano (pide `BLUETOOTH_SCAN` con `neverForLocation`, con justificación), Lector NFC (permiso normal, sin diálogo), Analizador de APK (de un archivo elegido por el usuario — **no** usa `QUERY_ALL_PACKAGES` ni enumera apps instaladas).

### Pendiente / decisiones tomadas

- **SSH**: no implementado a propósito. Un cliente SSH real necesita una
  librería de criptografía (JSch, sshj...) — es una dependencia con más peso
  y superficie de ataque que el resto de la app, así que se dejó fuera hasta
  que se decida explícitamente añadirla. El "telnet" implementado es solo
  conexión TCP en crudo, sin cifrado, útil para servicios de texto plano.
- Los monitores en segundo plano (sección "Vigilancia") sí están
  implementados, con `WorkManager` (comprobación cada ~6h) y notificación
  vía `POST_NOTIFICATIONS` (Android 13+), pedido solo al entrar en esa
  pantalla.

**Extras**: Utilidades geográficas (decimal↔DMS, país por TLD), Diccionario de códigos HTTP, Bandas de frecuencia, Calculadora de alcance WiFi, Búsqueda inversa de imágenes (abre Lens/Yandex/TinEye), Esteganografía básica (heurística LSB), Ping/Conexión TCP en crudo, Qué expone tu conexión (IP pública, ISP, DNS).

**Vigilancia**: vigila una web (detecta cambios de contenido) o un dominio (detecta subdominios nuevos vía Certificate Transparency). Comprobación automática cada ~6 horas con `WorkManager` + notificación si algo cambia, o botón de "revisar ahora" manual.

### Actualizaciones — ya conectado a la UI

`HomeScreen` comprueba la última Release de GitHub al abrir la app y muestra
un banner con botón de descarga si hay una versión más nueva. Sigue
pendiente cambiar `GITHUB_REPO` en `UpdateChecker.kt` por el repo real
cuando se publique en GitHub — hasta entonces la comprobación falla en
silencio (sin banner, sin errores visibles).

### Nota sobre permisos sensibles añadidos

`ACCESS_FINE_LOCATION` y `BLUETOOTH_SCAN` solo se piden **en el momento** de entrar en la herramienta que los necesita, nunca al abrir la app, y cada pantalla explica primero por qué se piden antes de lanzar el diálogo del sistema. `BLUETOOTH_SCAN` se declara con `neverForLocation` para no arrastrar también el permiso de ubicación en Android 12+. El analizador de APK opera solo sobre el fichero que el usuario elige explícitamente (vía selector de archivos del sistema), no sobre las apps instaladas.

Durante la implementación se detectó y corrigió un bug real: en Android 13+, registrar un `BroadcastReceiver` dinámico (usado por el radar Bluetooth) sin especificar `RECEIVER_EXPORTED`/`RECEIVER_NOT_EXPORTED` lanza `SecurityException` en tiempo de ejecución — se corrigió con `ContextCompat.registerReceiver(..., RECEIVER_NOT_EXPORTED)`.

## Firma de release

La keystore de firma vive en `keystore/indaga-release.keystore` (fuera de
git, en `.gitignore`) con sus credenciales en `keystore/keystore.properties`
(también ignorado). **Hacer una copia de seguridad de esa carpeta en un
sitio seguro**: si se pierde, no se podrán publicar más actualizaciones bajo
la misma firma — habría que forzar una reinstalación completa en cualquier
dispositivo que ya tenga la app instalada. `gradlew assembleRelease` firma
automáticamente si esa carpeta existe; si no existe (p. ej. en una máquina
nueva), el release simplemente sale sin firmar en vez de fallar el build.

## Cómo abrir el proyecto

1. Instala **Android Studio** (incluye el SDK y genera automáticamente el
   Gradle Wrapper al abrir el proyecto la primera vez).
2. `File > Open` y selecciona esta carpeta (`D:\Apk Inves`).
3. Espera a que sincronice Gradle (la primera vez descarga dependencias).
4. Ejecuta en un emulador o dispositivo físico con **Android 8.0 (API 26)** o superior.

**Ya compilado y verificado** (Gradle 8.0 + AGP 8.1.4 + JDK 17, generando
`app/build/outputs/apk/debug/app-debug.apk`). Un aviso si lo abres en
Android Studio: el JDK que trae empaquetado Android Studio puede ser
demasiado nuevo (JDK 25) para Gradle 8.0. Si al sincronizar da problemas,
ve a `Settings > Build, Execution, Deployment > Build Tools > Gradle` y
cambia el "Gradle JDK" a una versión 17 (Android Studio te deja descargar
una desde ahí mismo, "Download JDK").

## Actualizaciones (pendiente de activar)

`UpdateChecker.kt` ya está preparado para consultar la última Release de un
repo de GitHub y comparar versiones. Antes de publicar la app:

1. Sube el proyecto a un repositorio de GitHub.
2. Cambia `GITHUB_REPO` en
   `app/src/main/java/com/apkinves/toolbox/features/update/UpdateChecker.kt`
   por `"tu_usuario/tu_repo"`.
3. Cada vez que publiques una versión nueva, crea una Release en GitHub con
   tag `vX.Y.Z` y adjunta el APK firmado.

Todavía falta conectar `UpdateChecker` a la UI (pantalla/banner que avise
y abra la descarga) — se añadirá en una fase posterior junto con el resto
de piezas (Fase 2 en adelante).

## Optimización de tamaño (release)

- `minifyEnabled` + `shrinkResources` activados en el build de release (R8
  elimina código y recursos no usados).
- Recursos de idioma restringidos a español (`resConfigs("es")`): las
  librerías (Compose/Material3/Navigation) traen textos internos traducidos
  a decenas de idiomas que aquí nunca se muestran.
- Se quitaron dependencias no usadas (`lifecycle-runtime-ktx`,
  `lifecycle-viewmodel-compose`, `material-icons-core` — el único icono que
  usábamos ahora es un vector propio en vez de tirar de toda la librería).
- Reglas ProGuard específicas para que kotlinx.serialization no pierda los
  serializadores de `CaseEntry`, `IpInfo` y `GithubRelease` al minificar
  (verificado con dexdump que sobreviven).
- Resultado: el APK de **release** pesa **~1 MB** (minificado y con shrink
  de recursos), frente a los ~9 MB del APK de debug que se compartió al
  principio (el de debug nunca se optimiza, es normal que sea grande —
  lo relevante es lo que instalaría un usuario final, que es el release).
- Nada de esto afecta a funcionalidad: mismas herramientas, mismo
  comportamiento, solo menos bytes muertos.

## Notas de diseño

- Sin dependencias de terceros con API key: todo funciona con protocolos
  puros (TCP/UDP) o APIs públicas gratuitas sin registro.
- Firma de la app: genera una keystore propia y no la cambies nunca entre
  versiones (evita que Play Protect/antivirus desconfíen de actualizaciones).
- `ip-api.com` es la única llamada por HTTP (no HTTPS) en el plan gratuito;
  está permitida explícitamente solo para ese dominio en
  `network_security_config.xml`, el resto de tráfico exige HTTPS.
