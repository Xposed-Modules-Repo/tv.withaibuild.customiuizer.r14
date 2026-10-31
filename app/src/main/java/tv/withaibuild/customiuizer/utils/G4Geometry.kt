package tv.withaibuild.customiuizer.utils

/** Integral of the shared quintic curvature ramp used by Island capsules. */
internal object G4Geometry {
    fun curvatureIntegral(u: Double): Double = u * u * u * u * (2.5 - 3 * u + u * u)
}
