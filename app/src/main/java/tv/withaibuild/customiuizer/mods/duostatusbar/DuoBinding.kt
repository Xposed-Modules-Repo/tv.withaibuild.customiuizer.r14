package tv.withaibuild.customiuizer.mods.duostatusbar

import android.view.View
import android.view.ViewGroup
import android.widget.TextView

/** Owned by the inserted child. The runtime holds only a weak reference to this binding. */
internal class DuoBinding(
    val owner: ViewGroup,
    val root: ViewGroup,
    val view: DuoStatusBarView,
    private val abi: DuoAbi,
) {
    val displayId = root.display?.displayId ?: -1
    val primaryHost = abi.statusBar.isInstance(root) || abi.keyguard.isInstance(root)
    private class Original(val view: View) {
        val visibility = DuoVisibility(view.visibility)
        fun hide() {
            visibility.observe(view.visibility)
            visibility.hide()
            if (view.visibility != View.GONE) view.visibility = View.GONE
        }
        fun restore() {
            val original = visibility.release(view.visibility)
            if (view.visibility != original) view.visibility = original
        }
    }
    private val batteryChildren = ArrayList<Original>(owner.childCount)
    private val signals = ArrayList<Original>(4)
    private var active = false
    private var supported = true

    init {
        for (i in 0 until owner.childCount) {
            val child = owner.getChildAt(i)
            if (child !== view) batteryChildren.add(Original(child))
        }
        collectSignals(root)
        syncTypeface()
    }

    fun syncTypeface() {
        if (view.usesCellularText) view.syncTypeface((abi.percentView.get(owner) as? TextView)?.typeface)
    }

    private fun collectSignals(parent: ViewGroup) {
        for (i in 0 until parent.childCount) {
            val child = parent.getChildAt(i)
            if (abi.wifiView.isInstance(child) || abi.mobileView.isInstance(child)) {
                if (signals.size < 8) signals.add(Original(child)) else supported = false
            } else if (child is ViewGroup && child !== owner) collectSignals(child)
        }
    }

    fun restoreBattery() {
        for (i in batteryChildren.indices) batteryChildren[i].restore()
    }

    fun reconcile(level: Int, charging: Boolean, saver: Boolean, bits: Int, audio: Int) {
        val ready = supported && level in 0..100 &&
            bits and (DuoNetworkState.WIFI_READY or DuoNetworkState.MOBILE_READY) == 3
        if (!ready) {
            restore()
            return
        }
        val text = abi.percentView.get(owner) as TextView
        view.render(level, charging, saver, text.currentTextColor, bits, audio)
        view.visibility = View.VISIBLE
        active = true
        for (i in batteryChildren.indices) batteryChildren[i].hide()
        for (i in signals.indices) signals[i].hide()
    }

    fun stockChanged(stock: View, restoreBeforeNative: Boolean) {
        var index = signals.size - 1
        while (index >= 0) {
            val previous = signals[index]
            if (previous.view !== stock && !previous.view.isAttachedToWindow) {
                previous.restore()
                signals.removeAt(index)
            }
            index--
        }
        for (i in signals.indices) {
            val original = signals[i]
            if (original.view !== stock) continue
            if (restoreBeforeNative) {
                original.restore()
                return
            }
            val wifi = abi.wifiView.isInstance(stock)
            val state = (if (wifi) abi.wifiViewState else abi.mobileViewState).get(stock)
            if (state == null || !(if (wifi) abi.wifiVisible else abi.mobileVisible).getBoolean(state)) {
                original.visibility.observeNativeGone()
            }
            if (active) original.hide() else original.visibility.observe(stock.visibility)
            return
        }
        // Native icon groups may be replaced while the root stays attached.
        if (signals.size < 8 && stock.isAttachedToWindow) {
            val original = Original(stock)
            signals.add(original)
            if (active) original.hide()
        } else if (signals.size >= 8) {
            supported = false
            restore()
        }
    }

    fun restore() {
        active = false
        view.stopTransition()
        restoreBattery()
        for (i in signals.indices) signals[i].restore()
        view.visibility = View.GONE
    }

    fun release() {
        restore()
        signals.clear()
        batteryChildren.clear()
        view.binding = null
        owner.removeView(view)
    }
}
