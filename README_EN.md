# CustoMIUIzer A14 | HyperOS 1 / Android 14

[简体中文](README.md) | English

CustoMIUIzer A14 is a system UI and interaction customization module for HyperOS 1 / Android 14.

## Current Version

| Item | Value |
| --- | --- |
| Version | `r14.16.1` |
| versionCode | `192` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.16.1.apk` |
| Size | `3369409` bytes |
| APK SHA-256 | `F213BA3F939FAA7BD12150D75A538529E9517D9CE865B6611C7A3C93C8370258` |
| Signing certificate SHA-256 | `C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70` |

## Compatibility and Requirements

- HyperOS 1 / Android 14 (SDK 34);
- `arm64-v8a` devices;
- LSPosed / Vector implementing libxposed API 101 or 102;
- Android 15, Android 16, and other major MIUI / HyperOS versions are not supported;
- Do not enable this module together with upstream or another CustoMIUIzer-derived module.

An API 101 manager may warn that the module targets API 102. The warning alone does not mean the module failed to load.

## Main Features

- Status bar icons, battery, signal, network speed, date, and temperature;
- Control center, notifications, volume, brightness, lock screen, charging, and media UI;
- Launcher, recents, folders, icons, and home-screen gestures;
- Navigation bar, buttons, custom actions, power menu, and system animations;
- App, permission, installer, sharing, privacy-app, and app-lock behavior.

`r14.16.1` focuses on runtime stability, memory release, SystemUI hot-path performance, and immediate visual feedback when a preference switch is tapped. See [CHANGELOG_EN.md](CHANGELOG_EN.md) for details.

## Installation and Upgrade

1. Download the APK from this repository's Release and verify its SHA-256.
2. Install it and enable the module in LSPosed / Vector.
3. Make sure the recommended scope includes `system`.
4. Open module settings once and fully reboot the device.

Versions from `r14.13.5` onward using the current certificate can be updated in place. The old certificate used by `r14.12.0` and earlier is incompatible: back up settings and record the scope before uninstalling the old build, then install this release, restore settings, and fully reboot.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs.

This release passes the complete offline gates, formal Release/R8 build, version, signature, zip alignment, `debuggable=false`, and Xposed metadata checks. New changes have not completed per-feature device behavior verification and are not claimed as fully `DEVICE_VERIFIED` on every device.

Source and issue reporting: <https://github.com/tomthenpc/customiuizer-a14>
