package tv.withaibuild.customiuizer.mods.duostatusbar

import android.os.Handler
import android.os.Looper
import android.telephony.SubscriptionInfo
import android.util.SparseArray
import android.view.View
import android.view.ViewGroup
import io.github.libxposed.api.XposedInterface
import java.lang.ref.WeakReference
import java.lang.reflect.Method
import java.util.concurrent.atomic.AtomicBoolean
import tv.withaibuild.customiuizer.mods.utils.CallbackGuard
import tv.withaibuild.customiuizer.mods.utils.FatalErrors
import tv.withaibuild.customiuizer.mods.utils.FeatureInstallResult
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.AfterHookCallback
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.BeforeHookCallback
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.CustomMethodUnhooker
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers

/** No receiver, observer, native runtime, service, polling, or private changes to the stock state. */
internal class DuoStatusBarHooks(private val abi: DuoAbi, private val config: DuoConfig) {
    private val network = DuoNetworkState()
    private val handler = Handler(Looper.getMainLooper())
    private val queued = AtomicBoolean(false)
    private val bindings = arrayOfNulls<WeakReference<DuoBinding>>(4)
    @Volatile private var failed = false
    @Volatile private var battery = -1
    @Volatile private var bindingCount = 0
    private val refresh = Runnable {
        CallbackGuard.guarded {
            queued.set(false)
            protect {
                for (reference in bindings) {
                    val binding = reference?.get() ?: continue
                    if (failed) binding.restore()
                    else reconcile(binding)
                }
            }
        }
    }

    private inline fun protect(block: () -> Unit) {
        try { block() } catch (t: Throwable) {
            FatalErrors.unwrapAndRethrowIfFatal(t)
            fail(t)
        }
    }

    private fun fail(t: Throwable) {
        if (failed) return
        failed = true
        XposedHelpers.log(t)
        if (Looper.myLooper() == handler.looper) {
            for (reference in bindings) CallbackGuard.guarded { reference?.get()?.restore() }
        } else queueRefresh()
    }

    private fun queueRefresh() {
        if (bindingCount == 0) return
        if (queued.compareAndSet(false, true) && !handler.post(refresh)) queued.set(false)
    }

    private fun reconcile(binding: DuoBinding) {
        val bits = battery
        binding.reconcile(if (bits < 0) -1 else bits and 127, bits and 128 != 0,
            bits and 256 != 0, network.snapshot)
    }

    private fun readBattery(controller: Any) {
        val rawLevel = abi.batteryLevel.getInt(controller)
        val next = if (rawLevel !in 0..100) -1 else rawLevel or
            (if (abi.charging.getBoolean(controller)) 128 else 0) or
            (if (abi.saver.getBoolean(controller)) 256 else 0)
        if (next == battery) return
        battery = next
        queueRefresh()
    }

    private fun readWifi(controller: Any) {
        val state = abi.wifiState.get(controller) ?: return
        if (network.wifi(abi.wifiEnabled.getBoolean(state), abi.wifiConnected.getBoolean(state),
                abi.wifiLevel.getInt(state), abi.wifiInternet.getInt(state) == 1)) queueRefresh()
    }

    private fun readMobile(controller: Any) {
        val state = abi.mobileState.get(controller) ?: return
        val info = abi.subscription.get(controller) as SubscriptionInfo
        if (network.mobile(info.simSlotIndex, info.subscriptionId, abi.mobileLevel.getInt(state),
                abi.mobileConnected.getBoolean(state), abi.mobileDataSim.getBoolean(state))) queueRefresh()
    }

    private fun readSubscriptions(subscriptions: List<*>) {
        var first = -1
        var second = -1
        var supported = subscriptions.size <= 2
        for (item in subscriptions) {
            val info = item as SubscriptionInfo
            when (info.simSlotIndex) {
                0 -> { if (first >= 0) supported = false; first = info.subscriptionId }
                1 -> { if (second >= 0) supported = false; second = info.subscriptionId }
                else -> supported = false
            }
        }
        if (network.subscriptions(first, second, supported)) queueRefresh()
    }

    /** Called only on view attachment; Dependency/class discovery stays out of recurring callbacks. */
    private fun seed() {
        val loader = abi.batteryView.classLoader
        val battery = ModuleHelper.getDepInstance(loader, "com.android.systemui.statusbar.policy.BatteryController")
            ?: error("Duo: BatteryController unavailable")
        val controller = ModuleHelper.getDepInstance(loader, "com.android.systemui.statusbar.connectivity.NetworkController")
            ?: error("Duo: NetworkController unavailable")
        require(abi.batteryController.isInstance(battery) && abi.networkController.isInstance(controller))
        readBattery(battery)
        readWifi(abi.networkWifi.get(controller))
        val mobiles = abi.networkMobile.get(controller) as SparseArray<*>
        val subs = ArrayList<SubscriptionInfo>(2)
        for (i in 0 until mobiles.size()) subs.add(abi.subscription.get(mobiles.valueAt(i)) as SubscriptionInfo)
        readSubscriptions(subs)
        for (i in 0 until mobiles.size()) readMobile(mobiles.valueAt(i))
        network.airplane(abi.airplane.getBoolean(controller))
    }

    private fun root(view: View): ViewGroup? {
        var current = view.parent
        while (current is ViewGroup) {
            if (abi.statusBar.isInstance(current) || abi.keyguard.isInstance(current)) return current
            current = current.parent
        }
        return null
    }

    private fun child(owner: ViewGroup): DuoStatusBarView? {
        for (i in 0 until owner.childCount) {
            val candidate = owner.getChildAt(i)
            if (candidate is DuoStatusBarView) return candidate
        }
        return null
    }

    private fun attach(owner: ViewGroup) {
        if (failed || child(owner) != null) return
        val root = root(owner) ?: return // Control Center and unrelated battery views stay native.
        val slot = bindings.indexOfFirst { it?.get() == null }
        if (slot < 0) return // bounded multi-display capacity; excess hosts stay native
        seed()
        val view = DuoStatusBarView(owner.context, config, ::fail)
        view.visibility = View.GONE
        val binding = DuoBinding(owner, root, view, abi)
        view.binding = binding
        bindings[slot] = WeakReference(binding)
        bindingCount++
        val size = (config.sizeDp * owner.resources.displayMetrics.density + 0.5f).toInt()
        owner.addView(view, ViewGroup.LayoutParams(size, ViewGroup.LayoutParams.MATCH_PARENT))
        reconcile(binding)
    }

    private fun detach(owner: ViewGroup) {
        val binding = child(owner)?.binding ?: return
        for (i in bindings.indices) if (bindings[i]?.get() === binding) bindings[i] = null
        bindingCount = bindings.count { it?.get() != null }
        binding.release()
        if (bindings.all { it?.get() == null }) {
            handler.removeCallbacks(refresh)
            queued.set(false)
        }
    }

    private fun stockChanged(stock: View, restore: Boolean) {
        if (failed) return
        val root = root(stock) ?: return
        for (reference in bindings) {
            val binding = reference?.get() ?: continue
            if (binding.root === root) { binding.stockChanged(stock, restore); return }
        }
    }

    fun install(): FeatureInstallResult {
        val installed = ArrayList<CustomMethodUnhooker>(16)
        fun hook(method: Method, callback: MethodHook) {
            installed.add(ModuleHelper.hookMethod(method, callback) ?: error("Duo: hook failed: ${method.name}"))
        }
        fun after(method: Method, action: (Any) -> Unit) {
            hook(method, object : MethodHook(XposedInterface.PRIORITY_HIGHEST) {
                override fun after(param: AfterHookCallback) {
                    if (failed || param.getThrowable() != null) return
                    protect { param.getThisObject()?.let(action) }
                }
            })
        }
        try {
            after(abi.attach) { attach(it as ViewGroup) }
            hook(abi.detach, object : MethodHook() {
                override fun after(param: AfterHookCallback) {
                    protect { (param.getThisObject() as? ViewGroup)?.let { detach(it) } }
                }
            })
            hook(abi.updateBattery, object : MethodHook(XposedInterface.PRIORITY_HIGHEST) {
                override fun before(param: BeforeHookCallback) {
                    if (!failed) protect { child(param.getThisObject() as ViewGroup)?.binding?.restoreBattery() }
                }
                override fun after(param: AfterHookCallback) {
                    if (param.getThrowable() != null) {
                        protect { child(param.getThisObject() as ViewGroup)?.binding?.restore() }
                        return
                    }
                    if (!failed) protect {
                        child(param.getThisObject() as ViewGroup)?.binding?.let(::reconcile)
                    }
                }
            })
            after(abi.darkBattery) { child(it as ViewGroup)?.binding?.let(::reconcile) }
            after(abi.batteryChanged, ::readBattery)
            after(abi.saverChanged, ::readBattery)
            after(abi.wifiChanged, ::readWifi)
            after(abi.mobileChanged, ::readMobile)
            hook(abi.airplaneChanged, object : MethodHook() {
                override fun before(param: BeforeHookCallback) {
                    if (!failed) protect {
                        val icon = param.getArg(0) ?: return@protect
                        if (network.airplane(abi.airplaneVisible.getBoolean(icon))) queueRefresh()
                    }
                }
            })
            hook(abi.subscriptionsChanged, object : MethodHook() {
                override fun before(param: BeforeHookCallback) {
                    if (!failed) protect { (param.getArg(0) as? List<*>)?.let(::readSubscriptions) }
                }
            })
            after(abi.applyWifi) { stockChanged(it as View, false) }
            after(abi.applyMobile) { stockChanged(it as View, false) }
            after(abi.updateMobile) { stockChanged(it as View, false) }
            val visibilityHook = object : MethodHook(XposedInterface.PRIORITY_HIGHEST) {
                override fun before(param: BeforeHookCallback) {
                    if (!failed) protect { stockChanged(param.getThisObject() as View, true) }
                }
                override fun after(param: AfterHookCallback) {
                    if (!failed) protect { stockChanged(param.getThisObject() as View, false) }
                }
            }
            hook(abi.wifiVisibleState, visibilityHook)
            hook(abi.mobileVisibleState, visibilityHook)
            XposedHelpers.log("Duo status bar hooks installed")
            return FeatureInstallResult.INSTALLED
        } catch (t: Throwable) {
            FatalErrors.unwrapAndRethrowIfFatal(t)
            failed = true
            for (unhooker in installed.asReversed()) CallbackGuard.guarded { unhooker.unhook() }
            XposedHelpers.log(t)
            return FeatureInstallResult.FAILED_PERMANENT
        }
    }

    companion object {
        fun install(loader: ClassLoader, config: DuoConfig): FeatureInstallResult {
            val abi = DuoAbi.resolve(loader) ?: return FeatureInstallResult.FAILED_PERMANENT
            return DuoStatusBarHooks(abi, config).install()
        }
    }
}
