package `in`.vedicpanchang.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.l10n.PanchangLocalizer
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Duration.Companion.hours

@Composable
fun VedicCalendarCard(
    panchang: PanchangModel,
    strings: Map<String, String>,
    localizer: PanchangLocalizer
) {
    val isDark = isSystemInDarkTheme()
    // Using ordinal + 1 for month number
    val monthNum = panchang.date.month.ordinal + 1
    val vikramYear = localizer.vikramSamvatYear(panchang.date.year, monthNum)
    val shakaYear = localizer.shakaSamvatYear(panchang.date.year, monthNum, panchang.date.dayOfMonth)
    val kaliYear = shakaYear + 3179
    val vedicMonth = localizer.vedicMonthName(panchang)
    
    val currentPrahar = remember(panchang) { calculatePrahar(panchang, strings, localizer) }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Surface)
            .padding(16.dp)
    ) {
        Text(
            text = strings["vedic_calendar"] ?: "Vedic Calendar",
            style = AppTextStyles.saffronLabel.copy(fontSize = 14.sp)
        )
        Spacer(Modifier.height(16.dp))

        // Prahar info
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("⌚", fontSize = 18.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = "${strings["prahar_label"] ?: "Prahar"}: ",
                style = AppTextStyles.bodyMedium.copy(color = Color.White)
            )
            Text(
                text = currentPrahar,
                style = AppTextStyles.bodyMedium.copy(color = AppColors.Secondary, fontWeight = FontWeight.Bold)
            )
        }

        Spacer(Modifier.height(16.dp))

        // 2x2 Grid
        Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridItem(
                    icon = "🔱",
                    label = strings["vikram_samvat"] ?: "Vikram Samvat",
                    value = localizer.numerals(vikramYear.toString()),
                    subValue = vedicMonth,
                    modifier = Modifier.weight(1f)
                )
                GridItem(
                    icon = "🏛️",
                    label = strings["shaka_samvat"] ?: "Shaka Samvat",
                    value = localizer.numerals(shakaYear.toString()),
                    subValue = vedicMonth,
                    modifier = Modifier.weight(1f)
                )
            }
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                GridItem(
                    icon = "🕉️",
                    label = strings["kali_yuga_year"] ?: "Kali Yuga Year",
                    value = localizer.numerals(kaliYear.toString()),
                    subValue = vedicMonth,
                    modifier = Modifier.weight(1f)
                )
                GridItem(
                    icon = "🗓️",
                    label = strings["vedic_month"] ?: "Vedic Month",
                    value = vedicMonth,
                    subValue = "",
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Composable
fun GridItem(
    icon: String,
    label: String,
    value: String,
    subValue: String,
    modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(Color(0xFF231F35))
            .padding(12.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(label, style = AppTextStyles.bodySmall.copy(color = AppColors.TextMuted, fontSize = 10.sp))
            }
            Spacer(Modifier.height(8.dp))
            Text(value, style = AppTextStyles.displaySmall.copy(fontSize = 18.sp, color = AppColors.Primary))
            if (subValue.isNotEmpty()) {
                Text(subValue, style = AppTextStyles.bodySmall.copy(color = AppColors.TextMuted, fontSize = 10.sp))
            }
        }
    }
}

private fun calculatePrahar(panchang: PanchangModel, strings: Map<String, String>, localizer: PanchangLocalizer): String {
    val now = Clock.System.now()
    val sunrise = panchang.sunrise
    val sunset = panchang.sunset
    val tz = TimeZone.currentSystemDefault()
    
    val dayDuration = sunset - sunrise
    val nightDuration = 24.hours - dayDuration
    
    val dayPraharLen = dayDuration / 4
    val nightPraharLen = nightDuration / 4
    
    val (name, start, end) = when {
        now < sunrise -> {
            val prevSunset = sunrise - nightDuration
            val diff = now - prevSunset
            val idx = (diff.inWholeSeconds / nightPraharLen.inWholeSeconds).toInt().coerceIn(0, 3)
            val names = listOf("Pradosha", "Nishitha", "Triyama", "Ushah")
            Triple(names[idx], prevSunset + (nightPraharLen * idx.toDouble()), prevSunset + (nightPraharLen * (idx + 1).toDouble()))
        }
        now < sunset -> {
            val diff = now - sunrise
            val idx = (diff.inWholeSeconds / dayPraharLen.inWholeSeconds).toInt().coerceIn(0, 3)
            val names = listOf("Purvahna", "Madhyahna", "Aparahna", "Sayahna")
            Triple(names[idx], sunrise + (dayPraharLen * idx.toDouble()), sunrise + (dayPraharLen * (idx + 1).toDouble()))
        }
        else -> {
            val diff = now - sunset
            val idx = (diff.inWholeSeconds / nightPraharLen.inWholeSeconds).toInt().coerceIn(0, 3)
            val names = listOf("Pradosha", "Nishitha", "Triyama", "Ushah")
            Triple(names[idx], sunset + (nightPraharLen * idx.toDouble()), (sunset + (nightPraharLen * (idx + 1).toDouble())))
        }
    }
    
    val startT = start.toLocalDateTime(tz)
    val endT = end.toLocalDateTime(tz)
    val timeRange = localizer.numerals("(%02d:%02d – %02d:%02d)".format(startT.hour, startT.minute, endT.hour, endT.minute))

    return "${strings[name.lowercase()] ?: name} $timeRange"
}
