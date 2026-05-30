package `in`.vedicpanchang.app.ui.calendar

import android.app.TimePickerDialog
import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.SizeTransform
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideInVertically
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.slideOutVertically
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectDragGestures
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.outlined.Today
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavController
import `in`.vedicpanchang.app.R
import `in`.vedicpanchang.app.data.model.CustomCalendarNote
import `in`.vedicpanchang.app.data.model.RepeatType
import `in`.vedicpanchang.app.ui.navigation.AppBottomNav
import `in`.vedicpanchang.app.ui.navigation.NavRoutes
import `in`.vedicpanchang.app.ui.theme.AppColors
import `in`.vedicpanchang.app.ui.theme.AppTextStyles
import `in`.vedicpanchang.app.viewmodel.CalendarViewModel
import `in`.vedicpanchang.app.viewmodel.LocationUiState
import `in`.vedicpanchang.app.viewmodel.NotesUiState
import `in`.vedicpanchang.app.viewmodel.PanchangUiState
import `in`.vedicpanchang.app.viewmodel.PanchangViewModel
import `in`.vedicpanchang.app.viewmodel.SettingsViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.DateTimeUnit
import kotlinx.datetime.LocalDate
import kotlinx.datetime.Month
import kotlinx.datetime.TimeZone
import kotlinx.datetime.isoDayNumber
import kotlinx.datetime.minus
import kotlinx.datetime.plus
import kotlinx.datetime.toLocalDateTime
import java.text.SimpleDateFormat
import java.time.ZoneId
import java.util.Locale
import kotlin.math.abs
import kotlin.time.Clock
import kotlin.time.Instant
import java.time.LocalDateTime as JavaLocalDateTime

private enum class CalendarViewMode { MONTH, BIWEEK, WEEK }

private data class CalendarKey(val mode: CalendarViewMode, val month: Month, val year: Int)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalendarScreen(
    navController: NavController,
    panchangVm: PanchangViewModel = hiltViewModel(),
    calendarVm: CalendarViewModel = hiltViewModel(),
    settingsVm: SettingsViewModel = hiltViewModel()
) {

    val strings by settingsVm.strings.collectAsStateWithLifecycle()
    val localizer by settingsVm.panchangLocalizer.collectAsStateWithLifecycle()
    val locale by settingsVm.locale.collectAsStateWithLifecycle()
    val selectedDate by calendarVm.selectedDate.collectAsStateWithLifecycle()
    val notesState by calendarVm.notesState.collectAsStateWithLifecycle()
    val notesForDay by calendarVm.notesForSelectedDate.collectAsStateWithLifecycle()
    val panchangState by panchangVm.state.collectAsStateWithLifecycle()
    val location = (panchangState.location as? LocationUiState.Success)?.location
    isSystemInDarkTheme()
    val today =
        remember { Clock.System.now().toLocalDateTime(TimeZone.currentSystemDefault()).date }
    var showAddSheet by remember { mutableStateOf(false) }
    var focusedMonth by remember { mutableStateOf(today.month) }
    var focusedYear by remember { mutableIntStateOf(today.year) }
    var viewMode by remember { mutableStateOf(CalendarViewMode.MONTH) }
    var festivalsByDay by remember { mutableStateOf<Map<LocalDate, List<String>>>(emptyMap()) }
    var eclipsesByDay  by remember { mutableStateOf<Map<LocalDate, Boolean>>(emptyMap()) }

    val notesByDay = (notesState as? NotesUiState.Success)?.notesByDay ?: emptyMap()

    LaunchedEffect(focusedMonth, focusedYear, viewMode, selectedDate, location) {
        val dates = when (viewMode) {
            CalendarViewMode.MONTH -> {
                val daysInMonth = LocalDate(focusedYear, focusedMonth, 1).let {
                    val next = it.plus(1, DateTimeUnit.MONTH)
                    (next.toEpochDays() - it.toEpochDays()).toInt()
                }
                (1..daysInMonth).map { day -> LocalDate(focusedYear, focusedMonth, day) }
            }

            CalendarViewMode.BIWEEK -> {
                val startOffset = selectedDate.dayOfWeek.isoDayNumber - 1
                val weekStart = selectedDate.minus(startOffset, DateTimeUnit.DAY)
                (0..13).map { weekStart.plus(it, DateTimeUnit.DAY) }
            }

            CalendarViewMode.WEEK -> {
                val startOffset = selectedDate.dayOfWeek.isoDayNumber - 1
                val weekStart = selectedDate.minus(startOffset, DateTimeUnit.DAY)
                (0..6).map { weekStart.plus(it, DateTimeUnit.DAY) }
            }
        }
        val result = mutableMapOf<LocalDate, List<String>>()
        val eclipseResult = mutableMapOf<LocalDate, Boolean>()
        dates.forEach { date ->
            val p = panchangVm.getPanchangForDate(date, location)
            if (p != null) {
                if (p.festivals.isNotEmpty()) result[date] = p.festivals
                if (p.hasEclipse) eclipseResult[date] = true
            }
        }
        festivalsByDay = result
        eclipsesByDay  = eclipseResult
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(strings["calendar"] ?: stringResource(R.string.calendar)) },
                navigationIcon = {
                    if (navController.previousBackStackEntry != null) {
                        IconButton(onClick = { navController.popBackStack() }) {
                            Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = null)
                        }
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val t = today
                        focusedMonth = t.month
                        focusedYear = t.year
                        calendarVm.selectDate(t)
                    }) {
                        Icon(Icons.Outlined.Today, contentDescription = null)
                    }
                }
            )
        },
        bottomBar = {
            AppBottomNav(navController = navController, strings = strings)
        },
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            // Month navigator
            MonthNavigator(
                month = focusedMonth,
                year = focusedYear,
                viewMode = viewMode,
                onPrev = {
                    if (viewMode == CalendarViewMode.MONTH) {
                        val prev =
                            LocalDate(focusedYear, focusedMonth, 1).minus(1, DateTimeUnit.MONTH)
                        focusedMonth = prev.month
                        focusedYear = prev.year
                    } else {
                        val prev = selectedDate.minus(7, DateTimeUnit.DAY)
                        focusedMonth = prev.month
                        focusedYear = prev.year
                        calendarVm.selectDate(prev)
                    }
                },
                onNext = {
                    if (viewMode == CalendarViewMode.MONTH) {
                        val next =
                            LocalDate(focusedYear, focusedMonth, 1).plus(1, DateTimeUnit.MONTH)
                        focusedMonth = next.month
                        focusedYear = next.year
                    } else {
                        val next = selectedDate.plus(7, DateTimeUnit.DAY)
                        focusedMonth = next.month
                        focusedYear = next.year
                        calendarVm.selectDate(next)
                    }
                },
                onToggleView = {
                    viewMode = when (viewMode) {
                        CalendarViewMode.MONTH -> CalendarViewMode.BIWEEK
                        CalendarViewMode.BIWEEK -> CalendarViewMode.WEEK
                        CalendarViewMode.WEEK -> CalendarViewMode.MONTH
                    }
                    focusedMonth = selectedDate.month
                    focusedYear = selectedDate.year
                }
            )

            // Custom month grid
            MonthCalendarGrid(
                year = focusedYear,
                month = focusedMonth,
                selectedDate = selectedDate,
                today = today,
                notesByDay = notesByDay,
                festivalsByDay = festivalsByDay,
                eclipsesByDay = eclipsesByDay,
                viewMode = viewMode,
                onDaySelected = { date ->
                    calendarVm.selectDate(date)
                    focusedMonth = date.month
                    focusedYear = date.year
                },
                onSwipeLeft = {
                    if (viewMode == CalendarViewMode.MONTH) {
                        val next =
                            LocalDate(focusedYear, focusedMonth, 1).plus(1, DateTimeUnit.MONTH)
                        focusedMonth = next.month; focusedYear = next.year
                    } else {
                        val next = selectedDate.plus(7, DateTimeUnit.DAY)
                        calendarVm.selectDate(next); focusedMonth = next.month; focusedYear =
                            next.year
                    }
                },
                onSwipeRight = {
                    if (viewMode == CalendarViewMode.MONTH) {
                        val prev =
                            LocalDate(focusedYear, focusedMonth, 1).minus(1, DateTimeUnit.MONTH)
                        focusedMonth = prev.month; focusedYear = prev.year
                    } else {
                        val prev = selectedDate.minus(7, DateTimeUnit.DAY)
                        calendarVm.selectDate(prev); focusedMonth = prev.month; focusedYear =
                            prev.year
                    }
                },
                onSwipeUp = {
                    viewMode = when (viewMode) {
                        CalendarViewMode.MONTH -> CalendarViewMode.BIWEEK
                        CalendarViewMode.BIWEEK -> CalendarViewMode.WEEK
                        CalendarViewMode.WEEK -> CalendarViewMode.WEEK
                    }
                    focusedMonth = selectedDate.month; focusedYear = selectedDate.year
                },
                onSwipeDown = {
                    viewMode = when (viewMode) {
                        CalendarViewMode.WEEK -> CalendarViewMode.BIWEEK
                        CalendarViewMode.BIWEEK -> CalendarViewMode.MONTH
                        CalendarViewMode.MONTH -> CalendarViewMode.MONTH
                    }
                    focusedMonth = selectedDate.month; focusedYear = selectedDate.year
                }
            )

            HorizontalDivider()

            // Day summary panel
            DaySummaryPanel(
                selectedDate = selectedDate,
                panchangVm = panchangVm,
                notes = notesForDay,
                strings = strings,
                localizer = localizer,
                locale = locale,
                onDeleteNote = { note -> calendarVm.deleteNote(note) },
                onAddNote = { showAddSheet = true },
                onViewDetails = {
                    navController.navigate(
                        NavRoutes.dayDetail(
                            "%04d-%02d-%02d".format(
                                selectedDate.year,
                                selectedDate.month.ordinal + 1,
                                selectedDate.day
                            )
                        )
                    ) { launchSingleTop = true }
                },
                modifier = Modifier.weight(1f)
            )
        }
    }

    if (showAddSheet) {
        AddNoteSheet(
            selectedDate = selectedDate,
            strings = strings,
            localizer = localizer,
            calendarVm = calendarVm,
            onDismiss = { showAddSheet = false }
        )
    }
}

@Composable
private fun MonthNavigator(
    month: Month,
    year: Int,
    viewMode: CalendarViewMode,
    onPrev: () -> Unit,
    onNext: () -> Unit,
    onToggleView: () -> Unit
) {
    val javaLocale = Locale.ENGLISH
    val fmt = SimpleDateFormat("MMMM yyyy", javaLocale)
    val cal = java.util.Calendar.getInstance().apply { set(year, month.ordinal, 1) }
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        IconButton(onClick = onPrev) {
            Text(
                "<",
                style = AppTextStyles.labelLarge.copy(color = AppColors.Primary)
            )
        }
        Text(fmt.format(cal.time), style = AppTextStyles.displaySmall)
        Row(verticalAlignment = Alignment.CenterVertically) {
            Box(
                modifier = Modifier
                    .clip(RoundedCornerShape(20.dp))
                    .border(1.5.dp, AppColors.Primary, RoundedCornerShape(20.dp))
                    .clickable { onToggleView() }
                    .padding(horizontal = 14.dp, vertical = 6.dp)
            ) {
                Text(
                    text = when (viewMode) {
                        CalendarViewMode.MONTH -> "Month"
                        CalendarViewMode.BIWEEK -> "2 weeks"
                        CalendarViewMode.WEEK -> "1 week"
                    },
                    style = AppTextStyles.labelSmall.copy(color = AppColors.Primary)
                )
            }
            IconButton(onClick = onNext) {
                Text(
                    ">",
                    style = AppTextStyles.labelLarge.copy(color = AppColors.Primary)
                )
            }
        }
    }
}

@Composable
private fun MonthCalendarGrid(
    year: Int, month: Month, selectedDate: LocalDate, today: LocalDate,
    notesByDay: Map<LocalDate, List<CustomCalendarNote>>,
    festivalsByDay: Map<LocalDate, List<String>>,
    eclipsesByDay: Map<LocalDate, Boolean>,
    viewMode: CalendarViewMode,
    onDaySelected: (LocalDate) -> Unit,
    onSwipeLeft: () -> Unit,
    onSwipeRight: () -> Unit,
    onSwipeUp: () -> Unit,
    onSwipeDown: () -> Unit,
) {
    val weekDayLabels = listOf("M", "T", "W", "T", "F", "S", "S")

    @Composable
    fun DayCell(date: LocalDate, col: Int, modifier: Modifier = Modifier) {
        val isSelected = date == selectedDate
        val isToday = date == today
        val hasEclipse = eclipsesByDay[date] == true
        val hasFestival = festivalsByDay[date]?.isNotEmpty() == true
        val hasNote = notesByDay[date] != null

        Box(
            modifier = modifier
                .aspectRatio(1f)
                .padding(2.dp)
                .clip(CircleShape)
                .background(
                    when {
                        isSelected -> Brush.linearGradient(
                            listOf(
                                AppColors.SaffronGradientStart,
                                AppColors.SaffronGradientEnd
                            )
                        )

                        isToday -> Brush.linearGradient(
                            listOf(
                                AppColors.Primary.copy(alpha = 0.2f),
                                AppColors.Primary.copy(alpha = 0.2f)
                            )
                        )

                        else -> Brush.linearGradient(listOf(Color.Transparent, Color.Transparent))
                    }
                )
                .then(
                    if (isToday && !isSelected)
                        Modifier.border(1.dp, AppColors.Primary, CircleShape)
                    else Modifier
                )
                .clickable { onDaySelected(date) },
            contentAlignment = Alignment.Center
        ) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Text(
                    "${date.day}",
                    style = AppTextStyles.bodySmall.copy(
                        color = when {
                            isSelected -> Color.White
                            col == 6 || col == 5 -> AppColors.Primary
                            else -> MaterialTheme.colorScheme.onSurface
                        }
                    )
                )
                if (hasEclipse || hasFestival || hasNote) {
                    Row(horizontalArrangement = Arrangement.spacedBy(2.dp)) {
                        if (hasEclipse) Box(
                            Modifier
                                .size(5.dp)
                                .clip(CircleShape)
                                .background(Color(0xFF212121))
                        )
                        if (hasFestival) Box(
                            Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(AppColors.Secondary)
                        )
                        if (hasNote) Box(
                            Modifier
                                .size(4.dp)
                                .clip(CircleShape)
                                .background(AppColors.CustomNote)
                        )
                    }
                }
            }
        }
    }

    // Build the animated key: month-mode uses focusedMonth/Year; week modes anchor to selectedDate
    val animKey = when (viewMode) {
        CalendarViewMode.MONTH -> CalendarKey(viewMode, month, year)
        else -> CalendarKey(viewMode, selectedDate.month, selectedDate.year)
    }

    Column(
        modifier = Modifier
            .padding(horizontal = 8.dp)
            .pointerInput(onSwipeLeft, onSwipeRight, onSwipeUp, onSwipeDown) {
                var dragX = 0f
                var dragY = 0f
                detectDragGestures(
                    onDragStart = { dragX = 0f; dragY = 0f },
                    onDrag = { change, amount ->
                        change.consume()
                        dragX += amount.x
                        dragY += amount.y
                    },
                    onDragEnd = {
                        val threshold = 40f
                        if (abs(dragX) > abs(dragY)) {
                            when {
                                dragX < -threshold -> onSwipeLeft()
                                dragX > threshold -> onSwipeRight()
                            }
                        } else {
                            when {
                                dragY < -threshold -> onSwipeUp()
                                dragY > threshold -> onSwipeDown()
                            }
                        }
                    }
                )
            }
    ) {
        // Weekday header — always static
        Row(Modifier.fillMaxWidth()) {
            weekDayLabels.forEach { label ->
                Box(Modifier.weight(1f), contentAlignment = Alignment.Center) {
                    Text(
                        label,
                        style = AppTextStyles.labelSmall.copy(
                            color = if (label == "S") AppColors.Primary
                            else MaterialTheme.colorScheme.onSurface.copy(alpha = 0.6f)
                        )
                    )
                }
            }
        }
        Spacer(Modifier.height(4.dp))

        // Animated grid rows
        AnimatedContent(
            targetState = animKey,
            transitionSpec = {
                val modeChanged = targetState.mode != initialState.mode
                if (modeChanged) {
                    // Vertical: swipe up collapses (more→fewer rows), swipe down expands
                    val collapsing = targetState.mode.ordinal > initialState.mode.ordinal
                    if (collapsing) {
                        (slideInVertically { -it / 3 } + fadeIn()) togetherWith
                                (slideOutVertically { it / 3 } + fadeOut())
                    } else {
                        (slideInVertically { it / 3 } + fadeIn()) togetherWith
                                (slideOutVertically { -it / 3 } + fadeOut())
                    }
                } else {
                    // Horizontal: forward = swipe left = next month
                    val forward = targetState.year * 12 + targetState.month.ordinal >
                            initialState.year * 12 + initialState.month.ordinal
                    if (forward) {
                        (slideInHorizontally { it } + fadeIn()) togetherWith
                                (slideOutHorizontally { -it } + fadeOut())
                    } else {
                        (slideInHorizontally { -it } + fadeIn()) togetherWith
                                (slideOutHorizontally { it } + fadeOut())
                    }
                }.using(SizeTransform(clip = true))
            },
            label = "CalendarGrid"
        ) { key ->
            Column {
                when (key.mode) {
                    CalendarViewMode.MONTH -> {
                        val daysInMonth = LocalDate(key.year, key.month, 1).let {
                            val next = it.plus(1, DateTimeUnit.MONTH)
                            (next.toEpochDays() - it.toEpochDays()).toInt()
                        }
                        val firstDay = LocalDate(key.year, key.month, 1)
                        val startOffset = firstDay.dayOfWeek.isoDayNumber - 1
                        val rows = (startOffset + daysInMonth + 6) / 7
                        for (row in 0 until rows) {
                            Row(Modifier.fillMaxWidth()) {
                                for (col in 0..6) {
                                    val day = row * 7 + col - startOffset + 1
                                    if (day < 1 || day > daysInMonth) {
                                        Box(
                                            Modifier
                                                .weight(1f)
                                                .aspectRatio(1f)
                                        )
                                    } else {
                                        DayCell(
                                            LocalDate(key.year, key.month, day),
                                            col,
                                            Modifier.weight(1f)
                                        )
                                    }
                                }
                            }
                        }
                    }

                    CalendarViewMode.BIWEEK -> {
                        val startOffset = selectedDate.dayOfWeek.isoDayNumber - 1
                        val weekStart = selectedDate.minus(startOffset, DateTimeUnit.DAY)
                        for (weekOffset in 0..1) {
                            Row(Modifier.fillMaxWidth()) {
                                for (col in 0..6) {
                                    DayCell(
                                        weekStart.plus(weekOffset * 7 + col, DateTimeUnit.DAY),
                                        col,
                                        Modifier.weight(1f)
                                    )
                                }
                            }
                        }
                    }

                    CalendarViewMode.WEEK -> {
                        val startOffset = selectedDate.dayOfWeek.isoDayNumber - 1
                        val weekStart = selectedDate.minus(startOffset, DateTimeUnit.DAY)
                        Row(Modifier.fillMaxWidth()) {
                            for (col in 0..6) {
                                DayCell(
                                    weekStart.plus(col, DateTimeUnit.DAY),
                                    col,
                                    Modifier.weight(1f)
                                )
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun DaySummaryPanel(
    selectedDate: LocalDate,
    panchangVm: PanchangViewModel,
    notes: List<CustomCalendarNote>,
    strings: Map<String, String>,
    localizer: `in`.vedicpanchang.app.l10n.PanchangLocalizer,
    locale: String,
    onDeleteNote: (CustomCalendarNote) -> Unit,
    onAddNote: () -> Unit,
    onViewDetails: () -> Unit,
    modifier: Modifier
) {
    var panchang by remember(selectedDate) {
        mutableStateOf<`in`.vedicpanchang.app.data.model.PanchangModel?>(
            null
        )
    }
    val panchangState by panchangVm.state.collectAsStateWithLifecycle()
    val location = (panchangState.location as? LocationUiState.Success)?.location
    LaunchedEffect(selectedDate, location) {
        panchang = panchangVm.getPanchangForDate(selectedDate, location)
    }

    LazyColumn(modifier = modifier.padding(16.dp)) {
        item {
            val javaLocale =
                if (locale == "hi" || locale == "sa") Locale.forLanguageTag("hi-IN") else Locale.ENGLISH
            val cal = java.util.Calendar.getInstance()
                .apply { set(selectedDate.year, selectedDate.month.ordinal, selectedDate.day) }
            val fmt = SimpleDateFormat("EEEE, d MMMM", javaLocale)
            Row(
                Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    localizer.numerals(fmt.format(cal.time)),
                    style = AppTextStyles.displaySmall,
                    overflow = TextOverflow.Ellipsis,
                    modifier = Modifier.weight(1f)
                )
                TextButton(onClick = onViewDetails) {
                    Text(
                        strings["full_details"] ?: "Full Details →",
                        style = AppTextStyles.labelLarge.copy(color = AppColors.Primary)
                    )
                }
            }
            Spacer(Modifier.height(12.dp))
        }

        panchang?.let { p ->
            item {
                PanchangSummaryRows(p = p, strings = strings, localizer = localizer)
                Spacer(Modifier.height(12.dp))
            }
        }

        item {
            NotesSection(
                notes = notes,
                strings = strings,
                onDelete = onDeleteNote,
                onAdd = onAddNote
            )
        }


    }
}

@Composable
private fun PanchangSummaryRows(
    p: `in`.vedicpanchang.app.data.model.PanchangModel,
    strings: Map<String, String>,
    localizer: `in`.vedicpanchang.app.l10n.PanchangLocalizer
) {
    val isDark = MaterialTheme.colorScheme.background.luminance() < 0.5f
    val cardBorderColor =
        if (isDark) AppColors.Primary.copy(alpha = 0.45f) else Color(0xFFCBA35C).copy(alpha = 0.55f)
    val cardShape = RoundedCornerShape(12.dp)
    val tz = TimeZone.currentSystemDefault()
    val sunrise = p.sunrise.toLocalDateTime(tz)
    val rahuStart = p.rahuKaal.start.toLocalDateTime(tz)
    val rahuEnd = p.rahuKaal.end.toLocalDateTime(tz)

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(cardShape)
            .border(1.dp, cardBorderColor, cardShape)
            .background(AppColors.Primary.copy(alpha = 0.08f))
            .padding(12.dp)
    ) {
        Column {
            Text(strings["panchang"] ?: "Panchang", style = AppTextStyles.saffronLabel)
            Spacer(Modifier.height(8.dp))
            QuickRow("🌙 ${strings["tithi"] ?: "Tithi"}", localizer.tithiDisplay(p))
            QuickRow("⭐ ${strings["nakshatra"] ?: "Nakshatra"}", localizer.nakshatraName(p))
            QuickRow("☀️ ${strings["yoga"] ?: "Yoga"}", localizer.yogaWithAuspicious(p))
            QuickRow("🔀 ${strings["karana"] ?: "Karana"}", localizer.karanaName(p))
            QuickRow(
                "🌅 ${strings["sunrise"] ?: "Sunrise"}",
                localizer.numerals("%02d:%02d".format(sunrise.hour, sunrise.minute))
            )
            QuickRow(
                "⚠️ ${strings["rahu_kaal"] ?: "Rahu Kaal"}",
                localizer.numerals(
                    "%02d:%02d – %02d:%02d".format(
                        rahuStart.hour,
                        rahuStart.minute,
                        rahuEnd.hour,
                        rahuEnd.minute
                    )
                )
            )
            if (p.hasFestivals) {
                Spacer(Modifier.height(8.dp))
                Text(
                    "🪔 ${p.festivals.joinToString(" · ") { localizer.festivalName(it) }}",
                    style = AppTextStyles.bodySmall.copy(color = AppColors.Primary)
                )
            }
        }
    }
}

@Composable
private fun NotesSection(
    notes: List<CustomCalendarNote>,
    strings: Map<String, String>,
    onDelete: (CustomCalendarNote) -> Unit,
    onAdd: () -> Unit
) {
    Box(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .border(1.dp, AppColors.CustomNote.copy(alpha = 0.5f), RoundedCornerShape(12.dp))
            .background(AppColors.CustomNote.copy(alpha = 0.06f))
            .padding(12.dp)
    ) {
        Column {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.SpaceBetween
            ) {
                Text(
                    strings["my_notes"] ?: "My Notes",
                    style = AppTextStyles.labelLarge.copy(color = AppColors.CustomNote)
                )
                Row(
                    modifier = Modifier.clickable { onAdd() },
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Icon(
                        Icons.Filled.Add,
                        contentDescription = null,
                        tint = AppColors.CustomNote,
                        modifier = Modifier
                            .size(18.dp)
                            .clip(CircleShape)
                            .border(1.dp, AppColors.CustomNote, CircleShape)
                            .padding(2.dp)
                    )
                    Spacer(Modifier.width(4.dp))
                    Text(
                        strings["add_note"] ?: "नोट जोड़ें",
                        style = AppTextStyles.labelSmall.copy(color = AppColors.CustomNote)
                    )
                }
            }
            Spacer(Modifier.height(10.dp))
            if (notes.isEmpty()) {
                Text(
                    strings["no_notes_for_day"] ?: "इस दिन के लिए कोई कस्टम नोट नहीं",
                    style = AppTextStyles.bodySmall
                )
            } else {
                notes.forEach { note ->
                    Row(
                        Modifier
                            .fillMaxWidth()
                            .padding(vertical = 4.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Column(Modifier.weight(1f)) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Text(
                                    note.title,
                                    style = AppTextStyles.bodyMedium.copy(fontWeight = androidx.compose.ui.text.font.FontWeight.Bold)
                                )
                                if (note.repeatType != RepeatType.NONE) {
                                    Spacer(Modifier.width(6.dp))
                                    Box(
                                        modifier = Modifier
                                            .clip(RoundedCornerShape(50))
                                            .background(AppColors.Primary.copy(alpha = 0.12f))
                                            .padding(horizontal = 6.dp, vertical = 2.dp)
                                    ) {
                                        Text(
                                            "🔁 ${
                                                note.repeatType.name.lowercase()
                                                    .replaceFirstChar { it.uppercaseChar() }
                                            }",
                                            style = AppTextStyles.labelSmall.copy(
                                                color = AppColors.Primary,
                                                fontSize = androidx.compose.ui.unit.TextUnit(
                                                    9f,
                                                    androidx.compose.ui.unit.TextUnitType.Sp
                                                )
                                            )
                                        )
                                    }
                                }
                            }
                            if (note.description.isNotEmpty())
                                Text(note.description, style = AppTextStyles.bodySmall)
                        }
                        IconButton(onClick = { onDelete(note) }, modifier = Modifier.size(32.dp)) {
                            Icon(
                                Icons.Filled.Delete,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.error,
                                modifier = Modifier.size(16.dp)
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun QuickRow(label: String, value: String) {
    Row(
        Modifier
            .fillMaxWidth()
            .padding(vertical = 3.dp)
    ) {
        Text(label, style = AppTextStyles.bodySmall, modifier = Modifier.width(140.dp))
        Text(value, style = AppTextStyles.bodyMedium, modifier = Modifier.weight(1f))
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun AddNoteSheet(
    selectedDate: LocalDate,
    strings: Map<String, String>,
    localizer: `in`.vedicpanchang.app.l10n.PanchangLocalizer,
    calendarVm: CalendarViewModel,
    onDismiss: () -> Unit
) {
    var title by remember { mutableStateOf("") }
    var description by remember { mutableStateOf("") }
    var notifEnabled by remember { mutableStateOf(false) }
    var reminderHour by remember { mutableIntStateOf(8) }
    var reminderMinute by remember { mutableIntStateOf(0) }
    var repeatType by remember { mutableStateOf(RepeatType.NONE) }
    var isSaving by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()
    val context = LocalContext.current

    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp, vertical = 8.dp)
                .padding(bottom = 32.dp)
        ) {
            Text(strings["add_note_for_date"] ?: "Add Note", style = AppTextStyles.displaySmall)
            Spacer(Modifier.height(12.dp))
            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text(strings["note_title"] ?: "Title") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
            Spacer(Modifier.height(10.dp))
            OutlinedTextField(
                value = description,
                onValueChange = { description = it },
                label = { Text(strings["note_details"] ?: "Details") },
                modifier = Modifier.fillMaxWidth(),
                minLines = 3
            )
            Spacer(Modifier.height(12.dp))

            // ── Repeat selector ──────────────────────────────────────────────
            Text(
                strings["repeat"] ?: "Repeat",
                style = AppTextStyles.bodySmall.copy(color = MaterialTheme.colorScheme.onSurfaceVariant)
            )
            Spacer(Modifier.height(8.dp))
            Row(
                modifier = Modifier
                    .fillMaxWidth()
                    .horizontalScroll(rememberScrollState()),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                RepeatType.entries.forEach { option ->
                    val selected = option == repeatType
                    val label = when (option) {
                        RepeatType.NONE -> strings["repeat_none"] ?: "None"
                        RepeatType.DAILY -> strings["repeat_daily"] ?: "Daily"
                        RepeatType.WEEKLY -> strings["repeat_weekly"] ?: "Weekly"
                        RepeatType.MONTHLY -> strings["repeat_monthly"] ?: "Monthly"
                        RepeatType.YEARLY -> strings["repeat_yearly"] ?: "Yearly"
                    }
                    Box(
                        modifier = Modifier
                            .clip(RoundedCornerShape(50))
                            .background(if (selected) AppColors.Primary else Color.Transparent)
                            .border(
                                1.dp,
                                if (selected) AppColors.Primary else MaterialTheme.colorScheme.outline.copy(
                                    alpha = 0.5f
                                ),
                                RoundedCornerShape(50)
                            )
                            .clickable { repeatType = option }
                            .padding(horizontal = 16.dp, vertical = 8.dp),
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            label,
                            style = AppTextStyles.labelSmall.copy(
                                color = if (selected) Color.White else MaterialTheme.colorScheme.onSurface
                            )
                        )
                    }
                }
            }

            Spacer(Modifier.height(12.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                Text(
                    strings["enable_notification"] ?: "Enable Notification",
                    modifier = Modifier.weight(1f)
                )
                Switch(
                    checked = notifEnabled,
                    onCheckedChange = {
                        notifEnabled = it
                        if (it) {
                            reminderHour = 8
                            reminderMinute = 0
                        }
                    }
                )
            }
            if (notifEnabled) {
                Spacer(Modifier.height(6.dp))
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 4.dp),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        strings["reminder_time"] ?: "Reminder Time",
                        style = AppTextStyles.bodySmall,
                        modifier = Modifier.weight(1f)
                    )
                    TextButton(onClick = {
                        TimePickerDialog(
                            context,
                            { _, hour, minute ->
                                reminderHour = hour
                                reminderMinute = minute
                            },
                            reminderHour,
                            reminderMinute,
                            true
                        ).show()
                    }) {
                        Text(
                            localizer.numerals("%02d:%02d".format(reminderHour, reminderMinute)),
                            style = AppTextStyles.labelLarge.copy(color = AppColors.Primary)
                        )
                    }
                }
            }
            Spacer(Modifier.height(12.dp))
            Button(
                onClick = {
                    if (title.isBlank()) return@Button
                    isSaving = true
                    scope.launch {
                        TimeZone.currentSystemDefault()
                        val reminderAt = if (notifEnabled) {
                            val localDateTime = JavaLocalDateTime.of(
                                selectedDate.year,
                                selectedDate.month.ordinal + 1,
                                selectedDate.day,
                                reminderHour,
                                reminderMinute
                            )
                            Instant.fromEpochMilliseconds(
                                localDateTime.atZone(ZoneId.systemDefault()).toInstant()
                                    .toEpochMilli()
                            )
                        } else {
                            null
                        }
                        calendarVm.addNote(selectedDate, title, description, reminderAt, repeatType)
                        onDismiss()
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isSaving,
                colors = ButtonDefaults.buttonColors(containerColor = AppColors.Primary)
            ) {
                if (isSaving) CircularProgressIndicator(
                    Modifier.size(16.dp),
                    color = Color.White,
                    strokeWidth = 2.dp
                )
                else Text(strings["save_note"] ?: "Save")
            }
        }
    }
}
