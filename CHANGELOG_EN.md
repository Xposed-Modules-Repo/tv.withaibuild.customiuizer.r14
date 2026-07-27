# Changelog

[简体中文](CHANGELOG.md) | English

This file records user-visible changes in the LSPosed module repository. For the complete commit
history, engineering notes, and all public releases, see the
[source repository](https://github.com/tomthenpc/customiuizer-a14).

## r14.13.4

- Improved in-app locale handling, About page, day/night theme, and settings-page recreation.
- Fixed search return state and asynchronous Root-restart feedback for Launcher, SystemUI, and
  Security Center.
- Fixed status-bar temperature/current text icons in SystemUI retaining stale Views indefinitely.
- Optimized resource-hook miss paths by reducing boxing, reflection, and unnecessary parsing, with
  safe publication for sparse containers.
- Restored first-match exit behavior in CPU thermal-zone scanning after the Kotlin migration.
- Removed repeated Regex compilation from `first|second` preference parsing and added PrefPair
  regression tests.
- Improved RemotePreferences initial snapshot and listener-registration state.
- Same APK keeps libxposed API 101/102 compatibility.
- Release passes R8, resource shrinking, zipalign, and APK Signature Scheme v2 checks.

### Important upgrade note

The private signing key used for public `r14.12.0` and earlier releases has been lost. `r14.13.4`
uses a new official signing certificate and cannot be installed as an in-place update over older
public builds.

Before upgrading, back up module settings and record the LSPosed/Vector scope, then uninstall the
old build, install the new one, re-enable the scope, restore settings, and fully reboot.

- APK: `CustoMIUIzer-A14-r14.13.4.apk`
- SHA-256: `E8A2BD362C0540972441B8D1DE0BCACE8FE85FEF71F31406F3B4DA1A4027D26C`
- Signing-certificate SHA-256:
  `C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70`

Download: [182-r14.13.4](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/182-r14.13.4)

## r14.12.0

- One APK supports frameworks implementing libxposed API 101 or API 102.
- Core hooks, settings UI, and utilities completed a conservative Kotlin migration.
- Fixed duplicate hooks, receivers, observers, coroutines, and animation tasks after SystemUI
  recreation.
- Reduced repeated reflection, resource lookups, formatting, and temporary objects in frequent hook
  and drawing paths.
- Disabled features avoid registering unnecessary hooks and long-lived listeners where possible.
- Release passes R8, resource shrinking, zipalign, and APK Signature Scheme v2 checks.
- API 101 full reboot logs showed no module-attributable crash, ANR, hook failure, or linkage
  error.
- API 102 engineering compatibility was verified; independent API 102 device coverage was still
  pending.

Download: [174-r14.12.0](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/174-r14.12.0)

## Maintenance scope

- HyperOS 1 / Android 14 (SDK 34) and `arm64-v8a` only.
- Package name `tv.withaibuild.customiuizer.r14`; not interchangeable with upstream.
- `MonwF/customiuizer@v24.10.12` is used only as the Android 14 functional reference.
- Android 15, Android 16, and API 102 Hot Reload are not supported.
- Performance and power gains depend on the ROM, feature set, and usage; no fixed percentage is
  claimed without same-device controlled measurements.
