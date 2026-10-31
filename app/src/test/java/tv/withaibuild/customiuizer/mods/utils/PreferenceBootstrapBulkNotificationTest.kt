package tv.withaibuild.customiuizer.mods.utils

import android.content.SharedPreferences
import org.junit.Assert.*
import org.junit.Test
import tv.withaibuild.customiuizer.utils.FakeSharedPreferences
import tv.withaibuild.customiuizer.utils.PrefMap

class PreferenceBootstrapBulkNotificationTest {
    @Test fun nullListenerNotificationPublishesAnEmptySnapshotBeforeDispatch() {
        val fixture = Fixture()
        fixture.stored.edit().clear().apply()

        fixture.notifyBulk()

        assertTrue(fixture.prefs.getAll().isEmpty())
        assertEquals(PreferenceBootstrap.State.VALID_EMPTY, fixture.bootstrap.getState())
        assertTrue(fixture.bootstrap.isReady())
        assertEquals(listOf(emptyMap<String, Any>()), fixture.observedSnapshots)
        assertEquals(listOf<String?>(null), fixture.dispatchedKeys)
        assertEquals(3, fixture.reads)
        assertEquals(1, fixture.registrations)
    }

    @Test fun nullListenerNotificationReplacesTheWholeSnapshotAndKeepsTheListener() {
        val fixture = Fixture()
        fixture.stored.edit().clear().putInt("pref_key_system_charginginfo_fontsize", 30).apply()

        fixture.notifyBulk()

        val expected = mapOf("system_charginginfo_fontsize" to 30)
        assertEquals(expected, fixture.prefs.getAll())
        assertEquals(listOf(expected), fixture.observedSnapshots)
        assertEquals(PreferenceBootstrap.State.LOADED, fixture.bootstrap.getState())
        assertEquals(1, fixture.registrations)

        fixture.stored.put("pref_key_system_charginginfo_fontsize", 32)
        fixture.listener.onSharedPreferenceChanged(fixture.remote, "pref_key_system_charginginfo_fontsize")
        assertEquals(32, fixture.prefs.getInt("system_charginginfo_fontsize", 16))
        assertEquals(listOf(null, "system_charginginfo_fontsize"), fixture.dispatchedKeys)
        assertEquals("single-key update must retain its incremental read", 3, fixture.reads)
    }

    @Test fun aFailedOrUnavailableBulkReadKeepsTheLastGoodSnapshotWithoutDispatch() {
        val fixture = Fixture()
        val before = fixture.prefs.getAll()
        fixture.readFailure = IllegalStateException("remote unavailable")
        fixture.notifyBulk()
        fixture.readFailure = null
        fixture.nullSnapshot = true
        fixture.notifyBulk()

        assertEquals(before, fixture.prefs.getAll())
        assertTrue(fixture.dispatchedKeys.isEmpty())
        assertTrue(fixture.observedSnapshots.isEmpty())
        assertEquals(4, fixture.reads)
        assertEquals(PreferenceBootstrap.State.LOADED, fixture.bootstrap.getState())
    }

    @Test fun fatalBulkReadErrorsEscapeWithoutPublishingOrDispatching() {
        val fixture = Fixture()
        val before = fixture.prefs.getAll()
        val failure = OutOfMemoryError("remote fatal")
        fixture.readFailure = failure

        assertSame(failure, assertThrows(OutOfMemoryError::class.java) { fixture.notifyBulk() })

        assertEquals(before, fixture.prefs.getAll())
        assertTrue(fixture.dispatchedKeys.isEmpty())
        assertEquals(3, fixture.reads)
    }

    private class Fixture {
        val stored = FakeSharedPreferences().apply { put("pref_key_system_charginginfo", true) }
        val prefs = PrefMap()
        val dispatchedKeys = mutableListOf<String?>()
        val observedSnapshots = mutableListOf<Map<String, Any>>()
        lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener
        var registrations = 0
        var reads = 0
        var readFailure: Throwable? = null
        var nullSnapshot = false
        val remote = object : SharedPreferences by stored {
            override fun getAll(): Map<String, *>? {
                reads++
                readFailure?.let { throw it }
                return if (nullSnapshot) null else stored.all
            }

            override fun registerOnSharedPreferenceChangeListener(next: SharedPreferences.OnSharedPreferenceChangeListener) {
                listener = next
                registrations++
            }
        }
        val bootstrap = PreferenceBootstrap.create(prefs, { remote }) { key ->
            dispatchedKeys.add(key)
            observedSnapshots.add(prefs.getAll())
        }

        init {
            assertTrue(bootstrap.bootstrap())
            assertTrue("initial publication must stay silent", dispatchedKeys.isEmpty())
        }

        fun notifyBulk() = listener.onSharedPreferenceChanged(remote, null)
    }
}
