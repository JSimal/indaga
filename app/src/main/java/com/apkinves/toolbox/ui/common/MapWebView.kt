package com.apkinves.toolbox.ui.common

import android.annotation.SuppressLint
import android.webkit.JavascriptInterface
import android.webkit.WebView
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView

/** Referencia al WebView del mapa para poder actualizarlo desde fuera (nuevos marcadores, centro). */
class MapController {
    internal var webView: WebView? = null

    fun setCenter(lat: Double, lon: Double) {
        webView?.evaluateJavascript("setCenter($lat, $lon);", null)
    }

    fun setAtms(atmsJson: String) {
        val escaped = atmsJson.replace("\\", "\\\\").replace("'", "\\'")
        webView?.evaluateJavascript("setAtms('$escaped');", null)
    }
}

@SuppressLint("SetJavaScriptEnabled")
@Composable
fun MapWebView(
    controller: MapController,
    onSearchHere: (lat: Double, lon: Double) -> Unit,
    modifier: Modifier = Modifier.fillMaxWidth().height(300.dp),
) {
    AndroidView(
        modifier = modifier,
        factory = { context ->
            WebView(context).apply {
                settings.javaScriptEnabled = true
                settings.domStorageEnabled = true
                addJavascriptInterface(
                    object {
                        @JavascriptInterface
                        fun onSearchHere(lat: Double, lon: Double) {
                            onSearchHere(lat, lon)
                        }
                    },
                    "AndroidBridge",
                )
                loadUrl("file:///android_asset/map.html")
                controller.webView = this
            }
        },
    )
}
