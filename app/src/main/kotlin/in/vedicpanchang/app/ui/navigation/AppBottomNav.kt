package `in`.vedicpanchang.app.ui.navigation

import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoGraph
import androidx.compose.material.icons.filled.CalendarMonth
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.AutoGraph
import androidx.compose.material.icons.outlined.CalendarMonth
import androidx.compose.material.icons.outlined.Home
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.res.stringResource
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.NavController
import `in`.vedicpanchang.app.R
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import `in`.vedicpanchang.app.viewmodel.SettingsViewModel

private data class NavItem(
    val route: String,
    val labelRes: Int,
    val icon: ImageVector,
    val activeIcon: ImageVector
)

private val NAV_ITEMS = listOf(
    NavItem(NavRoutes.HOME,       R.string.nav_home,      Icons.Outlined.Home,         Icons.Filled.Home),
    NavItem(NavRoutes.CALENDAR,   R.string.nav_calendar,  Icons.Outlined.CalendarMonth, Icons.Filled.CalendarMonth),
    NavItem(NavRoutes.HOROSCOPE,  R.string.nav_horoscope, Icons.Outlined.AutoGraph,    Icons.Filled.AutoGraph),
    NavItem(NavRoutes.SETTINGS,   R.string.nav_settings,  Icons.Outlined.Settings,     Icons.Filled.Settings),
)

@Composable
fun AppBottomNav(
    currentRoute: String,
    navController: NavController
) {
    NavigationBar(
        containerColor = androidx.compose.material3.MaterialTheme.colorScheme.surface,
        windowInsets = WindowInsets(0)
    ) {
        NAV_ITEMS.forEach { item ->
            val selected = currentRoute == item.route
            NavigationBarItem(
                selected = selected,
                onClick = {
                    if (!selected) {
                        navController.navigate(item.route) {
                            popUpTo(NavRoutes.HOME) { saveState = true }
                            launchSingleTop = true
                            restoreState = true
                        }
                    }
                },
                icon = {
                    androidx.compose.material3.Icon(
                        imageVector = if (selected) item.activeIcon else item.icon,
                        contentDescription = null
                    )
                },
                label = { Text(stringResource(item.labelRes), style = AppTextStyles.labelSmall) },
                colors = NavigationBarItemDefaults.colors(
                    selectedIconColor = AppColors.Primary,
                    selectedTextColor = AppColors.Primary,
                    indicatorColor = AppColors.Primary.copy(alpha = 0.12f)
                )
            )
        }
    }
}
