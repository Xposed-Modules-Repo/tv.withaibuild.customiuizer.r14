package tv.withaibuild.customiuizer.mods.duostatusbar

import org.junit.Assert.*
import org.junit.Test
import tv.withaibuild.customiuizer.utils.PrefMap

class DuoGeometryTest {
    @Test fun percentAndChargingOpenOnlyTheAppropriateTopGap() {
        assertEquals(0f, DuoGeometry.topGap(false, false), 0f)
        assertEquals(71.3f, DuoGeometry.topGap(true, false), 0f)
        assertEquals(55.6f, DuoGeometry.topGap(true, true), 0f)
        assertEquals(55.6f, DuoGeometry.topGap(false, true), 0f)
    }

    @Test fun ringFillIsBoundedMonotonicAndHalfFullAtFiftyPercent() {
        for (gap in floatArrayOf(0f, 71.3f, 55.6f)) {
            val side = DuoGeometry.sideSweep(gap)
            var last = 0f
            for (level in 0..100) {
                val left = DuoGeometry.leftFill(level, side)
                val right = DuoGeometry.rightFill(level, side)
                assertTrue(left in 0f..side); assertTrue(right in 0f..side)
                assertTrue(left + right >= last)
                assertEquals(side * 2 * level / 100, left + right, 0.001f)
                last = left + right
            }
            assertEquals(side, DuoGeometry.leftFill(50, side), 0f)
            assertEquals(0f, DuoGeometry.rightFill(50, side), 0f)
            assertEquals(360f, 2 * side + gap + DuoGeometry.BOTTOM_GAP, 0.001f)
        }
    }

    @Test fun chargingWinsOverSaverAndCriticalBattery() {
        assertEquals(0xff34c759.toInt(), DuoGeometry.batteryColor(-1, 5, true, true))
        assertEquals(0xfff2b900.toInt(), DuoGeometry.batteryColor(-1, 5, false, true))
        assertEquals(0xffff3b30.toInt(), DuoGeometry.batteryColor(-1, 19, false, false))
        assertEquals(-1, DuoGeometry.batteryColor(-1, 20, false, false))
        assertEquals(0xff000000.toInt(), DuoGeometry.batteryColor(0xff000000.toInt(), 50, false, false))
    }

    @Test fun configHasSafeDefaultsAndBoundsImportedSettings() {
        val defaults = DuoConfig.read(PrefMap())
        assertTrue(defaults.showPercent); assertEquals(24, defaults.sizeDp)
        assertTrue(defaults.autoSize); assertTrue(defaults.showAudio)
        assertFalse(defaults.bold)
        val imported = PrefMap().apply {
            put("system_statusbar_duo_percent", false)
            put("system_statusbar_duo_size", 999)
            put("system_statusbar_duo_autosize", false)
            put("system_statusbar_duo_audio", false)
            put("system_statusbar_duo_bold", true)
        }
        assertFalse(DuoConfig.read(imported).showPercent)
        assertEquals(40, DuoConfig.read(imported).sizeDp)
        assertFalse(DuoConfig.read(imported).autoSize)
        assertFalse(DuoConfig.read(imported).showAudio)
        assertTrue(DuoConfig.read(imported).bold)
        assertEquals(20, DuoConfig(true, -999).sizeDp)
    }

    @Test fun automaticSizeUsesNativeDensityAndKeepsImportedManualSizeSeparate() {
        val auto = DuoConfig(true, 28)
        assertEquals(65, DuoSizing.pixels(auto, 59, 2.8875f))
        assertEquals(44, DuoSizing.pixels(auto, 0, 2f))
        assertEquals(40, DuoSizing.pixels(auto, 10, 2f))
        assertEquals(48, DuoSizing.pixels(auto, 200, 2f))
        assertEquals(56, DuoSizing.pixels(DuoConfig(true, 28, autoSize = false), 59, 2f))
        assertEquals(1, DuoSizing.pixels(auto, 0, 0.001f))
    }

    @Test fun repeatedHidingDoesNotLoseTheOriginalVisibility() {
        val state = DuoVisibility(0)
        state.observe(0); state.hide()
        repeat(10000) { state.observe(8); state.hide() }
        assertEquals(0, state.release())
        state.observe(4); state.hide()
        assertEquals(4, state.release())
    }

    @Test fun nativeRemovalWhileHiddenIsPreservedOnRollback() {
        val state = DuoVisibility(0)
        state.hide(); state.observeNativeGone(); state.observe(8)
        assertEquals(8, state.release())
        state.observe(0); state.hide()
        assertEquals(0, state.release())
    }

    @Test fun inactiveRollbackAndDetachDoNotOverwriteNewNativeVisibility() {
        val state = DuoVisibility(0)
        state.hide()
        assertEquals(0, state.release(8))
        assertEquals(8, state.release(8))
        assertEquals(4, state.release(4))
    }
}
