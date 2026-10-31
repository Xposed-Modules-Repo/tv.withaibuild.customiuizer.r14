package tv.withaibuild.customiuizer

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

/** Static contracts for the low-allocation preference-page transition path. */
class PreferenceNavigationContractTest {

    private val source = Files.readString(
        Path.of("src/main/java/tv/withaibuild/customiuizer/PreferenceFragmentBase.kt")
    )

    @Test
    fun composeOwnsPressFeedbackWithoutTraversingNativePreferenceChildren() {
        assertFalse(source.contains("finishNavigationFeedback"))
        val renderer = Files.readString(Path.of("src/main/java/tv/withaibuild/customiuizer/ui/SettingsPreferences.kt"))
        assertTrue(renderer.contains("modifier.clickable"))
        assertTrue(renderer.contains("modifier.toggleable"))
        assertFalse(renderer.contains("jumpDrawablesToCurrentState"))
    }

    @Test
    fun userNavigationIsNotCommittedAfterFragmentStateWasSaved() {
        val stringOverload = source.substring(
            source.indexOf("open fun openSubFragment(\n        fragment: Fragment", source.indexOf("open fun openSubFragment(") + 1),
            source.indexOf("override fun onCreateAnimator")
        )

        assertTrue(stringOverload.contains("isStateSaved"))
        assertTrue(stringOverload.contains(".commit()"))
        assertFalse(stringOverload.contains("commitAllowingStateLoss"))
        assertFalse(stringOverload.contains("executePendingTransactions"))
    }
}

class PreferenceSearchNavigationContractTest {

    @Test
    fun searchAndCategoryRoutesCreateFreshFragmentInstances() {
        val main = Files.readString(
            Path.of("src/main/java/tv/withaibuild/customiuizer/MainFragment.kt")
        )
        val category = Files.readString(
            Path.of("src/main/java/tv/withaibuild/customiuizer/subs/CategorySelector.kt")
        )

        assertFalse(main.contains("var prefSystem"))
        assertFalse(main.contains("val catSelector"))
        assertTrue(main.contains("openSubFragment(SubSystem(), bundle"))
        assertTrue(main.contains("openSubFragment(CategorySelector(), bundle"))
        assertTrue(main.contains("openSubFragment(Launcher(), bundle"))
        assertTrue(main.contains("openSubFragment(Controls(), bundle"))
        assertTrue(main.contains("openSubFragment(Various(), bundle"))

        assertFalse(category.contains("mainFrag.prefSystem"))
        assertTrue(category.contains("openSubFragment(System(), bundle"))
        assertTrue(category.contains("openSubFragment(Launcher(), bundle"))
        assertTrue(category.contains("openSubFragment(Controls(), bundle"))
        assertTrue(category.contains("openSubFragment(Various(), bundle"))
    }
}
