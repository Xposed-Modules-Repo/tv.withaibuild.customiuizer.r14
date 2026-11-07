# 米客 A14｜CustoMIUIzer A14

[简体中文](README.md) | English

System UI and interaction customization for **HyperOS 1 / Android 14 / arm64-v8a**, requiring **libxposed API 102 or later**.

[Download official APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/304-r14.27.3/CustoMIUIzer-A14-r14.27.3.apk) · [Release notes](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/304-r14.27.3) · [Changelog](CHANGELOG.md)

[Telegram community](https://t.me/customiuizerA14)

## Current stable release

`r14.27.3` · versionCode `304` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.27.3.apk` (6,160,184 bytes)

SHA-256: `1650c02a2f7ca654b87148a6d40f01356f04b10591609e802061f5da303bb149`

[Source commit](https://github.com/thetvplus/customiuizer-a14/commit/5169eb1c537d639c78fdd84eefdd479f066c4982): `5169eb1c537d639c78fdd84eefdd479f066c4982`

## Changes

Changes since r14.26.11:

- Add independently enabled iOS DECK task stacking for portrait and landscape on supported launchers.
- Refine DECK scaling, shadows and task reopening, and fix enlarged cards during quick app switching.
- Add the iOS home indicator with smoother black/white transitions and an easier-to-find switch.
- Add the iOS volume bar and an optional narrow default, with touch expansion, overscroll feedback and full controls.
- Refine island resizing, content transitions and app returns. Restore outer-aligned long-island captions while keeping camera clearance.
- Update Duo icons, fonts and layout, and simplify settings descriptions while preserving search and upgrades.
- Fix notification prompts, Control Center feedback, volume buttons and restoration of older settings pages.
- Restart the launcher and System UI after updating. DECK requires horizontal Recents; the home indicator requires gesture navigation and a visible system handle.

## Installation and compatibility

1. Install the official APK; updates with the same signature preserve app data.
2. Enable the module in your Xposed framework and select the required scope.
3. Restart the affected processes or device and follow the settings instructions.

Other Android / HyperOS major versions are unsupported. Do not enable alongside upstream or another CustoMIUIzer fork.

[Usage and support](https://github.com/thetvplus/customiuizer-a14/tree/5169eb1c537d639c78fdd84eefdd479f066c4982) · [Compatibility](https://github.com/thetvplus/customiuizer-a14/blob/5169eb1c537d639c78fdd84eefdd479f066c4982/COMPATIBILITY.md)

This repository documents published downloads. Unreleased development changes do not describe this APK or its device acceptance.
