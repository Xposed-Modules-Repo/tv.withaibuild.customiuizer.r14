# 米客 A14 — Changelog

[简体中文](CHANGELOG_CN.md) | English

## r14.27.9

Changes since r14.26.11:

- DECK shares its card layout across Home and app entry. Card size is adjustable, titles and icons fade by depth, and the window animation can be turned off separately.
- Refined DECK handoff during held swipes, front and rear card openings, rapid cancellation, backfill after removal and landscape resizing. Native cards render thumbnail shadows. Quick switching along the gesture indicator keeps native geometry.
- The iOS gesture indicator follows native visibility, light and dark transitions, and gesture input, including native landscape handles. Enable full-screen gestures, keep the system indicator visible and restart Launcher and System UI.
- The side volume style supports a narrow default, expansion while dragging and recoil. The center style supports vertical adjustment, hides shortcuts when collapsed and restores native mute and Do Not Disturb when expanded. Both retain native volume steps, routing and expansion controls.
- Show volume percentage in a floating overlay, Toast capsule or Dynamic Island. Consecutive changes update the same indicator.
- Refined the Island's first appearance, activity changes, press feedback, rapid reversals and recoil. Wide layouts align content to the outside edges in either layout direction. Notifications update their actions while remaining visible, and landscape alarms and timers give the native window room.
- The Island can size the absorption head automatically or use the width set in Motion and Waveform. Media return, recall and picture-in-picture retain native handoff.
- Optional iOS battery, five-dot cellular signal and notification-shade media styles preserve native status, media sessions and controls. Duo retains priority and has updated wireless glyphs, digits, spacing and transitions.
- Fixed Dock background blur after reattachment and return from Recents. Launcher folders fade blur on open and close.
- Location automation under Apps and Tools supports a 0–30 second shutoff delay. The allow list can enable location and turn off module-enabled location after the last matching task card is removed. The block list turns off location while a matching app enters the foreground.
- Updated settings categories, labels and search paths while retaining the four home entries. Fixed settings categories that could become inaccessible after restoring an older backup.
- Reduced duplicate Control Center feedback and improved cleanup for device monitoring, key actions and the Recents menu after cancellation or host recreation.
- If Island notices or return animations stop working, check mode, app lists and the return toggle.

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.27.9)

## r14.27.7

Changes since r14.27.5:

- The [r14.27.9 cumulative changelog](https://github.com/thetvplus/customiuizer-a14/blob/r14.27.9/CHANGELOG_EN.md#r14279--2026-11-16) covers the changes still in effect.

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.27.7)

## r14.27.5

Changes since r14.26.11:

- The [r14.27.9 cumulative changelog](https://github.com/thetvplus/customiuizer-a14/blob/r14.27.9/CHANGELOG_EN.md#r14279--2026-11-16) covers the changes still in effect.

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.27.5)

## r14.26.11

Changes since r14.25.9 (cumulative):

- Migrate settings to Compose with consistent lists, search, selectors, sliders, colors and action editors, preserving preferences, backups and behavior. Group app behavior, Wi-Fi and VPN automation under System → Apps and automation.
- Keep home search collapsed until opened, then focus and highlight matches. Improve keyboard handling, navigation, background restoration, language changes and index publication; reuse indexes by language and existing app lists without a query.
- Unify neutral light/dark surfaces and system dynamic colors. Refine rows, menus, current values and spacing; fix empty dialog titles, pressed-state residue and text jitter. Select numeric input automatically and support scrolling around long content and landscape keyboards.
- Add Wi-Fi automation using network names, optional BSSID and priority to adjust DND, ringer, rotation and VPN. Restore only owned state on exit, respecting manual changes and user switches with bounded recovery after failures.
- Add opt-in per-app rules for sensor rotation, media-volume memory, fixed or system brightness, and restrictions on app brightness requests, with Activity exceptions. Restart the device after first enabling.
- Reorganize island media, notifications, live activities and system feedback. Share manually selected media apps between display and return, use selected notification apps, and preserve settings, backups and search routes.
- Unify return effects as None, Instant and Absorption: native return, capture-free reception, or continuous absorption using native window springs. Support adapted older HyperOS 1 launchers with task, PiP, transparency and interruption safeguards.
- Refine island entry, reception, recoil, positioning and offsets. Retain same-session media briefly during track changes, fix stale recording levels after silence, and clear expired clock and activity state.
- Use bounded waveform sampling with display-frame interpolation and stop on settled silence, preserving hidden, paused and power-saving limits. Restore lightweight waveforms after system animations are re-enabled.
- Keep solid and frosted Dock materials, falling back from retired lightweight glass. Fix blur restoration, corner crashes and leftover areas across hiding, unlock and Recents. Avoid blur for solid panels and stop frame synchronization when stable.
- Apply global window-blur changes promptly while retaining developer, power-saving and thermal limits. Separate Recents blur from local island transitions by their actual callers.
- Use caches, system events and on-demand refresh for trusted Wi-Fi and Bluetooth lists, respecting permissions and scan cooldown. Move backup I/O off the UI thread, prevent duplicate operations and resume after permissions; preserve settings on cancellation or invalid choices.
- Add system-app updater disable/restore and options to hide updater and enterprise-mode entries. Preserve state snapshots, result checks and Settings search consistency; follow restart prompts after enabling or updating.
- Fix Control Center columns, screenshot folders and formats, foreground-app observation, nullable interfaces and recovery. Strengthen access checks for system actions, screenshot notifications and battery previews while retaining native failure feedback.
- Correct credential shortcut targets and results, unify keyboard spacing names with explicit Gboard support, refine Duo airplane icons and sizing, and reduce repeated clock, network-speed and charging updates.
- After updating, some island settings may need reselection. If the island or returns are missing, check mode, media/notification lists, return switches and None/Instant/Absorption; reselect as needed and restart affected processes.

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
