# AGENTS.md

## Project Snapshot
- Android app (`:app`) for browsing Polymarket events, details, markets, charts, comments, profiles, and a homescreen widget.
- Stack from `app/build.gradle.kts`: Kotlin 2.3, AGP 9.1, Compose + Material3, Navigation 3, Koin, Ktor, DataStore, WorkManager, Glance, Vico charts.
- Architecture follows `README.md`: `ui/` (Compose + ViewModels), `domain/` (repository contracts), `data/` (API clients, DTOs, repository impl), `di/` (Koin modules).

## High-Value Entry Points
- App startup: `app/src/main/java/com/streamatico/polymarketviewer/PolymarketApplication.kt` (Koin init, analytics ping, periodic widget refresh scheduling).
- Root UI + widget deep-link handoff: `app/src/main/java/com/streamatico/polymarketviewer/MainActivity.kt`.
- Navigation graph and screen wiring: `app/src/main/java/com/streamatico/polymarketviewer/ui/navigation/AppNavigation.kt` + `NavKeys.kt`.
- DI graph: `app/src/main/java/com/streamatico/polymarketviewer/di/{AppModule,NetworkModule,DataModule,ViewModelModule}.kt`.

## Data & Service Boundaries
- `PolymarketRepository` (`domain/repository/PolymarketRepository.kt`) is the main app-facing API; ViewModels should depend on this interface.
- `PolymarketRepositoryImpl` maps domain enums to wire params (e.g., sort order -> `volume24hr`, `endDate`) and centralizes `safeApiCall` error wrapping.
- Three Ktor clients are intentionally separated by base URL in `NetworkModule.kt`:
  - Gamma: `https://gamma-api.polymarket.com/` (events, markets, comments, search)
  - CLOB: `https://clob.polymarket.com/` (price history)
  - Data: `https://data-api.polymarket.com/` (positions, leaderboard, activity)
- Some calls intentionally use absolute non-base URLs (e.g., tags/profile in `PolymarketGammaApiClient.kt`). Preserve this behavior unless API contracts change.

## UI/State Patterns To Follow
- ViewModels expose `StateFlow` + sealed UI states (`EventListUiState`, `EventDetailUiState`, `SearchUiState`) and use `viewModelScope` for async work.
- Pagination is offset-based (`PAGE_SIZE = 20` in `EventListViewModel.kt`), with explicit flags for `isRefreshing`, `isLoadingMore`, and `canLoadMore`.
- Watchlist is DataStore-backed and shared via `WatchlistInteractor`; cap is 50 IDs (`UserPreferencesRepository.kt`).
- Search behavior is intentionally debounced (`1000ms`) and ignores queries shorter than 2 chars (`SearchViewModel.kt`).
- Navigation 3 + Koin parameter passing pattern: `koinViewModel<...> { parametersOf(navKey) }` in `AppNavigation.kt`.

## Widget Integration Notes
- Widget opens event details via `MainActivity.EXTRA_EVENT_SLUG` (`EventWidgetActions.kt` -> `MainActivity.kt`).
- `WidgetOpenCoordinator` avoids duplicate reopen loops; behavior is covered by `app/src/test/java/com/streamatico/polymarketviewer/ui/navigation/WidgetOpenCoordinatorTest.kt`.
- Periodic widget refresh runs every 15 minutes (`EventWidgetUpdater.kt`) through `EventWidgetWorker` + `EventWidgetRefresher`.

## Build/Test/Verification Workflows
- Verified locally in this repo:
  - `./gradlew --no-daemon :app:tasks --all`
  - `./gradlew --no-daemon :app:testDebugUnitTest`
- Common module tasks: `:app:assembleDebug`, `:app:installDebug`, `:app:lintDebug`, `:app:connectedDebugAndroidTest`.
- Unit tests run on JUnit Platform (`useJUnitPlatform()` in `app/build.gradle.kts`), so prefer JUnit 5 (`org.junit.jupiter.api.*`) for new local tests.

## Agent Guardrails For This Repo
- Keep changes scoped to one layer when possible: UI-only, repository/API-only, or DI-only.
- If adding a new API call, update all three touchpoints together: client (`data/network`), repository interface (`domain/repository`), repository impl (`data/repository`).
- If adding a new screen route, update `NavKeys.kt`, `AppNavigation.kt`, and ViewModel DI registration in `ViewModelModule.kt`.
- Use existing logging/error style (`Result` + `onSuccess/onFailure` + `Log.e`) rather than introducing a parallel error stack.

