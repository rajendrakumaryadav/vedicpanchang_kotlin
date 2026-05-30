package `in`.vedicpanchang.app.service

import android.content.Context
import android.content.Intent
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.l10n.PanchangLocalizer
import kotlinx.datetime.LocalDate
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime

/**
 * Generates shareable Panchang text and launches Android share sheet.
 * Equivalent of share_service.dart.
 */
object ShareService {

    fun sharePanchang(context: Context, panchang: PanchangModel, localizer: PanchangLocalizer, locale: String) {
        val text = formatPanchang(panchang, localizer, locale)
        val intent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
        context.startActivity(Intent.createChooser(intent, null).apply {
            addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
        })
    }

    fun formatPanchang(panchang: PanchangModel, localizer: PanchangLocalizer, locale: String): String {
        val tz = TimeZone.currentSystemDefault()
        val dateStr = formatDate(panchang.date.year,
            panchang.date.month.number, panchang.date.day, locale)

        val rahuStart = panchang.rahuKaal.start.toLocalDateTime(tz)
        val rahuEnd = panchang.rahuKaal.end.toLocalDateTime(tz)
        val sunriseLocal = panchang.sunrise.toLocalDateTime(tz)
        val sunsetLocal = panchang.sunset.toLocalDateTime(tz)

        return buildString {
            appendLine("🕉️ ${title(locale)} — $dateStr")
            appendLine()
            appendLine("📅 ${localizer.vaarName(panchang)}")
            appendLine("🌙 ${label("tithi", locale)}: ${localizer.tithiDisplay(panchang)}")
            appendLine("⭐ ${label("nakshatra", locale)}: ${localizer.nakshatraName(panchang)}")
            appendLine("☀️ ${label("yoga", locale)}: ${localizer.yogaWithAuspicious(panchang)}")
            appendLine("🔀 ${label("karana", locale)}: ${localizer.karanaName(panchang)}")
            appendLine()
            appendLine(
                "🌅 ${label("sunrise", locale)}: %02d:%02d  🌇 ${label("sunset", locale)}: %02d:%02d".format(
                    sunriseLocal.hour, sunriseLocal.minute,
                    sunsetLocal.hour, sunsetLocal.minute
                )
            )
            appendLine(
                "⚠️ ${label("rahu", locale)}: %02d:%02d – %02d:%02d".format(
                    rahuStart.hour, rahuStart.minute,
                    rahuEnd.hour, rahuEnd.minute
                )
            )
            if (panchang.hasFestivals) {
                appendLine()
                appendLine("🪔 ${localizer.festivals(panchang).joinToString(", ")}")
            }
            appendLine()
            append("— via Vedic Panchang")
        }
    }

    private fun title(locale: String) = when (locale) {
        "hi" -> "वैदिक पंचांग"
        "sa" -> "वैदिकपञ्चाङ्गम्"
        else -> "Vedic Panchang"
    }

    private fun label(key: String, locale: String): String {
        val labels = mapOf(
            "tithi"    to mapOf("en" to "Tithi",      "hi" to "तिथि",       "sa" to "तिथिः"),
            "nakshatra" to mapOf("en" to "Nakshatra",  "hi" to "नक्षत्र",    "sa" to "नक्षत्रम्"),
            "yoga"     to mapOf("en" to "Yoga",       "hi" to "योग",        "sa" to "योगः"),
            "karana"   to mapOf("en" to "Karana",     "hi" to "करण",        "sa" to "करणम्"),
            "sunrise"  to mapOf("en" to "Sunrise",    "hi" to "सूर्योदय",   "sa" to "सूर्योदयः"),
            "sunset"   to mapOf("en" to "Sunset",     "hi" to "सूर्यास्त",  "sa" to "सूर्यास्तः"),
            "rahu"     to mapOf("en" to "Rahu Kaal",  "hi" to "राहु काल",   "sa" to "राहुकालः"),
        )
        return labels[key]?.get(locale) ?: labels[key]?.get("en") ?: key
    }

    private fun formatDate(year: Int, month: Int, day: Int, locale: String): String {
        val javaLocale = when (locale) {
            "hi", "sa" -> java.util.Locale.forLanguageTag("hi-IN")
            else -> java.util.Locale.ENGLISH
        }
        val cal = java.util.Calendar.getInstance().apply { set(year, month - 1, day) }
        val fmt = java.text.SimpleDateFormat("d MMMM yyyy, EEEE", javaLocale)
        return fmt.format(cal.time)
    }
}
