package tv.withaibuild.customiuizer.prefs

import android.app.Application
import android.content.Context
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.utils.AppHelper

/** Storage, ownership and callback contracts shared by Compose drag and exact input. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class SeekBarPreferenceInputTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private val previous = AppHelper.appPrefs
    @Before fun setup() { AppHelper.appPrefs = context.getSharedPreferences("seekbar-input", Context.MODE_PRIVATE).also { it.edit().clear().commit() } }
    @After fun close() { AppHelper.appPrefs = previous }
    private fun preference(min: Int = 0, max: Int = 800, shift: Int = 400, divider: Int = 1,
        default: Int = 400, off: String? = null, step: Int = 1, key: String = "pref_key_test") =
        SeekBarPreference(context, null).apply {
            this.key = key; title = "Offset"
            configureComposeValues(min, max, step, default, shift, divider, true, "%s dp", off, null, true)
        }
    @Test fun replacingLinkedBoundsPreservesTheValueUntilTheCompleteNewRangeIsApplied() {
        val p = preference(min = 60, max = 100, shift = 80, default = 90)
        p.setValue(90); p.setValueRange(80, 160); assertEquals(90, p.getValue())
        p.setValueRange(0, 80); assertEquals(80, p.getValue())
        p.setValueRange(66, 94); assertEquals(80, p.getValue())
    }
    @Test fun exactInputSavesOnceAndDeliversStorageValueToTheBusinessCallback() {
        val p = preference(); val events = mutableListOf<String>(); var writes = 0
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, _ -> writes++ }
        AppHelper.appPrefs!!.registerOnSharedPreferenceChangeListener(listener)
        p.setOnValueChangeListener(preview = { events += "preview:$it" }, commit = { events += "commit:$it" })
        assertTrue(p.submitComposeInput("+25")); assertEquals(425, p.getValue())
        assertEquals(425, AppHelper.appPrefs!!.getInt(p.key, -1)); assertEquals("+25.0 dp", p.composeDisplay())
        assertEquals(listOf("preview:425", "commit:425"), events); assertEquals(1, writes)
        assertTrue(p.submitComposeInput("+25")); assertEquals(1, writes)
        AppHelper.appPrefs!!.unregisterOnSharedPreferenceChangeListener(listener)
    }
    @Test fun cancelInvalidStepAndOverflowLeaveValueAndStorageUntouched() {
        val p = preference(max = 30000, shift = 0, default = 0, step = 500)
        for (input in listOf("125", "30001", "2147483648", "")) {
            assertFalse(input, p.submitComposeInput(input)); assertEquals(0, p.getValue())
            assertFalse(AppHelper.appPrefs!!.contains(p.key))
        }
        assertTrue(p.submitComposeInput("29500")); assertEquals(29500, p.getValue())
    }
    @Test fun dividedInputAndDefaultSentinelHaveOneStorageMeaning() {
        val p = preference(max = 40, shift = 20, divider = 2, default = 20, off = "Default")
        assertEquals("0", p.composeDefaultInput)
        assertTrue(p.submitComposeInput("1.5")); assertEquals(23, p.getValue())
        assertTrue(p.submitComposeInput(p.composeDefaultInput!!)); assertEquals(20, p.getValue())
        assertEquals("Default", p.composeDisplay())
    }
    @Test fun aNamedNonzeroDefaultIsNotInventedAsNumericZero() {
        val p = preference(min = 45, max = 200, shift = 0, divider = 100, default = 45, off = "Default", step = 5)
        assertFalse(p.submitComposeInput("0")); assertEquals(45, p.getValue())
        assertTrue(p.submitComposeInput("1.25")); assertEquals(125, p.getValue())
        assertTrue(p.submitComposeInput(p.composeDefaultInput!!)); assertEquals(45, p.getValue())
    }
    @Test fun dynamicRangeAndLinkedValuesUseTheNewValueBeforeSaving() {
        val min = preference(max = 99, shift = 0, default = 0, key = "pref_key_min")
        val max = preference(max = 100, shift = 0, default = 50, key = "pref_key_max")
        min.setOnValueChangeListener(preview = { value ->
            if (max.getValue() <= value) max.setValue(value + 1, true)
            max.setMinValue(value + 1)
        })
        assertTrue(min.submitComposeInput("80")); assertEquals(81, max.getValue())
        assertEquals(81, AppHelper.appPrefs!!.getInt(max.key, -1))
        assertFalse(max.submitComposeInput("50")); assertTrue(max.submitComposeInput("90")); assertEquals(90, max.getValue())
    }
    @Test fun detachRollsBackAnUncommittedDragWithoutSavingOrTouchingAnotherOwner() {
        val first = preference(); val second = preference(shift = 0, default = 7, key = "pref_key_second")
        first.previewComposeValue(600); first.onDetached()
        assertEquals(400, first.getValue()); assertEquals(7, second.getValue())
        assertFalse(AppHelper.appPrefs!!.contains(first.key)); assertFalse(AppHelper.appPrefs!!.contains(second.key))
    }
    @Test fun disabledHiddenAndUnsupportedPreferencesCannotCommitAnOpenEditor() {
        for (mode in 0..2) {
            val p = preference()
            when (mode) { 0 -> p.isEnabled = false; 1 -> p.isVisible = false; else -> p.setUnsupported(true) }
            assertFalse(p.submitComposeInput("30")); assertEquals(400, p.getValue())
            assertFalse(AppHelper.appPrefs!!.contains(p.key))
        }
    }
    @Test fun toastTopBottomAndDurationKeepRangesAndDistinctDefaultEncoding() {
        for ((name, initial, input) in listOf(Triple("top", 24, "799"), Triple("bottom", 0, "777"))) {
            val p = preference(max = 800, shift = 0, default = initial, key = "pref_key_$name")
            assertTrue(p.submitComposeInput(input)); assertEquals(input.toInt(), p.getValue())
            assertTrue(p.submitComposeInput(p.composeDefaultInput!!)); assertEquals(initial, p.getValue())
        }
        val p = preference(max = 19, shift = 4, default = 0, off = "Default", key = "pref_key_duration")
        assertTrue(p.submitComposeInput("15")); assertEquals(19, p.getValue())
        assertFalse(p.submitComposeInput("16")); assertTrue(p.submitComposeInput("0")); assertEquals(4, p.getValue())
        assertTrue(p.submitComposeInput(p.composeDefaultInput!!)); assertEquals(0, p.getValue())
    }
    @Test fun nonzeroMinAndFractionalScaleDeliverRawStorageValuesToCommit() {
        for ((minimum, divider, input, stored, progress) in listOf(listOf(1, 1, 4, 4, 3), listOf(0, 10, 15, 15, 15))) {
            val p = preference(min = minimum, max = 100, shift = 0, divider = divider, default = minimum)
            var committed = -1; p.setOnValueChangeListener(commit = { committed = it })
            assertTrue(p.submitComposeInput(if (divider == 10) "1.5" else input.toString()))
            assertEquals(stored, committed); assertEquals(progress, p.composeProgress)
            AppHelper.appPrefs!!.edit().clear().commit()
        }
    }
    @Test fun accessibilityAndDraggingShareTheSameSingleCommitPath() {
        val p = preference(); var commits = 0; p.setOnValueChangeListener(commit = { commits++ })
        p.previewComposeValue(450); assertTrue(p.finishComposeChange())
        assertEquals(450, p.getValue()); assertEquals(450, AppHelper.appPrefs!!.getInt(p.key, -1)); assertEquals(1, commits)
    }
    @Test fun pastedNegativeSignsDecimalsAndIllegalCharactersAreNotSilentlyFiltered() {
        val p = preference(max = 800, shift = 0, default = 24)
        for (input in listOf("-1", "1.5", "1e3", "NaN", "Infinity", "50 dp", "1,2,3")) {
            assertFalse(input, p.submitComposeInput(input)); assertEquals(24, p.getValue())
            assertFalse(AppHelper.appPrefs!!.contains(p.key))
        }
        assertTrue(p.submitComposeInput("800")); assertEquals(800, p.getValue())
    }
    @Test fun aVetoRollsBackThePreviewAndNeverCallsCommitOrPersists() {
        val p = preference(); val preview = mutableListOf<Int>(); var commits = 0
        p.setOnValueChangeListener(preview = { preview += it }, commit = { commits++ })
        p.setOnPreferenceChangeListener { _, _ -> false }
        assertFalse(p.submitComposeInput("30")); assertEquals(400, p.getValue())
        assertEquals(listOf(430, 400), preview); assertEquals(0, commits); assertFalse(AppHelper.appPrefs!!.contains(p.key))
    }
}
