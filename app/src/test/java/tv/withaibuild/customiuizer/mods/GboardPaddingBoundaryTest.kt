package tv.withaibuild.customiuizer.mods

import android.app.Application
import io.github.libxposed.api.XposedInterface
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.feature.InputMethodGboardPaddingFeature
import tv.withaibuild.customiuizer.utils.PrefMap
import java.lang.reflect.Executable

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class GboardPaddingBoundaryTest {
    private val oldConfig = Various.variousConfig
    private val oldPrefs = MainModule.mPrefs.getAll()
    @After fun restore() { MainModule.mPrefs.replaceSnapshot(oldPrefs); Various.variousConfig = oldConfig }
    @Test fun portraitAndLandscapeKeepIndependentValuesWithinTheUiRange() {
        Various.variousConfig = oldConfig.copy(gboardPaddingPort = 1, gboardPaddingLand = 100)
        val hook = Various.gboardPaddingHook(); val port = Chain(PORT); val land = Chain(LAND)
        assertEquals("1", hook.intercept(port)); assertEquals("100", hook.intercept(land)); assertEquals(0, port.calls + land.calls)
    }
    @Test fun defaultsPreserveBothNativePropertyValues() {
        Various.variousConfig = oldConfig.copy(gboardPaddingPort = 0, gboardPaddingLand = 0)
        for (key in listOf(PORT, LAND)) { val chain = Chain(key); assertSame(chain.result, Various.gboardPaddingHook().intercept(chain)); assertEquals(1, chain.calls) }
    }
    @Test fun unrelatedPropertyPreservesItsNativeResult() {
        Various.variousConfig = oldConfig.copy(gboardPaddingPort = 42, gboardPaddingLand = 100)
        val chain = Chain("ro.unrelated.property"); assertSame(chain.result, Various.gboardPaddingHook().intercept(chain)); assertEquals(1, chain.calls)
    }
    @Test fun nativeFailureRetainsIdentityAndIsNeverRetried() {
        Various.variousConfig = oldConfig.copy(gboardPaddingPort = 0)
        val failure = IllegalStateException("native property failed"); val chain = Chain(PORT, failure)
        assertSame(failure, assertThrows(IllegalStateException::class.java) { Various.gboardPaddingHook().intercept(chain) }); assertEquals(1, chain.calls)
    }
    @Test fun existingHookReadsUpdatedAndRestoredSnapshots() {
        val hook = Various.gboardPaddingHook()
        MainModule.mPrefs.replaceSnapshot(mapOf("various_gboardpadding_port" to 42, "various_gboardpadding_land" to 16)); Various.refreshVariousSnapshot()
        assertEquals("42", hook.intercept(Chain(PORT))); assertEquals("16", hook.intercept(Chain(LAND)))
        MainModule.mPrefs.replaceSnapshot(emptyMap<String, Any>()); Various.refreshVariousSnapshot()
        val restored = Chain(PORT); assertSame(restored.result, hook.intercept(restored)); assertEquals(1, restored.calls)
    }
    @Test fun landscapeOnlySettingEnablesGboardButNotOtherInputMethods() {
        val prefs = PrefMap(); prefs.put("various_gboardpadding_land", 16)
        assertTrue(InputMethodGboardPaddingFeature.evaluateEnabled(prefs, "com.google.android.inputmethod.latin"))
        assertFalse(InputMethodGboardPaddingFeature.evaluateEnabled(prefs, "com.sohu.inputmethod.sogou.xiaomi"))
        assertFalse(InputMethodGboardPaddingFeature.evaluateEnabled(PrefMap(), "com.google.android.inputmethod.latin"))
    }
    private class Chain(val key: String, val failure: Throwable? = null) : XposedInterface.Chain {
        val result = String(charArrayOf('n', 'a', 't', 'i', 'v', 'e')); var calls = 0
        override fun getExecutable(): Executable = error("No reflective method access")
        override fun getThisObject(): Any = error("No owner reads")
        override fun getArgs(): List<Any?> = error("No argument copies")
        override fun getArg(index: Int): Any? { assertEquals(0, index); return key }
        override fun proceed(): Any? { calls++; failure?.let { throw it }; return result }
        override fun proceed(args: Array<Any?>): Any? = error("No rewriting")
        override fun proceedWith(receiver: Any): Any? = error("No rewriting")
        override fun proceedWith(receiver: Any, args: Array<Any>): Any? = error("No rewriting")
    }
    companion object { const val PORT = "ro.com.google.ime.kb_pad_port_b"; const val LAND = "ro.com.google.ime.kb_pad_land_b" }
}
