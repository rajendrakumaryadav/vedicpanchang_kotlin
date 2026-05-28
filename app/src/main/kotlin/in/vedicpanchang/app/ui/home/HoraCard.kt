package `in`.vedicpanchang.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.l10n.PanchangLocalizer
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
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
    val isDark = isSystemInDarkTheme()
    val weekday = panchang.date.dayOfWeek.isoDayNumber
    val slots = remember(panchang) {
        HoraCalculator.calculate(sunrise = panchang.sunrise, sunset = panchang.sunset, weekday = weekday)
    }
    val currentSlot = slots.firstOrNull { (liveNow >= it.start) && (liveNow < it.end) }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings["hora"] ?: "Hora (Planetary Hours)", style = AppTextStyles.saffronLabel)
                currentSlot?.let { slot ->
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(8.dp))
                            .background(AppColors.Primary.copy(alpha = 0.12f))
                            .padding(horizontal = 8.dp, vertical = 3.dp)
                    ) {
                        Text(
                            "${strings["current_hora"] ?: "Current"}: ${localizer.planetName(slot.planet)}",
                            style = AppTextStyles.labelSmall.copy(color = AppColors.Primary, fontSize = 10.sp)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            LazyVerticalGrid(
                columns = GridCells.Fixed(3),
                horizontalArrangement = Arrangement.spacedBy(6.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                modifier = Modifier.heightIn(max = 600.dp)
            ) {
                items(slots) { slot ->
                    HoraSlotCell(slot = slot, isCurrent = slot == currentSlot, localizer = localizer, isDark = isDark)
                }
            }
        }
    }
}

@Composable
private fun HoraSlotCell(slot: HoraSlot, isCurrent: Boolean, localizer: PanchangLocalizer, isDark: Boolean) {
    val tz = TimeZone.currentSystemDefault()
    val startLocal = slot.start.toLocalDateTime(tz)
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .background(
                when {
                    isCurrent -> AppColors.Primary.copy(alpha = if (isDark) 0.20f else 0.12f)
                    isDark    -> AppColors.SurfaceVariant
                    else      -> AppColors.SurfaceVariantLight
                }
            )
            .padding(horizontal = 6.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                localizer.planetName(slot.planet),
                style = AppTextStyles.bodySmall.copy(
                    fontWeight = androidx.compose.ui.text.font.FontWeight.Bold,
                    color = if (isCurrent) AppColors.Primary else MaterialTheme.colorScheme.onSurface,
                    fontSize = 12.sp
                )
            )
            Text(
                localizer.numerals("%02d:%02d".format(startLocal.hour, startLocal.minute)),
                style = AppTextStyles.timeSmall.copy(fontSize = 10.sp)
            )
            if (isCurrent) {
                Text("NOW", style = AppTextStyles.labelSmall.copy(color = AppColors.Primary, fontSize = 8.sp))
            }
        }
    }
}
