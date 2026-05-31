# AGENTS.md

This repository uses **specification-driven development**. Specs are the source of truth for scope, behavior, and acceptance criteria. Agents and contributors must keep specs and implementation in sync.

## Project Structure Overview

The project is organized into the following main directories and modules:

### 1. `:astronomy` (Core Calculation Engine)
A Kotlin Multiplatform module containing pure logic for astronomical calculations. This module must remain free of Android dependencies.

*   **Package: `in.vedicpanchang.astronomy`**
    *   `AstronomyService.kt`: The main facade for all calculations. Aggregates data from various calculators.
    *   `PanchangCalculators.kt`: Logic for Tithi, Nakshatra, Yoga, Karana, and Vara.
    *   `PlanetaryPositions.kt`: Calculates longitudes and latitudes of planets (Sun, Moon, etc.) for a given time and location.
    *   `ChoghadiyaCalculator.kt`: Calculates day and night Choghadiya periods based on sunrise/sunset.
    *   `HoraCalculator.kt`: Calculates planetary hours (Hora) for the day.
    *   `MuhurtaCalculator.kt`: Logic for Rahu Kaal, Gulika Kaal, Yamaganda, and Abhijit Muhurta.
    *   `EclipseCalculator.kt`: Solar and lunar eclipse predictions.
    *   `TimeRange.kt`: Data model representing a start and end time.
    *   `PanchangConstants.kt`: Astronomical constants and planet/sign indices.

### 2. `:app` (Android Application)
Contains the UI (Jetpack Compose), ViewModels, and Android-specific services.

*   **Package: `in.vedicpanchang.app`**
    *   `MainActivity.kt`: Entry point, sets up Compose Navigation and Hilt.
    *   `VedicPanchangApp.kt`: Application class, initializes Hilt.

*   **Package: `in.vedicpanchang.app.data`**
    *   **`datasource`**:
        *   `AppPreferences.kt`: Manages user settings (language, theme, chart style) via DataStore.
        *   `FestivalData.kt`: Static or JSON-based dataset of Hindu festivals.
        *   **`db`**: Room database for calendar notes (`NoteDatabase.kt`, `NoteEntity.kt`, `NoteDao.kt`).
    *   **`model`**: Data models for UI state (`PanchangModel.kt`, `HoroscopeModel.kt`, `FestivalModel.kt`, `CustomCalendarNote.kt`).

*   **Package: `in.vedicpanchang.app.viewmodel`**
    *   `PanchangViewModel.kt`: Manages state for Home and Day Detail screens.
    *   `HoroscopeViewModel.kt`: Calculates and holds Kundali data and Dasha info.
    *   `CalendarViewModel.kt`: Logic for monthly view and local notes.
    *   `SettingsViewModel.kt`: UI logic for updating user preferences.

*   **Package: `in.vedicpanchang.app.ui`** (Jetpack Compose)
    *   **`home`**: `HomeScreen.kt` and cards: `TodayPanchangCard.kt`, `ChoghadiyaCard.kt`, `HoraCard.kt`, `SunMoonCard.kt`, `UpcomingEventsCard.kt`.
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

*   **Package: `in.vedicpanchang.app.l10n`** (Localization)
    *   `AppStrings.kt`: Typed access to string resources.
    *   `PanchangLocalizer.kt`: Formats panchang terms based on locale (English/Hindi/Sanskrit).
    *   `HoroscopeLocalizer.kt`: Localization for zodiac signs and planet names.

*   **Package: `in.vedicpanchang.app.receiver`**
    *   `BootReceiver.kt`: Reschedules alarms after device reboot.
    *   `NotificationReceiver.kt`: Triggers notification display.

### 3. Other Directories
- **images/**: App icons and graphics.
- **keys/**: Keystore and signing files (not for source control).
- **specs/**: Specification documents for all features and changes.
- **gradle/**: Build configuration and version catalogs (`libs.versions.toml`).

This modular structure enforces a clear separation between UI/app logic and core calculation logic, supporting maintainability, testability, and onboarding for new contributors. Each module is responsible for a distinct concern, and all changes are governed by the specification-driven process described below.