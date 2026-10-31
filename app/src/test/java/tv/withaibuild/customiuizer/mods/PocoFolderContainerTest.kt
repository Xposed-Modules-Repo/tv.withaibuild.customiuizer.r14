package tv.withaibuild.customiuizer.mods

import android.app.Application
import android.os.Handler
import android.os.Looper
import android.view.View
import io.github.libxposed.api.XposedInterface
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import java.io.File
import java.lang.reflect.Executable
import java.lang.reflect.Method
import java.lang.reflect.Proxy

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PocoFolderContainerTest {
    class Native {
        companion object {
            @JvmStatic fun generalGate() = false
            @JvmStatic fun folderGate(small: Boolean) = false
            @JvmStatic fun publish() {}
            @JvmStatic fun render() {}
            @JvmStatic fun clear(view: View) {}
            @JvmStatic fun draw() {}
        }
    }
    class Root : View(RuntimeEnvironment.getApplication()) {
        var attached = true
        var hardware = true
        var sampler = false
        var layoutRequests = 0
        var posts = 0
        private val main = Handler(Looper.getMainLooper())
        override fun isAttachedToWindow() = attached
        override fun isHardwareAccelerated() = hardware
        override fun requestLayout() { layoutRequests++; super.requestLayout() }
        override fun post(action: Runnable): Boolean { posts++; return main.post(action) }
        override fun removeCallbacks(action: Runnable): Boolean { main.removeCallbacks(action); return true }
        fun layoutNative() {}
        fun detachNative() {}
    }
    class VendorView {
        fun setMiBackgroundBlurMode(value: Int) {}
        fun setMiBackgroundBlurRadius(value: Int) {}
        fun setMiViewBlurMode(value: Int) {}
        fun setPassWindowBlurEnabled(value: Boolean) = true
    }
    class WrongVendorView { fun setMiBackgroundBlurMode(value: Int) = true }

    private val root = Root().apply { layout(0, 0, 400, 600) }
    private var current: View? = root
    private var capability = true
    private var nativeOwns = false
    private var radius = 150
    private var appearance = 0
    private var applies = 0
    private var clears = 0
    private var applyFailure: Throwable? = null
    private var clearFailure: Throwable? = null
    private val observedInputs = ArrayList<Pair<Int, Int>>()
    private val controller = PocoFolderBlurContainer(
        allowed = { capability }, currentOwner = { current }, nativeAllowed = { nativeOwns },
        radius = { radius }, appearance = { appearance },
        apply = { view, size, style ->
            applies++; (view as Root).sampler = true; observedInputs.add(size to style)
            applyFailure?.let { throw it }
        },
        clear = { view -> clears++; clearFailure?.let { throw it }; (view as Root).sampler = false },
    )
    private fun method(name: String, vararg args: Class<*>) = Native::class.java.getDeclaredMethod(name, *args)
    private val gate = method("folderGate", Boolean::class.javaPrimitiveType!!)
    private val general = method("generalGate")
    private val publish = method("publish")
    private val render = method("render")
    private val clear = method("clear", View::class.java)
    private val draw = method("draw")
    private val layout = Root::class.java.getDeclaredMethod("layoutNative")
    private val detach = Root::class.java.getDeclaredMethod("detachNative")
    private val plan = PocoFolderBlurContainerPlan(controller, layout, detach, clear, publish, draw)
    private val active = linkedMapOf<Method, XposedInterface.Hooker>()
    private val attempts = ArrayList<Method>()
    private val retired = ArrayList<Method>()
    private val deoptimized = ArrayList<Executable>()
    private var rejected: Method? = null
    private var deoptAllowed = true
    private var previous: XposedInterface? = null
    private var proceededArgs: List<Any?>? = null

    @Before fun framework() {
        previous = XposedHelpers.moduleInst
        XposedHelpers.moduleInst = proxy(XposedInterface::class.java) { _, name, args -> when (name) {
            "deoptimize" -> { deoptimized.add(args!![0] as Executable); deoptAllowed }
            "log" -> null
            "hook" -> {
                val target = args!![0] as Method; attempts.add(target)
                proxy(XposedInterface.HookBuilder::class.java) { builder, operation, values -> when (operation) {
                    "setPriority", "setExceptionMode" -> builder
                    "intercept" -> {
                        check(target != rejected) { "Rejected" }
                        active[target] = values!![0] as XposedInterface.Hooker
                        proxy(XposedInterface.HookHandle::class.java) { _, action, _ -> when (action) {
                            "getExecutable" -> target
                            "unhook" -> { retired.add(target); active.remove(target); null }
                            else -> error(action)
                        } }
                    }
                    else -> error(operation)
                } }
            }
            else -> error(name)
        } }
    }
    @After fun restore() { XposedHelpers.moduleInst = previous }

    private fun install() = installPocoFolderBlurHooks(gate, publish, render, listOf(draw),
        readCapability = { FolderBlurCapability(capability, capability) }, defaultFolder = { true },
        isFolderBackground = { true }, container = plan)
    private fun call(target: Method, owner: View = root, args: List<Any?> = emptyList(),
        native: () -> Any? = { false }): Any? {
        val chain = proxy(XposedInterface.Chain::class.java) { _, name, values -> when (name) {
            "getThisObject" -> owner
            "getArg" -> args[values!![0] as Int]
            "getArgs" -> args
            "proceed" -> {
                proceededArgs = if (values.isNullOrEmpty()) args else (values[0] as Array<*>).toList()
                native()
            }
            else -> error(name)
        } }
        val hook = active[target]
        return if (hook == null) native() else hook.intercept(chain)
    }
    private fun layout(view: Root = root, native: () -> Any? = { Any() }) = call(layout, view, native = native)
    private fun gate() = call(gate, args = listOf(true))

    @Test fun installedLayoutPreparesTheSamplerBeforeNativeChildrenAndKeepsTheGeneralGateNative() {
        install(); val result = Any(); var calls = 0
        assertSame(result, layout { calls++; assertTrue(root.sampler); assertEquals(true, gate()); result })
        assertEquals(1, calls); assertEquals(1, applies)
        assertFalse(active.containsKey(general)); assertEquals(false, call(general))
        repeat(1000) { layout() }
        assertEquals("Unchanged native inputs reuse the prepared container", 1, applies)
        assertEquals(0, root.posts)
    }

    @Test fun theLegacyScopeAndContainerInstallAsOneGroupWithoutChangingOtherGateCallers() {
        val extras = plan.hooks() + (publish to controller.publicationHook())
        installFolderIconBlurHooks(general, listOf(draw), extras, listOf(draw)) { capability && controller.usable }
        assertEquals(listOf(draw), deoptimized)
        assertEquals(false, call(general))
        layout { assertTrue(root.sampler); assertEquals(true, call(draw) { call(general) }); null }
        assertEquals(false, call(general))
        capability = false; call(publish) { null }
        assertFalse(root.sampler); assertEquals(false, call(draw) { call(general) })
    }

    @Test fun everyModernInstallationRejectionRollsBackAllPreviouslyAddedHooksInReverseOrder() {
        val sequence = listOf(layout, detach, clear, publish, render, gate)
        for (target in sequence) {
            attempts.clear(); retired.clear(); rejected = target
            assertThrows(IllegalStateException::class.java) { install() }
            val prefix = sequence.takeWhile { it != target }
            assertEquals(prefix.reversed(), retired); assertTrue(active.isEmpty())
        }
    }

    @Test fun aRejectedLegacyContainerOrDeoptimizationCannotLeaveAnyGateInstalled() {
        val extras = plan.hooks() + (publish to controller.publicationHook())
        rejected = clear
        assertThrows(IllegalStateException::class.java) {
            installFolderIconBlurHooks(general, listOf(draw), extras, listOf(draw)) { true }
        }
        assertEquals(listOf(detach, layout), retired); assertTrue(active.isEmpty())
        attempts.clear(); retired.clear(); rejected = null; deoptAllowed = false
        assertThrows(IllegalStateException::class.java) {
            installFolderIconBlurHooks(general, listOf(draw), extras, listOf(draw)) { true }
        }
        assertTrue(attempts.isEmpty()); assertTrue(active.isEmpty())
    }

    @Test fun disabledCapabilityNeverCreatesASamplerOrBypassesTheNativeFolderGate() {
        capability = false; install(); layout()
        assertEquals(0, applies); assertFalse(root.sampler); assertEquals(false, gate())
    }

    @Test fun unattachedSoftwareAndUnmeasuredLayersStayNativeAndCanRetryLater() {
        install()
        root.attached = false; layout(); assertEquals(false, gate())
        root.attached = true; root.hardware = false; layout(); assertEquals(false, gate())
        root.hardware = true; root.layout(0, 0, 0, 0); layout(); assertEquals(false, gate())
        assertEquals(0, applies)
        root.layout(0, 0, 400, 600); layout(); assertTrue(root.sampler); assertEquals(true, gate())
    }

    @Test fun nativeOwnershipIsPreservedAndNeverClearedByTheModule() {
        nativeOwns = true; install()
        layout { root.sampler = true; null }
        capability = false; call(publish) { null }
        assertEquals(0, applies); assertEquals(0, clears); assertTrue(root.sampler)
    }

    @Test fun nativeLayoutFailureKeepsItsIdentityAndReleasesOnlyThePreparedSampler() {
        install()
        for (error in listOf(IllegalStateException("native"), OutOfMemoryError("native fatal"))) {
            var calls = 0
            assertSame(error, assertThrows(Throwable::class.java) { layout { calls++; throw error } })
            assertEquals(1, calls); assertFalse(root.sampler); assertEquals(false, gate())
        }
        assertEquals(2, clears)
    }

    @Test fun moduleSetterFailureClearsPartialWritesAndPreservesTheNativeResult() {
        applyFailure = IllegalStateException("setter"); install(); val result = Any(); var calls = 0
        assertSame(result, layout { calls++; result }); assertEquals(1, calls)
        assertEquals(1, applies); assertEquals(1, clears); assertFalse(root.sampler); assertEquals(false, gate())
        applyFailure = null; layout(); assertEquals(true, gate())
    }

    @Test fun fatalSetterFailureIsNotSwallowedAndCleanupCannotReplaceItsIdentity() {
        val fatal = OutOfMemoryError("setter"); applyFailure = fatal
        clearFailure = ThreadDeath(); install(); var calls = 0
        assertSame(fatal, assertThrows(Throwable::class.java) { layout { calls++; null } })
        assertEquals(0, calls); assertSame(clearFailure, fatal.suppressed.single())
    }

    @Test fun aFatalCleanupStillEscapesAnOrdinaryModuleFailure() {
        applyFailure = IllegalArgumentException("setter"); val fatal = OutOfMemoryError("cleanup")
        clearFailure = fatal; install()
        assertSame(fatal, assertThrows(Throwable::class.java) { layout() })
    }

    @Test fun nativeClearInvalidatesTheCacheEvenWhenTheNativeClearThrows() {
        install(); layout(); val error = IllegalStateException("clear failed")
        assertSame(error, assertThrows(Throwable::class.java) {
            call(clear, args = listOf(root)) { root.sampler = false; throw error }
        })
        layout(); assertEquals(2, applies); assertTrue(root.sampler)
    }

    @Test fun clearingAnUnrelatedViewCannotInvalidateTheCurrentSampler() {
        install(); layout(); call(clear, args = listOf(Root())) { null }; layout()
        assertEquals(1, applies); assertTrue(root.sampler)
    }

    @Test fun aFailedNativeClearRetainsOwnershipUntilTheSamplerIsActuallyReleased() {
        install(); layout(); val error = IllegalStateException("clear failed before reset")
        assertSame(error, assertThrows(Throwable::class.java) { call(clear, args = listOf(root)) { throw error } })
        capability = false; call(publish) { null }
        assertEquals(1, clears); assertFalse(root.sampler)
    }

    @Test fun aFailedContainerCannotApplyBlackTintToAnUnbackedNativeMaterial() {
        install(); layout()
        val args = listOf(root, 20, 0xb3767676.toInt(), 100, 0x66b4b4b4, 100,
            0xff149400.toInt(), 106, 0xff2ef200.toInt(), 106, 10)
        call(render, args = args) { null }; assertNotSame(args, proceededArgs)
        radius = 0; layout()
        call(render, args = args) { null }; assertSame(args, proceededArgs)
    }

    @Test fun aFailedReleaseCannotMakeTheOldScalarCacheBlockRecovery() {
        install(); layout(); capability = false; clearFailure = IllegalStateException("clear temporarily unavailable")
        call(publish) { null }
        clearFailure = null; capability = true; call(publish) { null }; layout()
        assertEquals(2, applies); assertTrue(root.sampler); assertEquals(true, gate())
    }

    @Test fun radiusThemeAndNativeSelectionChangesRefreshOnlyTheirOwner() {
        install(); layout(); radius = 180; layout(); appearance = 1; layout(); appearance = 3; layout()
        assertEquals(listOf(150 to 0, 180 to 0, 180 to 1, 180 to 3), observedInputs)
        repeat(20) { layout() }; assertEquals(4, applies)
    }

    @Test fun anInvalidRadiusFailsClosedWithoutPoisoningTheNextValidLayout() {
        install(); layout(); radius = 0; layout(); assertFalse(root.sampler); assertEquals(false, gate())
        radius = 150; layout(); assertTrue(root.sampler); assertEquals(true, gate())
    }

    @Test fun disablingAndReenablingNativeBlurReleasesAndRequestsANaturalLayout() {
        install(); layout(); capability = false; call(publish) { null }
        assertFalse(root.sampler); assertEquals(1, clears); assertEquals(false, gate())
        val before = root.layoutRequests; capability = true; call(publish) { null }
        assertEquals(before + 1, root.layoutRequests); layout(); assertTrue(root.sampler); assertEquals(true, gate())
    }

    @Test fun aFailedPublisherCannotReuseTheOldCapabilityUntilASuccessfulPublication() {
        install(); layout(); val error = IllegalArgumentException("publish")
        assertSame(error, assertThrows(Throwable::class.java) { call(publish) { throw error } })
        layout(); assertFalse(root.sampler); assertEquals(false, gate())
        call(publish) { null }; layout(); assertTrue(root.sampler); assertEquals(true, gate())
    }

    @Test fun anOffThreadPublicationQueuesOneRefreshAndNeverWritesTheViewOffThread() {
        install(); layout(); capability = false
        val worker = Thread { repeat(10) { controller.stateChanged() } }.apply { start(); join(2000) }
        assertFalse(worker.isAlive); assertEquals(1, root.posts); assertEquals(0, clears)
        shadowOf(Looper.getMainLooper()).idle(); assertEquals(1, clears); assertFalse(root.sampler)
    }

    @Test fun detachCancelsAQueuedRefreshAndCannotTouchAReplacementOwner() {
        install(); layout()
        Thread { controller.stateChanged() }.apply { start(); join(2000) }
        call(detach) { call(clear, args = listOf(root)) { root.sampler = false; null }; null }
        root.attached = false
        val next = Root().apply { layout(0, 0, 400, 600) }; current = next; layout(next)
        shadowOf(Looper.getMainLooper()).idle(); assertTrue(next.sampler); assertEquals(2, applies)
    }

    @Test fun oldNativeFailureAndDetachNeverDisableOrReleaseTheNewOwner() {
        install(); layout(); val next = Root().apply { layout(0, 0, 400, 600) }
        current = next; layout(next); assertTrue(root.sampler)
        val error = IllegalStateException("outgoing layout")
        assertSame(error, assertThrows(Throwable::class.java) { layout(root) { throw error } })
        call(detach) { root.sampler = false; null }
        layout(next); assertTrue(next.sampler); assertEquals(2, applies); assertEquals(true, gate())
    }

    @Test fun aFailedNativeDetachStillReleasesTheOwnedSamplerAndPreservesItsError() {
        install(); layout(); val error = OutOfMemoryError("detach")
        assertSame(error, assertThrows(Throwable::class.java) { call(detach) { throw error } })
        assertFalse(root.sampler); assertEquals(1, clears)
    }

    @Test fun vendorApiPreflightRejectsMissingOrWrongDeclarations() {
        checkPocoFolderBlurViewApi(VendorView::class.java)
        assertThrows(NoSuchMethodException::class.java) { checkPocoFolderBlurViewApi(View::class.java) }
        assertThrows(IllegalArgumentException::class.java) { checkPocoFolderBlurViewApi(WrongVendorView::class.java) }
        assertTrue(attempts.isEmpty())
    }

    @Test fun aFailedOffThreadPublicationCannotReactivateTheOldSnapshot() {
        install(); layout(); val error = IllegalStateException("publisher")
        val observed = java.util.concurrent.atomic.AtomicReference<Throwable>()
        Thread { try { call(publish) { throw error } } catch (failure: Throwable) { observed.set(failure) } }
            .apply { start(); join(2000) }
        assertSame(error, observed.get()); assertEquals(false, gate())
        shadowOf(Looper.getMainLooper()).idle(); assertFalse(root.sampler)
        layout(); assertFalse(root.sampler)
        call(publish) { null }; layout(); assertTrue(root.sampler)
    }

    @Test fun bothRealInstallersPassTheirPreflightedContainerAndTheFeaturePassesItsAttachedContext() {
        var base = File(java.lang.System.getProperty("user.dir")).absoluteFile
        val relative = "app/src/main/java/tv/withaibuild/customiuizer/mods/"
        while (!File(base, relative).isDirectory) base = base.parentFile ?: error("Repository missing")
        val modern = File(base, relative + "PocoFolderIconBlur.kt").readText()
        val legacy = File(base, relative + "LauncherFolderIconBlur.kt").readText()
        val feature = File(base, relative + "utils/feature/GenericAppFeatures.kt").readText()
        assertTrue(modern.contains("preparePocoFolderBlurContainer(loader, context, true)"))
        assertTrue(modern.contains("container = container,"))
        assertTrue(legacy.contains("preparePocoFolderBlurContainer(loader, it, false, allowed)"))
        assertTrue(legacy.contains("installFolderIconBlurHooks(gate, entries, extras, deoptimize)"))
        assertTrue(feature.contains("installPoco(lpparam.classLoader, attachedVersion(), checkNotNull(applicationContext))"))
    }

    private fun <T> proxy(type: Class<T>, body: (Any, String, Array<out Any?>?) -> Any?): T = type.cast(
        Proxy.newProxyInstance(type.classLoader, arrayOf(type)) { self, method, args -> body(self, method.name, args) })
}
