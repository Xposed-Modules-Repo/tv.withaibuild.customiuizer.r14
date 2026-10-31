package tv.withaibuild.customiuizer.mods.wifiauto

import org.junit.Assert.*
import org.junit.Test
import tv.withaibuild.customiuizer.mods.vpn.VpnAutoSwitchCoordinator
import tv.withaibuild.customiuizer.mods.vpn.VpnAutoSwitchPolicy.*
import tv.withaibuild.customiuizer.mods.wifiauto.WifiAutomationCoordinator.Property
import tv.withaibuild.customiuizer.utils.VpnAutoSwitchSettings.Target
import tv.withaibuild.customiuizer.utils.WifiAutomaticSettings.Rule

class WifiAutomationCoordinatorTest {
    private class Backend : WifiAutomationCoordinator.Backend, VpnAutoSwitchCoordinator.Backend {
        val values = mutableMapOf<Property, Int?>(Property.DND to 1, Property.RINGER to 2, Property.ROTATION to 0)
        val reads = mutableListOf<Property>()
        val writes = mutableListOf<Pair<Property, Int>>()
        var state = Snapshot(true)
        var starts = 0; var stops = 0; var vpnReads = 0; var vpnInterfaces = 0
        var busy = false; var fail = false; var nativeDndRinger = false
        var duringWrite: ((Property) -> Unit)? = null
        var target = Target("system", "profile")
        override fun read(property: Property): Int? { reads.add(property); return values[property] }
        override fun write(property: Property, value: Int): Int? {
            writes.add(property to value)
            if (fail || values[property] == null) return null
            values[property] = value
            if (nativeDndRinger && property == Property.DND) values[Property.RINGER] = if (value == 1) 2 else 0
            duringWrite?.invoke(property)
            return values[property]
        }
        override fun vpn(): VpnAutoSwitchCoordinator.Backend { vpnInterfaces++; return this }
        override fun vpnBusy() = busy
        override fun vpnTarget() = target
        override fun snapshot(): Snapshot { vpnReads++; return state }
        override fun start(target: Target): Boolean {
            starts++; state = Snapshot(true, session = Session(target, Any())); return true
        }
        override fun stop(expected: Session): Boolean {
            stops++
            if (!expected.same(state.session)) return false
            state = Snapshot(true); return true
        }
    }
    private val home = Rule("home", "Home", "home", dnd = 2)

    @Test fun manualRotationDuringAnAudioWriteIsRevalidatedBeforeRestore() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home.copy(rotation = 1), true)
        b.duringWrite = { if (it == Property.DND) b.values[Property.ROTATION] = 0 }
        c.network(home.copy(id = "office", dnd = 4, rotation = 1), true)
        assertEquals(0, b.values[Property.ROTATION])
        assertEquals(1, b.writes.count { it.first == Property.ROTATION })
    }
    @Test fun manualHistoryDuringUnknownIdentityRemainsVetoedAfterIdentityReturns() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home, true); c.network(null, false)
        b.values[Property.DND] = 1; c.statesChanged(false)
        b.values[Property.DND] = 2; c.statesChanged(false)
        c.network(home.copy(dnd = 4), true); c.network(null, true)
        assertEquals(listOf(Property.DND to 2), b.writes)
    }
    @Test fun preexistingAppVpnAndAlwaysOnAreNotChangedByUnsupportedCommands() {
        for (action in 0..1) {
            val b = Backend().apply { target = Target("app", "client"); state = Snapshot(true, session = Session(target, Any())) }
            val c = WifiAutomationCoordinator(b)
            c.network(home.copy(vpn = action), true); c.network(null, true)
            assertEquals(0, b.starts); assertEquals(0, b.stops)
        }
        val b = Backend().apply { state = Snapshot(true, protected = true) }
        WifiAutomationCoordinator(b).network(home.copy(vpn = 1), true)
        assertEquals(0, b.starts)
    }

    @Test fun homeEntersAndLeavesRestoringOnlyWhatWasChanged() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home, true)
        assertEquals(listOf(Property.DND to 2), b.writes)
        c.network(null, true)
        assertEquals(listOf(Property.DND to 2, Property.DND to 1), b.writes)
        assertEquals(0, b.vpnInterfaces)
        assertFalse(b.reads.contains(Property.ROTATION))
    }
    @Test fun scenesHandoffDirectlyWithoutRestoringBetweenTargets() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home, true)
        c.network(home.copy(id = "office", dnd = 4), true)
        c.network(null, true)
        assertEquals(listOf(2, 4, 1), b.writes.map { it.second })
    }
    @Test fun identicalTargetsRoamingAndDuplicateCallbacksDoNotWriteAgain() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home, true)
        val count = b.reads.size
        repeat(1000) { c.network(home, true) }
        assertEquals(count, b.reads.size)
        c.network(home.copy(id = "office"), true)
        assertEquals(1, b.writes.size)
    }
    @Test fun manualDndOffIsNeverTurnedBackOnOnExitOrHandoff() {
        val b = Backend().apply { values[Property.DND] = 4 }; val c = WifiAutomationCoordinator(b)
        c.network(home, true)
        b.values[Property.DND] = 1
        c.sync()
        c.network(home.copy(id = "office", dnd = 3), true)
        c.network(null, true)
        assertEquals(listOf(Property.DND to 2), b.writes)
        assertEquals(1, b.values[Property.DND])
    }
    @Test fun alreadyMatchingStateIsObservedButNeverClaimedForRestore() {
        val b = Backend().apply { values[Property.DND] = 2 }; val c = WifiAutomationCoordinator(b)
        c.network(home, true)
        b.values[Property.DND] = 1
        c.sync(); c.network(null, true)
        assertTrue(b.writes.isEmpty())
    }
    @Test fun newSceneNotManagingAPropertyRestoresItsOriginalState() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home.copy(rotation = 1), true)
        c.network(home.copy(id = "office", rotation = -1), true)
        assertEquals(0, b.values[Property.ROTATION])
        assertEquals(2, b.values[Property.DND])
    }
    @Test fun unknownIdentityDoesNotApplyOrRestoreButConfirmedDisconnectDoes() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home, true)
        c.network(null, false); c.sync()
        assertEquals(1, b.writes.size)
        c.network(null, true)
        assertEquals(2, b.writes.size)
    }
    @Test fun permissionRevocationOrFailedWriteNeverCreatesAnUnprovenLease() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home, true)
        b.values[Property.DND] = null
        c.sync(); c.network(null, true)
        assertEquals(1, b.writes.size)
        val broken = Backend().apply { fail = true }; val other = WifiAutomationCoordinator(broken)
        other.network(home, true); repeat(50) { other.sync() }; other.network(null, true)
        assertEquals(1, broken.writes.size)
    }
    @Test fun rebootStartsFreshAndNeverUsesThePreviousProcessSnapshot() {
        val b = Backend(); WifiAutomationCoordinator(b).network(home, true)
        val rebuilt = WifiAutomationCoordinator(b)
        rebuilt.network(home, true); rebuilt.network(null, true)
        assertEquals(2, b.values[Property.DND]) // No stale pre-reboot origin may be invented.
        assertEquals(1, b.writes.size)
    }
    @Test fun disabledCloseRestoresOnceAndLateCallsDoNotReapply() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home.copy(rotation = 1), true); c.close(true); c.sync()
        assertEquals(1, b.values[Property.DND]); assertEquals(0, b.values[Property.ROTATION])
        assertEquals(4, b.writes.size)
    }
    @Test fun nativeDndRingerEchoDoesNotBecomeAFalseManualOverride() {
        val b = Backend().apply { nativeDndRinger = true }; val c = WifiAutomationCoordinator(b)
        c.network(home.copy(ringer = 0), true)
        c.network(home.copy(id = "office", dnd = 1, ringer = 2), true)
        c.network(null, true)
        assertEquals(2, b.values[Property.RINGER]); assertEquals(1, b.values[Property.DND])
    }
    @Test fun vpnAlreadyOnIsNotStartedOrStoppedWhenLeaving() {
        val b = Backend().apply { state = Snapshot(true, session = Session(target, Any())) }; val c = WifiAutomationCoordinator(b)
        c.network(home.copy(vpn = 1), true); c.sync(); c.network(null, true)
        assertEquals(0, b.starts); assertEquals(0, b.stops)
    }
    @Test fun vpnOwnedStartDirectlyHandsOffToOffAndRestoresOriginalOff() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home.copy(vpn = 1), true)
        c.network(home.copy(id = "office", vpn = 0), true)
        c.network(null, true)
        assertEquals(1, b.starts); assertEquals(1, b.stops)
    }
    @Test fun vpnOffRestoresOnlyItsVerifiedOriginalSystemProfile() {
        val b = Backend().apply { state = Snapshot(true, session = Session(target, Any())) }; val c = WifiAutomationCoordinator(b)
        c.network(home.copy(vpn = 0), true); c.network(null, true)
        assertEquals(1, b.starts); assertEquals(1, b.stops)
    }
    @Test fun manualReplacementProtectedVpnAndApplicationAutomationAreUntouched() {
        for (mode in 0..2) {
            val b = Backend(); val c = WifiAutomationCoordinator(b)
            c.network(home.copy(vpn = 1), true)
            when (mode) {
                0 -> b.state = Snapshot(true, session = Session(Target("app", "other"), Any()))
                1 -> b.state = b.state.copy(protected = true)
                2 -> b.busy = true
            }
            c.sync(); c.network(null, true)
            assertEquals(0, b.stops)
            assertEquals(1, b.starts)
        }
    }
    @Test fun userSwitchAbandonsWithoutWritingAnotherUsersState() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home, true); c.close(false)
        assertEquals(1, b.writes.size)
    }
    @Test fun noRuleDoesNotReadOrWriteAnyPropertyOrVpnInterface() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(null, true); repeat(100) { c.sync() }
        assertTrue(b.reads.isEmpty()); assertTrue(b.writes.isEmpty()); assertEquals(0, b.vpnInterfaces)
    }
    @Test fun failedExitRestorationWaitsForAnExplicitStateOpportunity() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home, true); b.fail = true; c.network(null, true)
        assertEquals(2, b.values[Property.DND]); assertTrue(c.pendingRestoration)
        val attempts = b.writes.size
        repeat(100) { c.network(null, true) }
        assertEquals(attempts, b.writes.size)
        b.fail = false; c.statesChanged(true)
        assertEquals(1, b.values[Property.DND]); assertFalse(c.pendingRestoration)
    }
    @Test fun unavailableReadbackRetainsAnchorAndManualChangeRevokesIt() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home, true); b.values[Property.DND] = null; c.network(null, true)
        assertTrue(c.pendingRestoration)
        b.values[Property.DND] = 4; c.statesChanged(true)
        assertFalse(c.pendingRestoration); assertEquals(listOf(Property.DND to 2), b.writes)
        assertEquals(4, b.values[Property.DND])
    }
    @Test fun disabledRecoveryIsBoundedAndUserSwitchDropsItsAuthority() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home, true); b.fail = true; c.close(true)
        assertTrue(c.pendingRestoration)
        c.close(false); assertFalse(c.pendingRestoration)
        b.fail = false; c.statesChanged(true)
        assertEquals(2, b.values[Property.DND])
    }
    @Test fun disableThenReenableKeepsOneOriginalAcrossSceneHandoff() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home, true); b.fail = true; c.close(true)
        b.fail = false; c.network(home.copy(id = "office", dnd = 4), true)
        c.network(null, true)
        assertEquals(1, b.values[Property.DND]); assertFalse(c.pendingRestoration)
    }
    @Test fun failedReadbackCanLaterConfirmTheOriginalWithoutAnotherWrite() {
        val b = Backend(); val c = WifiAutomationCoordinator(b)
        c.network(home, true)
        b.duringWrite = { if (it == Property.DND) b.values[it] = null }
        c.network(null, true); assertTrue(c.pendingRestoration)
        b.values[Property.DND] = 1; b.duringWrite = null
        val count = b.writes.size; c.statesChanged(true)
        assertFalse(c.pendingRestoration); assertEquals(count, b.writes.size)
    }

}
