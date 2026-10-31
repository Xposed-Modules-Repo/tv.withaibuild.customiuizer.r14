package tv.withaibuild.customiuizer.prefs

import android.app.Application
import android.os.Looper
import androidx.compose.material3.MaterialTheme
import androidx.compose.runtime.*
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createComposeRule
import androidx.preference.Preference
import androidx.preference.PreferenceManager
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.ui.*
import tv.withaibuild.customiuizer.utils.AppHelper

/** Keyed Compose rows must never inherit another row's summary or state. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PreferenceSummaryRecyclingTest {
    @get:Rule val compose = createComposeRule()
    private val context get() = RuntimeEnvironment.getApplication()
    private lateinit var model: SettingsPreferenceList
    private val previous = AppHelper.appPrefs
    @Before fun setup() { AppHelper.appPrefs = context.getSharedPreferences("compose-summary", 0).also { it.edit().clear().commit() } }
    @After fun close() { if (::model.isInitialized) model.close(); AppHelper.appPrefs = previous }
    private fun render(vararg rows: Preference) {
        val manager = PreferenceManager(context)
        manager.sharedPreferencesName = "compose-summary"
        val screen = manager.createPreferenceScreen(context)
        manager.setPreferences(screen)
        rows.forEach(screen::addPreference)
        screen.onAttached()
        model = SettingsPreferenceList(screen)
        compose.setContent { SettingsTheme {
            var dialog by remember { mutableStateOf<Preference?>(null) }
            SettingsPreferencePage(model, null, {}, { dialog = it })
            dialog?.let { SettingsPreferenceDialog(it) { dialog = null } }
        } }
    }
    private fun pref(key: String, title: String, summary: String? = null) = PreferenceEx(context, null).apply {
        this.key = key; this.title = title; this.summary = summary
    }
    private fun update(block: () -> Unit) { compose.runOnUiThread(block); shadowOf(Looper.getMainLooper()).idle(); compose.waitForIdle() }
    @Test fun clearingAndReplacingSummaryCannotLeaveOldTextInAReusedRow() {
        val p = pref("first", "Feedback", "Intro"); render(p)
        compose.onNodeWithText("Intro", useUnmergedTree = true).assertIsDisplayed()
        update { p.summary = "" }
        compose.onNodeWithText("Intro", useUnmergedTree = true).assertDoesNotExist()
        update { p.summary = "Another summary" }
        compose.onNodeWithText("Another summary", useUnmergedTree = true).assertIsDisplayed()
    }
    @Test fun aLiveSummaryProviderIsReadAfterItsOwnerInvalidatesTheRow() {
        var value = "First"
        val p = pref("provider", "Live").apply { summaryProvider = Preference.SummaryProvider<PreferenceEx> { value } }
        render(p); compose.onNodeWithText("First", useUnmergedTree = true).assertIsDisplayed()
        update { value = ""; p.setCustomSummary(null) }
        compose.onNodeWithText("First", useUnmergedTree = true).assertDoesNotExist()
        update { value = "Updated"; p.setCustomSummary(null) }
        compose.onNodeWithText("Updated", useUnmergedTree = true).assertIsDisplayed()
    }
    @Test fun customCountAndPlainSummariesKeepTheirOwnIdentity() {
        val first = pref("first", "First").apply { setCustomSummary("Custom") }
        val second = pref("pref_key_second", "Second").apply { extras.putBoolean("ui.countAsSummary", true) }
        AppHelper.appPrefs!!.edit().putStringSet("pref_key_second", setOf("a", "b")).putStringSet("pref_key_second_black", setOf("c")).commit()
        render(first, second)
        compose.onNodeWithText("Custom", useUnmergedTree = true).assertIsDisplayed()
        compose.onNodeWithText("3", useUnmergedTree = true).assertIsDisplayed()
        update { first.isVisible = false }
        compose.onNodeWithText("Custom", useUnmergedTree = true).assertDoesNotExist()
        compose.onNodeWithText("3", useUnmergedTree = true).assertIsDisplayed()
    }
    @Test fun switchHasImmediateToggleSemanticsAndOnePersistenceCommit() {
        val p = CheckBoxPreferenceEx(context, null).apply { key = "pref_key_toggle"; title = "Switch" }
        render(p)
        var writes = 0
        val listener = android.content.SharedPreferences.OnSharedPreferenceChangeListener { _, key -> if (key == p.key) writes++ }
        AppHelper.appPrefs!!.registerOnSharedPreferenceChangeListener(listener)
        compose.onNodeWithTag(p.key).assertIsOff().performClick().assertIsOn()
        assertTrue(p.isChecked); assertTrue(AppHelper.appPrefs!!.getBoolean(p.key, false)); assertEquals(1, writes)
        AppHelper.appPrefs!!.unregisterOnSharedPreferenceChangeListener(listener)
    }
    @Test fun numericDialogKeepsFocusValidatesRawInputAndCommitsThroughImeDone() {
        val p = SeekBarPreference(context, null).apply {
            key = "pref_key_offset"; title = "Offset"
            configureComposeValues(0, 800, 1, 400, 400, 1, false, "%d dp", null, null, true)
        }
        render(p)
        compose.onNodeWithTag(p.key).performClick()
        compose.onNodeWithTag("setting.input").assertIsFocused().performTextReplacement("bad")
        compose.onNodeWithText(context.getString(android.R.string.ok)).performClick()
        compose.onNodeWithText(context.getString(tv.withaibuild.customiuizer.R.string.seekbar_value_error)).assertIsDisplayed()
        assertFalse(AppHelper.appPrefs!!.contains(p.key))
        compose.onNodeWithTag("setting.input").performTextReplacement("+25")
        compose.onNodeWithTag("setting.input").performImeAction()
        compose.onNodeWithTag("setting.input").assertDoesNotExist()
        assertEquals(425, AppHelper.appPrefs!!.getInt(p.key, -1))
        compose.onNodeWithTag(p.key).performClick()
        compose.onNodeWithTag("setting.input").performTextReplacement("+50")
        compose.onNodeWithText(context.getString(android.R.string.cancel)).performClick()
        assertEquals(425, AppHelper.appPrefs!!.getInt(p.key, -1))
    }
    @Test fun unsupportedAndDynamicStatusAreSemanticAndDoNotOverwriteTheTitle() {
        val p = pref("status", "Feature").apply { extras.putBoolean("ui.dynamic", true) }
        render(p)
        compose.onNodeWithTag("status").assertIsEnabled()
        update { p.setUnsupported(true) }
        compose.onNodeWithTag("status").assertIsNotEnabled()
        assertEquals("Feature", p.title)
    }
}
