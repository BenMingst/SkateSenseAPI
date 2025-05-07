package com.example.skatesenseapi.notifications

import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.os.Build
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.example.skatesenseapi.R
import com.example.skatesenseapi.ui.MainActivity

class SkateSenseNotificationManager(private val context: Context) {
    
    companion object {
        private const val CHANNEL_ID = "skatesense_alerts"
        private const val WATER_NOTIFICATION_ID = 1
        private const val ELECTROLYTE_NOTIFICATION_ID = 2
        private const val FOOD_NOTIFICATION_ID = 3
    }

    init {
        createNotificationChannel()
    }

    private fun createNotificationChannel() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val name = "SkateSense Alerts"
            val descriptionText = "Health and safety alerts for skating"
            val importance = NotificationManager.IMPORTANCE_HIGH
            
            val channel = NotificationChannel(CHANNEL_ID, name, importance).apply {
                description = descriptionText
                enableVibration(true)
                setShowBadge(true)
            }
            
            val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
            notificationManager.createNotificationChannel(channel)
        }
    }

    private fun getBasicNotificationBuilder(title: String, content: String): NotificationCompat.Builder {
        val intent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
        }
        
        val pendingIntent = PendingIntent.getActivity(
            context, 
            0, 
            intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_notification)
            .setContentTitle(title)
            .setContentText(content)
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setAutoCancel(true)
            .setContentIntent(pendingIntent)
    }

    fun showWaterAlert() {
        val notification = getBasicNotificationBuilder(
            "Water Break",
            "Time to hydrate! Take a water break to maintain your performance."
        ).build()

        NotificationManagerCompat.from(context).notify(WATER_NOTIFICATION_ID, notification)
    }

    fun showElectrolyteAlert() {
        val notification = getBasicNotificationBuilder(
            "Electrolyte Reminder",
            "Replenish your electrolytes to prevent cramps and maintain endurance."
        ).build()

        NotificationManagerCompat.from(context).notify(ELECTROLYTE_NOTIFICATION_ID, notification)
    }

    fun showFoodAlert() {
        val notification = getBasicNotificationBuilder(
            "Energy Boost",
            "Time for a snack! Maintain your energy levels for optimal performance."
        ).build()

        NotificationManagerCompat.from(context).notify(FOOD_NOTIFICATION_ID, notification)
    }

    fun clearAllNotifications() {
        NotificationManagerCompat.from(context).apply {
            cancel(WATER_NOTIFICATION_ID)
            cancel(ELECTROLYTE_NOTIFICATION_ID)
            cancel(FOOD_NOTIFICATION_ID)
        }
    }
}