package `in`.vedicpanchang.app.ui.daydetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.KeyboardArrowDown
import androidx.compose.material.icons.filled.KeyboardArrowUp
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.l10n.PanchangLocalizer
import `in`.vedicpanchang.app.ui.home.SunMoonCard
import `in`.vedicpanchang.app.ui.home.TodayPanchangCard
import `in`.vedicpanchang.app.ui.navigation.AppBottomNav
import `in`.vedicpanchang.app.ui.navigation.NavRoutes
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import `in`.vedicpanchang.app.viewmodel.LocationUiState
import `in`.vedicpanchang.app.viewmodel.PanchangViewModel
import `in`.vedicpanchang.app.viewmodel.SettingsViewModel
import `in`.vedicpanchang.astronomy.TimeRange
import kotlinx.coroutines.launch
import kotlinx.datetime.*
import kotlinx.datetime.LocalDateTime
import kotlin.time.Clock
import java.text.SimpleDateFormat
import java.util.Locale as JavaLocale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DayDetailScreen(
    dateString: String,
    navController: NavController,
    panchangVm: PanchangViewModel = hiltViewModel(),
    settingsVm: SettingsViewModel = hiltViewModel()
) {
    val strings by settingsVm.strings.collectAsStateWithLifecycle()
    val locale by settingsVm.locale.collectAsStateWithLifecycle()
    val localizer by settingsVm.panchangLocalizer.collectAsStateWithLifecycle()
    val panchangState by panchangVm.state.collectAsStateWithLifecycle()

    val date = remember(dateString) {
        runCatching {
            val parts = dateString.split("-")
            LocalDate(parts[0].toInt(), parts[1].toInt(), parts[2].toInt())
        }.getOrNull() ?: Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    }

    var panchang by remember { mutableStateOf<PanchangModel?>(null) }
    val location = (panchangState.location as? LocationUiState.Success)?.location
    LaunchedEffect(date, location) {
        panchang = panchangVm.getPanchangForDate(date, location)
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val p = panchang
                    if (p != null) {
                        val vaarName = localizer.vaarName(p)
                        val javaLocale = if (locale == "hi" || locale == "sa")
                            JavaLocale("hi", "IN") else JavaLocale.ENGLISH
                        val cal = java.util.Calendar.getInstance()
                        cal.set(date.year, date.monthNumber - 1, date.dayOfMonth)
                        val dateStr = localizer.numerals(
                            SimpleDateFormat("d MMM yyyy", javaLocale).format(cal.time)
                        )
                        Column {
//                            Row(verticalAlignment = Alignment.CenterVertically) {
//                                Icon(
//                                    imageVector = Icons.Default.LocationOn,
//                                    contentDescription = null,
//                                    tint = Color(0xFFE53935),
//                                    modifier = Modifier.size(12.dp)
//                                )
//                                Spacer(Modifier.width(3.dp))
//                                Text(
//                                    text = p.locationName,
//                                    style = AppTextStyles.bodySmall.copy(
//                                        color = AppColors.TextSecondary, fontSize = 11.sp
//                                    ),
//                                    maxLines = 1,
//                                    overflow = TextOverflow.Ellipsis
//                                )
//                            }
                            Text("$vaarName, $dateStr")
                        }
                    } else {
                        Text(date.toString())
                    }
                },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = AppColors.Background)
            )
        },
        bottomBar = {
            AppBottomNav(navController = navController)
        },
        containerColor = AppColors.Background
    ) { padding ->
        val p = panchang
        if (p == null) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                CircularProgressIndicator(color = AppColors.Primary)
            }
        } else {
            LazyColumn(
                modifier = Modifier.fillMaxSize().padding(padding),
                contentPadding = PaddingValues(bottom = 40.dp)
            ) {
                // 1. Festival & Events Banner
                if (p.hasFestivals) {
                    item { FestivalBannerCard(panchang = p, strings = strings, localizer = localizer) }
                }
                // 2. Vedic Calendar Section
                item { VedicCalendarSection(panchang = p, strings = strings, localizer = localizer) }
                // 5. Auspicious Periods
                item { AuspiciousMuhurtasCard(panchang = p, strings = strings, localizer = localizer) }
                // 6. Inauspicious Periods
                item { InauspiciousPeriodsCard(panchang = p, strings = strings, localizer = localizer) }
                // 7. Day Muhurtas (Collapsable)
                item { DaytimeMuhurtasCard(panchang = p, strings = strings, localizer = localizer) }
                // 8. Astronomical Data (Collapsable)
                item { AstronomicalDataCard(panchang = p, strings = strings, localizer = localizer) }
            }
        }
    }
}

// ─── 1. Festival Banner ───────────────────────────────────────────────────────

@Composable
private fun FestivalBannerCard(
    panchang: PanchangModel,
    strings: Map<String, String>,
    localizer: PanchangLocalizer
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(
                Brush.linearGradient(listOf(AppColors.SaffronGradientStart, AppColors.SaffronGradientEnd))
            )
            .padding(horizontal = 20.dp, vertical = 14.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text("🪔", fontSize = 22.sp)
            Spacer(Modifier.width(12.dp))
            Column {
                Text(
                    text = (strings["festivals_events"] ?: "🪔 Festivals & Events").trim(),
                    style = AppTextStyles.labelSmall.copy(color = Color.White.copy(alpha = 0.85f), fontSize = 10.sp)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = panchang.festivals.joinToString(" • ") { localizer.festivalName(it) },
                    style = AppTextStyles.bodyMedium.copy(
                        color = Color.White, fontWeight = FontWeight.Bold, fontSize = 15.sp
                    )
                )
            }
        }
    }
}

// ─── 2. Vedic Calendar Section ────────────────────────────────────────────────

@Composable
private fun VedicCalendarSection(
    panchang: PanchangModel,
    strings: Map<String, String>,
    localizer: PanchangLocalizer
) {
    val isDark = isSystemInDarkTheme()
    val monthNum = panchang.date.month.ordinal + 1
    val vedicMonth = localizer.vedicMonthName(panchang)
    val vikramYear = localizer.numerals(
        localizer.vikramSamvatYear(panchang.date.year, monthNum).toString()
    )
    val shakaYear = localizer.numerals(
        localizer.shakaSamvatYear(panchang.date.year, monthNum, panchang.date.dayOfMonth).toString()
    )
    val vedicDateLine = localizer.vedicDateLine(panchang)

    val dividerColor = if (isDark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.07f)
    val labelColor = if (isDark) AppColors.TextSecondary else AppColors.TextSecondaryLight
    val valueColor = if (isDark) AppColors.TextPrimary else AppColors.TextPrimaryLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDark) AppColors.Surface else AppColors.SurfaceLight)
            .padding(16.dp)
    ) {
        Text(
            text = strings["vedic_calendar"] ?: "Vedic Calendar",
            style = AppTextStyles.saffronLabel.copy(fontSize = 14.sp)
        )
        Spacer(Modifier.height(16.dp))

        VedicCalRow(icon = "🗓️", label = strings["vedic_month"] ?: "Vedic Month",   value = vedicMonth,  labelColor = labelColor, valueColor = valueColor)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = dividerColor, thickness = 0.5.dp)
        VedicCalRow(icon = "🔱", label = strings["vikram_samvat"] ?: "Vikram Samvat", value = vikramYear, labelColor = labelColor, valueColor = valueColor)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = dividerColor, thickness = 0.5.dp)
        VedicCalRow(icon = "🏛️", label = strings["shaka_samvat"] ?: "Shaka Samvat",  value = shakaYear,  labelColor = labelColor, valueColor = valueColor)
        HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = dividerColor, thickness = 0.5.dp)

        // Vedic Tithi — full composite string (wraps if needed)
        Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
            Text("🕉️", fontSize = 18.sp)
            Spacer(Modifier.width(12.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = strings["vedic_date"] ?: "Vedic Date",
                    style = AppTextStyles.labelSmall.copy(color = labelColor, fontSize = 11.sp)
                )
                Spacer(Modifier.height(3.dp))
                Text(
                    text = vedicDateLine,
                    style = AppTextStyles.bodySmall.copy(
                        color = valueColor,
                        fontWeight = FontWeight.SemiBold,
                        fontSize = 12.sp,
                        lineHeight = 18.sp
                    )
                )
            }
        }
    }
}

@Composable
private fun VedicCalRow(
    icon: String,
    label: String,
    value: String,
    labelColor: Color,
    valueColor: Color
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 18.sp)
        Spacer(Modifier.width(12.dp))
        Text(
            text = label,
            style = AppTextStyles.bodySmall.copy(color = labelColor, fontSize = 12.sp),
            modifier = Modifier.weight(1f)
        )
        Text(
            text = value,
            style = AppTextStyles.bodyMedium.copy(
                color = valueColor, fontWeight = FontWeight.SemiBold, fontSize = 14.sp
            )
        )
    }
}

// ─── 5. Auspicious Periods ────────────────────────────────────────────────────

@Composable
private fun AuspiciousMuhurtasCard(
    panchang: PanchangModel,
    strings: Map<String, String>,
    localizer: PanchangLocalizer
) {
    val isDark = isSystemInDarkTheme()
    val vijaya = panchang.auspiciousMuhurtas.find { it.id == "vijaya_muhurta" }?.range
    val godhuli = panchang.auspiciousMuhurtas.find { it.id == "godhuli_muhurta" }?.range

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDark) AppColors.Surface else AppColors.SurfaceLight)
            .padding(16.dp)
    ) {
        Text(
            text = strings["auspicious_periods"] ?: "Auspicious Periods",
            style = AppTextStyles.saffronLabel.copy(fontSize = 14.sp)
        )
        Spacer(Modifier.height(16.dp))
        MuhurtaRow(icon = "🙏", name = strings["brahma_muhurta"] ?: "Brahma Muhurta",   desc = strings["best_meditation"] ?: "Best for meditation & prayer",        range = panchang.brahmaMuhurta,  barColor = AppColors.Auspicious, timeColor = AppColors.Auspicious, localizer = localizer, isDark = isDark)
        Spacer(Modifier.height(14.dp))
        MuhurtaRow(icon = "⚡", name = strings["abhijit_muhurta"] ?: "Abhijit Muhurta", desc = strings["best_beginnings"] ?: "Best for new beginnings",             range = panchang.abhijitMuhurta, barColor = AppColors.Auspicious, timeColor = AppColors.Auspicious, localizer = localizer, isDark = isDark)
        if (vijaya != null) {
            Spacer(Modifier.height(14.dp))
            MuhurtaRow(icon = "🏆", name = strings["vijaya_muhurta"] ?: "Vijaya Muhurta",   desc = strings["vijaya_note"] ?: "Best for success and winning efforts", range = vijaya,                  barColor = AppColors.Auspicious, timeColor = AppColors.Auspicious, localizer = localizer, isDark = isDark)
        }
        if (godhuli != null) {
            Spacer(Modifier.height(14.dp))
            MuhurtaRow(icon = "🐄", name = strings["godhuli_muhurta"] ?: "Godhuli Muhurta",  desc = strings["godhuli_note"] ?: "Auspicious twilight window",         range = godhuli,                 barColor = AppColors.Auspicious, timeColor = AppColors.Auspicious, localizer = localizer, isDark = isDark)
        }
    }
}

// ─── 6. Inauspicious Periods ──────────────────────────────────────────────────

@Composable
private fun InauspiciousPeriodsCard(
    panchang: PanchangModel,
    strings: Map<String, String>,
    localizer: PanchangLocalizer
) {
    val isDark = isSystemInDarkTheme()

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDark) AppColors.Surface else AppColors.SurfaceLight)
            .padding(16.dp)
    ) {
        Text(
            text = strings["inauspicious_periods"] ?: "Inauspicious Periods",
            style = AppTextStyles.saffronLabel.copy(fontSize = 14.sp)
        )
        Spacer(Modifier.height(16.dp))
        MuhurtaRow(icon = "⚠️", name = strings["rahu_kaal"] ?: "Rahu Kaal",     desc = strings["avoid_auspicious"] ?: "Avoid auspicious activities", range = panchang.rahuKaal,   barColor = AppColors.Inauspicious, timeColor = AppColors.Inauspicious, localizer = localizer, isDark = isDark)
        Spacer(Modifier.height(14.dp))
        MuhurtaRow(icon = "💀", name = strings["yamaganda"] ?: "Yamaganda",     desc = strings["period_yama"] ?: "Period of Yama — inauspicious",    range = panchang.yamaganda,  barColor = AppColors.Inauspicious, timeColor = AppColors.Inauspicious, localizer = localizer, isDark = isDark)
        Spacer(Modifier.height(14.dp))
        MuhurtaRow(icon = "🪐", name = strings["gulika_kaal"] ?: "Gulika Kaal", desc = strings["saturn_period"] ?: "Saturn's inauspicious period",   range = panchang.gulikaKaal, barColor = AppColors.Inauspicious, timeColor = AppColors.Inauspicious, localizer = localizer, isDark = isDark)
    }
}

// ─── Shared Muhurta Row ───────────────────────────────────────────────────────

@Composable
private fun MuhurtaRow(
    icon: String,
    name: String,
    desc: String,
    range: TimeRange,
    barColor: Color,
    timeColor: Color,
    localizer: PanchangLocalizer,
    isDark: Boolean
) {
    val tz = TimeZone.currentSystemDefault()
    val s = range.start.toLocalDateTime(tz)
    val e = range.end.toLocalDateTime(tz)
    val sameDay = s.date == e.date
    val timeStr = localizer.numerals(
        if (sameDay) "%02d:%02d – %02d:%02d".format(s.hour, s.minute, e.hour, e.minute)
        else "%d %s, %02d:%02d – %d %s, %02d:%02d".format(
            s.day, s.month.name.take(3).lowercase().replaceFirstChar { it.uppercaseChar() }, s.hour, s.minute,
            e.day, e.month.name.take(3).lowercase().replaceFirstChar { it.uppercaseChar() }, e.hour, e.minute
        )
    )
    val textPrimary = if (isDark) AppColors.TextPrimary else AppColors.TextPrimaryLight
    val textSecondary = if (isDark) AppColors.TextSecondary else AppColors.TextSecondaryLight

    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Box(
            modifier = Modifier
                .width(4.dp)
                .height(46.dp)
                .clip(RoundedCornerShape(2.dp))
                .background(barColor)
        )
        Spacer(Modifier.width(12.dp))
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 14.sp)
                Spacer(Modifier.width(6.dp))
                Text(
                    name,
                    style = AppTextStyles.bodyMedium.copy(
                        color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp
                    )
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(desc, style = AppTextStyles.bodySmall.copy(color = textSecondary, fontSize = 11.sp))
        }
        Spacer(Modifier.width(8.dp))
        Text(
            timeStr,
            style = AppTextStyles.timeSmall.copy(
                color = timeColor, fontWeight = FontWeight.Bold, fontSize = 12.sp
            )
        )
    }
}

// ─── 7. Day Muhurtas (Collapsable) ───────────────────────────────────────────

private val DAYTIME_ICONS = listOf(
    "⚡", "🐍", "🌻", "🌿", "💎", "🐗", "🌍", "📖", "⭐", "🏹", "🔥", "🦉", "🌊", "✨", "🌸"
)

@Composable
private fun DaytimeMuhurtasCard(
    panchang: PanchangModel,
    strings: Map<String, String>,
    localizer: PanchangLocalizer
) {
    val isDark = isSystemInDarkTheme()
    var expanded by remember { mutableStateOf(false) }
    val muhurtas = panchang.daytimeMuhurtas
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.07f)

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDark) AppColors.Surface else AppColors.SurfaceLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings["day_muhurtas"] ?: "Day Muhurtas",
                style = AppTextStyles.saffronLabel.copy(fontSize = 14.sp)
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(22.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))

                muhurtas.chunked(2).forEachIndexed { rowIdx, pair ->
                    if (rowIdx > 0) Spacer(Modifier.height(10.dp))
                    Row(modifier = Modifier.fillMaxWidth()) {
                        pair.forEachIndexed { colIdx, muhurta ->
                            val globalIdx = rowIdx * 2 + colIdx
                            DaytimeMuhurtaCell(
                                icon = DAYTIME_ICONS.getOrElse(globalIdx) { "🕐" },
                                name = strings[muhurta.id] ?: muhurta.id,
                                range = muhurta.range,
                                localizer = localizer,
                                isDark = isDark,
                                modifier = Modifier.weight(1f)
                            )
                            if (colIdx == 0 && pair.size == 2) Spacer(Modifier.width(8.dp))
                        }
                        if (pair.size == 1) {
                            Spacer(Modifier.width(8.dp))
                            Spacer(Modifier.weight(1f))
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DaytimeMuhurtaCell(
    icon: String,
    name: String,
    range: TimeRange,
    localizer: PanchangLocalizer,
    isDark: Boolean,
    modifier: Modifier = Modifier
) {
    val tz = TimeZone.currentSystemDefault()
    val s = range.start.toLocalDateTime(tz)
    val e = range.end.toLocalDateTime(tz)
    val timeStr = localizer.numerals(
        "%02d:%02d – %02d:%02d".format(s.hour, s.minute, e.hour, e.minute)
    )
    val textPrimary = if (isDark) AppColors.TextPrimary else AppColors.TextPrimaryLight

    Box(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(if (isDark) AppColors.SurfaceVariant else AppColors.SurfaceVariantLight)
            .padding(horizontal = 10.dp, vertical = 10.dp)
    ) {
        Column {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(icon, fontSize = 13.sp)
                Spacer(Modifier.width(4.dp))
                Text(
                    name,
                    style = AppTextStyles.bodySmall.copy(
                        color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 11.sp
                    ),
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }
            Spacer(Modifier.height(6.dp))
            Text(timeStr, style = AppTextStyles.timeSmall.copy(fontSize = 11.sp))
        }
    }
}

// ─── 8. Astronomical Data (Collapsable) ──────────────────────────────────────

@Composable
private fun AstronomicalDataCard(
    panchang: PanchangModel,
    strings: Map<String, String>,
    localizer: PanchangLocalizer
) {
    val isDark = isSystemInDarkTheme()
    var expanded by remember { mutableStateOf(false) }
    val dividerColor = if (isDark) Color.White.copy(alpha = 0.07f) else Color.Black.copy(alpha = 0.07f)
    val textPrimary = if (isDark) AppColors.TextPrimary else AppColors.TextPrimaryLight
    val textSecondary = if (isDark) AppColors.TextSecondary else AppColors.TextSecondaryLight

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(20.dp))
            .background(if (isDark) AppColors.Surface else AppColors.SurfaceLight)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable { expanded = !expanded }
                .padding(horizontal = 16.dp, vertical = 14.dp),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Text(
                text = strings["astronomical_data"] ?: "Astronomical Data",
                style = AppTextStyles.saffronLabel.copy(fontSize = 14.sp)
            )
            Icon(
                imageVector = if (expanded) Icons.Filled.KeyboardArrowUp else Icons.Filled.KeyboardArrowDown,
                contentDescription = null,
                tint = AppColors.Primary,
                modifier = Modifier.size(22.dp)
            )
        }

        AnimatedVisibility(
            visible = expanded,
            enter = expandVertically(),
            exit = shrinkVertically()
        ) {
            Column(modifier = Modifier.padding(start = 16.dp, end = 16.dp, bottom = 16.dp)) {
                HorizontalDivider(color = dividerColor, thickness = 0.5.dp)
                Spacer(Modifier.height(12.dp))

                AstroDataRow(icon = "☀️", label = strings["sun_longitude"] ?: "Sun Longitude",   value = localizer.numerals("%.2f°".format(panchang.sunLongitude)),  textPrimary = textPrimary, textSecondary = textSecondary)
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = dividerColor, thickness = 0.5.dp)
                AstroDataRow(icon = "🌙", label = strings["moon_longitude"] ?: "Moon Longitude", value = localizer.numerals("%.2f°".format(panchang.moonLongitude)), textPrimary = textPrimary, textSecondary = textSecondary)
                HorizontalDivider(modifier = Modifier.padding(vertical = 10.dp), color = dividerColor, thickness = 0.5.dp)
                AstroDataRow(icon = "📍", label = strings["location"] ?: "Location",             value = panchang.locationName,                                       textPrimary = textPrimary, textSecondary = textSecondary)
            }
        }
    }
}

@Composable
private fun AstroDataRow(
    icon: String,
    label: String,
    value: String,
    textPrimary: Color,
    textSecondary: Color
) {
    Row(modifier = Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        Text(icon, fontSize = 16.sp)
        Spacer(Modifier.width(12.dp))
        Text(
            label,
            style = AppTextStyles.bodySmall.copy(color = textSecondary),
            modifier = Modifier.weight(1f)
        )
        Text(
            value,
            style = AppTextStyles.bodyMedium.copy(
                color = textPrimary, fontWeight = FontWeight.SemiBold, fontSize = 13.sp
            ),
            maxLines = 1,
            overflow = TextOverflow.Ellipsis
        )
    }
}
