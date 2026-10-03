package tv.withaibuild.customiuizer.mods.duostatusbar

import org.junit.Assert.*
import org.junit.Test
import java.util.concurrent.CountDownLatch

class DuoNetworkStateTest {
    @Test fun unknownStateDoesNotReplaceAnyNativeIcon() {
        val state = DuoNetworkState()
        assertEquals(0, state.snapshot)
        state.subscriptions(10, 20, true)
        assertEquals(0, state.snapshot and 3)
        state.wifi(true, true, 4)
        assertEquals(DuoNetworkState.WIFI_READY, state.snapshot and 3)
    }

    @Test fun connectedWifiIncludesLowestLevelAndDisconnectClearsStrength() {
        val state = DuoNetworkState()
        state.wifi(true, true, 0)
        assertTrue(state.snapshot and DuoNetworkState.WIFI_CONNECTED != 0)
        state.wifi(true, true, 4)
        assertEquals(4, DuoNetworkState.wifiLevel(state.snapshot))
        state.wifi(false, true, 4)
        assertEquals(0, DuoNetworkState.wifiLevel(state.snapshot))
        assertEquals(0, state.snapshot and DuoNetworkState.WIFI_CONNECTED)
    }

    @Test fun serviceFallbackDistinguishesZeroBarsFromNoServiceAndAirplaneMode() {
        val state = DuoNetworkState()
        state.subscriptions(10, -1, true)
        state.mobile(0, 10, 0, true, true)
        assertTrue(state.snapshot and DuoNetworkState.MOBILE_SERVICE != 0)
        assertEquals(DuoAudioState.CELLULAR_GLYPH, DuoAudioState.glyph(state.snapshot, 0))
        state.mobile(0, 10, 0, false, true)
        assertEquals(DuoAudioState.OFFLINE_GLYPH, DuoAudioState.glyph(state.snapshot, 0))
        state.airplane(true)
        assertEquals(DuoAudioState.PLANE_GLYPH, DuoAudioState.glyph(state.snapshot, 0))
        state.wifi(true, true, 2)
        assertEquals(DuoAudioState.WIFI_GLYPH, DuoAudioState.glyph(state.snapshot, 0))
    }

    @Test fun duplicateWifiAndMobileEventsDoNotScheduleRefresh() {
        val state = DuoNetworkState()
        state.subscriptions(10, -1, true)
        assertTrue(state.wifi(true, true, 3))
        repeat(10000) { assertFalse(state.wifi(true, true, 3)) }
        assertTrue(state.mobile(0, 10, 3, true, true))
        repeat(10000) { assertFalse(state.mobile(0, 10, 3, true, true)) }
    }

    @Test fun connectedWifiWithoutInternetKeepsItsWarningUntilValidationOrDisconnect() {
        val state = DuoNetworkState()
        state.wifi(true, true, 4, false)
        assertTrue(state.snapshot and DuoNetworkState.WIFI_UNVALIDATED != 0)
        assertFalse(state.wifi(true, true, 4, false))
        assertTrue(state.wifi(true, true, 4, true))
        assertEquals(0, state.snapshot and DuoNetworkState.WIFI_UNVALIDATED)
        state.wifi(true, true, 4, false)
        state.wifi(false, false, 0, false)
        assertEquals(0, state.snapshot and DuoNetworkState.WIFI_UNVALIDATED)
    }

    @Test fun dualSimUsesDefaultDataSimEvenIfSecondarySignalIsStronger() {
        val state = DuoNetworkState()
        state.subscriptions(10, 20, true)
        state.mobile(1, 20, 4, true, false)
        assertEquals(0, state.snapshot and DuoNetworkState.MOBILE_READY)
        state.mobile(0, 10, 1, true, true)
        assertEquals(1, DuoNetworkState.cellLevel(state.snapshot))
        assertFalse(state.mobile(1, 20, 3, true, false))
    }

    @Test fun switchingDataSimTakesEffectBeforeOldControllerRefreshes() {
        val state = DuoNetworkState()
        state.subscriptions(10, 20, true)
        state.mobile(0, 10, 1, true, true)
        state.mobile(1, 20, 4, true, true)
        assertEquals(4, DuoNetworkState.cellLevel(state.snapshot))
        assertFalse(state.mobile(0, 10, 3, true, false))
    }

    @Test fun losingServiceOnDefaultSimDoesNotBorrowOtherSimSignal() {
        val state = DuoNetworkState()
        state.subscriptions(10, 20, true)
        state.mobile(1, 20, 4, true, false)
        state.mobile(0, 10, 4, false, true)
        assertEquals(0, DuoNetworkState.cellLevel(state.snapshot))
        assertEquals(DuoNetworkState.MOBILE_READY, state.snapshot and DuoNetworkState.MOBILE_READY)
    }

    @Test fun retiredSubscriptionCannotResurrectAfterRemovalOrSlotReplacement() {
        val state = DuoNetworkState()
        state.subscriptions(10, -1, true)
        state.mobile(0, 10, 4, true, true)
        state.subscriptions(30, -1, true)
        assertFalse(state.mobile(0, 10, 4, true, true))
        assertEquals(0, state.snapshot and DuoNetworkState.MOBILE_READY)
        state.mobile(0, 30, 2, true, true)
        assertEquals(2, DuoNetworkState.cellLevel(state.snapshot))
        state.subscriptions(-1, -1, true)
        assertFalse(state.mobile(0, 30, 2, true, true))
        assertEquals(0, DuoNetworkState.cellLevel(state.snapshot))
        assertEquals(DuoNetworkState.MOBILE_READY, state.snapshot and DuoNetworkState.MOBILE_READY)
    }

    @Test fun airplaneStopsCellularAndAllowsWifiThenRestoresCellular() {
        val state = DuoNetworkState()
        state.subscriptions(10, -1, true)
        state.mobile(0, 10, 3, true, true)
        state.wifi(true, true, 4)
        state.airplane(true)
        assertEquals(0, DuoNetworkState.cellLevel(state.snapshot))
        assertEquals(4, DuoNetworkState.wifiLevel(state.snapshot))
        assertTrue(state.snapshot and DuoNetworkState.WIFI_CONNECTED != 0)
        state.airplane(false)
        assertEquals(3, DuoNetworkState.cellLevel(state.snapshot))
    }

    @Test fun unsupportedSubscriptionsKeepNativeMobileIcons() {
        val state = DuoNetworkState()
        state.subscriptions(10, 20, false)
        assertFalse(state.mobile(0, 10, 3, true, true))
        assertEquals(0, state.snapshot and DuoNetworkState.MOBILE_READY)
        state.subscriptions(10, -1, true)
        state.mobile(0, 10, 3, true, true)
        assertEquals(3, DuoNetworkState.cellLevel(state.snapshot))
    }

    @Test fun invalidLevelsAreBoundedAndInvalidSlotsAreIgnored() {
        val state = DuoNetworkState()
        assertFalse(state.mobile(2, 10, 5, true, true))
        assertFalse(state.mobile(0, -1, 5, true, true))
        state.subscriptions(-1, 20, true)
        state.mobile(1, 20, 99, true, false)
        assertEquals(4, DuoNetworkState.cellLevel(state.snapshot))
        state.mobile(1, 20, -9, true, false)
        assertEquals(0, DuoNetworkState.cellLevel(state.snapshot))
        state.wifi(true, true, 99)
        assertEquals(4, DuoNetworkState.wifiLevel(state.snapshot))
    }

    @Test fun interleavedNativeThreadsPublishCompleteBoundedSnapshots() {
        val state = DuoNetworkState()
        state.subscriptions(10, -1, true)
        val start = CountDownLatch(1)
        val wifi = Thread { start.await(); repeat(10000) { state.wifi(true, true, it % 5) } }
        val mobile = Thread { start.await(); repeat(10000) { state.mobile(0, 10, it % 5, true, true) } }
        wifi.start(); mobile.start(); start.countDown()
        wifi.join(10000); mobile.join(10000)
        assertFalse(wifi.isAlive); assertFalse(mobile.isAlive)
        assertEquals(3, state.snapshot and 3)
        assertEquals(4, DuoNetworkState.wifiLevel(state.snapshot))
        assertEquals(4, DuoNetworkState.cellLevel(state.snapshot))
    }
}
