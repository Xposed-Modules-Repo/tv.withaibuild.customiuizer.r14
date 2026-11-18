# 米客 A14｜CustoMIUIzer A14

[简体中文](README.md) | English

System UI and interaction customization for **HyperOS 1 / Android 14 / arm64-v8a**, requiring **libxposed API 102 or later**.

[Download official APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/311-r14.27.10/CustoMIUIzer-A14-r14.27.10.apk) · [Release notes](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/311-r14.27.10) · [Changelog](CHANGELOG.md)

[Telegram community](https://t.me/customiuizerA14)

## Current stable release

`r14.27.10` · versionCode `311` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.27.10.apk` (6,293,125 bytes)

SHA-256: `5468065aea787155300e4f513f861d5d36ca1c5f5f647e61ca2f3ed9f8df697a`

[Source commit](https://github.com/thetvplus/customiuizer-a14/commit/0bb31f1c2912ef9c6f8a8b3715e86f8f0ee51024): `0bb31f1c2912ef9c6f8a8b3715e86f8f0ee51024`

## Changes

Changes since r14.27.9:

- **Control Center**. Fix swapped pull-down gestures and refine custom tile columns and panel layout in portrait and landscape.
- **Status bar**. Dual-row signal indicators refresh the second SIM independently. Network speed follows the system refresh and reset.
- **Volume and brightness**. Percentages follow native slider progress, and the extended Do Not Disturb timer uses the system control.
- **Home screen**. Refine icon titles, folder layout and double-tap handling in the native launcher.

Restart Launcher and System UI after upgrading.

## Installation and compatibility

1. Install the official APK; updates with the same signature preserve app data.
2. Enable the module in your Xposed framework and select the required scope.
3. Restart the affected processes or device and follow the settings instructions.

Other Android / HyperOS major versions are unsupported. Do not enable alongside upstream or another CustoMIUIzer fork.

[Published usage guide](https://github.com/thetvplus/customiuizer-a14/blob/0bb31f1c2912ef9c6f8a8b3715e86f8f0ee51024/README_EN.md) · [Published compatibility matrix](https://github.com/thetvplus/customiuizer-a14/blob/0bb31f1c2912ef9c6f8a8b3715e86f8f0ee51024/COMPATIBILITY.md) · [Project home](https://github.com/thetvplus/customiuizer-a14/blob/master/README_EN.md)

This repository documents published downloads. Unreleased development changes do not describe this APK or its device acceptance.
