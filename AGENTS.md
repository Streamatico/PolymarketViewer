# Repository Guidelines

## Project and Entry Points

- Single-module Android app (`:app`) for browsing Polymarket events, markets, charts, comments, and profiles, with a watchlist and home screen widgets.
- Stack: Kotlin, Compose + Material3, Navigation 3, Koin, Ktor, DataStore, WorkManager, Glance, and Vico. See `README.md` for the product overview.
- Kotlin source paths below are relative to `app/src/main/java/com/streamatico/polymarketviewer/`.
- Layers: `ui/` (Compose + ViewModels), `domain/` (repository contracts and domain enums), `data/` (API clients, DTOs, repositories, preferences), `di/` (Koin modules), `core/events/` (shared UI events).
- `PolymarketApplication.kt` initializes Koin and schedules periodic widget refresh. `MainActivity.kt` hosts the root UI, tracks app launches, and handles widget intents.
- Navigation: `ui/navigation/AppNavigation.kt` and `NavKeys.kt`. DI: `di/AppModule.kt`, `NetworkModule.kt`, `DataModule.kt`, and `ViewModelModule.kt`.

## Configuration Sources

- Read dependency and plugin versions from `gradle/libs.versions.toml`; do not duplicate version numbers in these instructions.
- Read SDK levels, app version, build types, and test configuration from `app/build.gradle.kts`.
- Use JDK 21. Keep the toolchain in `app/build.gradle.kts`, daemon criteria in `gradle/gradle-daemon-jvm.properties`, and `.github/actions/setup-gradle-java/action.yml` aligned.
- Use the Gradle wrapper; its version and checksum are in `gradle/wrapper/gradle-wrapper.properties`.
- Read behavioral limits from their named constants in the source files identified below.

## Verification

Run commands from the repository root. Choose checks that cover the change:

| Change | Verification |
| --- | --- |
| Application logic, DTOs, or ViewModels | `./gradlew :app:testDebugUnitTest` |
| Android code, resources, or Compose UI | `./gradlew :app:lintDebug :app:assembleDebug`; verify affected UI on a device or emulator |
| Dependencies, build configuration, or release preparation | `./gradlew :app:testDebugUnitTest :app:lintRelease :app:assembleRelease` |
| Instrumented tests | `./gradlew :app:connectedDebugAndroidTest` with a device or emulator connected |
| Documentation or changelog text only | `git diff --check` and inspect the changed text, links, and relevant metadata limits |

- CI is defined in `.github/workflows/android-ci-fdroid.yml`: debug unit tests, release lint, then an unsigned release APK build.
- Local tests in `app/src/test/` use JUnit Jupiter (`org.junit.jupiter.api.*`) on JUnit Platform. Instrumented tests in `app/src/androidTest/` use AndroidJUnit4 / JUnit 4 and Compose test rules.
- Report checks actually run and any remaining validation gaps; an earlier successful run is not validation of the current change.

## Repository and Networking Contracts

- ViewModels access Polymarket through `domain/repository/PolymarketRepository.kt`. `data/repository/PolymarketRepositoryImpl.kt` maps domain enums to wire parameters and wraps API results through `safeApiCall`.
- For a new app-facing Polymarket API call, update the client in `data/network/`, the repository interface, and its implementation together.
- `di/NetworkModule.kt` configures three Polymarket Ktor clients: Gamma (`https://gamma-api.polymarket.com/`) for events, markets, comments, and search; CLOB (`https://clob.polymarket.com/`) for price history; Data (`https://data-api.polymarket.com/`) for positions, leaderboard, and activity.
- All three share `data/network/PolymarketDnsResolver.kt`, which follows the user's DNS/DoH preferences. Preserve this wiring when changing networking.
- Analytics uses a separate client without request logging or caching. Keep it separate from the Polymarket clients.
- Tags and profile calls in `data/network/PolymarketGammaApiClient.kt` intentionally use absolute `polymarket.com` URLs. Preserve them unless the API contract changes.
- Keep the existing `Result`, `onSuccess`/`onFailure`, and `Log.e` error-handling style. For UI errors, reuse `ui/shared/UiError.kt` and `Throwable.toUiError` from `UiErrorMapper.kt`.

## UI, State, and Widget Contracts

- ViewModels expose `StateFlow` with explicit loading/error/content states and use `viewModelScope` for asynchronous work.
- `ui/event_list/EventListViewModel.kt` owns offset pagination (`PAGE_SIZE`), refresh/load-more flags, and request IDs that reject stale success and failure responses. Preserve those guards when changing loading or filters.
- Watchlist state is DataStore-backed and shared through `data/preferences/WatchlistInteractor.kt`. Use `MAX_WATCHLIST_SIZE` from `UserPreferencesRepository.kt`; limit feedback goes through the interactor.
- Search uses `SEARCH_DEBOUNCE_MS` and `MIN_QUERY_LENGTH` in `ui/search_screen/SearchViewModel.kt`. Preserve debounce and short-query behavior.
- App-wide snackbars use `core/events/UiEventBus.kt`, resource-backed `UiText`, and `ui/shared/components/GlobalSnackbarHost.kt`. Reuse this path for transient global messages.
- For a new screen route, update `ui/navigation/NavKeys.kt` and `AppNavigation.kt`; register its ViewModel in `di/ViewModelModule.kt` when needed. Follow the existing `koinViewModel<...> { parametersOf(key) }` pattern for route arguments.
- Widget event handoff uses `MainActivity.EXTRA_EVENT_SLUG` and `ui/navigation/WidgetOpenCoordinator.kt`; preserve the protection against duplicate reopening.
- Keep periodic refresh tied to `EVENT_WIDGET_REFRESH_MINUTES` in `ui/widget/EventWidgetUpdater.kt`, through `EventWidgetWorker` and `EventWidgetRefresher` in `EventWidgetWorker.kt`.
- Widget snapshots in `ui/widget/EventWidgetSnapshotBuilder.kt` use shared market display rows. Keep their outcome ordering consistent with the event detail screen.

## Change Guidelines

- Verify facts against the checkout. Limit refactoring to what is needed for the task, coordinating affected layers when a contract changes.
- Use Koin for dependency injection; changing DI frameworks requires an explicit migration decision.
- Follow existing Material3 components and theming. Hoist state, collect it with lifecycle awareness, use stable lazy-list keys, and preserve accessibility and previews for affected UI.
- Use string resources for new user-facing text, and named constants for behavioral limits. Keep functions focused; explain non-obvious reasoning and contracts rather than restating code in comments.
- When adding or upgrading dependencies, prefer stable releases unless prereleases are explicitly requested. Verify versions and API documentation with Context7 when available, otherwise with official sources; check Kotlin, AGP, Gradle, JDK, and SDK compatibility.

## Release and F-Droid Metadata

- Update `versionName` and increase `versionCode` in `app/build.gradle.kts` together for a new release.
- Add `metadata/en-US/changelogs/<versionCode>.txt`; the filename uses the numeric version code, not the display version name. Preserve previous release notes.
- Keep each changelog within 500 characters, using concise English bullets that describe the actual changes. Do not claim unverified performance or stability improvements.
