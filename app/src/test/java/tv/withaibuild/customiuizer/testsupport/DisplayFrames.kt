package tv.withaibuild.customiuizer.testsupport

import android.os.Looper
import org.robolectric.Shadows.shadowOf
import org.robolectric.shadows.ShadowSystemClock
import java.time.Duration

/** Advance paused native vsync and then drain its posted frame, rather than jumping over callbacks. */
fun advanceDisplayFrames(count: Int = 1, milliseconds: Long = 16) {
    repeat(count) {
        ShadowSystemClock.advanceBy(Duration.ofMillis(milliseconds))
        shadowOf(Looper.getMainLooper()).idle()
    }
}
