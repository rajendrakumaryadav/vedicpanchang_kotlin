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
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
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

            currentMaha?.let { maha ->
                val currentAntar = maha.antardashas.firstOrNull { now >= it.start && now < it.end }
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .clip(RoundedCornerShape(8.dp))
                        .background(AppColors.Primary.copy(alpha = 0.10f))
                        .padding(10.dp)
                ) {
                    Column {
                        Text(strings["current_period"] ?: "Current Period", style = AppTextStyles.labelSmall.copy(color = AppColors.Primary))
                        Text(
                            "${localizer.planetName(maha.planet)} ${strings["mahadasha"] ?: "Mahadasha"}" +
                                    (currentAntar?.let { " · ${localizer.planetName(it.planet)} ${strings["antardasha"] ?: "Antardasha"}" } ?: ""),
                            style = AppTextStyles.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                        )
                        Text(
                            "${strings["dasha_ends"] ?: "Ends"}: ${formatDate(maha.end)}",
                            style = AppTextStyles.bodySmall
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            chart.dashas.forEach { dasha ->
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

    Column(modifier = Modifier.padding(vertical = 2.dp)) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clip(RoundedCornerShape(6.dp))
                .background(if (isCurrent) AppColors.Primary.copy(alpha = if (isDark) 0.15f else 0.08f) else Color.Transparent)
                .clickable { expanded = !expanded }
                .padding(horizontal = 8.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(Modifier.weight(1f)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Text(
                        localizer.planetName(dasha.planet),
                        style = AppTextStyles.bodyMedium.copy(
                            fontWeight = if (isCurrent) androidx.compose.ui.text.font.FontWeight.Bold else androidx.compose.ui.text.font.FontWeight.Normal,
                            color = if (isCurrent) AppColors.Primary else MaterialTheme.colorScheme.onSurface
                        )
                    )
                    Text(
                        " · ${dasha.totalYears} ${strings["years_short"] ?: "yrs"}",
                        style = AppTextStyles.bodySmall
                    )
                }
                Text(
                    "${formatDate(dasha.start)} – ${formatDate(dasha.end)}",
                    style = AppTextStyles.timeSmall.copy(fontSize = 10.sp)
                )
            }
            if (isCurrent) {
                Box(Modifier.clip(RoundedCornerShape(4.dp)).background(AppColors.Primary).padding(horizontal = 6.dp, vertical = 2.dp)) {
                    Text("▶", style = AppTextStyles.labelSmall.copy(color = Color.White, fontSize = 8.sp))
                }
                Spacer(Modifier.width(4.dp))
            }
            Icon(
                if (expanded) Icons.Filled.ExpandLess else Icons.Filled.ExpandMore,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f),
                modifier = Modifier.size(18.dp)
            )
        }

        AnimatedVisibility(visible = expanded) {
            Column(modifier = Modifier.padding(start = 16.dp, top = 4.dp, bottom = 4.dp)) {
                dasha.antardashas.forEach { antar ->
                    val isCurAntar = now >= antar.start && now < antar.end
                    Row(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 2.dp),
                        verticalAlignment = Alignment.CenterVertically
                    ) {
                        Text(
                            localizer.planetName(antar.planet),
                            style = AppTextStyles.bodySmall.copy(
                                color = if (isCurAntar) AppColors.Primary else MaterialTheme.colorScheme.onSurface
                            ),
                            modifier = Modifier.width(80.dp)
                        )
                        Text(
                            "${formatDate(antar.start)} – ${formatDate(antar.end)}",
                            style = AppTextStyles.timeSmall.copy(fontSize = 10.sp)
                        )
                    }
                }
            }
        }
    }
}

private fun formatDate(instant: Instant): String {
    val local = instant.toLocalDateTime(TimeZone.currentSystemDefault())
    return "%04d-%02d-%02d".format(local.year, local.monthNumber, local.dayOfMonth)
}
