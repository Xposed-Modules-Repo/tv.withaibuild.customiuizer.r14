# 米客 A14｜CustoMIUIzer A14

[简体中文](README.md) | English

System UI and interaction customization for **HyperOS 1 / Android 14 / arm64-v8a**, requiring **libxposed API 102 or later**.

[Download official APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/301-r14.27.1/CustoMIUIzer-A14-r14.27.1.apk) · [Release notes](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/301-r14.27.1) · [Changelog](CHANGELOG.md)

[Telegram community](https://t.me/customiuizerA14)

## Current stable release

`r14.27.1` · versionCode `301` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.27.1.apk` (6,148,040 bytes)

SHA-256: `defc3fdbc48f6998e1ea4fcd43cd0027656d6fe077ed9b90fda0bda34e4b5f68`

[Source commit](https://github.com/thetvplus/customiuizer-a14/commit/5c0cfc833d7863d3a6ae2473baaf27b19e6bd2f4): `5c0cfc833d7863d3a6ae2473baaf27b19e6bd2f4`

## Changes

Changes since r14.26.11:

- Add an opt-in iOS recents deck for Horizontal Recents on supported HyperOS 1 launchers, in portrait and landscape. Preserve native scrolling, task dismissal and window handoff.
- Add card sizing, independently scaled titles and icons with depth-based fading, and optional window motion. Select between two native window implementations by their interfaces instead of exact version numbers, including the older POCO spring interface; retain native timing when optional motion interfaces are unavailable.
- Fix card depth during dragging, dismissal, cancellation and return, window-to-card corner alignment, and premature card recycling. Add directional fling snapping; unsupported layouts restore native hit testing, input and scroll bounds.
- Add an opt-in iOS gesture indicator using native visibility, color and input. Admission now follows native drawing capabilities rather than device or build lists. Enable full-screen gestures, keep the system handle visible, and restart System UI.
- Add an iOS volume appearance with portrait and landscape support, continuous corners and native shadows. The thin bar moves toward the screen edge, stays thin during repeated key presses and expands for touch adjustment. Preserve native placement, volume steps, routing, mute state and expanded controls.
- Add “Start with thin bar” to appear thin and return to thin after touch adjustment. The footer transitions from three dots to one; fix the first tap taking focus instead of opening expanded controls.
- Follow native volume-fill animation and remove dark edge overpainting. Add maximum/minimum key feedback, touch overdrag and release recoil, with smoothly rounded thin-bar ends.
- Refine island resizing, idle forms, content changes and press feedback, including mid-animation reversals. Shorten activity handoff waits, retain existing content until the next activity makes contact, and add lateral shake and recoil when restoring ringing.
- Refine absorption entry and island acceptance. Remove the liquid dent, translucent leading edge and extra vertical compression, and avoid exciting the entire island again during acceptance. Preserve native return trajectories, springs, recall and picture-in-picture handoff.
- Prevent top cutout reservations from inflating automatic height and the camera mask. Restore the music island’s final exit timing to reduce translucent residue, and improve ordinary Toast fitting at larger font sizes and shorter island heights.
- Prioritize the actual function or mode in system feedback and show secondary state when space permits. Reveal text through its available area without an extra font-size pulse. Same-notification updates retain visible content while invalidating old click actions and continuing native privacy checks.
- Catch up initial foreground state for live activities; keep native foreground-change observation when singleton lookup is unavailable. Yield to native alarm/timer ringing in landscape and restore the island afterwards. Complete status-bar unbinding even if controller cleanup fails.
- Update Duo cellular, Wi-Fi, airplane and headphone arrangements, percentage/lightning spacing and four-dot arc symmetry. Preserve existing size, offset, spacing and headphone-scale settings. Handle small HD/roaming status icons by available fields, skip missing icons locally, and remove the unnecessary region restriction.
- Use a licensed rounded substitute for Duo numbers and network labels; make network-label weight follow the existing bold switch. Refine automatic size, central Wi-Fi proportions and state transitions.
- Avoid duplicate state messages after Control Center automatically collapses for a tile click, while preserving native feedback on failure. Keep the hide-mute/DND options effective after native volume-footer refreshes.
- Fix empty pages when restoring old settings categories and route volume and Recents options to their current pages. Simplify some feature names and complete translations while preserving preference keys, search, backups and upgrade compatibility.

## Installation and compatibility

1. Install the official APK; updates with the same signature preserve app data.
2. Enable the module in your Xposed framework and select the required scope.
3. Restart the affected processes or device and follow the settings instructions.

Other Android / HyperOS major versions are unsupported. Do not enable alongside upstream or another CustoMIUIzer fork.

[Usage and support](https://github.com/thetvplus/customiuizer-a14/tree/5c0cfc833d7863d3a6ae2473baaf27b19e6bd2f4) · [Compatibility](https://github.com/thetvplus/customiuizer-a14/blob/5c0cfc833d7863d3a6ae2473baaf27b19e6bd2f4/COMPATIBILITY.md)

This repository documents published downloads. Unreleased development changes do not describe this APK or its device acceptance.
