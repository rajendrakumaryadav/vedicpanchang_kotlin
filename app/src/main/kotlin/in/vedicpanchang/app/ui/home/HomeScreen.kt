package `in`.vedicpanchang.app.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.slideInVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Share
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import `in`.vedicpanchang.app.R
import `in`.vedicpanchang.app.service.ShareService
import `in`.vedicpanchang.app.ui.navigation.AppBottomNav
import `in`.vedicpanchang.app.ui.navigation.NavRoutes
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import `in`.vedicpanchang.app.viewmodel.PanchangUiState
import `in`.vedicpanchang.app.viewmodel.PanchangViewModel
import `in`.vedicpanchang.app.viewmodel.SettingsViewModel

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    navController: NavController,
    panchangVm: PanchangViewModel = hiltViewModel(),
    settingsVm: SettingsViewModel = hiltViewModel()
) {
    val state by panchangVm.state.collectAsStateWithLifecycle()
    val strings by settingsVm.strings.collectAsStateWithLifecycle()
    val locale by settingsVm.locale.collectAsStateWithLifecycle()
    val localizer by settingsVm.panchangLocalizer.collectAsStateWithLifecycle()
    val isDark = isSystemInDarkTheme()
    val context = LocalContext.current

    val currentRoute by remember(navController) {
        derivedStateOf { navController.currentDestination?.route ?: NavRoutes.HOME }
    }

    Scaffold(
        bottomBar = {
            AppBottomNav(currentRoute = NavRoutes.HOME, navController = navController)
        },
        containerColor = AppColors.Background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    Brush.verticalGradient(
                        listOf(AppColors.NightSkyEnd, AppColors.NightSkyStart)
                    )
                )
        ) {
            LazyColumn(
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 16.dp
                )
            ) {
                // Header: Location and App Title
                item {
                    val panchang = (state.todayPanchang as? PanchangUiState.Success)?.panchang
                    HomeHeader(
                        locationName = panchang?.locationName ?: "...",
                        onShare = {
                            panchang?.let {
                                val text = ShareService.formatPanchang(it, localizer, locale)
                                context.startActivity(
                                    Intent.createChooser(
                                        Intent(Intent.ACTION_SEND).apply {
                                            type = "text/plain"
                                            putExtra(Intent.EXTRA_TEXT, text)
                                        }, null
                                    ).addFlags(Intent.FLAG_ACTIVITY_NEW_TASK)
                                )
                            }
                        },
                        onSettings = { navController.navigate(NavRoutes.SETTINGS) },
                        strings = strings
                    )
                }

                // Date and Calendar Button
                item {
                    HomeDateHeader(
                        strings = strings,
                        locale = locale,
                        onCalendarTap = { navController.navigate(NavRoutes.CALENDAR) }
                    )
                }

                // Today panchang card
                item {
                val todayState = state.todayPanchang
                AnimatedVisibility(
                    visible = todayState is PanchangUiState.Success,
                    enter = fadeIn() + slideInVertically { it / 4 }
                ) {
                    if (todayState is PanchangUiState.Success) {
                        TodayPanchangCard(
                            panchang = todayState.panchang,
                            livePanchang = (state.livePanchang as? PanchangUiState.Success)?.panchang
                                ?: todayState.panchang,
                            strings = strings,
                            localizer = localizer,
                            locale = locale
                        )
                    }
                }
            }

            // Sun & Moon card
            item {
                val todayState = state.todayPanchang
                if (todayState is PanchangUiState.Success) {
                    SunMoonCard(
                        panchang = todayState.panchang,
                        liveNow = state.liveNow,
                        strings = strings,
                        locale = locale
                    )
                }
            }

            // Upcoming Events card
            item {
                UpcomingEventsCard(
                    panchangVm = panchangVm,
                    strings = strings,
                    localizer = localizer,
                    navController = navController
                )
            }

            // Vedic Calendar card
            item {
                val todayState = state.todayPanchang
                if (todayState is PanchangUiState.Success) {
                    VedicCalendarCard(
                        panchang = todayState.panchang,
                        strings = strings,
                        localizer = localizer
                    )
                }
            }

            // Choghadiya card
            item {
                val todayState = state.todayPanchang
                if (todayState is PanchangUiState.Success) {
                    ChoghadiyaCard(
                        panchang = todayState.panchang,
                        strings = strings,
                        locale = locale,
                        localizer = localizer
                    )
                }
            }

            // Hora card
            item {
                val todayState = state.todayPanchang
                if (todayState is PanchangUiState.Success) {
                    HoraCard(
                        panchang = todayState.panchang,
                        liveNow = state.liveNow,
                        strings = strings,
                        localizer = localizer
                    )
                }
            }

            item { Spacer(Modifier.height(32.dp)) }
        }
    }
}
}

@Composable
private fun isSystemInDarkTheme() = androidx.compose.foundation.isSystemInDarkTheme()

@Composable
fun HomeHeader(
    locationName: String,
    onShare: () -> Unit,
    onSettings: () -> Unit,
    strings: Map<String, String>
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(top = 16.dp, start = 16.dp, end = 8.dp, bottom = 8.dp),
        verticalAlignment = Alignment.Top
    ) {
        Column(modifier = Modifier.weight(1f)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.LocationOn,
                    contentDescription = null,
                    tint = Color(0xFFE53935),
                    modifier = Modifier.size(14.dp)
                )
                Spacer(Modifier.width(4.dp))
                Text(
                    text = locationName,
                    style = AppTextStyles.bodySmall.copy(color = AppColors.TextSecondary, fontSize = 11.sp)
                )
            }
            Spacer(Modifier.height(4.dp))
            Text(
                text = (strings["app_title"] ?: "VEDIC PANCHANG").uppercase(),
                style = AppTextStyles.displaySmall.copy(fontSize = 18.sp, color = Color.White)
            )
        }
        Row {
            IconButton(onClick = onShare) {
                Icon(Icons.Outlined.Share, contentDescription = null, tint = Color.White)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = null, tint = Color.White)
            }
        }
    }
}

@Composable
fun HomeDateHeader(
    strings: Map<String, String>,
    locale: String,
    onCalendarTap: () -> Unit
) {
    val javaLocale = if (locale == "sa" || locale == "hi") Locale("hi", "IN") else Locale.ENGLISH
    val now = Date()
    val weekdayFmt = SimpleDateFormat("EEEE", javaLocale)
    val dateFmt = SimpleDateFormat("d MMMM yyyy", javaLocale)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 12.dp),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.Bottom
    ) {
        Column {
            Text(
                text = weekdayFmt.format(now),
                style = AppTextStyles.bodySmall.copy(color = AppColors.TextSecondary)
            )
            Text(
                text = dateFmt.format(now).uppercase(),
                style = AppTextStyles.displayMedium.copy(fontSize = 24.sp, color = Color.White)
            )
        }
        TextButton(
            onClick = onCalendarTap,
            contentPadding = PaddingValues(horizontal = 8.dp, vertical = 4.dp)
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(
                    imageVector = Icons.Default.CalendarMonth,
                    contentDescription = null,
                    tint = AppColors.Primary,
                    modifier = Modifier.size(20.dp)
                )
                Spacer(Modifier.width(6.dp))
                Text(
                    text = (strings["nav_calendar"] ?: "Calendar").uppercase(),
                    style = AppTextStyles.labelLarge.copy(color = AppColors.Primary)
                )
            }
        }
    }
}

@Composable
fun PanchangLoadingCard() {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .height(120.dp)
            .background(
                MaterialTheme.colorScheme.surface,
                androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            ),
        contentAlignment = Alignment.Center
    ) {
        CircularProgressIndicator(color = AppColors.Primary)
    }
}

@Composable
fun PanchangErrorCard(strings: Map<String, String>) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
            .background(
                AppColors.Inauspicious.copy(alpha = 0.10f),
                androidx.compose.foundation.shape.RoundedCornerShape(16.dp)
            )
            .padding(16.dp)
    ) {
        Text(
            strings["loading_error"] ?: "Unable to load Panchang",
            style = AppTextStyles.bodyMedium
        )
    }
}
