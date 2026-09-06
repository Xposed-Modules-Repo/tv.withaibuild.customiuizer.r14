# CustoMIUIzer A14 | HyperOS 1 / Android 14

[简体中文](README.md) | English

CustoMIUIzer A14 is a system UI and interaction customization module for HyperOS 1 / Android 14.

## Current Version

| Item | Value |
| --- | --- |
| Version | `r14.21.5` |
| versionCode | `212` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.21.5.apk` |
| Size | `3886062` bytes |
| APK SHA-256 | `3F952F37A4E1EAB18C9D2BD17AF4B72DBFB231F7DB3309D054538B7E5141B820` |

## Compatibility and Requirements

- HyperOS 1 / Android 14 (SDK 34);
- `arm64-v8a` devices;
- libxposed API 101/102;
- Android 15, Android 16, and other major MIUI / HyperOS versions are not supported;
- Do not enable this module together with upstream or another CustoMIUIzer-derived module.

## Main Features

- Status bar icons, battery, signal, network speed, date, and temperature;
- Status capsule / Dynamic Island, USB default purpose, volume, and brightness;
- Control center, notifications, lock screen, charging, and media UI;
- Launcher, recents, folders, icons, and home-screen gestures;
- Navigation bar, buttons, custom actions, power menu, and system animations;
- App, permission, installer, sharing, privacy-app, and app-lock behavior.

`r14.21.5` optimizes the settings UI and fixes the notification channel settings entry. See [CHANGELOG.md](CHANGELOG.md) for details.

## Installation and Upgrade

- Download `CustoMIUIzer-A14-r14.21.5.apk` from this repository's Release;
- Enable the module;
- Confirm that scope includes `system`, the launcher, and the other required apps;
- Fully reboot the device.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs.

This release passes the formal Release/R8 build, version, v2 signature, zip alignment, `debuggable=false`, Xposed metadata, and provenance checks. A full reboot is required after enabling.

Source and issue reporting: <https://github.com/tomthenpc/customiuizer-a14>
