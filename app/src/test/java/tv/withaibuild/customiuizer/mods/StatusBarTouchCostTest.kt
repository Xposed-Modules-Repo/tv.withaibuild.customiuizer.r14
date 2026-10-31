package tv.withaibuild.customiuizer.mods

import android.app.Application
import android.view.MotionEvent
import android.view.View
import io.github.libxposed.api.XposedInterface
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import java.lang.reflect.Executable

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class StatusBarTouchCostTest {
    @Test fun touchObservationUsesSingleArgumentAndLeavesNativeDispatchUnchanged() {
        val owner = View(RuntimeEnvironment.getApplication())
        var observations = 0
        val hook = SystemUIControlCenterHooks.statusBarTouchHook { event, view ->
            assertSame(owner, view)
            assertEquals(42, event.deviceId)
            assertEquals(java.lang.System.identityHashCode(owner), event.ownerId)
            observations++
        }
        val event = MotionEvent.obtain(0, 10, MotionEvent.ACTION_MOVE, 1f, 2f, 1f, 1f, 0, 1f, 1f, 42, 0)
        try {
            val chain = TouchChain(owner, event)
            repeat(120) { assertSame(chain.result, hook.intercept(chain)) }
            assertEquals(120, observations)
            assertEquals(120, chain.nativeCalls)
            assertEquals(0, chain.listReads)
            assertEquals(0, chain.replacementCalls)
            assertEquals(120, chain.argumentReads)
        } finally { event.recycle() }
    }

    @Test fun observationFailureDoesNotRetryOrSuppressOriginalFailure() {
        val owner = View(RuntimeEnvironment.getApplication())
        val event = MotionEvent.obtain(0, 10, MotionEvent.ACTION_UP, 0f, 0f, 0)
        val hook = SystemUIControlCenterHooks.statusBarTouchHook { _, _ -> error("optional failure") }
        try {
            val nativeError = IllegalArgumentException("native failure")
            val chain = TouchChain(owner, event, nativeError)
            try { hook.intercept(chain); fail("Expected native failure") }
            catch (actual: IllegalArgumentException) { assertSame(nativeError, actual) }
            assertEquals(1, chain.nativeCalls)
        } finally { event.recycle() }
    }

    @Test fun fatalObservationFailureEscapesBeforeNativeDispatch() {
        val owner = View(RuntimeEnvironment.getApplication())
        val event = MotionEvent.obtain(0, 10, MotionEvent.ACTION_DOWN, 0f, 0f, 0)
        val fatal = OutOfMemoryError("module fatal")
        val hook = SystemUIControlCenterHooks.statusBarTouchHook { _, _ -> throw fatal }
        val chain = TouchChain(owner, event)
        try {
            try { hook.intercept(chain); fail("Expected fatal") }
            catch (actual: OutOfMemoryError) { assertSame(fatal, actual) }
            assertEquals(0, chain.nativeCalls)
        } finally { event.recycle() }
    }

    private class TouchChain(val owner: View, val event: MotionEvent, val failure: Throwable? = null) : XposedInterface.Chain {
        val result = Any()
        var listReads = 0
        var argumentReads = 0
        var nativeCalls = 0
        var replacementCalls = 0
        override fun getExecutable(): Executable = View::class.java.getMethod("onTouchEvent", MotionEvent::class.java)
        override fun getThisObject(): Any = owner
        override fun getArgs(): List<Any?> { listReads++; return listOf(event) }
        override fun getArg(index: Int): Any? { argumentReads++; return event }
        override fun proceed(): Any { nativeCalls++; failure?.let { throw it }; return result }
        override fun proceed(args: Array<Any?>): Any { replacementCalls++; return proceed() }
        override fun proceedWith(thisObject: Any): Any? = error("unused")
        override fun proceedWith(thisObject: Any, args: Array<Any>): Any? = error("unused")
    }
}
