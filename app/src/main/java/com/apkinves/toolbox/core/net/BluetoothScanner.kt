package com.apkinves.toolbox.core.net

import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import androidx.core.content.ContextCompat
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow

object BluetoothScanner {

    data class BtDevice(val name: String, val address: String, val rssi: Int?)

    /** Emite cada dispositivo encontrado durante ~12s de descubrimiento y cierra al terminar. */
    fun discover(context: Context) = callbackFlow<BtDevice> {
        val adapter = BluetoothAdapter.getDefaultAdapter()
        if (adapter == null || !adapter.isEnabled) {
            close(IllegalStateException("Bluetooth no disponible o desactivado"))
            return@callbackFlow
        }

        val receiver = object : BroadcastReceiver() {
            override fun onReceive(ctx: Context?, intent: Intent?) {
                when (intent?.action) {
                    BluetoothDevice.ACTION_FOUND -> {
                        @Suppress("DEPRECATION")
                        val device = intent.getParcelableExtra<BluetoothDevice>(BluetoothDevice.EXTRA_DEVICE) ?: return
                        val rssi = intent.getShortExtra(BluetoothDevice.EXTRA_RSSI, Short.MIN_VALUE)
                        val name = runCatching { device.name }.getOrNull() ?: "(sin nombre)"
                        trySend(BtDevice(name, device.address, if (rssi == Short.MIN_VALUE) null else rssi.toInt()))
                    }
                    BluetoothAdapter.ACTION_DISCOVERY_FINISHED -> close()
                }
            }
        }

        val filter = IntentFilter().apply {
            addAction(BluetoothDevice.ACTION_FOUND)
            addAction(BluetoothAdapter.ACTION_DISCOVERY_FINISHED)
        }
        ContextCompat.registerReceiver(context, receiver, filter, ContextCompat.RECEIVER_NOT_EXPORTED)

        val started = runCatching { adapter.startDiscovery() }.getOrDefault(false)
        if (!started) close(IllegalStateException("No se pudo iniciar el descubrimiento (¿falta permiso?)"))

        awaitClose {
            runCatching { adapter.cancelDiscovery() }
            runCatching { context.unregisterReceiver(receiver) }
        }
    }
}
