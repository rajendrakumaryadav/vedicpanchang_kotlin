package `in`.vedicpanchang.app.service

import `in`.vedicpanchang.app.data.datasource.AppPreferences
import `in`.vedicpanchang.app.data.datasource.FestivalData
import kotlin.time.Clock
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.atTime
import kotlinx.datetime.plus
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Locale
import javax.inject.Inject
import javax.inject.Singleton

private data class ReminderRule(val daysBefore: Int, val hour: Int, val minute: Int)

private val EKADASHI_IDS = setOf("Ekadashi", "Dev Uthani Ekadashi", "Rama Ekadashi")
private val PURNIMA_IDS = setOf("Purnima", "Guru Purnima", "Buddha Purnima", "Sharad Purnima", "Kartik Purnima")
private const val NOTIF_ID_START = 100
private const val NOTIF_ID_END = 9999

/**
 * Business-logic scheduler equivalent of notification_scheduler.dart.
 * Scans the next 30 days and schedules festival notifications per user preferences.
 * Call from Dispatchers.Default (CPU-bound Panchang calculations).
 */
@Singleton
class NotificationScheduler @Inject constructor(
    private val panchangService: PanchangService,
    private val notificationService: NotificationService
) {
    suspend fun reschedule(
        lat: Double,
        lon: Double,
        locationName: String,
        settings: AppPreferences.NotificationSettings,
        locale: String
    ): Int {
        notificationService.cancelRange(NOTIF_ID_START, NOTIF_ID_END)

        if (!settings.ekadashi && !settings.purnima && !settings.amavasya && !settings.festivals) {
            return 0
        }

        val rules = buildRules(settings)
        if (rules.isEmpty()) return 0

        val tz = TimeZone.currentSystemDefault()
        val today = Clock.System.now().toLocalDateTime(tz).date
        var notifId = NOTIF_ID_START
        var scheduledCount = 0
        val nowMs = System.currentTimeMillis()

        for (i in 0..30) {
            val date = today.plus(i, DateTimeUnit.DAY)
            val panchang = panchangService.calculate(date, lat, lon, locationName)
            if (panchang.festivals.isEmpty()) continue

            for (festival in panchang.festivals) {
                if (!shouldNotify(festival, settings)) continue

                val localizedName = FestivalData.getLocalizedName(festival, locale)

                for (rule in rules) {
                    if (i < rule.daysBefore) continue

                    val triggerDate = date.plus(-rule.daysBefore, DateTimeUnit.DAY)
                    val triggerInstant = triggerDate.atTime(rule.hour, rule.minute)
                        .toInstant(tz)
                    val triggerMs = triggerInstant.toEpochMilliseconds()
                    if (triggerMs <= nowMs) continue

                    val title = notifTitle(localizedName, rule.daysBefore, locale)
                    val body = if (rule.daysBefore == 0) sameDayBody(locale)
                               else dateLabel(date, locale)

                    if (notificationService.schedule(notifId, title, body, triggerMs)) {
                        scheduledCount++
                    }
                    notifId++
                    if (notifId > NOTIF_ID_END) return scheduledCount
                }
            }
        }
        return scheduledCount
    }

    private fun buildRules(s: AppPreferences.NotificationSettings): List<ReminderRule> = buildList {
        if (s.days3) add(ReminderRule(3, 9, 0))
        if (s.day1)  add(ReminderRule(1, 8, 0))
        if (s.sameDay) add(ReminderRule(0, 6, 0))
    }

    private fun shouldNotify(festival: String, s: AppPreferences.NotificationSettings): Boolean {
        if (festival in EKADASHI_IDS) return s.ekadashi
        if (festival in PURNIMA_IDS) return s.purnima
        if (festival == "Amavasya") return s.amavasya
        return s.festivals
    }

    private fun notifTitle(name: String, daysBefore: Int, locale: String): String = when (daysBefore) {
        0 -> "🪔 $name"
        1 -> "🪔 $name — ${tomorrowLabel(locale)}"
        else -> "🪔 $name — ${daysAwayLabel(daysBefore, locale)}"
    }

    private fun sameDayBody(locale: String) = when (locale) {
        "hi" -> "आज का त्योहार"
        "sa" -> "अद्य उत्सवः"
        else -> "Today's festival"
    }

    private fun tomorrowLabel(locale: String) = when (locale) {
        "hi" -> "कल"
        "sa" -> "श्वः"
        else -> "Tomorrow"
    }

    private fun daysAwayLabel(days: Int, locale: String) = when (locale) {
        "hi" -> "$days दिन बाद"
        "sa" -> "$days दिनानि शेषाणि"
        else -> "in $days days"
    }

    private fun dateLabel(date: LocalDate, locale: String): String {
        val javaLocale = when (locale) {
            "hi", "sa" -> Locale("hi", "IN")
            else -> Locale.ENGLISH
        }
        val fmt = SimpleDateFormat("d MMMM, EEEE", javaLocale)
        val cal = java.util.Calendar.getInstance().apply {
            set(date.year, date.monthNumber - 1, date.dayOfMonth)
        }
        return fmt.format(cal.time)
    }
}
