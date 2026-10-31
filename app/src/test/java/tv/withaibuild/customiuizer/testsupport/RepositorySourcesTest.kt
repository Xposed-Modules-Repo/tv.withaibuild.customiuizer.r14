package tv.withaibuild.customiuizer.testsupport

import org.junit.Assert.assertThrows
import org.junit.Assert.assertTrue
import org.junit.Test

class RepositorySourcesTest {
    @Test
    fun productionSourceIsRequired() {
        assertTrue(repositorySource("app/src/main/AndroidManifest.xml").contains("<application"))
        val error = assertThrows(IllegalStateException::class.java) {
            repositorySource("app/src/main/does-not-exist.kt")
        }
        assertTrue(error.message.orEmpty().contains("does-not-exist.kt"))
    }
}
