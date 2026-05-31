# Detailed Project Knowledge Base for VedicPanchang

## Project Overview
**VedicPanchang** is a high-precision Hindu Calendar and Vedic Astrology application. It uses astronomical algorithms to calculate Tithi, Nakshatra, Yoga, Karana, and various auspicious/inauspicious timings.

## Module Structure

### 1. `:astronomy` (Core Calculation Engine)
A Kotlin Multiplatform module containing pure logic for astronomical calculations.

*   **Package: `in.vedicpanchang.astronomy`**
    *   `AstronomyService.kt`: The main facade for all calculations. Aggregates data from various calculators.
    *   `PanchangCalculators.kt`: Logic for Tithi, Nakshatra, Yoga, Karana, and Vara.
    *   `PlanetaryPositions.kt`: Calculates longitudes and latitudes of planets (Sun, Moon, Mars, etc.) for a given time and location.
    *   `ChoghadiyaCalculator.kt`: Calculates day and night Choghadiya periods based on sunrise/sunset.
    *   `HoraCalculator.kt`: Calculates planetary hours (Hora) for the day.
    *   `MuhurtaCalculator.kt`: Logic for Rahu Kaal, Gulika Kaal, Yamaganda, and Abhijit Muhurta.
    *   `EclipseCalculator.kt`: Solar and lunar eclipse predictions (if implemented).
    *   `TimeRange.kt`: Data model representing a start and end time.
    *   `PanchangConstants.kt`: Astronomical constants and planet/sign indices.

---

### 2. `:app` (Android Application)
Contains the UI (Jetpack Compose), ViewModels, and Android-specific services.

*   **Package: `in.vedicpanchang.app`**
    *   `MainActivity.kt`: Entry point, sets up Compose Navigation and Hilt.
    *   `VedicPanchangApp.kt`: Application class, initializes Hilt.

*   **Package: `in.vedicpanchang.app.data`**
    *   **`datasource`**:
        *   `AppPreferences.kt`: Manages user settings (language, theme, chart style) via DataStore.
        *   `FestivalData.kt`: Static or JSON-based dataset of Hindu festivals.
        *   **`db`**: Room database for calendar notes.
            *   `NoteDatabase.kt`, `NoteEntity.kt`, `NoteDao.kt`.
    *   **`model`**: Data models for UI state.
        *   `PanchangModel.kt`, `HoroscopeModel.kt`, `FestivalModel.kt`, `CustomCalendarNote.kt`.

*   **Package: `in.vedicpanchang.app.viewmodel`**
    *   `PanchangViewModel.kt`: Manages state for Home and Day Detail screens.
    *   `HoroscopeViewModel.kt`: Calculates and holds Kundali data and Dasha info.
    *   `CalendarViewModel.kt`: Logic for monthly view and local notes.
    *   `SettingsViewModel.kt`: UI logic for updating user preferences.

*   **Package: `in.vedicpanchang.app.ui`** (Jetpack Compose)
    *   **`home`**: `HomeScreen.kt` and individual cards: `TodayPanchangCard.kt`, `ChoghadiyaCard.kt`, `HoraCard.kt`, `SunMoonCard.kt`, `UpcomingEventsCard.kt`.
    *   **`horoscope`**: `HoroscopeScreen.kt`, `NorthIndianChart.kt`, `SouthIndianChart.kt`, `DashaSection.kt`, `PlanetPositionsTable.kt`.
    *   **`calendar`**: `CalendarScreen.kt` (Monthly view with date picking).
    *   **`daydetail`**: `DayDetailScreen.kt` (Detailed breakdown of a specific day's panchang).
    *   **`settings`**: `SettingsScreen.kt`, `HelpScreen.kt`.
    *   **`navigation`**: `NavGraph.kt` (Routes), `AppBottomNav.kt`.
    *   **`theme`**: `AppTheme.kt`, `AppColors.kt`, `AppTextStyles.kt`.

*   **Package: `in.vedicpanchang.app.service`**
    *   `LocationService.kt`: Handles GPS updates for accurate panchang (lat/long).
    *   `NotificationScheduler.kt`: Uses AlarmManager for daily panchang alerts.
    *   `NotificationService.kt`: Builds and displays the actual notification.
    *   `ShareService.kt`: Logic for sharing panchang/kundali as text or image.
    *   `WidgetService.kt`: Updates the home screen widget.
    *   `HelpContentService.kt`: Provides static help text.

*   **Package: `in.vedicpanchang.app.l10n`** (Localization)
    *   `AppStrings.kt`: Typed access to string resources.
    *   `PanchangLocalizer.kt`: Formats panchang terms and numbers based on locale (English/Hindi/Sanskrit).
    *   `HoroscopeLocalizer.kt`: Localization for zodiac signs and planet names.

*   **Package: `in.vedicpanchang.app.receiver`**
    *   `BootReceiver.kt`: Reschedules alarms after device reboot.
    *   `NotificationReceiver.kt`: Triggers notification display.

*   **Package: `in.vedicpanchang.app.widget`**
    *   `PanchangWidgetProvider.kt`: AppWidgetProvider implementation.

---

## Tech Stack Details
- **Build System**: Gradle 9.4.1, AGP 9.2.1.
- **Dependency Injection**: Hilt (Dagger) with `@HiltViewModel`.
- **UI State**: `StateFlow` in ViewModels, collected as state in Composables.
- **Time**: `kotlinx-datetime` for all date/time logic.
- **Concurrency**: Coroutines for background calculations and database IO.

## Development Workflow
1.  **Specs First**: Check `specs/` for feature requirements.
2.  **Logic Separation**: Never put astronomical math in the `:app` module. Use `:astronomy`.
3.  **Stateless Composables**: Keep UI components pure; pass state down and events up.
4.  **Localization**: All user-facing strings must use `strings.xml` and be handled via `PanchangLocalizer` where needed.
5.  **Cultural Context**: Ensure calculations match traditional Vedic standards (e.g., Ayanamsha, Sunrise definition).

## Assistant Shortcuts
- To modify **Home Screen**, look at `in.vedicpanchang.app.ui.home.HomeScreen`.
- To fix **Panchang Logic**, look at `:astronomy/in.vedicpanchang.astronomy.PanchangCalculators`.
- To change **Colors/Theme**, look at `in.vedicpanchang.app.ui.theme`.
- To update **User Settings**, check `AppPreferences` and `SettingsViewModel`.
