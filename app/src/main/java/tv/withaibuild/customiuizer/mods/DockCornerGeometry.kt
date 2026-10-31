package tv.withaibuild.customiuizer.mods

import android.graphics.Path

/** One native outer outline for every Dock material. */
internal object DockCornerGeometry {
    /** The Launcher panel sets the scale. Physical display corners are only a ceiling. */
    fun radius(screenRadius: Float, manual: Float, fallback: Float, limit: Float): Float {
        val automatic = minOf(fallback, limit * .64f,
            if (screenRadius > 0f) screenRadius else fallback)
        val radius = if (manual > 0f) manual else automatic
        return radius.coerceIn(0f, limit.coerceAtLeast(0f))
    }
    fun outline(path: Path, width: Float, height: Float, radius: Float) {
        val r = radius.coerceIn(0f, minOf(width, height).coerceAtLeast(0f) * .5f)
        path.addRoundRect(0f, 0f, width, height, r, r, Path.Direction.CW)
    }
}
