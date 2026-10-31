package tv.withaibuild.customiuizer.mods.utils

import tv.withaibuild.customiuizer.testsupport.repositorySource as source

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class SettingsNotificationControlsContractTest {
    @Test
    fun settingsFeaturePreservesAndroid14ChannelAndAdministratorDecisions() {
        val hooks = source("app/src/main/java/tv/withaibuild/customiuizer/mods/SystemNotificationHooks.kt")
        val feature = source("app/src/main/java/tv/withaibuild/customiuizer/mods/utils/feature/SettingsFeatures.kt")
        assertFalse(hooks.contains("\"isAppBlockable\""))
        assertFalse(hooks.contains("\"isChannelBlockable\""))
        assertFalse(hooks.contains("\"isChannelConfigurable\""))
        assertFalse(hooks.contains("\"isChannelGroupBlockable\""))
        assertFalse(hooks.contains("\"setupBlock\""))
        assertFalse(hooks.contains("mHasNotifPermission"))
        assertFalse(hooks.contains("XposedHelpers.callMethod(block, \"setEnabled\", true)"))
        assertFalse(hooks.contains("as? Preference"))
        assertFalse(hooks.contains("result = HashSet<String>()"))
        assertFalse(hooks.contains("\"isBlockable\""))
        assertFalse(hooks.contains("\"setBlockable\""))
        assertFalse(hooks.contains("setStaticBooleanField(NotifyManagerCls"))
        assertFalse(feature.contains("UnlockSettingsNotificationControlsHook(lpparam)"))
        assertTrue(feature.contains("DisableAnyNotificationHook(lpparam)"))
        assertTrue(hooks.contains("ordinaryNotificationTarget(context, chain.getArg(1) as? String)"))
        val server = source("app/src/main/java/tv/withaibuild/customiuizer/mods/utils/feature/SystemServerFeatures.kt")
        assertFalse(server.contains("DisableAnyNotificationBlockFeatureId"))
    }

}
