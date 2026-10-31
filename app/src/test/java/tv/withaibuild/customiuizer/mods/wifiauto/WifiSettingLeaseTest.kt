package tv.withaibuild.customiuizer.mods.wifiauto

import org.junit.Assert.*
import org.junit.Test

class WifiSettingLeaseTest {
    @Test fun aRejectedSceneHandoffDoesNotEraseConfirmedOwnership() {
        val lease = WifiSettingLease(); lease.apply(2, 1, "home") { it }
        lease.apply(4, 2, "office") { null }
        assertTrue(lease.owned); assertEquals(1, lease.original); assertEquals(2, lease.last)
        lease.apply(null, 2, null) { it }; assertFalse(lease.tracked)
    }
    @Test fun unsuccessfulRestoreReadbackKeepsOneAnchorUntilConfirmed() {
        val lease = WifiSettingLease()
        lease.apply(2, 1, "home") { it }
        lease.apply(null, 2, null) { 2 }
        lease.finishSession()
        assertTrue(lease.owned); assertEquals(1, lease.original); assertEquals(2, lease.last)
        lease.apply(null, 2, null) { it }; lease.finishSession()
        assertFalse(lease.tracked); assertFalse(lease.owned)
    }
    @Test fun unknownReadsNeverImpersonateManualInputOrAuthorizeWrites() {
        val lease = WifiSettingLease(); lease.apply(2, 1, "home") { it }
        lease.observe(null)
        assertFalse(lease.overridden)
        lease.apply(null, null, null) { error("unknown state must not write") }
        assertTrue(lease.owned)
    }
    @Test fun manualOverwriteCancelsPendingRecoveryAndCannotBeReclaimedInTheSameScene() {
        val lease = WifiSettingLease(); lease.apply(2, 1, "home") { it }
        lease.apply(null, 2, null) { null }
        lease.apply(4, 3, "office") { error("manual input wins") }
        assertTrue(lease.overridden); assertFalse(lease.tracked)
        lease.end(); assertFalse(lease.overridden)
    }
}
