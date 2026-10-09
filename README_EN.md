# CustoMIUIzer A14 | HyperOS 1 / Android 14

[简体中文](README.md) | English

CustoMIUIzer A14 is a system UI and interaction customization module for HyperOS 1 / Android 14.

## Current Version

| Item | Value |
| --- | --- |
| Version | `r14.22.6` |
| versionCode | `229` |
| Maintainer | `thetvplus` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.22.6.apk` |
| Size | `3733397` bytes |
| APK SHA-256 | `BFAACA985D2C0F445711463E6381EC4CB5400C0AA9DEA2FE1ACFF72752939192` |

## Compatibility and Requirements

- HyperOS 1 / Android 14 (SDK 34);
- `arm64-v8a` devices;
- libxposed API 101/102;
- Android 15, Android 16, and other major MIUI / HyperOS versions are not supported;
- Do not enable this module together with upstream or another CustoMIUIzer-derived module.

## Main Features

- Status bar icons, battery, signal, network speed, date, and temperature;
- Native Duo three-in-one status icon, with short transitions, headphone hints and vertical adjustment;
- Status capsule / Dynamic Island, USB default purpose, volume, and brightness;
- Control center, notifications, lock screen, charging, and media UI;
- Launcher, recents, folders, icons, and home-screen gestures;
- Navigation bar, buttons, custom actions, power menu, and system animations;
- App, permission, installer, sharing, privacy-app, and app-lock behavior.

`r14.22.3` adds optional Duo with network text, brief headphone hints, automatic sizing and Control Center handoffs, while fixing signal recovery and reducing drawing overhead. Duo is disabled by default; restart System UI after setting changes. See the consolidated [CHANGELOG.md](CHANGELOG.md).

`r14.22.6` reduces repeated allocations in wallpaper callbacks, temperature formatting, backup, search and locale lists; verifies official libxposed dependencies and aligns Fragment KTX. Compact resource tables preserve all resource contents. The official APK is 151,787 bytes smaller than r14.22.5. Builds use JDK 25; JVM 21/25 trials did not demonstrate a stable runtime gain, so Android output remains 17.

## Installation and Upgrade

- Download `CustoMIUIzer-A14-r14.22.6.apk` from this repository's Release;
- Enable the module;
- Confirm that scope includes `system`, the launcher, and the other required apps;
- Fully reboot the device.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs.

The official APK passed installation, reboot, module loading, settings UI and resource-parity checks on Xiaomi 13 / HyperOS 1 / Android 14 / API 102. Existing settings and scope remain. API 101 has static compatibility coverage, with no API 101 framework device acceptance; this does not cover every feature or ROM combination.

Source and issue reporting: <https://github.com/thetvplus/customiuizer-a14>
