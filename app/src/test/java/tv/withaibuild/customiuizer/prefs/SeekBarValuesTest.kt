package tv.withaibuild.customiuizer.prefs

import java.io.File
import javax.xml.parsers.DocumentBuilderFactory
import org.junit.Assert.*
import org.junit.Test
import org.w3c.dom.Element

class SeekBarValuesTest {
    @Test fun toastOffsetsUseDisplayedNumbersAndKeepTheirStoredEncoding() {
        val values = SeekBarValues(0, 800, 1, 400, 1)
        assertEquals("0", values.number(400))
        assertEquals(425, values.parse("+25"))
        assertEquals(375, values.parse("-25"))
        assertEquals(0, values.parse("-400"))
        assertEquals(800, values.parse("400"))
        assertNull(values.parse("401"))
        assertNull(values.parse("-401"))
    }

    @Test fun decimalsPercentagesAndStepsAreExactBeforeIntegerConversion() {
        val half = SeekBarValues(0, 40, 1, 20, 2)
        assertEquals(23, half.parse("1.5"))
        assertEquals(17, half.parse("-1,5"))
        assertEquals("1.5", half.number(23))
        assertNull(half.parse("1.25"))
        val scale = SeekBarValues(45, 200, 5, 0, 100)
        assertEquals(125, scale.parse("1.25"))
        assertNull(scale.parse("1.26"))
        val percent = SeekBarValues(20, 95, 5, 0, 1)
        assertEquals(65, percent.parse("65"))
        assertNull(percent.parse("66"))
        assertNull(percent.parse("65%"))
    }

    @Test fun malformedUnboundedAndNonfiniteInputNeverProducesAStoredValue() {
        val values = SeekBarValues(0, Int.MAX_VALUE, 1, 0, 1)
        for (input in listOf("", " ", "NaN", "Infinity", "-Infinity", "2147483648", "-2147483649",
            "1e3", "1.2.3", "--1", "5 dp", "1,2,3", "9".repeat(1000))) assertNull(input, values.parse(input))
        assertEquals(Int.MAX_VALUE, values.parse(Int.MAX_VALUE.toString()))
        assertEquals(Int.MAX_VALUE, values.storage(values.progress(Int.MAX_VALUE)))
        val shifted = SeekBarValues(0, Int.MAX_VALUE, 1, Int.MAX_VALUE, 2)
        assertEquals(0, shifted.parse("-1073741823.5"))
        assertEquals(Int.MAX_VALUE, shifted.parse("0"))
    }

    @Test fun changedBoundsStayOnTheOriginalStepLattice() {
        val values = SeekBarValues(11, 29, 5, 0, 1)
        assertEquals(15, values.minimum)
        assertEquals(25, values.maximum)
        assertEquals(15, values.bounded(0))
        assertEquals(25, values.bounded(Int.MAX_VALUE))
        assertNull(values.parse("11"))
        assertEquals(20, values.parse("20"))
        assertFalse(SeekBarValues(11, 12, 5, 0, 1).isValid)
    }

}
