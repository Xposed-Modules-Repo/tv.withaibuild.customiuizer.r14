package tv.withaibuild.customiuizer

import tv.withaibuild.customiuizer.testsupport.repositorySource as source

import java.nio.file.Files
import java.nio.file.Path
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class PreferenceGroupChromeTest {

    @Test
    fun topLevelCategoriesBecomeHeadersOutsideCards() {
        val chrome = PreferenceGroupChrome.layout(
            listOf(
                header(),
                item(),
                item(),
                header(),
                item(),
            )
        )
        assertEquals(5, chrome.size)
        assertEquals(PreferenceGroupChrome.Kind.SECTION_HEADER, chrome[0].kind)
        assertFalse(chrome[0].inCard)
        assertTrue(chrome[1].inCard && chrome[1].cardStart)
        assertTrue(chrome[2].inCard && chrome[2].cardEnd)
        assertTrue(chrome[1].dividerBelow)
        assertFalse(chrome[2].dividerBelow)
        assertEquals(chrome[1].groupId, chrome[2].groupId)
        assertTrue(chrome[0].groupId != chrome[3].groupId)
        assertEquals(PreferenceGroupChrome.Kind.SECTION_HEADER, chrome[3].kind)
        assertTrue(chrome[4].inCard && chrome[4].cardStart && chrome[4].cardEnd)
    }

    @Test
    fun nestedCategoriesStayInsideTheCard() {
        val chrome = PreferenceGroupChrome.layout(
            listOf(
                header(),
                item(),
                innerHeader(),
                item(),
            )
        )
        assertEquals(PreferenceGroupChrome.Kind.INNER_HEADER, chrome[2].kind)
        assertTrue(chrome[2].inCard)
        assertEquals(chrome[1].groupId, chrome[2].groupId)
        assertEquals(chrome[2].groupId, chrome[3].groupId)
        assertTrue(chrome[1].cardStart)
        assertTrue(chrome[3].cardEnd)
        assertFalse("Inner headings have breathing room, without rules on either side", chrome[1].dividerBelow)
        assertFalse(chrome[2].dividerBelow)
    }

    @Test
    fun hiddenTopLevelTitleDoesNotSitInTheCard() {
        val chrome = PreferenceGroupChrome.layout(
            listOf(
                hiddenHeader(),
                item(),
                item(),
            )
        )
        assertEquals(PreferenceGroupChrome.Kind.HIDDEN_HEADER, chrome[0].kind)
        assertFalse(chrome[0].inCard)
        assertTrue(chrome[1].cardStart)
        assertTrue(chrome[2].cardEnd)
        assertEquals(chrome[0].groupId, chrome[1].groupId)
    }

    @Test
    fun headerOnlyGroupHasNoCard() {
        val chrome = PreferenceGroupChrome.layout(listOf(header(), header(), item()))
        assertEquals(PreferenceGroupChrome.Kind.SECTION_HEADER, chrome[0].kind)
        assertFalse(chrome[0].inCard)
        assertEquals(PreferenceGroupChrome.Kind.SECTION_HEADER, chrome[1].kind)
        assertTrue(chrome[2].inCard && chrome[2].cardStart && chrome[2].cardEnd)
    }

    @Test
    fun ungroupedPrefixBecomesItsOwnCard() {
        val chrome = PreferenceGroupChrome.layout(
            listOf(
                item(),
                item(),
                header(),
                item(),
            )
        )
        assertTrue(chrome[0].inCard && chrome[0].cardStart)
        assertTrue(chrome[1].inCard && chrome[1].cardEnd)
        assertEquals(PreferenceGroupChrome.Kind.SECTION_HEADER, chrome[2].kind)
        assertTrue(chrome[0].groupId != chrome[2].groupId)
    }

    @Test
    fun emptyInputIsEmpty() {
        assertTrue(PreferenceGroupChrome.layout(emptyList()).isEmpty())
    }

    private fun header() = PreferenceGroupChrome.InputRow(
        isCategory = true,
        isTopLevel = true,
        titleVisible = true,
    )

    @Test fun supportingTextStaysWithItsOwnerAndSectionIntrosDoNotGetARule() {
        val description = PreferenceGroupChrome.InputRow(false, false, true, supportingText = true)
        val chrome = PreferenceGroupChrome.layout(listOf(header(), description, item(), description, description, item()))
        assertFalse(chrome[1].dividerBelow)
        assertFalse(chrome[2].dividerBelow)
        assertFalse(chrome[3].dividerBelow)
        assertTrue("The rule follows the whole feature, including its explanation", chrome[4].dividerBelow)
        assertFalse(chrome[5].dividerBelow)
        assertTrue(chrome.drop(1).all { it.groupId == chrome[1].groupId })
    }

    private fun hiddenHeader() = PreferenceGroupChrome.InputRow(
        isCategory = true,
        isTopLevel = true,
        titleVisible = false,
    )

    private fun innerHeader() = PreferenceGroupChrome.InputRow(
        isCategory = true,
        isTopLevel = false,
        titleVisible = true,
    )

    private fun item() = PreferenceGroupChrome.InputRow(
        isCategory = false,
        isTopLevel = false,
        titleVisible = true,
    )
}
