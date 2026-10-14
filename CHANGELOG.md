# Changelog

English | [简体中文](CHANGELOG_CN.md)

Retained releases and withdrawal notices are listed. Each section covers changes since the previous retained release; Git preserves the detailed history.

## r14.23.1 — 2026-10-14

HyperOS 1 / Android 14, versionCode 235. Local JVM 25 trial only; the published release is build 233, which replaced the withdrawn build 232 of the same version. Changes since r14.22.7:

- Align Java source/target and Kotlin JVM output at 25 for local device evaluation after user feedback of no apparent issues with the JVM 21 trial (build 234). Retain the JDK 25 build runtime and existing compiler, R8 and runtime dependencies. JVM 25 device acceptance and performance measurements remain pending.

- Fix blank settings pages and the blank screen on returning from About by preserving page position and alpha while collecting animators; retain the pre-upgrade back behavior. Add Android 14 UI-model regression tests for all four categories and their settings pages, return and recreation; no HyperOS device acceptance is recorded.
- Add an independent Duo charging-bolt switch, enabled by default; percentage and charging colour remain independent.
- Refine silent and DND shortcut colours while preserving the native appearance and animation; restore original colours when custom styling is disabled.
- Fix navigation long presses, key consumption and Control Center gesture cleanup. Restart the phone when first enabling navigation long presses.
- Remove ineffective A14 features and old settings, refine native network-speed and international-ROM options, and improve settings-sync error handling with fewer startup-diagnostics objects.
- Update build tooling and Fragment dependencies, use public AGP APIs for generated tasks, consolidate APK tools and remove test-only production sources and duplicate configuration while retaining resource compression and Hook compatibility boundaries.

## r14.22.7 — 2026-10-09

HyperOS 1 / Android 14, versionCode 230. Consolidates the r14.22 series since r14.21.9.

- Add optional Duo battery, Wi-Fi and cellular icons, disabled by default, with lock screen, Control Center, double rows and size adjustments; refine network text, headphone hints, automatic sizing, handoffs and signal recovery.
- Preserve fatal errors in resource hooks, theme merging, logging and settings initialization; let the native Control Center icon manager tint custom text, avoiding conflicting wallpaper callbacks and duplicate listeners.
- Copy wallpaper and drawer-blur callback arguments only when rewriting is needed, reduce repeated thread-state reads and compute only selected temperatures. Preserve original calls, input ownership and exception propagation.
- Check backup CRC over the original byte slice; reduce search and locale-list intermediates while preserving backup format, matching and ordering.
- Use lossless WebP for the bundled QR image and Android 14-compatible compact resource entries; remove unused JVM coroutine-agent payload while retaining dynamic resources and Hook/JNI rules.
- Pin libxposed 102 to official Maven Central with SHA-256 verification and align Fragment KTX with Fragment 1.5.4; include validated dependency and build-tool security updates, retaining DexKit 2.2.0, the API 101 minimum runtime, JDK 25 and JVM 17 output.
- Coordinate Fast and Full CI while retaining mutation gates, clean builds without caches, and APK/R8 mapping reproducibility checks; remove unused resources and alias placeholders, obsolete API checks and resource qualifiers; strengthen signing/version checks and consolidate maintenance documentation and release history.

## r14.21.9 — 2026-09-30

versionCode 218.

- Coalesce Control Center step refreshes to prevent slow-query backlogs; cancel obsolete work on screen-off, view removal or controller replacement, preserving serial queries and recovery.

## r14.21.8 — 2026-09-15

versionCode 217.

- Optimize app-lock and privacy-list ordering and search, preserving selected-first order and dual-user identity.
- Initialize icon cache keys and reuse shortcut metadata, reducing repeated reads and list copies during scrolling, search and result publication.

## r14.21.7 — 2026-09-11

versionCode 216.

- Fix left-side mobile/Wi-Fi icons and add left-icon size and vertical-offset controls.
- Fix Control Center settings transitions; cover standalone pages, combined route queries, Wi-Fi/WIFI matching and result navigation in search.
- Remove duplicate whole-row switch feedback and update the About maintainer to thetvplus.

## r14.21.5 — 2026-09-06

versionCode 212.

- Refine settings; fix notification channel navigation, importance settings on both page variants, and clipping of the final extended-menu item.

## r14.20.9 — 2026-09-05

versionCode 206.

- Defer system_server business features until the preference snapshot is ready; retain the Hook catalog across rapid SystemUI restarts.
- Isolate WindowManager argument-type errors and scope status-bar resource overrides to the system, SystemUI and launcher; system_server still owns window geometry.

## r14.20.8 — 2026-08-19

versionCode 205.

- Fix settings and global actions not taking effect after initial setup; improve action synchronization and owner lifecycle.
- Correct hotspot, DND, dark-mode and media-action labels.

## r14.20.7 — 2026-08-19

versionCode 204. Includes r14.20.6 changes since the previous retained release.

- Improve CPU/battery temperature sources, left-side temperature placement, default text sizing and vertical positioning without clipping.
- Fix folder drag blur, wallpaper/transition zoom and recents gesture blur; clarify launcher and recents switch scope.
- Fix extended-action pickers, MultiAction persistence/execution and lock-screen label/value mappings; launcher gestures restart both launcher and SystemUI.
- Improve Dynamic Island dismissal and device-info refresh; reduce repeated gesture allocations and reflection, refreshing preference snapshots without reinstalling hooks.

## r14.20.0 — 2026-08-17

versionCode 198. Starting point of the retained release history.

- Provide status-bar, notification, lock-screen, Control Center, launcher-gesture and global-action customization; add Dynamic Island, default USB mode, recents label hiding and blur controls.
- Organize settings and About; use generated lazy settings pages, global search and typed V2 backups with legacy migration and restore validation.
- Route features by process and preferences; strengthen lifecycle, failure boundaries and status-bar/window geometry synchronization, using static scope and Git revision build provenance.
