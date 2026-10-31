package tv.withaibuild.customiuizer.mods

import android.app.Activity
import android.app.Application
import android.content.Context
import android.os.Message
import io.github.libxposed.api.XposedInterface
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class VariousHostFailureBoundaryTest {
    private fun hooks(): List<MethodHook> = listOf(Various.batteryTemperatureHook("owner"),
        Various.showCallUiHook(), Various.inCallBrightnessHook(javaClass.classLoader!!))

    @Test fun nativeFailureDoesNotReadIncompleteOwners() {
        for (hook in hooks()) {
            val error = IllegalStateException("native failure")
            val chain = Chain(nativeFailure = error)
            assertSame(error, assertThrows(IllegalStateException::class.java) { hook.intercept(chain) })
            assertEquals(1, chain.calls); assertEquals(0, chain.ownerReads); assertEquals(0, chain.argReads)
        }
    }
    @Test fun nativeFatalRetainsIdentityWithoutModuleWork() {
        for (hook in hooks()) {
            val error = OutOfMemoryError("native fatal"); val chain = Chain(nativeFailure = error)
            assertSame(error, assertThrows(OutOfMemoryError::class.java) { hook.intercept(chain) })
            assertEquals(1, chain.calls); assertEquals(0, chain.ownerReads)
        }
    }
    @Test fun ordinaryModuleFailureRetainsNativeResult() {
        for (hook in hooks()) {
            val chain = Chain(ownerFailure = IllegalStateException("owner unavailable"))
            assertSame(chain.result, hook.intercept(chain)); assertEquals(1, chain.calls)
        }
    }
    @Test fun moduleFatalEscapes() {
        for (hook in hooks()) {
            val error = OutOfMemoryError("module fatal"); val chain = Chain(ownerFailure = error)
            assertSame(error, assertThrows(OutOfMemoryError::class.java) { hook.intercept(chain) })
            assertEquals(1, chain.calls)
        }
    }
    @Test fun wrappedModuleFatalEscapesUnwrapped() {
        for (hook in hooks()) {
            val error = OutOfMemoryError("wrapped fatal")
            val chain = Chain(ownerFailure = InvocationTargetException(error))
            assertSame(error, assertThrows(OutOfMemoryError::class.java) { hook.intercept(chain) })
            assertEquals(1, chain.calls)
        }
    }
    @Test fun successfulIncomingCallStillShowsUiAndCancelsNotification() {
        val old = Various.variousConfig
        try {
            Various.variousConfig = old.copy(showCallUi = 2)
            val owner = Presenter(RuntimeEnvironment.getApplication())
            val chain = Chain(owner = owner, result = true)
            assertEquals(true, Various.showCallUiHook().intercept(chain))
            assertEquals(1, chain.calls); assertEquals(1, owner.shows); assertEquals(1, owner.mStatusBarNotifier.cancels)
        } finally { Various.variousConfig = old }
    }
    @Test fun unsuccessfulNativeStartCannotShowCallUi() {
        val owner = Presenter(RuntimeEnvironment.getApplication()); val chain = Chain(owner = owner, result = false)
        assertEquals(false, Various.showCallUiHook().intercept(chain)); assertEquals(0, owner.shows)
        assertEquals(0, owner.mStatusBarNotifier.cancels); assertEquals(1, chain.calls)
    }
    @Test fun successfulCreationStillAppliesConfiguredBrightness() {
        val old = Various.variousConfig
        val controller = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            Various.variousConfig = old.copy(callUiBrightType = 3, callUiBrightNight = false, callUiBrightVal = 45)
            val chain = Chain(owner = controller.get(), result = null)
            assertNull(Various.inCallBrightnessHook(javaClass.classLoader!!).intercept(chain))
            assertEquals(.45f, controller.get().window.attributes.screenBrightness, .001f); assertEquals(1, chain.calls)
        } finally { Various.variousConfig = old; controller.pause().stop().destroy() }
    }
    @Test fun successfulBatteryMessageUpdatesTheCurrentWeakOwner() {
        val activity = Robolectric.buildActivity(Activity::class.java).setup()
        try {
            activity.get().sendStickyBroadcast(android.content.Intent(android.content.Intent.ACTION_BATTERY_CHANGED).putExtra("temperature", 235))
            val fragment = Fragment(activity.get())
            val owner = BatteryOwner(fragment)
            val chain = Chain(owner = owner, result = null)
            assertNull(Various.batteryTemperatureHook("owner").intercept(chain))
            assertEquals("23℃", fragment.preference.text); assertEquals(1, chain.calls)
        } finally { activity.pause().stop().destroy() }
    }
    @Test fun installedBindingsUseTheTestedCallbacks() {
        var root = java.io.File(java.lang.System.getProperty("user.dir")).absoluteFile
        val path = "app/src/main/java/tv/withaibuild/customiuizer/mods/Various.kt"
        while (!java.io.File(root, path).isFile) root = root.parentFile ?: error("Repository not found")
        val source = java.io.File(root, path).readText()
        assertTrue(source.contains("Message::class.java, batteryTemperatureHook(finalFieldName))"))
        assertTrue(source.contains("\"startUi\", showCallUiHook())"))
        assertTrue(source.contains("Bundle::class.java, inCallBrightnessHook(lpparam.classLoader))"))
        assertTrue(source.contains("Boolean::class.javaPrimitiveType!!, answerCallInHeadUpHook())"))
    }
    class Preference { var text = ""; fun setText(value: CharSequence) { text = value.toString() } }
    class Fragment(private val activity: Activity) {
        val preference = Preference()
        fun getActivity() = activity
        fun findPreference(key: String): Preference { assertEquals("reference_current_temp", key); return preference }
    }
    class BatteryOwner(fragment: Fragment) { @JvmField val owner = java.lang.ref.WeakReference(fragment) }
    class Notifier { var cancels = 0; fun cancelInCall() { cancels++ } }
    class Presenter(@JvmField val mContext: Context) {
        @JvmField val mStatusBarNotifier = Notifier(); var shows = 0
        fun showInCall(a: Boolean, b: Boolean) { assertFalse(a); assertFalse(b); shows++ }
    }
    private class Chain(val owner: Any = Any(), val result: Any? = true,
        val nativeFailure: Throwable? = null, val ownerFailure: Throwable? = null) : XposedInterface.Chain {
        var calls = 0; var ownerReads = 0; var argReads = 0
        override fun getExecutable(): Executable = error("No reflective chain access")
        override fun getThisObject(): Any { ownerReads++; ownerFailure?.let { throw it }; return owner }
        override fun getArgs(): List<Any?> = error("No argument copies")
        override fun getArg(index: Int): Any? { argReads++; return if (owner is Presenter) "INCOMING" else Message.obtain().apply { what = 1 } }
        override fun proceed(): Any? { calls++; nativeFailure?.let { throw it }; return result }
        override fun proceed(args: Array<Any?>): Any? = error("No rewriting")
        override fun proceedWith(receiver: Any): Any? = error("No receiver rewriting")
        override fun proceedWith(receiver: Any, args: Array<Any>): Any? = error("No receiver rewriting")
    }
}
