package tv.withaibuild.customiuizer.mods.appbehavior

import android.content.pm.ActivityInfo
import tv.withaibuild.customiuizer.utils.PrefMap

/** Immutable configuration, prepared only on preference publication. No host objects are retained. */
internal data class AppBehaviorPolicy(
    val rotation: Set<String> = emptySet(),
    val volume: Set<String> = emptySet(),
    val brightness: Set<String> = emptySet(),
    val levels: Map<String, Int> = emptyMap(),
    val blocked: Set<String> = emptySet(),
    val limit: Int = 0,
    val exceptions: Set<String> = emptySet(),
) {
    private val scenes = if (exceptions.isEmpty()) emptyMap() else
        exceptions.groupBy({ it.substringBefore('/') }, { it.substringAfter('/') }).mapValues { it.value.toSet() }

    fun orientation(pkg: String, requested: Int): Int {
        if (pkg !in rotation) return requested
        return when (requested) {
            ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED, ActivityInfo.SCREEN_ORIENTATION_USER -> ActivityInfo.SCREEN_ORIENTATION_SENSOR
            ActivityInfo.SCREEN_ORIENTATION_FULL_USER -> ActivityInfo.SCREEN_ORIENTATION_FULL_SENSOR
            ActivityInfo.SCREEN_ORIENTATION_USER_LANDSCAPE -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_LANDSCAPE
            ActivityInfo.SCREEN_ORIENTATION_USER_PORTRAIT -> ActivityInfo.SCREEN_ORIENTATION_SENSOR_PORTRAIT
            else -> requested // Explicit locks, BEHIND and the native ignore sentinel remain native.
        }
    }

    fun brightness(pkg: String, activity: String, requested: Float): Float {
        if (scenes[pkg]?.contains(activity) == true) return requested
        // Explicit user preference wins; an unset preference follows the system when selected.
        if (pkg in brightness) return levels[pkg]?.let { it / 100f } ?: -1f
        if (pkg !in blocked || requested < 0f || !requested.isFinite()) return requested
        return if (limit == 0) -1f else minOf(requested, limit / 100f)
    }

    companion object {
        const val PREFIX = "system_appbehavior_"
        fun read(prefs: PrefMap, includeVolume: Boolean = true): AppBehaviorPolicy {
            fun apps(name: String) = if (prefs.getBoolean(PREFIX + name))
                prefs.getStringSet(PREFIX + name + "_apps").take(256).toSet() else emptySet()
            return AppBehaviorPolicy(apps("rotation"), if (includeVolume) apps("volume") else emptySet(), apps("brightness"),
                decodeLevels(prefs.getString(PREFIX + "brightness_values", "")), apps("block"),
                prefs.getInt(PREFIX + "block_limit", 0).coerceIn(0, 100),
                prefs.getString(PREFIX + "exceptions", "").take(32768).split(',', '\n').asSequence()
                    .map(String::trim).filter { '/' in it }.take(128).toSet())
        }

        fun decodeLevels(value: String): Map<String, Int> = value.take(32768).split(';').asSequence()
            .take(256).mapNotNull { entry ->
                val at = entry.indexOf('=')
                if (at <= 0) return@mapNotNull null
                val level = entry.substring(at + 1).toIntOrNull()?.takeIf { it in 1..100 } ?: return@mapNotNull null
                entry.substring(0, at) to level
            }.toMap()
    }
}
