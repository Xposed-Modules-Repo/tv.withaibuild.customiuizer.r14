package tv.withaibuild.customiuizer.mods.island

import android.graphics.Path
import kotlin.math.PI
import kotlin.math.cos
import kotlin.math.sin

/** Full capsule ends: G4 curvature ramps join the straight crown to a circular side.
 * Unlike four continuous rectangle corners, curvature stays nonzero at each side's midpoint.
 * The analytic curve is rendered with cached Hermite cubics, not an exact G4 spline.
 */
internal object IslandCapsuleGeometry {
    const val CONTACT_DEPTH_RATIO = .12f
    // 32 Hermite segments per cap; unit controls are generated once and reused.
    private const val STEPS = 32
    private const val RAMP = 0.25

    internal fun heading(t: Double): Double {
        if (t > 1.0) return PI - heading(2.0 - t)
        val integral = if (t < RAMP) {
            val u = t / RAMP
            RAMP * tv.withaibuild.customiuizer.utils.G4Geometry.curvatureIntegral(u)
        } else t - RAMP * 0.5
        return PI * 0.5 * integral / (1 - RAMP * 0.5)
    }

    private val cap = run {
        val result = DoubleArray(STEPS * 6)
        val step = 2.0 / STEPS
        var x = 0.0
        var y = 0.0
        for (i in 0 until STEPS) {
            val a = heading(i * step)
            val b = heading((i + 1) * step)
            val middle = heading((i + 0.5) * step)
            val nx = x + step / 6 * (cos(a) + 4 * cos(middle) + cos(b))
            val ny = y + step / 6 * (sin(a) + 4 * sin(middle) + sin(b))
            val offset = i * 6
            result[offset] = (x + step / 3 * cos(a))
            result[offset + 1] = (y + step / 3 * sin(a))
            result[offset + 2] = (nx - step / 3 * cos(b))
            result[offset + 3] = (ny - step / 3 * sin(b))
            result[offset + 4] = nx
            result[offset + 5] = ny
            x = nx; y = ny
        }
        val precise = FloatArray(result.size) { (result[it] / y).toFloat() }
        // Explicit collinear controls remove finite-cubic curvature at the straight join.
        precise[1] = 0f; precise[3] = 0f
        precise[precise.size - 5] = 1f; precise[precise.size - 3] = 1f
        precise[precise.lastIndex - 1] = 0f
        precise[precise.lastIndex] = 1f
        precise
    }

    fun build(path: Path, width: Float, height: Float, contact: Float = 0f,
        localX: Float = 1f, localY: Float = 1f) {
        // A short meniscus uses the existing shell fill and stays inside its bounds.
        // The end caps still use the same cached G4 controls; no blur/layer is added.
        val amount = if (contact.isFinite()) contact.coerceIn(0f, 1f) else 0f
        val depth = minOf(height * CONTACT_DEPTH_RATIO, (width - height).coerceAtLeast(0f) * .06f) * amount
        val bodyHeight = height - depth
        val extent = cap[(STEPS / 2 - 1) * 6 + 4] * bodyHeight
        val end = minOf(extent, width * 0.5f)
        val sx = if (extent > 0) bodyHeight * end / extent else 0f
        path.moveTo(end * localX, 0f)
        path.lineTo((width - end) * localX, 0f)
        for (side in 0..1) {
            val origin = if (side == 0) width - end else end
            val direction = if (side == 0) 1f else -1f
            val top = if (side == 0) 0f else bodyHeight
            for (i in 0 until STEPS) {
                val at = i * 6
                path.cubicTo((origin + direction * cap[at] * sx) * localX, (top + direction * cap[at + 1] * bodyHeight) * localY,
                    (origin + direction * cap[at + 2] * sx) * localX, (top + direction * cap[at + 3] * bodyHeight) * localY,
                    (origin + direction * cap[at + 4] * sx) * localX, (top + direction * cap[at + 5] * bodyHeight) * localY)
                // Subdivide the smooth end cap without changing its physical curvature.
                // The zero-length side joins let API 34 interpolate the actual contours.
                if (i == STEPS / 2 - 1) path.lineTo(if (side == 0) width * localX else 0f, bodyHeight * .5f * localY)
            }
            if (side == 0) {
                if (depth > 0f) {
                    val center = width * .5f
                    val span = minOf((width - 2f * end) * .3f, bodyHeight * .8f)
                    path.lineTo((center + span) * localX, bodyHeight * localY)
                    path.cubicTo((center + span * .45f) * localX, bodyHeight * localY,
                        (center + span * .55f) * localX, height * localY, center * localX, height * localY)
                    path.cubicTo((center - span * .55f) * localX, height * localY,
                        (center - span * .45f) * localX, bodyHeight * localY, (center - span) * localX, bodyHeight * localY)
                }
                path.lineTo(end * localX, bodyHeight * localY)
            }
        }
        path.close()
    }
}
