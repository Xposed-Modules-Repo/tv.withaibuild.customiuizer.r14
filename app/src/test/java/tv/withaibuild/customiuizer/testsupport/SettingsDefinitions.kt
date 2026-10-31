package tv.withaibuild.customiuizer.testsupport

import org.json.JSONObject

/** Test-only schema view. Production uses generated typed Kotlin, never runtime JSON parsing. */
fun settingsNodes(name: String): List<JSONObject> {
    val root = JSONObject(repositorySource("app/settings/$name.json"))
    return buildList {
        fun visit(node: JSONObject) {
            add(node)
            val children = node.optJSONArray("children") ?: return
            for (index in 0 until children.length()) visit(children.getJSONObject(index))
        }
        visit(root)
    }
}
