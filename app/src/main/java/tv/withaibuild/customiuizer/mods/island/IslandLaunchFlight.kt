package tv.withaibuild.customiuizer.mods.island

import android.os.SystemClock
import android.os.UserHandle

/** Controller-owned launch receipts, independent of cancellation of the action coroutine.
 * At most 16 destinations for 2.5 seconds; no polling or delayed cleanup task. */
internal class IslandLaunchFlight(private val now: () -> Long = SystemClock::uptimeMillis) {
    data class Target(val packageName: String, val user: UserHandle?, val identity: Any)
    class Request internal constructor(internal val target: Target) {
        internal var submittedAt: Long? = null
        internal var entered = false
    }
    private val requests = LinkedHashMap<Target, Request>()

    @Synchronized fun begin(target: Target): Request? {
        val time = now()
        requests.entries.removeAll { (_, request) -> request.submittedAt?.let { time - it >= 2500L } == true }
        if (requests.containsKey(target)) return null
        if (requests.size >= 16) requests.remove(requests.keys.first())
        return Request(target).also { requests[target] = it }
    }

    /** Called immediately after the external send returns, without a cancellation check. */
    @Synchronized fun submitted(request: Request) {
        if (requests[request.target] === request) request.submittedAt = now()
    }

    @Synchronized fun finished(request: Request) {
        if (request.submittedAt == null && requests[request.target] === request) requests.remove(request.target)
    }

    @Synchronized fun foreground(packageName: String?, user: UserHandle?) {
        requests.entries.removeAll { (target, request) ->
            val matches = target.packageName == packageName && target.user == user
            if (matches) request.entered = true
            request.entered && !matches
        }
    }

    @Synchronized fun clear() = requests.clear()
}
