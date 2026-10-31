package tv.withaibuild.customiuizer.mods.island

import org.junit.Assert.*
import org.junit.Test

class IslandLaunchFlightTest {
    private val a = IslandLaunchFlight.Target("clock", null, "timer")
    private val b = IslandLaunchFlight.Target("music", null, "session")

    @Test fun duplicatePendingAndSubmittedRequestsRemainSingleFlightAcrossCancellation() {
        var time = 0L
        val flight = IslandLaunchFlight { time }
        val request = flight.begin(a)!!
        repeat(30) { assertNull(flight.begin(a)) }
        flight.submitted(request)
        flight.finished(request) // Cancellation/completion cannot revoke the external side effect.
        repeat(30) { assertNull(flight.begin(a)) }
        time = 2499L; assertNull(flight.begin(a))
        time = 2500L; assertNotNull(flight.begin(a))
    }

    @Test fun differentTargetsAndABAStayIndependentAndForegroundDepartureAllowsReopen() {
        val flight = IslandLaunchFlight { 0L }
        val first = flight.begin(a)!!; flight.submitted(first); flight.finished(first)
        val second = flight.begin(b)!!; flight.submitted(second); flight.finished(second)
        assertNull(flight.begin(a))
        assertNotNull(flight.begin(a.copy(identity = "stopwatch")))
        flight.foreground("clock", null)
        assertNull(flight.begin(a))
        flight.foreground("launcher", null)
        assertNotNull(flight.begin(a))
        assertNull(flight.begin(b))
    }

    @Test fun failedOrUnexecutedRequestsReleaseOnlyTheirOwnReservationAndDisposeClearsAll() {
        var time = 0L
        val flight = IslandLaunchFlight { time }
        val canceled = flight.begin(a)!!
        flight.finished(canceled)
        val replacement = flight.begin(a)!!
        flight.finished(canceled)
        assertNull(flight.begin(a))
        flight.submitted(replacement); time = 2500L
        val retry = flight.begin(a)!!
        flight.finished(replacement)
        assertNull(flight.begin(a))
        flight.finished(retry)
        assertNotNull(flight.begin(a))
        flight.clear(); assertNotNull(flight.begin(a))
    }
}
