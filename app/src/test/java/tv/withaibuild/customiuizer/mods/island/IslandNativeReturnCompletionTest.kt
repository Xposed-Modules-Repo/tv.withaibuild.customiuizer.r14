package tv.withaibuild.customiuizer.mods.island

import android.animation.ValueAnimator
import android.animation.Animator
import android.animation.AnimatorListenerAdapter
import android.app.Activity
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.SharedMemory
import android.widget.FrameLayout
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.shadows.ShadowChoreographer
import org.robolectric.util.ReflectionHelpers.getField
import java.nio.ByteOrder

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class IslandNativeReturnCompletionTest {
    @Test fun aMinimumDefaultCapsuleKeepsTheCameraWidthDuringIntake() {
        val host = Robolectric.buildActivity(Activity::class.java).setup().visible().get()
        val memory = SharedMemory.create("minimum-default-return", 4)
        val writer = memory.mapReadWrite().order(ByteOrder.nativeOrder())
        val shell = IslandActivityView(host).apply { absorption = false }
        host.setContentView(shell, FrameLayout.LayoutParams(40, 40))
        ShadowChoreographer.setPaused(true)
        try {
            shell.bind("Music", null, ColorDrawable(Color.RED), true, false, false, 0, false)
            shell.geometry(40, 40, 40, 40)
            shell.cameraAnchor(20f, 38f)
            shell.prepareEntry()
            shell.transition(40, 40, 40, 40, entering = true)
            getField<ValueAnimator>(shell, "motion").end()
            shell.acceptReturnProgress(IslandReturnProgress(memory.mapReadOnly()))
            shell.prepareReturnFromCurrent()
            shell.transition(40, 40, 40, 40, entering = false)
            val clock = getField<ValueAnimator>(shell, "motion")
            for (time in 0L..800L step 10L) {
                writer.putInt(0, (time / 120f * 1_000_000).toInt().coerceAtMost(1_000_000))
                clock.currentPlayTime = time
                assertEquals("An inward cue cannot shrink the camera seed", 40f, shell.scaleX * 40f, .001f)
            }
            assertNull(getField<Any?>(shell, "motion"))
        } finally {
            getField<ValueAnimator?>(shell, "motion")?.cancel()
            shell.cancelMediaReturn()
            SharedMemory.unmap(writer); memory.close(); host.finish()
            ShadowChoreographer.setPaused(false)
        }
    }

    @Test fun aLateDefaultChannelDoesNotReplayTheReceivingGesture() {
        val host = Robolectric.buildActivity(Activity::class.java).setup().visible().get()
        val memory = SharedMemory.create("late-default-return", 4)
        val writer = memory.mapReadWrite().order(ByteOrder.nativeOrder())
        val shell = IslandActivityView(host).apply { absorption = false }
        host.setContentView(shell, FrameLayout.LayoutParams(240, 40))
        ShadowChoreographer.setPaused(true)
        try {
            shell.bind("Music", null, ColorDrawable(Color.RED), true, false, false, 0, false)
            shell.geometry(240, 40, 40, 260)
            shell.cameraAnchor(20f, 16f)
            shell.prepareEntry()
            shell.transition(240, 40, 240, 40, entering = true)
            getField<ValueAnimator>(shell, "motion").end()
            writer.putInt(0, 980_000)
            shell.acceptReturnProgress(IslandReturnProgress(memory.mapReadOnly()))
            shell.prepareReturnFromCurrent()
            shell.transition(240, 40, 240, 40, entering = false)
            val clock = getField<ValueAnimator>(shell, "motion")
            writer.putInt(0, 1_000_000)
            for (time in 0L..500L step 20L) {
                clock.currentPlayTime = time
                assertEquals("An already arrived page cannot replay intake or recoil", 240f, shell.scaleX * 240f, .001f)
            }
            assertNull(getField<Any?>(shell, "motion"))
        } finally {
            getField<ValueAnimator?>(shell, "motion")?.cancel()
            shell.cancelMediaReturn()
            SharedMemory.unmap(writer); memory.close(); host.finish()
            ShadowChoreographer.setPaused(false)
        }
    }

    @Test fun defaultReturnKeepsItsReboundWhenTheNativePageCompletesEarly() {
        for (hz in listOf(60, 90, 120)) for (existing in listOf(false, true))
            for (finishAt in listOf(120L, 260L)) for (maximumWidth in listOf(240, 260)) {
            val host = Robolectric.buildActivity(Activity::class.java).setup().visible().get()
            val memory = SharedMemory.create("default-return-rebound", 4)
            val writer = memory.mapReadWrite().order(ByteOrder.nativeOrder())
            val shell = IslandActivityView(host).apply { absorption = false }
            host.setContentView(shell, FrameLayout.LayoutParams(240, 40))
            ShadowChoreographer.setPaused(true)
            try {
                shell.bind("Music", null, ColorDrawable(Color.RED), true, false, false, 0, false)
                shell.geometry(240, 40, 40, maximumWidth)
                shell.cameraAnchor(20f, 16f)
                if (existing) {
                    shell.prepareEntry()
                    shell.transition(240, 40, 240, 40, entering = true)
                    getField<ValueAnimator>(shell, "motion").end()
                }
                shell.acceptReturnProgress(IslandReturnProgress(memory.mapReadOnly()))
                if (existing) shell.prepareReturnFromCurrent() else shell.prepareEntry(mediaReturn = true)
                shell.transition(240, 40, 240, 40, entering = !existing)
                val clock = getField<ValueAnimator>(shell, "motion")
                var endings = 0
                clock.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) { endings++ }
                })
                var peak = 0f
                var minimum = Float.POSITIVE_INFINITY
                var peakAt = 0L
                for (step in 0..hz * 4 / 5) {
                    val time = step * 1000L / hz
                    writer.putInt(0, (time.toFloat() / finishAt * 1_000_000).toInt().coerceAtMost(1_000_000))
                    clock.currentPlayTime = time
                    if (time < IslandMotion.returnDuration(0f, false))
                        assertSame("Page completion cannot retire the island's receiving clock", clock, getField(shell, "motion"))
                    val width = shell.scaleX * 240f
                    if (width > peak) { peak = width; peakAt = time }
                    minimum = minOf(minimum, width)
                    assertTrue("The rebound respects the prepared width", width <= maximumWidth + .001f)
                }
                if (maximumWidth > 240) {
                    assertTrue("Default must have a visible rebound: existing=$existing hz=$hz finish=$finishAt peak=$peak", peak > 241f)
                    assertTrue("A fast page cannot compress the island's recoil", peakAt > finishAt)
                } else if (existing) {
                    assertTrue("A width cap cannot suppress the inward receiving response", minimum < 239f)
                }
                assertEquals(1, endings)
                assertNull(getField<Any?>(shell, "motion"))
                assertEquals(1f, shell.scaleX, 0f)
                assertEquals(1f, shell.scaleY, 0f)
            } finally {
                getField<ValueAnimator?>(shell, "motion")?.cancel()
                shell.cancelMediaReturn()
                SharedMemory.unmap(writer); memory.close(); host.finish()
                ShadowChoreographer.setPaused(false)
            }
        }
    }

    @Test fun absorptionRespondsDuringIntakeAfterBothAFirstLaunchAndReopeningFromTheIsland() {
        for (existing in listOf(false, true)) {
            val host = Robolectric.buildActivity(Activity::class.java).setup().visible().get()
            val memory = SharedMemory.create("island-return-intake", 4)
            val writer = memory.mapReadWrite().order(ByteOrder.nativeOrder())
            val shell = IslandActivityView(host).apply { absorption = true }
            host.setContentView(shell, FrameLayout.LayoutParams(240, 40))
            ShadowChoreographer.setPaused(true)
            try {
                shell.bind("Music", null, ColorDrawable(Color.RED), true, false, false, 0, false)
                shell.geometry(240, 40, 40, 260)
                shell.cameraAnchor(20f, 16f)
                if (existing) {
                    shell.prepareEntry()
                    shell.transition(240, 40, 240, 40, entering = true)
                    getField<ValueAnimator>(shell, "motion").end()
                }
                shell.acceptReturnProgress(IslandReturnProgress(memory.mapReadOnly()))
                if (existing) shell.prepareReturnFromCurrent() else shell.prepareEntry(mediaReturn = true)
                shell.transition(240, 40, 240, 40, entering = !existing)
                val clock = getField<ValueAnimator>(shell, "motion")
                val origin = shell.scaleX
                writer.putInt(0, 60_000)
                clock.currentPlayTime = 40L
                assertTrue("Intake must move the island before page completion; existing=$existing",
                    kotlin.math.abs(shell.scaleX - origin) * 240f > 1f)
                writer.putInt(0, 550_000)
                clock.currentPlayTime = 320L
                assertTrue("The body also reacts while the page is still travelling", shell.scaleY < .99f)
                assertEquals(.55f, getField<Float>(shell, "mediaReturnProgress"), .00001f)
            } finally {
                getField<ValueAnimator?>(shell, "motion")?.cancel()
                shell.cancelMediaReturn()
                SharedMemory.unmap(writer); memory.close(); host.finish()
                ShadowChoreographer.setPaused(false)
            }
        }
    }

    @Test fun anArrivalInterruptingATopExitCannotCarryItsUpwardVelocityIntoTheNewIsland() {
        val host = Robolectric.buildActivity(Activity::class.java).setup().visible().get()
        val memory = SharedMemory.create("island-return-origin", 4)
        val writer = memory.mapReadWrite().order(ByteOrder.nativeOrder())
        val shell = IslandActivityView(host).apply { absorption = true }
        host.setContentView(shell, FrameLayout.LayoutParams(240, 40))
        ShadowChoreographer.setPaused(true)
        try {
            shell.bind("Music", null, ColorDrawable(Color.RED), true, false, false, 0, false)
            shell.geometry(240, 40, 40)
            shell.prepareEntry()
            shell.transition(240, 40, 240, 40, entering = true)
            getField<ValueAnimator>(shell, "motion").end()
            shell.exit {}
            getField<ValueAnimator>(shell, "motion").currentPlayTime = 55L
            val origin = shell.translationY + shell.pivotY * (1f - shell.scaleY)
            assertTrue(origin < 0f)
            shell.acceptReturnProgress(IslandReturnProgress(memory.mapReadOnly()))
            shell.resumeEntry()
            shell.prepareReturnFromCurrent()
            shell.transition(240, 40, 240, 40, entering = false)
            val clock = getField<ValueAnimator>(shell, "motion")
            var previous = origin
            for (step in 0..10) {
                writer.putInt(0, step * 100_000)
                clock.currentPlayTime = step * 25L
                val top = shell.translationY + shell.pivotY * (1f - shell.scaleY)
                assertTrue("The new arrival cannot continue travelling up", top >= previous - .0001f)
                assertTrue(top <= .0001f)
                previous = top
            }
        } finally {
            getField<ValueAnimator?>(shell, "motion")?.cancel()
            shell.cancelMediaReturn()
            SharedMemory.unmap(writer); memory.close(); host.finish()
            ShadowChoreographer.setPaused(false)
        }
    }

    @Test fun nativeCompletionAndCancellationEndOnceWithoutReenteringTheFrameCallback() {
        for (absorption in listOf(false, true)) for (terminal in intArrayOf(1_000_000, -1_000_000)) {
            val host = Robolectric.buildActivity(Activity::class.java).setup().visible().get()
            val memory = SharedMemory.create("island-return-completion", 4)
            val writer = memory.mapReadWrite().order(ByteOrder.nativeOrder())
            val shell = IslandActivityView(host).apply { this.absorption = absorption }
            host.setContentView(shell, FrameLayout.LayoutParams(240, 40))
            ShadowChoreographer.setPaused(true)
            var clock: ValueAnimator? = null
            try {
                shell.bind("Music", null, ColorDrawable(Color.RED), true, false, false, 0, false)
                shell.geometry(240, 40, 40)
                shell.acceptReturnProgress(IslandReturnProgress(memory.mapReadOnly()))
                shell.prepareEntry(mediaReturn = true)
                shell.transition(240, 40, 240, 40, entering = true)
                clock = getField(shell, "motion")
                assertNotNull("Exercise the native-driven clock, including its terminal frame", clock)
                assertTrue("Both effects start with a camera seed instead of a full-size jump", shell.scaleX < 1f)
                var endings = 0
                clock!!.addListener(object : AnimatorListenerAdapter() {
                    override fun onAnimationEnd(animation: Animator) { endings++ }
                })
                writer.putInt(0, 400_000)
                clock!!.currentPlayTime = 200L
                assertEquals(.4f, getField<Float>(shell, "mediaReturnProgress"), .00001f)
                assertEquals(0, endings)
                writer.putInt(0, terminal)
                clock!!.currentPlayTime = 400L
                if (terminal > 0) {
                    assertEquals("Page completion must not cut off the island's damped settling", 0, endings)
                    assertSame(clock, getField<Any?>(shell, "motion"))
                    clock!!.currentPlayTime = 850L
                }
                assertEquals("Exactly one terminal callback", 1, endings)
                clock!!.currentPlayTime = 950L // A stale pulse cannot resume the retired owner.
                assertNull(getField<Any?>(shell, "motion"))
                assertNull(getField<Any?>(shell, "returnProgress"))
                assertEquals(1f, shell.scaleX, 0f)
                assertEquals(1f, shell.alpha, 0f)
                assertEquals(1f, getField<Float>(shell, "mediaReturnProgress"), 0f)
            } finally {
                clock?.removeAllUpdateListeners()
                clock?.cancel()
                shell.cancelMediaReturn()
                SharedMemory.unmap(writer)
                memory.close()
                host.finish()
                ShadowChoreographer.setPaused(false)
            }
        }
    }
}
