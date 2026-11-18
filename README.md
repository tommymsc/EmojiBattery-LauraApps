# Emoji Battery: DIY Widgets — v1.6.0

LauraApps native Android. Target SDK 36, minimum SDK 26. Two languages: English and Spanish. No background network access, tracking or unnecessary permissions.

## Features
- Twelve originally generated glossy mascot characters, 24 design variants, 1024×768 pixel atlas checked into source.
- Battery percentage and charging status from Android APIs. Battery colors adapt to level and previews remain distinctly labeled.
- Home-screen widget with automatic Android scheduled refresh, battery charging events and immediate refresh while app is visible. Widgets are not continuous animations.
- Optional Android screensaver via DreamService; user must select it in Android Settings and Android decides when it runs. While active, it updates the displayed battery periodically and animates gently without acquiring wake locks.
- Optional **static** lock-screen wallpaper selected with explicit confirmation. Changing the lock-screen background requires Android's normal SET_WALLPAPER permission. The image is a snapshot and does not update automatically. This is not an overlay, lock-screen bypass or system battery indicator replacement.
- All changes process images and battery status on device.

## Test
GitHub Actions runs unit tests, resource parity, HD atlas hash checks, lint and debug APK compilation and signature verification. Manual testing still needed on physical devices from different Android vendors, especially the screensaver settings entry and wallpaper policy support.

**Play Store**: AAB needs the official private upload signing key and Play Console testing and policy declarations. Signed debug APKs are for local test only.
