# 米客 A14 — Changelog

[简体中文](CHANGELOG_CN.md) | English

## r14.26.11

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

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.26.11)

## r14.25.9

Changes since r14.24.1 (cumulative):

- Reorganize System, Launcher, Buttons and Gestures, and App Maintenance settings. Improve grouping, search, navigation, descriptions and current values; keep restart actions on the home page. Sliders accept validated numeric input.
- Add Dynamic Island for music, notifications, system feedback, calls, alarms, timers, stopwatch, recording and privacy status. Return to media or an idle island when a source ends; retain notification privacy and lock-screen rules.
- Use one island window and one active source, choosing long or short form from content. Notifications with a separate body use the long form; music and system activities use the short form. Swipe up to dismiss; keep an optional idle island without an expandable details panel.
- Support left, center and right positioning and horizontal adjustment. Fit dimensions, text, artwork, waveforms and touch areas to camera clearance, system insets and screen space, including stricter landscape limits.
- Add configurable music gestures for tracks, volume and shortcuts, plus opening the current player. Retain artwork and a static waveform while loading; improve duplicate sessions, multiple tracks, pause handling and track changes.
- Use native window returns for opt-in music-to-island transitions with manually selected players. Refine resizing, recoil and velocity handoffs; fix gaps, first-frame jumps, diagonal unlock motion, asymmetric deformation and repeated fades on quick returns.
- Offer lightweight and live waveforms with smoothed levels, noise filtering and silence decay, respecting privacy, pause and power-saving rules. Update clocks only while visible and clear state when sources or clock services end.
- Use app allowlists for notifications and category selection for feedback. Add optional charging and low-battery prompts; suppress the native low-battery warning only after the replacement is visible. Keep native feedback when takeover fails or content does not fit.
- Support top, center and bottom Toast placement with safe-area offsets. Short messages can use the island; notification shade keeps an idle island without touch interception, and idle taps no longer implicitly open the shade.
- Refine picture-in-picture entry, cancellation, exit, stashing and restoration. Floating-window exclusion affects only identifiable floating media while other allowed content remains visible; follow restart prompts after changes.
- Refine full-width Dock backgrounds, sizing, wallpaper colors and glass materials across unlock, Recents and app returns. Use one visible base and consistent corners; stop redundant sampling offscreen and prepare again on return.
- Refine Duo automatic size and optical centering. Add opt-in folder-icon background blur for supported MIUI and POCO interfaces, retaining system blur, default-icon and hardware limits. Restart the launcher after changes.
- Add VPN automation for multiple trigger apps, a VPN client or saved system IKEv2 profile. Configure delayed automatic disconnect independently, preserving manual, existing, always-on and lockdown connections. Restart after first enabling.
- Fix lock-screen artwork scaling, updates, stale screen-off requests and release. Validate complete native PIN keypads before scrambling, preserving click handling, fixed controls and authentication policy.
- Refine notification clicks, floating-window actions, expansion, collapse, rebinding, grouping and coloring. Fix shade and Control Center dates, app-lock timeout, installer details, volume keys and keyboard-dismiss boundaries.
- Synchronize fixed status-bar height with system resources while preserving fullscreen and contrast behavior. Refine A14 vibration strength, volume timers, panel hiding, loud-volume confirmation and audio focus. Restart after system-dimension changes.
- Fix vertical-dominant gestures triggering Dock swipes, default widget parameters and app-renaming refreshes. Improve back gestures, rotation, interruption, screenshot hiding, visualization and screen-off restoration.
- Improve migration, backup and maintenance recovery with saved state and result checks. Prevent stale startup or bulk-refresh snapshots from overwriting settings, retaining the last valid configuration on temporary read failures.

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.25.9)

## r14.24.1

versionCode 245. Changes since r14.23.5:

- Add an optional background inside the native Launcher Dock, disabled by default. Reuse system glass or Gaussian blur, fit the background to native icon bounds and device corners, and expose opacity, radius, horizontal spacing and signed four-edge shrink controls without moving or resizing icons.
- Keep Dock background visibility tied to the native recent-tasks state, retire hidden blur regions, and preserve native icon reorder and app-return animation coordinates. Use one shared corner radius for native material and outline; retain cached G4 geometry when native material is unavailable.
- Fix Dock size adjustment when one edge reaches its limit, and ease background width changes over 180 ms when the icon count changes. Raise common slider number-entry dialogs slightly and fit them above the visible keyboard.
- Select Duo cellular signal from the active data subscription instead of ambiguous per-controller flags. Preserve all native signal levels, distinguish Wi-Fi levels 3 and 4, and add adjustable horizontal spacing.
- Coalesce weather refreshes and discard cancelled provider results, recover step-view state after reattachment, and restore screenshot-hidden overlays across repeated events and detachments.
- Propagate direct and wrapped fatal JVM errors from receiver registration, replacement, cleanup, logging, callbacks and ROM fallbacks. Roll back failed registrations before propagation; release the screen controller's context even if unregister fails, and allow initialization to recover after a failed registration. Preserve ordinary optional-feature fallbacks.
- Mark the static Duo Bluetooth battery summaries as non-formatted text so literal percentages are not parsed as AAPT format tokens.
- Detach stale receiver and listener cleanup batches before calling framework or user cleanup. Keep bounded retry queues while avoiding recursive retries and map locks held across cleanup callbacks.
- Limit preference generation inputs to Kotlin feature declarations and preference XML. Check the generated catalog against a full-source scan so ordinary Hook, Java and UI edits no longer invalidate settings resources without losing preference keys.
- Include non-ignored untracked inputs in local change-based verification and preserve UTF-8 diagnostics on Windows.
- Retain the validated Gradle, AGP, JDK 25, Kotlin and DexKit versions, API 102 baseline, runtime behavior and release gates. Device performance and target-ROM acceptance require separate verification.

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.24.1)

## r14.23.5

versionCode 243. Changes since r14.22.7:

- Reuse one displayed settings-group snapshot between adapter updates instead of rebuilding it for every row and draw. Release observers and cached page references when replacing the adapter or destroying the page; retain existing cards, dividers, clipping and day/night colours.
- Restrict the Open With sample provider to explicit temporary read grants. Reject write and unknown URI requests; store the two tiny text/ZIP samples uncompressed so all six choices remain readable through seekable file descriptors. Remove the obsolete Bluetooth permission while retaining Bluetooth Connect.
- Reject unrelated Tasker actions and unidentified senders before reading extras. Preserve token/host verification, propagate fatal failures and throttle malformed-broadcast logs without exposing payloads.
- Handle native API 102 scope-read/request failures when the service dies or denies access: report unavailability without crashing settings, retrying or claiming approval; retain fatal-error propagation and one scope snapshot per selection.
- Protect the native Service Binder provider with the existing signature permission, blocking ordinary apps from replacing the settings connection while retaining privileged-framework access.
- Make official DexSQL CLI/MCP and shared queries the primary development path for ROM targets and final DEX evidence. Add definition-source and Hook API boundary queries without adding runtime dependencies or default CI work. Retain the validated toolchain, dependencies and compact-resource task.
- Require libxposed API 102. Share one native hook installation policy and remove API 101 capability flags, fallbacks and isolation bridges. Retain the three infrastructure hook IDs, readback, callback semantics and lifecycle; hot reload and explicit hook replacement remain disabled.
- Raise Java/Kotlin output to 25 and Kotlin language/API to 2.4; adopt AGP 9.4.1 (bundled R8/D8), Kotlin BOM/stdlib 2.4.21, DexKit 2.3.0 and Fragment 1.9.1. Retain JDK 25, Gradle 9.8.1 and Android SDK 34 runtime support; use public AGP APIs for generated tasks.
- Propagate wrapped fatal failures from original methods and before/after callbacks while preserving ordinary exception recovery, argument changes and proceed counts. Reuse the empty argument array.
- Check actual preference-mirror commits; optimistic service caches no longer confirm rejected writes. Preserve one retry per connection and recovery on a fresh connection.
- Read scope once per selection instead of repeating Binder queries for each app; asynchronous callbacks retain only Application Context. Adopt app-list filter results without a second list copy.
- Fix blank settings pages and the blank screen on returning from About by preserving page position and alpha while collecting animators; retain back animations and state restoration. Add Android 14 UI-model regression tests for all four categories and their settings pages, return and recreation.
- Add an independent Duo charging-bolt switch, enabled by default; percentage and charging colour remain independent.
- Refine silent and DND shortcut colours while preserving the native appearance and animation; restore original colours when custom styling is disabled.
- Fix navigation long presses, key consumption and Control Center gesture cleanup. Restart the phone when first enabling navigation long presses.
- Remove ineffective A14 features and old settings, refine native network-speed and international-ROM options, and improve settings-sync error handling with fewer startup-diagnostics objects. Remove obsolete API-specific R8 barriers and scope helpers.
- Consolidate APK tools and remove test-only production sources and duplicate configuration while retaining resource compression, Hook compatibility boundaries, diagnostics and optional DexSQL tools.

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.23.5)

## r14.22.7

versionCode 230. Changes since r14.21.9:

- Add optional Duo battery, Wi-Fi and cellular icons, disabled by default, with lock screen, Control Center, double rows and size adjustments; refine network text, headphone hints, automatic sizing, handoffs and signal recovery.
- Preserve fatal errors in resource hooks, theme merging, logging and settings initialization; let the native Control Center icon manager tint custom text, avoiding conflicting wallpaper callbacks and duplicate listeners.
- Copy wallpaper and drawer-blur callback arguments only when rewriting is needed, reduce repeated thread-state reads and compute only selected temperatures. Preserve original calls, input ownership and exception propagation.
- Check backup CRC over the original byte slice; reduce search and locale-list intermediates while preserving backup format, matching and ordering.
- Use lossless WebP for the bundled QR image and Android 14-compatible compact resource entries; remove unused JVM coroutine-agent payload while retaining dynamic resources and Hook/JNI rules.
- Pin libxposed 102 to official Maven Central with SHA-256 verification and align Fragment KTX with Fragment 1.5.4; include validated dependency and build-tool security updates, retaining DexKit 2.2.0, the API 101 minimum runtime, JDK 25 and JVM 17 output.
- Coordinate Fast and Full CI while retaining mutation gates, clean builds without caches, and APK/R8 mapping reproducibility checks; remove unused resources and alias placeholders, obsolete API checks and resource qualifiers; strengthen signing/version checks and consolidate maintenance documentation and release history.

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.22.7)

## r14.21.9

versionCode 218. Changes since r14.20.9:

- Coalesce Control Center step refreshes to prevent slow-query backlogs; cancel obsolete work on screen-off, view removal or controller replacement, preserving serial queries and recovery.
- Optimize app-lock and privacy-list ordering and search, preserving selected-first order and dual-user identity.
- Initialize icon cache keys and reuse shortcut metadata, reducing repeated reads and list copies during scrolling, search and result publication.
- Fix left-side mobile/Wi-Fi icons and add left-icon size and vertical-offset controls.
- Fix Control Center settings transitions; cover standalone pages, combined route queries, Wi-Fi/WIFI matching and result navigation in search.
- Remove duplicate whole-row switch feedback and update the About maintainer to thetvplus.
- Refine settings; fix notification channel navigation, importance settings on both page variants, and clipping of the final extended-menu item.

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.21.9)

## r14.20.9

versionCode 206. Earliest retained release; older history remains in Git.

- Defer system_server business features until the preference snapshot is ready; retain the Hook catalog across rapid SystemUI restarts.
- Isolate WindowManager argument-type errors and scope status-bar resource overrides to the system, SystemUI and launcher; system_server still owns window geometry.

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.20.9)
