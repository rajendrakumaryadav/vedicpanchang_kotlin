package `in`.vedicpanchang.app.receiver

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import dagger.hilt.android.AndroidEntryPoint
import `in`.vedicpanchang.app.data.datasource.AppPreferences
import `in`.vedicpanchang.app.service.NotificationScheduler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Reschedules all Panchang notifications after device reboot,
 * because AlarmManager alarms do not survive reboots.
 */
@AndroidEntryPoint
class BootReceiver : BroadcastReceiver() {

    @Inject lateinit var scheduler: NotificationScheduler
    @Inject lateinit var preferences: AppPreferences

    override fun onReceive(context: Context, intent: Intent) {
        if (intent.action != Intent.ACTION_BOOT_COMPLETED) return

        val pendingResult = goAsync()
        CoroutineScope(Dispatchers.IO + SupervisorJob()).launch {
            try {
                val location = preferences.cachedLocation.first()
                val settings = preferences.notificationSettings.first()
                val locale = preferences.locale.first()
                if (location != null) {
                    scheduler.reschedule(
                        lat = location.latitude,
                        lon = location.longitude,
                        locationName = location.city,
                        settings = settings,
                        locale = locale
                    )
                }
            } finally {
                pendingResult.finish()
            }
        }
    }
}
