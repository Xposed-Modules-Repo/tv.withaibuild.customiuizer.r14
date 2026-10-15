package tv.withaibuild.customiuizer.mods.utils

import android.annotation.SuppressLint
import io.github.libxposed.api.XposedInterface

/**
 * Isolated bridge for libxposed API 102 hook installation features.
 *
 * This is the **only** file in the module that may reference
 * [XposedInterface.HookBuilder.setId] and [XposedInterface.HookHandle.getId].
 * Keeping the symbols here means the rest of
 * the code base does not import it and the API 101 path can remain untouched.
 *
 * The bridge does not hold [HookHandle], [Context], or any long-lived state.  It
 * has no Maps, no Handlers, no threads and no Receivers.
 */
internal object Api102HookBridge {

    /**
     * Assigns a stable [id] to [builder] when the host supports it.
     *
     * The caller must ensure [XposedApiCapabilities.supportsStableHookId] is true.
     * The returned [HookBuilder] is the same builder, so the call can be chained.
     */
    @JvmStatic
    @SuppressLint("XposedNewApi")
    fun setStableHookId(builder: XposedInterface.HookBuilder, id: String): XposedInterface.HookBuilder {
        if (!XposedApiCapabilities.supportsStableHookId()) return builder
        return builder.setId(id)
    }

    /** Installation-only path; the callback and its invocation behavior are unchanged. */
    @JvmStatic
    fun interceptWithStableId(
        builder: XposedInterface.HookBuilder,
        hook: HookerClassHelper.MethodHook,
        id: String,
    ): XposedInterface.HookHandle {
        val configured = builder.setPriority(hook.mPriority)
            .setExceptionMode(XposedInterface.ExceptionMode.PASSTHROUGH)
        var hasId = false
        val identified = try {
            setStableHookId(configured, id).also {
                hasId = XposedApiCapabilities.supportsStableHookId()
            }
        } catch (t: Throwable) {
            FatalErrors.unwrapAndRethrowIfFatal(t)
            XposedHelpers.log("API 102 hook ID unavailable for $id: ${t.javaClass.simpleName}")
            configured
        }
        // Never retry intercept: an installation failure may have framework-side effects.
        val handle = identified.intercept(hook)
        if (hasId) confirmStableHookId(handle, id)
        return handle
    }

    /** Readback is diagnostic only; never retry or remove an already installed hook. */
    @SuppressLint("XposedNewApi")
    private fun confirmStableHookId(handle: XposedInterface.HookHandle, id: String) {
        val actualId = try {
            handle.id
        } catch (t: Throwable) {
            FatalErrors.unwrapAndRethrowIfFatal(t)
            XposedHelpers.log("API 102 hook ID readback unavailable for $id: ${t.javaClass.simpleName}")
            return
        }
        if (actualId == id) {
            XposedHelpers.log("API 102 stable hook ID installed: $id (confirmed)")
        } else {
            XposedHelpers.log("API 102 hook ID not confirmed: $id")
        }
    }

    /** Match the legacy declared-method selection; IDs are scoped per executable. */
    @JvmStatic
    fun interceptAllWithStableId(hookClass: Class<*>, methodName: String, hook: HookerClassHelper.MethodHook, id: String): Boolean {
        var installed = false
        for (method in hookClass.declaredMethods) {
            if (method.name == methodName) {
                interceptWithStableId(XposedHelpers.moduleInst.hook(method), hook, id)
                installed = true
            }
        }
        return installed
    }

    /** Short, fixed stable hook IDs for infrastructure hooks.  Never dynamically joined. */
    const val STABLE_ID_SYSTEMUI_INIT = "systemui.init"
    const val STABLE_ID_PLUGIN_CREATE = "systemui.plugin.create"
    const val STABLE_ID_APPLICATION_ATTACH = "application.attach"
}
