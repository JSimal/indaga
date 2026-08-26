package com.apkinves.toolbox.work

import android.Manifest
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.os.Build
import androidx.core.app.ActivityCompat
import androidx.core.app.NotificationCompat
import androidx.work.CoroutineWorker
import androidx.work.ExistingPeriodicWorkPolicy
import androidx.work.PeriodicWorkRequestBuilder
import androidx.work.WorkerParameters
import androidx.work.WorkManager
import com.apkinves.toolbox.MainActivity
import com.apkinves.toolbox.core.net.SubdomainFinder
import com.apkinves.toolbox.data.WatchRepository
import com.apkinves.toolbox.data.WatchType
import java.net.HttpURLConnection
import java.net.URL
import java.security.MessageDigest
import java.util.concurrent.TimeUnit

class WatchWorker(context: Context, params: WorkerParameters) : CoroutineWorker(context, params) {

    companion object {
        private const val UNIQUE_WORK_NAME = "watchlist_periodic_check"
        private const val CHANNEL_ID = "watchlist_changes"

        fun schedulePeriodic(context: Context) {
            val request = PeriodicWorkRequestBuilder<WatchWorker>(6, TimeUnit.HOURS).build()
            WorkManager.getInstance(context)
                .enqueueUniquePeriodicWork(UNIQUE_WORK_NAME, ExistingPeriodicWorkPolicy.KEEP, request)
        }

        fun cancelPeriodic(context: Context) {
            WorkManager.getInstance(context).cancelUniqueWork(UNIQUE_WORK_NAME)
        }
    }

    override suspend fun doWork(): Result {
        val repo = WatchRepository(applicationContext)
        val items = repo.snapshot()
        if (items.isEmpty()) return Result.success()

        ensureChannel()

        items.forEach { item ->
            val newSignature = runCatching {
                when (WatchType.valueOf(item.type)) {
                    WatchType.CONTENT -> hashOf(fetchContent(item.target))
                    WatchType.SUBDOMAINS -> SubdomainFinder.find(item.target).sorted().joinToString(",")
                }
            }.getOrNull() ?: return@forEach

            val changed = item.lastSignature != null && item.lastSignature != newSignature
            repo.updateResult(item.id, newSignature, changed)

            if (changed) notifyChange(item.target)
        }

        return Result.success()
    }

    private fun fetchContent(url: String): String {
        val target = if (url.startsWith("http")) url else "https://$url"
        val conn = URL(target).openConnection() as HttpURLConnection
        conn.connectTimeout = 10_000
        conn.readTimeout = 10_000
        return try {
            conn.inputStream.bufferedReader().readText().take(500_000)
        } finally {
            conn.disconnect()
        }
    }

    private fun hashOf(text: String): String {
        val digest = MessageDigest.getInstance("SHA-256").digest(text.toByteArray(Charsets.UTF_8))
        return digest.joinToString("") { "%02x".format(it) }
    }

    private fun ensureChannel() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) return
        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        val channel = NotificationChannel(CHANNEL_ID, "Cambios en vigilancia", NotificationManager.IMPORTANCE_DEFAULT)
        manager.createNotificationChannel(channel)
    }

    private fun notifyChange(target: String) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            val granted = ActivityCompat.checkSelfPermission(applicationContext, Manifest.permission.POST_NOTIFICATIONS) == PackageManager.PERMISSION_GRANTED
            if (!granted) return
        }

        val openIntent = Intent(applicationContext, MainActivity::class.java)
        val pendingIntentFlags = PendingIntent.FLAG_UPDATE_CURRENT or
            (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) PendingIntent.FLAG_IMMUTABLE else 0)
        val pendingIntent = PendingIntent.getActivity(applicationContext, target.hashCode(), openIntent, pendingIntentFlags)

        val notification = NotificationCompat.Builder(applicationContext, CHANNEL_ID)
            .setContentTitle("Cambio detectado")
            .setContentText(target)
            .setSmallIcon(android.R.drawable.ic_dialog_info)
            .setContentIntent(pendingIntent)
            .setAutoCancel(true)
            .build()

        val manager = applicationContext.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        runCatching { manager.notify(target.hashCode(), notification) }
    }
}
