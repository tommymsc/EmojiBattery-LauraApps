# Emoji Battery: DIY Widgets

LauraApps native Android app · Java 17 · Android API 36 · English + Spanish.

## Real features
- Four selectable battery emoji widget themes (Peach Pop, Lavender Dream, Mint Glow, Midnight).
- Real battery percentage and charging status, charging and scheduled widget updates.
- Add-to-home-screen flow through standard Android widget pinning (or launcher Widgets).
- Privacy-first local-only implementation; no network access or tracking.

## Build
GitHub Actions **Android CI** runs Android lint, JUnit tests and generates an **unsigned** Android App Bundle (AAB). The signed-release workflow is manual and requires four repository secrets:
ANDROID_KEYSTORE_BASE64, ANDROID_KEYSTORE_PASSWORD, ANDROID_KEY_ALIAS, ANDROID_KEY_PASSWORD.

**Do not reuse keys from other apps without verifying ownership.** Never commit a keystore or secret. CI success does not equal Google Play approval. Test on physical phones before release.

## ASO
See STORE_LISTINGS.md. 25,000 installs in 90 days is a growth goal, not a guarantee. Avoid claiming custom Android system status-bar replacement because this app provides home-screen widgets only.
