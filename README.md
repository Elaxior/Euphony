# Euphony

Euphony is an Android music experience built with Jetpack Compose and Kotlin. It integrates streaming, curated recommendations, and offline playback while coordinating with a playback service to handle background audio, looping, and shuffle behavior.

## Getting Started

### Requirements
- Android Studio Flamingo+ or higher (for Kotlin/Compose tooling)
- Java 17
- Android SDK 34 in `local.properties`
- Device/emulator running API 24+ (Android 7.0) – compatibility extends through Android 16 via the fixes in `app/src/main`

### Setup
1. Clone the repo and open it in Android Studio.
2. Let Gradle sync to download dependencies from Google, Maven Central, and JitPack.
3. Configure `local.properties` if not already set (e.g., `sdk.dir=...`).

### Running
```bash
./gradlew clean assembleDebug
```
Use the usual Run command from Android Studio once the project build succeeds.

## Features
- **Playback service** with `PlaybackService`, mini player, queue handling, and improved loop/shuffle controls.
- **Library** enabling favorites and playlists backed by the `LibraryRepository` use cases, with queue isolation per collection.
- **Offline mode** for downloading songs, tracking progress, and playing locally cached tracks with shuffle/loop support.
- **Recommendations** ("Recommended for You" and "Trending Mix") leveraging NewPipe data plus app-specific filtering for better results.
- **Android 16 optimizations** addressing playback reliability, loop behavior, and service lifecycle stability on API level 16 devices.

## Architecture Notes
- Dependency injection is handled in `com.example.euphony.di.AppContainer`. `LibraryViewModel` and playback repositories are provided there.
- Playback queues live in `com.example.euphony.domain.model.QueueState` and are exposed through the player and queue repositories consumed in Compose screens.
- Downloads use `com.example.euphony.data.remote.DownloaderImpl` via NewPipe to fetch audio; offline playback relies on persisted metadata accessible to the player.

## Libraries & Tools
- Jetpack Compose (Material 3 + Navigation)
- Kotlin coroutines + Flow for state updates
- NewPipe extractor for fetching online audio items
- WorkManager/Service for downloads and background playback

## Next Steps
1. Add instrumentation/unit tests to validate playback queuing and offline flows.
2. Expand recommendation filtering logic in `app` modules (see placeholder `RECOMMENDATIONS_IMPROVEMENTS.md` if restored).
3. Tune download caching/performance for large libraries.

Feel free to extend the README further with specific screen walkthroughs or API notes once additional features land.
