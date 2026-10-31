package tv.withaibuild.customiuizer.mods

import android.app.Application
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.utils.PrefMap

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class WeatherFormatDemandTest {
    private fun snapshot(vararg values: Pair<String, Any>) = SystemClockHooks.buildClockStyleSnapshot(
        PrefMap().apply {
            put("system_statusbar_enable_weather_param", true)
            for ((key, value) in values) put(key, value)
        }, RuntimeEnvironment.getApplication().resources)

    private fun needs(snapshot: SystemClockHooks.ClockStyleSnapshot, statusbar: Boolean = true,
        cc: Boolean = true, hideDate: Boolean = false, hideDrawer: Boolean = false) =
        SystemClockHooks.needsWeatherData(snapshot, statusbar, cc, hideDate, hideDrawer)

    @Test fun inactiveOrHiddenFormatsCannotRequestWeather() {
        assertFalse(needs(snapshot()))
        val statusbar = snapshot("system_statusbar_clock_customformat" to "HH:mm tq")
        assertFalse(needs(statusbar)) // Disabled custom format uses the default instead.
        assertFalse(needs(statusbar.copy(statusbarCustomFormatEnable = true), statusbar = false))
        assertFalse(needs(snapshot("system_cc_clock_customformat" to "tq"), cc = false))
        assertFalse(needs(snapshot("system_cc_dateformat" to "tq"), hideDate = true))
        assertFalse(needs(snapshot("system_drawer_dateformat" to "tq"), hideDrawer = true))
    }

    @Test fun everyEffectiveWeatherFormatCreatesDemandAndGlobalDisableRemovesIt() {
        val formats = listOf(
            snapshot("system_statusbar_clock_customformat" to "HH:mm tq",
                "system_statusbar_clock_customformat_enable" to true),
            snapshot("system_cc_clock_customformat" to "tq"),
            snapshot("system_cc_dateformat" to "EEE tq"),
            snapshot("system_drawer_dateformat" to "tq"),
        )
        for (format in formats) {
            assertTrue(needs(format))
            assertFalse(needs(format.copy(enableWeatherParam = false)))
        }
        // CC rendering uses its nonempty format irrespective of the custom-format toggle.
        assertTrue(needs(formats[1].copy(ccCustomFormatEnable = false)))
    }

    @Test fun eligibilityMatchesActualFormattingAcrossAllRoutes() {
        for (statusbar in listOf(false, true)) for (cc in listOf(false, true))
            for (hideDate in listOf(false, true)) for (hideDrawer in listOf(false, true)) {
                val format = snapshot("system_statusbar_clock_customformat" to "tq",
                    "system_statusbar_clock_customformat_enable" to true,
                    "system_cc_clock_customformat" to "tq", "system_cc_dateformat" to "tq",
                    "system_drawer_dateformat" to "tq")
                val names = listOf("clock", "ccClock") +
                    (if (hideDate) emptyList() else listOf("ccDate")) +
                    (if (hideDrawer) emptyList() else listOf("drawerDate"))
                val rendersWeather = names.any { name ->
                    SystemClockHooks.buildClockText(name, format, "weather", statusbar, cc)?.contains("weather") == true
                }
                assertEquals(rendersWeather, needs(format, statusbar, cc, hideDate, hideDrawer))
            }
    }
}
