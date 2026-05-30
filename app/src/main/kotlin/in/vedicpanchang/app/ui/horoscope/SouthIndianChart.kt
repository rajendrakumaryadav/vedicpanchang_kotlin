package `in`.vedicpanchang.app.ui.horoscope

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.Canvas
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.HoroscopeModel
import `in`.vedicpanchang.app.l10n.HoroscopeLocalizer
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles

// South Indian chart: fixed 4×3 grid. Sign positions are fixed (Aries always top-left second cell).
// Column 0-3, Row 0-2. Empty corners (0,0) (0,3) (2,0) (2,3).
private val SIGN_GRID = arrayOf(
    intArrayOf(-1,  0,  1, -1),  // Row 0: empty, Aries(0), Taurus(1), empty
    intArrayOf(11, -2, -2,  2),  // Row 1: Pisces(11), inner, inner, Gemini(2)
    intArrayOf(10, -2, -2,  3),  // Row 2: Aquarius(10), inner, inner, Cancer(3)
    intArrayOf(-1,  8,  7, -1),  // Row 3: empty, Scorpio(8), Libra(7), empty (need 4 rows)
)
// Actually South Indian is 4×4 grid. Let's use correct layout:
// Signs go clockwise from Aries at top-left
private val SI_POSITIONS = listOf(
    // (row, col) for sign index 0..11
    0 to 0,  // Aries
    0 to 1,  // Taurus
    0 to 2,  // Gemini
    0 to 3,  // Cancer
    1 to 3,  // Leo
    2 to 3,  // Virgo
    3 to 3,  // Libra
    3 to 2,  // Scorpio
    3 to 1,  // Sagittarius
    3 to 0,  // Capricorn
    2 to 0,  // Aquarius
    1 to 0,  // Pisces
)
private val SIGN_NAMES_EN = listOf("Ar","Ta","Ge","Ca","Le","Vi","Li","Sc","Sg","Cp","Aq","Pi")
private val SIGN_NAMES_FULL = listOf("Aries","Taurus","Gemini","Cancer","Leo","Virgo","Libra","Scorpio","Sagittarius","Capricorn","Aquarius","Pisces")
private val SIGN_SYMBOLS_SI = listOf("♈","♉","♊","♋","♌","♍","♎","♏","♐","♑","♒","♓")

@Composable
fun SouthIndianChart(
    chart: HoroscopeModel,
    isNavamsha: Boolean = false,
    strings: Map<String, String>,
    localizer: HoroscopeLocalizer
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val planets = if (isNavamsha) chart.navamshaData else chart.planets
    if (isNavamsha) chart.navamshaHouseSigns else chart.houseSigns
    val lagnaSignIdx = if (isNavamsha) chart.lagnaNavamshaSignIndex else chart.lagnaSignIndex
    val borderColor = if (isDark) AppColors.CardBorder else AppColors.CardBorderLight
    val cardBorderColor = if (isDark) AppColors.Primary.copy(alpha = 0.45f) else Color(0xFFCBA35C).copy(alpha = 0.55f)
    val cardShape = RoundedCornerShape(24.dp)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(cardShape)
            .border(1.dp, cardBorderColor, cardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings[if (isNavamsha) "navamsha_chart" else "birth_chart"] ?: "Birth Chart",
                    style = AppTextStyles.saffronLabel
                )
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.Primary.copy(alpha = 0.12f))
                        .padding(horizontal = 8.dp, vertical = 3.dp)
                ) {
                    Text(
                        strings["chart_style_south"] ?: "South Indian",
                        style = AppTextStyles.labelSmall.copy(fontSize = 9.sp, color = AppColors.Primary)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))

            val density = LocalDensity.current
            var cellSizePx by remember { mutableIntStateOf(0) }

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .aspectRatio(1f)
                    .onSizeChanged { cellSizePx = it.width / 4 }
            ) {
                // Grid lines — drawn immediately via Canvas intrinsic size
                Canvas(modifier = Modifier.fillMaxSize()) {
                    for (r in 0..4) drawLine(borderColor, Offset(0f, size.height * r / 4), Offset(size.width, size.height * r / 4), 1.2.dp.toPx())
                    for (c in 0..4) drawLine(borderColor, Offset(size.width * c / 4, 0f), Offset(size.width * c / 4, size.height), 1.2.dp.toPx())
                }

                if (cellSizePx > 0) {
                    val cellSize = with(density) { cellSizePx.toDp() }

                    // Place each sign in its fixed grid position
                    SI_POSITIONS.forEachIndexed { signIdx, (row, col) ->
                        val planetsInSign = planets.filter { it.signIndex == signIdx }
                        val isLagna = signIdx == lagnaSignIdx
                        Box(
                            modifier = Modifier
                                .offset(x = cellSize * col, y = cellSize * row)
                                .size(cellSize)
                                .then(if (isLagna) Modifier.background(AppColors.Primary.copy(alpha = if (isDark) 0.18f else 0.10f)) else Modifier)
                                .padding(3.dp),
                            contentAlignment = Alignment.Center
                        ) {
                            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                                Row(verticalAlignment = Alignment.CenterVertically) {
                                    Text(SIGN_SYMBOLS_SI[signIdx], fontSize = 10.sp)
                                    Spacer(Modifier.width(1.dp))
                                    Text("${signIdx + 1}", style = AppTextStyles.labelSmall.copy(
                                        fontSize = 8.sp,
                                        color = if (isLagna) AppColors.Primary else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                                    ))
                                }
                                if (isLagna) Text(
                                    strings["lagna"] ?: "L",
                                    style = AppTextStyles.labelSmall.copy(fontSize = 7.sp, color = AppColors.Primary)
                                )
                                planetsInSign.forEach { p ->
                                    Text(
                                        (if (p.isRetrograde) "R:" else "") + p.name.take(2).uppercase(),
                                        style = AppTextStyles.labelSmall.copy(
                                            fontSize = 7.sp,
                                            color = planetColor(p.name, isDark)
                                        )
                                    )
                                }
                            }
                        }
                    }

                    // Center 2×2 panel — Lagna summary
                    val centerBg = if (isDark) Color(0xFF2D1B69).copy(alpha = 0.65f)
                                   else AppColors.Primary.copy(alpha = 0.08f)
                    Box(
                        modifier = Modifier
                            .offset(x = cellSize, y = cellSize)
                            .size(cellSize * 2)
                            .background(centerBg)
                            .padding(6.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Column(horizontalAlignment = Alignment.CenterHorizontally) {
                            Text(
                                strings["lagna"] ?: "Lagna",
                                style = AppTextStyles.labelSmall.copy(
                                    fontSize = 10.sp,
                                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.65f)
                                ),
                                textAlign = TextAlign.Center
                            )
                            Spacer(Modifier.height(3.dp))
                            Text(
                                localizer.signNameFromEnglish(SIGN_NAMES_FULL[lagnaSignIdx]),
                                style = AppTextStyles.bodyMedium.copy(
                                    fontWeight = FontWeight.Bold,
                                    color = MaterialTheme.colorScheme.onSurface,
                                    fontSize = 15.sp
                                ),
                                textAlign = TextAlign.Center
                            )
                            if (!isNavamsha) {
                                Spacer(Modifier.height(3.dp))
                                Text(
                                    "%.1f°".format(chart.lagnaDegreeInSign),
                                    style = AppTextStyles.labelSmall.copy(
                                        fontSize = 11.sp,
                                        color = AppColors.Primary,
                                        fontWeight = FontWeight.SemiBold
                                    ),
                                    textAlign = TextAlign.Center
                                )
                            }
                        }
                    }
                }
            }
        }
    }
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
