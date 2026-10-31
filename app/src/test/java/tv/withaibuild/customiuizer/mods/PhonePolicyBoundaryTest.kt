package tv.withaibuild.customiuizer.mods

import android.app.Activity
import android.app.Application
import io.github.libxposed.api.XposedInterface
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException
import java.util.Calendar

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PhonePolicyBoundaryTest {
    @Test fun optionalForegroundFailureStillAnswersWithOriginalArgumentsOnce() {
        val chain = Chain()
        assertSame(chain.result, Various.answerCallInHeadUpHook { throw IllegalStateException("foreground unavailable") }.intercept(chain))
        assertEquals(1, chain.calls); assertEquals(chain.args, chain.received)
    }
    @Test fun unavailableForegroundPreservesNativeShowUi() {
        val chain = Chain(); assertSame(chain.result, Various.answerCallInHeadUpHook { null }.intercept(chain))
        assertEquals(1, chain.calls); assertEquals(chain.args, chain.received)
    }
    @Test fun nativeAnswerFailureRetainsIdentityWithoutRetry() {
        val failure = IllegalStateException("native answer failed"); val chain = Chain(failure = failure)
        assertSame(failure, assertThrows(IllegalStateException::class.java) { Various.answerCallInHeadUpHook { "example.app" }.intercept(chain) })
        assertEquals(1, chain.calls); assertEquals(false, chain.received!![3])
    }
    @Test fun fatalOptionalLookupEscapesWithoutAnswering() {
        val failure = OutOfMemoryError("lookup fatal"); val chain = Chain()
        assertSame(failure, assertThrows(OutOfMemoryError::class.java) { Various.answerCallInHeadUpHook { throw InvocationTargetException(failure) }.intercept(chain) })
        assertEquals(0, chain.calls)
    }
    @Test fun desktopAnswerRetainsNativePresentation() {
        val chain = Chain(); Various.answerCallInHeadUpHook { "com.miui.home" }.intercept(chain)
        assertEquals(1, chain.calls); assertEquals(chain.args, chain.received)
    }
    @Test fun existingNonUiAnswerNeverQueriesForeground() {
        val chain = Chain(showUi = false); Various.answerCallInHeadUpHook { error("unnecessary query") }.intercept(chain)
        assertEquals(1, chain.calls); assertEquals(chain.args, chain.received)
    }
    @Test fun foregroundAppOnlyChangesShowUiAndKeepsOtherArguments() {
        val chain = Chain(); Various.answerCallInHeadUpHook { "example.app" }.intercept(chain)
        assertEquals(1, chain.calls); assertEquals(false, chain.received!![3])
        for (i in 0..2) assertSame(chain.args[i], chain.received!![i])
    }
    @Test fun daytimeSuppressionIncludesStartAndExcludesEnd() = checkPeriod(8, 0, 10, 0,
        mapOf(7 * 60 + 59 to false, 8 * 60 to true, 9 * 60 + 59 to true, 10 * 60 to false))
    @Test fun overnightSuppressionIncludesStartAndMidnightAndExcludesEnd() = checkPeriod(22, 0, 7, 0,
        mapOf(21 * 60 + 59 to false, 22 * 60 to true, 0 to true, 6 * 60 + 59 to true, 7 * 60 to false))
    @Test fun equalEndpointsSuppressBrightnessForTheWholeDay() = checkPeriod(8, 15, 8, 15,
        mapOf(0 to true, 8 * 60 + 14 to true, 8 * 60 + 15 to true, 8 * 60 + 16 to true, 1439 to true))
    @Test fun disabledNightRuleDoesNotReadClockOrSuppressBrightness() {
        val old = Various.variousConfig; val activity = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            Various.variousConfig = old.copy(callUiBrightType = 3, callUiBrightNight = false, callUiBrightVal = 45)
            val chain = Chain(owner = activity.get()); Various.inCallBrightnessHook(javaClass.classLoader!!) { error("unnecessary clock") }.intercept(chain)
            assertEquals(.45f, activity.get().window.attributes.screenBrightness, .001f); assertEquals(1, chain.calls)
        } finally { Various.variousConfig = old; activity.pause().stop().destroy() }
    }
    private fun checkPeriod(sh: Int, sm: Int, eh: Int, em: Int, expected: Map<Int, Boolean>) {
        val old = Various.variousConfig; val activity = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            Various.variousConfig = old.copy(callUiBrightType = 3, callUiBrightNight = true, callUiBrightVal = 45,
                callUiBrightNightStartHour = sh, callUiBrightNightStartMinute = sm, callUiBrightNightEndHour = eh, callUiBrightNightEndMinute = em)
            for ((minute, suppressed) in expected) {
                activity.get().window.attributes = activity.get().window.attributes.apply { screenBrightness = .2f }
                val chain = Chain(owner = activity.get())
                assertSame(chain.result, Various.inCallBrightnessHook(javaClass.classLoader!!) {
                    Calendar.getInstance().apply { set(2026, Calendar.OCTOBER, 30, minute / 60, minute % 60, 0) }
                }.intercept(chain))
                assertEquals("minute=$minute suppressed=$suppressed", if (suppressed) .2f else .45f,
                    activity.get().window.attributes.screenBrightness, .001f)
                assertEquals(1, chain.calls)
            }
        } finally { Various.variousConfig = old; activity.pause().stop().destroy() }
    }
    private class Chain(showUi: Boolean = true, val owner: Any = Any(), val failure: Throwable? = null) : XposedInterface.Chain {
        val result = Any(); var calls = 0; var received: List<Any?>? = null
        private val arguments = listOf(Any(), "call-id", 3, showUi)
        override fun getExecutable(): Executable = error("No reflective method access")
        override fun getThisObject(): Any = owner
        override fun getArgs(): List<Any?> = arguments
        override fun getArg(index: Int): Any? = arguments[index]
        override fun proceed(): Any? { received = arguments; calls++; failure?.let { throw it }; return result }
        override fun proceed(args: Array<Any?>): Any? { received = args.toList(); calls++; failure?.let { throw it }; return result }
        override fun proceedWith(receiver: Any): Any? = error("No receiver rewriting")
        override fun proceedWith(receiver: Any, args: Array<Any>): Any? = error("No receiver rewriting")
    }
}
