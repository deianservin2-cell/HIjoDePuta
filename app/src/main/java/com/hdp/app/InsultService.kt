package com.hdp.app

import android.app.*
import android.content.Context
import android.content.Intent
import android.content.pm.ServiceInfo
import android.os.*

class InsultService : Service() {
    private val h = Handler(Looper.getMainLooper())
    private var wl: PowerManager.WakeLock? = null
    private var n = 100
    private val tick = object : Runnable {
        override fun run() {
            val t = Insults.random()
            val nm = getSystemService(NotificationManager::class.java)
            val pi = PendingIntent.getActivity(this@InsultService, 0, Intent(this@InsultService, MainActivity::class.java), PendingIntent.FLAG_IMMUTABLE)
            nm.notify(n++, Notification.Builder(this@InsultService, "insultos").setSmallIcon(android.R.drawable.stat_notify_chat)
                .setContentTitle("Hijo de Puta").setContentText(t).setStyle(Notification.BigTextStyle().bigText(t))
                .setContentIntent(pi).setAutoCancel(true).build())
            listener?.invoke(t)
            h.postDelayed(this, 30_000)
        }
    }
    override fun onBind(i: Intent?): IBinder? = null
    override fun onStartCommand(i: Intent?, f: Int, s: Int): Int {
        val nm = getSystemService(NotificationManager::class.java)
        nm.createNotificationChannel(NotificationChannel("fg", "Servicio", NotificationManager.IMPORTANCE_MIN))
        nm.createNotificationChannel(NotificationChannel("insultos", "Insultos", NotificationManager.IMPORTANCE_HIGH))
        val nf = Notification.Builder(this, "fg").setSmallIcon(android.R.drawable.stat_notify_chat).setContentTitle("Hijo de Puta te está vigilando").build()
        startForeground(1, nf, ServiceInfo.FOREGROUND_SERVICE_TYPE_DATA_SYNC)
        wl = (getSystemService(Context.POWER_SERVICE) as PowerManager).newWakeLock(PowerManager.PARTIAL_WAKE_LOCK, "hdp:wl").apply { acquire() }
        h.removeCallbacks(tick); h.postDelayed(tick, 5_000)
        return START_STICKY
    }
    override fun onDestroy() { h.removeCallbacks(tick); wl?.release(); super.onDestroy() }
    companion object { var listener: ((String) -> Unit)? = null }
}
