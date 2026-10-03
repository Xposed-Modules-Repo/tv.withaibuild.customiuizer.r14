package tv.withaibuild.customiuizer.mods.duostatusbar

/**
 * Two physical SIM slots, no controller references. Native callbacks can arrive off the UI thread;
 * synchronized writers publish one primitive snapshot. Duplicate/activity-only events do no work.
 * Unknown state keeps the corresponding original icon visible.
 */
internal class DuoNetworkState {
    private val ids = intArrayOf(-1, -1)
    private val levels = intArrayOf(-1, -1)
    private val connected = booleanArrayOf(false, false)
    private val dataSim = booleanArrayOf(false, false)
    private var subscriptionsKnown = false
    private var supported = true
    private var wifiKnown = false
    private var wifiConnected = false
    private var wifiLevel = 0
    private var wifiUnvalidated = false
    private var airplane = false

    @Volatile
    var snapshot = 0
        private set

    @Synchronized
    fun subscriptions(first: Int, second: Int, supported: Boolean): Boolean {
        subscriptionsKnown = true
        this.supported = supported
        replaceSlot(0, first)
        replaceSlot(1, second)
        return publish()
    }

    private fun replaceSlot(slot: Int, id: Int) {
        if (ids[slot] == id) return
        ids[slot] = id
        levels[slot] = -1
        connected[slot] = false
        dataSim[slot] = false
    }

    @Synchronized
    fun mobile(slot: Int, id: Int, level: Int, hasService: Boolean, isDataSim: Boolean): Boolean {
        if (slot !in 0..1 || !supported || id < 0) return false
        if (subscriptionsKnown && ids[slot] != id) return false // retired controller callback
        replaceSlot(slot, id)
        levels[slot] = level.coerceIn(0, 4)
        connected[slot] = hasService
        dataSim[slot] = isDataSim
        // A new default-data controller takes priority even before the old one refreshes.
        if (isDataSim) dataSim[1 - slot] = false
        return publish()
    }

    @Synchronized
    fun wifi(enabled: Boolean, isConnected: Boolean, level: Int, validated: Boolean = true): Boolean {
        wifiKnown = true
        wifiConnected = enabled && isConnected
        wifiLevel = if (wifiConnected) level.coerceIn(0, 4) else 0
        wifiUnvalidated = wifiConnected && !validated
        return publish()
    }

    @Synchronized
    fun airplane(enabled: Boolean): Boolean {
        airplane = enabled
        return publish()
    }

    private fun publish(): Boolean {
        val selected = when {
            dataSim[0] && levels[0] >= 0 -> 0
            dataSim[1] && levels[1] >= 0 -> 1
            ids[0] >= 0 && ids[1] < 0 && levels[0] >= 0 -> 0
            ids[1] >= 0 && ids[0] < 0 && levels[1] >= 0 -> 1
            else -> -1
        }
        val noSims = subscriptionsKnown && ids[0] < 0 && ids[1] < 0
        val mobileReady = supported && (noSims || selected >= 0)
        val hasService = !airplane && selected >= 0 && connected[selected]
        val cell = if (hasService) levels[selected] else 0
        val next = (if (wifiKnown) WIFI_READY else 0) or
            (if (mobileReady) MOBILE_READY else 0) or
            (if (wifiConnected) WIFI_CONNECTED else 0) or
            (if (airplane) AIRPLANE else 0) or (if (wifiUnvalidated) WIFI_UNVALIDATED else 0) or
            (if (hasService) MOBILE_SERVICE else 0) or
            (wifiLevel shl WIFI_SHIFT) or (cell shl CELL_SHIFT)
        if (next == snapshot) return false
        snapshot = next
        return true
    }

    companion object {
        const val WIFI_READY = 1
        const val MOBILE_READY = 2
        const val WIFI_CONNECTED = 4
        const val AIRPLANE = 8
        const val WIFI_UNVALIDATED = 1024
        const val MOBILE_SERVICE = 2048
        private const val WIFI_SHIFT = 4
        private const val CELL_SHIFT = 7
        fun wifiLevel(bits: Int) = (bits shr WIFI_SHIFT) and 7
        fun cellLevel(bits: Int) = (bits shr CELL_SHIFT) and 7
    }
}

/** Remember native visibility without accidentally treating our own GONE as the native value. */
internal class DuoVisibility(initial: Int) {
    var original = initial
        private set
    private var hidden = false

    fun observe(current: Int) {
        if (!hidden || current != 8) original = current
    }

    fun hide() { hidden = true }
    fun observeNativeGone() { original = 8 }
    fun release(current: Int = original): Int {
        if (!hidden) original = current
        hidden = false
        return original
    }
}
