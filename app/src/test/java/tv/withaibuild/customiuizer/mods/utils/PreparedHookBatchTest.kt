package tv.withaibuild.customiuizer.mods.utils

import android.app.Application
import io.github.libxposed.api.XposedInterface
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.CustomMethodUnhooker
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PreparedHookBatchTest {
    class Target { fun a() = Unit; fun b() = Unit; fun c() = Unit }
    private val methods = listOf("a", "b", "c").map { Target::class.java.getDeclaredMethod(it) }
    private fun handle(close: () -> Unit = {}) = object : CustomMethodUnhooker { override fun unhook() = close() }
    private fun callback(onCall: () -> Unit = {}) = object : MethodHook(37) { override fun intercept(chain: XposedInterface.Chain): Any? { onCall(); return "active" } }
    @Test fun emptyBatchIsPermanentAndDoesNotInstall() {
        var calls = 0; val batch = PreparedHookBatch { _, _ -> calls++; handle() }
        assertEquals(FeatureInstallResult.FAILED_PERMANENT, batch.install(emptyList())); assertEquals(0, calls)
    }
    @Test fun priorityAndNullableConstantArePreservedOnlyAfterCommit() {
        var installs = 0; var installed: MethodHook? = null
        val batch = PreparedHookBatch { _, hook ->
            installs++; installed = hook; assertEquals(37, hook.mPriority)
            val chain = Chain(); assertEquals("native", hook.intercept(chain)); assertEquals(1, chain.calls)
            handle()
        }
        val entries = listOf(methods[0] to HookerClassHelper.returnConstant(37, null))
        assertEquals(FeatureInstallResult.INSTALLED, batch.install(entries))
        val chain = Chain(); assertNull(installed!!.intercept(chain)); assertEquals(0, chain.calls)
        assertEquals(FeatureInstallResult.ALREADY_INSTALLED, batch.install(entries)); assertEquals(1, installs)
    }
    @Test fun failedBatchUnhooksInReverseAndItsCallbacksCannotBeReactivated() {
        val closed = ArrayList<Int>(); val callbacks = ArrayList<MethodHook>(); var attempts = 0; var business = 0
        val original = callback { business++ }
        val entries = methods.map { it to original }
        val batch = PreparedHookBatch { _, hook -> callbacks.add(hook); attempts++; if (attempts == 3) null else attempts.let { id -> handle { closed.add(id) } } }
        assertEquals(FeatureInstallResult.FAILED_TRANSIENT, batch.install(entries)); assertEquals(listOf(2, 1), closed)
        val chain = Chain(); assertEquals("native", callbacks[0].intercept(chain)); assertEquals(1, chain.calls); assertEquals(0, business)
        assertEquals(FeatureInstallResult.FAILED_PERMANENT, batch.install(entries)); assertEquals(3, attempts)
        var fresh: MethodHook? = null; val next = PreparedHookBatch { _, hook -> fresh = hook; handle() }
        assertEquals(FeatureInstallResult.INSTALLED, next.install(listOf(methods[0] to original))); assertEquals("active", fresh!!.intercept(Chain())); assertEquals(1, business)
        assertEquals("native", callbacks[0].intercept(Chain())); assertEquals(1, business)
    }
    @Test fun ordinaryInstallationFailureReleasesEarlierHandles() {
        var attempts = 0; var closes = 0
        val batch = PreparedHookBatch { _, _ -> attempts++; if (attempts == 2) throw IllegalStateException("install failed") else handle { closes++ } }
        assertEquals(FeatureInstallResult.FAILED_TRANSIENT, batch.install(methods.map { it to callback() })); assertEquals(1, closes)
    }
    @Test fun cleanupFailureIsPermanentAndDoesNotSkipRemainingCleanup() {
        var attempts = 0; var closes = 0; val callbacks = ArrayList<MethodHook>()
        val batch = PreparedHookBatch { _, hook ->
            callbacks.add(hook); attempts++
            when (attempts) { 1 -> handle { closes++ }; 2 -> handle { closes++; throw IllegalStateException("unhook failed") }; else -> null }
        }
        assertEquals(FeatureInstallResult.FAILED_PERMANENT, batch.install(methods.map { it to callback() })); assertEquals(2, closes)
        assertEquals("native", callbacks[0].intercept(Chain()))
    }
    @Test fun originalFatalWinsAfterAllCleanupIsAttempted() {
        var attempts = 0; var closes = 0; val primary = OutOfMemoryError("install fatal"); val secondary = OutOfMemoryError("cleanup fatal")
        val batch = PreparedHookBatch { _, _ -> attempts++; when (attempts) {
            1 -> handle { closes++ }
            2 -> handle { closes++; throw secondary }
            else -> throw primary
        } }
        assertSame(primary, assertThrows(OutOfMemoryError::class.java) { batch.install(methods.map { it to callback() }) })
        assertEquals(2, closes); assertTrue(primary.suppressed.contains(secondary))
    }
    @Test fun wrappedInstallationFatalIsUnwrappedAfterCleanup() {
        var attempts = 0; var closes = 0; val fatal = OutOfMemoryError("wrapped install")
        val batch = PreparedHookBatch { _, _ -> attempts++; if (attempts == 2) throw InvocationTargetException(fatal) else handle { closes++ } }
        assertSame(fatal, assertThrows(OutOfMemoryError::class.java) { batch.install(methods.map { it to callback() }) }); assertEquals(1, closes)
    }
    private class Chain : XposedInterface.Chain {
        var calls = 0
        override fun getExecutable(): Executable = error("No method reads")
        override fun getThisObject(): Any = error("No owner reads")
        override fun getArgs(): List<Any?> = error("No argument copies")
        override fun getArg(index: Int): Any? = error("No argument reads")
        override fun proceed(): Any? { calls++; return "native" }
        override fun proceed(args: Array<Any?>): Any? = error("No rewriting")
        override fun proceedWith(receiver: Any): Any? = error("No rewriting")
        override fun proceedWith(receiver: Any, args: Array<Any>): Any? = error("No rewriting")
    }
}
