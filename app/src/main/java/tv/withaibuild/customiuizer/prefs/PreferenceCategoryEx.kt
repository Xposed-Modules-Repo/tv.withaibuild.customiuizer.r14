package tv.withaibuild.customiuizer.prefs

import android.content.Context
import android.util.AttributeSet
import androidx.preference.Preference
import androidx.preference.PreferenceCategory
import androidx.preference.PreferenceScreen

class PreferenceCategoryEx(context: Context, attrs: AttributeSet?) : PreferenceCategory(context, attrs) {
    override fun onPrepareAddPreference(preference: Preference): Boolean {
        preference.onParentChanged(this, shouldDisableDependents())
        return true
    }
    fun setUnsupported(value: Boolean) { extras.putBoolean("ui.unsupported", value); isEnabled = !value }
    fun isDynamic(): Boolean = extras.getBoolean("ui.dynamic")
    fun isTitleVisible(): Boolean = extras.getInt("ui.state", 0) == 0
    fun isPageContainer(): Boolean = extras.getInt("ui.state", 0) == 2 && parent is PreferenceScreen
    fun hide() { extras.putInt("ui.state", 2); notifyChanged() }
}
