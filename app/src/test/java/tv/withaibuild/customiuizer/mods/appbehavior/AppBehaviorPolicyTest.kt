package tv.withaibuild.customiuizer.mods.appbehavior

import android.content.pm.ActivityInfo.*
import android.view.WindowManager
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.utils.PrefMap

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppBehaviorPolicyTest {
    @Test fun audioConfigurationDoesNotInvalidateWindowPolicy() {
        val prefs = PrefMap()
        val before = AppBehaviorPolicy.read(prefs, includeVolume = false)
        prefs.put("system_appbehavior_volume", true)
        prefs.put("system_appbehavior_volume_apps", setOf("music"))
        assertEquals(before, AppBehaviorPolicy.read(prefs, includeVolume = false))
        assertEquals(setOf("music"), AppBehaviorPolicy.read(prefs).volume)
    }
    @Test fun disabledDoesNotChangeAnyNativeRequest() {
        val policy = AppBehaviorPolicy.read(PrefMap())
        for (orientation in -2..14) assertEquals(orientation, policy.orientation("app", orientation))
        for (value in listOf(-1f, 0f, .3f, 1f)) assertEquals(value, policy.brightness("app", "Camera", value))
        assertTrue(policy.volume.isEmpty())
    }

    @Test fun rotationPreservesExplicitLocksAndNativeIgnoreSentinels() {
        val policy = AppBehaviorPolicy(rotation = setOf("app"))
        for (orientation in listOf(SCREEN_ORIENTATION_LANDSCAPE, SCREEN_ORIENTATION_PORTRAIT,
            SCREEN_ORIENTATION_LOCKED, SCREEN_ORIENTATION_NOSENSOR, SCREEN_ORIENTATION_BEHIND, -2))
            assertEquals(orientation, policy.orientation("app", orientation))
        assertEquals(SCREEN_ORIENTATION_SENSOR, policy.orientation("app", SCREEN_ORIENTATION_UNSPECIFIED))
        assertEquals(SCREEN_ORIENTATION_FULL_SENSOR, policy.orientation("app", SCREEN_ORIENTATION_FULL_USER))
        assertEquals(SCREEN_ORIENTATION_SENSOR_LANDSCAPE, policy.orientation("app", SCREEN_ORIENTATION_USER_LANDSCAPE))
        assertEquals(SCREEN_ORIENTATION_USER, policy.orientation("other", SCREEN_ORIENTATION_USER))
    }

    @Test fun userPreferenceThenSceneExceptionThenApplicationLimit() {
        val policy = AppBehaviorPolicy(brightness = setOf("app", "follow"), levels = mapOf("app" to 40),
            blocked = setOf("app", "follow", "limited"), limit = 60, exceptions = setOf("app/Scan"))
        assertEquals(.4f, policy.brightness("app", "Main", 1f))
        assertEquals(-1f, policy.brightness("follow", "Main", 1f))
        assertEquals(1f, policy.brightness("app", "Scan", 1f))
        assertEquals(.6f, policy.brightness("limited", "Main", 1f))
        assertEquals(.2f, policy.brightness("limited", "Main", .2f))
        assertEquals(-1f, policy.brightness("limited", "Main", -1f))
    }

    @Test fun blockUsesSystemAndNeverCapsSystemAutomaticBrightness() {
        val policy = AppBehaviorPolicy(blocked = setOf("app"))
        assertEquals(-1f, policy.brightness("app", "Main", 1f))
        assertEquals(-1f, policy.brightness("app", "Main", -1f))
        assertTrue(policy.brightness("app", "Main", Float.NaN).isNaN())
        assertEquals(1f, policy.brightness("other", "Main", 1f))
    }

    @Test fun preferenceRemovalAndDisableRestoreOriginalRequests() {
        val prefs = PrefMap().apply {
            put("system_appbehavior_brightness", true)
            put("system_appbehavior_brightness_apps", setOf("app"))
            put("system_appbehavior_brightness_values", "app=45")
        }
        assertEquals(.45f, AppBehaviorPolicy.read(prefs).brightness("app", "Main", 1f))
        prefs.remove("system_appbehavior_brightness_values")
        assertEquals(-1f, AppBehaviorPolicy.read(prefs).brightness("app", "Main", 1f))
        prefs.put("system_appbehavior_brightness", false)
        assertEquals(1f, AppBehaviorPolicy.read(prefs).brightness("app", "Main", 1f))
    }

    @Test fun malformedLevelsCannotReachWindowAttributes() {
        assertEquals(mapOf("good" to 50), AppBehaviorPolicy.decodeLevels("=12;nan=NaN;zero=0;high=101;good=50;bad=-4"))
        assertEquals(256, AppBehaviorPolicy.decodeLevels((1..400).joinToString(";") { "app$it=50" }).size)
    }

    @Test fun nativeCallSeesPolicyAndFinallyAlwaysRestoresRawWindow() {
        val attrs = WindowManager.LayoutParams().apply { screenBrightness = 1f }
        var calls = 0
        val result = AppWindowBehaviorHooks.withBrightness(attrs, .5f) {
            calls++
            assertEquals(.5f, attrs.screenBrightness)
            27
        }
        assertEquals(27, result)
        assertEquals(1, calls)
        assertEquals(1f, attrs.screenBrightness)
        val error = OutOfMemoryError("native")
        try {
            AppWindowBehaviorHooks.withBrightness(attrs, -1f) { calls++; throw error }
            fail()
        } catch (actual: OutOfMemoryError) { assertSame(error, actual) }
        assertEquals(2, calls)
        assertEquals(1f, attrs.screenBrightness)
    }

    @Test fun repeatedActivityTransitionsAndRequestsDoNotAccumulateBrightnessState() {
        val attrs = WindowManager.LayoutParams()
        val policy = AppBehaviorPolicy(brightness = setOf("app"), levels = mapOf("app" to 30))
        repeat(1000) { index ->
            val pkg = if (index % 2 == 0) "app" else "other"
            val raw = if (index % 3 == 0) 1f else -1f
            attrs.screenBrightness = raw
            AppWindowBehaviorHooks.withBrightness(attrs, policy.brightness(pkg, "Main", raw)) {
                assertEquals(if (pkg == "app") .3f else raw, attrs.screenBrightness)
            }
            assertEquals(raw, attrs.screenBrightness)
        }
    }
}
