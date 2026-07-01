<p align="center">
  <img src="https://raw.githubusercontent.com/zachvlat/gamelibrary/refs/heads/android-native-nonplaynite/app/src/main/play_store_512.png" alt="GameLibrary logo" height="80" />
</p>
<h1 align="center">GameShelf</h1>

<p align="center">
  <img src="https://raw.githubusercontent.com/zachvlat/gamelibrary/refs/heads/android-native-nonplaynite/fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot1.png" alt="main" height="400" />
  <img src="https://raw.githubusercontent.com/zachvlat/gamelibrary/refs/heads/android-native-nonplaynite/fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot2.png" alt="main" height="400" />
  <img src="https://raw.githubusercontent.com/zachvlat/gamelibrary/refs/heads/android-native-nonplaynite/fastlane/metadata/android/en-US/images/phoneScreenshots/Screenshot3.png" alt="main" height="400" />
</p>

<p align="center">
An Android app that aggregates your owned game libraries from Epic Games Store, GOG, and Amazon Gaming into one place.
Built with Jetpack Compose, Ktor, Room, and Material 3 dynamic colors. Auth is handled via in-app WebView popup. Games are cached locally with 7-day TTL.
</p>

## Stores Supported

- **Epic Games** – OAuth via WebView + entitlement API
- **GOG** – OAuth via WebView + Galaxy API
- **Amazon Gaming** – OAuth via WebView + entitlements API (device-auth flow)
- **Steam** - (not working currently)
- **Itch.io** - Scrape via WebView
