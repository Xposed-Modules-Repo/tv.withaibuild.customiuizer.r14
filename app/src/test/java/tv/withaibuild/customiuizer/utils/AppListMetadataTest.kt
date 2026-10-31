package tv.withaibuild.customiuizer.utils

import java.nio.file.Files
import java.nio.file.Path
import java.util.Locale
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AppListMetadataTest {
    @Test
    fun packageActivityAndInstalledAppKeysPreserveExistingCacheIdentity() {
        val app = AppData().apply { pkgName = "example.app" }
        for ((activity, expected) in listOf("" to "example.app", "-" to "example.app|-",
            "example.app.Main" to "example.app|example.app.Main")) {
            app.actName = activity
            app.prepareForList()
            assertEquals(expected, app.iconKey)
        }
        app.user = 999
        app.prepareForList()
        assertEquals("example.app|example.app.Main", app.iconKey)
    }

    @Test
    fun preparationRefreshesDerivedFieldsAndUsesRootLocale() {
        val previous = Locale.getDefault()
        try {
            Locale.setDefault(Locale.forLanguageTag("tr-TR"))
            val app = AppData().apply { label = "IMEI"; actName = "MainActivity" }
            app.prepareForList()
            assertEquals("imei", app.labelLower)
            assertEquals("mainactivity", app.actNameLower)
            app.label = "蓝牙"
            app.actName = ""
            app.prepareForList()
            assertEquals("蓝牙", app.labelLower)
            assertEquals("", app.actNameLower)
            assertEquals("", app.iconKey)
        } finally {
            Locale.setDefault(previous)
        }
    }

    @Test fun composeListsLoadIconsOffMainAndReusePreparedKeys() {
        val source = Files.readString(Path.of("src/main/java/tv/withaibuild/customiuizer/SubFragmentWithSearch.kt"))
        assertTrue(source.contains("Helpers.memoryCache[app.iconKey]"))
        assertTrue(source.contains("withContext(AppIconLoader.dispatcher)"))
        assertFalse(source.contains("loadLabel("))
        assertTrue(source.contains("app.labelLower.contains(filter)"))
        val icon = Files.readString(Path.of("src/main/java/tv/withaibuild/customiuizer/utils/AppIconLoader.kt"))
        assertTrue(icon.contains("Dispatchers.IO.limitedParallelism(4)"))
        assertTrue(icon.contains("coerceIn(1, 256)"))
        assertTrue(icon.contains("Helpers.memoryCache.put(iconKey, bitmap)"))
    }
}
