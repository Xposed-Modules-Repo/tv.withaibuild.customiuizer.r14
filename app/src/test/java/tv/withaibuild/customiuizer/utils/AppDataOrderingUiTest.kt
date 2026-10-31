package tv.withaibuild.customiuizer.utils

import android.app.Application
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.runtime.MutableState
import org.robolectric.Robolectric
import org.robolectric.util.ReflectionHelpers
import tv.withaibuild.customiuizer.AppSelectionRow
import tv.withaibuild.customiuizer.filterAppSelectionRows
import tv.withaibuild.customiuizer.subs.AppSelector
import tv.withaibuild.customiuizer.R
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class AppDataOrderingUiTest {
    private val context get() = RuntimeEnvironment.getApplication()
    private fun apps() = ArrayList<AppData>().apply {
        repeat(80) { index -> add(AppData().apply {
            pkgName = "app.${index / 2}"
            actName = "Activity.${index / 2}"
            label = "Name ${index / 2}"
            user = if (index % 2 == 0) 0 else 999
        }) }
    }
    private val controller = Robolectric.buildActivity(AppCompatActivity::class.java)
    private val activity by lazy { controller.get().apply { setTheme(R.style.AppTheme) }; controller.setup().visible().get() }
    private fun selector(rows: ArrayList<AppData>, multi: Boolean, key: String, blackWhite: Boolean = false): AppSelector {
        AppHelper.installedAppsList = rows
        Helpers.launchableAppsList = rows
        val page = AppSelector().apply { arguments = Bundle().apply {
            putInt("settingsType", AppHelper.SettingsType.Edit.ordinal)
            putInt("abType", AppHelper.ActionBarType.HomeUp.ordinal)
            putInt("contentResId", R.id.prefs_app_selector)
            putString("key", key); putBoolean("multi", multi); putBoolean("standalone", !multi)
            putBoolean("bw", blackWhite); putBoolean("share", key == "pref_key_system_cleanshare_apps")
        } }
        if (key == "pref_key_system_cleanshare_apps") Helpers.shareAppsList = rows
        activity.supportFragmentManager.beginTransaction().replace(android.R.id.content, page).commitNow()
        ReflectionHelpers.callInstanceMethod<Unit>(page, "setupList")
        return page
    }
    private fun items(page: AppSelector, query: String = ""): List<AppData> {
        val rows = ReflectionHelpers.getField<MutableState<List<AppSelectionRow>>>(page, "appRows\$delegate").value
        return filterAppSelectionRows(rows, query.lowercase(java.util.Locale.ROOT), false).map { it.app }
    }
    @After fun clean() {
        controller.pause().stop().destroy()
        AppHelper.appPrefs = null; AppHelper.installedAppsList = null
        Helpers.launchableAppsList = null; Helpers.shareAppsList = null
    }

    @Test fun positiveNegativeAndUserSelectionsKeepStableGroupsAcrossFiltering() {
        val rows = apps()
        val chosen = setOf("app.3", "app.8|0", "app.8|999", "app.15|999")
        val excluded = setOf("app.6", "app.9")
        for ((key, blackWhite) in listOf("pref_key_selection" to false, "pref_key_selection" to true,
            "pref_key_system_cleanshare_apps" to false)) {
            AppHelper.appPrefs = context.getSharedPreferences("ordering", 0).also {
                it.edit().clear().putStringSet(key, chosen).putStringSet(key + "_black", excluded).commit()
            }
            val multiUser = key == "pref_key_system_cleanshare_apps"
            val normalized = chosen.map { if ('|' !in it && multiUser) "$it|0" else it }.toSet()
            val admitted = rows.filter { multiUser || it.user == 0 }
            val adapter = selector(rows, true, key, blackWhite)
            val active = AppHelper.getStringSetOfAppPrefs(key, emptySet())
            val activeBlack = AppHelper.getStringSetOfAppPrefs(key + "_black", emptySet())
            fun selected(app: AppData) = if (blackWhite) app.pkgName in active || app.pkgName in activeBlack
                else if (multiUser) "${app.pkgName}|${app.user}" in active
                else app.pkgName in active || "${app.pkgName}|0" in active
            for (query in listOf("", "Name 1", "missing", "")) {
                val matching = admitted.filter { it.label.contains(query, ignoreCase = true) }
                val expected = matching.filter(::selected) + matching.filterNot(::selected)
                assertEquals("$key / blackWhite=$blackWhite / query=$query",
                    expected.map { "${it.pkgName}|${it.user}" }, items(adapter, query).map { "${it.pkgName}|${it.user}" })
            }
        }
        assertEquals("Sorting must not mutate the cached source", "app.0", rows.first().pkgName)
    }

    @Test fun standaloneDefaultRemainsFirstAndChangingSelectionDoesNotReuseOldMembership() {
        val rows = apps()
        val prefs = context.getSharedPreferences("standalone-ordering", 0)
        AppHelper.appPrefs = prefs
        prefs.edit().clear().putString("pref_key_pick", "app.8|Activity.8").putInt("pref_key_pick_user", 999).commit()
        val adapter = selector(rows, false, "pref_key_pick")
        assertEquals("", items(adapter)[0].pkgName)
        assertSame(rows[17], items(adapter)[1])
        prefs.edit().putString("pref_key_pick", "app.15|Activity.15").putInt("pref_key_pick_user", 0).commit()
        ReflectionHelpers.callInstanceMethod<Unit>(adapter, "setupList")
        assertEquals("", items(adapter)[0].pkgName)
        assertSame(rows[30], items(adapter, "Name 1")[1])
        assertEquals(listOf(items(adapter)[0]) + rows.filter { it.label.startsWith("Name 1") }
            .sortedBy { if (it === rows[30]) 0 else 1 }, items(adapter, "Name 1"))
    }
}
