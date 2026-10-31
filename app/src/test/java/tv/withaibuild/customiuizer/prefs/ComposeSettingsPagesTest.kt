package tv.withaibuild.customiuizer.prefs

import android.app.Application
import androidx.preference.Preference
import androidx.preference.PreferenceGroup
import androidx.preference.PreferenceManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import tv.withaibuild.customiuizer.R

/** Every generated page must bind dependencies against its own root exactly once per attachment. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class ComposeSettingsPagesTest {
    @Test fun allTypedPagesAttachDetachAndReattachWithoutMissingOrDuplicateDependencies() {
        val context = RuntimeEnvironment.getApplication()
        val pages = ComposeSettingsPages::class.java.declaredMethods.filter { it.name.startsWith("prefs_") }
        assertTrue("Canonical and lazy pages must both be covered", pages.size > 70)
        for (page in pages) {
            val manager = PreferenceManager(context)
            val id = R.id::class.java.getField(page.name).getInt(null)
            val screen = ComposeSettingsPages.create(id, context, manager)
            manager.setPreferences(screen)
            fun visit(group: PreferenceGroup): List<Preference> = buildList {
                for (index in 0 until group.preferenceCount) {
                    val child = group.getPreference(index); add(child)
                    if (child is PreferenceGroup) addAll(visit(child))
                }
            }
            val rows = visit(screen)
            repeat(2) {
                screen.onAttached()
                for (row in rows) {
                    val dependency = row.dependency ?: continue
                    val owner = requireNotNull(manager.findPreference<Preference>(dependency))
                    val dependents = ReflectionHelpers.getField<List<Preference>?>(owner, "mDependents").orEmpty()
                    assertEquals("${page.name}: ${row.key} must have one dependency binding", 1, dependents.count { it === row })
                }
                screen.onDetached()
                for (row in rows) {
                    val dependency = row.dependency ?: continue
                    val owner = requireNotNull(manager.findPreference<Preference>(dependency))
                    assertFalse(ReflectionHelpers.getField<List<Preference>?>(owner, "mDependents").orEmpty().any { it === row })
                }
            }
        }
    }
}
