package tv.withaibuild.customiuizer.mods

import android.app.Application
import android.content.Context
import io.github.libxposed.api.XposedInterface
import io.github.libxposed.api.XposedModuleInterface.PackageReadyParam
import org.junit.Assert.*
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.mods.utils.FeatureInstallResult
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import java.io.File
import java.lang.reflect.Executable
import java.lang.reflect.Proxy

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class InputMethodInstallContractTest {
    class Injector { companion object { @JvmField var sClassLoader: ClassLoader? = null } }
    class WrongReturn { companion object { @JvmField var sIsGestureLineEnable = true; @JvmStatic fun updateGestureLineEnable(context: Context): Int = 1 } }
    class ReadonlyFlag { companion object { const val sIsGestureLineEnable = true; @JvmStatic fun updateGestureLineEnable(context: Context) = Unit } }
    class WrongFlag { companion object { @JvmField var sIsGestureLineEnable = 42; @JvmStatic fun updateGestureLineEnable(context: Context) = Unit } }
    @After fun clear() { Injector.sClassLoader = null }
    @Test fun unsupportedReturnTypeCannotInstallOrChangeNativeState() {
        assertUnsupported(WrongReturn::class.java); assertTrue(WrongReturn.sIsGestureLineEnable)
    }
    @Test fun readonlyFlagCannotInstallOrChangeNativeState() {
        assertUnsupported(ReadonlyFlag::class.java); assertTrue(ReadonlyFlag.sIsGestureLineEnable)
    }
    @Test fun nonBooleanFlagCannotInstallOrChangeNativeState() {
        assertUnsupported(WrongFlag::class.java); assertEquals(42, WrongFlag.sIsGestureLineEnable)
    }
    private fun assertUnsupported(type: Class<*>) {
        Injector.sClassLoader = javaClass.classLoader; var attempts = 0
        val hook = Various.inputMethodBottomMarginHook(Injector::class.java, resolveUtil = { type }, installUtil = { attempts++; error("Unsupported ABI must not install") })
        val chain = Chain(); assertSame(chain.result, hook.intercept(chain)); assertEquals(1, chain.calls); assertEquals(0, attempts)
    }
    @Test fun missingBridgeIsTransientAndDoesNotReadPackageArguments() {
        val old = XposedHelpers.bridge
        try {
            XposedHelpers.bridge = null
            val parameter = Proxy.newProxyInstance(PackageReadyParam::class.java.classLoader, arrayOf(PackageReadyParam::class.java)) { _, _, _ -> error("Missing bridge must not inspect package") } as PackageReadyParam
            assertEquals(FeatureInstallResult.FAILED_TRANSIENT, Various.DisableDefraudAppsCheck(parameter))
        } finally { XposedHelpers.bridge = old }
    }
    @Test fun missingInjectorKeepsTheExistingClassMissingDiagnostic() {
        val diagnostics = tv.withaibuild.customiuizer.mods.utils.HookDiagnostics
        diagnostics.reset()
        try {
            val loader = object : ClassLoader(javaClass.classLoader) {
                override fun loadClass(name: String, resolve: Boolean): Class<*> {
                    if (name == "android.inputmethodservice.InputMethodServiceInjector") throw ClassNotFoundException(name)
                    return super.loadClass(name, resolve)
                }
            }
            val parameter = Proxy.newProxyInstance(PackageReadyParam::class.java.classLoader, arrayOf(PackageReadyParam::class.java)) { _, method, _ ->
                if (method.name == "getClassLoader") loader else error("No other package access")
            } as PackageReadyParam
            Various.FixInputMethodBottomMarginHook(parameter)
            assertTrue(diagnostics.snapshot().any {
                it.status == tv.withaibuild.customiuizer.mods.utils.HookDiagnostics.Status.TARGET_CLASS_MISSING && it.targetMember == "addMiuiBottomView"
            })
        } finally { diagnostics.reset() }
    }
    @Test fun installationUsesTestedCallbacksAndPropagatesTheGuardResultWithFinallyCleanup() {
        val source = source("app/src/main/java/tv/withaibuild/customiuizer/mods/Various.kt")
        assertTrue(source.contains("installInputMethodOuterHooks(XposedHelpers.findClassIfExists("))
        assertTrue(source.contains("val callback = inputMethodBottomMarginHook(injector)"))
        assertTrue(source.contains("PreparedHookBatch(installHook).install(methods.map { it to callback })"))
        assertTrue(source.contains("Context::class.java, inputMethodGestureLineHook(it))"))
        assertTrue(source.contains("String::class.java, gboardPaddingHook())"))
        assertTrue(source.contains("return installDefraudAppsHook("))
        assertTrue(source.contains("ModuleHelper.hookMethod(method, hook) != null"))
        val feature = source("app/src/main/java/tv/withaibuild/customiuizer/mods/utils/feature/GuardProviderFeatures.kt")
        assertTrue(feature.contains("return Various.DisableDefraudAppsCheck(lpparam)"))
        assertTrue(feature.contains("} finally {\n            XposedHelpers.closeBridge()"))
        val inputFeature = source("app/src/main/java/tv/withaibuild/customiuizer/mods/utils/feature/InputMethodFeatures.kt")
        assertTrue(inputFeature.contains("return Various.FixInputMethodBottomMarginHook(lpparam)"))
        val screenshotFeature = source("app/src/main/java/tv/withaibuild/customiuizer/mods/utils/feature/MediaFeatures.kt")
        assertTrue(screenshotFeature.contains("return ModsSystem.ScreenshotConfigHook(lpparam)"))
        assertTrue(screenshotFeature.contains("} finally {\n            XposedHelpers.closeBridge()"))
    }
    private fun source(path: String): String {
        var root = File(java.lang.System.getProperty("user.dir")).absoluteFile
        while (!File(root, path).isFile) root = root.parentFile ?: error("Repository not found")
        return File(root, path).readText().replace("\r\n", "\n")
    }
    private class Chain : XposedInterface.Chain {
        val result = Any(); var calls = 0
        override fun getExecutable(): Executable = error("No method access")
        override fun getThisObject(): Any = error("No owner access")
        override fun getArgs(): List<Any?> = error("No argument copies")
        override fun getArg(index: Int): Any? = error("No argument reads")
        override fun proceed(): Any? { calls++; return result }
        override fun proceed(args: Array<Any?>): Any? = error("No rewriting")
        override fun proceedWith(receiver: Any): Any? = error("No rewriting")
        override fun proceedWith(receiver: Any, args: Array<Any>): Any? = error("No rewriting")
    }
}
