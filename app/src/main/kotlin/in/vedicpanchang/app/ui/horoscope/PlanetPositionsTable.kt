package `in`.vedicpanchang.app.ui.horoscope

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Divider
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
            Text(strings["planetary_positions"] ?: "Planetary Positions (Navagraha)", style = AppTextStyles.saffronLabel)
            Spacer(Modifier.height(12.dp))

            // Header
            PlanetRow(
                planet = strings["planet_col"] ?: "Planet",
                sign = strings["sign_col"] ?: "Sign",
                deg = strings["deg_col"] ?: "Deg",
                house = strings["house_col"] ?: "H",
                nakshatra = strings["nakshatra_col"] ?: "Nakshatra",
                isHeader = true,
                isDark = isDark
            )
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.3f), modifier = Modifier.padding(vertical = 4.dp))

            chart.planets.forEach { p ->
                PlanetRow(
                    planet = localizer.planetName(p.name) + (if (p.isRetrograde) " ${strings["retrograde"] ?: "(R)"}" else ""),
                    sign = localizer.signNameFromEnglish(p.signName),
                    deg = "%.1f°".format(p.degreeInSign),
                    house = "${p.houseNumber}",
                    nakshatra = localizer.nakshatraName(p.nakshatraName),
                    planetData = p,
                    isDark = isDark
                )
            }

            // Lagna row
            Divider(color = MaterialTheme.colorScheme.outline.copy(alpha = 0.2f), modifier = Modifier.padding(vertical = 2.dp))
            PlanetRow(
                planet = strings["lagna"] ?: "Lagna",
                sign = localizer.signNameFromEnglish(chart.lagnaSignName),
                deg = "%.1f°".format(chart.lagnaDegreeInSign),
                house = "1",
                nakshatra = localizer.nakshatraName(chart.lagnaNakshatraName),
                isLagna = true,
                isDark = isDark
            )
        }
    }
}

@Composable
private fun PlanetRow(
    planet: String,
    sign: String,
    deg: String,
    house: String,
    nakshatra: String,
    isHeader: Boolean = false,
    isLagna: Boolean = false,
    planetData: PlanetData? = null,
    isDark: Boolean
) {
    val textStyle = if (isHeader) AppTextStyles.labelSmall else AppTextStyles.bodySmall
    val planetColor = when {
        isHeader -> MaterialTheme.colorScheme.onSurface
        isLagna  -> AppColors.Primary
        planetData != null -> getPlanetColor(planetData.name, isDark)
        else -> MaterialTheme.colorScheme.onSurface
    }
    Row(
        modifier = Modifier.fillMaxWidth().padding(vertical = 3.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(planet, style = textStyle.copy(color = planetColor, fontWeight = if (isHeader || isLagna) FontWeight.Bold else FontWeight.Normal), modifier = Modifier.width(72.dp))
        Text(sign, style = textStyle, modifier = Modifier.width(64.dp))
        Text(deg, style = textStyle.copy(fontFamily = AppTextStyles.timeSmall.fontFamily, fontSize = 10.sp), modifier = Modifier.width(40.dp))
        Text(house, style = textStyle, modifier = Modifier.width(20.dp))
        Text(nakshatra, style = textStyle, modifier = Modifier.weight(1f))
    }
}

private fun getPlanetColor(name: String, isDark: Boolean): Color = when (name) {
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
