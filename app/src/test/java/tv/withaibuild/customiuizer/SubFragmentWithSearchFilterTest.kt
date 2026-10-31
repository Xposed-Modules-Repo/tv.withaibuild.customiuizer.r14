package tv.withaibuild.customiuizer

import org.junit.Assert.*
import org.junit.Test
import tv.withaibuild.customiuizer.utils.AppData

class SubFragmentWithSearchFilterTest {
    private fun row(label: String, activity: String = "", pkg: String = "example") = AppSelectionRow(AppData().apply {
        this.label = label; actName = activity; pkgName = pkg; prepareForList()
    })
    @Test fun emptyQueryReusesTheSnapshotAndSearchUsesPreparedRootLocaleKeys() {
        val rows = listOf(row("IMEI"), row("蓝牙"), row("Other"))
        assertSame(rows, filterAppSelectionRows(rows, "", false))
        assertEquals(listOf(rows[0]), filterAppSelectionRows(rows, "imei", false))
        assertEquals(listOf(rows[1]), filterAppSelectionRows(rows, "蓝", false))
        assertTrue(filterAppSelectionRows(rows, "absent", false).isEmpty())
        assertEquals(3, rows.size)
    }
    @Test fun activitySearchAlsoMatchesClassAndSyntheticDefaultSurvivesEveryQuery() {
        val rows = listOf(row("Default", pkg = ""), row("App", "example.Scanner"), row("Else"))
        assertEquals(listOf(rows[0]), filterAppSelectionRows(rows, "scanner", false))
        assertEquals(rows.take(2), filterAppSelectionRows(rows, "scanner", true))
        assertEquals(listOf(rows[0]), filterAppSelectionRows(rows, "missing", true))
    }
}
