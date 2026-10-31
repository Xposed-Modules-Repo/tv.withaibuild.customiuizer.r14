package tv.withaibuild.customiuizer.mods.island

import android.graphics.Outline
import android.graphics.Path
import android.graphics.Region
import android.view.View
import android.view.ViewOutlineProvider

/** API 34 clips arbitrary Outline paths. Reuse geometry until bounds, scale or contact changes. */
internal open class IslandShape : ViewOutlineProvider() {
    val path = Path()
    private val region = Region()
    private var width = -1
    private var height = -1
    private var horizontal = 1f
    private var vertical = 1f
    private var contact = 0f
    private var regionDirty = false

    fun resize(w: Int, h: Int, scaleX: Float = horizontal, scaleY: Float = vertical, fusion: Float = contact): Boolean {
        val sx = scaleX.coerceAtLeast(.01f)
        val sy = scaleY.coerceAtLeast(.01f)
        val blend = if (w * sx > h * sy && fusion.isFinite()) fusion.coerceIn(0f, 1f) else 0f
        if (w == width && h == height && horizontal == sx && vertical == sy && contact == blend) return false
        width = w
        height = h
        horizontal = sx; vertical = sy; contact = blend
        path.rewind()
        if (w > 0 && h > 0) {
            // Construct in visible pixels, then undo the parent's stretch locally. The
            // straight centre changes length while the physical end caps stay round.
            // Emit local controls directly instead of transforming the completed Path.
            IslandCapsuleGeometry.build(path, w * sx, h * sy, blend, 1f / sx, 1f / sy)
            regionDirty = true
        } else { region.setEmpty(); regionDirty = false }
        return true
    }

    fun contains(x: Float, y: Float): Boolean {
        if (regionDirty) {
            region.setPath(path, Region(0, 0, width, height))
            regionDirty = false
        }
        return region.contains(x.toInt(), y.toInt())
    }

    override fun getOutline(view: View, outline: Outline) {
        resize(view.width, view.height, horizontal, vertical)
        if (path.isEmpty) outline.setEmpty() else outline.setPath(path)
    }
}
