package tv.withaibuild.customiuizer.mods.duostatusbar

import com.android.systemui.statusbar.SignalIcon
import com.android.systemui.statusbar.connectivity.MobileSignalController
import com.android.systemui.statusbar.policy.BatteryControllerImpl
import com.android.systemui.statusbar.policy.MiuiWifiSignalController
import org.junit.Assert.*
import org.junit.Test

class DuoAbiTest {
    private val loader = javaClass.classLoader!!

    @Test fun resolvesXiaomiControllerAndInheritedBatteryMembers() {
        val abi = requireNotNull(DuoAbi.resolve(loader))
        assertSame(MiuiWifiSignalController::class.java, abi.wifiController)
        assertSame(BatteryControllerImpl::class.java, abi.batteryLevel.declaringClass)
        assertSame(BatteryControllerImpl::class.java, abi.saverChanged.declaringClass)
    }

    @Test fun erasedCleanStateReturnDoesNotHideConcreteMobileDataSim() {
        assertSame(SignalIcon.State::class.java,
            MobileSignalController::class.java.getMethod("cleanState").returnType)
        val abi = requireNotNull(DuoAbi.resolve(loader))
        val mobile = MobileSignalController()
        val state = mobile.mCurrentState as SignalIcon.MobileState
        state.dataSim = true
        state.level = 4
        assertTrue(abi.mobileDataSim.getBoolean(abi.mobileState.get(mobile)))
        assertEquals(4, abi.mobileLevel.getInt(abi.mobileState.get(mobile)))
        val wifi = MiuiWifiSignalController()
        wifi.mCurrentState.connected = true
        assertTrue(abi.wifiConnected.getBoolean(abi.wifiState.get(wifi)))
    }

    @Test fun missingControllerKeepsTheFeatureUnavailable() {
        val absent = object : ClassLoader(loader) {
            override fun loadClass(name: String, resolve: Boolean): Class<*> {
                if (name == MiuiWifiSignalController::class.java.name) throw ClassNotFoundException(name)
                return super.loadClass(name, resolve)
            }
        }
        assertNull(DuoAbi.resolve(absent))
    }

    @Test(expected = OutOfMemoryError::class)
    fun resolverDoesNotConvertFatalLoaderFailureToNativeFallback() {
        val fatal = object : ClassLoader(loader) {
            override fun loadClass(name: String, resolve: Boolean): Class<*> {
                if (name == MiuiWifiSignalController::class.java.name) throw OutOfMemoryError("fixture")
                return super.loadClass(name, resolve)
            }
        }
        DuoAbi.resolve(fatal)
    }
}
