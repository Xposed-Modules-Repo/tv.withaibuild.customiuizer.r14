package tv.withaibuild.customiuizer.prefs

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference

/** Persistence/dependency model; Compose owns its presentation and input lifetime. */
open class PreferenceEx(context: Context, attrs: AttributeSet?) : Preference(context, attrs), PreferenceState {
    init { isIconSpaceReserved = false }
    private var customSummary: String? = null
    fun setCustomSummary(text: String?) { customSummary = text; notifyChanged() }
    internal fun composeSummary(): String? = customSummary
    override fun markAsNew() { extras.putBoolean("ui.new", true); notifyChanged() }
    override fun applyHighlight() { extras.putBoolean("ui.highlight", true); notifyChanged() }
    fun setUnsupported(value: Boolean) { extras.putBoolean("ui.unsupported", value); isEnabled = !value }
}
