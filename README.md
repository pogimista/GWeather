# GWeather — Android Weather App

A weather app built with **Kotlin**, **Jetpack Compose**, **Clean Architecture**, and **MVVM**, backed by the [OpenWeatherMap](https://openweathermap.org/api) Current Weather Data API.

## ✨ Features

- **Current weather tab** — city & country, temperature in Celsius, "feels like", humidity, wind speed, and sunrise/sunset times (shown in the location's own local time, not the device's).
- **Weather icon logic** — sun ☀️ for clear skies, switching to a moon 🌙 once it's past 6 PM at the location, and a dedicated rain 🌧️ icon (plus icons for clouds, snow, storms, fog, etc.), regardless of time of day.
- **Location detection** — uses the device's GPS/network location (via Android's `LocationManager`, no Google Play Services dependency) with full runtime permission handling: request, deny-and-retry, and permanently-denied → deep link to app settings. Falls back to a default location when permission isn't granted or no fix is available.
- **History tab** — every fetch (e.g. each time the app is opened) is logged and persisted locally, newest first, capped at 30 entries, with its own icon/time snapshot.
- **Configurable API key** — the OpenWeatherMap key is never hardcoded; it's read from `local.properties` (gitignored) or an environment variable at build time.
- **Modern, fluid UI** — swipeable tabs (`HorizontalPager` + `TabRow`), animated state transitions, gradient hero card.

## 🏗 Architecture

The project follows **Clean Architecture** with an **MVVM** presentation layer, organized by feature module:

```
app/src/main/java/com/mista/weather/
├── base/                 # Shared MVVM/navigation scaffolding
│   ├── error/            # AppError sealed type + Throwable -> AppError mapping
│   ├── BaseViewModel.kt, BaseUseCase.kt, BaseFragment.kt, BaseActivity.kt, BaseKey.kt
│   └── NavigationEvent.kt, NavigationAnimation.kt, ScreenResultBus.kt
├── di/                   # App-wide Koin modules (network, session)
├── session/              # SharedPreferences-backed cache/session wrappers
├── ui/                   # Compose theme (colors, typography) and shared components
├── home/                 # The weather feature module
│   ├── data/
│   │   ├── remote/       # Retrofit service, DTOs, API-key interceptor
│   │   ├── location/     # LocationProvider (LocationManager-based)
│   │   ├── WeatherRepository(Impl), WeatherHistoryRepository(Impl), WeatherMapper
│   ├── domain/           # Weather, Coordinates, WeatherHistoryEntry, use cases (pure Kotlin)
│   ├── presentation/     # HomeViewModel, HomeScreen + tabs, icon/time formatting
│   └── di/                # HomeModule (Koin bindings for the feature)
├── App.kt, MainActivity.kt
```

**The flow:** UI (Compose) → ViewModel → Use Case → Repository → Data Source (Retrofit / LocationManager / SharedPreferences).

- **Domain layer** is pure Kotlin — no Android or framework dependencies, so it's trivial to unit test.
- **Data layer** implements the repository contracts defined in domain and handles data sources.
- **Presentation layer** exposes UI state (`StateFlow`) from the `ViewModel`, which Compose observes and renders.

## 🛠 Tech Stack

| Area | Choice |
|------|--------|
| Language | Kotlin |
| UI | Jetpack Compose (Material 3, `HorizontalPager`, animations) |
| Architecture | Clean Architecture + MVVM |
| Build | Gradle (Kotlin DSL) |
| DI | Koin |
| Networking | Retrofit + OkHttp + Moshi |
| Location | Android `LocationManager` (no Play Services required) |
| Navigation | Simple Stack |
| Async | Coroutines / Flow |
| Testing | JUnit4, MockK, kotlinx-coroutines-test |

## 🚀 Getting Started

```bash
# 1. Clone
git clone https://github.com/pogimista/GWeather.git
cd GWeather

# 2. Add your OpenWeatherMap API key (see below)

# 3. Open in Android Studio (latest stable), let Gradle sync

# 4. Run on an emulator or device
./gradlew installDebug
```

**Requirements:** Android Studio (latest stable), JDK 17+, Android SDK (minSdk 24 / compileSdk 36).

### API key setup

The app calls the OpenWeatherMap [Current Weather Data API](https://openweathermap.org/current) and needs a free API key from [openweathermap.org/api](https://openweathermap.org/api). It's injected at build time via `BuildConfig.OPEN_WEATHER_API_KEY` and never committed to source control.

Add it to `local.properties` at the project root (create the file if it doesn't exist — it's gitignored):

```properties
OPEN_WEATHER_API_KEY=your_api_key_here
```

Alternatively, set it as an `OPEN_WEATHER_API_KEY` environment variable (useful for CI).

### Location permission

The app requests `ACCESS_FINE_LOCATION` / `ACCESS_COARSE_LOCATION` at runtime to show local weather. If denied, it falls back to a default location (Reggio Emilia, IT) and shows a banner to re-enable location access from the Current tab.

## ✅ Testing

Unit tests live under `app/src/test/java/com/mista/weather/` and cover the mapper, use cases, repositories, icon/time logic, location-permission gating, and `HomeViewModel`'s state flows, using MockK and hand-rolled fakes for the repository/provider interfaces.

```bash
./gradlew testDebugUnitTest
```

---

Built by **John Christopher B. Mista** — Senior/Lead Mobile Engineer (Android Native · iOS · Flutter).
[LinkedIn](https://www.linkedin.com/in/john-christopher-mista) · [GitHub](https://github.com/pogimista)
