package `in`.vedicpanchang.app.ui.theme

import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.Font
import androidx.compose.ui.text.font.FontFamily
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp
import `in`.vedicpanchang.app.R

val CinzelFamily = FontFamily(
    Font(R.font.cinzel_regular,  weight = FontWeight.Normal),
    Font(R.font.cinzel_semibold, weight = FontWeight.SemiBold),
    Font(R.font.cinzel_bold,     weight = FontWeight.Bold),
)

val InterFamily = FontFamily(
    Font(R.font.inter_regular,  weight = FontWeight.Normal),
    Font(R.font.inter_medium,   weight = FontWeight.Medium),
    Font(R.font.inter_semibold, weight = FontWeight.SemiBold),
    Font(R.font.inter_bold,     weight = FontWeight.Bold),
)

val MonoFamily = FontFamily(
    Font(R.font.jetbrains_mono_regular,  weight = FontWeight.Normal),
    Font(R.font.jetbrains_mono_semibold, weight = FontWeight.SemiBold),
    Font(R.font.jetbrains_mono_bold,     weight = FontWeight.Bold),
)

object AppTextStyles {

    val displayLarge = TextStyle(
        fontFamily = CinzelFamily, fontWeight = FontWeight.Bold,
        fontSize = 32.sp, letterSpacing = 1.2.sp
    )
    val displayMedium = TextStyle(
        fontFamily = CinzelFamily, fontWeight = FontWeight.Bold,
        fontSize = 24.sp, letterSpacing = 0.8.sp
    )
    val displaySmall = TextStyle(
        fontFamily = CinzelFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 18.sp, letterSpacing = 0.5.sp
    )

    val bodyLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 16.sp
    )
    val bodyMedium = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 14.sp
    )
    val bodySmall = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Normal, fontSize = 12.sp
    )

    val labelLarge = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 14.sp, letterSpacing = 0.5.sp
    )
    val labelSmall = TextStyle(
        fontFamily = InterFamily, fontWeight = FontWeight.Medium,
        fontSize = 11.sp, letterSpacing = 0.8.sp
    )

    val timeLarge = TextStyle(
        fontFamily = MonoFamily, fontWeight = FontWeight.Bold,
        fontSize = 28.sp, letterSpacing = 2.sp
    )
    val timeMedium = TextStyle(
        fontFamily = MonoFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 16.sp, letterSpacing = 1.sp
    )
    val timeSmall = TextStyle(
        fontFamily = MonoFamily, fontWeight = FontWeight.Normal,
        fontSize = 13.sp, letterSpacing = 0.5.sp
    )

    val saffronLabel = TextStyle(
        fontFamily = CinzelFamily, fontWeight = FontWeight.SemiBold,
        fontSize = 13.sp, color = AppColors.Primary, letterSpacing = 1.sp
    )
}
