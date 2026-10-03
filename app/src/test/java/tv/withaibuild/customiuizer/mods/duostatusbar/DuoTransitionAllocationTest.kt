package tv.withaibuild.customiuizer.mods.duostatusbar

import com.sun.management.ThreadMXBean
import java.lang.management.ManagementFactory
import org.junit.Assert.assertTrue
import org.junit.Assume.assumeTrue
import org.junit.Test

class DuoTransitionAllocationTest {
    @Test fun preparedCenterStateAndRapidTransitionsAllocateNoPerEventObjects() {
        val meter = ManagementFactory.getThreadMXBean() as ThreadMXBean
        assumeTrue(meter.isThreadAllocatedMemorySupported)
        meter.isThreadAllocatedMemoryEnabled = true
        val thread = Thread.currentThread().threadId()
        val config = DuoConfig(true, 24)
        val transition = DuoTransition()
        fun exercise(count: Int): Int {
            var checksum = 0
            for (i in 0 until count) {
                val network = DuoNetworkState.WIFI_CONNECTED or ((i % 5) shl 4)
                val audio = if (i % 2 == 0) DuoAudioState.BLUETOOTH or ((i % 10) shl 2) else 0
                transition.change(DuoCenter.key(network, audio, config), i.toLong(), 180L)
                checksum += (transition.fraction(i.toLong()) * 255f).toInt()
            }
            return checksum
        }
        repeat(5) { exercise(100000) }
        val before = meter.getThreadAllocatedBytes(thread)
        val checksum = exercise(1000000)
        val allocated = meter.getThreadAllocatedBytes(thread) - before
        println("Duo center/transition: 1000000 events, $allocated JVM allocated bytes, checksum=$checksum")
        assertTrue("Unexpected per-event allocation: $allocated bytes", allocated <= 4096L)
    }
}
