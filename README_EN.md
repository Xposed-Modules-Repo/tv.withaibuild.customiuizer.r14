# CustoMIUIzer A14 | HyperOS 1 / Android 14

[简体中文](README.md) | English

CustoMIUIzer A14 is a system UI and interaction customization module for HyperOS 1 / Android 14.

## Current Version

| Item | Value |
| --- | --- |
| Version | `r14.22.1` |
| versionCode | `224` |
| Maintainer | `thetvplus` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.22.1.apk` |
| Size | `3963343` bytes |
| APK SHA-256 | `F3F5E0809DAEB258CDA5E6D4D214272EB4DDC400107550F7A77556EC20FF2F12` |

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

`r14.22.1` adds Duo with automatic sizing, headphone styles, short transitions and vertical adjustment. Bluetooth connections show a three-second hint; disconnected Wi-Fi can show cellular or no-service glyphs. The native double-row layout is retained. Disabled by default; restart System UI after setting changes. See [CHANGELOG.md](CHANGELOG.md).

## Installation and Upgrade

- Download `CustoMIUIzer-A14-r14.22.1.apk` from this repository's Release;
- Enable the module;
- Confirm that scope includes `system`, the launcher, and the other required apps;
- Fully reboot the device.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs.

Local checks and testing on a HyperOS 1 / Android 14 device are complete. Fully reboot after enabling.

Source and issue reporting: <https://github.com/thetvplus/customiuizer-a14>
