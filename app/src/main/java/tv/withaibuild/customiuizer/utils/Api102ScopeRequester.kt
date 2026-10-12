package tv.withaibuild.customiuizer.utils

import android.os.Handler
import android.os.Looper
import io.github.libxposed.service.XposedService

/**
 * Scope requests enabled by the project's API 102 compatibility policy.
 * The service interfaces already exist in API 101; keep the current version guard until
 * their behavior is verified on an API 101 framework.
 */
internal object Api102ScopeRequester {
    fun request(
        service: XposedService,
        packages: List<String>,
        callback: (Boolean, String?) -> Unit,
    ) {
        val missing = packages.filterNot { it in service.scope }
        if (missing.isEmpty()) {
            callback(true, null)
            return
        }
        val mainHandler = Handler(Looper.getMainLooper())
        service.requestScope(
            missing,
            object : XposedService.OnScopeEventListener {
                override fun onScopeRequestApproved(approved: List<String>) {
                    mainHandler.post { callback(approved.containsAll(missing), null) }
                }

                override fun onScopeRequestFailed(message: String) {
                    mainHandler.post { callback(false, message) }
                }
            },
        )
    }
}
