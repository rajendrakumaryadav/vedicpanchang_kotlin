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
import kotlinx.datetime.Instant
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

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                HoraCell(
                    slot = prevSlot,
                    label = strings["previous_hora"] ?: "Previous",
                    isCurrent = false,
                    modifier = Modifier.weight(1f),
                    localizer = localizer,
                    isDark = isDark
                )
                HoraCell(
                    slot = currentSlot,
                    label = strings["current_hora"] ?: "Current",
                    isCurrent = true,
                    modifier = Modifier.weight(1f),
                    localizer = localizer,
                    isDark = isDark
                )
                HoraCell(
                    slot = nextSlot,
                    label = strings["next_hora"] ?: "Next",
                    isCurrent = false,
                    modifier = Modifier.weight(1f),
                    localizer = localizer,
                    isDark = isDark
                )
            }
        }
    }
}

@Composable
private fun HoraCell(
    slot: HoraSlot?,
    label: String,
    isCurrent: Boolean,
    modifier: Modifier,
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

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(bgColor)
            .then(if (isCurrent) Modifier.border(1.dp, AppColors.Primary, RoundedCornerShape(10.dp)) else Modifier)
            .padding(horizontal = 8.dp, vertical = 10.dp),
        contentAlignment = Alignment.Center
    ) {
        if (slot == null) {
            Text("—", style = AppTextStyles.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant))
        } else {
            val tz = TimeZone.currentSystemDefault()
            val startLocal = slot.start.toLocalDateTime(tz)
            val endLocal   = slot.end.toLocalDateTime(tz)
            val timeRange  = localizer.numerals(
                "%02d:%02d\n%02d:%02d".format(startLocal.hour, startLocal.minute, endLocal.hour, endLocal.minute)
            )
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    label,
                    style = AppTextStyles.labelSmall.copy(color = accentColor, fontSize = 9.sp)
                )
                Spacer(Modifier.height(4.dp))
                Text(slot.symbol, style = AppTextStyles.bodySmall.copy(fontSize = 20.sp))
                Spacer(Modifier.height(2.dp))
                Text(
                    localizer.planetName(slot.planet),
                    style = AppTextStyles.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 12.sp),
                    maxLines = 1
                )
                Spacer(Modifier.height(4.dp))
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .border(1.dp, borderColor, RoundedCornerShape(6.dp))
                        .padding(horizontal = 6.dp, vertical = 3.dp)
                ) {
                    Text(
                        timeRange,
                        style = AppTextStyles.timeSmall.copy(color = accentColor, fontSize = 9.sp),
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
