package `in`.vedicpanchang.app.ui.horoscope

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.nativeCanvas
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.HoroscopeModel
import `in`.vedicpanchang.app.data.model.PlanetData
import `in`.vedicpanchang.app.l10n.HoroscopeLocalizer
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles

// House centroid positions as fraction of canvas width/height
private val HOUSE_CENTROIDS = listOf(
    0.500f to 0.220f,   //  1
    0.775f to 0.078f,   //  2
    0.922f to 0.275f,   //  3
    0.775f to 0.500f,   //  4
    0.922f to 0.725f,   //  5
    0.775f to 0.922f,   //  6
    0.500f to 0.780f,   //  7
    0.225f to 0.922f,   //  8
    0.078f to 0.725f,   //  9
    0.225f to 0.500f,   // 10
    0.078f to 0.275f,   // 11
    0.225f to 0.078f,   // 12
)

private val SIGN_SYMBOLS = listOf("♈","♉","♊","♋","♌","♍","♎","♏","♐","♑","♒","♓")

private fun isInnerHouse(house: Int) = house == 1 || house == 4 || house == 7 || house == 10

@Composable
fun NorthIndianChart(
    chart: HoroscopeModel,
    isNavamsha: Boolean = false,
    strings: Map<String, String>,
    localizer: HoroscopeLocalizer
) {
    val isDark = isSystemInDarkTheme()
    val borderColor = if (isDark) AppColors.CardBorder else AppColors.CardBorderLight
    val planets = if (isNavamsha) chart.navamshaData else chart.planets
    val houseSigns = if (isNavamsha) chart.navamshaHouseSigns else chart.houseSigns
    val lagnaSignIdx = if (isNavamsha) chart.lagnaNavamshaSignIndex else chart.lagnaSignIndex
    val lagnaDeg = chart.lagnaDegreeInSign
    val lagnaSignName = localizer.signName(lagnaSignIdx)
    val titleKey = if (isNavamsha) "navamsha_chart" else "birth_chart"

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(strings[titleKey] ?: if (isNavamsha) "Navamsha (D-9)" else "Birth Chart", style = AppTextStyles.saffronLabel)
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.Primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(strings["chart_style_north"] ?: "North Indian",
                        style = AppTextStyles.labelSmall.copy(fontSize = 9.sp, color = AppColors.Primary))
                }
            }

            Spacer(Modifier.height(8.dp))
            Row {
                LegendDot(color = AppColors.Primary, label = strings["lagna"] ?: "Lagna")
                Spacer(Modifier.width(12.dp))
                LegendDot(color = AppColors.MoonColor, label = strings["planet_col"] ?: "Planet")
            }
            Spacer(Modifier.height(12.dp))

            val density = LocalDensity.current
            BoxWithConstraints(modifier = Modifier.fillMaxWidth().aspectRatio(1f)) {
                val sizePx = with(density) { maxWidth.toPx() }
                Canvas(modifier = Modifier.fillMaxSize()) {
                    drawNiGrid(borderColor)
                }
                // House cells as Compose text overlaid on canvas
                for (house in 1..12) {
                    val (cx, cy) = HOUSE_CENTROIDS[house - 1]
                    val inner = isInnerHouse(house)
                    val cellW = if (inner) maxWidth * 0.38f else maxWidth * 0.24f
                    val cellH = if (inner) maxWidth * 0.30f else maxWidth * 0.20f
                    val signIdx = houseSigns[house - 1]
                    val isLagna = house == 1
                    val planetsHere = planets.filter { it.houseNumber == house }

                    Box(
                        modifier = Modifier
                            .offset(
                                x = maxWidth * cx - cellW / 2,
                                y = maxWidth * cy - cellH / 2
                            )
                            .size(cellW, cellH)
                            .then(
                                if (isLagna) Modifier.clip(RoundedCornerShape(4.dp))
                                    .background(AppColors.Primary.copy(alpha = if (isDark) 0.18f else 0.10f))
                                else Modifier
                            ),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    "${signIdx + 1}",
                                    style = AppTextStyles.labelSmall.copy(
                                        fontSize = if (inner) 9.sp else 7.5.sp,
                                        color = if (isLagna) AppColors.Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    )
                                )
                                Spacer(Modifier.width(1.dp))
                                Text(SIGN_SYMBOLS[signIdx.coerceIn(0, 11)], fontSize = if (inner) 10.sp else 8.5.sp)
                            }
                            if (isLagna) {
                                Text(lagnaSignName, style = AppTextStyles.labelSmall.copy(fontSize = 7.5.sp, color = AppColors.Primary), maxLines = 1)
                                Text("%.1f°".format(lagnaDeg), style = AppTextStyles.labelSmall.copy(fontSize = 6.5.sp, color = AppColors.Primary.copy(alpha = 0.8f)))
                            }
                            planetsHere.forEach { p ->
                                Text(
                                    (if (p.isRetrograde) "(R)" else "") + p.name.take(2).uppercase(),
                                    style = AppTextStyles.labelSmall.copy(fontSize = 7.5.sp, color = planetColor(p.name, isDark)),
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

private fun DrawScope.drawNiGrid(borderColor: Color) {
    val w = size.width
    val h = size.height
    val paint = Stroke(width = 1.2f.dp.toPx())
    val tl = Offset(0f, 0f); val tr = Offset(w, 0f)
    val br = Offset(w, h);   val bl = Offset(0f, h)
    val midT = Offset(w/2, 0f); val midR = Offset(w, h/2)
    val midB = Offset(w/2, h);  val midL = Offset(0f, h/2)

    drawRect(color = borderColor, style = paint)
    drawLine(borderColor, midT, midR, strokeWidth = 1.2.dp.toPx())
    drawLine(borderColor, midR, midB, strokeWidth = 1.2.dp.toPx())
    drawLine(borderColor, midB, midL, strokeWidth = 1.2.dp.toPx())
    drawLine(borderColor, midL, midT, strokeWidth = 1.2.dp.toPx())
    drawLine(borderColor, tl, br, strokeWidth = 1.2.dp.toPx())
    drawLine(borderColor, tr, bl, strokeWidth = 1.2.dp.toPx())
}

private fun planetColor(name: String, isDark: Boolean) = when (name) {
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

@Composable
private fun LegendDot(color: Color, label: String) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Box(Modifier.size(8.dp).clip(androidx.compose.foundation.shape.CircleShape).background(color))
        Spacer(Modifier.width(4.dp))
        Text(label, style = AppTextStyles.bodySmall)
    }
}
