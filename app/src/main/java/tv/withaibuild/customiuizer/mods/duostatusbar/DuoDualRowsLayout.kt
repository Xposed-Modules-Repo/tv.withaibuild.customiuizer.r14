package tv.withaibuild.customiuizer.mods.duostatusbar

import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers

/** The combined icon gets its own column, instead of being squeezed into half a row. */
internal class DuoDualRowsLayout private constructor(
    private val branch: View,
    private val group: LinearLayout,
    private val first: LinearLayout,
    private val second: LinearLayout,
) {
    private val originalIndex = first.indexOfChild(branch)
    private val originalParams = branch.layoutParams
    private val rows = LinearLayout(branch.context).apply { orientation = LinearLayout.VERTICAL }
    private var applied = false

    fun apply() {
        if (applied || branch.parent !== first || group.childCount != 2 ||
            group.getChildAt(0) !== first || group.getChildAt(1) !== second) return
        // Mark before the first mutation, so a partial ordinary failure can still roll back.
        applied = true
        first.removeView(branch)
        group.removeView(first)
        group.removeView(second)
        rows.addView(first)
        rows.addView(second)
        group.orientation = LinearLayout.HORIZONTAL
        group.addView(rows, LinearLayout.LayoutParams(0, ViewGroup.LayoutParams.MATCH_PARENT, 1f))
        val params = LinearLayout.LayoutParams(originalParams).apply {
            width = ViewGroup.LayoutParams.WRAP_CONTENT
            height = ViewGroup.LayoutParams.MATCH_PARENT
            weight = 0f
        }
        group.addView(branch, params)
    }

    fun restore() {
        if (!applied) return
        applied = false
        if (branch.parent === group) group.removeView(branch)
        if (first.parent === rows) rows.removeView(first)
        if (second.parent === rows) rows.removeView(second)
        if (rows.parent === group) group.removeView(rows)
        group.orientation = LinearLayout.VERTICAL
        if (first.parent == null) group.addView(first, 0)
        if (second.parent == null) group.addView(second, minOf(1, group.childCount))
        if (branch.parent == null) first.addView(branch, originalIndex.coerceIn(0, first.childCount), originalParams)
    }

    companion object {
        /** Inspect the known custom layout once; unsupported layouts retain their native row. */
        fun prepare(owner: ViewGroup, root: ViewGroup, abi: DuoAbi): DuoDualRowsLayout? {
            if (!abi.statusBar.isInstance(root) ||
                XposedHelpers.getAdditionalInstanceField(root, "dualRowsLayoutAdded") != true) return null
            val group = XposedHelpers.getAdditionalInstanceField(root, "rightLayout") as? LinearLayout ?: return null
            if (group.orientation != LinearLayout.VERTICAL || group.childCount != 2) return null
            val first = group.getChildAt(0) as? LinearLayout ?: return null
            val second = group.getChildAt(1) as? LinearLayout ?: return null
            // Keep the native battery container together: it owns privacy visibility and tint.
            // HyperOS can nest the meter beneath that container instead of directly in the row.
            var branch: View = owner
            var depth = 0
            while (branch.parent !== first && depth++ < 8) {
                branch = branch.parent as? ViewGroup ?: return null
            }
            if (branch.parent !== first || branch.layoutParams == null) return null
            return DuoDualRowsLayout(branch, group, first, second)
        }
    }
}
