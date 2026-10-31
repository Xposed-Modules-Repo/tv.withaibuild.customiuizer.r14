package tv.withaibuild.customiuizer.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.semantics.heading
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.painterResource
import tv.withaibuild.customiuizer.R

@Composable
fun SettingsSection(title: String) {
    Text(title, Modifier.fillMaxWidth().padding(start = 16.dp, top = 20.dp, bottom = 8.dp)
        .semantics { heading() }, style = MaterialTheme.typography.labelLarge,
        color = MaterialTheme.colorScheme.onSurfaceVariant)
}

@Composable
fun SettingsActionRow(title: String, summary: String = "", value: String = "", enabled: Boolean = true,
    tag: String = title, onClick: (() -> Unit)? = null,
) {
    val modifier = Modifier.fillMaxWidth().testTag(tag).let {
        if (onClick == null) it else it.clickable(enabled = enabled, onClick = onClick)
    }
    Surface(modifier) {
        Row(Modifier.padding(16.dp).alpha(if (enabled) 1f else .45f),
            verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            Column(Modifier.weight(1f)) {
                Text(title, style = MaterialTheme.typography.bodyLarge)
                if (summary.isNotEmpty()) Text(summary, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
                if (value.isNotEmpty()) Text(value, style = MaterialTheme.typography.bodyMedium,
                    color = MaterialTheme.colorScheme.onSurfaceVariant)
            }
            if (onClick != null) SettingsChevron()
        }
    }
}

@Composable
internal fun SettingsChevron() {
    Icon(painterResource(R.drawable.ic_settings_chevron), null, Modifier.size(24.dp),
        tint = MaterialTheme.colorScheme.onSurfaceVariant)
}
