package tv.withaibuild.customiuizer.mods.duostatusbar

import tv.withaibuild.customiuizer.utils.PrefMap

/** Read once at installation; changing these settings requires a SystemUI restart. */
internal class DuoConfig(val showPercent: Boolean, sizeDp: Int,
    val autoSize: Boolean = true, val showAudio: Boolean = true, val bold: Boolean = false,
) {
    val sizeDp = sizeDp.coerceIn(20, 40)

    companion object {
        fun read(prefs: PrefMap) = DuoConfig(
            prefs.getBoolean("system_statusbar_duo_percent", true),
            prefs.getInt("system_statusbar_duo_size", 24),
            prefs.getBoolean("system_statusbar_duo_autosize", true),
            prefs.getBoolean("system_statusbar_duo_audio", true),
            prefs.getBoolean("system_statusbar_duo_bold", false),
        )
    }
}

internal object DuoSizing {
    /** A little room for the ring, bounded by the native row during measurement. */
    fun pixels(config: DuoConfig, nativeHeight: Int, density: Float): Int {
        val dp = if (!config.autoSize) config.sizeDp.toFloat()
            else if (nativeHeight > 0) (nativeHeight / density * 1.1f).coerceIn(20f, 24f)
            else 22f
        return (dp * density + 0.5f).toInt().coerceAtLeast(1)
    }
}

internal object DuoGeometry {
    const val RING_START = 148.7f
    const val BOTTOM_GAP = 117.4f

    fun topGap(showPercent: Boolean, charging: Boolean): Float = when {
        charging -> 55.6f
        showPercent -> 71.3f
        else -> 0f
    }

    fun sideSweep(topGap: Float) = (360f - BOTTOM_GAP - topGap) / 2f
    fun leftFill(level: Int, side: Float) = side * level.coerceIn(0, 50) / 50f
    fun rightFill(level: Int, side: Float) = side * (level - 50).coerceIn(0, 50) / 50f

    fun batteryColor(foreground: Int, level: Int, charging: Boolean, saver: Boolean): Int = when {
        charging -> 0xff34c759.toInt()
        saver -> 0xfff2b900.toInt()
        level in 0..19 -> 0xffff3b30.toInt()
        else -> foreground
    }
}
