package tv.withaibuild.customiuizer.subs

import android.annotation.SuppressLint
import android.bluetooth.BluetoothAdapter
import android.bluetooth.BluetoothDevice
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.os.Bundle
import android.util.Pair
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.runtime.getValue
import androidx.compose.runtime.setValue
import androidx.compose.runtime.mutableStateOf
import tv.withaibuild.customiuizer.ui.NetworkSelectionRow
import tv.withaibuild.customiuizer.ui.SettingsNetworkList
import tv.withaibuild.customiuizer.ui.settingsView
import android.widget.Toast
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import tv.withaibuild.customiuizer.R
import tv.withaibuild.customiuizer.SubFragment
import tv.withaibuild.customiuizer.mods.GlobalActions
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.mods.utils.FatalErrors

class BTList : SubFragment() {

    private val fetchInterval = 15 * 1000
    private var key: String? = null
    private var rows by mutableStateOf<List<NetworkSelectionRow>>(emptyList())
    private var loading by mutableStateOf(false)
    private val btList = mutableListOf<Pair<String, String>>()
    private var addresses = LinkedHashSet<String>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var fetchJob: Job? = null

    private val devicesReceiver = object : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            if (!ModuleHelper.isTrustedBroadcast(this, "com.android.systemui", rejectionResultCode = GlobalActions.ACTION_FAILED)) return
            val deviceList = intent.getParcelableArrayListExtra<BluetoothDevice>("device_list")
            btList.clear()
            deviceList?.forEach { btList.add(Pair(it.address, it.name)) }
            refreshRows()
            updateProgressBar()
            if (isOrderedBroadcast) setResultCode(GlobalActions.ACTION_HANDLED)
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        key = requireArguments().getString("key")
        addresses = LinkedHashSet(AppHelper.getStringSetOfAppPrefs(key, LinkedHashSet()))

        if (BluetoothAdapter.getDefaultAdapter()?.isEnabled == false) {
            Toast.makeText(requireActivity(), R.string.request_bt, Toast.LENGTH_SHORT).show()
        }
        updateProgressBar()
    }

    private fun fetchCachedDevices() {
        val intent = Intent(GlobalActions.ACTION_PREFIX + "FetchCachedDevices")
        intent.setPackage("com.android.systemui")
        ModuleHelper.sendBroadcastWithIdentity(getValidContext(), intent)
    }

    fun updateProgressBar() {
        loading = BluetoothAdapter.getDefaultAdapter()?.isEnabled == true && btList.isEmpty()
        refreshRows()
    }

    private fun registerReceivers() {
        unregisterReceivers()
        getValidContext().registerReceiver(
            devicesReceiver,
            IntentFilter(GlobalActions.EVENT_PREFIX + "CACHEDDEVICESUPDATE"),
            Context.RECEIVER_EXPORTED
        )
        fetchJob?.cancel()
        fetchJob = scope.launch {
            delay(1000L)
            while (isActive) {
                fetchCachedDevices()
                delay(fetchInterval.toLong())
            }
        }
    }

    private fun unregisterReceivers() {
        try {
            fetchJob?.cancel()
            fetchJob = null
            getValidContext().unregisterReceiver(devicesReceiver)
        } catch (t: Throwable) {
            FatalErrors.unwrapAndRethrowIfFatal(t)
        }
    }

    override fun onDestroy() {
        unregisterReceivers()
        scope.cancel()
        super.onDestroy()
    }

    override fun onPause() {
        unregisterReceivers()
        super.onPause()
    }

    override fun onResume() {
        super.onResume()
        registerReceivers()
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View = settingsView {
        SettingsNetworkList(getString(R.string.bt_fetch_devices_title),
            getString(R.string.bt_fetch_devices_summ), { btList.clear(); updateProgressBar(); fetchCachedDevices() },
            getString(R.string.bt_category1), getString(R.string.bt_category2), rows, loading, ::toggleNetwork)
    }

    @SuppressLint("MissingPermission")
    private fun refreshRows() {
        val current = try { BluetoothAdapter.getDefaultAdapter()?.bondedDevices?.map { it.address }?.toSet() ?: emptySet() } catch (_: SecurityException) { emptySet() }
        val selectedRows = addresses.map {
            val parts = it.split("|", limit = 2)
            NetworkSelectionRow(parts[0], parts.getOrElse(1) { "" }, selected = true, current = parts[0] in current)
        }
        val known = selectedRows.mapTo(HashSet()) { it.id }
        rows = selectedRows + btList.distinctBy { it.first }.filter { it.first !in known }.map {
            NetworkSelectionRow(it.first, it.second.orEmpty(), current = it.first in current)
        }
    }

    private fun toggleNetwork(row: NetworkSelectionRow) {
        addresses = LinkedHashSet(AppHelper.getStringSetOfAppPrefs(key, LinkedHashSet()))
        if (row.selected) AppHelper.removeStringPair(addresses, row.id)
        else AppHelper.addStringPair(addresses, row.id, row.name)
        AppHelper.appPrefs?.edit()?.putStringSet(key, addresses.takeIf { it.isNotEmpty() })?.apply()
        refreshRows()
    }
}
