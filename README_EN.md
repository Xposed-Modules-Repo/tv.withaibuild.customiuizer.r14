# CustoMIUIzer A14 | HyperOS 1 / Android 14

[简体中文](README.md) | English

CustoMIUIzer A14 is a system UI and interaction customization module for HyperOS 1 / Android 14.

## Current Version

| Item | Value |
| --- | --- |
| Version | `r14.22.5` |
| versionCode | `228` |
| Maintainer | `thetvplus` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.22.5.apk` |
| Size | `3885184` bytes |
| APK SHA-256 | `B59F25D3CFC5E8DF2C3BF91761E1ED118DF7E5F0B1F4C4398549F81638CF86D8` |

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

`r14.22.5` hardens theme-resource error handling and Control Center text tint, simplifies repeated CI work, and encodes the QR image as lossless WebP with identical pixels. The official APK is 76,703 bytes (1.936%) smaller than the pre-optimization build. Required dependencies, dynamic signal resources and R8 optimization are retained.

## Installation and Upgrade

- Download `CustoMIUIzer-A14-r14.22.5.apk` from this repository's Release;
- Enable the module;
- Confirm that scope includes `system`, the launcher, and the other required apps;
- Fully reboot the device.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs.

The user reported successful HyperOS 1 / Android 14 device acceptance for the pre-optimization r14.22.5 build. The pre-release changes only affect lossless QR encoding and build provenance; the final APK passed local build, resource consistency and signature checks. Fully reboot after enabling.

Source and issue reporting: <https://github.com/thetvplus/customiuizer-a14>
