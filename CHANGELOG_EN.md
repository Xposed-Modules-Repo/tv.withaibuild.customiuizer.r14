# Changelog

[简体中文](CHANGELOG.md) | English

## r14.16.1 — 2026-08-01

- Installs module features by target process and preference state, so disabled features do not create unrelated Hooks, Receivers, Observers, or tasks, and a process cannot install the same feature repeatedly.
- Fixes early preference snapshots, failed-install state, and reflection cache boundaries, reducing the risk of ignored toggles, duplicate installation, and unbounded cache growth.
- Completes release paths for Receivers, Observers, weather, step counter, album art, battery indicator, and overlay Views, reducing retained Contexts, Views, and Bitmaps.
- Shared Hook and callback boundaries continue to isolate ordinary compatibility failures without swallowing `OutOfMemoryError`.
- Optimizes network speed, charging hints, navigation icons, battery indicators, and pass-through Hook hot paths to reduce temporary objects and repeated updates.
- Preference switches show the target state immediately before the existing save and restart-notice logic, improving feedback for rapid taps.
- Module-load logs include the version and short Git SHA. API 102-only Hook capabilities remain isolated from production paths.

### Artifact and Verification

- APK: `CustoMIUIzer-A14-r14.16.1.apk`
- Size: `3369409` bytes
- SHA-256: `F213BA3F939FAA7BD12150D75A538529E9517D9CE865B6611C7A3C93C8370258`
- Signing certificate SHA-256: `C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70`
- versionCode / versionName: `192 / r14.16.1`
- Passed the complete offline gates, Release/R8 build, v2 signature, zip alignment, `debuggable=false`, SDK, and Xposed metadata checks.
- New changes have not completed per-feature device behavior verification.

### Historical Core Implementation Summary

The r14 line established an independent package, signing identity, and HyperOS 1 / Android 14 maintenance path; completed Kotlin migration, one-APK libxposed API 101/102 compatibility, `system` scope restoration, preference synchronization, lifecycle governance, reflection and resource cache hardening, and ongoing status-bar, Launcher, lock-screen, control-center, and settings UI improvements. Fine-grained history remains in the source repository's Git history and old tags, while obsolete APKs are no longer retained as Release assets.
