package tv.withaibuild.customiuizer.mods.wifiauto

import tv.withaibuild.customiuizer.utils.WifiAutomaticSettings.Identity

/** null entry = identity pending/redacted; an empty map alone proves disconnection. */
internal class WifiNetworkTracker {
    data class State(val known: Boolean, val identity: Identity? = null)
    private val wifi = HashMap<Any, Identity?>()
    private val pending = HashSet<Any>()
    private val vpn = HashSet<Any>()
    private var overflow = false
    private var initialized = false
    val snapshotReady get() = initialized && !overflow
    fun initialReadComplete() { initialized = true }
    fun invalidateSnapshot() { initialized = false }
    /** Only a complete native enumeration may lift an overflow quarantine. */
    fun beginSnapshot() {
        initialized = false
        wifi.clear(); pending.clear(); vpn.clear(); overflow = false
    }
    private fun accepts(key: Any): Boolean {
        if (key in wifi || key in pending || key in vpn || wifi.size + pending.size + vpn.size < 16) return true
        overflow = true
        return false
    }
    fun available(key: Any) { if (accepts(key) && key !in wifi && key !in vpn) pending.add(key) }
    fun wifi(key: Any, identity: Identity?) { if (!accepts(key)) return; pending.remove(key); vpn.remove(key); wifi[key] = identity }
    fun vpn(key: Any) { if (!accepts(key)) return; pending.remove(key); wifi.remove(key); vpn.add(key) }
    fun lost(key: Any) { pending.remove(key); wifi.remove(key); vpn.remove(key) }
    fun state(): State {
        if (!initialized || overflow || pending.isNotEmpty() || wifi.values.any { it == null }) return State(false)
        var identity: Identity? = null
        for (candidate in wifi.values) {
            if (identity != null && identity != candidate) return State(false)
            identity = candidate
        }
        return State(true, identity)
    }
}
