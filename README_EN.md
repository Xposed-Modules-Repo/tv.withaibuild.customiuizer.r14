# 米客 A14｜CustoMIUIzer A14

[简体中文](README.md) | English

米客 A14｜CustoMIUIzer A14 is a system UI and interaction customization module for **HyperOS 1 / Android 14**, supporting **arm64-v8a / libxposed API 102**.

r14.23.5 reduces repeated settings grouping, protects sample-file and Service entry points, and handles native scope failures. Updates preserve the signing identity and application data.

The full About-page name in source is 米客 A14｜CustoMIUIzer A14; short labels retain 米客 A14. Existing official APKs are not repackaged for wording changes; this app text takes effect in a future official build.

[Download the latest release](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/latest) · [Release history](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases)

## Current Version

| Item | Value |
| --- | --- |
| Version | `r14.23.5` |
| versionCode | `243` |
| Maintainer | `thetvplus` |
| Application ID | `tv.withaibuild.customiuizer.r14` |
| APK | `CustoMIUIzer-A14-r14.23.5.apk` |
| Size | `3,799,533` bytes |
| APK SHA-256 | `99625206aa8814626da57d4c5448fb05e19d6b2d1da9c34a4cbab6885bfe3c75` |
| Source tag | [r14.23.5](https://github.com/thetvplus/customiuizer-a14/tree/r14.23.5) |
| Source commit | `04f6204ac7fe2e0286cc6c6d566123f9ec0ba8f0` |

## Compatibility and Requirements

- HyperOS 1 / Android 14 (SDK 34);
- `arm64-v8a` devices;
- libxposed API 102 minimum;
- Android 13, Android 15, Android 16, and other major MIUI / HyperOS versions are not supported;
- Do not enable this module together with upstream or another CustoMIUIzer-derived module.

## Main Features

- Status bar icons, battery, signal, network speed, date, and temperature;
- Native Duo three-in-one status icon, with short transitions, headphone hints and vertical adjustment;
- Status capsule / Dynamic Island, USB default purpose, volume, and brightness;
- Control center, notifications, lock screen, charging, and media UI;
- Launcher, recents, folders, icons, and home-screen gestures;
- Navigation bar, buttons, custom actions, power menu, and system animations;
- App, permission, installer, sharing, privacy-app, and app-lock behavior.

Duo is disabled by default; restart System UI after changing its settings. See [CHANGELOG.md](CHANGELOG.md).

## Installation and Upgrade

- Download `CustoMIUIzer-A14-r14.23.5.apk` from this repository's Release;
- Enable the module;
- Confirm that scope includes `system`, the launcher, and the other required apps;
- Fully reboot the device.

## Risk Notice

This module changes system processes through Hooks. Availability depends on the ROM and system-app versions, and ROM updates may change classes, methods, or resources. If a problem occurs, disable the related feature first and retain the logs.

Full local regression and focused follow-up tests, Release Lint, R8/DEX and official-signature checks passed. The final APK was checked on fuxi / HyperOS 1 V816.0.7.0.UMCTWXM with Vector 2.2 (3112) / API 102 for settings navigation, service binding, permission isolation, all six sample types and SystemUI / Launcher restart. These are targeted correctness checks, not measurements of device CPU, PSS or power savings.

Acceptance covers the recorded device, ROM and scenarios; other ROMs/frameworks, all feature combinations and long-term operation need separate validation.

Source and issue reporting: <https://github.com/thetvplus/customiuizer-a14>
