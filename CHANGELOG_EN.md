# Changelog

> Note: The Releases page only keeps the current formal release. Full changelogs for older versions are preserved in this file. Older APKs are no longer available for download; historical source tags remain.

This file records only user-visible changes for the LSPosed module repository.

## r14.15.3

* Restores the previously-removed `system` scope, fixing `system_server` hook loading and the silent
  failure of related system-level features.
* Hardens the Global Actions Receiver with exception isolation, trust validation, and ordered-broadcast
  handling.
* Improves Receiver / Observer lifecycle and concurrent-registration handling.
* Improves hook-load diagnostics and compatibility logging.
* Keeps network-speed bold text in the current SystemUI font family.
* Adds dual-row network speed line spacing from `70%` to `130%`, with related localization notes.
* Fixes settings text-style inheritance and attribution/version text wrapping on the About page.
* Remains compatible with HyperOS 1 / Android 14, `arm64-v8a`, and libxposed API 101/102.

### APK

* File: `CustoMIUIzer-A14-r14.15.3.apk`
* Size: `3107265` bytes
* SHA-256: `F7AB34722B0193DD8C97DF0146C968E5A6064655AD497061E902CD1545375E7E`
* Signing certificate SHA-256: `C0EFF2DC4E662717195490DA78B12A984C6F2E6BD38ACF4EDAD14D53E3D22E70`
* versionCode / versionName: `191 / r14.15.3`

### Verification

This release completed APK build, production signing, zipalign, package metadata, and Xposed metadata
basic checks, and confirmed `scope.list` contains `system` and `android`. It did not run the complete
test suite or a full real-device smoke test.
