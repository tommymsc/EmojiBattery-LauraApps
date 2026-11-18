# Emoji Battery: DIY Widgets

LauraApps Android, Java 17, target API 36, English and Spanish.

Version 1.3.0 contains twelve original mascot species and two color variants per species, giving 24 selectable designs. Each widget uses the real battery level and changes its illustrated expression at low, medium, high or charging states. The app includes category filters, preview levels, a quick Add Widget button and local-only theme storage.

Existing home-screen widgets retain their selected design independently. To add another design, choose it in the gallery then tap Add Widget beside the preview. No accounts, Internet, ads, analytics or invasive permissions are required.

Home-screen widgets update on Android's scheduled intervals (30 minutes or later, at the system's discretion), on supported charging events, and when the app is opened. The app itself listens to battery events while visible. No continuously animated status-bar replacement is claimed.

The Android CI workflow runs unit tests, Android Lint, resource validation, builds an unsigned AAB and a debug APK, and checks APK signature/package metadata. The debug APK is for device testing only. Play releases require private upload-key secrets; never commit a keystore.
