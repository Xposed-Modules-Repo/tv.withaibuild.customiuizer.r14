# 米客 A14 — Changelog

[简体中文](CHANGELOG_CN.md) | English

## r14.27.7

Changes since r14.27.5:

- Select apps under Various → Automatic location. The allowlist turns location on, and the blocklist turns it off. Both modes support a 0–30 second delay before turning location off. The allowlist starts the delay when the last matching card is removed. The blocklist starts it when a listed app enters the foreground. Restart Android after enabling the feature for the first time.
- DECK uses the same card layout calculation when entered from Home or an app. The native card projects the thumbnail outline, preserving the side shadows of deeper cards and the window handoff.
- Launcher folder blur fades smoothly when opening and closing folders.
- Dynamic Island absorption can size the leading edge automatically or use a custom width set under Effects and Waveform.
- Expanding the central volume panel restores the native mute and Do Not Disturb shortcuts.

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.27.7)

## r14.27.5

Changes since r14.26.11:

- Add an optional iOS 12 central volume HUD while retaining native volume, routing and expanded controls. Upgrades keep the side style and the saved narrow-bar choice.
- Add default-off iOS 10 five-dot cellular signals using native network state, hiding and no-service fallback, with Duo taking priority.
- Add a default-off iOS media presentation using the notification shade's native media sessions, actions and progress controls. Its display lifecycle stays independent of island media. Control Center media is not replaced.
- Add a default-off iOS battery presentation retaining native charging and power-saving states. Duo, battery hiding and existing battery styles take priority.
- Reorganize settings within pages and update search routes while preserving the four home entries, saved keys, defaults and backup format.
- Fix shared-hook and plugin-loading isolation and fallback after partial initialization. Improve cancellation and resource release for monitoring, key combinations and Recents menus.
- Refine the central volume HUD with vector icons and signed vertical positioning. The central style hides the mute and Do Not Disturb shortcuts and reclaims their space.
- Make the volume percentage overlay more compact and add an automatic toast capsule or Dynamic Island option. Existing settings keep the overlay by default, and repeated adjustments update the same hint.
- Restore Dock background and blur when its view reattaches or Home returns. Refine the battery percentage, five-dot spacing and notification-shade media card.
- Fix DECK card jumps on release, initial refill scale and window shadow handoff. When opening a back card, foreground cards move toward their recentered positions. The app window and its settled card share one shadow during the swipe.

- Fix DECK scaling interfering with app switching along the Home Indicator. Quick switch keeps system geometry; entering the deck and opening its cards retain DECK motion.
- Give DECK, Duo and iOS Home Indicator switches their full feature names for easier discovery and search. Shorten descriptions while preserving setup and restart requirements.
- Fix position and size jumps when a neighboring card fills the gap after dismissing the leftmost recents deck card.
- Reject stale card and window geometry during repeated orientation changes to improve position, size and direction handoff.
- Restore the launcher's native rotation policy, including landscape task containers inside portrait windows. Convert opening-window and neighboring-card coordinates consistently, with shared iOS motion in both orientations. Restart the launcher after updating.
- Align card and window scaling centers when native entry changes the pivot. Complete handoff checks for both landscape directions and unprepared layouts.
- Fix double scaling and window misalignment during swipe-up entry on newer launchers. Fold neighboring cards using the current native window, reducing extra spring lag and stepped position changes.
- Fix rear-card title and icon opacity on swipe-up deck entry without requiring another scroll.
- Reuse the system's retargetable black/white gesture-handle tint animation, with a 250 ms ordinary gesture-mode transition. Preserve explicit window-handoff timing.
- Restore right alignment for long-island trailing content, mirrored at the outer edge in RTL layouts. Camera clearance constrains the available lane and clipping; resizing and translation preserve the outer margin. Keep existing measured or manual capsule widths without changing text alignment to fill space.
- Add visible horizontal expansion and recoil on first island appearance, music returning home, and new activity reception. Keep same-size handoffs from ending early, reduce vertical expansion and press enlargement, and preserve interrupted and reversed transitions.
- Add an opt-in iOS recents deck for Horizontal Recents on supported HyperOS 1 launchers, in portrait and landscape. Preserve native scrolling, task dismissal and window handoff.
- Add card sizing, independently scaled titles and icons with depth-based fading, and optional window motion. Select between two native window implementations by their interfaces instead of exact version numbers, including the older POCO spring interface; retain native timing when optional motion interfaces are unavailable.
- Fix card depth during dragging, dismissal, cancellation and return, window-to-card corner alignment, and premature card recycling. Add directional fling snapping; unsupported layouts restore native hit testing, input and scroll bounds.
- Add an opt-in iOS gesture indicator using native visibility, color and input. Admission now follows native drawing capabilities rather than device or build lists. Enable full-screen gestures, keep the system handle visible, and restart System UI.
- Add an iOS volume appearance with portrait and landscape support, continuous corners and native shadows. The thin bar moves toward the screen edge, stays thin during repeated key presses and expands for touch adjustment. Preserve native placement, volume steps, routing, mute state and expanded controls.
- Add “Start with thin bar” to appear thin and return to thin after touch adjustment. The footer transitions from three dots to one; fix the first tap taking focus instead of opening expanded controls.
- Follow native volume-fill animation and remove dark edge overpainting. Add maximum/minimum key feedback, touch overdrag and release recoil, with smoothly rounded thin-bar ends.
- Refine island resizing, idle forms, content changes and press feedback, including mid-animation reversals. Shorten activity handoff waits, retain existing content until the next activity makes contact, and add lateral shake and recoil when restoring ringing.
- Refine absorption entry and island acceptance. Remove the liquid dent, translucent leading edge and extra vertical compression, and avoid exciting the entire island again during acceptance. Preserve native return trajectories, springs, recall and picture-in-picture handoff.
- Prevent top cutout reservations from inflating automatic height and the camera mask. Restore the music island’s final exit timing to reduce translucent residue, and improve ordinary Toast fitting at larger font sizes and shorter island heights.
- Prioritize the actual function or mode in system feedback and show secondary state when space permits. Reveal text through its available area without an extra font-size pulse. Same-notification updates retain visible content while invalidating old click actions and continuing native privacy checks.
- Catch up initial foreground state for live activities; keep native foreground-change observation when singleton lookup is unavailable. Yield to native alarm/timer ringing in landscape and restore the island afterwards. Complete status-bar unbinding even if controller cleanup fails.
- Update Duo cellular, Wi-Fi, airplane and headphone arrangements, percentage/lightning spacing and four-dot arc symmetry. Preserve existing size, offset, spacing and headphone-scale settings. Handle small HD/roaming status icons by available fields, skip missing icons locally, and remove the unnecessary region restriction.
- Use a licensed rounded substitute for Duo numbers and network labels; make network-label weight follow the existing bold switch. Refine automatic size, central Wi-Fi proportions and state transitions.
- Avoid duplicate state messages after Control Center automatically collapses for a tile click, while preserving native feedback on failure. Keep the hide-mute/DND options effective after native volume-footer refreshes.
- Fix empty pages when restoring old settings categories and route volume and Recents options to their current pages. Simplify some feature names and complete translations while preserving preference keys, search, backups and upgrade compatibility.

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
