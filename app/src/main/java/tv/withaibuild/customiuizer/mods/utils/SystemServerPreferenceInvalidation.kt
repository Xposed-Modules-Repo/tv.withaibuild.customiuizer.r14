package tv.withaibuild.customiuizer.mods.utils

import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.net.Uri
import android.os.Binder
import tv.withaibuild.customiuizer.PrefsProvider
import tv.withaibuild.customiuizer.utils.CommittedAutomationPreferences
import tv.withaibuild.customiuizer.utils.Helpers
import tv.withaibuild.customiuizer.mods.statusbarheight.StatusBarHeightResources

/**
 * Bridges preference change notifications from the module app to system_server.
 *
 * libxposed RemotePreferences in system_server does not deliver change callbacks, so a
 * preference the user changes after boot never reaches the process-local snapshot on its own.
 * (Confirmed device evidence 2026-08-19.) All other hooked processes keep their native
 * listener; this receiver exists only because system_server has none.
 *
 * Contract:
 * - Single-key: Intent carries the changed storage key only, never a value.
 * - Bulk: Intent carries no values, only a bulk marker.
 * - Only trusted sender (module app) is accepted.
 * - system_server reads its RemotePreferences; installed height resources can additionally
 *   read one confirmed scalar from the non-exported, system-only settings provider. This
 *   avoids frameworks that acknowledge changes but retain a stale hook-side cache.
 * - system_server never writes back to RemotePreferences (one-way, no feedback loop).
 * - Installed once per process lifetime; no polling and no timers.
 */
object SystemServerPreferenceInvalidation {

    const val ACTION_INVALIDATE = "tv.withaibuild.customiuizer.mods.action.InvalidatePreference"
    const val EXTRA_KEY = "k"
    const val EXTRA_BULK = "b"

    private const val RECEIVER_KEY = "systemServerPreferenceInvalidationReceiver"
    private const val MAX_KEY_LENGTH = 256

    private var installed = false

    @JvmStatic
    fun install(context: Context, bootstrap: PreferenceBootstrap) {
        if (installed) return
        installed = true

        val filter = IntentFilter(ACTION_INVALIDATE)
        ModuleHelper.registerModuleReceiver(
            context,
            RECEIVER_KEY,
            InvalidationReceiver(bootstrap),
            filter,
            Context.RECEIVER_EXPORTED,
        )
    }

    private class InvalidationReceiver(
        private val bootstrap: PreferenceBootstrap,
    ) : BroadcastReceiver() {
        override fun onReceive(context: Context, intent: Intent) {
            ModuleHelper.guarded {
                if (!ModuleHelper.isTrustedBroadcast(this, Helpers.modulePkg)) return@guarded

                if (intent.getBooleanExtra(EXTRA_BULK, false)) {
                    val automation = committedAutomation(context, null) ?: return@guarded
                    bootstrap.refreshRemoteKey(null, StatusBarHeightResources.committedPreference(context) + automation)
                    SystemServerInstaller.scheduleCatchUp(context)
                    return@guarded
                }

                val key = intent.getStringExtra(EXTRA_KEY)
                if (key == null || !isValidKey(key)) return@guarded
                val height = if (key == "pref_key_" + StatusBarHeightConfig.PREF_KEY)
                    StatusBarHeightResources.committedPreference(context) else emptyMap()
                val automation = committedAutomation(context, key) ?: return@guarded
                bootstrap.refreshRemoteKey(key, height + automation)
                SystemServerInstaller.scheduleCatchUp(context)
            }
        }
    }

    private fun committedAutomation(context: Context, key: String?): Map<String, Any>? {
        if (key != null && !CommittedAutomationPreferences.owns(key)) return emptyMap()
        val token = Binder.clearCallingIdentity()
        try {
            // An unavailable acknowledged mirror must not replay the framework's stale cache.
            return ModuleHelper.guarded<Map<String, Any>?>(null) {
                context.contentResolver.call(Uri.parse("content://" + PrefsProvider.AUTHORITY),
                    PrefsProvider.COMMITTED_AUTOMATION, key, null)?.let(CommittedAutomationPreferences::values)
            }
        } finally { Binder.restoreCallingIdentity(token) }
    }

    private fun isValidKey(key: String): Boolean {
        return key.isNotEmpty() && key.length <= MAX_KEY_LENGTH
    }
}
