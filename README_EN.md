# 米客 A14｜CustoMIUIzer A14

[简体中文](README.md) | English

System UI and interaction customization for **HyperOS 1 / Android 14 / arm64-v8a**, requiring **libxposed API 102 or later**.

[Download official APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/303-r14.27.3/CustoMIUIzer-A14-r14.27.3.apk) · [Release notes](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/303-r14.27.3) · [Changelog](CHANGELOG.md)

[Telegram community](https://t.me/customiuizerA14)

## Current stable release

`r14.27.3` · versionCode `303` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.27.3.apk` (6,160,184 bytes)

SHA-256: `91b95b19990c8f037a5a7038f45503f5c278521d6e3d5ce006727856c720d4dc`

[Source commit](https://github.com/thetvplus/customiuizer-a14/commit/680c6c39de7d54416f7b9169b2dc2b360f72b839): `680c6c39de7d54416f7b9169b2dc2b360f72b839`

## Changes

Changes since r14.27.1:

- Refine DECK transitions in both orientations, fix card jumps and shadow flashes, and keep quick app switching free of deck scaling.
- Refine island arrival and content transitions, and bring long-island text closer to the camera-safe edge.
- Soften Home Indicator color transitions, make feature switches easier to find, and simplify Duo and related settings.
- Restart the launcher and System UI after updating. DECK requires Horizontal Recents.

## Installation and compatibility

1. Install the official APK; updates with the same signature preserve app data.
2. Enable the module in your Xposed framework and select the required scope.
3. Restart the affected processes or device and follow the settings instructions.

Other Android / HyperOS major versions are unsupported. Do not enable alongside upstream or another CustoMIUIzer fork.

[Usage and support](https://github.com/thetvplus/customiuizer-a14/tree/680c6c39de7d54416f7b9169b2dc2b360f72b839) · [Compatibility](https://github.com/thetvplus/customiuizer-a14/blob/680c6c39de7d54416f7b9169b2dc2b360f72b839/COMPATIBILITY.md)

This repository documents published downloads. Unreleased development changes do not describe this APK or its device acceptance.
