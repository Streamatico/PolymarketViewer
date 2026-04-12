# Repository Guidelines

## Project Snapshot
- Android app (`:app`) for browsing Polymarket events, details, markets, charts, comments, and profiles, plus home screen widget support.
- Stack from `app/build.gradle.kts`: Kotlin 2.3, AGP 9.1, Compose + Material3, Navigation 3, Koin, Ktor, DataStore, WorkManager, Glance, Vico charts.
- Architecture follows `README.md`: `ui/` (Compose + ViewModels), `domain/` (repository contracts), `data/` (API clients, DTOs, repository impl), `di/` (Koin modules).

## High-Value Entry Points
- App startup: `app/src/main/java/com/streamatico/polymarketviewer/PolymarketApplication.kt` (Koin init, analytics ping, periodic widget refresh scheduling).
- Root UI + widget deep-link handoff: `app/src/main/java/com/streamatico/polymarketviewer/MainActivity.kt`.
- Navigation graph and screen wiring: `app/src/main/java/com/streamatico/polymarketviewer/ui/navigation/AppNavigation.kt` and `NavKeys.kt`.
- DI graph: `app/src/main/java/com/streamatico/polymarketviewer/di/{AppModule,NetworkModule,DataModule,ViewModelModule}.kt`.

## Repository and Networking Boundaries
- `PolymarketRepository` (`domain/repository/PolymarketRepository.kt`) is the main app-facing API; ViewModels should depend on this interface.
- `PolymarketRepositoryImpl` maps domain enums to wire params (e.g., sort order -> `volume24hr`, `endDate`) and centralizes `safeApiCall` error wrapping.
- Three Ktor clients are intentionally separated by base URL in `NetworkModule.kt`:
  - Gamma: `https://gamma-api.polymarket.com/` (events, markets, comments, search)
  - CLOB: `https://clob.polymarket.com/` (price history)
  - Data: `https://data-api.polymarket.com/` (positions, leaderboard, activity)
- Some calls intentionally use absolute non-base URLs (e.g., tags/profile in `PolymarketGammaApiClient.kt`). Preserve this behavior unless API contracts change.

## UI and State Patterns
- ViewModels expose `StateFlow` + sealed UI states (`EventListUiState`, `EventDetailUiState`, `SearchUiState`) and use `viewModelScope` for async work.
- Pagination is offset-based (`PAGE_SIZE = 20` in `EventListViewModel.kt`), with explicit flags for `isRefreshing`, `isLoadingMore`, and `canLoadMore`.
- Watchlist is DataStore-backed and shared via `WatchlistInteractor`; cap is 50 IDs (`UserPreferencesRepository.kt`).
- Search behavior is intentionally debounced (`1000ms`) and ignores queries shorter than 2 chars (`SearchViewModel.kt`).
- Navigation 3 + Koin parameter passing pattern: `koinViewModel<...> { parametersOf(navKey) }` in `AppNavigation.kt`.

## Widget Contracts

- Widget deep-link event handoff uses `MainActivity.EXTRA_EVENT_SLUG`.
- `WidgetOpenCoordinator` should continue preventing duplicate reopen loops.
- Periodic refresh contract remains 15 minutes via `EventWidgetUpdater` -> `EventWidgetWorker`/`EventWidgetRefresher`.

## Build/Test/Verification Workflows
- Verified locally in this repo:
  - `./gradlew --no-daemon :app:tasks --all`
  - `./gradlew --no-daemon :app:testDebugUnitTest`
- Common module tasks: `:app:assembleDebug`, `:app:installDebug`, `:app:lintDebug`, `:app:connectedDebugAndroidTest`.
- Unit tests run on JUnit Platform (`useJUnitPlatform()` in `app/build.gradle.kts`), so prefer JUnit 5 (`org.junit.jupiter.api.*`) for new local tests.

## Agent Guardrails For This Repo
- Verify facts from repository context before asserting them.
- Prefer keeping changes within one layer when feasible.
- If behavior depends on cross-layer contracts, apply coordinated multi-file updates across all affected layers.
- If adding a new API call, update all three touchpoints together: client (`data/network`), repository interface (`domain/repository`), repository impl (`data/repository`).
- If adding a new screen route, update `NavKeys.kt`, `AppNavigation.kt`, and ViewModel DI registration in `ViewModelModule.kt`.
- Use existing logging/error style (`Result` + `onSuccess/onFailure` + `Log.e`) rather than introducing a parallel error stack.

# Coding Standards & Style Guide

## Core Code Quality

- Prefer named constants over magic numbers.
- Use meaningful, descriptive names for classes, functions, and variables.
- Keep functions focused and small (single responsibility).
- Apply DRY: extract repeated logic to shared abstractions.
- Keep code organized by feature/layer with consistent file and folder naming.
- Hide implementation details behind clear interfaces.
- Refactor continuously and leave touched code cleaner than before.

## Comments and Documentation

- Prefer self-documenting code; avoid comments that restate obvious behavior.
- Use comments to explain non-obvious reasoning, side effects, and complex tradeoffs.
- Document public APIs and complex algorithmic decisions.

## Compose and Android Practices

- Follow Material 3 components and theming patterns already used in this project.
- Compose performance:
    - minimize unnecessary recompositions,
    - use stable keys for lazy lists,
    - avoid avoidable state churn,
    - keep lifecycle-aware async collection.
- Compose UI:
    - use proper state hoisting,
    - apply `remember`/`derivedStateOf` intentionally,
    - maintain loading/error/content UI states,
    - keep accessibility and preview support in mind.

## Dependency Injection Standard

- Use Koin for dependency injection.
- Do not introduce Hilt in this repository unless there is an explicit migration decision.

## Dependency Versioning and Docs

- Default to the latest stable (release/GA) dependency versions; avoid alpha/beta/rc/snapshot versions unless explicitly requested.
- Before suggesting or upgrading dependency versions, verify current stable versions and API docs using Context7 when available.
- Keep dependency changes compatible with existing project constraints (for example Kotlin/AGP/SDK compatibility and current Gradle setup).
