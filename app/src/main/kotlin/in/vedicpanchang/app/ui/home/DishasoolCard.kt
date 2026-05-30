package `in`.vedicpanchang.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles

private val cardShape = RoundedCornerShape(12.dp)

// Dishasool (Direction to Avoid) data keyed by weekday index 0=Sunday..6=Saturday
private val DISHASOOL_DIRECTION_KEYS = listOf(
    "dir_west",   // Sunday
    "dir_east",   // Monday
    "dir_north",  // Tuesday
    "dir_north",  // Wednesday
    "dir_south",  // Thursday
    "dir_west",   // Friday
    "dir_east"    // Saturday
)

// Direction emoji compass indicators
private val DIRECTION_EMOJI = mapOf(
    "dir_north" to "⬆",
    "dir_south" to "⬇",
    "dir_east"  to "➡",
    "dir_west"  to "⬅"
)

// Parihaar string key index matches weekday 0=Sunday..6=Saturday
private fun parihaarKey(vaarIndex: Int) = "parihaar_$vaarIndex"

@Composable
fun DishasoolCard(
    panchang: PanchangModel,
    strings: Map<String, String>
) {
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.luminance() < 0.5f
    val borderColor = if (isDark)
        AppColors.Primary.copy(alpha = 0.45f)
    else
        Color(0xFFCBA35C).copy(alpha = 0.55f)

    // Convert kotlinx-datetime DayOfWeek to vaarIndex (0=Sunday..6=Saturday)
    val vaarIndex = (panchang.date.dayOfWeek.ordinal + 1) % 7
    val dirKey = DISHASOOL_DIRECTION_KEYS[vaarIndex]
    val dirText = strings[dirKey] ?: dirKey
    val dirEmoji = DIRECTION_EMOJI[dirKey] ?: "🧭"
    val parihaarText = strings[parihaarKey(vaarIndex)] ?: ""

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(cardShape)
            .border(1.dp, borderColor, cardShape)
            .background(colors.surface)
            .padding(32.dp)
    ) {
        // Card header
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = strings["dishasool"] ?: "Dishasool",
                style = AppTextStyles.saffronLabel.copy(fontSize = 14.sp)
            )
            Text(
                text = strings["dishasool_subtitle"] ?: "Inauspicious direction today",
                style = AppTextStyles.bodySmall.copy(
                    color = if (isDark) AppColors.TextSecondary else AppColors.TextSecondaryLight,
                    fontSize = 11.sp
                )
            )
        }

        Spacer(Modifier.height(16.dp))

        // Direction block
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDark) AppColors.Inauspicious.copy(alpha = 0.12f)
                    else Color(0xFFFFF3E0)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Column {
                Text(
                    text = (strings["direction_to_avoid"] ?: "Direction to Avoid").uppercase(),
                    style = AppTextStyles.labelSmall.copy(
                        color = if (isDark) AppColors.TextSecondary else AppColors.TextSecondaryLight,
                        fontSize = 10.sp
                    )
                )
                Spacer(Modifier.height(4.dp))
                Text(
                    text = dirText,
                    style = AppTextStyles.bodyMedium.copy(
                        fontWeight = FontWeight.Bold,
                        fontSize = 22.sp,
                        color = AppColors.Inauspicious
                    )
                )
            }
            Text(text = dirEmoji, fontSize = 36.sp)
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = if (isDark) AppColors.CardBorder else AppColors.CardBorderLight)
        Spacer(Modifier.height(12.dp))

        // Parihaar block
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(12.dp))
                .background(
                    if (isDark) AppColors.Auspicious.copy(alpha = 0.10f)
                    else Color(0xFFF1F8E9)
                )
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Text(
                text = (strings["parihaar"] ?: "Parihaar (Counter-Measure)").uppercase(),
                style = AppTextStyles.labelSmall.copy(
                    color = if (isDark) AppColors.TextSecondary else AppColors.TextSecondaryLight,
                    fontSize = 10.sp
                )
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.Top) {
                Text("✅", fontSize = 14.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    text = parihaarText,
                    style = AppTextStyles.bodyMedium.copy(
                        fontSize = 14.sp,
                        color = if (isDark) AppColors.TextPrimary else AppColors.TextPrimaryLight
                    )
                )
            }
        }
    }
}
