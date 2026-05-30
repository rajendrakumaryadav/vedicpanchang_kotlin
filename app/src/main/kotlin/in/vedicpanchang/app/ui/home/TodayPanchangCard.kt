package `in`.vedicpanchang.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.l10n.PanchangLocalizer
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import `in`.vedicpanchang.app.ui.theme.InterFamily
import androidx.compose.ui.graphics.luminance
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.TimeZone
import kotlinx.datetime.number
import kotlinx.datetime.toLocalDateTime
import kotlin.time.Instant
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Clock

private val cardShape = RoundedCornerShape(20.dp)

@Composable
fun TodayPanchangCard(
    panchang: PanchangModel,
    livePanchang: PanchangModel,
    strings: Map<String, String>,
    localizer: PanchangLocalizer,
    locale: String
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val bgGradient = if (isDark)
        Brush.linearGradient(listOf(Color(0xFF1F1B2E), Color(0xFF2A1F3D)))
    else
        Brush.linearGradient(listOf(Color(0xFFFEFDFB), Color(0xFFF5E6D3)))

    val borderColor = if (isDark)
        AppColors.Primary.copy(alpha = 0.45f)
    else
        Color(0xFFCBA35C).copy(alpha = 0.55f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(cardShape)
            .border(1.dp, borderColor, cardShape)
            .background(bgGradient)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {

            // Header row — "Panchang" label + optional festival/eclipse badge
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    strings["panchang"] ?: "Panchang",
                    style = AppTextStyles.saffronLabel
                )
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                    if (panchang.isAdhikmash) {
                        AdhikmashBadge(strings["adhikmash_label"] ?: "Adhikmash")
                    }
                    if (panchang.hasEclipse) {
                        EclipseBadge(
                            label = if (panchang.lunarEclipse)
                                strings["lunar_eclipse"] ?: "Lunar Eclipse"
                            else
                                strings["solar_eclipse"] ?: "Solar Eclipse"
                        )
                    }
                    if (panchang.hasFestivals) {
                        FestivalBadge(name = localizer.festivalName(panchang.primaryFestival!!))
                    }
                }
            }

            Spacer(Modifier.height(16.dp))

            // Current live tithi banner
            ActualTithiRow(
                label = strings["actual_tithi"] ?: "Actual Tithi",
                tithi = localizer.tithiDisplay(livePanchang),
                endTime = livePanchang.tithiEndTime,
                localizer = localizer,
                isDark = isDark
            )

            Spacer(Modifier.height(20.dp))

            // 2×2 grid
            Row(Modifier.fillMaxWidth()) {
                PanchangElement(
                    modifier = Modifier.weight(1f),
                    icon = moonPhaseEmoji(panchang.tithiIndex),
                    label = (strings["day_tithi"] ?: "Day Tithi").uppercase(),
                    value = localizer.tithiDisplay(panchang),
//                    subvalue = "#${panchang.tithiIndex + 1}",
                    endTime = panchang.tithiEndTime,
                    localizer = localizer,
                    isDark = isDark
                )
                Spacer(Modifier.width(20.dp))
                PanchangElement(
                    modifier = Modifier.weight(1f),
                    icon = "⭐",
                    label = (strings["nakshatra"] ?: "Nakshatra").uppercase(),
                    value = localizer.nakshatraName(livePanchang),
                    endTime = livePanchang.nakshatraEndTime,
                    localizer = localizer,
                    isDark = isDark
                )
            }

            Spacer(Modifier.height(20.dp))

            Row(Modifier.fillMaxWidth()) {
                PanchangElement(
                    modifier = Modifier.weight(1f),
                    icon = "☀️",
                    label = (strings["yoga"] ?: "Yoga").uppercase(),
                    value = localizer.yogaName(livePanchang),
                    subvalue = localizer.yogaAuspiciousLabel(livePanchang),
                    subvalueColor = if (livePanchang.isYogaAuspicious) AppColors.Auspicious else AppColors.Inauspicious,
                    endTime = livePanchang.yogaEndTime,
                    localizer = localizer,
                    isDark = isDark
                )
                Spacer(Modifier.width(20.dp))
                PanchangElement(
                    modifier = Modifier.weight(1f),
                    icon = "🔀",
                    label = (strings["karana"] ?: "Karana").uppercase(),
                    value = localizer.karanaName(livePanchang),
                    subvalue = "→ ${localizer.karanaNext(livePanchang)}",
                    endTime = livePanchang.karanaChangeTime,
                    localizer = localizer,
                    isDark = isDark
                )
            }

            Spacer(Modifier.height(18.dp))

            // Vaar row
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .clip(RoundedCornerShape(10.dp))
                    .background(if (isDark) AppColors.SurfaceVariant else Color(0xFFFDFCFB))
                    .padding(horizontal = 12.dp, vertical = 8.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("📅", fontSize = 16.sp)
                Spacer(Modifier.width(8.dp))
                Text(
                    "${strings["vaar"] ?: "Vaar"}: ",
                    style = AppTextStyles.bodySmall.copy(
                        color = if (isDark) AppColors.TextSecondary else AppColors.TextSecondaryLight
                    )
                )
                Text(
                    localizer.vaarName(panchang),
                    style = AppTextStyles.labelLarge.copy(
                        color = if (isDark) AppColors.Secondary else AppColors.Primary
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                val dateFmt = SimpleDateFormat("dd MMM yyyy", Locale.ENGLISH)
                Text(
                    localizer.numerals(dateFmt.format(
                        java.util.Calendar.getInstance().apply {
                            set(panchang.date.year,
                                panchang.date.month.number - 1, panchang.date.day)
                        }.time
                    )),
                    style = AppTextStyles.timeSmall.copy(
                        color = if (isDark) AppColors.TextSecondary else Color(0xFF4E4238)
                    )
                )
            }
        }
    }
}

private fun moonPhaseEmoji(tithiIndex: Int) = when {
    tithiIndex == 14 -> "🌕"
    tithiIndex == 29 -> "🌑"
    tithiIndex < 7   -> "🌒"
    tithiIndex < 11  -> "🌓"
    tithiIndex < 14  -> "🌔"
    tithiIndex < 19  -> "🌖"
    tithiIndex < 22  -> "🌗"
    else             -> "🌘"
}

private val ENGLISH_MONTHS = arrayOf(
    "Jan","Feb","Mar","Apr","May","Jun","Jul","Aug","Sep","Oct","Nov","Dec"
)

// Returns only the formatted time — callers prepend the localized "until" label.
private fun endTimeStr(endTime: Instant): String {
    val tz = TimeZone.currentSystemDefault()
    val e = endTime.toLocalDateTime(tz)
    val today = Clock.System.now().toLocalDateTime(tz).date
    return if (e.date == today)
        "%02d:%02d".format(e.hour, e.minute)
    else
        "%d %s, %02d:%02d".format(
            e.day,
            ENGLISH_MONTHS[e.month.number - 1],
            e.hour, e.minute
        )
}

@Composable
private fun ActualTithiRow(
    label: String,
    tithi: String,
    endTime: Instant,
    localizer: PanchangLocalizer,
    isDark: Boolean
) {
    val rowShape = RoundedCornerShape(10.dp)
    val borderColor = if (isDark)
        AppColors.Primary.copy(alpha = 0.35f)
    else
        Color(0xFFCBA35C).copy(alpha = 0.45f)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(rowShape)
            .border(1.dp, borderColor, rowShape)
            .background(
                if (isDark) AppColors.SurfaceVariant.copy(alpha = 0.45f)
                else AppColors.SurfaceVariantLight.copy(alpha = 0.55f)
            )
            .padding(horizontal = 10.dp, vertical = 8.dp),
        contentAlignment = Alignment.Center
    ) {
        val timeStr = "${localizer.untilLabel} ${localizer.numerals(endTimeStr(endTime))}"
        Text(
            "$label: $tithi  •  $timeStr",
            style = AppTextStyles.bodySmall.copy(
                color = if (isDark) AppColors.TextPrimary else AppColors.TextPrimaryLight
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun PanchangElement(
    modifier: Modifier = Modifier,
    icon: String,
    label: String,
    value: String,
    subvalue: String? = null,
    subvalueColor: Color? = null,
    endTime: Instant,
    localizer: PanchangLocalizer,
    isDark: Boolean
) {
    val labelColor = if (isDark) AppColors.TextSecondary else AppColors.TextSecondaryLight
    val valueColor = if (isDark) AppColors.TextPrimary else AppColors.TextPrimaryLight
    val rangeColor = if (isDark) AppColors.TextSecondary else Color(0xFF4E4238)
    val resolvedSubColor = subvalueColor ?: if (isDark) AppColors.TextMuted else AppColors.TextSecondaryLight

    Column(modifier = modifier) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(icon, fontSize = 14.sp)
            Spacer(Modifier.width(4.dp))
            Text(label, style = AppTextStyles.labelSmall.copy(color = labelColor), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(6.dp))
        Text(
            value,
            style = AppTextStyles.bodyMedium.copy(
                fontFamily = InterFamily,
                fontWeight = FontWeight.SemiBold,
                fontSize = 15.sp,
                color = valueColor
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
        if (subvalue != null) {
            Spacer(Modifier.height(2.dp))
            Text(subvalue, style = AppTextStyles.bodySmall.copy(color = resolvedSubColor, fontSize = 11.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
        }
        Spacer(Modifier.height(2.dp))
        Text(
            "${localizer.untilLabel} ${localizer.numerals(endTimeStr(endTime))}",
            style = AppTextStyles.timeSmall.copy(fontSize = 11.sp, color = rangeColor)
        )
    }
}

@Composable
fun FestivalBadge(name: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(AppColors.SaffronGradientStart, AppColors.SaffronGradientEnd)))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(name, style = AppTextStyles.labelSmall.copy(color = Color.White, fontSize = 11.sp), maxLines = 1, overflow = TextOverflow.Ellipsis)
    }
}

@Composable
fun AdhikmashBadge(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF6A1B9A), Color(0xFF9C27B0))))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            "🌀 $label",
            style = AppTextStyles.labelSmall.copy(color = Color.White, fontSize = 10.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun EclipseBadge(label: String) {
    Box(
        modifier = Modifier
            .clip(RoundedCornerShape(20.dp))
            .background(Brush.linearGradient(listOf(Color(0xFF212121), Color(0xFF424242))))
            .padding(horizontal = 10.dp, vertical = 4.dp)
    ) {
        Text(
            "🌑 $label",
            style = AppTextStyles.labelSmall.copy(color = Color(0xFFFFD54F), fontSize = 10.sp),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}

@Composable
fun ColumnDivider(isDark: Boolean) {
    Box(
        modifier = Modifier
            .width(1.dp)
            .height(60.dp)
            .padding(horizontal = 12.dp)
            .background(if (isDark) AppColors.CardBorder else AppColors.CardBorderLight)
    )
    Spacer(Modifier.width(12.dp))
}
