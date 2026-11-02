# Ramachetan's Couchy fork

Based on Couchy Launcher 1.0.6, under the upstream GPL-3.0 license.

This fork adds an optional **Couchy Home button** accessibility service for Google TV devices that ignore the HOME role. It observes only window-state changes from Google TV's exact main Home activity and opens Couchy. It cannot read screen content or filter keys. It leaves Google TV's stock launcher, setup, casting, and other system packages enabled. After three failed activity launches, redirects stop until Couchy is manually opened successfully.

The Google TV setup wizard links to Accessibility settings instead of suggesting that system packages be disabled. Enable **Couchy Home button** there. If a sideload restriction appears, use App info → Allow restricted settings before enabling it.

To undo, turn off **Couchy Home button** in Android Accessibility settings or uninstall Couchy. If you also assigned the HOME role, restore it with:

```sh
adb shell cmd package set-home-activity com.google.android.apps.tv.launcherx/.home.HomeActivity
```

Do not follow the upstream README's package-disabling fallback on the Google TV Streamer. No package needs to be disabled for this fork's redirect.

Build instructions are in CONTRIBUTING.md. Preserve the signing key used for your installed build so later builds can update it without clearing settings.

Build `1.0.6-ramachetan.2` restores the previous app row once: SmartTube, YouTube Music, YouTube, BrowseHere, Downloader, Google Play Store. It retains wallpaper and other preferences, and respects subsequent manual reordering. Package-change handling also refreshes tiles when an app is disabled.

Build `1.0.6-ramachetan.3` adds local-video import for Google TV firmware without a document picker. Copy a video to the app's external files directory, then explicitly apply it:

```sh
adb shell mkdir -p /sdcard/Android/data/com.conreo.couchytv/files
adb push YOUR_VIDEO.mp4 /sdcard/Android/data/com.conreo.couchytv/files/wallpaper.mp4
adb shell am start -n com.conreo.couchytv/.MainActivity -a com.conreo.couchytv.APPLY_LOCAL_VIDEO
```

Only that fixed local file is accepted. Playback uses the existing muted, looping video wallpaper, which releases its decoder when another app opens. No media is bundled with this fork. To switch it off, choose a gradient or photo in Couchy's wallpaper settings.
