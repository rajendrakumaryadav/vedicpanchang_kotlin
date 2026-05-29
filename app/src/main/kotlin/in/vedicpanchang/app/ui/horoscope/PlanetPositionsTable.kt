package `in`.vedicpanchang.app.ui.horoscope

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
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
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.HoroscopeModel
import `in`.vedicpanchang.app.data.model.PlanetData
import `in`.vedicpanchang.app.l10n.HoroscopeLocalizer
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles

@Composable
fun PlanetPositionsTable(
    chart: HoroscopeModel,
    strings: Map<String, String>,
    localizer: HoroscopeLocalizer
) {
    val isDark = isSystemInDarkTheme()

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Text(strings["planetary_positions"] ?: "Navagraha Positions", style = AppTextStyles.saffronLabel)
            Spacer(Modifier.height(14.dp))

            chart.planets.chunked(2).forEach { pair ->
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    pair.forEach { planet ->
                        PlanetCard(
                            planet = planet,
                            localizer = localizer,
                            isDark = isDark,
                            strings = strings,
                            modifier = Modifier.weight(1f)
                        )
                    }
                    if (pair.size == 1) Spacer(Modifier.weight(1f))
                }
                Spacer(Modifier.height(8.dp))
            }

            // Lagna as a standalone full-width card
            HorizontalDivider(
                color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f),
                modifier = Modifier.padding(vertical = 4.dp)
            )
            LagnaCard(chart = chart, localizer = localizer, strings = strings)
        }
    }
}

@Composable
private fun PlanetCard(
    planet: PlanetData,
    localizer: HoroscopeLocalizer,
    strings: Map<String, String>,
    isDark: Boolean,
    modifier: Modifier
) {
    val color = getPlanetColor(planet.name, isDark)
    val retroLabel = strings["retrograde"] ?: "(R)"

    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.07f))
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        // Symbol + name header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(planet.symbol, style = AppTextStyles.bodyLarge)
            Spacer(Modifier.width(6.dp))
            Column {
                Text(
                    localizer.planetName(planet.name),
                    style = AppTextStyles.labelSmall.copy(
                        color = color,
                        fontWeight = FontWeight.Bold
                    )
                )
                if (planet.isRetrograde) {
                    Text(
                        retroLabel,
                        style = AppTextStyles.bodySmall.copy(
                            fontSize = 9.sp,
                            color = AppColors.Inauspicious
                        )
                    )
                }
            }
        }

        Spacer(Modifier.height(6.dp))
        HorizontalDivider(color = color.copy(alpha = 0.25f), thickness = 0.5.dp)
        Spacer(Modifier.height(6.dp))

        // Sign + House
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                localizer.signNameFromEnglish(planet.signName),
                style = AppTextStyles.bodySmall.copy(fontWeight = FontWeight.Medium),
                maxLines = 1
            )
            Text(
                "  H${planet.houseNumber}",
                style = AppTextStyles.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                )
            )
        }

        // Degree
        Text(
            planet.degreeStr,
            style = AppTextStyles.timeSmall.copy(fontSize = 11.sp)
        )

        // Nakshatra
        Text(
            localizer.nakshatraName(planet.nakshatraName),
            style = AppTextStyles.bodySmall.copy(
                fontSize = 10.sp,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
            ),
            maxLines = 1
        )
    }
}

@Composable
private fun LagnaCard(
    chart: HoroscopeModel,
    localizer: HoroscopeLocalizer,
    strings: Map<String, String>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Primary.copy(alpha = 0.08f))
            .padding(horizontal = 12.dp, vertical = 10.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text("⬆", style = AppTextStyles.bodyLarge.copy(color = AppColors.Primary))
        Spacer(Modifier.width(10.dp))
        Column(Modifier.weight(1f)) {
            Text(
                strings["lagna"] ?: "Lagna (Ascendant)",
                style = AppTextStyles.labelSmall.copy(color = AppColors.Primary, fontWeight = FontWeight.Bold)
            )
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    localizer.signNameFromEnglish(chart.lagnaSignName),
                    style = AppTextStyles.bodySmall.copy(fontWeight = FontWeight.Medium)
                )
                Text(
                    "  H1",
                    style = AppTextStyles.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                )
            }
        }
        Column(horizontalAlignment = Alignment.End) {
            Text(
                "%.1f°".format(chart.lagnaDegreeInSign),
                style = AppTextStyles.timeSmall.copy(fontSize = 12.sp)
            )
            Text(
                localizer.nakshatraName(chart.lagnaNakshatraName),
                style = AppTextStyles.bodySmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f))
            )
        }
    }
}

internal fun getPlanetColor(name: String, isDark: Boolean): Color = when (name) {
    "Sun"     -> AppColors.SunColor
    "Moon"    -> AppColors.MoonColor
    "Mars"    -> AppColors.Inauspicious
    "Mercury" -> Color(0xFF4CAF50)
    "Jupiter" -> if (isDark) AppColors.Secondary else AppColors.SecondaryOnLight
    "Venus"   -> Color(0xFFE91E63)
    "Saturn"  -> AppColors.TextMuted
    "Rahu"    -> AppColors.Festival
    "Ketu"    -> Color(0xFF795548)
    else      -> AppColors.TextSecondary
}
