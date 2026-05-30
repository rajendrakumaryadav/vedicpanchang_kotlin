package `in`.vedicpanchang.app.service

import android.app.AlarmManager
import android.app.NotificationChannel
import android.app.NotificationManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.os.Build
import androidx.annotation.RequiresApi
import androidx.core.app.NotificationCompat
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.vedicpanchang.app.R
import `in`.vedicpanchang.app.receiver.NotificationReceiver
import javax.inject.Inject
import javax.inject.Singleton

private const val CHANNEL_ID = "vedic_panchang_events"
private const val CHANNEL_NAME = "Panchang Events"
private const val CHANNEL_DESC = "Notifications for Vedic calendar events and festivals"
private const val PREFS_PENDING = "notif_pending_ids"
private const val KEY_IDS = "ids"

/**
 * Low-level wrapper replacing flutter_local_notifications.
 * Uses AlarmManager for exact scheduling and NotificationManager for display.
 * Equivalent of notification_service.dart.
 */
@Singleton
class NotificationService @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    private val notifManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
    private val alarmManager = context.getSystemService(Context.ALARM_SERVICE) as AlarmManager
    private val pendingPrefs: SharedPreferences =
        context.getSharedPreferences(PREFS_PENDING, Context.MODE_PRIVATE)

    init {
        createChannel()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun createChannel() {
        val channel = NotificationChannel(CHANNEL_ID, CHANNEL_NAME, NotificationManager.IMPORTANCE_HIGH).apply {
            description = CHANNEL_DESC
            enableVibration(true)
            enableLights(true)
        }
        notifManager.createNotificationChannel(channel)
    }

    fun canScheduleExactAlarms(): Boolean {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.S) {
            alarmManager.canScheduleExactAlarms()
        } else {
            true
        }
    }

    /**
     * Schedule a notification at [triggerAtMs] (epoch milliseconds).
     * Uses AlarmManager.setAlarmClock when exact alarms are permitted,
     * falls back to setAndAllowWhileIdle.
     */
    @RequiresApi(Build.VERSION_CODES.M)
    fun schedule(id: Int, title: String, body: String, triggerAtMs: Long): Boolean {
        if (triggerAtMs <= System.currentTimeMillis()) return false

        val intent = Intent(context, NotificationReceiver::class.java).apply {
            putExtra("id", id)
            putExtra("title", title)
            putExtra("body", body)
        }
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        return try {
            if (canScheduleExactAlarms()) {
                val info = AlarmManager.AlarmClockInfo(triggerAtMs, pendingIntent)
                alarmManager.setAlarmClock(info, pendingIntent)
            } else {
                alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            }
            addPendingId(id)
            true
        } catch (_: SecurityException) {
            alarmManager.setAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, triggerAtMs, pendingIntent)
            addPendingId(id)
            true
        } catch (_: Exception) {
            false
        }
    }

    fun cancel(id: Int) {
        val intent = Intent(context, NotificationReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(
            context, id, intent,
            PendingIntent.FLAG_NO_CREATE or PendingIntent.FLAG_IMMUTABLE
        ) ?: return
        alarmManager.cancel(pendingIntent)
        pendingIntent.cancel()
        removePendingId(id)
    }

    fun cancelRange(startId: Int, endId: Int) {
        val ids = getPendingIds().filter { it in startId..endId }
        ids.forEach { cancel(it) }
    }

    fun cancelAll() {
        getPendingIds().forEach { cancel(it) }
    }

    /** Show a notification immediately (for test notifications). */
    fun show(id: Int, title: String, body: String) {
        val notification = NotificationCompat.Builder(context, CHANNEL_ID)
            .setSmallIcon(R.drawable.ic_launcher_foreground)
            .setContentTitle(title)
            .setContentText(body)
            .setStyle(NotificationCompat.BigTextStyle().bigText(body))
            .setPriority(NotificationCompat.PRIORITY_HIGH)
            .setColor(0xFFFF9800.toInt())
            .setAutoCancel(true)
            .build()
        notifManager.notify(id, notification)
    }

    // ── Pending ID tracking in SharedPreferences ──────────────────────────────

    private fun getPendingIds(): Set<Int> {
        return pendingPrefs.getStringSet(KEY_IDS, emptySet())
            ?.mapNotNull { it.toIntOrNull() }?.toSet() ?: emptySet()
    }

    private fun addPendingId(id: Int) {
        val current = pendingPrefs.getStringSet(KEY_IDS, emptySet())?.toMutableSet() ?: mutableSetOf()
        current.add(id.toString())
        pendingPrefs.edit().putStringSet(KEY_IDS, current).apply()
    }

    private fun removePendingId(id: Int) {
        val current = pendingPrefs.getStringSet(KEY_IDS, emptySet())?.toMutableSet() ?: return
        current.remove(id.toString())
        pendingPrefs.edit().putStringSet(KEY_IDS, current).apply()
    }
}
