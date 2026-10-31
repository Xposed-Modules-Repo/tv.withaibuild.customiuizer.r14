package tv.withaibuild.customiuizer.mods

import android.app.Application
import io.github.libxposed.api.XposedInterface
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.mods.utils.FeatureInstallResult
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class GuardProviderInstallOutcomeTest {
    class Target { fun apps(): ArrayList<Any?> = arrayListOf("native") }
    private val method = Target::class.java.getDeclaredMethod("apps")
    @Test fun noUniqueTargetIsPermanentFailureAndCannotInstall() {
        var installs = 0
        assertEquals(FeatureInstallResult.FAILED_PERMANENT, Various.installDefraudAppsHook({ null }) { _, _ -> installs++; true })
        assertEquals(0, installs)
    }
    @Test fun ordinaryResolutionFailureIsTransient() {
        assertEquals(FeatureInstallResult.FAILED_TRANSIENT, Various.installDefraudAppsHook({ throw IllegalStateException("query failed") }) { _, _ -> error("must not install") })
    }
    @Test fun nullHandleCannotBecomeInstalled() {
        assertEquals(FeatureInstallResult.FAILED_TRANSIENT, Various.installDefraudAppsHook({ method }) { _, _ -> false })
    }
    @Test fun ordinaryInstallationFailureIsTransient() {
        assertEquals(FeatureInstallResult.FAILED_TRANSIENT, Various.installDefraudAppsHook({ method }) { _, _ -> throw IllegalStateException("install failed") })
    }
    @Test fun directFatalResolutionEscapes() {
        val failure = OutOfMemoryError("query fatal")
        assertSame(failure, assertThrows(OutOfMemoryError::class.java) { Various.installDefraudAppsHook({ throw failure }) { _, _ -> true } })
    }
    @Test fun wrappedFatalInstallationEscapes() {
        val failure = OutOfMemoryError("install fatal")
        assertSame(failure, assertThrows(OutOfMemoryError::class.java) { Various.installDefraudAppsHook({ method }) { _, _ -> throw InvocationTargetException(failure) } })
    }
    @Test fun onlyConfirmedInstallationReturnsInstalled() {
        var installs = 0
        assertEquals(FeatureInstallResult.INSTALLED, Various.installDefraudAppsHook({ method }) { target, callback ->
            installs++; assertSame(method, target)
            val first = callback.intercept(Chain()) as ArrayList<Any?>; assertTrue(first.isEmpty()); first.add("consumer mutation")
            val second = callback.intercept(Chain()) as ArrayList<Any?>; assertTrue(second.isEmpty()); assertNotSame(first, second)
            true
        }); assertEquals(1, installs)
    }
    private class Chain : XposedInterface.Chain {
        override fun getExecutable(): Executable = error("No native method access")
        override fun getThisObject(): Any = error("No native owner access")
        override fun getArgs(): List<Any?> = error("No argument copies")
        override fun getArg(index: Int): Any? = error("No argument reads")
        override fun proceed(): Any? = error("Native query must be suppressed")
        override fun proceed(args: Array<Any?>): Any? = error("No rewriting")
        override fun proceedWith(receiver: Any): Any? = error("No rewriting")
        override fun proceedWith(receiver: Any, args: Array<Any>): Any? = error("No rewriting")
    }
}
