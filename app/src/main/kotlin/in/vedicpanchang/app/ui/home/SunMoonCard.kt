package `in`.vedicpanchang.app.ui.home

import androidx.compose.foundation.background
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
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import androidx.compose.ui.graphics.luminance
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.cos
import kotlin.math.max
import kotlin.math.min
import kotlin.math.sin
import kotlin.time.Instant
@Composable
fun SunMoonCard(
    panchang: PanchangModel,
    liveNow: Instant,
    strings: Map<String, String>,
    locale: String
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    val sunProgress = calculateProgress(panchang.sunrise, panchang.sunset, liveNow)
    val moonProgress = calculateProgress(panchang.moonrise, panchang.moonset, liveNow)
    val isDay = liveNow > panchang.sunrise && liveNow < panchang.sunset
    // isMoonVisible handles both the normal case (moonrise→moonset same day) and the
    // overnight case (moonset early morning < moonrise evening).
    // Extra pre-dawn guard: if today's moonset is in the early morning (before sunrise)
    // and we're before that moonset, the moon from yesterday's cycle is still up.
    val isMoonVisible = isMoonVisible(panchang.moonrise, panchang.moonset, liveNow) ||
        (panchang.moonset < panchang.sunrise && liveNow < panchang.moonset)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(24.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Text(
                strings["sun_moon"] ?: "Sun & Moon",
                style = AppTextStyles.saffronLabel
            )
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                // Left: Sun column
                SunMoonTimeColumn(
                    topIcon = "🌅",
                    topLabel = strings["sunrise"] ?: "Sunrise",
                    topTime = panchang.sunrise,
                    topColor = AppColors.SunColor,
                    bottomIcon = "🌇",
                    bottomLabel = strings["sunset"] ?: "Sunset",
                    bottomTime = panchang.sunset,
                    bottomColor = AppColors.Primary,
                    alignEnd = false
                )

                // Center: arc canvas
                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    androidx.compose.foundation.Canvas(modifier = Modifier.fillMaxSize()) {
                        drawCelestialArc(isDark)
                        val moonDotColor = if (isDark) AppColors.MoonColor else Color(0xFF607D8B)
                        drawCelestialDot(sunProgress,  AppColors.SunColor, 12f, if (isDay) 1f else 0.45f, !isDay)
                        drawCelestialDot(moonProgress, moonDotColor,        9f,  if (isMoonVisible) 1f else 0.50f, !isMoonVisible)
                    }
                    // HUD: remaining time
                    val sunToRise = if (liveNow < panchang.sunrise) panchang.sunrise.toEpochMilliseconds() - liveNow.toEpochMilliseconds() else Long.MAX_VALUE
                    val moonToRise = if (!isMoonVisible) { val t = panchang.moonrise.toEpochMilliseconds() - liveNow.toEpochMilliseconds(); if (t > 0) t else Long.MAX_VALUE } else Long.MAX_VALUE
                    Column(
                        modifier = Modifier.align(Alignment.BottomCenter).padding(bottom = 12.dp),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        SunMoonHud(
                            label = strings["sun_short"] ?: "SUN",
                            color = AppColors.SunColor,
                            remaining = if (isDay) {
                                (panchang.sunset.toEpochMilliseconds() - liveNow.toEpochMilliseconds()).coerceAtLeast(0)
                            } else if (sunToRise <= 7_200_000L) {
                                sunToRise.coerceAtLeast(0)
                            } else null,
                            isLeft = isDay,
                            strings = strings,
                            locale = locale,
                            isDark = isDark
                        )
                        Spacer(Modifier.height(4.dp))
                        SunMoonHud(
                            label = strings["moon_short"] ?: "MOON",
                            color = if (isDark) AppColors.MoonColor else Color(0xFF607D8B),
                            remaining = if (isMoonVisible) {
                                val moonEnd = panchang.moonset.toEpochMilliseconds()
                                if (moonEnd > liveNow.toEpochMilliseconds()) (moonEnd - liveNow.toEpochMilliseconds()).coerceAtLeast(0) else null
                            } else if (moonToRise <= 7_200_000L) {
                                moonToRise.coerceAtLeast(0)
                            } else null,
                            isLeft = isMoonVisible,
                            strings = strings,
                            locale = locale,
                            isDark = isDark
                        )
                    }
                }

                // Right: Moon column
                SunMoonTimeColumn(
                    topIcon = "🌙",
                    topLabel = strings["moonrise"] ?: "Moonrise",
                    topTime = panchang.moonrise,
                    topColor = if (isDark) AppColors.MoonColor else Color(0xFF607D8B),
                    bottomIcon = "🌛",
                    bottomLabel = strings["moonset"] ?: "Moonset",
                    bottomTime = panchang.moonset,
                    bottomColor = if (isDark) AppColors.TextMuted else Color(0xFF607D8B),
                    alignEnd = true
                )
            }
        }
    }
}

@Composable
private fun SunMoonTimeColumn(
    topIcon: String, topLabel: String, topTime: Instant, topColor: Color,
    bottomIcon: String, bottomLabel: String, bottomTime: Instant, bottomColor: Color,
    alignEnd: Boolean
) {
    val tz = TimeZone.currentSystemDefault()
    val topLocal = topTime.toLocalDateTime(tz)
    val bottomLocal = bottomTime.toLocalDateTime(tz)
    val align = if (alignEnd) Alignment.End else Alignment.Start
    Column(horizontalAlignment = align) {
        TimeItem(icon = topIcon, label = topLabel, hour = topLocal.hour, minute = topLocal.minute, color = topColor, alignEnd = alignEnd)
        Spacer(Modifier.height(32.dp))
        TimeItem(icon = bottomIcon, label = bottomLabel, hour = bottomLocal.hour, minute = bottomLocal.minute, color = bottomColor, alignEnd = alignEnd)
    }
}

@Composable
private fun TimeItem(icon: String, label: String, hour: Int, minute: Int, color: Color, alignEnd: Boolean) {
    val align = if (alignEnd) Alignment.End else Alignment.Start
    Column(horizontalAlignment = align) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!alignEnd) Text(icon, fontSize = 12.sp)
            Spacer(Modifier.width(2.dp))
            Text(label, style = AppTextStyles.labelSmall.copy(fontSize = 10.sp, color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)))
            Spacer(Modifier.width(2.dp))
            if (alignEnd) Text(icon, fontSize = 12.sp)
        }
        Text("%02d:%02d".format(hour, minute), style = AppTextStyles.timeMedium.copy(color = color, fontSize = 16.sp))
    }
}

@Composable
private fun SunMoonHud(
    label: String, color: Color, remaining: Long?, isLeft: Boolean,
    strings: Map<String, String>, locale: String, isDark: Boolean
) {
    if (remaining == null) return
    val h = (remaining / 3600000).toInt()
    val m = ((remaining % 3600000) / 60000).toInt()
    val hourLabel = strings["hour_short"] ?: "h"
    val minLabel = strings["minute_short"] ?: "min"
    val actionLabel = if (isLeft) strings["time_left"] ?: "left" else strings["time_to_rise"] ?: "to rise"
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(6.dp))
            .background(color.copy(alpha = if (isDark) 0.10f else 0.16f))
            .padding(horizontal = 8.dp, vertical = 2.dp)
    ) {
        Text(
            "$label: $h$hourLabel $m$minLabel $actionLabel",
            style = androidx.compose.ui.text.TextStyle(
                fontSize = 8.sp,
                fontWeight = androidx.compose.ui.text.font.FontWeight.ExtraBold,
                color = if (isDark) color else AppColors.PrimaryDark,
                letterSpacing = 0.5.sp
            )
        )
    }
}

private fun calculateProgress(start: Instant, end: Instant, now: Instant): Float {
    var s = start.toEpochMilliseconds()
    var e = end.toEpochMilliseconds()
    val n = now.toEpochMilliseconds()
    if (e < s) {
        if (n < e) s -= 86400_000L else e += 86400_000L
    }
    val total = (e - s).toFloat()
    if (total == 0f) return 0.5f
    return ((n - s).toFloat() / total).coerceIn(0f, 1f)
}

private fun isMoonVisible(rise: Instant, set: Instant, now: Instant): Boolean {
    val r = rise.toEpochMilliseconds()
    val s = set.toEpochMilliseconds()
    val n = now.toEpochMilliseconds()
    return if (s > r) n in r..s else n !in s..r
}

private fun DrawScope.drawCelestialArc(isDark: Boolean) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f + 30f
    val radius = max(24f, min(size.width / 2f - 20f, size.height - 48f))
    val arcColor = if (isDark) Color.White.copy(alpha = 0.1f) else Color.Black.copy(alpha = 0.05f)

    drawArc(
        color = arcColor,
        startAngle = 180f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(centerX - radius, centerY - radius),
        size = androidx.compose.ui.geometry.Size(radius * 2, radius * 2),
        style = Stroke(width = 2.dp.toPx())
    )
    drawLine(
        color = arcColor.copy(alpha = arcColor.alpha * 0.2f),
        start = Offset(10f, centerY),
        end = Offset(size.width - 10f, centerY),
        strokeWidth = 1.dp.toPx()
    )
}

private fun DrawScope.drawCelestialDot(progress: Float, color: Color, radiusDp: Float, alpha: Float = 1f, belowHorizon: Boolean = false) {
    val centerX = size.width / 2f
    val centerY = size.height / 2f + 30f
    val arcRadius = max(24f, min(size.width / 2f - 20f, size.height - 48f))
    val angle = Math.PI - (progress * Math.PI)
    val dx = (arcRadius * cos(angle)).toFloat()
    // Below-horizon bodies sit just under the chord so they remain visible and
    // don't overlap with in-sky bodies that happen to share the same arc edge.
    val dy = if (belowHorizon) 14.dp.toPx() else (-arcRadius * sin(angle)).toFloat()
    val bodyRadius = radiusDp.dp.toPx()
    drawCircle(color.copy(alpha = 0.35f * alpha), bodyRadius * 2f, Offset(centerX + dx, centerY + dy))
    drawCircle(color.copy(alpha = alpha),          bodyRadius,      Offset(centerX + dx, centerY + dy))
}
