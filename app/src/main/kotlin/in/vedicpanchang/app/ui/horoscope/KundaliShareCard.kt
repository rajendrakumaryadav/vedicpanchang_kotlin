package `in`.vedicpanchang.app.ui.horoscope

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.HoroscopeModel
import `in`.vedicpanchang.app.l10n.HoroscopeLocalizer
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import kotlin.time.Clock

@Composable
fun KundaliShareCard(
    chart: HoroscopeModel,
    localizer: HoroscopeLocalizer,
    strings: Map<String, String>,
    locale: String,
    modifier: Modifier = Modifier
) {
    val now = Clock.System.now()
    val javaLocale = if (locale == "hi" || locale == "sa") Locale("hi", "IN") else Locale.ENGLISH
    val birthDate = SimpleDateFormat("d MMM yyyy, HH:mm", javaLocale)
        .format(Date(chart.birthDetails.birthInstant.toEpochMilliseconds()))

    val sun = chart.planets.firstOrNull { it.name == "Sun" } ?: chart.planets.first()
    val moon = chart.planets.firstOrNull { it.name == "Moon" } ?: chart.planets.first()
    val currentMaha = chart.dashas.firstOrNull { now >= it.start && now < it.end }
    val currentAntar = currentMaha?.antardashas?.firstOrNull { now >= it.start && now < it.end }

    Column(
        modifier = modifier
            .width(360.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(
                Brush.verticalGradient(
                    colors = listOf(Color(0xFF1A0A2E), Color(0xFF2D1054))
                )
            )
            .padding(20.dp)
    ) {
        // Header
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🕉", fontSize = 22.sp)
            Spacer(Modifier.width(8.dp))
            Column {
                Text(
                    "Vedic Kundali",
                    style = AppTextStyles.saffronLabel.copy(fontSize = 15.sp, color = AppColors.Primary)
                )
                Text(
                    strings["nav_horoscope"] ?: "Janma Kundali",
                    style = AppTextStyles.bodySmall.copy(color = Color.White.copy(alpha = 0.5f), fontSize = 10.sp)
                )
            }
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = AppColors.Primary.copy(alpha = 0.3f))
        Spacer(Modifier.height(14.dp))

        // Birth details
        Text(
            birthDate,
            style = AppTextStyles.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.SemiBold)
        )
        Text(
            "📍 ${chart.birthDetails.locationName}",
            style = AppTextStyles.bodySmall.copy(color = Color.White.copy(alpha = 0.6f))
        )

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(Modifier.height(14.dp))

        // Lagna / Sun / Moon row
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            ShareSignBox(
                symbol = "⬆",
                label = strings["lagna_rising"] ?: "Lagna",
                sign = localizer.signNameFromEnglish(chart.lagnaSignName),
                sub = localizer.nakshatraName(chart.lagnaNakshatraName),
                color = AppColors.Primary,
                modifier = Modifier.weight(1f)
            )
            ShareSignBox(
                symbol = "☀️",
                label = strings["sun_sign_rashi"] ?: "Sun",
                sign = localizer.signNameFromEnglish(sun.signName),
                sub = localizer.nakshatraName(sun.nakshatraName),
                color = AppColors.SunColor,
                modifier = Modifier.weight(1f)
            )
            ShareSignBox(
                symbol = "🌙",
                label = strings["moon_sign_chandra"] ?: "Moon",
                sign = localizer.signNameFromEnglish(moon.signName),
                sub = localizer.nakshatraName(moon.nakshatraName),
                color = AppColors.MoonColor,
                modifier = Modifier.weight(1f)
            )
        }

        // Current Dasha
        if (currentMaha != null) {
            Spacer(Modifier.height(14.dp))
            HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
            Spacer(Modifier.height(12.dp))
            Text(
                strings["current_period"] ?: "Current Dasha Period",
                style = AppTextStyles.labelSmall.copy(color = AppColors.Primary)
            )
            Spacer(Modifier.height(6.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(DASHA_SYMBOLS[currentMaha.planet] ?: "★", fontSize = 18.sp)
                Spacer(Modifier.width(8.dp))
                Column {
                    Text(
                        "${localizer.planetName(currentMaha.planet)} ${strings["mahadasha"] ?: "Mahadasha"}",
                        style = AppTextStyles.bodyMedium.copy(color = Color.White, fontWeight = FontWeight.Bold)
                    )
                    if (currentAntar != null) {
                        Row {
                            Text(DASHA_SYMBOLS[currentAntar.planet] ?: "★", fontSize = 12.sp)
                            Spacer(Modifier.width(4.dp))
                            Text(
                                "${localizer.planetName(currentAntar.planet)} ${strings["antardasha"] ?: "Antardasha"}",
                                style = AppTextStyles.bodySmall.copy(color = Color.White.copy(alpha = 0.65f))
                            )
                        }
                    }
                }
            }
        }

        Spacer(Modifier.height(14.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(Modifier.height(12.dp))

        // Compact planet grid (3 columns of 3)
        Text(
            strings["planetary_positions"] ?: "Navagraha",
            style = AppTextStyles.labelSmall.copy(color = AppColors.Primary)
        )
        Spacer(Modifier.height(8.dp))

        chart.planets.chunked(3).forEach { row ->
            Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(4.dp)) {
                row.forEach { planet ->
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .clip(RoundedCornerShape(8.dp))
                            .background(Color.White.copy(alpha = 0.06f))
                            .padding(6.dp)
                    ) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(planet.symbol, fontSize = 10.sp)
                            Spacer(Modifier.width(3.dp))
                            Text(
                                localizer.planetName(planet.name),
                                style = AppTextStyles.labelSmall.copy(fontSize = 8.sp, color = Color.White.copy(alpha = 0.7f)),
                                maxLines = 1
                            )
                        }
                        Text(
                            localizer.signNameFromEnglish(planet.signName),
                            style = AppTextStyles.bodySmall.copy(color = Color.White, fontSize = 9.sp, fontWeight = FontWeight.Medium),
                            maxLines = 1
                        )
                        Text(
                            "H${planet.houseNumber}  ${planet.degreeStr}",
                            style = AppTextStyles.timeSmall.copy(fontSize = 8.sp, color = Color.White.copy(alpha = 0.5f)),
                            maxLines = 1
                        )
                    }
                }
                repeat(3 - row.size) { Spacer(Modifier.weight(1f)) }
            }
            Spacer(Modifier.height(4.dp))
        }

        Spacer(Modifier.height(12.dp))
        HorizontalDivider(color = Color.White.copy(alpha = 0.1f))
        Spacer(Modifier.height(10.dp))

        // Footer
        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.Center) {
            Text(
                "Generated by Vedic Panchang",
                style = AppTextStyles.bodySmall.copy(color = Color.White.copy(alpha = 0.35f), fontSize = 9.sp)
            )
        }
    }
}

@Composable
private fun ShareSignBox(
    symbol: String,
    label: String,
    sign: String,
    sub: String,
    color: Color,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(10.dp))
            .background(color.copy(alpha = 0.12f))
            .padding(8.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(symbol, fontSize = 16.sp)
        Spacer(Modifier.height(3.dp))
        Text(
            label,
            style = AppTextStyles.labelSmall.copy(fontSize = 8.sp, color = color.copy(alpha = 0.85f))
        )
        Text(
            sign,
            style = AppTextStyles.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold, fontSize = 11.sp),
            maxLines = 1
        )
        Text(
            sub,
            style = AppTextStyles.bodySmall.copy(color = Color.White.copy(alpha = 0.5f), fontSize = 8.sp),
            maxLines = 1
        )
    }
}

private val DASHA_SYMBOLS = mapOf(
    "Sun" to "☀️", "Moon" to "🌙", "Mars" to "♂️", "Mercury" to "☿",
    "Jupiter" to "♃", "Venus" to "♀️", "Saturn" to "♄", "Rahu" to "☊", "Ketu" to "☋"
)
