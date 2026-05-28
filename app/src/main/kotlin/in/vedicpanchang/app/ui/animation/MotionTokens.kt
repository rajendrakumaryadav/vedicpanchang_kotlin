package `in`.vedicpanchang.app.ui.animation

import androidx.compose.animation.core.FastOutSlowInEasing
import androidx.compose.animation.core.Easing

object MotionTokens {
    const val ShortDurationMillis = 200
    const val MediumDurationMillis = 260

    val StandardEasing: Easing = FastOutSlowInEasing

    const val FadeInStart = 0f
    const val FadeOutEnd = 0f

    const val BottomNavSlideFraction = 0.08f
    const val DetailSlideFraction = 0.2f
}
