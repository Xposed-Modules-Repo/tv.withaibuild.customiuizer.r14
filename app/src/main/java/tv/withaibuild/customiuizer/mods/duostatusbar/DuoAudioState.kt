package tv.withaibuild.customiuizer.mods.duostatusbar

/** Connection indicators, not a claim about the current media route. No device list or Binder calls. */
internal class DuoAudioState {
    private var wired = false
    private var bluetoothAudio = false
    @Volatile var snapshot = 0
        private set

    @Synchronized fun wired(connected: Boolean): Boolean {
        wired = connected
        return publish()
    }

    @Synchronized fun bluetooth(enabled: Boolean, connected: Boolean, audioOnly: Boolean, active: Boolean): Boolean {
        bluetoothAudio = enabled && connected && (audioOnly || active)
        return publish()
    }

    @Synchronized fun clear(): Boolean {
        wired = false
        bluetoothAudio = false
        return publish()
    }

    private fun publish(): Boolean {
        val next = (if (wired) WIRED else 0) or (if (bluetoothAudio) BLUETOOTH else 0)
        if (next == snapshot) return false
        snapshot = next
        return true
    }

    companion object {
        const val WIRED = 1
        const val BLUETOOTH = 2
        const val WIFI_GLYPH = 0
        const val PLANE_GLYPH = 1
        const val WIRED_GLYPH = 2
        const val BLUETOOTH_GLYPH = 3

        fun glyph(network: Int, audio: Int): Int = when {
            network and DuoNetworkState.WIFI_UNVALIDATED != 0 -> WIFI_GLYPH
            network and DuoNetworkState.WIFI_CONNECTED == 0 &&
                network and DuoNetworkState.AIRPLANE != 0 -> PLANE_GLYPH
            audio and WIRED != 0 -> WIRED_GLYPH
            audio and BLUETOOTH != 0 -> BLUETOOTH_GLYPH
            else -> WIFI_GLYPH
        }
    }
}
