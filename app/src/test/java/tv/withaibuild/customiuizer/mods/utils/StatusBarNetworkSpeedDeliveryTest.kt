package tv.withaibuild.customiuizer.mods.utils

import java.lang.ref.WeakReference
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong
import org.junit.Assert.*
import org.junit.Test
import tv.withaibuild.customiuizer.mods.utils.StatusBarNetworkSpeedDispatcher.LatestDelivery
import tv.withaibuild.customiuizer.mods.utils.StatusBarNetworkSpeedDispatcher.NetworkSpeedPayload

class StatusBarNetworkSpeedDeliveryTest {
    private class Queue {
        val tasks = ArrayDeque<Runnable>()
        var posts = 0
        fun post(task: Runnable): Boolean { posts++; tasks.addLast(task); return true }
        fun runNext() = tasks.removeFirst().run()
    }

    private fun payload(value: Int) = NetworkSpeedPayload("$value", "KB/s", value != 0)

    @Test fun thirtyPendingUpdatesUseOneTaskAndOneCurrentDisplaySnapshot() {
        val queue = Queue()
        val registry = StatusBarDisplayRegistry<Any, Any>()
        val owners = listOf(Any(), Any())
        owners.forEachIndexed { display, owner -> registry.bind(owner, display) }
        var snapshots = 0
        val applied = mutableListOf<Pair<Any?, NetworkSpeedPayload>>()
        val lastApplied = AtomicLong()
        val delivery = LatestDelivery(queue::post) { p, seq ->
            snapshots++
            StatusBarNetworkSpeedDispatcher.dispatch(p, seq, lastApplied, registry) { state, value ->
                applied += state.generation?.get() to value
            }
        }
        repeat(30) { delivery.submit(payload(it), it.toLong() + 1) }
        assertEquals(1, queue.posts)
        assertEquals(1, queue.tasks.size)
        queue.runNext()
        assertEquals(1, snapshots)
        assertEquals(owners.map { it to payload(29) }, applied)
        assertEquals(30L, lastApplied.get())
        assertTrue(queue.tasks.isEmpty())
    }

    @Test fun steadyUpdatesKeepEveryDeliveryWithoutRetainingHistory() {
        val queue = Queue()
        val applied = mutableListOf<Long>()
        val delivery = LatestDelivery(queue::post) { _, seq -> applied += seq }
        repeat(100) {
            delivery.submit(payload(it), it.toLong() + 1)
            queue.runNext()
            assertTrue(queue.tasks.isEmpty())
        }
        assertEquals(100, queue.posts)
        assertEquals((1L..100L).toList(), applied)
    }

    @Test fun olderOrDuplicateSequenceCannotReplaceNewestEvenAfterDrain() {
        val queue = Queue()
        val applied = mutableListOf<Int>()
        val delivery = LatestDelivery(queue::post) { p, _ -> applied += p.number.toString().toInt() }
        delivery.submit(payload(2), 2)
        delivery.submit(payload(1), 1)
        delivery.submit(payload(99), 2)
        queue.runNext()
        delivery.submit(payload(1), 1)
        delivery.submit(payload(99), 2)
        assertEquals(listOf(2), applied)
        assertEquals(1, queue.posts)
        assertTrue(queue.tasks.isEmpty())
    }

    @Test fun updatesDuringDeliveryYieldOneFollowUpTurnWithOnlyTheLatestValue() {
        val queue = Queue()
        val applied = mutableListOf<Int>()
        lateinit var delivery: LatestDelivery
        delivery = LatestDelivery(queue::post) { p, _ ->
            applied += p.number.toString().toInt()
            if (applied.size == 1) {
                repeat(1000) { delivery.submit(payload(it + 2), it.toLong() + 2) }
                assertTrue("follow-up waits for this delivery to finish", queue.tasks.isEmpty())
            }
        }
        delivery.submit(payload(1), 1)
        queue.runNext()
        assertEquals(listOf(1), applied)
        assertEquals(1, queue.tasks.size)
        queue.runNext()
        assertEquals(listOf(1, 1001), applied)
        assertEquals(2, queue.posts)
        assertTrue(queue.tasks.isEmpty())
    }

    @Test fun backgroundProducerDoesNotWaitForMainThreadApplier() {
        val queue = Queue()
        val applied = mutableListOf<Int>()
        lateinit var delivery: LatestDelivery
        delivery = LatestDelivery(queue::post) { p, _ ->
            applied += p.number.toString().toInt()
            if (applied.size == 1) {
                val finished = CountDownLatch(1)
                val producer = Thread {
                    try { delivery.submit(payload(2), 2) } finally { finished.countDown() }
                }.apply { isDaemon = true; start() }
                assertTrue("no delivery work under the producer lock", finished.await(5, TimeUnit.SECONDS))
                producer.join(1000)
            }
        }
        delivery.submit(payload(1), 1)
        queue.runNext()
        assertEquals(1, queue.tasks.size)
        queue.runNext()
        assertEquals(listOf(1, 2), applied)
    }

    @Test fun rejectedPostReleasesPendingValueAndNewerEventCanTryAgain() {
        val queue = Queue()
        var reject = true
        val applied = mutableListOf<Long>()
        val delivery = LatestDelivery({ if (reject) false else queue.post(it) }) { _, seq -> applied += seq }
        delivery.submit(payload(1), 1)
        reject = false
        delivery.submit(payload(2), 2)
        queue.runNext()
        assertEquals(listOf(2L), applied)
        assertTrue(queue.tasks.isEmpty())
    }

    @Test fun throwingPostPreservesFailureAndAllowsTheNextEvent() {
        val queue = Queue()
        val failure = IllegalStateException("post failed")
        var fail = true
        val applied = mutableListOf<Long>()
        val delivery = LatestDelivery({ if (fail) throw failure else queue.post(it) }) { _, seq -> applied += seq }
        assertSame(failure, assertThrows(IllegalStateException::class.java) { delivery.submit(payload(1), 1) })
        fail = false
        delivery.submit(payload(2), 2)
        queue.runNext()
        assertEquals(listOf(2L), applied)
    }

    @Test fun rejectedFollowUpDoesNotKeepTheSlotActive() {
        val queue = Queue()
        var reject = false
        val applied = mutableListOf<Long>()
        lateinit var delivery: LatestDelivery
        delivery = LatestDelivery({ if (reject) false else queue.post(it) }) { _, seq ->
            applied += seq
            if (seq == 1L) { delivery.submit(payload(2), 2); reject = true }
        }
        delivery.submit(payload(1), 1)
        queue.runNext()
        assertTrue(queue.tasks.isEmpty())
        reject = false
        delivery.submit(payload(3), 3)
        queue.runNext()
        assertEquals(listOf(1L, 3L), applied)
    }

    @Test fun deliveryFailurePreservesIdentityAndReleasesSlotForNewEvents() {
        for (failure in listOf(IllegalStateException("apply"), OutOfMemoryError("fatal apply"))) {
            val queue = Queue()
            var fail = true
            val applied = mutableListOf<Long>()
            val delivery = LatestDelivery(queue::post) { _, seq -> if (fail) throw failure else applied.add(seq) }
            delivery.submit(payload(1), 1)
            assertSame(failure, assertThrows(Throwable::class.java) { queue.runNext() })
            fail = false
            delivery.submit(payload(2), 2)
            queue.runNext()
            assertEquals(listOf(2L), applied)
            assertTrue(queue.tasks.isEmpty())
        }
    }

    @Test fun generationReplacementAndDetachAreResolvedAtDeliveryTime() {
        val queue = Queue()
        val registry = StatusBarDisplayRegistry<Any, Any>()
        val oldOwner = Any(); val replacement = Any(); val detached = Any(); val current = Any()
        val oldRow = Any(); val newRow = Any()
        registry.bind(oldOwner, 0).secondRow = WeakReference(oldRow)
        registry.bind(detached, 1)
        val applied = mutableListOf<Pair<Any?, Any?>>()
        val delivery = LatestDelivery(queue::post) { p, seq ->
            StatusBarNetworkSpeedDispatcher.dispatch(p, seq, AtomicLong(), registry) { state, _ ->
                applied += state.generation?.get() to state.secondRow?.get()
            }
        }
        delivery.submit(payload(1), 1)
        delivery.submit(payload(2), 2)
        registry.bind(replacement, 0).secondRow = WeakReference(newRow)
        registry.detach(oldOwner) // a late old detach cannot remove its replacement
        registry.detach(detached)
        registry.bind(current, 2)
        queue.runNext()
        assertEquals(listOf(replacement to newRow, current to null), applied)
        assertTrue(queue.tasks.isEmpty())
    }
}
