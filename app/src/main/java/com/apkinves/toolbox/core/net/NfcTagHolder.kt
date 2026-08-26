package com.apkinves.toolbox.core.net

import android.nfc.NdefMessage
import android.nfc.Tag
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow

data class NfcTagReport(
    val techList: List<String>,
    val idHex: String,
    val ndefRecords: List<String>,
)

/**
 * Puente simple entre el foreground dispatch de NFC (que solo puede recibirse
 * en la Activity, vía onNewIntent) y la pantalla Compose que quiere mostrarlo.
 */
object NfcTagHolder {
    private val _lastTag = MutableStateFlow<NfcTagReport?>(null)
    val lastTag = _lastTag.asStateFlow()

    fun publish(tag: Tag, ndef: NdefMessage?) {
        val idHex = tag.id.joinToString(":") { "%02X".format(it) }
        val records = ndef?.records?.map { record ->
            runCatching { String(record.payload, Charsets.UTF_8) }.getOrDefault("(datos binarios, ${record.payload.size} bytes)")
        } ?: emptyList()
        _lastTag.value = NfcTagReport(tag.techList.map { it.substringAfterLast('.') }, idHex, records)
    }

    fun clear() {
        _lastTag.value = null
    }
}
