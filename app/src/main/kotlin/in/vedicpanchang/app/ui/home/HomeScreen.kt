package `in`.vedicpanchang.app.ui.home

import android.content.Intent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutVertically
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.rememberLazyListState
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
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.platform.LocalContext
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
    val context = LocalContext.current
    val colors = MaterialTheme.colorScheme
    val isDark = colors.background.luminance() < 0.5f
    val backgroundBrush = if (isDark) {
        Brush.verticalGradient(listOf(AppColors.NightSkyEnd, AppColors.NightSkyStart))
    } else {
        Brush.verticalGradient(listOf(colors.background, colors.surfaceVariant))
    }

    val listState = rememberLazyListState()
    val showStickyHeader by remember {
        derivedStateOf { listState.firstVisibleItemIndex > 0 }
    }

    // Hoist callbacks so sticky header can reuse them
    val panchang = (state.todayPanchang as? PanchangUiState.Success)?.panchang
    val onShare = {
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
        Unit
    }
    val onSettings = { navController.navigate(NavRoutes.SETTINGS) { launchSingleTop = true } }

    Scaffold(
        bottomBar = {
            AppBottomNav(navController = navController)
        },
        containerColor = colors.background
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    backgroundBrush
                )
        ) {
            LazyColumn(
                state = listState,
                modifier = Modifier.fillMaxSize(),
                contentPadding = PaddingValues(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding() + 16.dp
                )
            ) {
                // Header: Icons | Location | Title (expanded)
                item {
                    HomeHeader(
                        locationName = panchang?.locationName ?: "...",
                        onShare = onShare,
                        onSettings = onSettings,
                        strings = strings
                    )
                }

                // Date and Calendar Button
                item {
                    HomeDateHeader(
                        strings = strings,
                        locale = locale,
                        onCalendarTap = { navController.navigate(NavRoutes.CALENDAR) { launchSingleTop = true } }
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

        // Sticky collapsed header — slides in when expanded header scrolls off
        AnimatedVisibility(
            visible = showStickyHeader,
            enter = slideInVertically { -it } + fadeIn(),
            exit  = slideOutVertically { -it } + fadeOut()
        ) {
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .background(colors.background)
                    .statusBarsPadding()
                    .padding(start = 16.dp, end = 8.dp, top = 4.dp, bottom = 4.dp)
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = (strings["app_title"] ?: "VEDIC PANCHANG").uppercase(),
                        style = AppTextStyles.displaySmall.copy(fontSize = 18.sp, color = colors.onBackground),
                        modifier = Modifier.weight(1f)
                    )
                    IconButton(onClick = onShare) {
                        Icon(Icons.Outlined.Share, contentDescription = null, tint = colors.onBackground)
                    }
                    IconButton(onClick = onSettings) {
                        Icon(Icons.Outlined.Settings, contentDescription = null, tint = colors.onBackground)
                    }
                }
            }
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
    val colors = MaterialTheme.colorScheme
    val secondaryText = colors.onSurfaceVariant
    val iconTint = colors.onBackground
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(start = 16.dp, end = 8.dp, top = 8.dp, bottom = 8.dp)
    ) {
        // Row 1: Share + Settings icons — right-aligned
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            IconButton(onClick = onShare) {
                Icon(Icons.Outlined.Share, contentDescription = null, tint = iconTint)
            }
            IconButton(onClick = onSettings) {
                Icon(Icons.Outlined.Settings, contentDescription = null, tint = iconTint)
            }
        }

        // Row 2: Location — left
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
                style = AppTextStyles.bodySmall.copy(color = secondaryText, fontSize = 11.sp),
                maxLines = 1,
                overflow = TextOverflow.Ellipsis
            )
        }

        Spacer(Modifier.height(6.dp))

        // Row 3: App title — left
        Text(
            text = (strings["app_title"] ?: "VEDIC PANCHANG").uppercase(),
            style = AppTextStyles.displaySmall.copy(fontSize = 22.sp, color = colors.onBackground)
        )

        Spacer(Modifier.height(8.dp))
    }
}

@Composable
fun HomeDateHeader(
    strings: Map<String, String>,
    locale: String,
    onCalendarTap: () -> Unit
) {
    val colors = MaterialTheme.colorScheme
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
                style = AppTextStyles.bodySmall.copy(color = colors.onSurfaceVariant)
            )
            Text(
                text = dateFmt.format(now).uppercase(),
                style = AppTextStyles.displayMedium.copy(fontSize = 24.sp, color = colors.onBackground)
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
