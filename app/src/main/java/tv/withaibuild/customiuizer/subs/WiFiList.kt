package tv.withaibuild.customiuizer.subs

import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.location.LocationManager
import android.net.NetworkInfo
import android.net.wifi.ScanResult
import android.net.wifi.WifiManager
import android.os.Bundle
import android.provider.Settings
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
import tv.withaibuild.customiuizer.utils.AppHelper
import tv.withaibuild.customiuizer.mods.utils.FatalErrors

class WiFiList : SubFragment() {

    private val scanInterval = 15 * 1000
    private var key: String? = null
    private var rows by mutableStateOf<List<NetworkSelectionRow>>(emptyList())
    private var loading by mutableStateOf(false)
    private lateinit var wifiManager: WifiManager
    private var wifiList: List<ScanResult> = emptyList()
    private var bssids = LinkedHashSet<String>()

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.Main)
    private var scanJob: Job? = null

    private val wifiReceiver = object : BroadcastReceiver() {
        @Suppress("DEPRECATION")
        @SuppressLint("MissingPermission")
        override fun onReceive(context: Context, intent: Intent) {
            val action = intent.action ?: return
            if (action == WifiManager.SCAN_RESULTS_AVAILABLE_ACTION) {
                wifiList = try {
                    wifiManager.scanResults
                } catch (ignored: SecurityException) {
                    emptyList()
                }
                refreshRows()
                updateProgressBar()
            } else if (action == WifiManager.NETWORK_STATE_CHANGED_ACTION) {
                val netInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO) as? NetworkInfo ?: return
                when (netInfo.detailedState) {
                    NetworkInfo.DetailedState.CONNECTED -> {
                        isWiFiReady()
                        scheduleScan(1000L)
                    }
                    NetworkInfo.DetailedState.DISCONNECTED -> scheduleScan(1000L)
                    else -> {}
                }
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
    }

    override fun onActivityCreated(savedInstanceState: Bundle?) {
        super.onActivityCreated(savedInstanceState)

        key = requireArguments().getString("key")
        bssids = LinkedHashSet(AppHelper.getStringSetOfAppPrefs(key, LinkedHashSet()))

        wifiManager = requireActivity().getSystemService(Context.WIFI_SERVICE) as WifiManager
        isWiFiReady()
        updateProgressBar()
    }

    fun updateProgressBar() {
        loading = wifiManager.isWifiEnabled && isLocationServicesEnabled() && wifiList.isEmpty()
        refreshRows()
    }

    fun isLocationServicesEnabled(): Boolean {
        val locationManager = requireActivity().getSystemService(Context.LOCATION_SERVICE) as? LocationManager
        return try {
            locationManager?.run {
                isProviderEnabled(LocationManager.GPS_PROVIDER) || isProviderEnabled(LocationManager.NETWORK_PROVIDER)
            } ?: false
        } catch (t: Throwable) {
            false
        }
    }

    fun isWiFiReady() {
        if (!wifiManager.isWifiEnabled) {
            Toast.makeText(requireActivity(), R.string.request_wifi, Toast.LENGTH_SHORT).show()
            return
        }
        if (!isLocationServicesEnabled()) {
            Toast.makeText(requireActivity(), R.string.request_location, Toast.LENGTH_LONG).show()
        }
    }

    private fun scheduleScan(initialDelayMs: Long = 0L) {
        scanJob?.cancel()
        scanJob = scope.launch {
            if (initialDelayMs > 0) delay(initialDelayMs)
            while (isActive) {
                try { wifiManager.startScan() } catch (t: Throwable) { FatalErrors.unwrapAndRethrowIfFatal(t) }
                updateProgressBar()
                delay(scanInterval.toLong())
            }
        }
    }

    private fun registerReceivers() {
        unregisterReceivers()
        isWiFiReady()
        val intentFilter = IntentFilter().apply {
            addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION)
            addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION)
        }
        requireActivity().registerReceiver(wifiReceiver, intentFilter, Context.RECEIVER_NOT_EXPORTED)
        scheduleScan(1000L)
    }

    private fun unregisterReceivers() {
        try {
            scanJob?.cancel()
            scanJob = null
            requireActivity().unregisterReceiver(wifiReceiver)
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
        SettingsNetworkList(getString(R.string.wifi_location_title),
            getString(R.string.wifi_location_summ), { startActivity(Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS)) },
            getString(R.string.wifi_category1), getString(R.string.wifi_category2), rows, loading, ::toggleNetwork)
    }

    @SuppressLint("MissingPermission")
    private fun refreshRows() {
        val current = try { wifiManager.connectionInfo?.bssid?.let { setOf(it) } ?: emptySet() } catch (_: SecurityException) { emptySet() }
        val selectedRows = bssids.map {
            val parts = it.split("|", limit = 2)
            NetworkSelectionRow(parts[0], parts.getOrElse(1) { "" }, selected = true, current = parts[0] in current)
        }
        val known = selectedRows.mapTo(HashSet()) { it.id }
        rows = selectedRows + wifiList.map { Pair(it.BSSID, it.SSID) }.distinctBy { it.first }.filter { it.first !in known }.map {
            NetworkSelectionRow(it.first, it.second.orEmpty(), current = it.first in current)
        }
    }

    private fun toggleNetwork(row: NetworkSelectionRow) {
        bssids = LinkedHashSet(AppHelper.getStringSetOfAppPrefs(key, LinkedHashSet()))
        if (row.selected) AppHelper.removeStringPair(bssids, row.id)
        else AppHelper.addStringPair(bssids, row.id, row.name)
        AppHelper.appPrefs?.edit()?.putStringSet(key, bssids.takeIf { it.isNotEmpty() })?.apply()
        refreshRows()
    }
}
