# CustoMIUIzer A14 Kotlin Refactor｜HyperOS 1 / Android 14｜API 101/102

[简体中文](README.md) | English

A system UI and interaction customization module for **HyperOS 1 / Android 14**.

## Current Release

| Item | Value |
| --- | --- |
| Version | `r14.15.3` |
| versionCode | `191` |
| System | HyperOS 1 / Android 14 |
| ABI | `arm64-v8a` |
| applicationId | `tv.withaibuild.customiuizer.r14` |
| libxposed | API 101–102 |
| APK | `CustoMIUIzer-A14-r14.15.3.apk` |
| Size | `3107265` bytes |
| APK SHA-256 | `F7AB34722B0193DD8C97DF0146C968E5A6064655AD497061E902CD1545375E7E` |
| Signing certificate SHA-256 | `C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70` |

## r14.15.3 Highlights

* Restores the previously-removed `system` scope, fixing `system_server` hook loading.
* Hardens Global Actions and other Receivers with exception isolation, trust validation, lifecycle,
  and concurrent-registration handling.
* Improves hook-load diagnostics and compatibility information.
* Keeps network-speed bold text in the current system typeface and adds dual-row line-spacing
  adjustment.
* Adds localization notes for network-speed settings.
* Fixes settings text-style inheritance and About-page text wrapping.

## Supported Environment

* HyperOS 1 / Android 14 (SDK 34);
* `arm64-v8a`;
* LSPosed / Vector implementing libxposed API 101 or 102;
* Android 15 and Android 16 are not supported.

Feature availability depends on the device ROM and system-app versions. Do not enable this module
with the upstream version or another CustoMIUIzer-derived module.

## Important Upgrade Notes

Builds from `r14.13.5` and later using the new signing key can be installed as updates.

When upgrading from `r14.12.0` or earlier, the old signing key is lost. Back up your settings and
record the scope, then uninstall the old version, install the new version, re-enable the scope,
restore settings, and fully reboot.

## Installation

1. Download the APK from this repository's Release.
2. Verify the APK SHA-256.
3. Install the APK.
4. Enable the module in LSPosed / Vector.
5. Make sure the recommended scope includes `system`.
6. Open the module settings once and fully reboot the device.

An API 101 manager may warn that `targetApiVersion=102` targets a newer API. The warning alone does
not mean loading failed; use target-process logs and actual behavior as the source of truth.

## Verification Status

This release completed the official Release APK build, signing, zipalign, package metadata, and
Xposed metadata checks, and confirmed that the APK contains both `system` and `android` scopes.

This release did not run the full unit test suite, Lint, project Audit, ADB regression, or a complete
real-device smoke test.

## Source and Feedback

Source code, the complete changelog, and engineering documentation:

`https://github.com/tomthenpc/customiuizer-a14`

When reporting issues, please provide the module version, device, ROM, framework version, actual
scope, reproduction steps, and relevant `system_server`, SystemUI, or Launcher logs.
