# 米客 A14｜CustoMIUIzer A14

[简体中文](README.md) | English

System UI and interaction customization for **HyperOS 1 / Android 14 / arm64-v8a**, requiring **libxposed API 102 or later**.

[Download official APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/305-r14.27.5/CustoMIUIzer-A14-r14.27.5.apk) · [Release notes](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/305-r14.27.5) · [Changelog](CHANGELOG.md)

[Telegram community](https://t.me/customiuizerA14)

## Current stable release

`r14.27.5` · versionCode `305` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.27.5.apk` (6,265,729 bytes)

SHA-256: `99bb4e3b2094d49b6f90bb8c3c719bdf5b5fd78d16e78815a74f06439dbb0645`

[Source commit](https://github.com/thetvplus/customiuizer-a14/commit/1a64b974b732105837adf6d7f8f9d3756ec12205): `1a64b974b732105837adf6d7f8f9d3756ec12205`

## Changes

Changes since r14.27.3:

- Add an optional iOS 12 central volume HUD with vertical adjustment and automatic hiding of the mute and Do Not Disturb shortcuts. Volume percentages can appear in an overlay, a toast capsule or the Dynamic Island.
- Add optional iOS battery, five-dot cellular signal and notification-shade media styles.
- Improve foreground card exits, refill and window shadow handoff in DECK, and restore the Dock background and blur when returning Home.
- Reorganize settings and search paths while preserving the four home entries, existing preferences and backups.
- Restart the launcher and System UI after updating to apply the affected styles.

## Installation and compatibility

1. Install the official APK; updates with the same signature preserve app data.
2. Enable the module in your Xposed framework and select the required scope.
3. Restart the affected processes or device and follow the settings instructions.

Other Android / HyperOS major versions are unsupported. Do not enable alongside upstream or another CustoMIUIzer fork.

[Usage and support](https://github.com/thetvplus/customiuizer-a14/tree/1a64b974b732105837adf6d7f8f9d3756ec12205) · [Compatibility](https://github.com/thetvplus/customiuizer-a14/blob/1a64b974b732105837adf6d7f8f9d3756ec12205/COMPATIBILITY.md)

This repository documents published downloads. Unreleased development changes do not describe this APK or its device acceptance.
