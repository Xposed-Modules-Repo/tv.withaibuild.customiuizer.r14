# CustoMIUIzer A14 | HyperOS 1 / Android 14

[简体中文](README.md) | English

CustoMIUIzer A14 is a system UI and interaction customization module for HyperOS 1 / Android 14.

## Current Version

| Item | Value |
| --- | --- |
| Version | `r14.18.2` |
| versionCode | `195` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.18.2.apk` |
| Size | `3468849` bytes |
| APK SHA-256 | `77F868590C631271251991EDEBF066919460E2F1DA955EFDC10271207EAF3E77` |

## Compatibility and Requirements

- HyperOS 1 / Android 14 (SDK 34);
- `arm64-v8a` devices;
- libxposed API 101/102;
- Android 15, Android 16, and other major MIUI / HyperOS versions are not supported;
- Do not enable this module together with upstream or another CustoMIUIzer-derived module.

## Main Features

- Status bar icons, battery, signal, network speed, date, and temperature;
- Control center, notifications, volume, brightness, lock screen, charging, and media UI;
- Launcher, recents, folders, icons, and home-screen gestures;
- Navigation bar, buttons, custom actions, power menu, and system animations;
- App, permission, installer, sharing, privacy-app, and app-lock behavior.

`r14.18.2` improves the compatibility and fail-closed behavior of heads-up notification and lockscreen hooks, fixes the charging-info font size being reset by system styles, and simplifies SystemUI / system_server lifecycle hooks. See [CHANGELOG.md](CHANGELOG.md) for details.

## Installation and Upgrade

- Download `CustoMIUIzer-A14-r14.18.2.apk` from this repository's Release;
- Enable the module;
- Make sure the recommended scope includes `system`;
- Fully reboot the device.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs.

This release passes the complete offline gates, formal Release/R8 build, version, v2 signature, zip alignment, `debuggable=false`, and Xposed metadata checks, and the release candidate was installed and passed device smoke validation. Feature availability still depends on the ROM and system-app versions, and not all features or all devices are claimed as fully `DEVICE_VERIFIED`.

Source and issue reporting: <https://github.com/tomthenpc/customiuizer-a14>
