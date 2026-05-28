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
            val prefs: SharedPreferences =
                context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE)

            val tithi = prefs.getString("widget_tithi", "Loading…") ?: "Loading…"
            val nakshatra = prefs.getString("widget_nakshatra", "--") ?: "--"
            val yoga = prefs.getString("widget_yoga", "--") ?: "--"
            val sunrise = prefs.getString("widget_sunrise", "--:--") ?: "--:--"
            val sunset = prefs.getString("widget_sunset", "--:--") ?: "--:--"
            val date = prefs.getString("widget_date", "Vedic Panchang") ?: "Vedic Panchang"
            val location = prefs.getString("widget_location", "") ?: ""
            val festival = prefs.getString("widget_festival", "") ?: ""

            val views = RemoteViews(context.packageName, R.layout.panchang_widget)

            views.setTextViewText(R.id.widget_tithi, tithi)
            views.setTextViewText(R.id.widget_nakshatra, nakshatra)
            views.setTextViewText(R.id.widget_yoga, yoga)
            views.setTextViewText(R.id.widget_sunrise, sunrise)
            views.setTextViewText(R.id.widget_sunset, sunset)
            views.setTextViewText(R.id.widget_date, date)

            if (location.isNotEmpty()) {
                views.setTextViewText(R.id.widget_location, location)
                views.setViewVisibility(R.id.widget_location, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_location, View.GONE)
            }

            if (festival.isNotEmpty()) {
                views.setTextViewText(R.id.widget_festival, "🪔 $festival")
                views.setViewVisibility(R.id.widget_festival, View.VISIBLE)
            } else {
                views.setViewVisibility(R.id.widget_festival, View.GONE)
            }

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
