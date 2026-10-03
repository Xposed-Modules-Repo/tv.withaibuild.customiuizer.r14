package tv.withaibuild.customiuizer.mods.duostatusbar

/** Connection indicators, not a claim about the current media route. No device list or Binder calls. */
internal class DuoAudioState {
    private var wired = false
    private var bluetoothAudio = false
    private var batteryStep = 0
    @Volatile var snapshot = 0
        private set
    @Volatile var expiresAt = 0L
        private set

    @Synchronized fun wired(connected: Boolean): Boolean {
        wired = connected
        return publish()
    }

    @Synchronized fun bluetooth(enabled: Boolean, connected: Boolean, audioOnly: Boolean, active: Boolean,
        nowMillis: Long, notify: Boolean,
    ): Boolean {
        val next = enabled && connected && (audioOnly || active)
        if (next != bluetoothAudio) batteryStep = 0 // never carry an old device's battery into a new hint
        if (next && !bluetoothAudio && notify) expiresAt = nowMillis + BLUETOOTH_HINT_MS
        if (!next || (expiresAt != 0L && expiresAt <= nowMillis)) expiresAt = 0L
        bluetoothAudio = next
        return publish()
    }

    @Synchronized fun expire(nowMillis: Long): Boolean {
        if (expiresAt == 0L || nowMillis < expiresAt) return false
        return finishHint()
    }

    @Synchronized fun finishHint(): Boolean {
        expiresAt = 0L
        batteryStep = 0
        return publish()
    }

    /** HyperOS reports hands-free battery in ten steps; absent/ambiguous reports stay unknown. */
    @Synchronized fun bluetoothBattery(step: Int, singleDevice: Boolean): Boolean {
        batteryStep = if (bluetoothAudio && expiresAt != 0L && singleDevice && step in 1..10) step else 0
        return publish()
    }

    @Synchronized fun clear(): Boolean {
        wired = false
        bluetoothAudio = false
        batteryStep = 0
        expiresAt = 0L
        return publish()
    }

    private fun publish(): Boolean {
        val next = (if (wired) WIRED else 0) or
            (if (expiresAt != 0L) BLUETOOTH or (batteryStep shl 2) else 0)
        if (next == snapshot) return false
        snapshot = next
        return true
    }

    companion object {
        const val WIRED = 1
        const val BLUETOOTH = 2
        const val BLUETOOTH_HINT_MS = 3000L
        const val WIFI_GLYPH = 0
        const val PLANE_GLYPH = 1
        const val WIRED_GLYPH = 2
        const val BLUETOOTH_GLYPH = 3
        const val CELLULAR_GLYPH = 4
        const val OFFLINE_GLYPH = 5
        fun batteryStep(audio: Int) = (audio shr 2) and 15

        fun glyph(network: Int, audio: Int, networkFallback: Boolean = true): Int = when {
            network and DuoNetworkState.WIFI_UNVALIDATED != 0 -> WIFI_GLYPH
            network and DuoNetworkState.WIFI_CONNECTED == 0 &&
                network and DuoNetworkState.AIRPLANE != 0 -> PLANE_GLYPH
            audio and WIRED != 0 -> WIRED_GLYPH
            audio and BLUETOOTH != 0 -> BLUETOOTH_GLYPH
            network and DuoNetworkState.WIFI_CONNECTED == 0 && networkFallback ->
                if (network and DuoNetworkState.MOBILE_SERVICE != 0) CELLULAR_GLYPH else OFFLINE_GLYPH
            else -> WIFI_GLYPH
        }
    }
}
