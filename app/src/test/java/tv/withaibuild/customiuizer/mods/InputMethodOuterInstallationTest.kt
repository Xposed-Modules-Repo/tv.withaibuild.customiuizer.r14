package tv.withaibuild.customiuizer.mods

import android.app.Application
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.mods.utils.FeatureInstallResult
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.CustomMethodUnhooker

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class InputMethodOuterInstallationTest {
    class Injector {
        companion object { @JvmField var sClassLoader: ClassLoader? = null }
        fun addMiuiBottomView() = Unit
        fun addMiuiBottomView(flags: Int) = Unit
    }
    class NoEntry { companion object { @JvmField var sClassLoader: ClassLoader? = null } }
    class WrongField { companion object { @JvmField var sClassLoader = 42 }; fun addMiuiBottomView() = Unit }
    private fun handle(close: () -> Unit = {}) = object : CustomMethodUnhooker { override fun unhook() = close() }
    @Test fun absentClassIsPermanentFailureWithoutHooks() {
        var installs = 0; assertEquals(FeatureInstallResult.FAILED_PERMANENT, Various.installInputMethodOuterHooks(null) { _, _ -> installs++; handle() }); assertEquals(0, installs)
    }
    @Test fun absentEntryIsPermanentFailureWithoutHooks() {
        var installs = 0; assertEquals(FeatureInstallResult.FAILED_PERMANENT, Various.installInputMethodOuterHooks(NoEntry::class.java) { _, _ -> installs++; handle() }); assertEquals(0, installs)
    }
    @Test fun incompatibleLoaderFieldCannotBecomeActive() {
        var installs = 0; assertEquals(FeatureInstallResult.FAILED_PERMANENT, Various.installInputMethodOuterHooks(WrongField::class.java) { _, _ -> installs++; handle() }); assertEquals(0, installs)
    }
    @Test fun everyExistingOverloadMustInstallExactlyOnce() {
        val methods = ArrayList<java.lang.reflect.Method>()
        assertEquals(FeatureInstallResult.INSTALLED, Various.installInputMethodOuterHooks(Injector::class.java) { method, _ -> methods.add(method); handle() })
        assertEquals(2, methods.size); assertEquals(2, methods.toSet().size); assertEquals(setOf(0, 1), methods.map { it.parameterCount }.toSet())
    }
    @Test fun failedSecondOverloadCannotReportSuccessAndReleasesTheFirst() {
        var attempts = 0; var closes = 0
        assertEquals(FeatureInstallResult.FAILED_TRANSIENT, Various.installInputMethodOuterHooks(Injector::class.java) { _, _ -> attempts++; if (attempts == 2) null else handle { closes++ } })
        assertEquals(2, attempts); assertEquals(1, closes)
    }
    @Test fun fatalSecondOverloadReleasesTheFirstAndRetainsIdentity() {
        var attempts = 0; var closes = 0; val failure = OutOfMemoryError("install fatal")
        assertSame(failure, assertThrows(OutOfMemoryError::class.java) { Various.installInputMethodOuterHooks(Injector::class.java) { _, _ -> attempts++; if (attempts == 2) throw failure else handle { closes++ } } })
        assertEquals(2, attempts); assertEquals(1, closes)
    }
}
