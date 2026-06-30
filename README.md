# GameLibrary

An Android app that aggregates your owned game libraries from Epic Games Store, GOG, and Amazon Gaming into one place.

Built with Jetpack Compose, Ktor, Room, and Material 3 dynamic colors. Auth is handled via in-app WebView popup. Games are cached locally with 7-day TTL.

## Stores Supported

- **Epic Games** – OAuth via WebView + entitlement API
- **GOG** – OAuth via WebView + Galaxy API
- **Amazon Gaming** – OAuth via WebView + entitlements API (device-auth flow)
- **Steam** - (not working currently)
