package tv.withaibuild.customiuizer

import android.app.Application
import android.net.Uri
import java.io.FileNotFoundException
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Assert.assertThrows
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PrefsProviderTest {

    private val provider = Robolectric.buildContentProvider(PrefsProvider::class.java).create().get()
    private fun uri(path: String) = Uri.parse("content://${PrefsProvider.AUTHORITY}/$path")

    @Test
    fun allOpenWithSamplesRemainReadable() {
        for (sample in 0..5) {
            provider.openAssetFile(uri("test/$sample"), "r")!!.use { descriptor ->
                descriptor.createInputStream().use { assertTrue(it.read() >= 0) }
            }
        }
    }

    @Test
    fun writeRequestsAndUnknownPathsAreRejected() {
        for (mode in listOf("w", "rw", "rwt", "wa", "")) {
            assertThrows(FileNotFoundException::class.java) { provider.openAssetFile(uri("test/0"), mode) }
        }
        for (path in listOf("", "test", "test/6", "test/0/extra", "test/../test0.png", "prefs/token")) {
            assertThrows(FileNotFoundException::class.java) { provider.openAssetFile(uri(path), "r") }
        }
        assertThrows(FileNotFoundException::class.java) {
            provider.openAssetFile(Uri.parse("content://other.provider/test/0"), "r")
        }
    }

    @Test
    fun sampleProviderRequiresAnExplicitUriGrant() {
        val context = RuntimeEnvironment.getApplication()
        val info = context.packageManager.resolveContentProvider(PrefsProvider.AUTHORITY, 0)!!
        assertFalse(info.exported)
        assertTrue(info.grantUriPermissions)
    }
}
