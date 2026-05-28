package `in`.vedicpanchang.app.ui.theme

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.material3.ColorScheme
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Typography
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.sp

private val DarkColorScheme = darkColorScheme(
    primary          = AppColors.Primary,
    primaryContainer = AppColors.PrimaryDark,
    secondary        = AppColors.Secondary,
    secondaryContainer = AppColors.SecondaryDark,
    surface          = AppColors.Surface,
    background       = AppColors.Background,
    error            = AppColors.Inauspicious,
    onPrimary        = Color.White,
    onSecondary      = AppColors.Background,
    onSurface        = AppColors.TextPrimary,
    onBackground     = AppColors.TextPrimary,
    onError          = Color.White,
    outline          = AppColors.CardBorder,
    surfaceVariant   = AppColors.SurfaceVariant,
)

private val LightColorScheme = lightColorScheme(
    primary          = AppColors.Primary,
    primaryContainer = AppColors.PrimaryDark,
    secondary        = AppColors.SecondaryOnLight,
    secondaryContainer = AppColors.SecondaryDark,
    surface          = AppColors.SurfaceLight,
    background       = AppColors.BackgroundLight,
    error            = AppColors.Inauspicious,
    onPrimary        = Color.White,
    onSecondary      = Color.White,
    onSurface        = AppColors.TextPrimaryLight,
    onBackground     = AppColors.TextPrimaryLight,
    onError          = Color.White,
    outline          = AppColors.CardBorderLight,
    surfaceVariant   = AppColors.SurfaceVariantLight,
    onSurfaceVariant = AppColors.TextSecondaryLight,
)

private fun buildTypography(isDark: Boolean): Typography {
    val textPrimary = if (isDark) AppColors.TextPrimary else AppColors.TextPrimaryLight
    val textSecondary = if (isDark) AppColors.TextSecondary else AppColors.TextSecondaryLight
    val textMuted = if (isDark) AppColors.TextMuted else AppColors.TextMutedLight
    return Typography(
        displayLarge  = AppTextStyles.displayLarge.copy(color = textPrimary),
        displayMedium = AppTextStyles.displayMedium.copy(color = textPrimary),
        displaySmall  = AppTextStyles.displaySmall.copy(color = textPrimary),
        headlineLarge = AppTextStyles.displayMedium.copy(fontSize = 22.sp, color = textPrimary),
        headlineMedium= AppTextStyles.displaySmall.copy(fontSize = 18.sp, color = textPrimary),
        headlineSmall = AppTextStyles.displaySmall.copy(fontSize = 16.sp, color = textPrimary),
        titleLarge    = AppTextStyles.bodyLarge.copy(fontWeight = FontWeight.SemiBold, color = textPrimary),
        titleMedium   = AppTextStyles.bodyMedium.copy(fontWeight = FontWeight.Medium, color = textPrimary),
        titleSmall    = AppTextStyles.bodySmall.copy(fontWeight = FontWeight.Medium, color = textSecondary),
        bodyLarge     = AppTextStyles.bodyLarge.copy(color = textPrimary),
        bodyMedium    = AppTextStyles.bodyMedium.copy(color = textPrimary),
        bodySmall     = AppTextStyles.bodySmall.copy(color = textSecondary),
        labelLarge    = AppTextStyles.labelLarge.copy(color = textPrimary),
        labelMedium   = AppTextStyles.labelSmall.copy(fontSize = 12.sp, color = textSecondary),
        labelSmall    = AppTextStyles.labelSmall.copy(color = textMuted),
    )
}

@Composable
fun VedicPanchangTheme(
    darkTheme: Boolean = isSystemInDarkTheme(),
    content: @Composable () -> Unit
) {
    val colorScheme = if (darkTheme) DarkColorScheme else LightColorScheme
    val typography = buildTypography(darkTheme)

    MaterialTheme(
        colorScheme = colorScheme,
        typography = typography,
        content = content
    )
}
