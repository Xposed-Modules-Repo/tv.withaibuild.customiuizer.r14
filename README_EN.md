# CustoMIUIzer A14 | HyperOS 1 / Android 14

[简体中文](README.md) | English

CustoMIUIzer A14 is a system UI and interaction customization module for HyperOS 1 / Android 14.

## Current Version

| Item | Value |
| --- | --- |
| Version | `r14.22.2` |
| versionCode | `225` |
| Maintainer | `thetvplus` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.22.2.apk` |
| Size | `3980143` bytes |
| APK SHA-256 | `86B7B40DAB72C973A42DD32DD5FD1771ECF88A21E3E55B6B9C4E18D6B4AFFD1D` |

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

Compared with `r14.21.9`, `r14.22.2` adds optional Duo with automatic sizing, double rows and continuous Control Center handoffs. It fixes cellular signal display and offers simplified artwork or local-font 2G/3G/4G/5G text. Wired and Bluetooth headphones show a three-second hint, then restore Wi-Fi/network artwork. Disabled by default; restart System UI after setting changes. See the consolidated [CHANGELOG.md](CHANGELOG.md).

## Installation and Upgrade

- Download `CustoMIUIzer-A14-r14.22.2.apk` from this repository's Release;
- Enable the module;
- Confirm that scope includes `system`, the launcher, and the other required apps;
- Fully reboot the device.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs.

Local checks are complete, and the user has reported successful acceptance on a HyperOS 1 / Android 14 device. Fully reboot after enabling.

Source and issue reporting: <https://github.com/thetvplus/customiuizer-a14>
