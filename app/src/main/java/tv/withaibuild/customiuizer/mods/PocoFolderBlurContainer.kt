package tv.withaibuild.customiuizer.mods

import android.content.Context
import android.os.Looper
import android.view.View
import io.github.libxposed.api.XposedInterface
import tv.withaibuild.customiuizer.mods.utils.FatalErrors
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import java.lang.ref.WeakReference
import java.lang.reflect.Method

/** The existing native layer samples the wallpaper; FolderIcon only blends that
 * sample. Resolve the two verified ABIs before installing either half. */
internal fun preparePocoFolderBlurContainer(loader: ClassLoader, context: Context, modern: Boolean,
    allowed: () -> Boolean): PocoFolderBlurContainerPlan {
    fun type(name: String) = XposedHelpers.findClass("com.miui.home.launcher.$name", loader)
    val blur = type("common.BlurUtilities")
    val layer = type("ShortcutMenuLayer")
    val launcher = type("Launcher")
    val boolean = Boolean::class.javaPrimitiveType!!
    val int = Int::class.javaPrimitiveType!!
    require(View::class.java.isAssignableFrom(layer))
    checkPocoFolderBlurViewApi(View::class.java)
    val layout = checkedFolderBlurMethod(layer, "onLayout", Void.TYPE, false, boolean, int, int, int, int)
    val detach = checkedFolderBlurMethod(layer, "onDetachedFromWindow", Void.TYPE, false)
    val clear = checkedFolderBlurMethod(blur, "clearAllBlur", Void.TYPE, true, View::class.java)
    val publish = checkedFolderBlurMethod(blur, "setBackgroundBlurEnabled", Void.TYPE, true, launcher)
    val publicationCaller = pocoFolderBlurPublicationCaller(launcher) {
        XposedHelpers.findClass("org.aspectj.lang.JoinPoint", loader)
    }
    val render = checkedFolderBlurMethod(blur, "setContainerBlur", Void.TYPE, true,
        View::class.java, int, boolean, *Array(if (modern) 1 else 5) { int })
    val nativeGate = checkedFolderBlurMethod(blur, "isBlurSupported", boolean, true)
    val getLauncher = checkedFolderBlurMethod(type("Application"), "getLauncher", launcher, true)
    val getLayer = checkedFolderBlurMethod(launcher, "getShortcutMenuLayer", layer, false)
    val radiusId = context.resources.getIdentifier("shortcut_menu_layer_blur_radius", "dimen", context.packageName)
    require(radiusId != 0 && context.resources.getDimensionPixelSize(radiusId) > 0) {
        "Missing native POCO sampler radius"
    }
    val framework = checkNotNull(XposedHelpers.moduleInst)
    val renderNative = framework.getInvoker(render)
    val clearNative = framework.getInvoker(clear)
    val gateNative = framework.getInvoker(nativeGate)
    val launcherNative = framework.getInvoker(getLauncher)
    val layerNative = framework.getInvoker(getLayer)
    val darkNative = if (modern) null else framework.getInvoker(
        checkedFolderBlurMethod(type("DeviceConfig"), "isDarkMode", boolean, true))
    val selectionNative = if (modern) null else framework.getInvoker(
        checkedFolderBlurMethod(launcher, "isMultiSelectEnabled", boolean, false))
    val belowNative = if (modern) null else framework.getInvoker(
        checkedFolderBlurMethod(blur, "disableMiBackgroundContainBelow", Void.TYPE, true, View::class.java, boolean)
            .also { checkedFolderBlurMethod(View::class.java, "disableMiBackgroundContainBelow", Void.TYPE, false, boolean) })
    val controller = PocoFolderBlurContainer(
        allowed = allowed,
        currentOwner = { launcherNative.invoke(null)?.let { layerNative.invoke(it) as? View } },
        nativeAllowed = { gateNative.invoke(null) == true },
        radius = { it.resources.getDimensionPixelSize(radiusId) },
        appearance = {
            (if (darkNative?.invoke(null) == true) 1 else 0) or
                (if (selectionNative != null && selectionNative.invoke(launcherNative.invoke(null)) == true) 2 else 0)
        },
        apply = { view, radius, appearance ->
            if (modern) renderNative.invoke(null, view, 2, true, radius)
            else {
                renderNative.invoke(null, view, 2, true, radius, 0xb3767676.toInt(), 100, 0x66b4b4b4, 100)
                belowNative!!.invoke(null, view, (appearance and 2) == 0)
            }
        },
        clear = { clearNative.invoke(null, it) },
    )
    return PocoFolderBlurContainerPlan(controller, layout, detach, clear, publish, publicationCaller)
}

internal fun checkPocoFolderBlurViewApi(view: Class<*>) {
    val int = Int::class.javaPrimitiveType!!
    checkedFolderBlurMethod(view, "setMiBackgroundBlurMode", Void.TYPE, false, int)
    checkedFolderBlurMethod(view, "setMiBackgroundBlurRadius", Void.TYPE, false, int)
    checkedFolderBlurMethod(view, "setMiViewBlurMode", Void.TYPE, false, int)
    checkedFolderBlurMethod(view, "setPassWindowBlurEnabled", Boolean::class.javaPrimitiveType!!, false,
        Boolean::class.javaPrimitiveType!!)
}

internal class PocoFolderBlurContainerPlan(
    val controller: PocoFolderBlurContainer,
    val layout: Method, val detach: Method, val clear: Method, val publish: Method,
    val publicationCaller: Method,
) {
    fun hooks() = listOf(layout to controller.layoutHook(), detach to controller.detachHook(),
        clear to controller.clearHook())
}

/** One weak current owner and scalar native inputs. Outgoing layers retain their
 * native sampler until their own detach; a stale callback cannot clear a new one.
 * No bitmap, drawable, animation, listener or periodic task is created. */
internal class PocoFolderBlurContainer(
    private val allowed: () -> Boolean,
    private val currentOwner: () -> View?,
    private val nativeAllowed: () -> Boolean,
    private val radius: (View) -> Int,
    private val appearance: () -> Int,
    private val apply: (View, Int, Int) -> Unit,
    private val clear: (View) -> Unit,
) {
    @Volatile private var owner = WeakReference<View>(null)
    private var owned = false
    private var lastRadius = -1
    private var lastAppearance = 0
    @Volatile private var failed = false
    @Volatile private var publicationValid = true
    private var pending: Runnable? = null
    @Volatile private var epoch = 0
    private val diagnostic = FolderBlurDiagnostic()
    val usable get() = !failed

    fun prepare(view: View) {
        try {
            if (currentOwner() !== view) return
            if (owner.get() !== view) {
                cancelPending()
                owner = WeakReference(view)
                owned = false
                lastRadius = -1
                failed = false
            }
            if (!publicationValid || !allowed()) {
                release(view)
                failed = !publicationValid
                return
            }
            if (nativeAllowed()) {
                // The original onLayout will prepare its own container.
                owned = false
                lastRadius = -1
                failed = false
                return
            }
            if (!view.isAttachedToWindow || !view.isHardwareAccelerated || view.width <= 0 || view.height <= 0) {
                failed = true
                release(view)
                return
            }
            val nextRadius = radius(view)
            require(nextRadius > 0)
            val nextAppearance = appearance()
            if (owned && lastRadius == nextRadius && lastAppearance == nextAppearance) return
            // Own partial native writes too, so a failed setter can be cleared.
            owned = true
            apply(view, nextRadius, nextAppearance)
            lastRadius = nextRadius
            lastAppearance = nextAppearance
            failed = false
            diagnostic.once(1) { "native-container-requested radius=$nextRadius mode=2 passWindow=true visible=unverified" }
        } catch (error: Throwable) {
            failed = true
            cleanupAfterFailure(view, error)
            throw error
        }
    }

    private fun release(view: View) {
        if (owner.get() !== view || !owned) return
        lastRadius = -1
        clear(view)
        owned = false
    }

    private fun cleanupAfterFailure(view: View, primary: Throwable) {
        try { release(view) } catch (cleanup: Throwable) {
            if (cleanup !== primary) primary.addSuppressed(cleanup)
            try { FatalErrors.unwrapAndRethrowIfFatal(primary) } catch (fatal: Throwable) { throw fatal }
            FatalErrors.unwrapAndRethrowIfFatal(cleanup)
        }
    }

    @Synchronized private fun cancelPending() {
        epoch++
        val work = pending
        pending = null
        work?.let { owner.get()?.removeCallbacks(it) }
    }

    fun cleared(view: View, successful: Boolean = true) {
        if (owner.get() !== view) return
        cancelPending()
        if (successful) owned = false else failed = true
        lastRadius = -1
    }

    private fun detached(view: View) {
        if (owner.get() !== view) return
        cleared(view)
        owner.clear()
        failed = false
    }

    fun stateChanged() {
        publicationValid = true
        refresh()
    }

    private fun refresh() {
        val view = owner.get() ?: return
        if (Looper.myLooper() != Looper.getMainLooper()) {
            // At most one owner-bound main-thread refresh for a publication.
            synchronized(this) {
                if (pending != null) return
                val reference = WeakReference(view)
                val expectedEpoch = epoch
                val work = Runnable {
                    synchronized(this) { pending = null }
                    reference.get()?.let { target ->
                        if (epoch == expectedEpoch && owner.get() === target) ModuleHelper.guarded { refresh() }
                    }
                }
                pending = work
                if (!view.post(work)) pending = null
            }
            return
        }
        cancelPending()
        try {
            if (currentOwner() !== view) return
            if (!publicationValid || !allowed()) {
                failed = true
                release(view)
            } else if (!owned || failed) {
                view.requestLayout()
            }
        } catch (error: Throwable) {
            failed = true
            cleanupAfterFailure(view, error)
            throw error
        }
    }

    fun publicationFailed(primary: Throwable) {
        publicationValid = false
        failed = true
        val view = owner.get() ?: return
        if (Looper.myLooper() == Looper.getMainLooper()) cleanupAfterFailure(view, primary)
        else try { refresh() } catch (cleanup: Throwable) {
            if (cleanup !== primary) primary.addSuppressed(cleanup)
            FatalErrors.unwrapAndRethrowIfFatal(primary)
            FatalErrors.unwrapAndRethrowIfFatal(cleanup)
        }
    }

    fun layoutHook() = object : MethodHook() {
        override fun intercept(chain: XposedInterface.Chain): Any? {
            val view = ModuleHelper.guarded<View?>(null) { chain.thisObject as? View }
            if (view != null) ModuleHelper.guarded { prepare(view) }
            return try { chain.proceed() } catch (native: Throwable) {
                if (view != null && owner.get() === view) {
                    failed = true
                    cleanupAfterFailure(view, native)
                }
                throw native
            }
        }
    }

    fun detachHook() = object : MethodHook() {
        override fun intercept(chain: XposedInterface.Chain) = afterNative(chain) { failure ->
            (chain.thisObject as? View)?.let { view ->
                try { failure?.let { cleanupAfterFailure(view, it) } }
                finally { detached(view) }
            }
        }
    }

    fun clearHook() = object : MethodHook() {
        override fun intercept(chain: XposedInterface.Chain) = afterNative(chain) { failure ->
            (chain.getArg(0) as? View)?.let { cleared(it, failure == null) }
        }
    }

    private fun afterNative(chain: XposedInterface.Chain, complete: (Throwable?) -> Unit): Any? {
        var failure: Throwable? = null
        try { return chain.proceed() } catch (native: Throwable) { failure = native; throw native }
        finally {
            try { complete(failure) } catch (cleanup: Throwable) {
                failure?.let { primary ->
                    if (cleanup !== primary) primary.addSuppressed(cleanup)
                    FatalErrors.unwrapAndRethrowIfFatal(primary)
                }
                FatalErrors.unwrapAndRethrowIfFatal(cleanup)
                XposedHelpers.log(cleanup)
            }
        }
    }

    fun publicationHook() = object : MethodHook() {
        override fun intercept(chain: XposedInterface.Chain): Any? {
            val result = try { chain.proceed() } catch (native: Throwable) { publicationFailed(native); throw native }
            ModuleHelper.guarded { stateChanged() }
            return result
        }
    }
}
