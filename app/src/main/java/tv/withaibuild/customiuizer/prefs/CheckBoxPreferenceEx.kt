package tv.withaibuild.customiuizer.prefs

import android.content.Context
import android.util.AttributeSet
import androidx.preference.SwitchPreference

/** Persistence/dependency model; Compose owns its presentation and input lifetime. */
class CheckBoxPreferenceEx(context: Context, attrs: AttributeSet?) : SwitchPreference(context, attrs), PreferenceState {
    init { isIconSpaceReserved = false }
    override fun markAsNew() { extras.putBoolean("ui.new", true); notifyChanged() }
    override fun applyHighlight() { extras.putBoolean("ui.highlight", true); notifyChanged() }
    fun setUnsupported(value: Boolean) { extras.putBoolean("ui.unsupported", value); isEnabled = !value }
}
