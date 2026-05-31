package `in`.vedicpanchang.app.ui.daydetail

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.expandVertically
import androidx.compose.animation.shrinkVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Home
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.l10n.PanchangLocalizer
import `in`.vedicpanchang.app.ui.home.DishasoolCard
import `in`.vedicpanchang.app.ui.home.EclipseAlertCard
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
import kotlinx.datetime.LocalDate
import kotlinx.datetime.LocalDateTime
import kotlinx.datetime.number
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
    var panchangLoadFailed by remember { mutableStateOf(false) }
    var retryTrigger by remember { mutableIntStateOf(0) }
    val location = (panchangState.location as? LocationUiState.Success)?.location
    LaunchedEffect(date, location, retryTrigger) {
        panchangLoadFailed = false
        val result = panchangVm.getPanchangForDate(date, location)
        if (result != null) panchang = result else panchangLoadFailed = true
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    val p = panchang
                    if (p != null) {
                        val vaarName = localizer.vaarName(p)
                        val javaLocale = if (locale == "hi" || locale == "sa")
                            JavaLocale.forLanguageTag("hi-IN") else JavaLocale.ENGLISH
                        val cal = java.util.Calendar.getInstance()
                        cal.set(date.year, date.month.number - 1, date.day)
                        val dateStr = localizer.numerals(
                            SimpleDateFormat("d MMM yyyy", javaLocale).format(cal.time)
                        )
                        Column {
                            Text("$vaarName, $dateStr")
                        }
                    } else {
                        Text(date.toString())
                    }
                },
                navigationIcon = {
                    IconButton(onClick = {
                        val popped = navController.popBackStack()
                        if (!popped) {
                            navController.navigate(NavRoutes.HOME) {
                                popUpTo(NavRoutes.HOME) { inclusive = false }
                                launchSingleTop = true
                            }
                        }
                    }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                    }
                },
                actions = {
                    IconButton(onClick = {
                        navController.navigate(NavRoutes.HOME) {
                            popUpTo(NavRoutes.HOME) { inclusive = false }
                            launchSingleTop = true
                        }
                    }) {
                        Icon(Icons.Filled.Home, contentDescription = strings["nav_home"] ?: "Home")
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = MaterialTheme.colorScheme.background)
            )
        },
        bottomBar = {
            AppBottomNav(navController = navController, strings = strings)
        },
        containerColor = MaterialTheme.colorScheme.background
    ) { padding ->
        val p = panchang
        if (p == null && panchangLoadFailed) {
            Box(Modifier.fillMaxSize().padding(padding), contentAlignment = Alignment.Center) {
                Column(horizontalAlignment = Alignment.CenterHorizontally) {
                    Text(strings["error_loading"] ?: "Could not load panchang")
                    Spacer(Modifier.height(12.dp))
                    Button(onClick = { retryTrigger++ }) {
                        Text(strings["retry"] ?: "Retry")
                    }
                }
            }
        } else if (p == null) {
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
                // 1b. Eclipse alert (distinct from festivals)
                if (p.hasEclipse) {
                    item { EclipseAlertCard(panchang = p, strings = strings) }
                }
                // 2. Vedic Calendar Section
                item { VedicCalendarSection(panchang = p, strings = strings, localizer = localizer) }
                // 5. Auspicious Periods
                item { AuspiciousMuhurtasCard(panchang = p, strings = strings, localizer = localizer) }
                // 6. Inauspicious Periods
                item { InauspiciousPeriodsCard(panchang = p, strings = strings, localizer = localizer) }
                // 6b. Dishasool
                item { DishasoolCard(panchang = p, strings = strings) }
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardBorderColor = if (isDark) AppColors.Primary.copy(alpha = 0.45f) else Color(0xFFCBA35C).copy(alpha = 0.55f)
    val cardShape = RoundedCornerShape(20.dp)
    val monthNum = panchang.date.month.ordinal + 1
    val vedicMonth = localizer.vedicMonthName(panchang)
    val vikramYear = localizer.numerals(
        localizer.vikramSamvatYear(panchang.date.year, monthNum).toString()
    )
    val shakaYear = localizer.numerals(
        localizer.shakaSamvatYear(panchang.date.year, monthNum, panchang.date.day).toString()
    )
    val vedicDateLine = localizer.vedicDateLine(panchang)

    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val labelColor = MaterialTheme.colorScheme.onSurfaceVariant
    val valueColor = MaterialTheme.colorScheme.onSurface

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(cardShape)
            .border(1.dp, cardBorderColor, cardShape)
            .background(MaterialTheme.colorScheme.surface)
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardBorderColor = if (isDark) AppColors.Primary.copy(alpha = 0.45f) else Color(0xFFCBA35C).copy(alpha = 0.55f)
    val cardShape = RoundedCornerShape(20.dp)
    val vijaya = panchang.auspiciousMuhurtas.find { it.id == "vijaya_muhurta" }?.range
    val godhuli = panchang.auspiciousMuhurtas.find { it.id == "godhuli_muhurta" }?.range
    val pradosh = panchang.auspiciousMuhurtas.find { it.id == "pradosh_kaal" }?.range
    val now = remember { Clock.System.now() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(cardShape)
            .border(1.dp, cardBorderColor, cardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = strings["auspicious_periods"] ?: "Auspicious Periods",
            style = AppTextStyles.saffronLabel.copy(fontSize = 14.sp)
        )
        Spacer(Modifier.height(16.dp))
        MuhurtaRow(icon = "🙏", name = strings["brahma_muhurta"] ?: "Brahma Muhurta",   desc = strings["best_meditation"] ?: "Best for meditation & prayer",        range = panchang.brahmaMuhurta,  barColor = AppColors.Auspicious, timeColor = AppColors.Auspicious, localizer = localizer, isDark = isDark, isCurrent = now in panchang.brahmaMuhurta.start..panchang.brahmaMuhurta.end)
        Spacer(Modifier.height(14.dp))
        MuhurtaRow(icon = "⚡", name = strings["abhijit_muhurta"] ?: "Abhijit Muhurta", desc = strings["best_beginnings"] ?: "Best for new beginnings",             range = panchang.abhijitMuhurta, barColor = AppColors.Auspicious, timeColor = AppColors.Auspicious, localizer = localizer, isDark = isDark, isCurrent = now in panchang.abhijitMuhurta.start..panchang.abhijitMuhurta.end)
        if (vijaya != null) {
            Spacer(Modifier.height(14.dp))
            MuhurtaRow(icon = "🏆", name = strings["vijaya_muhurta"] ?: "Vijaya Muhurta",   desc = strings["vijaya_note"] ?: "Best for success and winning efforts", range = vijaya,                  barColor = AppColors.Auspicious, timeColor = AppColors.Auspicious, localizer = localizer, isDark = isDark, isCurrent = now in vijaya.start..vijaya.end)
        }
        if (godhuli != null) {
            Spacer(Modifier.height(14.dp))
            MuhurtaRow(icon = "🐄", name = strings["godhuli_muhurta"] ?: "Godhuli Muhurta",  desc = strings["godhuli_note"] ?: "Auspicious twilight window",         range = godhuli,                 barColor = AppColors.Auspicious, timeColor = AppColors.Auspicious, localizer = localizer, isDark = isDark, isCurrent = now in godhuli.start..godhuli.end)
        }
        if (pradosh != null) {
            Spacer(Modifier.height(14.dp))
            MuhurtaRow(icon = "🕉️", name = strings["pradosh_kaal"] ?: "Pradosh Kaal",    desc = strings["pradosh_note"] ?: "Ideal for Shiva Puja",              range = pradosh,                 barColor = AppColors.Auspicious, timeColor = AppColors.Auspicious, localizer = localizer, isDark = isDark, isCurrent = now in pradosh.start..pradosh.end)
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardBorderColor = if (isDark) AppColors.Primary.copy(alpha = 0.45f) else Color(0xFFCBA35C).copy(alpha = 0.55f)
    val cardShape = RoundedCornerShape(20.dp)
    val now = remember { Clock.System.now() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(cardShape)
            .border(1.dp, cardBorderColor, cardShape)
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Text(
            text = strings["inauspicious_periods"] ?: "Inauspicious Periods",
            style = AppTextStyles.saffronLabel.copy(fontSize = 14.sp)
        )
        Spacer(Modifier.height(16.dp))
        MuhurtaRow(icon = "⚠️", name = strings["rahu_kaal"] ?: "Rahu Kaal",     desc = strings["avoid_auspicious"] ?: "Avoid auspicious activities", range = panchang.rahuKaal,   barColor = AppColors.Inauspicious, timeColor = AppColors.Inauspicious, localizer = localizer, isDark = isDark, isCurrent = now in panchang.rahuKaal.start..panchang.rahuKaal.end)
        Spacer(Modifier.height(14.dp))
        MuhurtaRow(icon = "💀", name = strings["yamaganda"] ?: "Yamaganda",     desc = strings["period_yama"] ?: "Period of Yama — inauspicious",    range = panchang.yamaganda,  barColor = AppColors.Inauspicious, timeColor = AppColors.Inauspicious, localizer = localizer, isDark = isDark, isCurrent = now in panchang.yamaganda.start..panchang.yamaganda.end)
        Spacer(Modifier.height(14.dp))
        MuhurtaRow(icon = "🪐", name = strings["gulika_kaal"] ?: "Gulika Kaal", desc = strings["saturn_period"] ?: "Saturn's inauspicious period",   range = panchang.gulikaKaal, barColor = AppColors.Inauspicious, timeColor = AppColors.Inauspicious, localizer = localizer, isDark = isDark, isCurrent = now in panchang.gulikaKaal.start..panchang.gulikaKaal.end)
        
        // Add Durmuhurta and Varjyam
        panchang.durmuhurtas.forEach { range ->
            Spacer(Modifier.height(14.dp))
            MuhurtaRow(icon = "🚫", name = strings["durmuhurta"] ?: "Durmuhurta", desc = strings["durmuhurta_note"] ?: "Inauspicious segments", range = range, barColor = AppColors.Inauspicious, timeColor = AppColors.Inauspicious, localizer = localizer, isDark = isDark, isCurrent = now in range.start..range.end)
        }
        panchang.varjyams.forEach { range ->
            Spacer(Modifier.height(14.dp))
            MuhurtaRow(icon = "🛑", name = strings["varjyam"] ?: "Varjyam", desc = strings["varjyam_note"] ?: "Inauspicious Nakshatra window", range = range, barColor = AppColors.Inauspicious, timeColor = AppColors.Inauspicious, localizer = localizer, isDark = isDark, isCurrent = now in range.start..range.end)
        }
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
    isDark: Boolean,
    isCurrent: Boolean = false
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
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant
    val highlightShape = RoundedCornerShape(8.dp)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .then(
                if (isCurrent) Modifier
                    .clip(highlightShape)
                    .background(Color(0xFFFF9800).copy(alpha = 0.12f))
                    .border(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f), highlightShape)
                else Modifier
            )
            .padding(
                horizontal = if (isCurrent) 8.dp else 0.dp,
                vertical = if (isCurrent) 6.dp else 0.dp
            ),
        verticalAlignment = Alignment.CenterVertically
    ) {
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
        Column(horizontalAlignment = Alignment.End) {
            Text(
                timeStr,
                style = AppTextStyles.timeSmall.copy(
                    color = timeColor, fontWeight = FontWeight.Bold, fontSize = 12.sp
                )
            )
            val sSec = range.start.toLocalDateTime(tz).second
            val eSec = range.end.toLocalDateTime(tz).second
            Text(
                localizer.numerals("%02ds – %02ds".format(sSec, eSec)),
                style = AppTextStyles.labelSmall.copy(color = timeColor.copy(alpha = 0.5f), fontSize = 9.sp)
            )
        }
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardBorderColor = if (isDark) AppColors.Primary.copy(alpha = 0.45f) else Color(0xFFCBA35C).copy(alpha = 0.55f)
    val cardShape = RoundedCornerShape(20.dp)
    var expanded by remember { mutableStateOf(false) }
    val muhurtas = panchang.daytimeMuhurtas
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val now = remember { Clock.System.now() }

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(cardShape)
            .border(1.dp, cardBorderColor, cardShape)
            .background(MaterialTheme.colorScheme.surface)
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
                                isCurrent = now in muhurta.range.start..muhurta.range.end,
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
    isCurrent: Boolean = false,
    modifier: Modifier = Modifier
) {
    val tz = TimeZone.currentSystemDefault()
    val s = range.start.toLocalDateTime(tz)
    val e = range.end.toLocalDateTime(tz)
    val timeStr = localizer.numerals(
        "%02d:%02d – %02d:%02d".format(s.hour, s.minute, e.hour, e.minute)
    )
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val cellShape = RoundedCornerShape(12.dp)

    Box(
        modifier = modifier
            .clip(cellShape)
            .background(
                if (isCurrent) Color(0xFFFF9800).copy(alpha = 0.12f)
                else MaterialTheme.colorScheme.surfaceVariant
            )
            .then(
                if (isCurrent) Modifier.border(1.dp, Color(0xFFFF9800).copy(alpha = 0.5f), cellShape)
                else Modifier
            )
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
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardBorderColor = if (isDark) AppColors.Primary.copy(alpha = 0.45f) else Color(0xFFCBA35C).copy(alpha = 0.55f)
    val cardShape = RoundedCornerShape(20.dp)
    var expanded by remember { mutableStateOf(false) }
    val dividerColor = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
    val textPrimary = MaterialTheme.colorScheme.onSurface
    val textSecondary = MaterialTheme.colorScheme.onSurfaceVariant

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(cardShape)
            .border(1.dp, cardBorderColor, cardShape)
            .background(MaterialTheme.colorScheme.surface)
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
