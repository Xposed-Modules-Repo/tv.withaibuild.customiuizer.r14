# 米客 A14 — Changelog

[简体中文](CHANGELOG_CN.md) | English

## r14.27.10

Changes since r14.27.9:

- With notification shade and Control Center gestures swapped, the notification shade can continue a pull-down when Control Center declines it.
- Custom Control Center tile columns adapt to portrait and landscape. If the tile layout hook is unavailable, the system panel layout and column count are retained. The edit entry and security notice can still be hidden independently.
- The Control Center carrier text follows system visibility. Step counts start refreshing when the header attaches, move as it expands and follow the system privacy indicator's visibility rules.
- Dual-row indicators update the second SIM when the primary SIM state is unchanged and restore the single-SIM display after switching back. Detailed network speed uses the system refresh and reset, and battery styles use the system battery state.
- Volume and brightness percentages use native slider progress. Extended timer intervals and labels line up with the native slider in portrait and landscape.
- Refine the native entry points for home-screen icon title size, folder width and double-tap gestures on the verified MiuiHome and POCO Launcher versions.

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.27.10)

## r14.27.9

A stable milestone for HyperOS 1 / Android 14.

- DECK keeps native scrolling, task dismissal and quick switching. Card size and window animation are adjustable. Landscape openings use native window coordinates, and Dock blur is restored on return to Home.
- The iOS gesture indicator follows native visibility and color. Keep the system handle visible with full-screen gestures and restart Launcher and System UI after changing the setting.
- Dynamic Island offers adjustable absorption width and leaves room for native landscape alarm and timer windows. Check mode, app lists and the return toggle if notifications or return animations stop working.
- The status bar offers Duo, iOS battery and five-dot cellular styles. The notification-shade media card uses native media sessions and playback controls.
- Use a narrow default with side volume, or adjust center volume placement and expand it to access native mute and Do Not Disturb. Both styles use native volume and routing. Show percentage in an overlay, Toast or Dynamic Island.
- Configure location automation under Apps and Tools and reboot Android after first enabling it. The allow list can enable location for a matching app. With auto-close enabled, removing the last matching card can turn off module-enabled location. The block list can turn location off when a matching app enters the foreground. Both support a 0–30 second delay.
- Settings categories and search paths are aligned. Existing preferences and backups remain usable, and an update with the same signature preserves app data.

[Release](https://github.com/thetvplus/customiuizer-a14/releases/tag/r14.27.9)
