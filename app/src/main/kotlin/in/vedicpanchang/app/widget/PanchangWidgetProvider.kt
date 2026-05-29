package `in`.vedicpanchang.app.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.content.SharedPreferences
import android.view.View
import android.widget.RemoteViews
import `in`.vedicpanchang.app.MainActivity
import `in`.vedicpanchang.app.R

/**
 * Android Home Screen Widget displaying today's Panchang summary.
 * Data is written by WidgetService into "HomeWidgetPreferences" SharedPreferences.
 * Labels and values are both localized by WidgetService — no string resources needed here.
 */
class PanchangWidgetProvider : AppWidgetProvider() {

    override fun onUpdate(
        context: Context,
        appWidgetManager: AppWidgetManager,
        appWidgetIds: IntArray
    ) {
        for (id in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, id)
        }
    }

    companion object {
        private const val PREFS_NAME = "HomeWidgetPreferences"

        fun updateAppWidget(
            context: Context,
            appWidgetManager: AppWidgetManager,
            appWidgetId: Int
        ) {
            val p: SharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            val views = RemoteViews(context.packageName, R.layout.panchang_widget)

            // ── Date / header ─────────────────────────────────────────────────
            views.setTextViewText(R.id.widget_date,     p.getString("widget_date",     "Vedic Panchang") ?: "Vedic Panchang")
            views.setTextViewText(R.id.widget_app_name, p.getString("widget_lbl_app",  "Panchang")       ?: "Panchang")

            val location = p.getString("widget_location", "") ?: ""
            if (location.isNotEmpty()) {
                views.setTextViewText(R.id.widget_location, location)
                views.setViewVisibility(R.id.widget_location, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_location, View.GONE)
            }

            // ── Festival ──────────────────────────────────────────────────────
            val festival = p.getString("widget_festival", "") ?: ""
            if (festival.isNotEmpty()) {
                views.setTextViewText(R.id.widget_festival, "🪔  $festival")
                views.setViewVisibility(R.id.widget_festival, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_festival, View.GONE)
            }

            // ── Panchang element labels ────────────────────────────────────────
            views.setTextViewText(R.id.widget_lbl_tithi,     p.getString("widget_lbl_tithi",     "Tithi")     ?: "Tithi")
            views.setTextViewText(R.id.widget_lbl_nakshatra, p.getString("widget_lbl_nakshatra", "Nakshatra") ?: "Nakshatra")
            views.setTextViewText(R.id.widget_lbl_yoga,      p.getString("widget_lbl_yoga",      "Yoga")      ?: "Yoga")
            views.setTextViewText(R.id.widget_lbl_karana,    p.getString("widget_lbl_karana",    "Karana")    ?: "Karana")

            // ── Panchang element values ────────────────────────────────────────
            views.setTextViewText(R.id.widget_tithi,     p.getString("widget_tithi",     "Loading…") ?: "Loading…")
            views.setTextViewText(R.id.widget_nakshatra, p.getString("widget_nakshatra", "--")        ?: "--")
            views.setTextViewText(R.id.widget_yoga,      p.getString("widget_yoga",      "--")        ?: "--")
            views.setTextViewText(R.id.widget_karana,    p.getString("widget_karana",    "--")        ?: "--")

            // ── Vaar ──────────────────────────────────────────────────────
            views.setTextViewText(R.id.widget_lbl_vaar, p.getString("widget_lbl_vaar", "Vaar") ?: "Vaar")
            views.setTextViewText(R.id.widget_vaar,     p.getString("widget_vaar",     "--")   ?: "--")

            // ── Sun / Moon labels ─────────────────────────────────────────────
            views.setTextViewText(R.id.widget_lbl_sunrise,  p.getString("widget_lbl_sunrise",  "Sunrise")  ?: "Sunrise")
            views.setTextViewText(R.id.widget_lbl_sunset,   p.getString("widget_lbl_sunset",   "Sunset")   ?: "Sunset")
            views.setTextViewText(R.id.widget_lbl_moonrise, p.getString("widget_lbl_moonrise", "Moonrise") ?: "Moonrise")
            views.setTextViewText(R.id.widget_lbl_moonset,  p.getString("widget_lbl_moonset",  "Moonset")  ?: "Moonset")

            // ── Sun / Moon times ──────────────────────────────────────────────
            views.setTextViewText(R.id.widget_sunrise,  p.getString("widget_sunrise",  "--:--") ?: "--:--")
            views.setTextViewText(R.id.widget_sunset,   p.getString("widget_sunset",   "--:--") ?: "--:--")
            views.setTextViewText(R.id.widget_moonrise, p.getString("widget_moonrise", "--:--") ?: "--:--")
            views.setTextViewText(R.id.widget_moonset,  p.getString("widget_moonset",  "--:--") ?: "--:--")

            // ── Launch app on tap ─────────────────────────────────────────────
            val intent = Intent(context, MainActivity::class.java)
            val pendingIntent = PendingIntent.getActivity(
                context, 0, intent,
                PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
            )
            views.setOnClickPendingIntent(R.id.widget_root, pendingIntent)

            appWidgetManager.updateAppWidget(appWidgetId, views)
        }
    }
}
