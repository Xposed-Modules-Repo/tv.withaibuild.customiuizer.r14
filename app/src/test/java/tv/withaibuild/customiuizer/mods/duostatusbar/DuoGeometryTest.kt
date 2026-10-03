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
        assertEquals(67, DuoSizing.pixels(auto, 59, 2.8875f))
        assertEquals(46, DuoSizing.pixels(auto, 0, 2f))
        assertEquals(40, DuoSizing.pixels(auto, 10, 2f))
        assertEquals(48, DuoSizing.pixels(auto, 200, 2f))
        assertEquals(56, DuoSizing.pixels(DuoConfig(true, 28, autoSize = false), 59, 2f))
        assertEquals(1, DuoSizing.pixels(auto, 0, 0.001f))
    }

    @Test fun verticalAdjustmentFitsEveryRowAndCannotEraseASmallDoubleRow() {
        for (height in 1..200) for (offset in -40..40) {
            val actual = DuoSizing.offset(height, offset / 2f)
            val size = DuoSizing.drawingSize(120, height, actual)
            val top = (height - size) / 2f + actual
            assertTrue(size >= minOf(120f, height * 0.75f))
            assertTrue(top >= -0.001f)
            assertTrue(top + size <= height + 0.001f)
        }
        assertEquals(4f, DuoConfig(true, 24, verticalOffset = 999).offsetDp, 0f)
        assertEquals(-4f, DuoConfig(true, 24, verticalOffset = -999).offsetDp, 0f)
        assertEquals(0f, DuoConfig.read(PrefMap()).offsetDp, 0f)
    }

    @Test fun automaticSizeScalesAcrossDensitiesAndPreservesTheOnePercentReduction() {
        val config = DuoConfig(true, 24)
        for (density in floatArrayOf(1f, 1.5f, 2f, 2.8875f, 4f)) {
            for (nativeDp in floatArrayOf(12f, 18f, 20f, 24f, 40f)) {
                // Both versions receive the same integer native resource, not the unrounded input.
                val nativeHeight = (nativeDp * density).toInt()
                val oldDp = (nativeHeight / density * 1.155f).coerceIn(20f, 24f)
                val px = DuoSizing.pixels(config, nativeHeight, density)
                assertEquals(oldDp * 0.99f * density, px.toFloat(), 0.501f)
            }
        }
    }

    @Test fun tintAlphaIsPreservedAndUnknownBluetoothBatteryNeverMeansFull() {
        val native = 0x99ffffff.toInt()
        assertEquals(0x99, DuoGeometry.batteryColor(native, 100, true, false) ushr 24)
        assertEquals(33, DuoGeometry.dim(native) ushr 24)
        assertEquals(0x99, DuoGeometry.bluetoothColor(native, 2) ushr 24)
        assertEquals(DuoGeometry.bluetoothColor(native, 0), DuoGeometry.bluetoothColor(native, 10))
        assertNotEquals(DuoGeometry.bluetoothColor(native, 0), DuoGeometry.bluetoothColor(native, 1))
        assertNotEquals(DuoGeometry.bluetoothColor(native, 0), DuoGeometry.bluetoothColor(native, 5))
        assertNotEquals(DuoGeometry.bluetoothColor(-1, 0), DuoGeometry.bluetoothColor(0xff000000.toInt(), 0))
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
