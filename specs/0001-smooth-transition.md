# Spec: Smooth & Flutter-Like Kotlin Android Application
Status: Implemented
Owners: RJ

## Goal
Deliver polished, smooth transitions across primary navigation flows so the app feels fluid and “Flutter-like” without changing feature scope.

## Non-goals
- Redesign screens or change visual style.
- Introduce new navigation destinations or data features.
- Add heavy motion graphics or complex, long-running animations.
- Re-architect the navigation stack.

## Context
The app is built with Jetpack Compose and uses a Navigation Compose `NavHost` with routes for Home, Calendar, Day Detail, Horoscope, Settings, and Help. Screen changes are currently abrupt, which makes the app feel less refined. We need consistent, subtle transitions aligned with Android/Material motion while keeping performance stable.

## Design

### Motion principles
- **Subtle and short:** transitions should complete in ~200–300ms.
- **Directional:** forward navigation slides in from the right, back navigation slides out to the right.
- **Consistent:** use a small set of shared durations and easings.
- **Respect system settings:** animations must respect system animator duration scale (when disabled, transitions are immediate).

### Motion tokens
Create a small shared motion config (e.g., `ui/animation/MotionTokens.kt`) with:
- Durations: `short = 200ms`, `medium = 260ms`.
- Easing: `FastOutSlowInEasing` (or equivalent Material easing).
- Shared alpha values for fades (0.0 → 1.0).

### Navigation transitions
Implement transitions in the navigation layer so screens do not need to re-implement animation logic.

**Bottom navigation (Home / Calendar / Horoscope / Settings):**
- Use a **crossfade + slight slide** on screen change.
- Direction is based on the tab order in `AppBottomNav`.
- No animation when reselecting the current tab.

**Detail navigation:**
- **Calendar → Day Detail:** slide in from right + fade in.
- **Back from Day Detail:** slide out to right + fade out.

**Settings → Help:**
- Same horizontal slide + fade pattern as Day Detail.

### Implementation approach
Prefer built-in Compose animation APIs (no new dependencies unless needed). Options:
- Use a single animated container around the `NavHost` with `AnimatedContent` keyed on route.
- Or use navigation animation APIs if already available in the current `navigation-compose` version.

If a new dependency is required, add it to the version catalog and keep it minimal.

### Performance considerations
- Avoid long or chained animations.
- Ensure animations do not trigger heavy recomposition; keep transitions at the navigation level.
- Verify no visible layout jumps when content loads.

## Acceptance criteria
1. Switching between bottom navigation tabs animates with a consistent crossfade + slight slide, completing within ~260ms.
2. Navigating from Calendar to Day Detail animates forward (slide-in from right + fade-in).
3. Back navigation from Day Detail animates backward (slide-out to right + fade-out).
4. Navigating from Settings to Help uses the same forward/back animation pattern.
5. When system animations are disabled (Animator duration scale = 0), transitions are immediate.
6. No new runtime crashes or navigation regressions.

## Test plan
1. Run unit tests: `.\gradlew.bat test`.
2. Manual verification on a device/emulator:
   - Switch tabs in bottom navigation and confirm smooth crossfade + slight slide.
   - Open Day Detail from Calendar and verify forward/back animations.
   - Open Help from Settings and verify forward/back animations.
3. Set Animator duration scale to 0 in Developer Options and verify transitions are immediate.

## Rollout / compatibility
- No database or data migration.
- Backward compatible with existing routes and state restoration.
- No feature flag required; behavior aligns with existing UI expectations.

## Decision log
- 2026-05-28: Define a shared, lightweight motion system and apply it at the navigation layer for consistent transitions.
- 2026-05-28: Spec approved for implementation.
- 2026-05-28: Implementation completed and validated.
