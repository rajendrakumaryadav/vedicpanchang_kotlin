package `in`.vedicpanchang.app.ui.home

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.rounded.Nightlight
import androidx.compose.material.icons.rounded.WbSunny
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Rect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.PathOperation
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import kotlin.math.cos
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
    val cardBorderColor = if (isDark) AppColors.Primary.copy(alpha = 0.45f) else Color(0xFFCBA35C).copy(alpha = 0.55f)
    val cardShape = RoundedCornerShape(24.dp)

    val sunProgress = calculateProgress(panchang.sunrise, panchang.sunset, liveNow)
    val moonProgress = calculateProgress(panchang.moonrise, panchang.moonset, liveNow)
    val isDay = liveNow > panchang.sunrise && liveNow < panchang.sunset
    val isMoonVisible = isMoonVisible(panchang.moonrise, panchang.moonset, liveNow) ||
        (panchang.moonset < panchang.sunrise && liveNow < panchang.moonset)

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
            Text(
                strings["sun_moon"] ?: "Sun & Moon",
                style = AppTextStyles.saffronLabel
            )
            Spacer(Modifier.height(20.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                SunMoonTimeColumn(
                    topIcon = {
                        Icon(
                            Icons.Rounded.WbSunny,
                            contentDescription = null,
                            tint = AppColors.SunColor,
                            modifier = Modifier.size(13.dp)
                        )
                    },
                    topLabel = strings["sunrise"] ?: "Sunrise",
                    topTime = panchang.sunrise,
                    topColor = AppColors.SunColor,
                    bottomIcon = {
                        Icon(
                            Icons.Rounded.WbSunny,
                            contentDescription = null,
                            tint = AppColors.Primary,
                            modifier = Modifier.size(13.dp)
                        )
                    },
                    bottomLabel = strings["sunset"] ?: "Sunset",
                    bottomTime = panchang.sunset,
                    bottomColor = AppColors.Primary,
                    alignEnd = false
                )

                Box(
                    modifier = Modifier
                        .weight(1f)
                        .height(140.dp),
                    contentAlignment = Alignment.Center
                ) {
                    val moonColor = if (isDark) AppColors.MoonColor else Color(0xFF607D8B)
                    Canvas(modifier = Modifier.fillMaxSize()) {
                        drawSkyArc(isDark)
                        if (isDay) drawSunIcon(sunProgress, 1f)
                        if (isMoonVisible) drawMoonIcon(moonProgress, 1f, moonColor)
                    }

                    val sunToRise = if (liveNow < panchang.sunrise)
                        panchang.sunrise.toEpochMilliseconds() - liveNow.toEpochMilliseconds()
                    else Long.MAX_VALUE
                    val moonToRise = if (!isMoonVisible) {
                        val t = panchang.moonrise.toEpochMilliseconds() - liveNow.toEpochMilliseconds()
                        if (t > 0) t else Long.MAX_VALUE
                    } else Long.MAX_VALUE

                    Column(
                        modifier = Modifier
                            .align(Alignment.BottomCenter)
                            .padding(bottom = 10.dp),
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
                            color = moonColor,
                            remaining = if (isMoonVisible) {
                                val moonEnd = panchang.moonset.toEpochMilliseconds()
                                if (moonEnd > liveNow.toEpochMilliseconds())
                                    (moonEnd - liveNow.toEpochMilliseconds()).coerceAtLeast(0)
                                else null
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

                val moonColor = if (isDark) AppColors.MoonColor else Color(0xFF607D8B)
                SunMoonTimeColumn(
                    topIcon = {
                        Icon(
                            Icons.Rounded.Nightlight,
                            contentDescription = null,
                            tint = moonColor,
                            modifier = Modifier.size(13.dp)
                        )
                    },
                    topLabel = strings["moonrise"] ?: "Moonrise",
                    topTime = panchang.moonrise,
                    topColor = moonColor,
                    bottomIcon = {
                        Icon(
                            Icons.Rounded.Nightlight,
                            contentDescription = null,
                            tint = moonColor.copy(alpha = 0.45f),
                            modifier = Modifier.size(13.dp)
                        )
                    },
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
    topIcon: @Composable () -> Unit,
    topLabel: String, topTime: Instant, topColor: Color,
    bottomIcon: @Composable () -> Unit,
    bottomLabel: String, bottomTime: Instant, bottomColor: Color,
    alignEnd: Boolean
) {
    val tz = TimeZone.currentSystemDefault()
    val topLocal = topTime.toLocalDateTime(tz)
    val bottomLocal = bottomTime.toLocalDateTime(tz)
    val align = if (alignEnd) Alignment.End else Alignment.Start
    Column(horizontalAlignment = align) {
        TimeItem(topIcon, topLabel, topLocal.hour, topLocal.minute, topColor, alignEnd)
        Spacer(Modifier.height(32.dp))
        TimeItem(bottomIcon, bottomLabel, bottomLocal.hour, bottomLocal.minute, bottomColor, alignEnd)
    }
}

@Composable
private fun TimeItem(
    icon: @Composable () -> Unit,
    label: String, hour: Int, minute: Int, color: Color, alignEnd: Boolean
) {
    val align = if (alignEnd) Alignment.End else Alignment.Start
    Column(horizontalAlignment = align) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            if (!alignEnd) { icon(); Spacer(Modifier.width(3.dp)) }
            Text(
                label,
                style = AppTextStyles.labelSmall.copy(
                    fontSize = 10.sp,
                    color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                )
            )
            if (alignEnd) { Spacer(Modifier.width(3.dp)); icon() }
        }
        Text(
            "%02d:%02d".format(hour, minute),
            style = AppTextStyles.timeMedium.copy(color = color, fontSize = 16.sp)
        )
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

// Arc geometry: horizon sits at 62% of canvas height, leaving room for HUD below.
private fun DrawScope.arcCenter(): Triple<Float, Float, Float> {
    val paddingTop = 6.dp.toPx()
    val arcRadius = min(size.width / 2f - 10.dp.toPx(), size.height * 0.60f)
        .coerceAtLeast(20.dp.toPx())
    val centerX = size.width / 2f
    val centerY = arcRadius + paddingTop
    return Triple(centerX, centerY, arcRadius)
}

private fun DrawScope.celestialOffset(progress: Float): Offset {
    val (cx, cy, r) = arcCenter()
    val angle = Math.PI - progress * Math.PI
    return Offset(cx + (r * cos(angle)).toFloat(), cy + (-r * sin(angle)).toFloat())
}

private fun DrawScope.drawSkyArc(isDark: Boolean) {
    val (cx, cy, r) = arcCenter()
    val arcColor = if (isDark) Color.White.copy(alpha = 0.10f) else Color.Black.copy(alpha = 0.06f)
    drawArc(
        color = arcColor,
        startAngle = 180f, sweepAngle = 180f, useCenter = false,
        topLeft = Offset(cx - r, cy - r),
        size = androidx.compose.ui.geometry.Size(r * 2, r * 2),
        style = Stroke(width = 1.5.dp.toPx())
    )
    drawLine(
        color = arcColor,
        start = Offset(cx - r, cy),
        end = Offset(cx + r, cy),
        strokeWidth = 1.dp.toPx()
    )
}

private fun DrawScope.drawSunIcon(progress: Float, alpha: Float) {
    val center = celestialOffset(progress)
    val bodyR = 8.dp.toPx()
    val rayInner = bodyR + 3.dp.toPx()
    val rayOuter = bodyR + 6.dp.toPx()
    val color = AppColors.SunColor.copy(alpha = alpha)

    // Subtle halo
    drawCircle(AppColors.SunColor.copy(alpha = 0.14f * alpha), bodyR * 1.7f, center)

    // Eight rays
    repeat(8) { i ->
        val a = i * 45.0 * Math.PI / 180.0
        drawLine(
            color = color,
            start = Offset(center.x + (rayInner * cos(a)).toFloat(), center.y + (rayInner * sin(a)).toFloat()),
            end   = Offset(center.x + (rayOuter * cos(a)).toFloat(), center.y + (rayOuter * sin(a)).toFloat()),
            strokeWidth = 1.8.dp.toPx(),
            cap = StrokeCap.Round
        )
    }

    // Core disc
    drawCircle(color, bodyR, center)
}

private fun DrawScope.drawMoonIcon(progress: Float, alpha: Float, moonColor: Color) {
    val center = celestialOffset(progress)
    val bodyR = 7.dp.toPx()
    val tint = moonColor.copy(alpha = alpha)

    // Subtle halo
    drawCircle(tint.copy(alpha = 0.10f), bodyR * 1.7f, center)

    // Crescent: full disc minus offset inner disc
    val outer = Path().apply {
        addOval(Rect(center.x - bodyR, center.y - bodyR, center.x + bodyR, center.y + bodyR))
    }
    val cutShift = bodyR * 0.52f
    val inner = Path().apply {
        addOval(Rect(center.x - bodyR + cutShift, center.y - bodyR, center.x + bodyR + cutShift, center.y + bodyR))
    }
    drawPath(Path.combine(PathOperation.Difference, outer, inner), tint)
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
