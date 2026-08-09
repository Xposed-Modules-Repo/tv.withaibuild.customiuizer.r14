# CustoMIUIzer A14 | HyperOS 1 / Android 14

[简体中文](README.md) | English

CustoMIUIzer A14 is a system UI and interaction customization module for HyperOS 1 / Android 14.

## Current Version

| Item | Value |
| --- | --- |
| Version | `r14.18.6` |
| versionCode | `196` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.18.6.apk` |
| Size | `3353186` bytes |
| APK SHA-256 | `7434C6B40E1FA913D7BC465D31DC04CD6C66C4763958A3053223EB3991320D7D` |

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

`r14.18.6` introduces lazy settings pages and hot-path optimizations, fixes status-bar height, observer lifecycles, startup activation state, and restored app selections, and clarifies the Various/input-method structure. Status-capsule controls remain in development. See [CHANGELOG.md](CHANGELOG.md) for details.

## Installation and Upgrade

- Download `CustoMIUIzer-A14-r14.18.6.apk` from this repository's Release;
- Enable the module;
- Make sure the recommended scope includes `system`;
- Fully reboot the device.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs.

This release passes the complete offline gates, formal Release/R8 build, version, v2 signature, zip alignment, `debuggable=false`, and Xposed metadata checks. Charging-scenario integration for the status capsule is complete, while status-bar height matching remains under compatibility work; availability still depends on the ROM and system-app versions.

Source and issue reporting: <https://github.com/tomthenpc/customiuizer-a14>
