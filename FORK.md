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

Build `1.0.6-ramachetan.4` adds a personal racing appearance: a navy timing panel with a bold italic clock, red/yellow accents, and a narrower glass dock. Dock cards are 14% smaller in this mode, with at most six columns so the preferred app row stays intact. All apps remain available in the expanded grid. The appearance can be switched off in Settings → Display → Racing style, restoring the previous clock, accent, and dock sizing. Wallpaper and app-order preferences are not changed by this toggle.

Build `1.0.6-ramachetan.5` refines Racing style into a minimal motorsport layout: a white Barlow Condensed Bold Italic clock with a thin red underline, no team label or clock panel, and a translucent charcoal dock with white focus outlines. Dock cards are 24% smaller than the classic style and spacing gains 8dp. A small down arrow replaces cropped second-row tiles; D-pad Down still opens all apps. The existing date format setting controls the optional date below the clock. The toggle still restores the classic appearance. Barlow is bundled offline under the SIL Open Font License; its copyright and complete license are packaged in `app/src/main/assets/licenses/BarlowCondensed-OFL.txt`. Font source: https://github.com/google/fonts/tree/main/ofl/barlowcondensed.

Build `1.0.6-ramachetan.6` adds a Mac-style icon dock in Racing style: the first six ordered apps appear as compact native icons, with focus magnification and a name above the selected icon. A separated All apps button opens the full launchable-app grid, including apps removed from sections (hidden-app preferences still apply). Back restores focus to the tile that opened the drawer. Launching from the drawer closes it so Home returns to the dock. The existing app order controls the favorites; long-press still opens the app menu. The classic banner dock remains available by turning Racing style off. Icons are cached during the existing app scan, including for apps that also provide TV banners.

Build `1.0.6-ramachetan.7` reduces compact dock icons by about 24%, trims the glass padding, and uses a thinner 1.25dp focus ring with gentler 12% magnification. App ordering and drawer behavior are unchanged.
