package `in`.vedicpanchang.app.ui.horoscope

import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.HoroscopeModel
import `in`.vedicpanchang.app.l10n.HoroscopeLocalizer
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles

@Composable
fun NavamshaSection(
    chart: HoroscopeModel,
    isSouth: Boolean,
    strings: Map<String, String>,
    localizer: HoroscopeLocalizer
) {
    Column {
        if (isSouth) {
            SouthIndianChart(chart = chart, isNavamsha = true, strings = strings, localizer = localizer)
        } else {
            NorthIndianChart(chart = chart, isNavamsha = true, strings = strings, localizer = localizer)
        }

        NavamshaComparisonTable(chart = chart, strings = strings, localizer = localizer)
    }
}

@Composable
private fun NavamshaComparisonTable(
    chart: HoroscopeModel,
    strings: Map<String, String>,
    localizer: HoroscopeLocalizer
) {
    val isDark = isSystemInDarkTheme()
    val navamshaLagna = localizer.signName(chart.lagnaNavamshaSignIndex)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 4.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text("🔯", style = AppTextStyles.bodyLarge)
                Spacer(Modifier.width(8.dp))
                Column(Modifier.weight(1f)) {
                    Text(strings["navamsha_subtitle"] ?: "Navamsha", style = AppTextStyles.labelSmall)
                    Text(
                        "${strings["navamsha_lagna"] ?: "Navamsha Lagna"}: $navamshaLagna",
                        style = AppTextStyles.bodySmall.copy(
                            color = if (isDark) AppColors.Secondary else AppColors.SecondaryOnLight
                        )
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            Row {
                Text(
                    strings["planet_col"] ?: "Planet",
                    style = AppTextStyles.labelSmall.copy(fontSize = 9.sp),
                    modifier = Modifier.width(90.dp)
                )
                Text(strings["d1_sign"] ?: "D1", style = AppTextStyles.labelSmall.copy(fontSize = 9.sp), modifier = Modifier.weight(1f))
                Text(
                    strings["d9_sign"] ?: "D9",
                    style = AppTextStyles.labelSmall.copy(
                        fontSize = 9.sp,
                        color = if (isDark) AppColors.Secondary else AppColors.SecondaryOnLight
                    ),
                    modifier = Modifier.weight(1f)
                )
            }
            Divider(color = AppColors.CardBorder.copy(alpha = 0.4f), modifier = Modifier.padding(vertical = 6.dp))

            chart.planets.forEachIndexed { index, planet ->
                val navamsha = chart.navamshaData.getOrNull(index) ?: return@forEachIndexed
                Row(Modifier.fillMaxWidth().padding(vertical = 3.dp), verticalAlignment = Alignment.CenterVertically) {
                    Row(Modifier.width(90.dp), verticalAlignment = Alignment.CenterVertically) {
                        Text(planet.symbol, style = AppTextStyles.bodyMedium)
                        Spacer(Modifier.width(4.dp))
                        Text(
                            localizer.planetName(planet.name),
                            style = AppTextStyles.bodySmall,
                            maxLines = 1
                        )
                    }
                    Text(
                        localizer.signNameFromEnglish(planet.signName),
                        style = AppTextStyles.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    Text(
                        localizer.signNameFromEnglish(navamsha.signName),
                        style = AppTextStyles.bodySmall.copy(
                            color = if (isDark) AppColors.Secondary else AppColors.SecondaryOnLight
                        ),
                        modifier = Modifier.weight(1f)
                    )
                }
            }
        }
    }
}
