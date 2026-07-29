# Changelog

[简体中文](CHANGELOG.md) | English

This file records user-visible changes in the LSPosed module repository. For the complete commit
history, engineering notes, and all public releases, see the
[source repository](https://github.com/tomthenpc/customiuizer-a14).

## r14.13.6

- Fixes the interface language never being applied: `AppCompatDelegate.setApplicationLocales()`
  is a silent no-op during application start-up; it now calls the framework `LocaleManager`.
- Fixes the language row writing a preference value during binding, which broke the settings
  screen and reverted the saved language to the XML placeholder.
- Fixes the spurious "module not active" warning: a timeout is now distinct from a proven
  disconnect, and gets one further wait before anything is reported.
- Fixes a toggle opened from search not updating until the screen was left and re-entered.
- Hardens 23 callbacks the module registers from hooks, two of which run inside `system_server`.
- Fixes several receiver and observer cleanup paths that never ran; additional instance fields
  are now stored by identity.
- Performance: hook arguments are no longer copied and re-marshalled per invocation; reflection
  cache hits do not allocate; the main-screen search is a single allocation-free scan.
- The same APK continues to support libxposed API 101/102 and passes R8, resource shrinking,
  zipalign, and APK v2 signing checks.

### Important upgrade note

`r14.13.6` is signed with the same official certificate as `r14.13.5`, so it installs in place.

`r14.12.0` and earlier public builds used a signing key that has been lost; upgrading from those
still requires backing up, uninstalling, installing, re-enabling the scope, restoring settings,
and a full reboot.

**This release has not completed on-device acceptance.**

- APK: `CustoMIUIzer-A14-r14.13.6.apk`
- SHA-256: `35AEE1FEA1D7B38D967267210B7C272340B56B580ED49BEF4945AA9FC6F2ED96`
- Signing certificate SHA-256: `C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70`

Download: [184-r14.13.6](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/184-r14.13.6)

## r14.13.5

- Fixes the home search navigation regression: `Various` search results and sub-category items no
  longer return to the home page immediately; the target Preference is highlighted and scrolled into
  view.
- Restores the search state machine: three states `0/1/2`, automatically collapsing the SearchView
  and clearing the query when returning to the home page.
- Unifies empty/blank `sub` semantics: `ModData.sub` is now nullable, preventing empty strings from
  being treated as valid sub-categories.
- Corrects `openModCat()` return value: System / Launcher / Controls / Various now return `true` on
  successful navigation.
- Adds `SearchRouteResolver` and `SearchStateMachine` unit tests.
- Same APK keeps libxposed API 101/102 compatibility.
- Release passes R8, resource shrinking, zipalign, and APK Signature Scheme v2 checks.

### Important upgrade note

`r14.13.4` has a home search navigation regression and is superseded by `r14.13.5`. `r14.13.5` is
signed with the same new official certificate as `r14.13.4`, so users on `r14.13.4` can update in
place without uninstalling.

The private signing key used for public `r14.12.0` and earlier releases has been lost. Upgrading
from those builds still requires backing up, uninstalling, installing, re-enabling scope, restoring
settings, and fully rebooting.

- APK: `CustoMIUIzer-A14-r14.13.5.apk`
- SHA-256: `89AE5046564F69D491DC44F7B853443113FEC7100FE997ABA9984181C4983EA5`
- Signing-certificate SHA-256:
  `C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70`

Download: [183-r14.13.5](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/183-r14.13.5) (removed; the source tag remains)

## r14.13.4

> Withdrawn; superseded by `r14.13.5`.

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

Download: [182-r14.13.4](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/182-r14.13.4) (deleted)

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

Download: [174-r14.12.0](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/174-r14.12.0) (removed; the Git tag and source remain)

## Maintenance scope

- HyperOS 1 / Android 14 (SDK 34) and `arm64-v8a` only.
- Package name `tv.withaibuild.customiuizer.r14`; not interchangeable with upstream.
- `MonwF/customiuizer@v24.10.12` is used only as the Android 14 functional reference.
- Android 15, Android 16, and API 102 Hot Reload are not supported.
- Performance and power gains depend on the ROM, feature set, and usage; no fixed percentage is
  claimed without same-device controlled measurements.
