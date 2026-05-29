package `in`.vedicpanchang.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import `in`.vedicpanchang.app.data.model.HelpEntry
import `in`.vedicpanchang.app.data.model.HelpSection
import `in`.vedicpanchang.app.l10n.PanchangLocalizer
import `in`.vedicpanchang.app.service.HelpContentService
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import `in`.vedicpanchang.app.viewmodel.LocationUiState
import `in`.vedicpanchang.app.viewmodel.PanchangUiState
import `in`.vedicpanchang.app.viewmodel.PanchangViewModel
import `in`.vedicpanchang.app.viewmodel.SettingsViewModel
import `in`.vedicpanchang.astronomy.AstronomyService
import kotlin.time.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HelpScreen(
    navController: NavController,
    settingsVm: SettingsViewModel = hiltViewModel(),
    panchangVm: PanchangViewModel = hiltViewModel()
) {
    val strings by settingsVm.strings.collectAsStateWithLifecycle()
    val panchangState by panchangVm.state.collectAsStateWithLifecycle()
    val locale by settingsVm.locale.collectAsStateWithLifecycle()
    val localizer by settingsVm.panchangLocalizer.collectAsStateWithLifecycle()
    val sections = remember(locale) { HelpContentService.sectionsForLocale(locale) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings["help"] ?: "Help") },
                navigationIcon = {
                    IconButton(onClick = { navController.popBackStack() }) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                    }
                }
            )
        }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.fillMaxSize().padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(12.dp)
        ) {
            item {
                LiveAstronomyCard(
                    strings = strings,
                    localizer = localizer,
                    locale = locale,
                    liveNow = panchangState.liveNow,
                    locationState = panchangState.location,
                    todayPanchang = panchangState.todayPanchang
                )
            }

            items(sections) { section ->
                HelpSectionCard(
                    section = section,
                    meaningLabel = strings["help_meaning"] ?: "Meaning",
                    calculationLabel = strings["help_calculation"] ?: "Calculation"
                )
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }
}

@Composable
private fun LiveAstronomyCard(
    strings: Map<String, String>,
    localizer: PanchangLocalizer,
    locale: String,
    liveNow: Instant?,
    locationState: LocationUiState,
    todayPanchang: PanchangUiState
) {
    val effectiveInstant = liveNow ?: Instant.fromEpochMilliseconds(System.currentTimeMillis())
    val javaLocale = if (locale == "hi" || locale == "sa") Locale("hi", "IN") else Locale.ENGLISH
    val asOf = SimpleDateFormat("yyyy-MM-dd HH:mm", javaLocale)
        .format(Date(effectiveInstant.toEpochMilliseconds()))
    // Compute JD on main thread only (pure math, no shared cache).
    // Use sun/moon longitudes from the already-computed panchang to avoid
    // concurrent access to AstronomyService's non-thread-safe caches.
    val jd = remember(effectiveInstant) { AstronomyService.julianDayFromInstant(effectiveInstant) }
    val panchang = (todayPanchang as? PanchangUiState.Success)?.panchang
    val sunLon = panchang?.sunLongitude
    val moonLon = panchang?.moonLongitude
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f

    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        // Header
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(Brush.horizontalGradient(listOf(AppColors.SaffronGradientStart, AppColors.SaffronGradientEnd)))
                .padding(horizontal = 16.dp, vertical = 12.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Box(
                    modifier = Modifier
                        .clip(RoundedCornerShape(6.dp))
                        .background(Color.White.copy(alpha = 0.2f))
                        .padding(horizontal = 7.dp, vertical = 3.dp)
                ) {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Box(
                            modifier = Modifier.size(6.dp)
                                .clip(CircleShape)
                                .background(Color.White)
                        )
                        Spacer(Modifier.width(4.dp))
                        Text("LIVE", style = AppTextStyles.labelSmall.copy(color = Color.White, fontSize = 9.sp))
                    }
                }
                Spacer(Modifier.width(10.dp))
                Text(
                    strings["help_live_metrics"] ?: "Current astronomical values",
                    style = AppTextStyles.bodySmall.copy(color = Color.White, fontWeight = FontWeight.Bold)
                )
            }
        }

        // Metrics
        when (locationState) {
            is LocationUiState.Success -> {
                Column(Modifier.padding(16.dp)) {
                    Text(
                        "${strings["help_as_of"] ?: "As of"}: ${localizer.numerals(asOf)}",
                        style = AppTextStyles.bodySmall.copy(
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    )
                    Spacer(Modifier.height(10.dp))
                    MetricRow("🗓️ ${strings["julian_day"] ?: "Julian Day"}", localizer.numerals("%.6f".format(jd)))
                    MetricRow("☀️ ${strings["sun_longitude"] ?: "Sun Longitude"}", if (sunLon != null) "${localizer.numerals("%.4f".format(sunLon))}°" else "—")
                    MetricRow("🌙 ${strings["moon_longitude"] ?: "Moon Longitude"}", if (moonLon != null) "${localizer.numerals("%.4f".format(moonLon))}°" else "—")
                    MetricRow("📍 ${strings["latitude"] ?: "Latitude"}", localizer.numerals("%.4f".format(locationState.location.latitude)))
                    MetricRow("📍 ${strings["longitude"] ?: "Longitude"}", localizer.numerals("%.4f".format(locationState.location.longitude)))
                    MetricRow("🏙️ ${strings["location"] ?: "Location"}", locationState.location.displayName)
                }
            }
            else -> {
                Box(
                    modifier = Modifier.fillMaxWidth().padding(24.dp),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator(color = AppColors.Primary, strokeWidth = 2.dp)
                }
            }
        }
    }
}

@Composable
private fun MetricRow(label: String, value: String) {
    Row(Modifier.fillMaxWidth().padding(vertical = 3.dp)) {
        Text(label, style = AppTextStyles.bodySmall, modifier = Modifier.weight(1f))
        Text(
            value,
            style = AppTextStyles.timeSmall.copy(fontSize = 12.sp),
            textAlign = TextAlign.End,
            modifier = Modifier.weight(1f)
        )
    }
}

@Composable
private fun HelpSectionCard(
    section: HelpSection,
    meaningLabel: String,
    calculationLabel: String
) {
    Column(
        modifier = Modifier.fillMaxWidth()
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier.size(40.dp)
                    .clip(RoundedCornerShape(10.dp))
                    .background(AppColors.Primary.copy(alpha = 0.12f)),
                contentAlignment = Alignment.Center
            ) {
                Text(section.icon, style = AppTextStyles.bodyLarge)
            }
            Spacer(Modifier.width(12.dp))
            Text(section.title, style = AppTextStyles.saffronLabel, modifier = Modifier.weight(1f))
        }
        if (section.intro.isNotEmpty()) {
            Text(
                section.intro,
                style = AppTextStyles.bodySmall.copy(
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                ),
                modifier = Modifier.padding(horizontal = 16.dp, vertical = 4.dp)
            )
        }
        if (section.entries.isNotEmpty()) {
            HorizontalDivider(
                Modifier,
                DividerDefaults.Thickness,
                color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
            )
            section.entries.forEachIndexed { idx, entry ->
                HelpEntryView(entry = entry, meaningLabel = meaningLabel, calculationLabel = calculationLabel)
                if (idx < section.entries.lastIndex) {
                    HorizontalDivider(
                        modifier = Modifier.padding(horizontal = 16.dp),
                        thickness = DividerDefaults.Thickness,
                        color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.08f)
                    )
                }
            }
        }
    }
}

@Composable
private fun HelpEntryView(entry: HelpEntry, meaningLabel: String, calculationLabel: String) {
    Column(modifier = Modifier.padding(16.dp)) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(entry.icon, style = AppTextStyles.bodyLarge)
            Spacer(Modifier.width(10.dp))
            Text(entry.parameter, style = AppTextStyles.labelLarge, modifier = Modifier.weight(1f))
        }
        Spacer(Modifier.height(10.dp))
        TaggedContent(tag = meaningLabel, content = entry.meaning, tagColor = AppColors.Auspicious)
        Spacer(Modifier.height(6.dp))
        TaggedContent(tag = calculationLabel, content = entry.calculation, tagColor = AppColors.Primary)
    }
}

@Composable
private fun TaggedContent(tag: String, content: String, tagColor: androidx.compose.ui.graphics.Color) {
    Row(verticalAlignment = Alignment.Top) {
        Box(
            modifier = Modifier
                .clip(RoundedCornerShape(5.dp))
                .background(tagColor.copy(alpha = 0.12f))
                .padding(horizontal = 6.dp, vertical = 3.dp)
        ) {
            Text(tag.uppercase(), style = AppTextStyles.labelSmall.copy(color = tagColor, letterSpacing = 0.6.sp, fontSize = 9.sp))
        }
        Spacer(Modifier.width(8.dp))
        Text(content, style = AppTextStyles.bodySmall, modifier = Modifier.weight(1f))
    }
}
