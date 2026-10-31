package tv.withaibuild.customiuizer.mods.island

import java.nio.file.Path
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.*
import org.junit.Test
import tv.withaibuild.customiuizer.utils.PrefMap

@org.junit.runner.RunWith(org.robolectric.RobolectricTestRunner::class)
@org.robolectric.annotation.Config(sdk = [34], application = android.app.Application::class)
class IslandDefaultContractTest {
    @Test fun layoutXmlAndRuntimeDefaultsDescribeTheSameAutomaticConfiguration() {
        val nodes = tv.withaibuild.customiuizer.testsupport.settingsNodes("prefs_system_island_layout")
        val prefs = PrefMap()
        for (node in nodes) {
            val attrs = node.optJSONObject("attributes") ?: continue
            if (!attrs.has("android:key") || !attrs.has("android:defaultValue")) continue
            val key = attrs.getString("android:key")
            val raw = attrs.getString("android:defaultValue")
            val value: Any = when {
                raw == "true" -> true
                raw == "false" -> false
                node.getString("type") == "slider" -> raw.toInt()
                else -> raw
            }
            prefs.put(key, value)
        }
        assertEquals(IslandOptions.read(PrefMap()), IslandOptions.read(prefs))
        prefs.put("system_island_height", 64)
        assertTrue(IslandOptions.read(prefs).automaticHeight)
        prefs.put("system_island_height_auto", false)
        assertEquals(64, IslandOptions.read(prefs).height)
        assertFalse(IslandOptions.read(prefs).automaticHeight)
        prefs.put("system_island_height", 40)
        assertFalse("Explicit manual 40 dp must not be inferred as automatic", IslandOptions.read(prefs).automaticHeight)
    }
}
