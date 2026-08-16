# CustoMIUIzer A14 | HyperOS 1 / Android 14

[简体中文](README.md) | English

CustoMIUIzer A14 is a system UI and interaction customization module for HyperOS 1 / Android 14.

## Current Version

| Item | Value |
| --- | --- |
| Version | `r14.20.0` |
| versionCode | `198` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.20.0.apk` |
| Size | `3805054` bytes |
| APK SHA-256 | `AD9A8B86CFC5C55A0AB1D6EE1AE5EB3316897838A39B667F44154D219215722B` |

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

`r14.20.0` adds Dynamic Island mode, USB default purpose, recents app-name hiding, backup V2, and settings/UI reorganization. The module now uses a static scope and only exposes currently supported Hook targets. See [CHANGELOG.md](CHANGELOG.md) for details.

## Installation and Upgrade

- Download `CustoMIUIzer-A14-r14.20.0.apk` from this repository's Release;
- Enable the module;
- Confirm that scope includes `system`, the launcher, and the other required apps;
- Fully reboot the device.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs.

This release passes the complete offline gates, formal Release/R8 build, version, v2 signature, zip alignment, `debuggable=false`, and Xposed metadata checks. A full reboot is required after enabling. If USB is already connected after changing the default purpose, unplug and replug once.

Source and issue reporting: <https://github.com/tomthenpc/customiuizer-a14>
