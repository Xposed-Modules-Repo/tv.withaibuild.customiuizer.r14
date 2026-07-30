# CustoMIUIzer A14

[简体中文](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/blob/main/README.md) |
[English](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/blob/main/README_EN.md)

An Xposed module for customizing the system UI and interactions on
**HyperOS 1 / Android 14**.

This repository is the LSPosed module listing and download page. Source code, the complete
changelog, build instructions, and engineering documentation are maintained in the
[source repository](https://github.com/tomthenpc/customiuizer-a14).

User-visible changes are listed in this repository's
[CHANGELOG](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/blob/main/CHANGELOG.md)
(Chinese) and
[CHANGELOG_EN](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/blob/main/CHANGELOG_EN.md)
(English). The source repository remains authoritative for complete engineering records.

## Supported Environment

- HyperOS 1 / Android 14 (SDK 34)
- `arm64-v8a`
- A framework implementing modern libxposed API 101 or API 102
- Module package: `tv.withaibuild.customiuizer.r14`

Android 15, Android 16, and other MIUI/HyperOS versions are not supported. Do not enable this
module together with upstream or another CustoMIUIzer-derived module.

## Feature Overview

- Status-bar icons, battery, signal, network speed, date, and temperature;
- Control center, volume panel, brightness, and notification behavior;
- Lock screen, charging information, media UI, and shortcuts;
- Launcher, recents, folders, icons, and home-screen gestures;
- Navigation bar, buttons, custom actions, power menu, and system animations;
- App, permission, installer, sharing, privacy-app, and app-lock behavior.

Feature availability depends on the device ROM and system-app versions.

## r14.13.8 Highlights

- Tightens the boundary between hook-process utilities and settings-app utilities, reducing
  unrelated class loading inside system processes.
- Removes six obsolete GlobalActions forwarding stubs and calls their implementations directly.
- Registers the soft-reboot receiver independently of custom actions, so in-app "Reboot system"
  still works when no custom action is configured.
- Distinguishes an unclaimed broadcast from receiver-side execution failure, so the latter is no
  longer reported as "LSPosed service not connected".
- Custom-action behavior is unchanged; the same APK continues to support libxposed API 101/102.
- The Release passes R8, resource shrinking, zipalign, and production APK v2 signing checks.

### Verification status

This release passes the static gate (117 files, no violations), 176 unit tests, lint at all three
levels, and both build variants.

On-device acceptance completed on Android 14 / HyperOS 1 with LSPosed 2.1.1 (7790): the module
loaded in SystemUI and Launcher, both reboot cycles completed, P0/P1 were zero, and no
target-process crash, hook exception, or duplicate receiver registration was found.

Known issue: system Toast suppression may still be ineffective; this release does not change that
logic. The signing certificate is unchanged since `r14.13.5`, so it installs in place.

## Differences in This Maintenance Build

- `MonwF/customiuizer@v24.10.12` is used only as the Android 14 functional reference; this is not
  an official upstream release.
- The module uses an independent package, version line, signing identity, and release process, so
  it does not replace the upstream installation identity.
- Maintenance is limited to HyperOS 1 / Android 14; Android 15/16 compatibility is not mixed into
  this release line.
- It uses modern libxposed API 101/102 and does not depend on the Legacy Xposed Hook API.
- Long-lived system processes receive focused lifecycle, duplicate-registration, hot-path, and
  disabled-feature overhead controls.

## Performance and Power Note

Compared with earlier implementations, this release reduces unnecessary hooks, duplicate
listeners, orphaned background tasks, hot-path allocations, and exception retries. In theory,
this can lower the module's additional CPU, memory, and wakeup overhead in SystemUI, Launcher,
and `system_server`.

Actual gains depend on enabled features, ROM, and usage. No fixed battery, CPU, or memory
improvement percentage is claimed without a same-device controlled comparison.

When upgrading from `r14.12.0` or earlier public builds, back up settings first; the signing
key has been changed, so you must uninstall the old version before installing this one.

## Installation

1. Download and install the APK from the
   [r14.13.8 Release](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/186-r14.13.8).
2. Enable the module in LSPosed/Vector and confirm the recommended scope.
3. Open module settings once.
4. Fully reboot the device.

An API 101 manager may warn that `targetApiVersion=102` targets a newer API. The warning alone does
not mean loading failed; use target-process logs and actual behavior as the source of truth.

APK SHA-256:

`B0E7D4A3CB50E39748531D5B0FD3CB95F81C1F777DDAC9E346B8C8D67B8CBE62`

Signing certificate SHA-256:

`C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70`

## Feedback

Report issues in the [source repository](https://github.com/tomthenpc/customiuizer-a14) and
include:

- Module version and APK source;
- Device, ROM, and system-app versions;
- Framework name and actual libxposed API version;
- Module, `system_server`, SystemUI, or Launcher logs collected after a full reboot;
- Reproducible feature settings and steps.
