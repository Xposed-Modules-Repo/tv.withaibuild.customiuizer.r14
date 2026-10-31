package tv.withaibuild.customiuizer

import org.junit.Assert.*
import org.junit.Test
import tv.withaibuild.customiuizer.testsupport.repositorySource as source

class SubFragmentWithSearchLifecycleTest {
    @Test fun searchWorkAndIconWorkBelongToTheFragmentViewComposition() {
        val page = source("app/src/main/java/tv/withaibuild/customiuizer/SubFragmentWithSearch.kt")
        val chrome = source("app/src/main/java/tv/withaibuild/customiuizer/ui/SettingsChrome.kt")
        assertTrue(page.contains("settingsView {"))
        assertTrue(chrome.contains("DisposeOnViewTreeLifecycleDestroyed"))
        assertTrue(page.contains("produceState(source, source, filter)"))
        assertTrue(page.contains("withContext(Dispatchers.Default)"))
        assertTrue(page.contains("withContext(AppIconLoader.dispatcher)"))
        assertFalse(page.contains("ListView"))
        assertFalse(page.contains("Thread("))
    }
}
