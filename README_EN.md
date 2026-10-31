# 米客 A14｜CustoMIUIzer A14

[简体中文](README.md) | English

System UI and interaction customization for **HyperOS 1 / Android 14 / arm64-v8a**, requiring **libxposed API 102 or later**.

[Download official APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/267-r14.26.7/CustoMIUIzer-A14-r14.26.7.apk) · [Release notes](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/267-r14.26.7) · [Changelog](CHANGELOG.md)

[Telegram community](https://t.me/customiuizerA14)

## Current stable release

`r14.26.7` · versionCode `267` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.26.7.apk` (5,997,909 bytes)

SHA-256: `80f32bd50fec08a8a0ffbdfea46878fc2a9a5e0d54a15b883b95d4286824b031`

[Source commit](https://github.com/thetvplus/customiuizer-a14/commit/8a9ce0d8dcbd3c16601b3e543247674677c69f3e): `8a9ce0d8dcbd3c16601b3e543247674677c69f3e`

## Changes

Changes since r14.26.5:

- Trusted Wi-Fi networks now use cached scans, system broadcasts and on-demand refresh, with request cooldowns across rapid navigation, rejected scans and wait timeouts. Trusted Bluetooth devices replace the 15-second loop with page entry, system events and manual cache requests; empty results also end the wait.
- Move backup filtering, encoding and file writes off the UI thread and share a single operation slot with restore. Finish started file work and close streams when a page is destroyed, preventing overlapping writes and stale success messages while retaining the backup format and system document picker.
- Defer language refresh while Fragment state is saved and publish search indexes only after preparation completes. Restore editor drafts by stable preference keys and allow complete text and numeric forms to scroll when the landscape keyboard occupies most of the screen.
- Continue opening trusted-network lists after their permissions are granted, and resume adding the current network in Wi-Fi automatic settings. Directory selection accepts confirmed local storage folders; cancellation or invalid selection preserves the previous setting.
- Report credential-confirmation success only from the system result and restore pending requests without relaunching them. Ordinary saved Activity state no longer implies authentication is pending.
- Foreground monitoring no longer installs global Hooks from each network-speed controller constructor. Base initialization and Application.attach share ownership of the actual target, while audio, alarm and brightness callbacks follow their current instances. Correct the nullable Boolean signature that could hide Control Center tiles with custom columns, and rebind each actual left adapter after recreation. Restart the device after updating.
- Validate screenshot folders against their actual storage volume and MediaStore image-directory rules, letting the system create child folders. Unsupported custom folders retain the native destination. Preserve existing privileged host paths and keep filename extensions and MIME types consistent.

## Installation and compatibility

1. Install the official APK; updates with the same signature preserve app data.
2. Enable the module in your Xposed framework and select the required scope.
3. Restart the affected processes or device and follow the settings instructions.

Other Android / HyperOS major versions are unsupported. Do not enable alongside upstream or another CustoMIUIzer fork.

[Usage and support](https://github.com/thetvplus/customiuizer-a14/tree/8a9ce0d8dcbd3c16601b3e543247674677c69f3e) · [Compatibility](https://github.com/thetvplus/customiuizer-a14/blob/8a9ce0d8dcbd3c16601b3e543247674677c69f3e/COMPATIBILITY.md)

This repository documents published downloads. Unreleased development changes do not describe this APK or its device acceptance.
