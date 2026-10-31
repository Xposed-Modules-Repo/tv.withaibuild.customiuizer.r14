package tv.withaibuild.customiuizer.mods.appbehavior

import org.junit.Assert.*
import org.junit.Test

class VolumeMemoryTest {
    @Test fun rapidSwitchDeathDisableAndManualInputInvalidatePendingRestores() {
        val session = VolumeFocusSession()
        val a = Any(); val b = Any()
        val first = session.gained(a, "app.a", 10001, true)!!
        val revision = session.version
        assertTrue(session.canRestore(first, revision))
        assertNull(session.gained(a, "app.a", 10001, true)) // Duplicate native gain is not another restore.
        session.manuallyChanged()
        assertFalse(session.canRestore(first, revision))
        val second = session.gained(b, "app.b", 10002, true)!!
        assertFalse(session.canRestore(first, session.version))
        session.released(a) // Delayed release from the old process must not clear the new owner.
        assertTrue(session.canRestore(second, session.version))
        session.configure(emptySet())
        assertFalse(session.canRestore(second, session.version))
        val revived = session.gained(a, "app.a", 10001, true)!!
        session.released(a)
        assertFalse(session.canRestore(revived, session.version))
        session.gained(b, "transient", 10003, false)
        assertNull(session.owner)
    }
    @Test fun applicationsAndSpeakerBluetoothRoutesRemainIndependentAfterReconstruction() {
        val memory = VolumeMemory()
        assertNull(memory.get("app", 2))
        memory.put("app", 2, 5)
        memory.put("app", 128, 8)
        memory.put("other", 2, 3)
        val rebuilt = VolumeMemory().apply { decode(memory.encode()) }
        assertEquals(5, rebuilt.get("app", 2))
        assertEquals(8, rebuilt.get("app", 128))
        assertEquals(3, rebuilt.get("other", 2))
        assertNull(rebuilt.get("other", 128))
    }

    @Test fun onlyActualChangesNeedPersistenceAndGrowthIsBounded() {
        val memory = VolumeMemory()
        assertTrue(memory.put("app", 2, 5))
        assertFalse(memory.put("app", 2, 5))
        repeat(300) { memory.put("app$it", 2, it) }
        assertEquals(256, memory.encode().split(';').size)
        assertNull(memory.get("app", 2))
        assertEquals(299, memory.get("app299", 2))
    }

    @Test fun corruptPersistedValuesAreIgnored() {
        val memory = VolumeMemory().apply { decode("bad;app@2=NaN;app@128=-1;app@4=5000;app@2=4") }
        assertEquals(4, memory.get("app", 2))
        assertNull(memory.get("app", 128))
        assertNull(memory.get("app", 4))
    }
}
