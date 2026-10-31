package tv.withaibuild.customiuizer.mods.utils.gesture

import org.junit.Assert.*
import org.junit.Test

class GestureBrightnessReadCostTest {
    class Display {
        var reads = 0
        var failure: Throwable? = null
        fun getBrightness(id: Int): Float {
            reads++
            failure?.let { throw it }
            return 0.25f
        }
    }

    private class Fixture(val config: GestureConfig) {
        val display = Display()
        val effects = FakeGestureEffectExecutor()
        val machine = GestureMachine("cost", { config }, object : GestureDependenciesResolver {
            override fun prepare(ownerId: Int, classLoaderIdentity: String, context: Any) =
                GestureDependenciesResult.Ready(GestureDependencies(ownerId, classLoaderIdentity,
                    display, 0, 0f, 1f, Any(), 80, 1000, 1f,
                    getBrightnessMethod = Display::class.java.getMethod("getBrightness", Int::class.java)))
        }, effects)
        init { machine.prepare(1, this) }
        fun event(action: Int, x: Float = 0f, y: Float = 10f, time: Long = 0,
            pointers: Int = 1, entry: GestureEntry = GestureEntry.STATUS_BAR_TOUCH) =
            GestureEvent(entry, action, 0, time, x, y, pointers, ownerId = 1)
        fun send(action: Int, x: Float = 0f, time: Long = 0, pointers: Int = 1) =
            machine.dispatch(event(action, x, time = time, pointers = pointers), this)
    }

    @Test fun volumeAndTapOnlySessionsDoNotReadBrightness() {
        for (config in listOf(GestureConfig(singleAction = 3, dualAction = 3), GestureConfig(doubleTapAction = 7))) {
            val f = Fixture(config)
            repeat(120) { i ->
                f.send(GestureAction.DOWN, time = i * 1000L)
                f.send(GestureAction.MOVE, x = 200f, time = i * 1000L + 20)
                f.send(GestureAction.UP, x = 200f, time = i * 1000L + 40)
            }
            assertEquals(0, f.display.reads)
            if (config.singleAction == 3) assertEquals(120, f.effects.volumeAdjusted.size)
            assertEquals(120, f.effects.resets)
        }
    }

    @Test fun rejectedControlCenterDownDoesNotReadBrightness() {
        val f = Fixture(GestureConfig(singleAction = 2))
        repeat(120) {
            f.machine.dispatch(f.event(GestureAction.DOWN, y = 81f, entry = GestureEntry.CONTROL_CENTER_TOUCH), f)
        }
        assertEquals(0, f.display.reads)
        assertEquals(GestureState.IDLE, f.machine.snapshot(1).state)
        assertTrue(f.effects.brightnessApplied.isEmpty())
    }

    @Test fun possibleDualBrightnessStillCapturesDownValueBeforePointerChange() {
        val f = Fixture(GestureConfig(singleAction = 3, dualAction = 2))
        f.send(GestureAction.DOWN)
        f.send(GestureAction.POINTER_DOWN, pointers = 2, time = 10)
        f.send(GestureAction.MOVE, x = 200f, pointers = 2, time = 20)
        f.send(GestureAction.UP, x = 200f, time = 30)
        assertEquals(1, f.display.reads)
        assertEquals(0.25f + 0.2f * 0.618f, f.effects.brightnessApplied.single(), 0.0001f)
        assertEquals(f.effects.brightnessApplied, f.effects.brightnessCommitted)
    }

    @Test fun brightnessReadFailureAndFatalPropagationRemainIntact() {
        val f = Fixture(GestureConfig(singleAction = 2))
        f.display.failure = IllegalStateException("display unavailable")
        f.send(GestureAction.DOWN)
        f.send(GestureAction.MOVE, x = 200f, time = 20)
        assertTrue(f.effects.brightnessApplied.isEmpty())
        val fatal = OutOfMemoryError("fatal")
        f.display.failure = fatal
        try { f.send(GestureAction.DOWN); fail("Expected fatal") }
        catch (actual: OutOfMemoryError) { assertSame(fatal, actual) }
    }
}
