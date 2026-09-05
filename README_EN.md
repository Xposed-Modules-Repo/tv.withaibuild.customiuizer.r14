# CustoMIUIzer A14 | HyperOS 1 / Android 14

[简体中文](README.md) | English

CustoMIUIzer A14 is a system UI and interaction customization module for HyperOS 1 / Android 14.

## Current Version

| Item | Value |
| --- | --- |
| Prerelease | `r14.20.9` |
| Stable | `r14.20.8` |
| versionCode | `206` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.20.9.apk` |
| Size | `3882834` bytes |
| APK SHA-256 | `BB179572B7CF9FB6D80DC5E078126EAE383375486EF9D93816345CDCC5C1B9DC` |

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

`r14.20.9` is a prerelease: system_server does not install preference-gated features before the snapshot is ready, a fast SystemUI restart no longer skips the hook catalog, WindowManager type errors stay in the hook, and status-bar height resource replacement is limited to android, SystemUI, and the launcher. See [CHANGELOG.md](CHANGELOG.md) for details.

## Installation and Upgrade

- Download `CustoMIUIzer-A14-r14.20.9.apk` from this repository's Release;
- Enable the module;
- Confirm that scope includes `system`, the launcher, and the other required apps;
- Fully reboot the device.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs. This is a prerelease; the current stable release remains `r14.20.8`.

This prerelease passes the formal Release/R8 build, version, v2 signature, zip alignment, `debuggable=false`, Xposed metadata, and provenance checks. A full reboot is required after enabling.

Source and issue reporting: <https://github.com/tomthenpc/customiuizer-a14>
