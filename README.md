# Emoji Battery: DIY Widgets — v1.8.0 — mascot expansion

LauraApps native Android. Target SDK 36, minimum SDK 26. Two languages: English and Spanish. No background network access, tracking or unnecessary permissions.

## Features
- New optional adaptive mascot mode: different original character for low / medium / high battery and while charging. Users can switch back to a fixed favorite. The gallery changes the mode to fixed when a mascot is selected. Battery levels refresh under Android's normal widget timing, not continuously.

- Twelve original atlas mascots plus twenty-one individually packaged transparent WebP artworks (14 kittens and 7 puppies, 512×512). Seven palettes yield 231 character/color combinations. The new artworks keep fixed facial expressions while battery level, status and colors update.
- Battery percentage and charging status from Android APIs. Battery colors adapt to level and previews remain distinctly labeled.
- Home-screen widget with automatic Android scheduled refresh, battery charging events and immediate refresh while app is visible. Widgets are not continuous animations.
- Optional Android screensaver via DreamService; user must select it in Android Settings and Android decides when it runs. While active, it updates the displayed battery periodically and animates gently without acquiring wake locks.
- Optional **static** lock-screen wallpaper selected with explicit confirmation. Changing the lock-screen background requires Android's normal SET_WALLPAPER permission. The image is a snapshot and does not update automatically. This is not an overlay, lock-screen bypass or system battery indicator replacement.
- All changes process images and battery status on device.

## Test
GitHub Actions runs unit tests, resource parity, HD atlas hash checks, lint and debug APK compilation and signature verification. Manual testing still needed on physical devices from different Android vendors, especially the screensaver settings entry and wallpaper policy support.

**Play Store**: AAB needs the official private upload signing key and Play Console testing and policy declarations. Signed debug APKs are for local test only.

## Google Play compliance review (2026-11-18)
- Android 16 (API 36) target satisfies the current mobile submission target requirement; minimum API 26 is an app compatibility choice, not a Play minimum.
- Only the `SET_WALLPAPER` manifest permission is requested. The screensaver service uses the system-required `BIND_DREAM_SERVICE` service protection and is opt-in through Android Settings.
- Full English/Spanish privacy policy is readable inside the app; a static web version is stored in `docs/privacy.html`. **Enable GitHub Pages** with main branch /docs (or host this HTML at another verified public URL), then enter that verified URL in Play Console. Do not submit an unverified URL.
- Google Play Data safety: validate the actual signed AAB and all bundled libraries. This source version has no Internet permission, advertising SDK, analytics SDK or user data transmission, but declarations must reflect the real published build.
- Build checks reject unexpected permissions and native `.so` libraries so that 16 KB device compatibility cannot silently regress. If native libraries are intentionally added in the future, update tests and verify 16 KB support.
- Do not claim live/animated battery information on the system lock screen; only the home-screen widget and active Android screensaver update. Lock-screen wallpaper is static.
- Review target audience, content rating, store listing screenshots, legal ownership of assets, privacy contact and developer/trader profile in Play Console. Review in physical Android 8–16 devices and App Bundle Explorer before submitting.

## Mascot expansion
- `app/src/main/assets/mascots/`: 21 individual WebP artworks. `mascot-manifest.json` records permanent IDs, sizes and Git hashes.
- The Kittens and Puppies galleries supports every individual artwork, with seven palettes. Preview, widgets, screensaver and static wallpaper share BatteryArt rendering.
- Existing IDs 0–83 are preserved. New IDs start at 1000; append future mascots to the catalog without reordering existing entries. Each new mascot reserves seven consecutive palette IDs.
- All art is packaged locally; the decoded-image cache is bounded to 5 MiB. No network permissions were added.
