package `in`.vedicpanchang.app.ui.navigation

import androidx.compose.animation.AnimatedContentTransitionScope
import androidx.compose.animation.EnterTransition
import androidx.compose.animation.ExitTransition
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.core.tween
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.IntOffset
import androidx.navigation.NavBackStackEntry
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import `in`.vedicpanchang.app.ui.animation.MotionTokens
import `in`.vedicpanchang.app.ui.calendar.CalendarScreen
import `in`.vedicpanchang.app.ui.daydetail.DayDetailScreen
import `in`.vedicpanchang.app.ui.home.HomeScreen
import `in`.vedicpanchang.app.ui.horoscope.HoroscopeScreen
import `in`.vedicpanchang.app.ui.settings.HelpScreen
import `in`.vedicpanchang.app.ui.settings.SettingsScreen
import kotlin.math.roundToInt

object NavRoutes {
    const val HOME       = "home"
    const val CALENDAR   = "calendar"
    const val DAY_DETAIL = "day/{date}"   // date = "yyyy-MM-dd"
    const val HOROSCOPE  = "horoscope"
    const val SETTINGS   = "settings"
    const val HELP       = "help"

    fun dayDetail(date: String) = "day/$date"

    val BOTTOM_NAV_ORDER = listOf(HOME, CALENDAR, HOROSCOPE, SETTINGS)
}

@Composable
fun VedicPanchangNavGraph(
    navController: NavHostController,
    modifier: Modifier = Modifier
) {
    NavHost(
        navController = navController,
        startDestination = NavRoutes.HOME,
        modifier = modifier,
        enterTransition = {
            val initialRoute = initialState.destination.route
            val targetRoute = targetState.destination.route
            when {
                isBottomNavTransition(initialRoute, targetRoute) -> {
                    val forward = isForwardBottomNav(initialRoute, targetRoute)
                    bottomNavEnter(forward)
                }
                isDetailRoute(targetRoute) -> detailEnter()
                else -> EnterTransition.None
            }
        },
        exitTransition = {
            val initialRoute = initialState.destination.route
            val targetRoute = targetState.destination.route
            when {
                isBottomNavTransition(initialRoute, targetRoute) -> {
                    val forward = isForwardBottomNav(initialRoute, targetRoute)
                    bottomNavExit(forward)
                }
                isDetailRoute(targetRoute) -> detailExitForward()
                else -> ExitTransition.None
            }
        },
        popEnterTransition = {
            val initialRoute = initialState.destination.route
            val targetRoute = targetState.destination.route
            when {
                isBottomNavTransition(initialRoute, targetRoute) -> {
                    val forward = isForwardBottomNav(initialRoute, targetRoute)
                    bottomNavEnter(forward)
                }
                isDetailRoute(initialRoute) -> detailPopEnter()
                else -> EnterTransition.None
            }
        },
        popExitTransition = {
            val initialRoute = initialState.destination.route
            val targetRoute = targetState.destination.route
            when {
                isBottomNavTransition(initialRoute, targetRoute) -> {
                    val forward = isForwardBottomNav(initialRoute, targetRoute)
                    bottomNavExit(forward)
                }
                isDetailRoute(initialRoute) -> detailPopExit()
                else -> ExitTransition.None
            }
        }
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

private fun isDetailRoute(route: String?): Boolean =
    route == NavRoutes.DAY_DETAIL || route == NavRoutes.HELP

private fun isBottomNavTransition(initialRoute: String?, targetRoute: String?): Boolean =
    bottomNavIndex(initialRoute) >= 0 && bottomNavIndex(targetRoute) >= 0

private fun isForwardBottomNav(initialRoute: String?, targetRoute: String?): Boolean =
    bottomNavIndex(targetRoute) > bottomNavIndex(initialRoute)

private fun bottomNavIndex(route: String?): Int =
    NavRoutes.BOTTOM_NAV_ORDER.indexOf(route)

private fun AnimatedContentTransitionScope<NavBackStackEntry>.bottomNavEnter(
    forward: Boolean
): EnterTransition {
    val direction = if (forward) 1 else -1
    val fraction = MotionTokens.BottomNavSlideFraction
    val slideSpec = tween<IntOffset>(
        durationMillis = MotionTokens.MediumDurationMillis,
        easing = MotionTokens.StandardEasing
    )
    val fadeSpec = tween<Float>(
        durationMillis = MotionTokens.MediumDurationMillis,
        easing = MotionTokens.StandardEasing
    )
    return slideInHorizontally(
        animationSpec = slideSpec,
        initialOffsetX = { fullWidth -> (fullWidth * fraction).roundToInt() * direction }
    ) + fadeIn(
        animationSpec = fadeSpec,
        initialAlpha = MotionTokens.FadeInStart
    )
}

private fun AnimatedContentTransitionScope<NavBackStackEntry>.bottomNavExit(
    forward: Boolean
): ExitTransition {
    val direction = if (forward) -1 else 1
    val fraction = MotionTokens.BottomNavSlideFraction
    val slideSpec = tween<IntOffset>(
        durationMillis = MotionTokens.MediumDurationMillis,
        easing = MotionTokens.StandardEasing
    )
    val fadeSpec = tween<Float>(
        durationMillis = MotionTokens.MediumDurationMillis,
        easing = MotionTokens.StandardEasing
    )
    return slideOutHorizontally(
        animationSpec = slideSpec,
        targetOffsetX = { fullWidth -> (fullWidth * fraction).roundToInt() * direction }
    ) + fadeOut(
        animationSpec = fadeSpec,
        targetAlpha = MotionTokens.FadeOutEnd
    )
}

// ── Detail transitions (Calendar → DayDetail) ────────────────────────────────
//
// Pattern: new screen slides in full-width over the source (source barely moves).
// This avoids the "double fade" artifact from both screens going transparent.

private fun AnimatedContentTransitionScope<NavBackStackEntry>.detailEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(
            durationMillis = MotionTokens.DetailEnterDurationMillis,
            easing = MotionTokens.EmphasizedDecelerate
        ),
        initialOffsetX = { it }            // enter from the full right edge
    ) + fadeIn(
        animationSpec = tween(
            durationMillis = MotionTokens.DetailEnterDurationMillis / 2,
            easing = MotionTokens.EmphasizedDecelerate
        ),
        initialAlpha = MotionTokens.FadeInStart
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.detailExitForward(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(
            durationMillis = MotionTokens.DetailEnterDurationMillis,
            easing = MotionTokens.EmphasizedDecelerate
        ),
        targetOffsetX = { -(it * 0.08f).roundToInt() }   // barely nudge left, no fade
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.detailPopEnter(): EnterTransition =
    slideInHorizontally(
        animationSpec = tween(
            durationMillis = MotionTokens.DetailEnterDurationMillis,
            easing = MotionTokens.EmphasizedDecelerate
        ),
        initialOffsetX = { -(it * 0.08f).roundToInt() }  // return from slight left nudge
    )

private fun AnimatedContentTransitionScope<NavBackStackEntry>.detailPopExit(): ExitTransition =
    slideOutHorizontally(
        animationSpec = tween(
            durationMillis = MotionTokens.DetailExitDurationMillis,
            easing = MotionTokens.EmphasizedAccelerate
        ),
        targetOffsetX = { it }             // exit to the full right edge
    ) + fadeOut(
        animationSpec = tween(
            durationMillis = MotionTokens.DetailExitDurationMillis / 2,
            easing = MotionTokens.EmphasizedAccelerate
        ),
        targetAlpha = MotionTokens.FadeOutEnd
    )
