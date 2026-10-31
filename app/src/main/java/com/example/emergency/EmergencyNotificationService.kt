package com.example.emergency

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.MainActivity

private const val CHANNEL_ID = "veil_emergency_alerts"
private const val NOTIFICATION_ID = 9110

class EmergencyNotificationService(private val context: Context) {

    private val notificationManager =
        context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val channel = NotificationChannel(
                CHANNEL_ID,
                "Emergency SOS Alerts",
                NotificationManager.IMPORTANCE_HIGH
            ).apply {
                description = "Urgent audible emergency alerts triggered by VEIL wristband or SOS action"
                enableVibration(true)
                enableLights(true)
                setShowBadge(true)
                lockscreenVisibility = NotificationCompat.VISIBILITY_PUBLIC
            }
            notificationManager.createNotificationChannel(channel)
        }
    }

    fun showEmergencyNotification(source: String, timestamp: Long) {
        val launchIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TOP
            putExtra("EXTRA_EMERGENCY_TRIGGERED", true)
        }

        val pendingIntent = PendingIntent.getActivity(
            context,
            0,
            launchIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or (if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) PendingIntent.FLAG_IMMUTABLE else 0)
        )

        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(android.R.drawable.ic_dialog_alert)
            .setContentTitle("🚨 VEIL EMERGENCY SOS ACTIVATED")
            .setContentText("Distress alert triggered via $source. Siren & GPS active.")
            .setStyle(
                NotificationCompat.BigTextStyle()
                    .bigText("EMERGENCY DISTRESS ACTIVATED via $source.\nHigh-decibel alert siren and GPS broadcasting are active. Tap to view location and alert contacts.")
            )
            .setPriority(NotificationCompat.PRIORITY_MAX)
            .setCategory(NotificationCompat.CATEGORY_ALARM)
            .setContentIntent(pendingIntent)
            .setOngoing(true)
            .setAutoCancel(false)
            .build()

        try {
            notificationManager.notify(NOTIFICATION_ID, notification)
        } catch (e: SecurityException) {
            // Notifications permission not granted
        }
    }

    fun cancelEmergencyNotification() {
        try {
            notificationManager.cancel(NOTIFICATION_ID)
        } catch (e: Exception) {
            // Ignore
        }
    }
}
