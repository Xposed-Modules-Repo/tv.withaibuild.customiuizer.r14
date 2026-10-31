package tv.withaibuild.customiuizer.mods.wifiauto

import org.junit.Assert.*
import org.junit.Test
import tv.withaibuild.customiuizer.utils.WifiAutomaticSettings.Identity

class WifiNetworkTrackerTest {
    @Test fun vpnDefaultChangesNeverHideUnderlyingWifi() {
        val tracker = WifiNetworkTracker()
        assertFalse(tracker.state().known)
        tracker.initialReadComplete()
        tracker.available("wifi"); assertFalse(tracker.state().known)
        val home = Identity("home", "00:11:22:33:44:55")
        tracker.wifi("wifi", home); tracker.available("vpn"); tracker.vpn("vpn")
        assertEquals(home, tracker.state().identity)
        tracker.invalidateSnapshot(); assertFalse(tracker.state().known)
        tracker.initialReadComplete(); assertEquals(home, tracker.state().identity)
        tracker.lost("vpn"); assertEquals(home, tracker.state().identity)
        tracker.lost("wifi"); assertTrue(tracker.state().known); assertNull(tracker.state().identity)
    }
    @Test fun redactedOrAmbiguousNetworksAreNotGuessedAndOverflowIsBounded() {
        val tracker = WifiNetworkTracker()
        tracker.initialReadComplete()
        tracker.wifi("a", null); assertFalse(tracker.state().known)
        tracker.wifi("a", Identity("home", null)); tracker.wifi("b", Identity("office", null))
        assertFalse(tracker.state().known)
        repeat(1000) { tracker.available(it); tracker.wifi(it, Identity("other", null)) }
        assertFalse(tracker.state().known)
        val records = listOf("wifi", "pending", "vpn").sumOf { name ->
            val value = tracker.javaClass.getDeclaredField(name).apply { isAccessible = true }.get(tracker)
            if (value is Map<*, *>) value.size else (value as Collection<*>).size
        }
        assertTrue(records <= 16)
    }
    @Test fun overflowStaysUnknownUntilACompleteTrustedSnapshot() {
        val tracker = WifiNetworkTracker(); tracker.initialReadComplete()
        repeat(17) { tracker.wifi(it, Identity("home", null)) }
        assertFalse(tracker.snapshotReady); assertFalse(tracker.state().known)
        repeat(17) { tracker.lost(it) }
        assertFalse(tracker.state().known)
        tracker.beginSnapshot(); tracker.wifi("current", Identity("home", null))
        assertFalse(tracker.state().known)
        tracker.initialReadComplete()
        assertTrue(tracker.snapshotReady); assertEquals(Identity("home", null), tracker.state().identity)
    }
    @Test fun incompleteOrStillOverflowingSnapshotsRemainUnknown() {
        val tracker = WifiNetworkTracker(); tracker.beginSnapshot()
        tracker.wifi("redacted", null); tracker.initialReadComplete()
        assertFalse(tracker.state().known)
        tracker.beginSnapshot(); repeat(17) { tracker.available(it) }; tracker.initialReadComplete()
        assertFalse(tracker.snapshotReady); assertFalse(tracker.state().known)
        tracker.beginSnapshot(); tracker.initialReadComplete()
        assertTrue(tracker.state().known); assertNull(tracker.state().identity)
    }

}
