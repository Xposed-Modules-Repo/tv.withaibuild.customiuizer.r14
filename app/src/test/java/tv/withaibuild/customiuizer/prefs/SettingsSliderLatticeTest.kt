package tv.withaibuild.customiuizer.prefs

import java.io.File
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.testsupport.settingsNodes

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class SettingsSliderLatticeTest {
    @Test fun everyCurrentSliderRoundTripsIncludingItsOriginalDefaultAndSentinel() {
        var root = File(requireNotNull(System.getProperty("user.dir"))).absoluteFile
        while (!File(root, "app/settings").isDirectory) root = root.parentFile ?: error("repo not found")
        var count = 0
        for (file in File(root, "app/settings").listFiles()!!.filter { it.extension == "json" }) {
            for (node in settingsNodes(file.nameWithoutExtension).filter { it.getString("type") == "slider" }) {
                val attrs = node.getJSONObject("attributes")
                fun attr(name: String, fallback: Int) = attrs.optString("app:$name").toIntOrNull() ?: fallback
                val values = SeekBarValues(attr("minValue", 0), attr("maxValue", 10), attr("stepValue", 1),
                    attr("negativeShift", 0), attr("displayDividerValue", 1))
                val key = attrs.optString("android:key")
                val default = attrs.getString("android:defaultValue").toInt()
                assertTrue(key, values.isValid); assertTrue("$key default=$default", values.contains(default))
                for (progress in 0..values.progressMax) {
                    val raw = values.storage(progress)
                    assertEquals(key, raw, values.parse(values.number(raw)))
                    assertEquals(key, progress, values.progress(raw))
                }
                count++
            }
        }
        assertTrue("all slider pages covered", count >= 125)
    }
}
