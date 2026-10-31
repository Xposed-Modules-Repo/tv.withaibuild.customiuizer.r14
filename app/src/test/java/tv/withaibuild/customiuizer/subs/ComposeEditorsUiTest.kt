package tv.withaibuild.customiuizer.subs

import android.app.Activity
import android.app.Application
import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.os.Looper
import androidx.compose.ui.test.*
import androidx.compose.ui.test.junit4.createEmptyComposeRule
import org.junit.*
import org.junit.Assert.*
import org.junit.runner.RunWith
import org.robolectric.*
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import tv.withaibuild.customiuizer.*
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.utils.Helpers
import java.io.File
import java.time.Duration

/** Actual settings fragments: edits remain drafts across navigation and configuration changes. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, qualifiers = "w411dp-h891dp")
@LooperMode(LooperMode.Mode.PAUSED)
class ComposeEditorsUiTest {
    @get:Rule val compose = createEmptyComposeRule()
    private lateinit var host: ActivityController<MainActivity>
    private lateinit var activity: MainActivity
    private val prefs get() = requireNotNull(AppHelper.appPrefs)
    @Before fun launch() {
        val app = RuntimeEnvironment.getApplication()
        Helpers.withinAppContext = true; Helpers.appContentResolver = app.contentResolver
        AppHelper.appPrefs = app.getSharedPreferences(AppHelper.prefsName, 0).also { it.edit().clear().commit() }
        host = Robolectric.buildActivity(MainActivity::class.java).setup().visible()
        activity = host.get(); settle()
    }
    @After fun close() {
        host.pause().stop().destroy()
        AppHelper.appPrefs = null; Helpers.appContentResolver = null; Helpers.withinAppContext = false
    }
    private fun page() = activity.supportFragmentManager.findFragmentById(R.id.fragment_container) as PreferenceFragmentBase
    private fun settle() {
        activity.supportFragmentManager.executePendingTransactions()
        Shadows.shadowOf(Looper.getMainLooper()).idleFor(Duration.ofSeconds(1)); compose.waitForIdle()
    }
    private fun open(editor: SubFragment, key: String, id: Int) {
        page().openSubFragment(editor, Bundle().apply { putString("key", key); putInt("actions", MultiAction.Actions.LAUNCHER.ordinal) },
            AppHelper.SettingsType.Edit, AppHelper.ActionBarType.Edit, "Editor", id)
        settle()
    }
    private fun chooseAction(id: Int) {
        val values = activity.resources.getIntArray(R.array.global_actions_launcher_val)
        val labels = activity.resources.getStringArray(R.array.global_actions_launcher)
        compose.onNodeWithText(activity.getString(R.string.launcher_gestures_action)).performClick()
        compose.onAllNodesWithText(labels[values.indexOf(id)]).onLast().performScrollTo().performClick(); settle()
    }
    @Test fun actionAndTargetSurviveRecreationWithoutWritingUntilDone() {
        val key = "pref_key_editor_test"
        open(MultiAction(), key, R.id.prefs_multiaction); chooseAction(8)
        (page() as MultiAction).onActivityResult(0, Activity.RESULT_OK, Intent().putExtra("app", "fixture.target|fixture.Main").putExtra("user", 10))
        assertFalse(prefs.contains(key + "_action")); assertFalse(prefs.contains(key + "_app"))
        host.recreate(); activity = host.get(); settle()
        val label = activity.resources.getStringArray(R.array.global_actions_launcher)[activity.resources.getIntArray(R.array.global_actions_launcher_val).indexOf(8)]
        compose.onNodeWithText(label).assertIsDisplayed()
        (page() as MultiAction).confirmEdit(); settle()
        assertEquals(8, prefs.getInt(key + "_action", -1))
        assertEquals("fixture.target|fixture.Main", prefs.getString(key + "_app", null))
        assertEquals(10, prefs.getInt(key + "_app_user", -1))
    }
    @Test fun cancelDoesNotWriteAndRemovesOnlyOwnedPendingShortcut() {
        val key = "pref_key_cancel_test"
        open(MultiAction(), key, R.id.prefs_multiaction); chooseAction(9)
        val pending = File(activity.filesDir, "shortcuts/tmp.png").apply { parentFile!!.mkdirs(); writeBytes(byteArrayOf(1, 2, 3)) }
        (page() as MultiAction).onActivityResult(1, Activity.RESULT_OK, Intent().putExtra("shortcut_icon", pending.absolutePath).putExtra("shortcut_name", "Draft"))
        (page() as MultiAction).finish(); settle()
        assertFalse(prefs.contains(key + "_action")); assertFalse(pending.exists())
        open(MultiAction(), key, R.id.prefs_multiaction)
        val unrelated = File(activity.filesDir, "unrelated.png").apply { writeBytes(byteArrayOf(4)) }
        (page() as MultiAction).onActivityResult(1, Activity.RESULT_OK, Intent().putExtra("shortcut_icon", unrelated.absolutePath))
        (page() as MultiAction).finish(); settle()
        assertTrue(unrelated.exists()); unrelated.delete()
    }
    @Test fun failedIconMoveCannotCommitPreferencesOrCloseEditor() {
        val key = "pref_key_failure_test"
        open(MultiAction(), key, R.id.prefs_multiaction); chooseAction(9)
        val pending = File(activity.filesDir, "shortcuts/tmp.png").apply { parentFile!!.mkdirs(); writeBytes(byteArrayOf(1)) }
        val blocked = File(activity.filesDir, "shortcuts/${key}_shortcut.png").apply { mkdirs(); File(this, "occupied").writeText("x") }
        val editor = page() as MultiAction
        editor.onActivityResult(1, Activity.RESULT_OK, Intent().putExtra("shortcut_icon", pending.absolutePath))
        editor.confirmEdit(); settle()
        assertSame(editor, page()); assertFalse(prefs.contains(key + "_action")); assertTrue(pending.exists())
        blocked.deleteRecursively(); editor.finish(); settle(); assertFalse(pending.exists())
    }
    @Test fun colorRoundTripsAlphaAndRecreationWhileCancelKeepsSavedValue() {
        val key = "pref_key_color_test"
        prefs.edit().putInt(key, 0x7F123456).commit()
        open(ColorSelector(), key, R.id.fragment_selectcolor)
        compose.onNodeWithText("#7F123456").assertIsDisplayed()
        compose.onNodeWithText(activity.getString(R.string.black_color)).performClick()
        assertEquals(0x7F123456, prefs.getInt(key, 0))
        host.recreate(); activity = host.get(); settle()
        compose.onNodeWithText("#FF000000").assertIsDisplayed()
        (page() as ColorSelector).confirmEdit(); settle(); assertEquals(Color.BLACK, prefs.getInt(key, 0))
        open(ColorSelector(), key, R.id.fragment_selectcolor)
        compose.onNodeWithText(activity.getString(R.string.white_color)).performClick()
        (page() as ColorSelector).finish(); settle(); assertEquals(Color.BLACK, prefs.getInt(key, 0))
    }
}
