package tv.withaibuild.customiuizer.mods.island

import android.os.SystemClock
import android.view.View
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.annotation.RealObject
import org.robolectric.shadows.ShadowView
import org.robolectric.util.ReflectionHelpers

/** Keep actual wave state / rendering in controller tests without auto-vsync recursion.
 * Only this app View is instrumented; native Choreographer / window setup stays intact.
 * Dedicated wave tests exercise the original scheduler with paused 8 / 16 ms vsync.
 */
@Implements(className = "tv.withaibuild.customiuizer.mods.island.IslandWaveView", isInAndroidSdk = false)
class IslandLifecycleFrameShadow : ShadowView() {
    @RealObject private lateinit var owner: View

    @Implementation
    protected fun schedule() {
        if (!ReflectionHelpers.callInstanceMethod<Boolean>(owner, "shouldRender")) {
            ReflectionHelpers.setField(owner, "lastFrame", 0L)
            return
        }
        if (ReflectionHelpers.getField<Boolean>(owner, "scheduled")) return
        if (ReflectionHelpers.getField<Long>(owner, "lastFrame") == 0L)
            ReflectionHelpers.setField(owner, "lastFrame", SystemClock.uptimeMillis())
        ReflectionHelpers.setField(owner, "scheduled", true)
        owner.postDelayed(ReflectionHelpers.getField<Runnable>(owner, "frame"), 16L)
    }
}
