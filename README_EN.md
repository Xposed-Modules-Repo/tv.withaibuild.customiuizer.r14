# CustoMIUIzer A14 | HyperOS 1 / Android 14

[简体中文](README.md) | English

CustoMIUIzer A14 is a system UI and interaction customization module for HyperOS 1 / Android 14.

## Current Version

| Item | Value |
| --- | --- |
| Version | `r14.22.7` |
| versionCode | `230` |
| Maintainer | `thetvplus` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.22.7.apk` |
| Size | `3733141` bytes |
| APK SHA-256 | `C2B3B043DD7A1323A0977600B6D8AB560C91EFA2AC2BEC159911EA9A650D4907` |

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

This release consolidates the r14.22 series since r14.21.9: Duo, stability fixes, leaner callbacks and backups, resource compaction, and maintenance cleanup. Duo is disabled by default; restart SystemUI after changing its settings. See [CHANGELOG.md](CHANGELOG.md).

## Installation and Upgrade

- Download `CustoMIUIzer-A14-r14.22.7.apk` from this repository's Release;
- Enable the module;
- Confirm that scope includes `system`, the launcher, and the other required apps;
- Fully reboot the device.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs.

The official APK passed upgrade, reboot, settings UI and launcher-icon hide/restore checks on Xiaomi 13 / HyperOS 1 / Android 14 / API 102. All 407 existing settings and module scope were preserved; resource semantics matched across ten languages and day/night modes. API 101 has static compatibility coverage, with no API 101 framework device acceptance; verification does not cover every feature or ROM combination.

Source and issue reporting: <https://github.com/thetvplus/customiuizer-a14>
