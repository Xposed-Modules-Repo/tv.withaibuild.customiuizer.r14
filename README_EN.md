# 米客 A14｜CustoMIUIzer A14

[简体中文](README.md) | English

System UI and interaction customization for **HyperOS 1 / Android 14 / arm64-v8a**, requiring **libxposed API 102 or later**.

[Download official APK](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/download/281-r14.26.11/CustoMIUIzer-A14-r14.26.11.apk) · [Release notes](https://github.com/Xposed-Modules-Repo/tv.withaibuild.customiuizer.r14/releases/tag/281-r14.26.11) · [Changelog](CHANGELOG.md)

[Telegram community](https://t.me/customiuizerA14)

## Current stable release

`r14.26.11` · versionCode `281` · `tv.withaibuild.customiuizer.r14`

APK: `CustoMIUIzer-A14-r14.26.11.apk` (5,989,293 bytes)

SHA-256: `4a2d34d1e6ffd42c895f8def321932b1a0972a4ee12c51dc084afd7bca752410`

[Anonymized source snapshot](https://github.com/thetvplus/customiuizer-a14/commit/052e5cf98764acfb052e6f0567a215a71bc2230b): `052e5cf98764acfb052e6f0567a215a71bc2230b`

## Changes

Changes since r14.26.9:

- Reorganize Dynamic Island settings into media, notifications, live activities and system feedback. Media display and returns share one explicitly selected app list; notifications use their selected apps. Preserve saved preferences, backups and search routes.
- Unify return effects as None, Instant and Absorption. None retains native launcher returns, Instant captures no page, and Absorption retains native window springs and secure capture. Retire Simplified motion and the old effect scope while respecting system animation and power-saving settings.
- Refine receiving motion, rebound, centered and side-position bounds and offsets. Give track changes a bounded loading hold for the same media session so the island does not disappear prematurely.
- Add gesture returns and absorption to older HyperOS 1 launchers without WindowElement, preserving native returns, interruption recovery and secure-window restrictions. Recover the media island after a bounded missing handoff; late page captures no longer end accepted return progress early. Restart the device after updating.
- Fix a native system smooth-corner crash during Recents or Home transitions with Dock backgrounds enabled, preserving the background and layout. Remove residual blur when Recents hides the Dock.
- Solid Dock panels no longer create blur. Frosted glass reuses native materials and commits bounds, opacity and visibility after layout settles, follows system blur and power-saving availability, and stops frame synchronization at rest.
- Apply global window blur changes promptly while retaining developer, power-saving and thermal restrictions. Scope Recents blur ratios to their actual calls and separate local island transitions from the global window blur switch.
- Retain five measured music frequency bands with mild display compensation and smooth rise and fall. Cap music and recorder sampling at 10 Hz, retain display-frame interpolation, and stop rendering once silence settles.
- Align spacing and corners across settings rows, groups, search and action editors, and correct single-line centering and long current values. Add search guidance, preparation and no-results messages, and switch menu subitems directly while retaining page animations and system dynamic colors.
- Reuse unchanged settings rows and groups and reuse search indexes by language. Correct the credential shortcut target, align portrait and landscape keyboard bottom-spacing names while preserving saved values, and clarify Gboard-only support.
- Center Duo airplane icons by their actual ink and calculate automatic sizing consistently in pixels, preserving layout and user font settings.

## Installation and compatibility

1. Install the official APK; updates with the same signature preserve app data.
2. Enable the module in your Xposed framework and select the required scope.
3. Restart the affected processes or device and follow the settings instructions.

Other Android / HyperOS major versions are unsupported. Do not enable alongside upstream or another CustoMIUIzer fork.

[Usage and support](https://github.com/thetvplus/customiuizer-a14/tree/052e5cf98764acfb052e6f0567a215a71bc2230b) · [Compatibility](https://github.com/thetvplus/customiuizer-a14/blob/052e5cf98764acfb052e6f0567a215a71bc2230b/COMPATIBILITY.md)

This repository documents published downloads. Unreleased development changes do not describe this APK or its device acceptance.

Original build source identifier: `eea6315fe8ed309575f5d0c97e62df42de935dd9`. The signed APK and original metadata are unchanged; the snapshot consolidates history and redacts private documentation.
