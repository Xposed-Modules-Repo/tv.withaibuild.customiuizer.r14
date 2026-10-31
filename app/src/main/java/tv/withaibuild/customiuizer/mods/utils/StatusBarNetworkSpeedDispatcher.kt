package tv.withaibuild.customiuizer.mods.utils

import java.util.concurrent.atomic.AtomicLong

/**
 * Main-thread dispatcher for the network speed second-row update.
 *
 * The hook that receives network speed payloads may run on a background handler. This dispatcher
 * takes an immutable payload and a sequence number, drops the update if a newer payload has already
 * been applied, and applies the payload to a snapshot of the registry on the caller's thread. The
 * caller must post the [dispatch] call to the SystemUI main looper; this object does not touch
 * Android handlers and is fully testable on the JVM.
 */
object StatusBarNetworkSpeedDispatcher {

    /**
     * Immutable network speed payload. The values are whatever the SystemUI network speed state
     * object exposes; they are not interpreted on the background thread.
     */
    data class NetworkSpeedPayload(
        val number: Any?,
        val unit: Any?,
        val visible: Any?,
    )

    /**
     * Apply [payload] to the current registry snapshot.
     *
     * [seq] is a monotonically increasing sequence number generated on the background thread.
     * [lastApplied] is the last sequence number that reached the main thread. If [seq] is older
     * than [lastApplied], the payload is dropped. Otherwise [lastApplied] is updated and the
     * [applier] is called for every state in the current registry snapshot.
     */
    fun <O : Any, R : Any> dispatch(
        payload: NetworkSpeedPayload,
        seq: Long,
        lastApplied: AtomicLong,
        registry: StatusBarDisplayRegistry<O, R>,
        applier: (StatusBarDisplayState<O, R>, NetworkSpeedPayload) -> Unit,
    ) {
        if (seq < lastApplied.get()) return
        lastApplied.set(seq)
        for (state in registry.allStatesSnapshot()) {
            applier(state, payload)
        }
    }

    /**
     * Process-owned latest-value slot for the second-row mirror, not native sampling/events.
     * [post] must enqueue on the existing main looper, never execute inline. One reusable task
     * and one pending payload bound a burst; updates during delivery yield to a later main turn.
     * No View/owner/snapshot is retained. Delivery and failed posts release their pending data.
     */
    internal class LatestDelivery(
        private val post: (Runnable) -> Boolean,
        private val deliver: (NetworkSpeedPayload, Long) -> Unit,
    ) : Runnable {
        private val lock = Any()
        private var pending: NetworkSpeedPayload? = null
        private var pendingSequence = 0L
        private var lastSubmittedSequence = Long.MIN_VALUE
        private var scheduled = false

        fun submit(payload: NetworkSpeedPayload, sequence: Long) {
            synchronized(lock) {
                // A producer may reach this lock after a newer publication already did.
                if (sequence <= lastSubmittedSequence) return
                lastSubmittedSequence = sequence
                pending = payload
                pendingSequence = sequence
                if (!scheduled) {
                    scheduled = true
                    enqueueLocked()
                }
            }
        }

        override fun run() {
            val payload: NetworkSpeedPayload
            val sequence: Long
            synchronized(lock) {
                payload = pending ?: return
                sequence = pendingSequence
                pending = null
            }
            // Reflection/UI work must never hold the producer lock. The caller guards ordinary
            // UI failures; an escaping failure releases everything and preserves its identity.
            try {
                deliver(payload, sequence)
            } catch (t: Throwable) {
                synchronized(lock) { releaseLocked() }
                throw t
            }
            synchronized(lock) {
                if (pending == null) scheduled = false else enqueueLocked()
            }
        }

        private fun enqueueLocked() {
            try {
                if (!post(this)) releaseLocked()
            } catch (t: Throwable) {
                releaseLocked()
                throw t
            }
        }

        private fun releaseLocked() {
            pending = null
            scheduled = false
        }
    }
}
