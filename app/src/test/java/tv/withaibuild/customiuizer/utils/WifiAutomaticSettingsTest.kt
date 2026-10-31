package tv.withaibuild.customiuizer.utils

import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.utils.WifiAutomaticSettings as Settings

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class WifiAutomaticSettingsTest {
    @Test fun nativeZenConfigurationsWithAutomaticOrTimedRulesAreNeverTakenOver() {
        val loader = javaClass.classLoader!!
        val configType = Class.forName("android.service.notification.ZenModeConfig", true, loader)
        val ruleType = Class.forName("android.service.notification.ZenModeConfig\$ZenRule", true, loader)
        val config = configType.getDeclaredConstructor().newInstance()
        val gate = tv.withaibuild.customiuizer.mods.wifiauto.WifiDndPolicyGate(loader)
        assertTrue(gate.accepts(config))
        val rule = ruleType.getDeclaredConstructor().newInstance()
        @Suppress("UNCHECKED_CAST")
        val automatic = configType.getField("automaticRules").get(config) as MutableMap<String, Any>
        automatic["schedule"] = rule
        ruleType.getField("enabled").setBoolean(rule, true)
        assertFalse(gate.accepts(config))
        ruleType.getField("enabled").setBoolean(rule, false)
        assertTrue(gate.accepts(config))
        configType.getField("manualRule").set(config, rule)
        ruleType.getField("conditionId").set(rule, android.net.Uri.parse("condition://android/countdown/123"))
        assertFalse(gate.accepts(config))
        assertFalse(gate.accepts(null))
    }
    @Test fun unknownSsidAndRedactedBssidNeverBecomeAnExactApMatch() {
        assertNull(Settings.identity("<unknown ssid>", "00:11:22:33:44:55"))
        assertNull(Settings.identity("", null))
        val id = Settings.identity("\"home\"", "02:00:00:00:00:00")!!
        assertEquals("home", id.ssid); assertNull(id.bssid)
        val exact = Settings.Rule("ap", "AP", "home", "00:11:22:33:44:55", dnd = 2)
        assertNull(Settings.Options(true, listOf(exact)).match(id))
    }
    @Test fun priorityThenBssidThenStableOrderAndAnyApRoaming() {
        val any = Settings.Rule("home", "Home", "home", dnd = 2)
        val exact = any.copy(id = "ap", bssid = "00:11:22:33:44:55")
        val id = Settings.Identity("home", "00:11:22:33:44:55")
        assertEquals(exact, Settings.Options(true, listOf(any, exact)).match(id))
        assertEquals(any.copy(priority = 1), Settings.Options(true, listOf(any.copy(priority = 1), exact)).match(id))
        assertEquals(any, Settings.Options(true, listOf(any)).match(id.copy(bssid = "aa:bb:cc:dd:ee:ff")))
        assertNull(Settings.Options(false, listOf(any)).match(id))
        assertFalse(Settings.Options(true, listOf(any.copy(enabled = false))).active)
        assertFalse(Settings.Options(true, listOf(any.copy(dnd = -1))).active)
    }
    @Test fun roundTripAndTypedBackupPreserveAllIndependentChoices() {
        val rules = listOf(Settings.Rule("a", "Home", "家里的网络", dnd = 4, ringer = 1, rotation = 1, vpn = 0))
        val encoded = Settings.encode(rules)
        assertEquals(rules, Settings.decode(encoded))
        val (restored, counts) = BackupRestore.validateAndNormalizeEntries(mapOf("pref_key_system_wifiauto_rules" to encoded))
        assertEquals(encoded, restored["pref_key_system_wifiauto_rules"]); assertEquals(0, counts.unknownIgnored)
        assertTrue(Settings.decode("broken").isEmpty())
        assertTrue(Settings.decode("[{}]").isEmpty())
    }
}
