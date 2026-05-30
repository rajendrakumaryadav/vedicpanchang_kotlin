package `in`.vedicpanchang.app.ui.settings

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.outlined.Help
import androidx.compose.material.icons.outlined.ChevronRight
import androidx.compose.material3.BottomSheetDefaults
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.RadioButton
import androidx.compose.material3.RadioButtonDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SegmentedButton
import androidx.compose.material3.SegmentedButtonDefaults
import androidx.compose.material3.SingleChoiceSegmentedButtonRow
import androidx.compose.material3.Switch
import androidx.compose.material3.SwitchDefaults
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.rememberModalBottomSheetState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import `in`.vedicpanchang.app.l10n.AppStrings
import `in`.vedicpanchang.app.ui.navigation.AppBottomNav
import `in`.vedicpanchang.app.ui.navigation.NavRoutes
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import `in`.vedicpanchang.app.viewmodel.SettingsViewModel

private val defaultStrings = AppStrings.of(AppStrings.DEFAULT_LOCALE)


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    navController: NavController,
    settingsVm: SettingsViewModel = hiltViewModel()
) {
    val strings by settingsVm.strings.collectAsStateWithLifecycle()
    val locale by settingsVm.locale.collectAsStateWithLifecycle()
    val themeMode by settingsVm.themeMode.collectAsStateWithLifecycle()
    val notifSettings by settingsVm.notificationSettings.collectAsStateWithLifecycle()
    var showLangSheet by remember { mutableStateOf(false) }
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings["settings"] ?: defaultStrings["settings"]!!) },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, null)
                        }
                    }
                }
            )
        },
        bottomBar = { AppBottomNav(navController = navController, strings = strings) }
    ) { padding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(20.dp)
        ) {
            // Theme
            item {
                SectionHeader(strings["theme"] ?: defaultStrings["theme"]!!)
                Spacer(Modifier.height(8.dp))
                SettingsCard {
                    Center {
                        SingleChoiceSegmentedButtonRow {
                            listOf(
                                "system" to (strings["theme_system"]
                                    ?: defaultStrings["theme_system"]!!),
                                "light" to (strings["theme_light"]
                                    ?: defaultStrings["theme_light"]!!),
                                "dark" to (strings["theme_dark"] ?: defaultStrings["theme_dark"]!!)
                            ).forEachIndexed { idx, (key, label) ->
                                SegmentedButton(
                                    selected = themeMode == key,
                                    onClick = { settingsVm.setThemeMode(key) },
                                    shape = SegmentedButtonDefaults.itemShape(idx, 3),
                                    colors = SegmentedButtonDefaults.colors(
                                        activeContainerColor = AppColors.Primary.copy(
                                            alpha = 0.2f
                                        ), activeContentColor = AppColors.Primary
                                    )
                                ) { Text(label, style = AppTextStyles.labelSmall) }
                            }
                        }
                    }
                }
            }

            // Language
            item {
                SectionHeader(strings["language"] ?: defaultStrings["language"]!!)
                Spacer(Modifier.height(8.dp))
                SettingsCard {
                    ListTile(
                        leading = { Text("🌐", style = AppTextStyles.bodyLarge) },
                        title = strings["select_language"] ?: defaultStrings["select_language"]!!,
                        subtitle = AppStrings.LOCALE_DISPLAY_NAMES[locale]
                            ?: AppStrings.LOCALE_DISPLAY_NAMES[AppStrings.DEFAULT_LOCALE]!!,
                        trailing = {
                            Icon(
                                Icons.Outlined.ChevronRight,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = { showLangSheet = true }
                    )
                }
            }

            // Notifications
            item {
                SectionHeader(strings["notifications"] ?: defaultStrings["notifications"]!!)
                Spacer(Modifier.height(8.dp))
                SettingsCard {
                    SettingSwitch(
                        label = strings["ekadashi"] ?: defaultStrings["ekadashi"]!!,
                        subtitle = strings["notify_ekadashi"]
                            ?: defaultStrings["notify_ekadashi"]!!,
                        checked = notifSettings.ekadashi,
                        onCheckedChange = { settingsVm.toggleEkadashi(it) })
                    SettingSwitch(
                        label = strings["purnima_label"] ?: defaultStrings["purnima_label"]!!,
                        subtitle = strings["notify_purnima"] ?: defaultStrings["notify_purnima"]!!,
                        checked = notifSettings.purnima,
                        onCheckedChange = { settingsVm.togglePurnima(it) })
                    SettingSwitch(
                        label = strings["amavasya_label"] ?: defaultStrings["amavasya_label"]!!,
                        subtitle = strings["notify_amavasya"]
                            ?: defaultStrings["notify_amavasya"]!!,
                        checked = notifSettings.amavasya,
                        onCheckedChange = { settingsVm.toggleAmavasya(it) })
                    SettingSwitch(
                        label = strings["major_festivals"] ?: defaultStrings["major_festivals"]!!,
                        subtitle = strings["notify_festivals"]
                            ?: defaultStrings["notify_festivals"]!!,
                        checked = notifSettings.festivals,
                        onCheckedChange = { settingsVm.toggleFestivals(it) })
                }
            }

            // Reminder timing
            item {
                SectionHeader(strings["reminder_timing"] ?: defaultStrings["reminder_timing"]!!)
                Spacer(Modifier.height(8.dp))
                SettingsCard {
                    SettingSwitch(
                        label = strings["3_days_before"] ?: defaultStrings["3_days_before"]!!,
                        subtitle = strings["remind_3_days"] ?: defaultStrings["remind_3_days"]!!,
                        checked = notifSettings.days3,
                        onCheckedChange = { settingsVm.toggle3Days(it) })
                    SettingSwitch(
                        label = strings["1_day_before"] ?: defaultStrings["1_day_before"]!!,
                        subtitle = strings["remind_1_day"] ?: defaultStrings["remind_1_day"]!!,
                        checked = notifSettings.day1,
                        onCheckedChange = { settingsVm.toggle1Day(it) })
                    SettingSwitch(
                        label = strings["on_the_day"] ?: defaultStrings["on_the_day"]!!,
                        subtitle = strings["remind_same_day"]
                            ?: defaultStrings["remind_same_day"]!!,
                        checked = notifSettings.sameDay,
                        onCheckedChange = { settingsVm.toggleSameDay(it) })
                }
            }

            // Help
            item {
                SettingsCard {
                    ListTile(
                        leading = {
                            Icon(
                                Icons.AutoMirrored.Outlined.Help,
                                null,
                                tint = AppColors.Primary
                            )
                        },
                        title = strings["help"] ?: defaultStrings["help"]!!,
                        subtitle = strings["help_subtitle"] ?: defaultStrings["help_subtitle"]!!,
                        trailing = {
                            Icon(
                                Icons.Outlined.ChevronRight,
                                null,
                                tint = MaterialTheme.colorScheme.onSurfaceVariant
                            )
                        },
                        onClick = { navController.navigate(NavRoutes.HELP) }
                    )
                }
            }

            // App info
            item {
                SettingsCard {
                    ListTile(
                        leading = { Text("ℹ️", style = AppTextStyles.bodyLarge) },
                        title = "${strings["version"] ?: defaultStrings["version"]!!} ${settingsVm.appVersion}",
                        subtitle = strings["calc_description"]
                            ?: defaultStrings["calc_description"]!!,
                        trailing = null,
                        onClick = {}
                    )
                }
            }

            item { Spacer(Modifier.height(16.dp)) }
        }
    }

    if (showLangSheet) {
        LanguageBottomSheet(
            currentLocale = locale,
            onLocaleSelected = {
                settingsVm.setLocale(it)
                showLangSheet = false
            },
            onDismiss = { showLangSheet = false },
            strings = strings
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LanguageBottomSheet(
    currentLocale: String,
    onLocaleSelected: (String) -> Unit,
    onDismiss: () -> Unit,
    strings: Map<String, String>
) {
    val sheetState = rememberModalBottomSheetState()
    ModalBottomSheet(
        onDismissRequest = onDismiss,
        sheetState = sheetState,
        containerColor = MaterialTheme.colorScheme.surface,
        dragHandle = {
            BottomSheetDefaults.DragHandle(
                color = MaterialTheme.colorScheme.onSurfaceVariant.copy(
                    alpha = 0.4f
                )
            )
        }
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(bottom = 40.dp, start = 16.dp, end = 16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                strings["select_language"] ?: defaultStrings["select_language"]!!,
                style = AppTextStyles.displaySmall.copy(fontSize = 18.sp),
                modifier = Modifier.padding(bottom = 24.dp)
            )

            val languages = listOf(
                Triple("hi", "हिन्दी", "हिन्दी — Default"),
                Triple("en", "English", "English"),
                Triple("sa", "संस्कृतम्", "संस्कृतम् — Sanskrit")
            )

            languages.forEach { (code, native, english) ->
                LanguageOption(
                    nativeName = native,
                    englishName = english,
                    isSelected = currentLocale == code,
                    onClick = { onLocaleSelected(code) }
                )
                Spacer(Modifier.height(12.dp))
            }
        }
    }
}

@Composable
fun LanguageOption(
    nativeName: String,
    englishName: String,
    isSelected: Boolean,
    onClick: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) AppColors.Primary.copy(alpha = 0.08f) else MaterialTheme.colorScheme.surfaceVariant.copy(
                    alpha = 0.3f
                )
            )
            .border(
                width = 1.dp,
                color = if (isSelected) AppColors.Primary else Color.Transparent,
                shape = RoundedCornerShape(12.dp)
            )
            .clickable(onClick = onClick)
            .padding(16.dp)
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            RadioButton(
                selected = isSelected,
                onClick = null,
                colors = RadioButtonDefaults.colors(
                    selectedColor = AppColors.Primary,
                    unselectedColor = MaterialTheme.colorScheme.onSurfaceVariant
                )
            )
            Spacer(Modifier.width(12.dp))
            Column {
                Text(nativeName, style = AppTextStyles.labelLarge)
                Text(
                    englishName,
                    style = AppTextStyles.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
                )
            }
        }
    }
}

// ── Helpers ───────────────────────────────────────────────────────────────────

@Composable
fun SectionHeader(title: String) {
    Text(title, style = AppTextStyles.saffronLabel)
}

@Composable
fun SettingsCard(content: @Composable ColumnScope.() -> Unit) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardBorderColor =
        if (isDark) AppColors.Primary.copy(alpha = 0.45f) else Color(0xFFCBA35C).copy(alpha = 0.55f)
    val cardShape = RoundedCornerShape(16.dp)
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, cardBorderColor, cardShape)
            .background(MaterialTheme.colorScheme.surface)
    ) { content() }
}

@Composable
fun Center(content: @Composable () -> Unit) {
    Box(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 12.dp, horizontal = 16.dp),
        contentAlignment = Alignment.Center
    ) { content() }
}

@Composable
fun ListTile(
    leading: @Composable () -> Unit,
    title: String,
    subtitle: String,
    trailing: (@Composable () -> Unit)?,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = 16.dp, vertical = 12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        leading()
        Spacer(Modifier.width(12.dp))
        Column(Modifier.weight(1f)) {
            Text(title, style = AppTextStyles.bodyMedium)
            if (subtitle.isNotEmpty()) Text(subtitle, style = AppTextStyles.bodySmall)
        }
        trailing?.invoke()
    }
}

@Composable
fun SettingSwitch(
    label: String,
    subtitle: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Column(Modifier.weight(1f)) {
            Text(label, style = AppTextStyles.bodyMedium)
            if (subtitle.isNotEmpty()) Text(subtitle, style = AppTextStyles.bodySmall)
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange,
            colors = SwitchDefaults.colors(
                checkedThumbColor = Color.White,
                checkedTrackColor = AppColors.Primary
            )
        )
    }
}
