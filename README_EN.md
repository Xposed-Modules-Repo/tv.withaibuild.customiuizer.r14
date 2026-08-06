# CustoMIUIzer A14 | HyperOS 1 / Android 14

[简体中文](README.md) | English

CustoMIUIzer A14 is a system UI and interaction customization module for HyperOS 1 / Android 14.

## Current Version

| Item | Value |
| --- | --- |
| Version | `r14.18.0` |
| versionCode | `193` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.18.0.apk` |
| Size | `3436081` bytes |
| APK SHA-256 | `31D839BDE68749D16FC13FC426B3B4975E84A29F9910326D33BBE00815FE9953` |

## Compatibility and Requirements

- HyperOS 1 / Android 14 (SDK 34);
- `arm64-v8a` devices;
- [Vector v2.2](https://github.com/JingMatrix/Vector/releases/tag/v2.2), libxposed API 101/102;
- Android 15, Android 16, and other major MIUI / HyperOS versions are not supported;
- Do not enable this module together with upstream or another CustoMIUIzer-derived module.

## Main Features

- Status bar icons, battery, signal, network speed, date, and temperature;
- Control center, notifications, volume, brightness, lock screen, charging, and media UI;
- Launcher, recents, folders, icons, and home-screen gestures;
- Navigation bar, buttons, custom actions, power menu, and system animations;
- App, permission, installer, sharing, privacy-app, and app-lock behavior.

`r14.18.0` adds adjustable lock-screen charging text size, live status-bar height synchronization, a fix for dark-visibility of left-side custom text icons, and hardens SystemUI and gesture lifecycles. See [CHANGELOG_EN.md](CHANGELOG_EN.md) for details.

## Installation and Upgrade

- Download `CustoMIUIzer-A14-r14.18.0.apk` from this repository's Release;
- Enable the module in [Vector v2.2](https://github.com/JingMatrix/Vector/releases/tag/v2.2);
- Make sure the recommended scope includes `system`;
- Fully reboot the device.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs.

This release passes the complete offline gates, formal Release/R8 build, version, v2 signature, zip alignment, `debuggable=false`, and Xposed metadata checks. The fuxi no-reboot status-bar-height switch has not been run on a physical device, so not all features or all devices are claimed as `DEVICE_VERIFIED`.

Source and issue reporting: <https://github.com/tomthenpc/customiuizer-a14>
