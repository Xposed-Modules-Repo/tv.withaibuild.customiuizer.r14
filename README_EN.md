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

## r14.13.7 Highlights

- **A setting changed while the LSPosed service is unreachable is no longer lost.** Every change
  made while unbound used to be dropped, and reconnecting did not resend it. The module reads its
  snapshot once per hooked process and installs hooks from it, so a toggle flipped at the wrong
  moment stayed off for good — which is what "album art as wallpaper does nothing" was. The mirror
  now reconciles in full on bind, and says so while anything is still undelivered.
- **Soft reboot is no longer refused on the wrong evidence.** It broadcasts to the module inside
  SystemUI, which is unrelated to whether the settings app holds a service binder. It is attempted
  as an ordered broadcast and reported only if nobody claims it.
- **A damaged list preference no longer risks a system process.** Reading one threw on a changed
  stored type or a non-numeric string, from hooks running in SystemUI and `system_server`.
  Unreadable values now fall back to the caller's default.
- **Status bar battery/temperature formats apply without restarting SystemUI.** The ticker used the
  config captured at hook time. The two options that genuinely cannot hot-update now say so.
- **Lock-screen album art concurrency and cache.** Skipping tracks quickly could run several
  full-screen passes at once, and the cache was bounded by entry count (~31 MB on a tall screen)
  with a key that could never hit. Output quality and cropping are unchanged.
- **A saturated icon queue no longer leaves an icon permanently blank.**
- The same APK continues to support libxposed API 101/102 and passes R8, resource shrinking,
  zipalign, and APK v2 signing checks.

> The root cause of this round is that the LSPosed/Vector daemon stops pushing the service binder
> after the module's app process restarts rapidly several times, and `libxposed-service` offers no
> way to ask for it. That belongs to the framework and cannot be fixed inside the module; what
> this release fixes is everything it was costing. See
> [LSPOSED_BINDER_DELIVERY.md](https://github.com/tomthenpc/customiuizer-a14/blob/r14.13.7/docs/LSPOSED_BINDER_DELIVERY.md).

### Verification status

This release passes the static gate (116 files, no violations), 171 unit tests, lint at all three
levels, and both build variants, but has **not completed on-device acceptance**. It changes the
lock-screen album art processor and the status bar ticker, both of which run inside SystemUI, so it
sits closer to a system process than `r14.13.6` did; roll back to `r14.13.6` if SystemUI misbehaves.
The signing certificate is unchanged since `r14.13.5`, so installing in either direction works.

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
   [r14.13.7 Release](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/185-r14.13.7).
2. Enable the module in LSPosed/Vector and confirm the recommended scope.
3. Open module settings once.
4. Fully reboot the device.

An API 101 manager may warn that `targetApiVersion=102` targets a newer API. The warning alone does
not mean loading failed; use target-process logs and actual behavior as the source of truth.

APK SHA-256:

`11D01A737BED25C3C4D31153DE22CB918A651D0DD043D0374E2C0E41D32492CC`

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
