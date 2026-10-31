package tv.withaibuild.customiuizer

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.Image
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.asImageBitmap
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.*
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import tv.withaibuild.customiuizer.ui.settingsView
import tv.withaibuild.customiuizer.ui.SettingsSearchField
import tv.withaibuild.customiuizer.utils.AppData
import tv.withaibuild.customiuizer.utils.Helpers
import tv.withaibuild.customiuizer.utils.AppIconLoader
import java.util.Locale

data class AppSelectionRow(val app: AppData, val summary: String = "", val selected: Boolean? = null,
    val state: String = "", val enabled: Boolean = true) {
    val id: String = "${app.pkgName}|${app.actName}|${app.user}"
}

/** Shared search/list presentation. Controllers retain ownership of persistence and trusted IPC. */
open class SubFragmentWithSearch : SubFragment() {
    protected var appRows by mutableStateOf<List<AppSelectionRow>>(emptyList())
    protected var loading by mutableStateOf(true)
    private var query by mutableStateOf("")
    protected open fun selectApp(row: AppSelectionRow) = Unit
    protected open fun longSelectApp(row: AppSelectionRow): Boolean = false
    protected open val activityRows = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        query = savedInstanceState?.getString("selector.query").orEmpty()
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putString("selector.query", query)
        super.onSaveInstanceState(outState)
    }

    @OptIn(ExperimentalFoundationApi::class)
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = settingsView {
        val source = appRows
        val filter = query.trim().lowercase(Locale.ROOT)
        val rows = if (filter.isEmpty()) source else {
            val filtered by produceState(source, source, filter) {
                value = withContext(Dispatchers.Default) {
                    filterAppSelectionRows(source, filter, activityRows)
                }
            }
            filtered
        }
        Column(Modifier.fillMaxSize().widthIn(max = 720.dp)) {
            SettingsSearchField(query, { query = it },
                Modifier.fillMaxWidth().padding(horizontal = 12.dp, vertical = 8.dp).testTag("selector.search"))
            if (loading) LinearProgressIndicator(Modifier.fillMaxWidth().testTag("selector.loading"))
            LazyColumn(Modifier.fillMaxSize().testTag("selector.list"), contentPadding = PaddingValues(bottom = 24.dp)) {
                items(rows, key = { it.id }) { row ->
                    Row(Modifier.fillMaxWidth().combinedClickable(enabled = row.enabled,
                        onClick = { selectApp(row) }, onLongClick = { longSelectApp(row) })
                        .semantics { if (row.selected != null) selected = row.selected; if (row.state.isNotEmpty()) stateDescription = row.state }
                        .padding(horizontal = 16.dp, vertical = 12.dp),
                        verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        if (!activityRows) AppSelectionIcon(row.app)
                        Column(Modifier.weight(1f)) {
                            Text(row.app.label, style = MaterialTheme.typography.bodyLarge,
                                color = if (row.enabled) MaterialTheme.colorScheme.onSurface else MaterialTheme.colorScheme.onSurfaceVariant)
                            if (row.summary.isNotEmpty()) Text(row.summary, style = MaterialTheme.typography.bodySmall,
                                color = MaterialTheme.colorScheme.onSurfaceVariant)
                            if (!row.app.enabled) Text(getString(R.string.app_disabled), style = MaterialTheme.typography.labelSmall)
                            if (row.app.user != 0) Text(getString(R.string.a14_secondary_user), style = MaterialTheme.typography.labelSmall)
                        }
                        if (row.selected != null) Checkbox(row.selected, null, enabled = row.enabled)
                        if (row.state.isNotEmpty()) Text(row.state, style = MaterialTheme.typography.labelMedium)
                    }
                    HorizontalDivider(Modifier.padding(start = if (activityRows) 16.dp else 68.dp))
                }
            }
        }
    }
}

@Composable
private fun AppSelectionIcon(app: AppData) {
    val context = LocalContext.current.applicationContext
    val bitmap by produceState(Helpers.memoryCache[app.iconKey], app.iconKey) {
        value = withContext(AppIconLoader.dispatcher) { AppIconLoader.loadIcon(context, app) }
    }
    val image = bitmap
    if (image != null) Image(image.asImageBitmap(), null, Modifier.size(40.dp))
    else Spacer(Modifier.size(40.dp))
}

internal fun filterAppSelectionRows(source: List<AppSelectionRow>, filter: String, activities: Boolean): List<AppSelectionRow> =
    if (filter.isEmpty()) source else source.filter {
        it.app.labelLower.contains(filter) || activities && it.app.actNameLower.contains(filter) ||
            it.app.pkgName.isEmpty() && it.app.actName.isEmpty()
    }
