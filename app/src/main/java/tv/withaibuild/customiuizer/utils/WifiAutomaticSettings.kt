package tv.withaibuild.customiuizer.utils

import org.json.JSONArray
import org.json.JSONObject
import java.util.Locale

/** Bounded preference contract; also usable by the settings process without the Hook API. */
internal object WifiAutomaticSettings {
    const val PREFIX = "system_wifiauto_"
    const val REFRESH = "tv.withaibuild.customiuizer.mods.action.WifiAutomaticRefresh"
    const val MAX_RULES = 32
    const val KEEP = -1
    private val mac = Regex("[0-9a-f]{2}(:[0-9a-f]{2}){5}")

    data class Identity(val ssid: String, val bssid: String?)
    data class Rule(val id: String, val name: String, val ssid: String, val bssid: String = "",
        val enabled: Boolean = true, val priority: Int = 0, val dnd: Int = KEEP, val ringer: Int = KEEP,
        val rotation: Int = KEEP, val vpn: Int = KEEP) {
        val valid: Boolean get() = id.isNotBlank() && id.length <= 64 && name.isNotBlank() && name.length <= 80 &&
            validSsid(ssid) && (bssid.isEmpty() || validBssid(bssid)) && priority in 0..999 &&
            dnd in -1..4 && dnd != 0 && ringer in -1..2 && rotation in -1..1 && vpn in -1..1
        val manages: Boolean get() = dnd != KEEP || ringer != KEEP || rotation != KEEP || vpn != KEEP
    }

    data class Options(val enabled: Boolean, val rules: List<Rule>) {
        // Equal priority: exact AP first, then stable list order. Prepared only on configuration changes.
        private val ordered = rules.filter { it.enabled && it.valid && it.manages }
            .sortedWith(compareByDescending<Rule> { it.priority }.thenByDescending { it.bssid.isNotEmpty() })
        val active get() = enabled && ordered.isNotEmpty()
        val audio get() = ordered.any { it.dnd != KEEP || it.ringer != KEEP }
        val rotation get() = ordered.any { it.rotation != KEEP }
        val vpn get() = ordered.any { it.vpn != KEEP }
        fun match(identity: Identity): Rule? {
            if (!enabled) return null
            for (rule in ordered) if (rule.ssid == identity.ssid && (rule.bssid.isEmpty() || rule.bssid == identity.bssid)) return rule
            return null
        }
    }

    fun read(prefs: PrefMap): Options {
        val values = prefs.getAll()
        return Options(values[PREFIX + "enabled"] == true, decode(values[PREFIX + "rules"] as? String ?: ""))
    }

    fun validSsid(value: String): Boolean = value.isNotEmpty() && value != "<unknown ssid>" && value.toByteArray(Charsets.UTF_8).size <= 32
    fun validBssid(value: String): Boolean = mac.matches(value) && value != "02:00:00:00:00:00" && value != "00:00:00:00:00:00"
    fun identity(rawSsid: String?, rawBssid: String?): Identity? {
        val ssid = rawSsid?.let { if (it.length >= 2 && it.first() == '"' && it.last() == '"') it.substring(1, it.length - 1) else it } ?: return null
        if (!validSsid(ssid)) return null
        val bssid = rawBssid?.lowercase(Locale.ROOT)?.takeIf(::validBssid)
        return Identity(ssid, bssid)
    }

    fun decode(text: String): List<Rule> {
        if (text.isBlank() || text.length > 65536) return emptyList()
        return try {
            val array = JSONArray(text)
            if (array.length() > MAX_RULES) return emptyList()
            val rules = ArrayList<Rule>(array.length())
            val ids = HashSet<String>()
            repeat(array.length()) { index ->
                val value = array.getJSONObject(index)
                val rule = Rule(value.getString("id"), value.getString("name"), value.getString("ssid"),
                    value.optString("bssid").lowercase(Locale.ROOT), value.optBoolean("enabled", true), value.optInt("priority", 0),
                    value.optInt("dnd", KEEP), value.optInt("ringer", KEEP), value.optInt("rotation", KEEP), value.optInt("vpn", KEEP))
                if (!rule.valid || !ids.add(rule.id)) return emptyList()
                rules.add(rule)
            }
            rules
        } catch (_: org.json.JSONException) { emptyList() }
    }

    fun encode(rules: List<Rule>): String {
        require(rules.size <= MAX_RULES && rules.all { it.valid } && rules.map { it.id }.toSet().size == rules.size)
        return JSONArray().apply { rules.forEach { rule -> put(JSONObject().apply {
            put("id", rule.id); put("name", rule.name); put("ssid", rule.ssid); put("bssid", rule.bssid)
            put("enabled", rule.enabled); put("priority", rule.priority); put("dnd", rule.dnd)
            put("ringer", rule.ringer); put("rotation", rule.rotation); put("vpn", rule.vpn)
        }) } }.toString()
    }
}
