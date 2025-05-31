# Polymarket Viewer

Android client application for browsing Polymarket prediction markets. Built with Jetpack Compose and modern Android development practices following Clean Architecture principles.

## Features

*   Browse a list of active prediction market events
*   Filter events by category and search functionality  
*   Sort events by various criteria (Volume 24h/All Time, Liquidity, Newest, Ending Soon, Competitive)
*   View detailed event information with description, markets, and volume data
*   Navigate to individual market details with outcomes and pricing
*   Display interactive price history charts for events
*   Adaptive card layouts for different market types (binary, categorical, multi-market)
*   Comments system with hierarchical structure and holder filtering
*   Pull-to-refresh functionality for updating data
*   Pagination for loading more events and comments
*   Material 3 design with dynamic theming
*   User profile viewing

## Architecture

This project follows **Clean Architecture** principles with clear separation of concerns:

*   **Presentation Layer** (`ui/`) - Jetpack Compose UI with ViewModels and UI states
*   **Domain Layer** (`domain/`) - Use cases and business logic interfaces
*   **Data Layer** (`data/`) - Repositories, data sources, and DTOs
*   **Dependency Injection** (`di/`) - Hilt modules for dependency management

## Technologies

*   **Language**: Kotlin
*   **UI Framework**: Jetpack Compose with Material 3 Design
*   **Architecture**: Clean Architecture with MVVM pattern
*   **Asynchronous Programming**: Kotlin Coroutines & Flow
*   **Networking**: Ktor Client with OkHttp engine
*   **JSON Parsing**: Kotlinx Serialization
*   **Image Loading**: Coil 3 with Ktor integration
*   **Charts**: Vico Charts for interactive data visualization
*   **Dependency Injection**: Hilt (Dagger)
*   **Navigation**: Jetpack Navigation Compose
*   **Data Storage**: DataStore Preferences
*   **State Management**: StateFlow and Compose State

## Project Structure

```
app/src/main/java/com/streamatico/polymarketviewer/
├── data/           # Data layer (repositories, DTOs, network)
├── domain/         # Domain layer (use cases, business logic)
├── ui/             # Presentation layer (Compose UI, ViewModels)
├── di/             # Dependency injection modules
└── MainActivity.kt # Application entry point
```

## Requirements

*   **Android API Level**: 27+ (Android 8.1)
*   **Target SDK**: 35 (Android 15)
*   **Kotlin**: 2.1.21
*   **Java**: 17

## Disclaimer

This is an unofficial, experimental application and is not affiliated with Polymarket. Use at your own discretion.

## Contact

If you have any questions or suggestions, please contact us at: <streamatico+polymarket@gmail.com>.