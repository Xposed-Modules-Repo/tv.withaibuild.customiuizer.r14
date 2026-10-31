package tv.withaibuild.customiuizer.prefs

import android.content.Context
import android.util.AttributeSet
import androidx.preference.ListPreference

/** Persistence/dependency model; Compose owns its presentation and input lifetime. */
class ListPreferenceEx(context: Context, attrs: AttributeSet?) : ListPreference(context, attrs), PreferenceState {
    init { isIconSpaceReserved = false }
    private var listDefaultValue: String? = null
    override fun setDefaultValue(defaultValue: Any?) {
        listDefaultValue = defaultValue?.toString()
        super.setDefaultValue(defaultValue)
    }
    override fun notifyChanged() {
        super.notifyChanged()
        notifyDependencyChange(shouldDisableDependents())
    }
    override fun shouldDisableDependents(): Boolean =
        android.text.TextUtils.equals(listDefaultValue, value) || super.shouldDisableDependents()
    override fun markAsNew() { extras.putBoolean("ui.new", true); notifyChanged() }
    override fun applyHighlight() { extras.putBoolean("ui.highlight", true); notifyChanged() }
    fun setUnsupported(value: Boolean) { extras.putBoolean("ui.unsupported", value); isEnabled = !value }
}
