# 米客 A14｜CustoMIUIzer A14

[简体中文](README.md) | English

System UI and interaction customization for **HyperOS 1 / Android 14 / arm64-v8a**, requiring **libxposed API 102 or later**.

[Download official APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/310-r14.27.9/CustoMIUIzer-A14-r14.27.9.apk) · [Release notes](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/310-r14.27.9) · [Changelog](CHANGELOG.md)

[Telegram community](https://t.me/customiuizerA14)

## Current stable release

`r14.27.9` · versionCode `310` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.27.9.apk` (6,293,117 bytes)

SHA-256: `d2bd1f64612eb2d5b4184a8ee87907cadd902631a6767520b725cf8efc138bfa`

[Source commit](https://github.com/thetvplus/customiuizer-a14/commit/9224160f6b13be43a9dd4a6da8a1ff92a0e795c0): `9224160f6b13be43a9dd4a6da8a1ff92a0e795c0`

## Changes

Changes since r14.26.11:

This summary compares the shipped r14.27.7 implementation. See the [changelog](https://github.com/thetvplus/customiuizer-a14/blob/r14.27.9/CHANGELOG_EN.md#r14279--2026-11-16) for cumulative changes.

- Refined DECK's window coordinates and occlusion when opening a card in landscape. Opening the front card skips unnecessary window clipping.
- The iOS gesture indicator now styles native landscape handles in Launcher and System UI. Native drawing continues while the window size settles.
- Improved Dock blur preparation when returning from Recents and reduced the work Dynamic Island does to update status-bar occlusion.
- Delayed location shutoff keeps the identity of the task that scheduled it so that unrelated task removals cannot interfere with the last-card check.
- Simplified volume auto-hide and notification auto-expand while preserving their settings and behavior.
- Restart Launcher and System UI after upgrading. Reboot Android after first enabling location automation.

## Installation and compatibility

1. Install the official APK; updates with the same signature preserve app data.
2. Enable the module in your Xposed framework and select the required scope.
3. Restart the affected processes or device and follow the settings instructions.

Other Android / HyperOS major versions are unsupported. Do not enable alongside upstream or another CustoMIUIzer fork.

[Published usage guide](https://github.com/thetvplus/customiuizer-a14/blob/9224160f6b13be43a9dd4a6da8a1ff92a0e795c0/README_EN.md) · [Published compatibility matrix](https://github.com/thetvplus/customiuizer-a14/blob/9224160f6b13be43a9dd4a6da8a1ff92a0e795c0/COMPATIBILITY.md) · [Project home](https://github.com/thetvplus/customiuizer-a14/blob/master/README_EN.md)

This repository documents published downloads. Unreleased development changes do not describe this APK or its device acceptance.
