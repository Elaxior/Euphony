# 🎧 Euphony

Euphony is a modern Android music streaming experience built with **Kotlin** and **Jetpack Compose**. It combines online streaming, smart recommendations, and offline playback into a smooth, responsive app powered by a background playback service.

---

## ✨ Features

* 🎵 **Seamless Playback**

  * Background playback via `PlaybackService`
  * Queue management with shuffle & loop
  * Mini player + full player UI

* 📚 **Library System**

  * Favorites and playlists
  * Queue isolation per collection
  * Clean repository-based architecture

* 📥 **Offline Mode**

  * Download songs for offline listening
  * Progress tracking
  * Local playback with full controls

* 🔍 **Smart Recommendations**

  * “Recommended for You”
  * “Trending Mix”
  * Powered by NewPipe data + custom filtering

* ⚙️ **Optimized Performance**

  * Stable playback service lifecycle
  * Improved reliability across Android versions
  * Efficient state handling using Kotlin Flow

---

## 🏗️ Tech Stack

* **Language:** Kotlin
* **UI:** Jetpack Compose (Material 3)
* **Architecture:** Clean Architecture + MVVM
* **DI:** Custom AppContainer (manual DI)
* **Async:** Coroutines + Flow
* **Media:** ExoPlayer
* **Background Work:** Services + WorkManager
* **Storage:** Room / local persistence
* **Streaming Source:** NewPipe Extractor

---

## 📁 Project Structure

```
com.example.euphony
│
├── data/              # Repositories, remote + local sources
├── domain/            # Models (QueueState, etc.)
├── ui/                # Compose screens & components
├── playback/          # Player + PlaybackService
├── di/                # Dependency container (AppContainer)
```

---

## 🚀 Getting Started

### ✅ Requirements

* Android Studio Flamingo or newer
* Java 17
* Android SDK 34
* Device/emulator with API 24+

---

### ⚙️ Setup

```bash
git clone https://github.com/your-username/euphony.git
cd euphony
```

1. Open in Android Studio
2. Let Gradle sync
3. Ensure `local.properties` contains:

```
sdk.dir=YOUR_ANDROID_SDK_PATH
```

---

### ▶️ Run the App

```bash
./gradlew clean assembleDebug
```

Or simply click **Run ▶️** in Android Studio.

---

## 🧠 Architecture Notes

* **Dependency Injection**

  * Managed via `AppContainer`
  * Provides ViewModels and repositories

* **Playback System**

  * Queue handled via `QueueState`
  * Shared across UI and service layers

* **Offline System**

  * Downloads via `DownloaderImpl`
  * Metadata persisted for playback

---

## 🛠️ Key Components

* `PlaybackService` → Core audio engine
* `LibraryRepository` → Manages playlists & favorites
* `QueueState` → Central playback queue logic
* `DownloaderImpl` → Handles audio fetching

---

## 🔮 Future Improvements

* ✅ Add unit & instrumentation tests
* 🎯 Improve recommendation algorithm
* ⚡ Optimize download performance & caching
* 🎨 Enhance UI animations & polish

---

## ⚠️ Disclaimer

This project uses the **NewPipe Extractor** to fetch publicly available media streams.

* This app is **for educational purposes only**
* It does **not host or own any content**
* Users are responsible for complying with YouTube’s Terms of Service

---
