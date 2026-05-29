package `in`.vedicpanchang.app.service

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.widget.PanchangWidgetProvider
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import java.time.format.DateTimeFormatter
import javax.inject.Inject
import javax.inject.Singleton
import androidx.core.content.edit

/**
 * Pushes today's Panchang data to the Android home-screen widget via SharedPreferences.
 * The native PanchangWidgetProvider reads these keys on each onUpdate call.
 * Equivalent of widget_service.dart.
 */
@Singleton
class WidgetService @Inject constructor(
    @ApplicationContext private val context: Context
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("HomeWidgetPreferences", Context.MODE_PRIVATE)

    private val timeFormatter = DateTimeFormatter.ofPattern("HH:mm")
    private val dateFormatter = DateTimeFormatter.ofPattern("EEEE, d MMM")

    fun updateWidget(panchang: PanchangModel) {
        try {
            val tz = TimeZone.currentSystemDefault()
            val sunriseLocal = panchang.sunrise.toLocalDateTime(tz)
            val sunsetLocal = panchang.sunset.toLocalDateTime(tz)
            val dateLocal = panchang.date

            prefs.edit {
                putString("widget_tithi", panchang.tithiDisplay)
                    .putString("widget_nakshatra", panchang.nakshatraName)
                    .putString("widget_yoga", panchang.yogaName)
                    .putString("widget_sunrise", formatTime(sunriseLocal.hour, sunriseLocal.minute))
                    .putString("widget_sunset", formatTime(sunsetLocal.hour, sunsetLocal.minute))
                    .putString("widget_location", panchang.locationName)
                    .putString(
                        "widget_date", formatDate(
                            dateLocal.year,
                            dateLocal.month.number, dateLocal.day
                        )
                    )
                    .putString(
                        "widget_festival",
                        if (panchang.hasFestivals) panchang.primaryFestival ?: "" else ""
                    )
            }

            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, PanchangWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            if (ids.isNotEmpty()) {
                ids.forEach { id ->
                    PanchangWidgetProvider.updateAppWidget(context, manager, id)
                }
            }
        } catch (_: Exception) {
            // Widget update is best-effort
        }
    }

    private fun formatTime(hour: Int, minute: Int) =
        "%02d:%02d".format(hour, minute)

    private fun formatDate(year: Int, month: Int, day: Int): String {
        val cal = java.util.Calendar.getInstance().apply { set(year, month - 1, day) }
        val sdf = java.text.SimpleDateFormat("EEEE, d MMM", java.util.Locale.ENGLISH)
        return sdf.format(cal.time)
    }
}
