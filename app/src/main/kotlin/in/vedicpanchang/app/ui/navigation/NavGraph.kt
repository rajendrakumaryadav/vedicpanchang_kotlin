package `in`.vedicpanchang.app.ui.navigation

import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import `in`.vedicpanchang.app.ui.calendar.CalendarScreen
import `in`.vedicpanchang.app.ui.daydetail.DayDetailScreen
import `in`.vedicpanchang.app.ui.home.HomeScreen
import `in`.vedicpanchang.app.ui.horoscope.HoroscopeScreen
import `in`.vedicpanchang.app.ui.settings.HelpScreen
import `in`.vedicpanchang.app.ui.settings.SettingsScreen

object NavRoutes {
    const val HOME       = "home"
    const val CALENDAR   = "calendar"
    const val DAY_DETAIL = "day/{date}"   // date = "yyyy-MM-dd"
    const val HOROSCOPE  = "horoscope"
    const val SETTINGS   = "settings"
    const val HELP       = "help"

    fun dayDetail(date: String) = "day/$date"
}

@Composable
fun VedicPanchangNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = modifier
    ) {
        composable(NavRoutes.HOME) {
            HomeScreen(navController = navController)
        }

        composable(NavRoutes.CALENDAR) {
            CalendarScreen(navController = navController)
        }

        composable(
            route = NavRoutes.DAY_DETAIL,
            arguments = listOf(navArgument("date") { type = NavType.StringType })
        ) { backStack ->
            val dateStr = backStack.arguments?.getString("date") ?: return@composable
            DayDetailScreen(dateString = dateStr, navController = navController)
        }

        composable(NavRoutes.HOROSCOPE) {
            HoroscopeScreen(navController = navController)
        }

        composable(NavRoutes.SETTINGS) {
            SettingsScreen(navController = navController)
        }

        composable(NavRoutes.HELP) {
            HelpScreen(navController = navController)
        }
    }
}
