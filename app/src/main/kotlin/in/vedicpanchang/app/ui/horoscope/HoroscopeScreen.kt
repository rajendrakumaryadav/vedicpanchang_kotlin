package `in`.vedicpanchang.app.ui.horoscope

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.AccessTime
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.MyLocation
import androidx.compose.material.icons.filled.Search
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import `in`.vedicpanchang.app.R
import `in`.vedicpanchang.app.data.model.BirthDetails
import `in`.vedicpanchang.app.l10n.HoroscopeLocalizer
import `in`.vedicpanchang.app.ui.navigation.AppBottomNav
import `in`.vedicpanchang.app.ui.navigation.NavRoutes
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import `in`.vedicpanchang.app.viewmodel.HoroscopeUiState
import `in`.vedicpanchang.app.viewmodel.HoroscopeViewModel
import `in`.vedicpanchang.app.viewmodel.LocationSearchState
import `in`.vedicpanchang.app.viewmodel.SettingsViewModel
import kotlinx.datetime.*
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale
import kotlin.time.Instant

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HoroscopeScreen(
    navController: NavController,
    horoscopeVm: HoroscopeViewModel = hiltViewModel(),
    settingsVm: SettingsViewModel = hiltViewModel()
) {
    val state by horoscopeVm.state.collectAsStateWithLifecycle()
    val strings by settingsVm.strings.collectAsStateWithLifecycle()
    val localizer by settingsVm.horoscopeLocalizer.collectAsStateWithLifecycle()
    val locale by settingsVm.locale.collectAsStateWithLifecycle()
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val scrollBehavior = TopAppBarDefaults.exitUntilCollapsedScrollBehavior()
    val topBarColor = if (isDark) Color(0xFF1A0A2E) else Color(0xFFFDFCFB)

    Scaffold(
        topBar = {
            LargeTopAppBar(
                title = { Text(strings["nav_horoscope"] ?: stringResource(R.string.nav_horoscope), style = AppTextStyles.displaySmall.copy(fontSize = 16.sp)) },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                },
                scrollBehavior = scrollBehavior,
                colors = TopAppBarDefaults.largeTopAppBarColors(
                    containerColor = topBarColor,
                    scrolledContainerColor = topBarColor
                )
            )
        },
        bottomBar = { AppBottomNav(navController = navController, strings = strings) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier.nestedScroll(scrollBehavior.nestedScrollConnection),
            contentPadding = PaddingValues(top = padding.calculateTopPadding(), bottom = padding.calculateBottomPadding() + 16.dp)
        ) {
            // Intro banner
            item { IntroBanner(strings = strings) }

            // Birth input form
            item {
                BirthInputForm(
                    strings = strings,
                    searchState = state.locationSearch,
                    onSearch = { horoscopeVm.searchBirthPlace(it) },
                    onUseCurrentLocation = { horoscopeVm.useCurrentLocation() },
                    onCalculate = { details -> horoscopeVm.calculateChart(details) }
                )
            }

            // Chart results
            when (val chartState = state.chart) {
                is HoroscopeUiState.Loading -> item {
                    Box(Modifier.fillMaxWidth().height(120.dp), contentAlignment = Alignment.Center) {
                        CircularProgressIndicator(color = AppColors.Primary)
                    }
                }
                is HoroscopeUiState.Success -> {
                    val chart = chartState.chart
                    // Chart style toggle
                    item { ChartStyleToggle(isSouth = state.isSouthIndianStyle, strings = strings, onToggle = { horoscopeVm.toggleChartStyle() }) }
                    // Main chart
                    item {
                        if (state.isSouthIndianStyle) SouthIndianChart(chart = chart, strings = strings, localizer = localizer)
                        else NorthIndianChart(chart = chart, strings = strings, localizer = localizer)
                    }
                    // Navamsha
                    item {
                        NavamshaSection(
                            chart = chart,
                            isSouth = state.isSouthIndianStyle,
                            strings = strings,
                            localizer = localizer
                        )
                    }
                    // Chart summary
                    item { ChartSummaryCard(chart = chart, strings = strings, localizer = localizer, locale = locale) }
                    // Dasha
                    item { DashaSection(chart = chart, strings = strings, localizer = localizer) }
                    // Planet table
                    item { PlanetPositionsTable(chart = chart, strings = strings, localizer = localizer) }
                    // Disclaimer
                    item {
                        Text(
                            strings["kundali_disclaimer"] ?: "",
                            style = AppTextStyles.bodySmall.copy(color = MaterialTheme.colorScheme.onSurface.copy(alpha = 0.5f)),
                            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
                        )
                    }
                }
                is HoroscopeUiState.Error -> item {
                    Text(chartState.message, style = AppTextStyles.bodySmall.copy(color = AppColors.Inauspicious), modifier = Modifier.padding(16.dp))
                }
                else -> {}
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}

@Composable
private fun IntroBanner(strings: Map<String, String>) {
    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(12.dp))
            .background(AppColors.Primary.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Text(strings["kundali_intro"] ?: "Enter your birth details to generate a Kundali.", style = AppTextStyles.bodySmall)
    }
}

@Composable
private fun ChartStyleToggle(isSouth: Boolean, strings: Map<String, String>, onToggle: () -> Unit) {
    Row(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 4.dp),
        horizontalArrangement = Arrangement.End
    ) {
        SegmentedButton(
            checked = isSouth,
            onCheckedChange = { onToggle() },
            modifier = Modifier
        ) {
            Text(
                if (isSouth) (strings["chart_style_south"] ?: "South Indian") else (strings["chart_style_north"] ?: "North Indian"),
                style = AppTextStyles.labelSmall.copy(color = AppColors.Primary)
            )
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BirthInputForm(
    strings: Map<String, String>,
    searchState: LocationSearchState,
    onSearch: (String) -> Unit,
    onUseCurrentLocation: () -> Unit,
    onCalculate: (BirthDetails) -> Unit
) {
    var dobYear by remember { mutableIntStateOf(1990) }
    var dobMonth by remember { mutableIntStateOf(1) }
    var dobDay by remember { mutableIntStateOf(1) }
    var tobHour by remember { mutableIntStateOf(12) }
    var tobMinute by remember { mutableIntStateOf(0) }
    var locationQuery by remember { mutableStateOf("") }
    var selectedLat by remember { mutableDoubleStateOf(28.6139) }
    var selectedLon by remember { mutableDoubleStateOf(77.2090) }
    var selectedCity by remember { mutableStateOf("New Delhi") }

    var showDatePicker by remember { mutableStateOf(false) }
    var showTimePicker by remember { mutableStateOf(false) }

    val initialDateMillis = remember {
        Calendar.getInstance().apply { set(1990, 0, 1, 12, 0, 0); set(Calendar.MILLISECOND, 0) }.timeInMillis
    }
    val datePickerState = rememberDatePickerState(initialSelectedDateMillis = initialDateMillis)
    val timePickerState = rememberTimePickerState(initialHour = tobHour, initialMinute = tobMinute, is24Hour = true)

    if (searchState is LocationSearchState.Found) {
        selectedLat = searchState.location.latitude
        selectedLon = searchState.location.longitude
        selectedCity = searchState.location.displayName
        LaunchedEffect(searchState) { locationQuery = searchState.location.displayName }
    }

    // Date picker dialog
    if (showDatePicker) {
        DatePickerDialog(
            onDismissRequest = { showDatePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    datePickerState.selectedDateMillis?.let { millis ->
                        val cal = Calendar.getInstance().apply { timeInMillis = millis }
                        dobYear = cal.get(Calendar.YEAR)
                        dobMonth = cal.get(Calendar.MONTH) + 1
                        dobDay = cal.get(Calendar.DAY_OF_MONTH)
                    }
                    showDatePicker = false
                }) { Text(strings["ok"] ?: "OK") }
            },
            dismissButton = {
                TextButton(onClick = { showDatePicker = false }) { Text(strings["cancel"] ?: "Cancel") }
            }
        ) {
            DatePicker(state = datePickerState)
        }
    }

    // Time picker dialog
    if (showTimePicker) {
        AlertDialog(
            onDismissRequest = { showTimePicker = false },
            confirmButton = {
                TextButton(onClick = {
                    tobHour = timePickerState.hour
                    tobMinute = timePickerState.minute
                    showTimePicker = false
                }) { Text(strings["ok"] ?: "OK") }
            },
            dismissButton = {
                TextButton(onClick = { showTimePicker = false }) { Text(strings["cancel"] ?: "Cancel") }
            },
            text = { TimePicker(state = timePickerState) }
        )
    }

    Box(
        modifier = Modifier.fillMaxWidth().padding(16.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Text(strings["birth_details"] ?: "Birth Details", style = AppTextStyles.saffronLabel)
            Spacer(Modifier.height(12.dp))

            // Date of Birth — tappable field opens DatePickerDialog
            Text(strings["date_of_birth"] ?: "Date of Birth", style = AppTextStyles.labelSmall)
            Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                OutlinedTextField(
                    value = "%04d-%02d-%02d".format(dobYear, dobMonth, dobDay),
                    onValueChange = {},
                    label = { Text(strings["date_of_birth"] ?: "Date of Birth") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.CalendarMonth, null, tint = AppColors.Primary) }
                )
                Box(modifier = Modifier.matchParentSize().clickable { showDatePicker = true })
            }
            Spacer(Modifier.height(8.dp))

            // Time of Birth — tappable field opens TimePickerDialog
            Text(strings["time_of_birth"] ?: "Time of Birth", style = AppTextStyles.labelSmall)
            Box(modifier = Modifier.fillMaxWidth().padding(top = 4.dp)) {
                OutlinedTextField(
                    value = "%02d:%02d".format(tobHour, tobMinute),
                    onValueChange = {},
                    label = { Text(strings["time_of_birth"] ?: "Time of Birth") },
                    modifier = Modifier.fillMaxWidth(),
                    readOnly = true,
                    trailingIcon = { Icon(Icons.Default.AccessTime, null, tint = AppColors.Primary) }
                )
                Box(modifier = Modifier.matchParentSize().clickable { showTimePicker = true })
            }
            Spacer(Modifier.height(8.dp))

            // Place of birth — text field with search icon inside + GPS icon outside
            Text(strings["place_of_birth"] ?: "Place of Birth", style = AppTextStyles.labelSmall)
            Row(
                modifier = Modifier.fillMaxWidth().padding(top = 4.dp),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(4.dp)
            ) {
                OutlinedTextField(
                    value = locationQuery,
                    onValueChange = { locationQuery = it },
                    label = { Text(strings["enter_location"] ?: "Enter name of place") },
                    modifier = Modifier.weight(1f),
                    singleLine = true,
                    trailingIcon = {
                        if (searchState is LocationSearchState.Searching) {
                            CircularProgressIndicator(Modifier.size(20.dp), strokeWidth = 2.dp, color = AppColors.Primary)
                        } else {
                            IconButton(onClick = { onSearch(locationQuery) }) {
                                Icon(Icons.Default.Search, contentDescription = strings["search"] ?: "Search", tint = AppColors.Primary)
                            }
                        }
                    }
                )
                IconButton(onClick = onUseCurrentLocation) {
                    Icon(Icons.Default.MyLocation, contentDescription = strings["use_current_location"] ?: "Use current location", tint = AppColors.Primary)
                }
            }
            if (searchState is LocationSearchState.NotFound) {
                Text(strings["location_not_found"] ?: "Not found", style = AppTextStyles.bodySmall.copy(color = AppColors.Inauspicious))
            }
            Spacer(Modifier.height(8.dp))

            Button(
                onClick = {
                    val cal = Calendar.getInstance().apply { set(dobYear, dobMonth - 1, dobDay, tobHour, tobMinute, 0); set(Calendar.MILLISECOND, 0) }
                    val birthInstant = Instant.fromEpochMilliseconds(cal.timeInMillis)
                    onCalculate(BirthDetails(birthInstant = birthInstant, latitude = selectedLat, longitude = selectedLon, locationName = selectedCity))
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
            ) {
                Text(strings["calculate_chart"] ?: "Calculate Kundali Chart", style = AppTextStyles.labelLarge.copy(color = Color.White))
            }
        }
    }
}

@Composable
private fun ChartSummaryCard(
    chart: `in`.vedicpanchang.app.data.model.HoroscopeModel,
    strings: Map<String, String>,
    localizer: HoroscopeLocalizer,
    locale: String
) {
    val tz = TimeZone.currentSystemDefault()
    val javaLocale = if (locale == "hi" || locale == "sa") Locale("hi", "IN") else Locale.ENGLISH
    val birthDate = SimpleDateFormat("d MMM yyyy, HH:mm", javaLocale)
        .format(Date(chart.birthDetails.birthInstant.toEpochMilliseconds()))
    val sun = chart.planets.firstOrNull { it.name == "Sun" } ?: chart.planets.first()
    val moon = chart.planets.firstOrNull { it.name == "Moon" } ?: chart.planets.first()

    Box(
        modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 8.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(MaterialTheme.colorScheme.surface)
            .padding(16.dp)
    ) {
        Column {
            Text(strings["kundali_summary"] ?: "Kundali Summary", style = AppTextStyles.saffronLabel)
            Spacer(Modifier.height(12.dp))
            InfoRow(
                icon = "📅",
                label = strings["born"] ?: "Born",
                value = "$birthDate\n${chart.birthDetails.locationName}"
            )
            Spacer(Modifier.height(8.dp))

            Row {
                SignBox(
                    label = strings["lagna_rising"] ?: "Lagna",
                    sign = localizer.signNameFromEnglish(chart.lagnaSignName),
                    degree = chart.lagnaDegreeInSign,
                    nakshatra = localizer.nakshatraName(chart.lagnaNakshatraName),
                    color = AppColors.Primary,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                SignBox(
                    label = strings["sun_sign_rashi"] ?: "Sun Sign",
                    sign = localizer.signNameFromEnglish(sun.signName),
                    degree = sun.degreeInSign,
                    nakshatra = localizer.nakshatraName(sun.nakshatraName),
                    color = AppColors.SunColor,
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                SignBox(
                    label = strings["moon_sign_chandra"] ?: "Moon Sign",
                    sign = localizer.signNameFromEnglish(moon.signName),
                    degree = moon.degreeInSign,
                    nakshatra = localizer.nakshatraName(moon.nakshatraName),
                    color = AppColors.MoonColor,
                    modifier = Modifier.weight(1f)
                )
            }

            Spacer(Modifier.height(12.dp))
            Text(strings["chart_insights"] ?: "Chart Insights", style = AppTextStyles.saffronLabel)
            Spacer(Modifier.height(8.dp))

            chart.planets.forEach { planet ->
                PlanetNote(planet = planet, localizer = localizer)
            }
        }
    }
}

@Composable
private fun InfoRow(icon: String, label: String, value: String) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.Top) {
        Text(icon, style = AppTextStyles.bodyLarge)
        Spacer(Modifier.width(8.dp))
        Column(Modifier.weight(1f)) {
            Text(label, style = AppTextStyles.labelSmall)
            Text(value, style = AppTextStyles.bodySmall)
        }
    }
}

@Composable
private fun SignBox(
    label: String,
    sign: String,
    degree: Double,
    nakshatra: String,
    color: Color,
    modifier: Modifier
) {
    Column(
        modifier = modifier
            .clip(RoundedCornerShape(12.dp))
            .background(color.copy(alpha = 0.08f))
            .padding(10.dp)
    ) {
        Text(label, style = AppTextStyles.labelSmall.copy(fontSize = 8.sp))
        Spacer(Modifier.height(2.dp))
        Text(sign, style = AppTextStyles.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold, color = color, fontSize = 12.sp))
        Text("%.1f°".format(degree), style = AppTextStyles.timeSmall.copy(fontSize = 10.sp))
        Text(nakshatra, style = AppTextStyles.bodySmall.copy(fontSize = 9.sp))
    }
}

@Composable
private fun PlanetNote(
    planet: `in`.vedicpanchang.app.data.model.PlanetData,
    localizer: HoroscopeLocalizer
) {
    val note = localizer.planetNote(planet)
    if (note.isBlank()) return

    Row(Modifier.fillMaxWidth().padding(vertical = 4.dp), verticalAlignment = Alignment.Top) {
        Text(planet.symbol, style = AppTextStyles.bodyMedium)
        Spacer(Modifier.width(6.dp))
        Column(Modifier.weight(1f)) {
            Text(localizer.planetPositionLabel(planet), style = AppTextStyles.labelSmall.copy(fontSize = 11.sp))
            Text(note, style = AppTextStyles.bodySmall.copy(fontSize = 10.sp))
        }
    }
}

// ToggleButton helper
@Composable
private fun SegmentedButton(checked: Boolean, onCheckedChange: (Boolean) -> Unit, modifier: Modifier, content: @Composable RowScope.() -> Unit) {
    androidx.compose.material3.FilledTonalButton(
        onClick = { onCheckedChange(!checked) },
        modifier = modifier,
        colors = ButtonDefaults.filledTonalButtonColors(
            containerColor = if (checked) AppColors.Primary.copy(alpha = 0.15f) else MaterialTheme.colorScheme.surface
        ),
        content = { content() }
    )
}
