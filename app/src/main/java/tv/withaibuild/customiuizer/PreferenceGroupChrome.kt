package tv.withaibuild.customiuizer

import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen
import tv.withaibuild.customiuizer.prefs.PreferenceCategoryEx

/**
 * Settings-app chrome only. Maps the visible Preference adapter order onto
 * Telegram-style grouped inset cards: top-level category titles sit outside the
 * card, nested categories stay inside as inner headers.
 */
internal object PreferenceGroupChrome {

    enum class Kind {
        SECTION_HEADER,
        HIDDEN_HEADER,
        INNER_HEADER,
        CARD,
    }

    data class InputRow(
        val isCategory: Boolean,
        val isTopLevel: Boolean,
        val titleVisible: Boolean,
        val supportingText: Boolean = false,
    )

    data class Chrome(
        val kind: Kind,
        val groupId: Int,
        val inCard: Boolean,
        val cardStart: Boolean,
        val cardEnd: Boolean,
        val dividerBelow: Boolean,
    )

    fun layout(rows: List<InputRow>): List<Chrome> {
        if (rows.isEmpty()) return emptyList()
        val result = ArrayList<Chrome>(rows.size)
        var index = 0
        var groupId = 0
        while (index < rows.size) {
            val row = rows[index]
            if (isTopLevelCategory(row)) {
                val bodyStart = index + 1
                var bodyEnd = bodyStart
                while (bodyEnd < rows.size && !isTopLevelCategory(rows[bodyEnd])) {
                    bodyEnd++
                }
                result.add(
                    Chrome(
                        kind = if (row.titleVisible) Kind.SECTION_HEADER else Kind.HIDDEN_HEADER,
                        groupId = groupId,
                        inCard = false,
                        cardStart = false,
                        cardEnd = false,
                        dividerBelow = false,
                    )
                )
                appendCard(result, rows, bodyStart, bodyEnd, groupId)
                index = bodyEnd
            } else {
                var bodyEnd = index
                while (bodyEnd < rows.size && !isTopLevelCategory(rows[bodyEnd])) {
                    bodyEnd++
                }
                appendCard(result, rows, index, bodyEnd, groupId)
                index = bodyEnd
            }
            groupId++
        }
        return result
    }

    private fun isTopLevelCategory(row: InputRow): Boolean = row.isCategory && row.isTopLevel

    private fun appendCard(
        result: MutableList<Chrome>,
        rows: List<InputRow>,
        start: Int,
        end: Int,
        groupId: Int,
    ) {
        if (start >= end) return
        val last = end - 1
        var hasOwner = false
        for (i in start until end) {
            if (rows[i].isCategory) hasOwner = false
            else if (!rows[i].supportingText) hasOwner = true
            result.add(
                Chrome(
                    kind = if (rows[i].isCategory) Kind.INNER_HEADER else Kind.CARD,
                    groupId = groupId,
                    inCard = true,
                    cardStart = i == start,
                    cardEnd = i == last,
                    dividerBelow = i != last && !rows[i].isCategory && !rows[i + 1].isCategory &&
                        !rows[i + 1].supportingText && (!rows[i].supportingText || hasOwner),
                )
            )
        }
    }
}

internal fun preferenceGroupInputRow(pref: Preference): PreferenceGroupChrome.InputRow {
    return PreferenceGroupChrome.InputRow(
        isCategory = pref is PreferenceCategory,
        isTopLevel = pref.parent is PreferenceScreen ||
            (pref.parent as? PreferenceCategoryEx)?.isPageContainer() == true,
        titleVisible = (pref as? PreferenceCategoryEx)?.isTitleVisible() ?: true,
        supportingText = isSupportingPreference(pref),
    )
}

internal fun isSupportingPreference(pref: Preference): Boolean =
    pref !is PreferenceCategory && !pref.isSelectable && pref.title.isNullOrEmpty() && !pref.summary.isNullOrEmpty()
