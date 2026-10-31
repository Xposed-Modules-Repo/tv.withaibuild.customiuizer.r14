package tv.withaibuild.customiuizer.tasker

import android.app.Activity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.foundation.selection.selectable
import androidx.compose.foundation.text.selection.SelectionContainer
import androidx.compose.material3.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.unit.dp
import tv.withaibuild.customiuizer.ui.SettingsTheme
import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import tv.withaibuild.customiuizer.R

class UnlockSettings : AppCompatActivity() {

    private var hostSummary by mutableStateOf("")
    private var selectedState by mutableIntStateOf(-1)
    private val provider = UnlockTokenProvider()
    private var hostInfo: UnlockTokenProvider.HostInfo? = null
    private var bindingStatus: UnlockTokenProvider.HostBindingStatus? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent { SettingsTheme {
            Surface(Modifier.fillMaxSize(), color = MaterialTheme.colorScheme.background) {
                Column(Modifier.fillMaxWidth().verticalScroll(rememberScrollState()).padding(24.dp),
                    verticalArrangement = Arrangement.spacedBy(16.dp)) {
                    Text(stringResource(R.string.system_noscreenlock_scr_title), style = MaterialTheme.typography.headlineSmall)
                    SelectionContainer { Text(hostSummary, Modifier.testTag("unlock_host_info")) }
                    for ((value, label) in listOf(0 to R.string.system_noscreenlock_force_locked,
                        1 to R.string.system_noscreenlock_force_unlocked, -1 to R.string.system_noscreenlock_force_off)) {
                        Row(Modifier.fillMaxWidth().selectable(selectedState == value, role = Role.RadioButton,
                            onClick = { selectedState = value }).padding(vertical = 12.dp), verticalAlignment = Alignment.CenterVertically) {
                            RadioButton(selectedState == value, null)
                            Text(stringResource(label), Modifier.padding(start = 12.dp))
                        }
                    }
                    Row(Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                        TextButton(onClick = { setResult(Activity.RESULT_CANCELED); finish() }) { Text(stringResource(android.R.string.cancel)) }
                        Button(onClick = ::onConfirm, modifier = Modifier.testTag("unlock_confirm")) { Text(stringResource(android.R.string.ok)) }
                    }
                }
            }
        } }
        val callingPackage = callingPackage

        if (callingPackage == null) {
            hostSummary = getString(R.string.unlock_host_missing)
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        this.hostInfo = provider.getHostInfo(this, callingPackage)
        val info = this.hostInfo
        if (info == null) {
            hostSummary = getString(R.string.unlock_host_untrusted, callingPackage)
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        // Read-only: do not create or update any binding here.
        val status = provider.prepare(this, info)
        this.bindingStatus = status

        when (status) {
            is UnlockTokenProvider.HostBindingStatus.NewHost -> {
                hostSummary = getString(
                    R.string.unlock_host_first,
                    formatHostSummary(info)
                )
            }
            is UnlockTokenProvider.HostBindingStatus.Reuse -> {
                hostSummary = getString(
                    R.string.unlock_host_reuse,
                    formatHostSummary(info)
                )
            }
            UnlockTokenProvider.HostBindingStatus.Mismatch -> {
                hostSummary = getString(
                    R.string.unlock_host_cert_mismatch,
                    info.applicationLabel,
                    info.packageName,
                    info.currentFingerprint ?: "?"
                )
                setResult(Activity.RESULT_CANCELED)
                finish()
                return
            }
        }

        val bundle = intent.getBundleExtra(Constants.EXTRA_BUNDLE)
        selectedState = when (val value = savedInstanceState?.getInt("selected_lock_state")
            ?: bundle?.getInt("system_noscreenlock_force", -1) ?: -1) {
            0, 1 -> value
            else -> -1
        }

        onBackPressedDispatcher.addCallback(this, object : androidx.activity.OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                setResult(Activity.RESULT_CANCELED)
                finish()
            }
        })

        if (bundle == null || !bundle.containsKey(UnlockTokenProvider.BUNDLE_KEY_TOKEN)) {
            Toast.makeText(this, R.string.unlock_token_missing, Toast.LENGTH_LONG).show()
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        outState.putInt("selected_lock_state", selectedState)
        super.onSaveInstanceState(outState)
    }

    private fun formatHostSummary(info: UnlockTokenProvider.HostInfo): String {
        return getString(
            R.string.unlock_host_summary,
            info.applicationLabel,
            info.packageName,
            info.currentFingerprint ?: "?",
            info.historySummary
        )
    }

    private fun onConfirm() {
        val info = hostInfo ?: run {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        // bind() is the only place that may write token or certificate state.
        val hostToken = provider.bind(this, info)
        if (hostToken == null) {
            setResult(Activity.RESULT_CANCELED)
            finish()
            return
        }

        val lockState = selectedState

        val stringRes = when (lockState) {
            1 -> R.string.system_noscreenlock_force_unlocked
            0 -> R.string.system_noscreenlock_force_locked
            else -> R.string.system_noscreenlock_force_off
        }

        val resultIntent = Intent().apply {
            putExtra(Constants.EXTRA_STRING_BLURB, getString(stringRes))
            val outBundle = Bundle().apply {
                putInt("system_noscreenlock_force", lockState)
                putString(UnlockTokenProvider.BUNDLE_KEY_TOKEN, hostToken.token)
                putString(UnlockTokenProvider.BUNDLE_KEY_HOST_PACKAGE, hostToken.hostPackage)
            }
            putExtra(Constants.EXTRA_BUNDLE, outBundle)
        }
        setResult(Activity.RESULT_OK, resultIntent)
        finish()
    }

}
