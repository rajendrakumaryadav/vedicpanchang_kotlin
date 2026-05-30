package `in`.vedicpanchang.app.service

import android.appwidget.AppWidgetManager
import android.content.ComponentName
import android.content.Context
import android.content.SharedPreferences
import androidx.core.content.edit
import dagger.hilt.android.qualifiers.ApplicationContext
import `in`.vedicpanchang.app.data.datasource.AppPreferences
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.l10n.AppStrings
import `in`.vedicpanchang.app.l10n.PanchangLocalizer
import `in`.vedicpanchang.app.widget.PanchangWidgetProvider
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import javax.inject.Inject
import javax.inject.Singleton

/**
 * Pushes today's Panchang data (localized) to the home-screen widget via SharedPreferences.
 * The native PanchangWidgetProvider reads these keys on each onUpdate call.
 */
@Singleton
class WidgetService @Inject constructor(
    @param:ApplicationContext private val context: Context,
    private val preferences: AppPreferences
) {
    private val prefs: SharedPreferences =
        context.getSharedPreferences("HomeWidgetPreferences", Context.MODE_PRIVATE)

    fun updateWidget(panchang: PanchangModel) {
        try {
            val locale = runBlocking { preferences.locale.first() }
            val localizer = PanchangLocalizer(locale)
            val strings = AppStrings.of(locale)

            val tz = TimeZone.currentSystemDefault()
            val sunriseLocal  = panchang.sunrise.toLocalDateTime(tz)
            val sunsetLocal   = panchang.sunset.toLocalDateTime(tz)
            val moonriseLocal = panchang.moonrise.toLocalDateTime(tz)
            val moonsetLocal  = panchang.moonset.toLocalDateTime(tz)

            prefs.edit {
                // ── Localized values ─────────────────────────────────────────
                putString("widget_tithi",     localizer.tithiDisplay(panchang))
                putString("widget_nakshatra", localizer.nakshatraName(panchang))
                putString("widget_yoga",      localizer.yogaName(panchang))
                putString("widget_karana",    localizer.karanaName(panchang))
                putString("widget_vaar",      localizer.vaarName(panchang))

                // ── Sun / Moon times ──────────────────────────────────────────
                putString("widget_sunrise",  fmt(sunriseLocal.hour,  sunriseLocal.minute))
                putString("widget_sunset",   fmt(sunsetLocal.hour,   sunsetLocal.minute))
                putString("widget_moonrise", fmt(moonriseLocal.hour, moonriseLocal.minute))
                putString("widget_moonset",  fmt(moonsetLocal.hour,  moonsetLocal.minute))

                // ── Date / location ───────────────────────────────────────────
                putString("widget_date",     fmtDate(panchang.date.year,
                                                     panchang.date.month.ordinal + 1,
                                                     panchang.date.day,
                                                     locale))
                putString("widget_location", panchang.locationName)
                putString("widget_festival",
                    if (panchang.hasFestivals)
                        localizer.festivalName(panchang.primaryFestival ?: "") else "")

                // ── Localized UI labels ───────────────────────────────────────
                putString("widget_lbl_tithi",     strings["tithi"]     ?: "Tithi")
                putString("widget_lbl_nakshatra", strings["nakshatra"] ?: "Nakshatra")
                putString("widget_lbl_yoga",      strings["yoga"]      ?: "Yoga")
                putString("widget_lbl_karana",    strings["karana"]    ?: "Karana")
                putString("widget_lbl_sunrise",   strings["sunrise"]   ?: "Sunrise")
                putString("widget_lbl_sunset",    strings["sunset"]    ?: "Sunset")
                putString("widget_lbl_moonrise",  strings["moonrise"]  ?: "Moonrise")
                putString("widget_lbl_moonset",   strings["moonset"]   ?: "Moonset")
                putString("widget_lbl_vaar",       strings["vaar"]      ?: "Vaar")
                putString("widget_lbl_app",       strings["panchang"]  ?: "Panchang")
            }

            val manager = AppWidgetManager.getInstance(context)
            val component = ComponentName(context, PanchangWidgetProvider::class.java)
            val ids = manager.getAppWidgetIds(component)
            ids.forEach { id -> PanchangWidgetProvider.updateAppWidget(context, manager, id) }
        } catch (_: Exception) {
            // Widget update is best-effort
        }
    }

    private fun fmt(hour: Int, minute: Int) = "%02d:%02d".format(hour, minute)

    private fun fmtDate(year: Int, month: Int, day: Int, locale: String): String {
        val cal = java.util.Calendar.getInstance().apply { set(year, month - 1, day) }
        val javaLocale = when (locale) {
            "hi" -> java.util.Locale.forLanguageTag("hi-IN")
            else -> java.util.Locale.ENGLISH
        }
        val sdf = java.text.SimpleDateFormat("EEEE, d MMM", javaLocale)
        return sdf.format(cal.time)
    }
}
