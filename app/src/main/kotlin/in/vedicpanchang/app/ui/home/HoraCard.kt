package `in`.vedicpanchang.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.l10n.PanchangLocalizer
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import androidx.compose.ui.graphics.luminance
import `in`.vedicpanchang.astronomy.HoraCalculator
import `in`.vedicpanchang.astronomy.HoraSlot
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

@Composable
fun HoraCard(
    panchang: PanchangModel,
    liveNow: Instant,
    strings: Map<String, String>,
    localizer: PanchangLocalizer,
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val weekday = panchang.date.dayOfWeek.isoDayNumber
    val slots = remember(panchang) {
        HoraCalculator.calculate(sunrise = panchang.sunrise, sunset = panchang.sunset, weekday = weekday)
    }
    // Pre-dawn gap fix: today's slots start at sunrise, leaving midnight→sunrise uncovered.
    // Prepend yesterday's night slots (sunset-24h → today's sunrise) so there's always a
    // current slot regardless of the time of day.
    val allSlots = remember(panchang) {
        val estYestSunrise = Instant.fromEpochMilliseconds(panchang.sunrise.toEpochMilliseconds() - 86_400_000L)
        val estYestSunset  = Instant.fromEpochMilliseconds(panchang.sunset.toEpochMilliseconds()  - 86_400_000L)
        val yesterdayWeekday = if (weekday == 1) 7 else weekday - 1
        val yesterdayNight = HoraCalculator.calculate(
            sunrise = estYestSunrise,
            sunset  = estYestSunset,
            weekday = yesterdayWeekday,
            nextSunrise = panchang.sunrise   // today's sunrise = yesterday's next sunrise
        ).filter { !it.isDay }
        yesterdayNight + slots
    }
    val currentIndex = allSlots.indexOfFirst { liveNow >= it.start && liveNow < it.end }
    val currentSlot = if (currentIndex >= 0) allSlots[currentIndex] else null
    val prevSlot    = if (currentIndex > 0) allSlots[currentIndex - 1] else null
    val nextSlot    = if (currentIndex in 0 until allSlots.lastIndex) allSlots[currentIndex + 1] else null

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Text(strings["hora"] ?: "Hora (Planetary Hours)", style = AppTextStyles.saffronLabel)
            Spacer(Modifier.height(12.dp))

            Column(verticalArrangement = Arrangement.spacedBy(6.dp)) {
                HoraRow(
                    slot = prevSlot,
                    label = strings["previous_hora"] ?: "Previous",
                    isCurrent = false,
                    localizer = localizer,
                    isDark = isDark
                )
                HoraRow(
                    slot = currentSlot,
                    label = strings["current_hora"] ?: "Current",
                    isCurrent = true,
                    localizer = localizer,
                    isDark = isDark
                )
                HoraRow(
                    slot = nextSlot,
                    label = strings["next_hora"] ?: "Next",
                    isCurrent = false,
                    localizer = localizer,
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
private fun HoraRow(
    slot: HoraSlot?,
    label: String,
    isCurrent: Boolean,
    localizer: PanchangLocalizer,
    isDark: Boolean
) {
    val accentColor = if (isCurrent) AppColors.Primary else MaterialTheme.colorScheme.onSurfaceVariant
    val borderColor = if (isCurrent) AppColors.Primary else MaterialTheme.colorScheme.outline
    val bgColor = when {
        isCurrent && isDark  -> AppColors.Primary.copy(alpha = 0.18f)
        isCurrent && !isDark -> AppColors.Primary.copy(alpha = 0.10f)
        isDark               -> AppColors.SurfaceVariant
        else                 -> AppColors.SurfaceVariantLight
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .then(if (isCurrent) Modifier.border(1.dp, AppColors.Primary, RoundedCornerShape(10.dp)) else Modifier)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // Planet symbol / logo
        Box(
            modifier = Modifier.size(40.dp),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = slot?.symbol ?: "—",
                style = AppTextStyles.bodySmall.copy(fontSize = 24.sp)
            )
        }

        Spacer(Modifier.width(12.dp))

        // Label + planet name
        Column(modifier = Modifier.weight(1f)) {
            Text(
                text = label,
                style = AppTextStyles.labelSmall.copy(color = accentColor, fontSize = 10.sp)
            )
            Text(
                text = if (slot != null) localizer.planetName(slot.planet) else "—",
                style = AppTextStyles.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 13.sp),
                maxLines = 1
            )
        }

        // Time range FROM–TO
        if (slot != null) {
            val tz = TimeZone.currentSystemDefault()
            val startLocal = slot.start.toLocalDateTime(tz)
            val endLocal   = slot.end.toLocalDateTime(tz)
            val timeRange  = localizer.numerals(
                "%02d:%02d – %02d:%02d".format(startLocal.hour, startLocal.minute, endLocal.hour, endLocal.minute)
            )
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(6.dp))
                    .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                    .padding(horizontal = 8.dp, vertical = 4.dp)
            ) {
                Text(
                    text = timeRange,
                    style = AppTextStyles.timeSmall.copy(color = accentColor, fontSize = 11.sp),
                    textAlign = TextAlign.End
                )
            }
        }
    }
}
