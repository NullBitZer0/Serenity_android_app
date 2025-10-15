# Serenity – MAD Lab

## Table of Contents
- [Overview](#overview)
- [Features](#features)
- [Architecture](#architecture)
- [Tech Stack](#tech-stack)
- [Project Layout](#project-layout)
- [Build & Run](#build--run)
- [Testing](#testing)
- [Data & Persistence](#data--persistence)
- [Hydration Reminders](#hydration-reminders)
- [Resources](#resources)

## Overview
Serenity is an Android wellness companion that helps students build sustainable habits, log moods with meaningful context, and stay hydrated through gentle reminders. The app focuses on fast daily input, lightweight analytics, and an accessible UI that adapts to light and dark themes.

## Features
- **Habit tracker** – Create custom habits with emoji badges, target counts, and auto-resetting daily progress. View completion streaks in a full-month calendar.
- **Mood journaling** – Capture how you feel using an emoji picker and required reflection note. Filter history by day and share a weekly summary with friends.
- **Mood analytics** – Visualize the trailing 7-day average in a smooth MPAndroidChart line graph to spot trends at a glance.
- **Hydration reminders** – Toggle WorkManager-powered notifications, choose the interval (15–240 minutes), and preview reminders instantly in debug builds.
- **Personalization** – Switch between light and dark themes, with BottomNavigation icons that adapt to the active screen. Layouts are optimized for both portrait and landscape.

## Architecture
- Single-activity app (`MainActivity`) hosting a `NavHostFragment` with three primary destinations: Habits, Mood, and Settings.
- UI built with Fragments + Material components; lists handled through RecyclerView adapters.
- Navigation component + Safe Args manage type-safe movement between screens (e.g., Habit calendar drill-down).
- WorkManager handles background scheduling for hydration reminders.
- SharedPreferences (via `Prefs`) persists habits, moods, settings, and completion history using Gson serialization.

## Tech Stack
- Kotlin + Android Gradle Plugin 8.12
- AndroidX Core, AppCompat, Activity, ConstraintLayout, RecyclerView
- Material Components 1.13
- Navigation Component 2.8 with Safe Args
- WorkManager 2.9 for periodic reminders
- Gson 2.11 for simple JSON persistence
- MPAndroidChart 3.1 for line chart visualization
- AndroidX Splash Screen and Edge-to-Edge APIs
- MikePenz Android-Iconics for FontAwesome vector icons

## Project Layout
```
mad_lab/
├─ app/
│  ├─ src/main/java/com/example/serenity_mad_le3/
│  │  ├─ ui/…             # Habits, Mood, Settings, Calendar, Chart fragments & adapters
│  │  ├─ data/Prefs.kt    # SharedPreferences wrapper + seeding + migrations
│  │  ├─ model/…          # Habit, Mood, Settings data classes
│  │  ├─ util/…           # Emoji scoring, notification helper
│  │  └─ workers/…        # HydrationWorker (WorkManager)
│  ├─ src/main/res/…      # Material layouts, theming, navigation graph
│  └─ build.gradle.kts
├─ gradle/                # Version catalog, wrapper
├─ gradlew / gradlew.bat
└─ JAVA_ENVIRONMENT_SETUP.md / scripts
```

## Build & Run
1. Install Android Studio Ladybug (or newer) with Android SDK 36, Build Tools 36, and Android Emulator (API 24+).
2. Ensure a Java 11 toolchain is available (the project configures `sourceCompatibility` + `jvmTarget` 11).
3. Clone or open the project in Android Studio and let Gradle sync.
4. Choose an emulator or physical device running Android 7.0 (API 24) or higher.
5. Press *Run* or execute `./gradlew installDebug` from the project root.

### Command-line build
```bash
# from mad_lab/
./gradlew clean assembleDebug
```

## Testing
- Unit tests: `./gradlew test`
- Instrumented tests (if emulator/device connected): `./gradlew connectedAndroidTest`
- WorkManager reminders can be smoke-tested by enabling notifications in debug builds, which queues a 10-second one-off reminder.

## Data & Persistence
- Habits, mood entries, and settings live in a single SharedPreferences file (`serenity_prefs`) serialized via Gson.
- Daily habit resets persist the previous day’s completion state so the calendar stays accurate.
- Mood entries are timestamped (UTC) and include both score and note for reliable analytics.

## Hydration Reminders
- Notification channel `hydration` registers at app start (`SerenityApp`).
- Settings screen enqueues unique periodic WorkManager tasks (`hydration_reminder`) and requests runtime `POST_NOTIFICATIONS` permission on Android 13+.
- Notifications deep-link back into `MainActivity` so users can update progress immediately.

## Resources
- `JAVA_ENVIRONMENT_SETUP.md` – Repository instructions for configuring Java on Windows.
- `fix_java_environment.ps1` / `set_java_env.bat` – Helper scripts for Android Studio tooling.

