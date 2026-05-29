package `in`.vedicpanchang.app.ui.horoscope

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ExpandLess
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.DashaPeriod
import `in`.vedicpanchang.app.data.model.HoroscopeModel
import `in`.vedicpanchang.app.l10n.HoroscopeLocalizer
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import kotlin.time.Clock
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

private val DASHA_SYMBOLS = mapOf(
    "Sun" to "☀️", "Moon" to "🌙", "Mars" to "♂️", "Mercury" to "☿",
    "Jupiter" to "♃", "Venus" to "♀️", "Saturn" to "♄", "Rahu" to "☊", "Ketu" to "☋"
)

private fun planetSymbol(name: String) = DASHA_SYMBOLS[name] ?: "★"

@Composable
fun DashaSection(
    chart: HoroscopeModel,
    strings: Map<String, String>,
    localizer: HoroscopeLocalizer
) {
    val isDark = isSystemInDarkTheme()
    val now = Clock.System.now()
    val currentMaha = chart.dashas.firstOrNull { now >= it.start && now < it.end }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Text(strings["vimshottari_dasha"] ?: "Vimshottari Dasha", style = AppTextStyles.saffronLabel)
            Spacer(Modifier.height(12.dp))

            // Current period highlight card
            currentMaha?.let { maha ->
                val currentAntar = maha.antardashas.firstOrNull { now >= it.start && now < it.end }
                val progress = dashaProgress(now, maha.start, maha.end)

                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(12.dp))
                        .background(AppColors.Primary.copy(alpha = if (isDark) 0.18f else 0.10f))
                        .padding(12.dp)
                ) {
                    Column {
                        Text(
                            strings["current_period"] ?: "Current Period",
                            style = AppTextStyles.labelSmall.copy(color = AppColors.Primary)
                        )
                        Spacer(Modifier.height(6.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text(planetSymbol(maha.planet), style = AppTextStyles.bodyLarge)
                            Spacer(Modifier.width(8.dp))
                            Column(Modifier.weight(1f)) {
                                Text(
                                    "${localizer.planetName(maha.planet)} ${strings["mahadasha"] ?: "Mahadasha"}",
                                    style = AppTextStyles.bodyMedium.copy(fontWeight = FontWeight.Bold)
                                )
                                currentAntar?.let { antar ->
                                    Row(verticalAlignment = Alignment.CenterVertically) {
                                        Text(
                                            planetSymbol(antar.planet),
                                            style = AppTextStyles.bodySmall
                                        )
                                        Spacer(Modifier.width(4.dp))
                                        Text(
                                            "${localizer.planetName(antar.planet)} ${strings["antardasha"] ?: "Antardasha"}",
                                            style = AppTextStyles.bodySmall.copy(
                                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.7f)
                                            )
                                        )
                                    }
                                }
                            }
                        }
                        Spacer(Modifier.height(10.dp))
                        // Progress bar
                        LinearProgressIndicator(
                            progress = { progress },
                            modifier = Modifier.fillMaxWidth().height(4.dp).clip(RoundedCornerShape(2.dp)),
                            color = AppColors.Primary,
                            trackColor = AppColors.Primary.copy(alpha = 0.2f)
                        )
                        Spacer(Modifier.height(4.dp))
                        Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.SpaceBetween) {
                            Text(
                                formatDate(maha.start),
                                style = AppTextStyles.timeSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            )
                            Text(
                                "${(progress * 100).toInt()}% ${strings["elapsed"] ?: "elapsed"}",
                                style = AppTextStyles.timeSmall.copy(fontSize = 10.sp, color = AppColors.Primary.copy(alpha = 0.8f))
                            )
                            Text(
                                formatDate(maha.end),
                                style = AppTextStyles.timeSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
                            )
                        }
                    }
                }
                Spacer(Modifier.height(16.dp))
            }

            // All dasha rows
            Text(
                strings["all_dashas"] ?: "All Periods",
                style = AppTextStyles.labelSmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f))
            )
            Spacer(Modifier.height(8.dp))

            chart.dashas.forEachIndexed { index, dasha ->
                if (index > 0) {
                    HorizontalDivider(
                        color = MaterialTheme.colorScheme.outline.copy(alpha = 0.12f),
                        modifier = Modifier.padding(vertical = 2.dp)
                    )
                }
                DashaRow(dasha = dasha, now = now, strings = strings, localizer = localizer, isDark = isDark)
            }
        }
    }
}

@Composable
private fun DashaRow(
    dasha: DashaPeriod,
    now: Instant,
    strings: Map<String, String>,
    localizer: HoroscopeLocalizer,
    isDark: Boolean
) {
    var expanded by remember { mutableStateOf(false) }
    val isCurrent = now >= dasha.start && now < dasha.end

    Column(modifier = Modifier.padding(vertical = 1.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(8.dp))
                .background(
                    if (isCurrent) AppColors.Primary.copy(alpha = if (isDark) 0.12f else 0.07f)
                    else Color.Transparent
                )
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp, vertical = 8.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Symbol
            Text(
                planetSymbol(dasha.planet),
                style = AppTextStyles.bodyMedium,
                modifier = Modifier.width(28.dp)
            )
            // Planet name + dates
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        localizer.planetName(dasha.planet),
                        style = AppTextStyles.bodyMedium.copy(
                            fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal,
                            color = if (isCurrent) AppColors.Primary else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        "  ·  ${dasha.totalYears} ${strings["years_short"] ?: "yrs"}",
                        style = AppTextStyles.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                        )
                    )
                }
                Spacer(Modifier.height(2.dp))
                Text(
                    "${formatDate(dasha.start)} – ${formatDate(dasha.end)}",
                    style = AppTextStyles.timeSmall.copy(
                        fontSize = 11.sp,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                    )
                )
            }
            if (isCurrent) {
                Box(
                    Modifier
                        .clip(RoundedCornerShape(4.dp))
                        .background(AppColors.Primary)
                        .padding(horizontal = 6.dp, vertical = 2.dp)
                ) {
                    Text("NOW", style = AppTextStyles.labelSmall.copy(color = Color.White, fontSize = 8.sp))
                }
                Spacer(Modifier.width(4.dp))
            }
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.4f),
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(
                modifier = Modifier
                    .padding(start = 36.dp, end = 8.dp, top = 2.dp, bottom = 8.dp)
                    .clip(RoundedCornerShape(8.dp))
                    .background(MaterialTheme.colorScheme.surfaceVariant.copy(alpha = 0.4f))
                    .padding(8.dp)
            ) {
                dasha.antardashas.forEach { antar ->
                    val isCurAntar = now >= antar.start && now < antar.end
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clip(RoundedCornerShape(6.dp))
                            .background(if (isCurAntar) AppColors.Primary.copy(alpha = 0.08f) else Color.Transparent)
                            .padding(horizontal = 8.dp, vertical = 5.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            planetSymbol(antar.planet),
                            style = AppTextStyles.bodySmall,
                            modifier = Modifier.width(22.dp)
                        )
                        Text(
                            localizer.planetName(antar.planet),
                            style = AppTextStyles.bodySmall.copy(
                                fontWeight = if (isCurAntar) FontWeight.SemiBold else FontWeight.Normal,
                                color = if (isCurAntar) AppColors.Primary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.width(80.dp)
                        )
                        Text(
                            "${formatDate(antar.start)} – ${formatDate(antar.end)}",
                            style = AppTextStyles.timeSmall.copy(
                                fontSize = 11.sp,
                                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)
                            )
                        )
                    }
                }
            }
        }
    }
}

private fun dashaProgress(now: Instant, start: Instant, end: Instant): Float {
    val total = end.toEpochMilliseconds() - start.toEpochMilliseconds()
    val elapsed = now.toEpochMilliseconds() - start.toEpochMilliseconds()
    return if (total <= 0L) 0f else (elapsed.toFloat() / total.toFloat()).coerceIn(0f, 1f)
}

private fun formatDate(instant: Instant): String {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "%04d-%02d-%02d".format(local.year, local.monthNumber, local.dayOfMonth)
}
