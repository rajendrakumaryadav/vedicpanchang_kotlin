package `in`.vedicpanchang.app.ui.animation

import androidx.compose.animation.core.CubicBezierEasing
import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Easing

object MotionTokens {
    const val ShortDurationMillis  = 200
    const val MediumDurationMillis = 300

    // M3 emphasized easing curves for navigation transitions
    val EmphasizedDecelerate: Easing = CubicBezierEasing(0.05f, 0.7f, 0.1f, 1.0f)
    val EmphasizedAccelerate: Easing = CubicBezierEasing(0.3f, 0.0f, 0.8f, 0.15f)
    val StandardEasing: Easing = FastOutSlowInEasing

    // Detail enter/exit durations (M3 nav recommendation)
    const val DetailEnterDurationMillis = 400
    const val DetailExitDurationMillis  = 250

    // Detail transitions fade in from partial opacity — avoids the harsh "pop" of starting at 0
    const val FadeInStart = 0.3f
    const val FadeOutEnd  = 0f

    const val BottomNavFadeInAlpha = 0.5f
}
