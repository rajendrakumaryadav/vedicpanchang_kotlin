package `in`.vedicpanchang.app.ui.home

import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Text
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.navigation.NavController
import `in`.vedicpanchang.app.data.model.PanchangModel
import `in`.vedicpanchang.app.l10n.PanchangLocalizer
import `in`.vedicpanchang.app.ui.navigation.NavRoutes
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import androidx.compose.material3.MaterialTheme
import androidx.compose.ui.graphics.luminance
import `in`.vedicpanchang.app.viewmodel.LocationUiState
import `in`.vedicpanchang.app.viewmodel.PanchangViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import kotlinx.datetime.*
import kotlinx.datetime.LocalDate
import kotlinx.datetime.number
import java.text.SimpleDateFormat
import java.util.Locale
import kotlin.time.Clock
@Composable
fun UpcomingEventsCard(
    panchangVm: PanchangViewModel,
    strings: Map<String, String>,
    localizer: PanchangLocalizer,
    navController: NavController
) {
    val today = Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date
    var upcomingEvents by remember { mutableStateOf<List<Pair<Int, PanchangModel>>>(emptyList()) }
    val panchangState by panchangVm.state.collectAsStateWithLifecycle()
    val location = (panchangState.location as? LocationUiState.Success)?.location

    LaunchedEffect(today, location) {
        val results = mutableListOf<Pair<Int, PanchangModel>>()
        for (i in 0..30) {
            val date = today.plus(i, DateTimeUnit.DAY)
            val p = panchangVm.getPanchangForDate(date, location)
            if (p != null && p.festivals.isNotEmpty()) {
                results.add(i to p)
                if (results.size >= 8) break
            }
        }
        upcomingEvents = results
    }

    if (upcomingEvents.isEmpty()) return

    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = strings["upcoming_events"] ?: "Upcoming Events",
            style = AppTextStyles.displaySmall.copy(fontSize = 18.sp),
            modifier = Modifier.padding(horizontal = 16.dp, vertical = 8.dp)
        )

        LazyRow(
            contentPadding = PaddingValues(horizontal = 16.dp),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            modifier = Modifier.fillMaxWidth()
        ) {
            items(upcomingEvents) { (daysFromNow, panchang) ->
                EventCard(
                    daysFromNow = daysFromNow,
                    panchang = panchang,
                    strings = strings,
                    localizer = localizer,
                    onClick = {
                        navController.navigate(NavRoutes.dayDetail(panchang.date.toString())) {
                            launchSingleTop = true
                        }
                    }
                )
            }
        }
    }
}

@Composable
fun EventCard(
    daysFromNow: Int,
    panchang: PanchangModel,
    strings: Map<String, String>,
    localizer: PanchangLocalizer,
    onClick: () -> Unit
) {
    val isToday = daysFromNow == 0
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val date = panchang.date
    val javaLocale = if (localizer.locale == "sa" || localizer.locale == "hi") Locale.forLanguageTag("hi-IN") else Locale.ENGLISH

    val calendar = java.util.Calendar.getInstance()
    calendar.set(date.year, date.month.number - 1, date.day)
    val dateStr = localizer.numerals(SimpleDateFormat("d MMM", javaLocale).format(calendar.time))

    val dayLabel = when (daysFromNow) {
        0 -> strings["today"] ?: "TODAY"
        1 -> strings["tomorrow"] ?: "TOMORROW"
        else -> localizer.numerals(daysFromNow.toString()) + " " + (strings["days_away"] ?: "D AWAY")
    }

    val paksha = localizer.paksha(panchang)
    val tithiName = localizer.tithiName(panchang)
    val subInfo = "$paksha $tithiName"

    val cardBg = when {
        isToday -> Brush.verticalGradient(listOf(Color(0xFFFF8C38), Color(0xFFFFD12B)))
        isDark  -> Brush.verticalGradient(listOf(Color(0xFF242033), Color(0xFF1A1629)))
        else    -> Brush.verticalGradient(listOf(Color(0xFFFEF9F2), Color(0xFFF5E8D0)))
    }
    val primaryTextColor = if (isToday || isDark) Color.White else AppColors.TextPrimaryLight
    val secondaryTextColor = if (isToday || isDark) Color.White.copy(alpha = 0.8f) else AppColors.TextSecondaryLight
    val tertiaryTextColor = if (isToday || isDark) Color.White.copy(alpha = 0.6f) else AppColors.TextMutedLight

    Box(
        modifier = Modifier
            .size(width = 120.dp, height = 110.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(cardBg)
            .clickable(onClick = onClick)
            .padding(12.dp)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            // Badge
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(8.dp))
                    .background(if (isToday) Color.White.copy(alpha = 0.2f) else AppColors.Primary.copy(alpha = if (isDark) 0.1f else 0.12f))
                    .padding(horizontal = 6.dp, vertical = 3.dp)
            ) {
                Text(
                    text = dayLabel,
                    style = AppTextStyles.labelSmall.copy(
                        color = if (isToday) Color.White else AppColors.Primary,
                        fontSize = 9.sp,
                        fontWeight = FontWeight.Bold
                    )
                )
            }

            Spacer(Modifier.height(10.dp))

            // Festival name
            Text(
                text = panchang.festivals.joinToString("\n") { localizer.festivalName(it) },
                style = AppTextStyles.bodyMedium.copy(
                    color = primaryTextColor,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    lineHeight = 18.sp
                ),
                maxLines = 2,
                modifier = Modifier.weight(1f)
            )

            // Bottom info
            Column {
                Text(
                    text = dateStr,
                    style = AppTextStyles.bodySmall.copy(color = secondaryTextColor, fontSize = 11.sp)
                )
                Text(
                    text = subInfo,
                    style = AppTextStyles.bodySmall.copy(color = tertiaryTextColor, fontSize = 9.sp)
                )
            }
        }
    }
}
