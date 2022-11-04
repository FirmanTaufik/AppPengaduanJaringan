package com.app.pengaduanjaringan.services

import com.google.firebase.messaging.FirebaseMessagingService
import android.annotation.SuppressLint
import android.app.*
import com.google.firebase.messaging.RemoteMessage
import android.content.Intent
import android.graphics.Color
import android.os.Build
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.core.app.NotificationManagerCompat
import com.app.pengaduanjaringan.MainActivity
import com.app.pengaduanjaringan.R

class MyFirebaseMessagingService : FirebaseMessagingService() {
    private val TAG = "MyFirebaseMessagingServiceTAG"
    override fun onNewToken(s: String) {
        Log.i("getToken", s)
    }

    @SuppressLint("WrongThread")
    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        showNotification(remoteMessage.notification!!.title, remoteMessage.notification!!.body)
    }

    fun showNotification(title: String?,message: String?) {
        val notificationIntent = Intent(applicationContext, MainActivity::class.java)
        notificationIntent.putExtra("from", "Notif")
        notificationIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
        val stackBuilder = TaskStackBuilder.create(applicationContext)
        stackBuilder.addParentStack(MainActivity::class.java)
        stackBuilder.addNextIntent(notificationIntent)
        val pendingIntent = PendingIntent.getActivity(
            applicationContext, 0 /* Request code */, notificationIntent,
            PendingIntent.FLAG_ONE_SHOT
        )
        val id = "main channel"
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            val notificationManager =
                applicationContext.getSystemService(NOTIFICATION_SERVICE) as NotificationManager
            val name: CharSequence = "Channel Name"
            val description = "Channel Description"
            val importance = NotificationManager.IMPORTANCE_HIGH
            val notificationChannel = NotificationChannel(id, name, importance)
            notificationChannel.description = description
            notificationChannel.enableLights(true)
            notificationChannel.enableVibration(true)
            notificationChannel.lockscreenVisibility = Notification.VISIBILITY_PUBLIC
            notificationManager.createNotificationChannel(notificationChannel)
        }
        val notificationBuilder = NotificationCompat.Builder(applicationContext, id)
        notificationBuilder.setSmallIcon(R.mipmap.ic_logo)
        notificationBuilder.setContentTitle(title)
        notificationBuilder.setContentText(message)
        notificationBuilder.setLights(Color.WHITE, 500, 5000)
        notificationBuilder.color = resources.getColor(R.color.primary)
        notificationBuilder.setAutoCancel(true)
        notificationBuilder.setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
        notificationBuilder.setDefaults(Notification.DEFAULT_SOUND)
        notificationBuilder.setContentIntent(pendingIntent)
        val notificationManagerCompat = NotificationManagerCompat.from(applicationContext)
        notificationManagerCompat.notify(1400, notificationBuilder.build())
    }
}