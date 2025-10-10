# Viva Questions and Answers

## 1. How does the habit tracker screen let users add, edit, delete habits, and keep daily progress in sync?
**Answer:** `HabitsFragment` wires a `HabitAdapter` with callbacks for incrementing counts, toggling completion, editing, or deleting rows (`HabitsFragment.kt`). When a user taps a card the `onIncrement` lambda advances the count up to its target and persists the update via `Prefs.saveHabits`. The floating action button and focus button both open `showHabitDialog`, which either creates a new `Habit` (with a generated UUID) or mutates the existing one, then refreshes the adapter. Deletions remove the item from the in-memory list, persist the change, and notify the adapter so the list stays in sync.

## 2. How is daily habit completion tracked across days?
**Answer:** Habit entities keep a `completionHistory` map keyed by ISO dates (`Habit.kt`). Whenever a completion toggle changes, `Prefs.recordHabitCompletion` snapshots that day’s state. On load, `Prefs.getHabits` invokes `resetIfNeeded`, which records the previous day’s completion, zeroes out counts, and clears the `completedToday` flag when the stored reset date differs from the current date. The history map powers the monthly visualization in `CalendarFragment`, which pulls the latest habit from storage and feeds `CalendarAdapter` so users can review streaks.

## 3. Why did you choose SharedPreferences for persistence, and how are the models serialized?
**Answer:** The data set is lightweight (habits, moods, and settings), so `Prefs` wraps a single `SharedPreferences` file (`Prefs.kt`). It serializes each list using Gson, mapping to strongly typed data classes (`Habit`, `Mood`, `Settings`). Helper methods sanitize legacy data, seed default habits, and normalize targets, which avoids schema migration overhead while keeping the storage API simple for a coursework project.

## 4. How is navigation structured between the Habits, Mood Journal, Calendar, and Settings screens?
**Answer:** `MainActivity` inflates the navigation graph and binds a `BottomNavigationView` to the `NavController` so the three primary fragments remain the root destinations. Tapping the calendar button on any habit triggers the generated `HabitsFragmentDirections.actionHabitsToCalendar`, pushing `CalendarFragment` onto the stack. Non-root destinations hide the bottom bar via an `OnDestinationChangedListener`, keeping the UI focused on the secondary flow.

## 5. What validation protects habit entries from bad data?
**Answer:** In `showHabitDialog`, the code trims the entered title and bails out if it’s blank. Targets default to at least one repetition, current counts are clamped between zero and the target, and empty emoji entries fall back to `Habit.DEFAULT_EMOJI`. These guards prevent divide-by-zero when computing percentages and ensure the recycler never binds negative or invalid values.

## 6. How are mood entries captured, and why require a note before selecting an emoji?
**Answer:** `MoodFragment` loads existing moods into `MoodHistoryAdapter` and watches the save button. Pressing save validates the note field; only when text is present does it open the emoji picker dialog. Selecting an emoji creates a `Mood` with the current timestamp, the mapped score, and the user’s note, then persists it through `Prefs`. The note-first requirement ensures each entry has context, which improves both the history list and trend analysis.

## 7. How does the emoji selector map to mood scoring?
**Answer:** The picker pulls its options from `EmojiMapper.allEmojis`, which is a deterministic list tied to a score map ranging from -10 (distressed) to +10 (euphoric). When an emoji is chosen, `EmojiMapper.scoreFor` supplies the numeric value that gets stored with the mood entry. That score feeds directly into analytics, so the visual choice doubles as structured data without extra user input.

## 8. What advanced feature did you implement, and how does it reveal trends?
**Answer:** The project includes an MPAndroidChart-powered line chart (`MoodChartFragment`). It iterates over the past seven days, groups moods by day, averages the mapped scores, and plots the result. Axis styling emphasizes the neutral baseline at zero and labels each point with the weekday, giving students a quick read on how their mood has shifted throughout the week.

## 9. How do hydration reminders respect user-configured intervals?
**Answer:** `SettingsFragment` exposes a slider (bounded to a 15-minute minimum) and a notification toggle. When saved, `scheduleOrCancel` enqueues or cancels a unique `WorkManager` periodic task named `hydration_reminder`. The worker (`HydrationWorker`) simply calls `NotificationHelper.showHydrationReminder`, which builds the notification on the shared hydration channel, so reminders recur at the chosen cadence until the user turns them off.

## 10. How is the notification channel prepared, and why is it created in the Application class?
**Answer:** `SerenityApp` (declared in the manifest) registers the “hydration” channel inside `onCreate`, gated by an SDK check. Centralizing channel creation prevents duplicate setup and guarantees it exists before any background worker tries to post a notification, which is required on Android O and above.

## 11. In what ways are user settings preserved across sessions and configuration changes?
**Answer:** All settings writes go through `Prefs.saveSettings`, and screens read the latest copy on start. `MainActivity` picks up the persisted dark-mode flag before inflating the layout, so rotations or process death restore the desired theme automatically. Since WorkManager keeps periodic work alive across restarts, hydration reminders survive configuration changes too once the schedule has been enqueued.

## 12. What techniques make the UI responsive on phones and tablets?
**Answer:** Layouts use `ConstraintLayout` roots, scalable spacing resources (for example `fragment_habits.xml` relies on `@dimen/spacing_*` values), and vector assets so elements reflow cleanly in both portrait and landscape orientations. List screens rely on `RecyclerView` instead of static containers, and Material components such as `MaterialToolbar`, `MaterialCardView`, and `Slider` inherit adaptive styles, which keeps touch targets and typography appropriate across different screen widths.
