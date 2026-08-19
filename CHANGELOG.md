# Changelog

English | [简体中文](CHANGELOG_CN.md)

## r14.20.8 — 2026-08-19

Targeting HyperOS 1 / Android 14 (SDK 34), `arm64-v8a`, and libxposed API 101/102.

### Fixes

- Fix display names for hotspot, Do Not Disturb, and night mode actions.
- Fix display names for play/pause, previous, and next media actions.

### Runtime and Lifecycle

- Improve runtime availability of global actions in system services and SystemUI.
- Improve runtime synchronization and host lifecycle handling for action configuration.

### Artifact Information

- APK: `CustoMIUIzer-A14-r14.20.8.apk`
- Size: `3882830` bytes
- SHA-256: `25E9B5EA763419843E9EE78E579BC8C43B00382DF6BFCED38893793709BC7911`
- versionCode / versionName: `205 / r14.20.8`

---

## r14.20.2 — 2026-08-17

Targeting HyperOS 1 / Android 14 (SDK 34), `arm64-v8a`, and libxposed API 101/102.

### Status Bar

- Device temperature now uses separate CPU and battery sources, with more compatible CPU thermal-zone parsing.
- In dual-row mode, temperature moves to the left when “show on the right” is off.
- Default font size is kept when it fits. If a custom height or vertical offset leaves too little room, text shrinks instead of being clipped.
- Status-bar contents can be moved vertically without leaving the status-bar window.

### Fixes

- Dynamic Island upward recall is more reliable.
- Device-info updates no longer depend on network-speed controller slots, so temperature text can still appear without that controller.

### Stability and Compatibility

- Custom status-bar height, dual-row layout, and vertical offset keep text and system icons inside the status-bar window.

### Artifact Information

- APK: `CustoMIUIzer-A14-r14.20.2.apk`
- Size: `3844538` bytes
- SHA-256: `64D4241AB6F8F6970EEBDCF9D871F366FC035D2399FC706EFE3166F05F62BD48`
- versionCode / versionName: `199 / r14.20.2`

---

## r14.20.0 — 2026-08-17

Targeting HyperOS 1 / Android 14 (SDK 34), `arm64-v8a`, and libxposed API 101/102.

### New Features

- Dynamic Island mode for the HyperOS status capsule: the ROM forehead is reshaped in place, aligned to the camera cutout, and can be fine-tuned with a signed vertical offset. Custom status-bar height is no longer a hard clip. Status-bar contents fade with platform alpha while the island is visible, consecutive events do not flash the icons back, and the ROM animation is kept.
- USB default purpose: follow system default, charge only, file transfer (MTP), or photo transfer (PTP).
- Recents cards can hide app names.
- The keyboard dismiss button can be hidden in gesture navigation.
- Folder background blur and window-level blur (volume panel, power menu, and similar surfaces) can be disabled.
- Volume-panel Do Not Disturb / mute shortcuts can be hidden, and mode-button colors can be customized.
- System location and notification permission prompts can be dismissed without granting access.
- Exclusive options: disable Xiaomi updater services with exact restore, clear update state, disable MIUI daemon, trim daemon network components, disable Xiaomi analytics, trim Security Center marketing components, and remove the antivirus entry.

### Settings and Interface

- The home page is regrouped into Mods and Settings; the interface language moved from About to home Settings.
- About is now a dedicated page with author and project information.
- Secondary categories, search, and long text continue to follow feature semantics, with clearer soft-reboot notes.

### Backup and Restore

- Backups use a typed V2 format and can still restore older backups.
- Restore validates structure, sanitizes app selections, rolls back a failed commit, and reconciles language / launcher-icon state.
- Only settings still valid in this version are backed up and restored. Removed features and unrecognized old keys are ignored as compatibility cleanup, not treated as a corrupt backup. The old “disable status capsule” switch migrates to the new presentation mode.

### Fixes

- Volume percentage is placed below the live status-bar bottom and follows custom status-bar height in real time.
- The status-bar digital-signal font-size slider now defaults to the system value.

### Stability and Compatibility

- The module now uses a static Xposed scope and only exposes currently supported Hook targets. After enabling it, confirm that LSPosed scope includes `system`, the launcher, and the other required apps.
- Features install by process and preference, with tighter lifecycle, failure boundaries, and runtime status-bar height synchronization.

### Performance

- Status-bar, battery, clock, icon, and notification hot paths do less repeated resolution and allocation. Disabled features no longer create unrelated Hooks.

### Artifact Information

- APK: `CustoMIUIzer-A14-r14.20.0.apk`
- Size: `3805054` bytes
- SHA-256: `AD9A8B86CFC5C55A0AB1D6EE1AE5EB3316897838A39B667F44154D219215722B`
- versionCode / versionName: `198 / r14.20.0`

---

## r14.18.6 — 2026-08-09

### Settings and Interaction

- Settings pages are generated by category and loaded on demand, while a build-generated index preserves global search without initializing unrelated Preferences.
- Click-animation residue from parent pages is removed when opening subpages. The Various section is reorganized into clearer categories and entry points.
- A rapid close/restart race that could briefly report the module as inactive is fixed; the UI remains in a waiting state while the LSPosed service connects.
- Input-method entry points use ROM-neutral wording, while Gboard-only portrait and landscape bottom-padding controls remain explicitly labeled.

### Performance and Lifecycle

- Full-screen Launcher gestures, lock-screen charging hints, and Security Center dock callbacks no longer rescan caller stacks on frequent paths; bounded process-local caller state replaces repeated stack and allocation work.
- AudioVisualizer and battery-indicator observers now have explicit owner, replacement, stale-state, and release paths, preventing duplicate registration and retention of short-lived objects.
- Feature setup metrics are limited to development builds, and overbroad R8 keep rules are narrowed without adding release hot-path overhead.

### Compatibility and Fixes

- Imported app selections discard packages that are uninstalled, disabled, or no longer resolvable. Invalid apps are neither selected nor counted, so summary counts match the selector contents.
- Custom status-bar height now updates WindowInsets and app-window geometry instead of moving icons alone. A full reboot is required after changing the fixed height.
- HyperOS status-capsule controls cover charging, silent mode, and Do Not Disturb. Hide mode is device-verified; match-height mode applies after a full reboot, with corner-radius synchronization reserved for a later update.

### Verification

- Complete A14 gates, formal Release/R8 build, version, v2 signature, zip alignment, SDK, ABI, `debuggable=false`, and Xposed entry checks passed.
- Testing on fuxi / HyperOS 1 confirms hide mode and post-reboot height matching; the remaining known limitation is the unmatched corner radius in match-height mode.

### Artifact Information

- APK: `CustoMIUIzer-A14-r14.18.6.apk`
- Size: `3353458` bytes
- SHA-256: `398658DBCA2C43F6ADCD1F5A905BA6E0FA028A63AA741C740669886904D4A823`
- versionCode / versionName: `196 / r14.18.6`

## r14.18.2 — 2026-08-08

- Improved compatibility and fail-closed behavior for notification and lockscreen hooks.
- Fixed charging-info font size being reset after lockscreen indication updates.
- Simplified several system lifecycle hooks.
- Improved fatal-error handling during hook installation.
- Passed signed Release checks and device smoke validation.

### Artifact Information

- APK: `CustoMIUIzer-A14-r14.18.2.apk`
- Size: `3468849` bytes
- SHA-256: `77F868590C631271251991EDEBF066919460E2F1DA955EFDC10271207EAF3E77`
- versionCode / versionName: `195 / r14.18.2`

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
