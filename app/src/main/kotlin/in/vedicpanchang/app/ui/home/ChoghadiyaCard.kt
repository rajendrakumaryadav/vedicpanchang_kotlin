package `in`.vedicpanchang.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.VerticalDivider
import androidx.compose.ui.graphics.luminance
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
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.luminance() < 0.5f
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
            .background(colors.surface)
            .padding(16.dp)
    ) {
        Text(
            text = strings["choghadiya"] ?: "Choghadiya",
            style = AppTextStyles.saffronLabel.copy(fontSize = 14.sp)
        )
        Spacer(Modifier.height(16.dp))

        Row(modifier = Modifier.fillMaxWidth()) {
            // Day column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings["day_choghadiya"] ?: "Day",
                    style = AppTextStyles.labelLarge.copy(
                        color = AppColors.Primary,
                        fontSize = 11.sp
                    )
                )
                Spacer(Modifier.height(10.dp))
                daySlots.forEach { slot ->
                    ChoghadiyaSlotCompact(
                        slot = slot,
                        isCurrent = slot == currentSlot,
                        localizer = localizer
                    )
                }
            }

            VerticalDivider(
                modifier = Modifier
                    .padding(horizontal = 12.dp)
                    .fillMaxHeight(),
                thickness = 0.5.dp,
                color = colors.onSurface.copy(alpha = 0.12f)
            )

            // Night column
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings["night_choghadiya"] ?: "Night",
                    style = AppTextStyles.labelLarge.copy(
                        color = if (isDark) Color(0xFFFFD700) else AppColors.SecondaryOnLight,
                        fontSize = 11.sp
                    )
                )
                Spacer(Modifier.height(10.dp))
                nightSlots.forEach { slot ->
                    ChoghadiyaSlotCompact(
                        slot = slot,
                        isCurrent = slot == currentSlot,
                        localizer = localizer
                    )
                }
            }
        }
    }
}

@Composable
private fun ChoghadiyaSlotCompact(
    slot: ChoghadiyaSlot,
    isCurrent: Boolean,
    localizer: PanchangLocalizer
) {
    val tz = TimeZone.currentSystemDefault()
    val startLocal = slot.start.toLocalDateTime(tz)
    val endLocal = slot.end.toLocalDateTime(tz)
    val timeStr = localizer.numerals(
        "%02d:%02d–%02d:%02d".format(
            startLocal.hour, startLocal.minute,
            endLocal.hour, endLocal.minute
        )
    )

    val colors = MaterialTheme.colorScheme
    val typeColor = when (slot.type) {
        ChoghadiyaType.VERY_AUSPICIOUS -> Color(0xFF4CAF50)
        ChoghadiyaType.AUSPICIOUS      -> Color(0xFF81C784)
        ChoghadiyaType.GOOD            -> Color(0xFF64B5F6)
        ChoghadiyaType.NEUTRAL         -> Color(0xFFFFB300)
        ChoghadiyaType.INAUSPICIOUS    -> Color(0xFFE53935)
    }

    val highlightShape = RoundedCornerShape(8.dp)
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
            .then(
                if (isCurrent) Modifier
                    .clip(highlightShape)
                    .background(Color(0xFFFF9800).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f), highlightShape)
                else Modifier
            )
            .padding(vertical = 4.dp, horizontal = if (isCurrent) 6.dp else 0.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(7.dp)
                .clip(RoundedCornerShape(50))
                .background(typeColor)
        )
        Spacer(Modifier.width(8.dp))
        Column {
            Text(
                text = localizer.choghadiyaName(slot.name),
                style = AppTextStyles.bodyMedium.copy(
                    color = if (isCurrent) colors.onSurface else colors.onSurfaceVariant,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                    fontSize = 12.sp
                )
            )
            Text(
                text = timeStr,
                style = AppTextStyles.timeSmall.copy(
                    color = if (isCurrent) colors.onSurface else colors.onSurfaceVariant.copy(alpha = 0.6f),
                    fontSize = 10.sp
                )
            )
        }
    }
}
