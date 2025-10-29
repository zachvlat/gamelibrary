# Game Library

Is an app to store your games from multiple sources that you have on Playnite and exported through [Playnite JSON](https://github.com/zachvlat/playnite-json)

[<img src="https://raw.githubusercontent.com/jiangtian616/JHenTai/master/badges/get_it_on_obtainium.png" alt="Get it on Obtainium" height="60">](https://apps.obtainium.imranr.dev/redirect?r=obtainium://app/%7B%22id%22%3A%22com.zachvlat.gamelibrary%22%2C%22url%22%3A%22https%3A%2F%2Fgithub.com%2Fzachvlat%2Fgamelibrary%22%2C%22author%22%3A%22zachvlat%22%2C%22name%22%3A%22Game%20Library%22%2C%22preferredApkIndex%22%3A0%2C%22additionalSettings%22%3A%22%7B%5C%22includePrereleases%5C%22%3Atrue%2C%5C%22fallbackToOlderReleases%5C%22%3Atrue%2C%5C%22filterReleaseTitlesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22filterReleaseNotesByRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22verifyLatestTag%5C%22%3Afalse%2C%5C%22sortMethodChoice%5C%22%3A%5C%22date%5C%22%2C%5C%22useLatestAssetDateAsReleaseDate%5C%22%3Afalse%2C%5C%22releaseTitleAsVersion%5C%22%3Afalse%2C%5C%22trackOnly%5C%22%3Afalse%2C%5C%22versionExtractionRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22matchGroupToUse%5C%22%3A%5C%22%5C%22%2C%5C%22versionDetection%5C%22%3Atrue%2C%5C%22releaseDateAsVersion%5C%22%3Afalse%2C%5C%22useVersionCodeAsOSVersion%5C%22%3Afalse%2C%5C%22apkFilterRegEx%5C%22%3A%5C%22%5C%22%2C%5C%22invertAPKFilter%5C%22%3Afalse%2C%5C%22autoApkFilterByArch%5C%22%3Atrue%2C%5C%22appName%5C%22%3A%5C%22%5C%22%2C%5C%22appAuthor%5C%22%3A%5C%22%5C%22%2C%5C%22shizukuPretendToBeGooglePlay%5C%22%3Afalse%2C%5C%22allowInsecure%5C%22%3Afalse%2C%5C%22exemptFromBackgroundUpdates%5C%22%3Afalse%2C%5C%22skipUpdateNotifications%5C%22%3Afalse%2C%5C%22about%5C%22%3A%5C%22%5C%22%2C%5C%22refreshBeforeDownload%5C%22%3Afalse%2C%5C%22includeZips%5C%22%3Afalse%2C%5C%22zippedApkFilterRegEx%5C%22%3A%5C%22%5C%22%7D%22%2C%22overrideSource%22%3Anull%7D)

## Building

To build the application, clone the repository and run `npm install`.

```
git clone https://github.com/zachvlat/gamelibrary.git
cd gamelibrary
npm install
```

## Usage

To run the application, run `npm start`.

```
npx expo start
```

## Screenshots

Here are some screenshots of the application:

### Homepage

<img src="screenshots/homepage.png" alt="Homepage" width="200"/>

### Drawer

<img src="screenshots/drawer.png" alt="Drawer" width="200"/>

### List

<img src="screenshots/list.png" alt="List" width="200"/>

# [Guide] Exporting Your Game Library

This guide will walk you through the process of exporting your game library from Playnite into a JSON format.

## Step 1: Download the Library Exporter Extension

1. **Download the Library Exporter Extension**: 
   - Go to the following link: [playnite-json](https://github.com/zachvlat/playnite-json).
   - Install the plugin from the Releases.

## Step 2: Export Your Game Library

1. Take the generated games_export.json
2. Import the json file into your app.
