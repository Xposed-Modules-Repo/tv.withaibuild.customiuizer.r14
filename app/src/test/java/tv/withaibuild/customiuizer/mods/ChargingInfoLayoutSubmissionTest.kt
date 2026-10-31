package tv.withaibuild.customiuizer.mods

import android.app.Application
import android.content.Context
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.text.method.SingleLineTransformationMethod
import android.widget.TextView
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Executable
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.utils.PrefMap

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ChargingInfoLayoutSubmissionTest {
    private lateinit var view: CountingTextView
    private lateinit var prefs: PrefMap
    private var savedPrefs: Map<String, Any> = emptyMap()

    @Before fun setUp() {
        savedPrefs = MainModule.mPrefs.getAll()
        view = CountingTextView(RuntimeEnvironment.getApplication()).apply { setSingleLine(true) }
        prefs = PrefMap().apply {
            put("system_charginginfo", true)
            put("system_charginginfo_fontsize", 16)
            put("system_charginginfo_view", "1")
        }
    }

    @After fun tearDown() { MainModule.mPrefs.replaceSnapshot(savedPrefs) }

    @Test fun stableMultilineStyleDoesNotResubmitOrRequestLayoutOneHundredTimes() {
        apply()
        view.resetCounts()
        repeat(100) { apply() }
        println("stable multiline: setters=${view.singleLineCalls}, requestLayout=${view.layoutRequests}")
        assertEquals(0, view.singleLineCalls)
        assertEquals(0, view.layoutRequests)
        assertMultiline(view)
    }

    @Test fun stableOriginalSingleLineStyleDoesNotResubmitOneHundredTimes() {
        prefs.put("system_charginginfo_view", "2")
        apply()
        view.resetCounts()
        repeat(100) { apply() }
        println("stable original: setters=${view.singleLineCalls}, requestLayout=${view.layoutRequests}")
        assertEquals(0, view.singleLineCalls)
        assertEquals(0, view.layoutRequests)
        assertSingleLine(view)
    }

    @Test fun disabledStyleRestoresOnceThenStopsRepeatingLayoutWork() {
        apply()
        prefs.put("system_charginginfo", false)
        apply()
        assertSingleLine(view)
        view.resetCounts()
        repeat(100) { apply() }
        assertEquals(0, view.singleLineCalls)
        assertEquals(0, view.layoutRequests)
    }

    @Test fun nativeIndicationsAndTextChangesStillRunOnceWithoutExtraModuleLayout() {
        MainModule.mPrefs.replaceSnapshot(prefs.getAll())
        val hook = SystemLockScreenHooks.chargingInfoIndicationHook()
        apply()
        var submissions = 0
        var extraLayouts = 0
        repeat(100) { index ->
            val chain = Chain(view) {
                view.text = "Charging $index"
                view.resetCounts() // count only work after the original native text update
            }
            assertSame(chain.result, hook.intercept(chain))
            assertEquals(1, chain.calls)
            submissions += view.singleLineCalls
            extraLayouts += view.layoutRequests
        }
        println("native indications: original=100, module setters=$submissions, extra layouts=$extraLayouts")
        assertEquals(0, submissions)
        assertEquals(0, extraLayouts)
        assertEquals("Charging 99", view.text.toString())
    }

    @Test fun matchingFalseFlagStillRepairsEveryNativeMultilineDependency() {
        val mutations: List<(TextView) -> Unit> = listOf(
            { it.maxLines = 1 },
            { it.maxHeight = 30 },
            { it.setHorizontallyScrolling(true) },
            { it.transformationMethod = PasswordTransformationMethod.getInstance() },
        )
        for (mutate in mutations) {
            val target = CountingTextView(view.context).apply { setSingleLine(false) }
            SystemLockScreenHooks.applyChargingInfoStyle(target, prefs)
            mutate(target)
            assertFalse(target.isSingleLine)
            target.resetCounts()
            SystemLockScreenHooks.applyChargingInfoStyle(target, prefs)
            assertEquals(1, target.singleLineCalls)
            assertMultiline(target)
        }
    }

    @Test fun matchingTrueFlagStillRepairsEveryNativeSingleLineDependency() {
        prefs.put("system_charginginfo_view", "3")
        val mutations: List<(TextView) -> Unit> = listOf(
            { it.minLines = 2 },
            { it.minHeight = 30 },
            { it.maxLines = 2 },
            { it.maxHeight = 40 },
            { it.setHorizontallyScrolling(false) },
            { it.transformationMethod = PasswordTransformationMethod.getInstance() },
        )
        for (mutate in mutations) {
            val target = CountingTextView(view.context).apply { setSingleLine(true) }
            SystemLockScreenHooks.applyChargingInfoStyle(target, prefs)
            mutate(target)
            assertTrue(target.isSingleLine)
            target.resetCounts()
            SystemLockScreenHooks.applyChargingInfoStyle(target, prefs)
            assertEquals(1, target.singleLineCalls)
            assertSingleLine(target)
        }
    }

    @Test fun nativeMinimumLinesArePreservedInMultilineMode() {
        apply()
        view.minLines = 4
        view.resetCounts()
        apply()
        assertEquals(4, view.minLines)
        assertMultiline(view)
        assertEquals(0, view.singleLineCalls)
        assertEquals(0, view.layoutRequests)
    }

    @Test fun editableBuffersKeepTheCompleteNativeSetterIncludingLengthFilters() {
        view.setText("editable", TextView.BufferType.EDITABLE)
        apply()
        assertNotNull(view.editableText)
        assertMultiline(view)
        view.resetCounts()
        repeat(3) { apply() }
        assertEquals("editable views retain native filter reconciliation", 3, view.singleLineCalls)
        prefs.put("system_charginginfo_view", "2")
        apply()
        assertSingleLine(view)
        assertTrue(view.filters.any { it is android.text.InputFilter.LengthFilter })
        prefs.put("system_charginginfo_view", "1")
        apply()
        assertTrue(view.filters.none { it is android.text.InputFilter.LengthFilter })
    }

    @Test fun textInputViewsKeepNativeInputTypeReconciliationEvenWithMatchingGeometry() {
        apply()
        view.setRawInputType(InputType.TYPE_CLASS_TEXT)
        view.resetCounts()
        apply()
        assertEquals(1, view.singleLineCalls)
        assertTrue(view.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE != 0)
        prefs.put("system_charginginfo_view", "2")
        apply()
        assertEquals(0, view.inputType and InputType.TYPE_TEXT_FLAG_MULTI_LINE)
    }

    @Test fun ownerReplacementUsesItsOwnOriginalSingleLineState() {
        apply()
        val replacement = CountingTextView(view.context).apply { setSingleLine(false); minLines = 3 }
        SystemLockScreenHooks.applyChargingInfoStyle(replacement, prefs)
        prefs.put("system_charginginfo", false)
        SystemLockScreenHooks.applyChargingInfoStyle(replacement, prefs)
        assertFalse(replacement.isSingleLine)
        assertEquals(3, replacement.minLines)
        assertFalse(view.isSingleLine) // another owner was not restored accidentally
        apply()
        assertSingleLine(view)
    }

    private fun apply() = SystemLockScreenHooks.applyChargingInfoStyle(view, prefs)

    private fun assertMultiline(target: TextView) {
        assertFalse(target.isSingleLine)
        assertEquals(Int.MAX_VALUE, target.maxLines)
        assertFalse(target.isHorizontallyScrollable)
        assertNull(target.transformationMethod)
    }

    private fun assertSingleLine(target: TextView) {
        assertTrue(target.isSingleLine)
        assertEquals(1, target.minLines)
        assertEquals(1, target.maxLines)
        assertTrue(target.isHorizontallyScrollable)
        assertSame(SingleLineTransformationMethod.getInstance(), target.transformationMethod)
    }

    private class CountingTextView(context: Context) : TextView(context) {
        var singleLineCalls = 0
        var layoutRequests = 0
        override fun setSingleLine(singleLine: Boolean) { singleLineCalls++; super.setSingleLine(singleLine) }
        override fun requestLayout() { layoutRequests++; super.requestLayout() }
        fun resetCounts() { singleLineCalls = 0; layoutRequests = 0 }
    }

    private class Chain(private val owner: Any, private val native: () -> Unit) : XposedInterface.Chain {
        val result = Any()
        var calls = 0
        override fun getExecutable(): Executable = error("unused")
        override fun getThisObject(): Any = owner
        override fun getArgs(): List<Any?> = error("unused")
        override fun getArg(index: Int): Any? = error("unused")
        override fun proceed(): Any { calls++; native(); return result }
        override fun proceed(args: Array<Any?>): Any? = error("unused")
        override fun proceedWith(receiver: Any): Any? = error("unused")
        override fun proceedWith(receiver: Any, args: Array<Any>): Any? = error("unused")
    }
}
