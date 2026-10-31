package tv.withaibuild.customiuizer.mods.appbehavior

import android.content.pm.ActivityInfo
import android.os.Handler
import android.view.WindowManager
import io.github.libxposed.api.XposedInterface
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.AfterHookCallback
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import java.lang.reflect.Field
import java.lang.reflect.Method

/** Window manager owns visibility, ordering, sensor activation and restoration. */
internal object AppWindowBehaviorHooks {
    @Volatile private var policy = AppBehaviorPolicy()

    fun install(loader: ClassLoader) {
        val initial = AppBehaviorPolicy.read(MainModule.mPrefs, includeVolume = false)
        val rotation = MainModule.mPrefs.getBoolean(AppBehaviorPolicy.PREFIX + "rotation")
        val brightness = MainModule.mPrefs.getBoolean(AppBehaviorPolicy.PREFIX + "brightness") ||
            MainModule.mPrefs.getBoolean(AppBehaviorPolicy.PREFIX + "block")
        val handles = ArrayList<tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.CustomMethodUnhooker>()
        if (!rotation && !brightness) return
        val handler = XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.internal.os.BackgroundThread", loader), "getHandler") as Handler
        val common = CommonAccess(loader)
        var rotationAccess: WindowAccess? = null
        var brightnessAccess: WindowAccess? = null
        try {
            if (rotation) rotationAccess = ModuleHelper.guarded<WindowAccess?>(null) {
                WindowAccess(loader, true, common).also { access ->
                    handles.add(checkNotNull(ModuleHelper.hookMethod(checkNotNull(access.orientation), object : MethodHook() {
                        override fun after(callback: AfterHookCallback) = ModuleHelper.guarded {
                            if (callback.getThrowable() != null) return@guarded
                            val record = callback.getThisObject() ?: return@guarded
                            val cfg = policy
                            if (cfg.rotation.isEmpty()) return@guarded
                            val pkg = access.pkg.get(record) as String
                            if (pkg !in cfg.rotation || !access.eligible(record)) return@guarded
                            access.wm = checkNotNull(access.recordWm).get(record)
                            val requested = callback.getResult() as Int
                            val target = cfg.orientation(pkg, requested)
                            if (target != requested) callback.setResult(target)
                        }
                    })))
                }
            }
            if (brightness) brightnessAccess = ModuleHelper.guarded<WindowAccess?>(null) {
                WindowAccess(loader, false, common).also { access ->
                    handles.add(checkNotNull(ModuleHelper.hookMethod(checkNotNull(access.handleWindow), object : MethodHook() {
                        override fun intercept(chain: XposedInterface.Chain): Any? {
                            val attrs = ModuleHelper.guarded<WindowManager.LayoutParams?>(null) {
                                val cfg = policy
                                if (cfg.brightness.isEmpty() && cfg.blocked.isEmpty()) return@guarded null
                                val window = chain.getArg(0) ?: return@guarded null
                                val record = checkNotNull(access.activity).get(window) ?: return@guarded null
                                val pkg = access.pkg.get(record) as String
                                if (pkg !in cfg.brightness && pkg !in cfg.blocked) return@guarded null
                                if (!access.eligible(record)) return@guarded null
                                access.wm = checkNotNull(access.wmService).get(chain.thisObject)
                                checkNotNull(access.attrs).get(window) as WindowManager.LayoutParams
                            } ?: return chain.proceed()
                            val raw = attrs.screenBrightness
                            val target = ModuleHelper.guarded(raw) {
                                val record = checkNotNull(access.activity).get(chain.getArg(0))
                                policy.brightness(access.pkg.get(record) as String,
                                    (checkNotNull(access.info).get(record) as ActivityInfo).name, raw)
                            }
                            return withBrightness(attrs, target) { chain.proceed() }
                        }
                    })))
                }
            }
        } catch (error: Throwable) {
            handles.asReversed().forEach { ModuleHelper.guarded { it.unhook() } }
            throw error
        }
        check(rotationAccess != null || brightnessAccess != null) { "No enabled app-window capability could be installed" }
        policy = initial
        ModuleHelper.observePreferenceChange(object : ModuleHelper.PreferenceObserver {
            override fun onChange(key: String?) {
                if (key != null && !key.startsWith(AppBehaviorPolicy.PREFIX)) return
                handler.post { ModuleHelper.guarded {
                    val next = AppBehaviorPolicy.read(MainModule.mPrefs, includeVolume = false)
                    if (next == policy) return@guarded
                    policy = next
                    // No window snapshot to restore: request a native traversal with the new policy.
                    val rotationOwner = rotationAccess?.wm
                    rotationAccess?.let { access -> ModuleHelper.guarded { access.refresh() } }
                    brightnessAccess?.let { access ->
                        // A shared WM needs one traversal, even when both features are enabled.
                        if (access.wm !== rotationOwner || rotationOwner == null)
                            ModuleHelper.guarded { access.refresh() }
                    }
                } }
            }
        })
    }

    internal inline fun <T> withBrightness(attrs: WindowManager.LayoutParams, target: Float, block: () -> T): T {
        val raw = attrs.screenBrightness
        if (raw == target) return block()
        // The native method runs under the WM global lock. Never leave the app's request altered.
        attrs.screenBrightness = target
        try { return block() } finally { attrs.screenBrightness = raw }
    }

    /** Shared only by the enabled groups; contains no orientation or window-brightness API. */
    private class CommonAccess(loader: ClassLoader) {
        val record = Class.forName("com.android.server.wm.ActivityRecord", false, loader)
        val wmClass = Class.forName("com.android.server.wm.WindowManagerService", false, loader)
        val pkg = field(record, "packageName")
        val user = field(record, "mUserId")
        val multi = method(record, "inMultiWindowMode")
        val display = method(record, "getDisplayId")
        val globalLock = field(wmClass, "mGlobalLock")
        val traverse = method(wmClass, "requestTraversal")
    }

    private class WindowAccess(loader: ClassLoader, rotation: Boolean, common: CommonAccess) {
        private val record = common.record
        private val window = if (rotation) null else Class.forName("com.android.server.wm.WindowState", false, loader)
        private val root = if (rotation) null else Class.forName("com.android.server.wm.RootWindowContainer", false, loader)
        private val wmClass = common.wmClass
        val pkg = common.pkg
        val recordWm = if (rotation) field(record, "mWmService") else null
        val info = if (rotation) null else field(record, "info")
        private val user = common.user
        private val multi = common.multi
        private val display = common.display
        val activity = if (rotation) null else field(checkNotNull(window), "mActivityRecord")
        val attrs = if (rotation) null else field(checkNotNull(window), "mAttrs")
        val orientation = if (rotation) method(record, "getOrientation", Int::class.javaPrimitiveType!!) else null
        val handleWindow = if (rotation) null else method(checkNotNull(root), "handleNotObscuredLocked", checkNotNull(window), Boolean::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!)
        val wmService = if (rotation) null else field(checkNotNull(root), "mWmService")
        private val globalLock = common.globalLock
        private val traverse = common.traverse
        private val updateRotation = if (rotation) method(wmClass, "updateRotation", Boolean::class.javaPrimitiveType!!, Boolean::class.javaPrimitiveType!!) else null
        @Volatile var wm: Any? = null // system_server lifetime; never retains an Activity/window.
        fun refresh() {
            val owner = wm ?: return
            synchronized(checkNotNull(globalLock.get(owner))) {
                traverse.invoke(owner)
                updateRotation?.invoke(owner, false, false)
            }
        }
        fun eligible(value: Any): Boolean = user.getInt(value) == 0 && display.invoke(value) == 0 && multi.invoke(value) == false
    }

    internal fun field(type: Class<*>, name: String): Field {
        var current: Class<*>? = type
        while (current != null) {
            try { return current.getDeclaredField(name).apply { isAccessible = true } }
            catch (_: NoSuchFieldException) { current = current.superclass }
        }
        throw NoSuchFieldException("${type.name}.$name")
    }

    internal fun method(type: Class<*>, name: String, vararg args: Class<*>): Method {
        var current: Class<*>? = type
        while (current != null) {
            try { return current.getDeclaredMethod(name, *args).apply { isAccessible = true } }
            catch (_: NoSuchMethodException) { current = current.superclass }
        }
        throw NoSuchMethodException("${type.name}.$name")
    }
}
