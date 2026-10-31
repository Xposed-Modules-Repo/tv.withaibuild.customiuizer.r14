package tv.withaibuild.customiuizer.mods

import android.app.Application
import io.github.libxposed.api.XposedInterface
import org.junit.Assert.*
import org.junit.After
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.CustomMethodUnhooker
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class InputMethodLifecycleBoundaryTest {
    class Injector { companion object { @JvmField var sClassLoader: ClassLoader? = null } }
    @After fun clear() { Injector.sClassLoader = null }
    private fun setLoader(): Class<*> { val loader = Loader(javaClass.classLoader!!); Injector.sClassLoader = loader; return loader.loadClass(TARGET) }
    private fun enabled(type: Class<*>) = type.getField("sIsGestureLineEnable").getBoolean(null)
    private fun reset(type: Class<*>) = type.getField("sIsGestureLineEnable").setBoolean(null, true)
    private fun handle() = object : CustomMethodUnhooker { override fun unhook() = Unit }
    @Test fun nativeFailureCannotPrepareOrInstallThePlugin() {
        setLoader(); var resolutions = 0
        val hook = Various.inputMethodBottomMarginHook(Injector::class.java, resolveUtil = { resolutions++; it.loadClass(TARGET) }, installUtil = { handle() })
        val failure = IllegalStateException("native add failed"); val chain = Chain(failure)
        assertSame(failure, assertThrows(IllegalStateException::class.java) { hook.intercept(chain) })
        assertEquals(1, chain.calls); assertEquals(0, resolutions)
    }
    @Test fun nativeFatalCannotPrepareOrInstallThePlugin() {
        setLoader(); var resolutions = 0
        val hook = Various.inputMethodBottomMarginHook(Injector::class.java, resolveUtil = { resolutions++; it.loadClass(TARGET) }, installUtil = { handle() })
        val failure = OutOfMemoryError("native fatal"); val chain = Chain(failure)
        assertSame(failure, assertThrows(OutOfMemoryError::class.java) { hook.intercept(chain) })
        assertEquals(1, chain.calls); assertEquals(0, resolutions)
    }
    @Test fun absentClassCanBeRetriedOnTheNextSuccessfulNativeAdd() {
        val type = setLoader(); var ready = false; var installs = 0
        val hook = Various.inputMethodBottomMarginHook(Injector::class.java, resolveUtil = { if (ready) it.loadClass(TARGET) else null }, installUtil = { installs++; handle() })
        val first = Chain(); assertSame(first.result, hook.intercept(first)); assertEquals(0, installs); assertTrue(enabled(type))
        ready = true; val second = Chain(); assertSame(second.result, hook.intercept(second)); assertEquals(1, installs); assertFalse(enabled(type))
    }
    @Test fun failedInstallationCannotMutateTheNativeFlagAndCanRetry() {
        val type = setLoader(); var attempts = 0
        val hook = Various.inputMethodBottomMarginHook(Injector::class.java, installUtil = { attempts++; if (attempts == 1) null else handle() })
        hook.intercept(Chain()); assertTrue(enabled(type))
        hook.intercept(Chain()); assertEquals(2, attempts); assertFalse(enabled(type))
    }
    @Test fun twoSameNamedClassesHaveIndependentOnceOnlyInstallations() {
        val first = setLoader(); var installs = 0
        val hook = Various.inputMethodBottomMarginHook(Injector::class.java, installUtil = { installs++; handle() })
        hook.intercept(Chain()); hook.intercept(Chain()); assertEquals(1, installs)
        val second = setLoader(); assertNotSame(first, second); assertEquals(first.name, second.name)
        hook.intercept(Chain()); assertEquals(2, installs); assertFalse(enabled(first)); assertFalse(enabled(second))
        Injector.sClassLoader = first.classLoader; hook.intercept(Chain()); assertEquals(2, installs)
    }
    @Test fun recreatedNativeBottomViewRestoresTheOwnedFlagWithoutReinstalling() {
        val type = setLoader(); var installs = 0
        val hook = Various.inputMethodBottomMarginHook(Injector::class.java, installUtil = { installs++; handle() })
        hook.intercept(Chain()); reset(type); hook.intercept(Chain())
        assertEquals(1, installs); assertFalse(enabled(type))
    }
    @Test fun fatalInstallLeavesTheClassRetryableAndNativeFlagIntact() {
        val type = setLoader(); var attempts = 0; val failure = OutOfMemoryError("install fatal")
        val hook = Various.inputMethodBottomMarginHook(Injector::class.java, installUtil = {
            attempts++; if (attempts == 1) throw InvocationTargetException(failure) else handle()
        })
        assertSame(failure, assertThrows(OutOfMemoryError::class.java) { hook.intercept(Chain()) }); assertTrue(enabled(type))
        hook.intercept(Chain()); assertEquals(2, attempts); assertFalse(enabled(type))
    }
    @Test fun reentrantNativeAddCannotInstallTwice() {
        setLoader(); var attempts = 0
        lateinit var hook: tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
        hook = Various.inputMethodBottomMarginHook(Injector::class.java, installUtil = { attempts++; hook.intercept(Chain()); handle() })
        hook.intercept(Chain()); assertEquals(1, attempts)
    }
    @Test fun suppressedUpdateWritesFalseAndDoesNotInvokeTheNativeUpdate() {
        val type = setLoader(); val chain = Chain(); assertNull(Various.inputMethodGestureLineHook(type).intercept(chain))
        assertFalse(enabled(type)); assertEquals(0, chain.calls)
    }
    private class Loader(parent: ClassLoader) : ClassLoader(parent) {
        override fun loadClass(name: String, resolve: Boolean): Class<*> {
            if (name != TARGET) return super.loadClass(name, resolve)
            return synchronized(this) {
                val type = findLoadedClass(name) ?: run {
                    val bytes = requireNotNull(parent.getResourceAsStream(name.replace('.', '/') + ".class")).use { it.readBytes() }
                    defineClass(name, bytes, 0, bytes.size)
                }
                if (resolve) resolveClass(type)
                type
            }
        }
    }
    private class Chain(val failure: Throwable? = null) : XposedInterface.Chain {
        val result = Any(); var calls = 0
        override fun getExecutable(): Executable = error("No reflective method access")
        override fun getThisObject(): Any = Any()
        override fun getArgs(): List<Any?> = error("No argument copies")
        override fun getArg(index: Int): Any? = error("No argument reads")
        override fun proceed(): Any? { calls++; failure?.let { throw it }; return result }
        override fun proceed(args: Array<Any?>): Any? = error("No rewriting")
        override fun proceedWith(receiver: Any): Any? = error("No rewriting")
        override fun proceedWith(receiver: Any, args: Array<Any>): Any? = error("No rewriting")
    }
    companion object { const val TARGET = "com.miui.inputmethod.InputMethodUtil" }
}
