# 米客 A14｜CustoMIUIzer A14

[简体中文](README.md) | English

System UI and interaction customization for **HyperOS 1 / Android 14 / arm64-v8a**, requiring **libxposed API 102 or later**.

[Download official APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/265-r14.26.5/CustoMIUIzer-A14-r14.26.5.apk) · [Release notes](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/265-r14.26.5) · [Changelog](CHANGELOG.md)

[Telegram community](https://t.me/customiuizerA14)

## Current stable release

`r14.26.5` · versionCode `265` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.26.5.apk` (5,979,909 bytes)

SHA-256: `37d0a445199083f4ff56ab3562c0200c5ba4db03dda3d2abbef90b5dc05581ca`

[Anonymized source snapshot](https://github.com/thetvplus/customiuizer-a14/commit/313defbd7b1f50892d3cbd48e72f228844ded5d5): `313defbd7b1f50892d3cbd48e72f228844ded5d5`

## Changes

Changes since r14.25.9:

- Migrate the settings interface to Compose with consistent lists, search, selectors, sliders, color and action editors, and state feedback. Preserve settings keys, backups and feature behavior. Group app behavior, Wi-Fi and VPN automation under “System → Apps & automation”.
- Keep home search collapsed by default, with a dedicated view, automatic focus and highlighted matches. Fix retained focus and keyboard flashing across back navigation and background recovery. App selectors use prepared rows directly when the query is empty, avoiding unnecessary background dispatch and empty-list handoffs.
- Use consistent neutral light / dark pages, toolbars and cards, with Material 3 dynamic colors for buttons, switches and sliders. Match system bars to the page background, align overflow typography and colors, add a restrained restart-submenu transition, and clear pressed feedback as pages or menus leave so a dark click state does not travel with them or linger.
- Fix “null” titles in language, Settings-entry and other selection dialogs. The shared title fallback also covers text and multi-selection editors. Use 16sp primary text for choices, with system font scaling and clearer contrast.
- Select the current numeric value when manual input opens, leave space above the keyboard, and allow long dialog content to scroll. Cancel continues to discard edits.
- Add the missing section heading for Apps & automation and align settings grouping. Restore native edge stretching and use motion-aware text rendering to reduce glyph and spacing jumps during scrolling, rebound and navigation.
- Restore native Dock blur opacity after hiding or locking, and stop resubmitting identical opacity in a stable state.
- Add “System → Apps & automation → Wi-Fi automatic settings”: use SSID, optional BSSID and priority to control Do Not Disturb, ringer mode, auto rotation and VPN. Restore only owned state on exit and respect observed manual changes. Grant the network-identity permissions and restart the device after first enabling.
- Add “System → Apps & automation → App behavior”: enable sensor rotation, media-volume memory, fixed or system-controlled brightness, and limits on or suppression of app brightness requests for selected apps, with exact Activity exceptions. Each option is off by default; restart the device after first enabling.
- Remove Dock “Light glass” while retaining Solid panel and Frosted glass. Retired selections fall back to the panel. Remove its gradient rim, extra drawing and blur / power observers while preserving layout, editing, dragging and unlock behavior. Restart Launcher after updating.
- Retain bounded Wi-Fi setting restoration records after a failed scene exit until readback confirms success; respect manual changes and user switches. Recover network tracking after overflow through a complete trusted snapshot, and prepare rotation and brightness Hook interfaces independently. Fix app-behavior and Wi-Fi changes remaining stale on frameworks that cache old settings.
- Restore smoother Island waveforms with display updates synchronized to screen frames and the earlier music / recorder sampling cadence. Preserve hidden-state capture release, measured-silence / stable-state frame suspension, pause and power-saving guards. Fix lightweight waveforms remaining frozen after system animations are re-enabled.
- Maintain Island opacity only for occluded nodes, skip repeated occlusion queries when geometry is unchanged, and tighten the texture budget for full transition surfaces. Preserve native gestures and animation handoffs.
- Stop weather-specific subscriptions and queries when no clock uses weather content, and skip updates for a hidden second clock.
- Coalesce duplicate status-bar network-speed mirror deliveries and update charging-text layout only when content or layout conditions change. Avoid unrelated brightness reads and argument-array copies in touch handling.
- Clear stale recording activity levels after silence and reset the previous envelope after a failed read instead of retaining an incorrect peak.

## Installation and compatibility

1. Install the official APK; updates with the same signature preserve app data.
2. Enable the module in your Xposed framework and select the required scope.
3. Restart the affected processes or device and follow the settings instructions.

Other Android / HyperOS major versions are unsupported. Do not enable alongside upstream or another CustoMIUIzer fork.

[Usage and support](https://github.com/thetvplus/customiuizer-a14/tree/313defbd7b1f50892d3cbd48e72f228844ded5d5) · [Compatibility](https://github.com/thetvplus/customiuizer-a14/blob/313defbd7b1f50892d3cbd48e72f228844ded5d5/COMPATIBILITY.md)

This repository documents published downloads. Unreleased development changes do not describe this APK or its device acceptance.

Original build source identifier: `2037b6da9195fbf406113ded89698a3812be81d2`. The signed APK and original metadata are unchanged; the snapshot consolidates history and redacts private documentation.
