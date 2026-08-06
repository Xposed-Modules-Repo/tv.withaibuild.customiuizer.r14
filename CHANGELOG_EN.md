# Changelog

[简体中文](CHANGELOG.md) | English

## r14.18.0 — 2026-08-06

Targeting HyperOS 1 / Android 14 (SDK 34), `arm64-v8a`, and libxposed API 101/102.

### Core Changes

- Added adjustable lock-screen charging text size; the default keeps the system text size and changes apply after restarting SystemUI.
- Hardened charging-info initialization and hot paths. Disabled details skip unnecessary work, reducing duplicate installation, invalid reads, and fallback overhead.
- Fixed a possible SystemUI crash when status-bar battery or temperature information is enabled, and hardened stale Handlers, detached Views, ROM field fallbacks, and custom-icon creation.
- Fixed left-side custom status-bar text icons becoming invisible on dark backgrounds by completing tint registration, initial synchronization, recreation, and release lifecycles.
- Added status-bar height synchronization with WindowInsets and the SystemUI window, including runtime application and restoration of the system height when disabled; no-reboot fuxi switching still awaits device verification.
- Hardened status-bar and control-center gesture, View, callback, and ClassLoader lifecycles to reduce duplicate effects, state conflicts, and stale-object retention.
- Optimized process routing, feature install deduplication, and disabled-feature initialization. Ordinary failures remain isolated while fatal errors continue to propagate.
- Added Git revision and APK provenance records, with feature semantics, Python gates, unit tests, and lint integrated into the unified verification flow.

### Verification Status

- `python tools/verify.py full`, feature-semantics validation, source-hazard scanning, CI portability checks, and the full Python suite pass.
- All 405 Python tool tests pass, together with Android JVM unit tests and `lintDebug`.
- The no-reboot `44 → 40 → 12 → 44 → disabled` status-bar-height sequence has not yet been run on fuxi, so this release is not claimed as fully `DEVICE_VERIFIED`.

### Artifact Information

- APK: `CustoMIUIzer-A14-r14.18.0.apk`
- Size: `3436081` bytes
- SHA-256: `31D839BDE68749D16FC13FC426B3B4975E84A29F9910326D33BBE00815FE9953`
- versionCode / versionName: `193 / r14.18.0`
- Passed the complete offline gates, Release/R8 build, v2 signature, zip alignment, `debuggable=false`, SDK, and Xposed metadata checks.

## r14.16.1 — 2026-08-01

- Installs module features by target process and preference state, so disabled features do not create unrelated Hooks, Receivers, Observers, or tasks, and a process cannot install the same feature repeatedly.
- Fixes early preference snapshots, failed-install state, and reflection cache boundaries, reducing the risk of ignored toggles, duplicate installation, and unbounded cache growth.
- Completes release paths for Receivers, Observers, weather, step counter, album art, battery indicator, and overlay Views, reducing retained Contexts, Views, and Bitmaps.
- Shared Hook and callback boundaries continue to isolate ordinary compatibility failures without swallowing `OutOfMemoryError`.
- Optimizes network speed, charging hints, navigation icons, battery indicators, and pass-through Hook hot paths to reduce temporary objects and repeated updates.
- Preference switches show the target state immediately before the existing save and restart-notice logic, improving feedback for rapid taps.
- Module-load logs include the version and short Git SHA. API 102-only Hook capabilities remain isolated from production paths.

### Artifact Information

- APK: `CustoMIUIzer-A14-r14.16.1.apk`
- Size: `3369409` bytes
- SHA-256: `F213BA3F939FAA7BD12150D75A538529E9517D9CE865B6611C7A3C93C8370258`
- versionCode / versionName: `192 / r14.16.1`
- Passed the complete offline gates, Release/R8 build, v2 signature, zip alignment, `debuggable=false`, SDK, and Xposed metadata checks.

### Historical Core Implementation Summary

The r14 line established an independent package, signing identity, and HyperOS 1 / Android 14 maintenance path; completed Kotlin migration, one-APK libxposed API 101/102 compatibility, `system` scope restoration, preference synchronization, lifecycle governance, reflection and resource cache hardening, and ongoing status-bar, Launcher, lock-screen, control-center, and settings UI improvements. Fine-grained history remains in the source repository's Git history and old tags, while obsolete APKs are no longer retained as Release assets.
