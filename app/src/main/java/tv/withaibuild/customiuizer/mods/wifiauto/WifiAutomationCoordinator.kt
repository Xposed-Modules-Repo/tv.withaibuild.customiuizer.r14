package tv.withaibuild.customiuizer.mods.wifiauto

import tv.withaibuild.customiuizer.mods.vpn.VpnAutoSwitchCoordinator
import tv.withaibuild.customiuizer.utils.WifiAutomaticSettings.Rule
import tv.withaibuild.customiuizer.utils.VpnAutoSwitchSettings.Target

/** Single-handler state; only ownership is shared. Each native control keeps its own implementation. */
internal class WifiAutomationCoordinator(private val backend: Backend) {
    enum class Property { DND, RINGER, ROTATION }
    interface Backend {
        fun read(property: Property): Int?
        fun write(property: Property, value: Int): Int?
        fun vpn(): VpnAutoSwitchCoordinator.Backend?
        fun vpnBusy(): Boolean
        fun vpnTarget(): Target
    }
    private val leases = Property.entries.associateWith { WifiSettingLease() }
    private val vpn = WifiVpnLease()
    private var selected: Rule? = null
    private var known = false
    private var vpnUsed = false
    val pendingRestoration get() = leases.values.any { it.owned }

    fun network(scene: Rule?, reliable: Boolean) {
        if (known == reliable && selected == scene) return
        known = reliable
        selected = scene
        if (reliable) sync()
    }

    fun sync() {
        if (!known) return
        val scene = selected
        val targets = arrayOf(scene?.dnd?.takeIf { it != -1 }, scene?.ringer?.takeIf { it != -1 }, scene?.rotation?.takeIf { it != -1 })
        // Observe both audio properties before either write, so a native DND side effect isn't mistaken for manual input.
        val current = Property.entries.associateWith { property ->
            if (targets[property.ordinal] != null || leases.getValue(property).tracked) backend.read(property) else null
        }
        for (property in Property.entries) if (leases.getValue(property).tracked) leases.getValue(property).observe(current[property])
        for (property in Property.entries) if (targets[property.ordinal] != null) leases.getValue(property).prepare(current[property], scene?.id)
        var priorWrite = false
        for (property in Property.entries) {
            val lease = leases.getValue(property)
            if (targets[property.ordinal] == null && !lease.tracked) continue
            val value = if (priorWrite) backend.read(property) else current[property]
            var confirmed = false
            val changed = lease.apply(targets[property.ordinal], value, scene?.id) {
                backend.write(property, it).also { result -> confirmed = result == it }
            }
            if (property == Property.DND && confirmed) {
                if (leases.getValue(Property.RINGER).tracked) leases.getValue(Property.RINGER).nativeSideEffect(backend.read(Property.RINGER))
            }
            priorWrite = priorWrite || changed
        }
        if (scene?.vpn?.let { it != -1 } == true) vpnUsed = true
        if (vpnUsed) backend.vpn()?.let { vpn.reconcile(scene?.vpn?.takeIf { it != -1 }, backend.vpnTarget(), backend.vpnBusy(), it) }
        if (scene == null) { leases.values.forEach { it.finishSession() }; vpn.end(); vpnUsed = false }
    }

    fun statesChanged(commandsAllowed: Boolean) {
        if (known && commandsAllowed) sync()
        else for (property in Property.entries) if (leases.getValue(property).tracked)
            leases.getValue(property).observe(backend.read(property))
    }

    fun close(restore: Boolean) {
        if (restore) { known = true; selected = null; sync() }
        if (!restore) leases.values.forEach { it.end() }
        vpn.end(); vpnUsed = false; known = restore && pendingRestoration; selected = null
    }
}
