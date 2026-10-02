package tv.withaibuild.customiuizer.mods.duostatusbar

import java.lang.reflect.Field
import java.lang.reflect.Method
import tv.withaibuild.customiuizer.mods.utils.FatalErrors
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers

/** Per-install/ClassLoader metadata only. A missing required member leaves every native icon alone. */
internal class DuoAbi(private val loader: ClassLoader) {
    private fun cls(name: String): Class<*> = Class.forName("com.android.systemui.$name", false, loader)
    private fun field(type: Class<*>, name: String, valueType: Class<*>? = null): Field {
        val result = XposedHelpers.findField(type, name)
        require(valueType == null || result.type == valueType) { "Duo: incompatible $name" }
        return result
    }
    private fun method(type: Class<*>, name: String, count: Int): Method {
        var current: Class<*>? = type
        while (current != null && current.name.startsWith("com.android.systemui.")) {
            val found = current.declaredMethods.firstOrNull {
                it.name == name && it.parameterCount == count && !it.isBridge
            }
            if (found != null) return found.apply { isAccessible = true }
            current = current.superclass
        }
        error("Duo: missing ${type.name}.$name/$count")
    }

    val batteryView = cls("statusbar.views.MiuiBatteryMeterView")
    val statusBar = cls("statusbar.phone.MiuiPhoneStatusBarView")
    val keyguard = cls("statusbar.phone.KeyguardStatusBarView")
    val wifiView = cls("statusbar.StatusBarWifiView")
    val mobileView = cls("statusbar.StatusBarMobileView")
    val attach = method(batteryView, "onAttachedToWindow", 0)
    val detach = method(batteryView, "onDetachedFromWindow", 0)
    val updateBattery = method(batteryView, "updateAll", 0)
    val darkBattery = method(batteryView, "onDarkChanged", 6)
    val percentView = field(batteryView, "mBatteryPercentView")

    val batteryController = cls("statusbar.policy.MiuiBatteryControllerImpl")
    val batteryLevel = field(batteryController, "mLevel", Int::class.javaPrimitiveType)
    val charging = field(batteryController, "mCharging", Boolean::class.javaPrimitiveType)
    val saver = field(batteryController, "mPowerSave", Boolean::class.javaPrimitiveType)
    val batteryChanged = method(batteryController, "fireBatteryLevelChanged", 0)
    val saverChanged = method(batteryController, "firePowerSaveChanged", 0)

    val wifiController = cls("statusbar.connectivity.WifiSignalController")
    val mobileController = cls("statusbar.connectivity.MobileSignalController")
    val wifiChanged = method(wifiController, "notifyListeners", 1)
    val mobileChanged = method(mobileController, "notifyListeners", 1)
    val wifiState = field(wifiController, "mCurrentState")
    val mobileState = field(mobileController, "mCurrentState")
    private val wifiStateType = method(wifiController, "cleanState", 0).returnType
    private val mobileStateType = method(mobileController, "cleanState", 0).returnType
    val wifiLevel = field(wifiStateType, "level", Int::class.javaPrimitiveType)
    val wifiEnabled = field(wifiStateType, "enabled", Boolean::class.javaPrimitiveType)
    val wifiConnected = field(wifiStateType, "connected", Boolean::class.javaPrimitiveType)
    val wifiInternet = field(wifiStateType, "inetCondition", Int::class.javaPrimitiveType)
    val mobileLevel = field(mobileStateType, "level", Int::class.javaPrimitiveType)
    val mobileConnected = field(mobileStateType, "connected", Boolean::class.javaPrimitiveType)
    val mobileDataSim = field(mobileStateType, "dataSim", Boolean::class.javaPrimitiveType)
    val subscription = field(mobileController, "mSubscriptionInfo")

    val networkController = cls("statusbar.connectivity.NetworkControllerImpl")
    val networkWifi = field(networkController, "mWifiSignalController")
    val networkMobile = field(networkController, "mMobileSignalControllers")
    val airplane = field(networkController, "mAirplaneMode", Boolean::class.javaPrimitiveType)
    private val callbackHandler = cls("statusbar.connectivity.CallbackHandler")
    val airplaneChanged = method(callbackHandler, "setIsAirplaneMode", 1)
    val subscriptionsChanged = method(callbackHandler, "setSubs", 1)
    val airplaneVisible = field(airplaneChanged.parameterTypes[0], "visible", Boolean::class.javaPrimitiveType)

    val applyWifi = method(wifiView, "applyWifiState", 1)
    val applyMobile = method(mobileView, "applyMobileState", 1)
    val updateMobile = method(mobileView, "updateState", 1)
    val wifiVisibleState = method(wifiView, "setVisibleState", 2)
    val mobileVisibleState = method(mobileView, "setVisibleState", 2)
    val wifiViewState = field(wifiView, "mState")
    val mobileViewState = field(mobileView, "mState")
    val wifiVisible = field(applyWifi.parameterTypes[0], "visible", Boolean::class.javaPrimitiveType)
    val mobileVisible = field(applyMobile.parameterTypes[0], "visible", Boolean::class.javaPrimitiveType)

    companion object {
        fun resolve(loader: ClassLoader): DuoAbi? = try {
            DuoAbi(loader)
        } catch (t: Throwable) {
            FatalErrors.unwrapAndRethrowIfFatal(t)
            XposedHelpers.log("Duo status bar unavailable: ${t.message}")
            null
        }
    }
}
