package tv.withaibuild.customiuizer.mods.island

import android.app.Activity
import android.graphics.Rect
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers.getField
import java.time.Duration

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class IslandWaveVisibilityTest {
    @Test fun hiddenWaveCancelsFramesRetainsLevelsAndResumesOnItsExistingPhaseClock() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().visible().get()
        val wave = IslandWaveView(activity)
        activity.setContentView(wave, FrameLayout.LayoutParams(25, 24))
        wave.onVisibilityAggregated(true)
        wave.outputVisible = true
        wave.playing = true
        try {
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(99))
            val phase = getField<Float>(wave, "phase")
            assertTrue(phase > 0f)
            wave.outputVisible = false
            assertFalse(getField(wave, "scheduled"))
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(500))
            assertEquals(phase, getField<Float>(wave, "phase"), 0f)
            wave.outputVisible = true
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofMillis(33))
            assertTrue("Resume uses elapsed time without running hidden frames", getField<Float>(wave, "phase") > phase + 2f)
            wave.spectrum(FloatArray(5) { .4f })
            val levels = getField<FloatArray>(wave, "energies").copyOf()
            wave.outputVisible = false
            shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(3))
            assertArrayEquals(levels, getField(wave, "energies"), 0f)
            assertFalse(getField(wave, "scheduled"))
            assertTrue(wave.playing)
        } finally { activity.finish() }
    }

    @Test fun presentationChecksAlphaExplicitClipsSpaceAndVisibilityWithoutLosingMediaState() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup().visible().get()
        val parent = FrameLayout(activity)
        val shell = IslandActivityView(activity)
        parent.addView(shell, FrameLayout.LayoutParams(240, 40))
        activity.setContentView(parent)
        shell.bind("Track", null, null, true, true, true, 0, false)
        shell.geometry(240, 40, 40)
        parent.layout(0, 0, 400, 100)
        shell.measure(View.MeasureSpec.makeMeasureSpec(240, View.MeasureSpec.EXACTLY),
            View.MeasureSpec.makeMeasureSpec(40, View.MeasureSpec.EXACTLY))
        shell.layout(0, 0, 240, 40)
        val attach = getField<Any>(shell, "mAttachInfo")
        org.robolectric.util.ReflectionHelpers.setField(attach, "mWindowVisibility", View.VISIBLE)
        val wave = shell.getChildAt(2) as IslandWaveView
        var changes = 0
        shell.onWaveformVisibilityChanged = { changes++ }
        fun refresh() = shell.refreshWaveformVisibility()
        try {
            refresh(); assertTrue(shell.waveformSampling)
            val baseline = changes
            repeat(120) { refresh() }
            assertEquals(baseline, changes)
            wave.alpha = 0f; refresh()
            assertFalse(shell.waveformSampling); assertFalse(getField(wave, "scheduled"))
            assertTrue(shell.isMedia); assertTrue(wave.playing)
            wave.alpha = 1f; refresh(); assertTrue(shell.waveformSampling)
            shell.clipBounds = Rect(0, 0, wave.left, 40); refresh()
            assertFalse("A nonempty parent clip can completely exclude the bars", shell.waveformSampling)
            shell.clipBounds = null; refresh(); assertTrue(shell.waveformSampling)
            parent.alpha = 0f; refresh(); assertFalse(shell.waveformSampling)
            parent.alpha = 1f; refresh(); assertTrue(shell.waveformSampling)
            wave.visibility = View.GONE; refresh(); assertFalse(shell.waveformSampling)
            wave.visibility = View.VISIBLE; refresh(); assertTrue(shell.waveformSampling)
            shell.clipBounds = Rect(0, 0, 1, 1)
            shell.transition(240, 40, 240, 40, false)
            refresh()
            assertTrue("A finite transition may prepare sampling before its lane appears", shell.waveformSampling)
            assertFalse(wave.outputVisible)
            getField<android.animation.ValueAnimator>(shell, "motion").end()
            refresh(); assertFalse("Preparation ends with the shell clock", shell.waveformSampling)
            shell.clipBounds = null; refresh()
            val oldWidth = wave.width
            wave.layout(wave.left, wave.top, wave.left, wave.bottom); refresh()
            assertFalse(shell.waveformSampling)
            wave.layout(wave.left, wave.top, wave.left + oldWidth, wave.bottom); refresh()
            assertTrue(shell.waveformSampling)
            parent.removeView(shell)
            assertFalse(wave.outputVisible); assertFalse(getField(wave, "scheduled"))
            parent.addView(shell)
            shell.layout(0, 0, 240, 40); refresh()
            assertTrue(shell.waveformSampling)
        } finally { activity.finish() }
    }
}
