package tv.withaibuild.customiuizer.ui

import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.font.FontWeight
import tv.withaibuild.customiuizer.utils.normalizeSearchText

/** Visible search results only; retain display offsets when search normalization removes dashes. */
internal fun highlightSearchText(text: String, terms: List<String>, color: Color): AnnotatedString {
    val normalized = normalizeSearchText(text)
    val ranges = ArrayList<IntRange>()
    for (term in terms) {
        if (term.isEmpty()) continue
        var start = normalized.indexOf(term)
        while (start >= 0) {
            ranges.add(start until start + term.length)
            start = normalized.indexOf(term, start + 1)
        }
    }
    if (ranges.isEmpty()) return AnnotatedString(text)
    val offsets = IntArray(normalized.length)
    var cursor = 0
    text.forEachIndexed { index, char ->
        repeat(normalizeSearchText(char.toString()).length) { offsets[cursor++] = index }
    }
    val style = SpanStyle(color = color, fontWeight = FontWeight.SemiBold)
    return buildAnnotatedString {
        append(text)
        for (range in ranges) addStyle(style, offsets[range.first], offsets[range.last] + 1)
    }
}
