package tv.withaibuild.customiuizer.mods.wifiauto

import android.annotation.SuppressLint
import android.app.NotificationManager

/** Read-only system_server capability. A scalar filter lease cannot restore automatic-rule snoozing
 * or an expiring manual condition, so these user policies must stay outside our ownership. */
@SuppressLint("BlockedPrivateApi", "SoonBlockedPrivateApi")
internal class WifiDndPolicyGate(loader: ClassLoader) {
    private val config = Class.forName("android.service.notification.ZenModeConfig", false, loader)
    private val rule = Class.forName("android.service.notification.ZenModeConfig\$ZenRule", false, loader)
    private val getConfig = NotificationManager::class.java.getDeclaredMethod("getZenModeConfig")
    private val manual = config.getDeclaredField("manualRule")
    private val automatic = config.getDeclaredField("automaticRules")
    private val condition = rule.getDeclaredField("conditionId")
    private val enabled = rule.getDeclaredField("enabled")

    fun allows(manager: NotificationManager): Boolean = accepts(getConfig.invoke(manager))

    internal fun accepts(value: Any?): Boolean {
        if (value == null || !config.isInstance(value)) return false
        val rules = automatic.get(value) as? Map<*, *> ?: return false
        for (candidate in rules.values) if (candidate == null || !rule.isInstance(candidate) || enabled.getBoolean(candidate)) return false
        val current = manual.get(value) ?: return true
        return rule.isInstance(current) && condition.get(current) == null
    }
}
