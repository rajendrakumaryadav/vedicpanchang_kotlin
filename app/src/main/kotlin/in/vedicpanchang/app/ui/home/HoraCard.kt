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
    val currentIndex = slots.indexOfFirst { (liveNow >= it.start) && (liveNow < it.end) }
    val currentSlot = if (currentIndex >= 0) slots[currentIndex] else null
    val prevSlot = if (currentIndex > 0) slots[currentIndex - 1] else null
    val nextSlot = if (currentIndex >= 0 && currentIndex < slots.lastIndex) slots[currentIndex + 1] else null

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

            prevSlot?.let { slot ->
                HoraSlotRow(slot = slot, isCurrent = false, label = strings["previous_hora"] ?: "Previous", localizer = localizer, isDark = isDark)
                Spacer(Modifier.height(6.dp))
            }
            currentSlot?.let { slot ->
                CurrentHoraRow(slot = slot, strings = strings, localizer = localizer, isDark = isDark)
                Spacer(Modifier.height(6.dp))
            }
            nextSlot?.let { slot ->
                HoraSlotRow(slot = slot, isCurrent = false, label = strings["next_hora"] ?: "Next", localizer = localizer, isDark = isDark)
            }
        }
    }
}

@Composable
private fun CurrentHoraRow(
    slot: HoraSlot,
    strings: Map<String, String>,
    localizer: PanchangLocalizer,
    isDark: Boolean,
) {
    val tz = TimeZone.currentSystemDefault()
    val startLocal = slot.start.toLocalDateTime(tz)
    val endLocal = slot.end.toLocalDateTime(tz)
    val timeRange = localizer.numerals(
        "%02d:%02d – %02d:%02d".format(startLocal.hour, startLocal.minute, endLocal.hour, endLocal.minute)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) AppColors.SurfaceVariant else AppColors.SurfaceVariantLight)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(slot.symbol, style = AppTextStyles.bodySmall.copy(fontSize = 22.sp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                strings["current_hora"] ?: "Current Hora",
                style = AppTextStyles.labelSmall.copy(color = AppColors.Primary, fontSize = 10.sp)
            )
            Text(
                localizer.planetName(slot.planet),
                style = AppTextStyles.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, AppColors.Primary, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                timeRange,
                style = AppTextStyles.timeSmall.copy(color = AppColors.Primary, fontSize = 12.sp)
            )
        }
    }
}

@Composable
private fun HoraSlotRow(slot: HoraSlot, isCurrent: Boolean, label: String, localizer: PanchangLocalizer, isDark: Boolean) {
    val tz = TimeZone.currentSystemDefault()
    val startLocal = slot.start.toLocalDateTime(tz)
    val endLocal = slot.end.toLocalDateTime(tz)
    val timeRange = localizer.numerals(
        "%02d:%02d – %02d:%02d".format(startLocal.hour, startLocal.minute, endLocal.hour, endLocal.minute)
    )
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(10.dp))
            .background(if (isDark) AppColors.SurfaceVariant else AppColors.SurfaceVariantLight)
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(slot.symbol, style = AppTextStyles.bodySmall.copy(fontSize = 22.sp))
        Spacer(Modifier.width(10.dp))
        Column(modifier = Modifier.weight(1f)) {
            Text(
                label,
                style = AppTextStyles.labelSmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    fontSize = 10.sp
                )
            )
            Text(
                localizer.planetName(slot.planet),
                style = AppTextStyles.bodySmall.copy(fontWeight = FontWeight.Bold, fontSize = 14.sp)
            )
        }
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(8.dp))
                .border(1.dp, MaterialTheme.colorScheme.outline, RoundedCornerShape(8.dp))
                .padding(horizontal = 10.dp, vertical = 6.dp)
        ) {
            Text(
                timeRange,
                style = AppTextStyles.timeSmall.copy(fontSize = 12.sp)
            )
        }
    }
}
