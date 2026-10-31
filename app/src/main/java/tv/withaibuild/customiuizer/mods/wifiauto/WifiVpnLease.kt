package tv.withaibuild.customiuizer.mods.wifiauto

import tv.withaibuild.customiuizer.mods.vpn.VpnAutoSwitchCoordinator.Backend
import tv.withaibuild.customiuizer.mods.vpn.VpnAutoSwitchPolicy.Session
import tv.withaibuild.customiuizer.utils.VpnAutoSwitchSettings.Target

/** VPN ownership uses the native runner/connection token, never only package or connected=true. */
internal class WifiVpnLease {
    private var original: Session? = null
    private var last: Session? = null
    private var tracked = false
    private var owned = false
    private var overridden = false

    fun reconcile(action: Int?, target: Target, busy: Boolean, backend: Backend) {
        if (!tracked && action == null) return
        if (busy) { abandon(true); return } // Application automation has priority; do not stop its session.
        val state = backend.snapshot()
        if (!state.known || state.protected) { abandon(true); return }
        if (tracked && !same(last, state.session)) { abandon(true); return }
        if (overridden) return
        if (action == null) {
            if (owned && !same(original, state.session)) {
                if (original == null) state.session?.let(backend::stop)
                else if (state.session == null && original!!.target.kind == "system") backend.start(original!!.target)
            }
            abandon(false)
            return
        }
        if (!target.valid || (state.session != null && state.session.target != target)) { abandon(true); return }
        if (!tracked) { original = state.session; last = state.session; tracked = true }
        if ((action == 1) == (state.session != null)) return
        // The reused manager's app-start paths may open UI or lack a connect contract. Do not automate them.
        if (target.kind != "system") { abandon(true); return }
        val success = if (action == 1) backend.start(target) else state.session?.let(backend::stop) == true
        val after = backend.snapshot()
        if (success && after.known && !after.protected &&
            (if (action == 1) after.session?.target == target else after.session == null)) {
            last = after.session; owned = true
        } else abandon(true)
    }

    private fun same(a: Session?, b: Session?) = if (a == null) b == null else a.same(b)
    private fun abandon(manual: Boolean) { original = null; last = null; tracked = false; owned = false; overridden = overridden || manual }
    fun end() { abandon(false); overridden = false }
}
