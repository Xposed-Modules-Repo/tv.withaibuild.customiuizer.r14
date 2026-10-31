package tv.withaibuild.customiuizer.ui

import androidx.compose.ui.graphics.Color
import org.junit.Assert.assertEquals
import org.junit.Test
import tv.withaibuild.customiuizer.utils.searchTerms

class SearchHighlightTest {
    private fun spans(text: String, query: String): List<Pair<Int, Int>> {
        val result = highlightSearchText(text, searchTerms(query), Color.Blue)
        assertEquals(text, result.text)
        return result.spanStyles.map { it.start to it.end }
    }

    @Test fun matchesCaseInsensitiveTermsInTitlesAndBreadcrumbs() {
        assertEquals(listOf(0 to 4, 18 to 24), spans("Dock background / Layout", "DOCK layout"))
    }

    @Test fun removedDashesDoNotShiftDisplayOffsets() {
        assertEquals(listOf(0 to 5, 8 to 12), spans("Wi-Fi / WIFI", "wi-fi"))
        assertEquals(listOf(0 to 5), spans("Wi‑Fi", "wifi"))
    }

    @Test fun allChineseOccurrencesAreHighlighted() {
        assertEquals(listOf(0 to 3, 8 to 11), spans("灵动岛 / 通知灵动岛", "灵动岛"))
    }

    @Test fun expandingLowercaseAndSurrogatePairsRetainOriginalOffsets() {
        assertEquals(listOf(0 to 1, 1 to 4), spans("İkon", "i kon"))
        assertEquals(listOf(2 to 6), spans("🎵Dock", "dock"))
    }

    @Test fun overlappingTermsAndMissingMatchesKeepTheDisplayText() {
        assertEquals(listOf(0 to 2, 1 to 3, 2 to 4), spans("aaaa", "aa"))
        assertEquals(emptyList<Pair<Int, Int>>(), spans("Dock", "volume"))
        assertEquals(emptyList<Pair<Int, Int>>(), spans("Dock", "  "))
    }
}
