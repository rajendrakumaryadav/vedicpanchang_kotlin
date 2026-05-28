package `in`.vedicpanchang.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
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
import `in`.vedicpanchang.astronomy.ChoghadiyaCalculator
import `in`.vedicpanchang.astronomy.ChoghadiyaSlot
import `in`.vedicpanchang.astronomy.ChoghadiyaType
import kotlin.time.Clock
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.toLocalDateTime

@Composable
fun ChoghadiyaCard(
    panchang: PanchangModel,
    strings: Map<String, String>,
    locale: String,
    localizer: PanchangLocalizer
) {
    val isDark = isSystemInDarkTheme()
    val now = Clock.System.now()
    val weekday = panchang.date.dayOfWeek.isoDayNumber
    val slots = remember(panchang) {
        ChoghadiyaCalculator.calculate(
            sunrise = panchang.sunrise,
            sunset = panchang.sunset,
            weekday = weekday
        )
    }
    val currentSlot = remember(now, slots) { ChoghadiyaCalculator.currentSlot(slots, now) }
    val daySlots = slots.filter { it.isDay }
    val nightSlots = slots.filter { !it.isDay }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(AppColors.Surface)
            .padding(16.dp)
    ) {
        Text(
            text = strings["choghadiya"] ?: "Choghadiya",
            style = AppTextStyles.saffronLabel.copy(fontSize = 14.sp)
        )
        Spacer(Modifier.height(16.dp))

        // Day Choghadiya
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("☀️", fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = strings["day_choghadiya"] ?: "Day Choghadiya",
                style = AppTextStyles.labelLarge.copy(color = AppColors.Primary, fontSize = 12.sp)
            )
        }
        Spacer(Modifier.height(12.dp))
        daySlots.forEach { slot ->
            ChoghadiyaSlotRow(slot = slot, isCurrent = slot == currentSlot, localizer = localizer)
        }

        Spacer(Modifier.height(16.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f), thickness = 0.5.dp)
        Spacer(Modifier.height(16.dp))

        // Night Choghadiya
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🌙", fontSize = 14.sp)
            Spacer(Modifier.width(8.dp))
            Text(
                text = strings["night_choghadiya"] ?: "Night Choghadiya",
                style = AppTextStyles.labelLarge.copy(color = Color(0xFFFFD700), fontSize = 12.sp)
            )
        }
        Spacer(Modifier.height(12.dp))
        nightSlots.forEach { slot ->
            ChoghadiyaSlotRow(slot = slot, isCurrent = slot == currentSlot, localizer = localizer)
        }
    }
}

@Composable
private fun ChoghadiyaSlotRow(
    slot: ChoghadiyaSlot,
    isCurrent: Boolean,
    localizer: PanchangLocalizer
) {
    val tz = TimeZone.currentSystemDefault()
    val startLocal = slot.start.toLocalDateTime(tz)
    val endLocal = slot.end.toLocalDateTime(tz)
    val timeStr = localizer.numerals("%02d:%02d – %02d:%02d".format(startLocal.hour, startLocal.minute, endLocal.hour, endLocal.minute))

    val typeColor = when (slot.type) {
        ChoghadiyaType.VERY_AUSPICIOUS -> Color(0xFF4CAF50)
        ChoghadiyaType.AUSPICIOUS      -> Color(0xFF81C784)
        ChoghadiyaType.GOOD            -> Color(0xFF64B5F6)
        ChoghadiyaType.NEUTRAL         -> Color(0xFFFFB300)
        ChoghadiyaType.INAUSPICIOUS    -> Color(0xFFE53935)
    }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 6.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(8.dp)
                .clip(RoundedCornerShape(50))
                .background(typeColor)
        )
        Spacer(Modifier.width(16.dp))
        Text(
            text = slot.name,
            style = AppTextStyles.bodyMedium.copy(
                color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.7f),
                fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
            ),
            modifier = Modifier.width(100.dp)
        )
        Spacer(Modifier.weight(1f))
        Text(
            text = timeStr,
            style = AppTextStyles.timeSmall.copy(
                color = if (isCurrent) Color.White else Color.White.copy(alpha = 0.5f),
                fontSize = 12.sp
            )
        )
    }
}
