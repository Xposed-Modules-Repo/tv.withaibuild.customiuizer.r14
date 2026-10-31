package tv.withaibuild.customiuizer.mods.appbehavior

import android.content.Context
import android.media.AudioAttributes
import android.media.AudioManager
import android.os.Binder
import android.os.Handler
import android.provider.Settings
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.ContextResolver
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.AfterHookCallback
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.ModuleHelper
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import java.lang.ref.WeakReference
import java.util.concurrent.atomic.AtomicLong

/** Media focus defines the use scene. Foreground navigation never changes music volume. */
internal object AppVolumeMemoryHooks {
    private const val STORE = "customiuizer_a14_app_volume_v1"
    @Volatile private var selected: Set<String> = emptySet()
    private val session = VolumeFocusSession()
    private val owner get() = session.owner

    fun install(loader: ClassLoader) {
        val focus = Class.forName("com.android.server.audio.FocusRequester", false, loader)
        val service = Class.forName("com.android.server.audio.AudioService", false, loader)
        val pkg = AppWindowBehaviorHooks.field(focus, "mPackageName")
        val uid = AppWindowBehaviorHooks.field(focus, "mCallingUid")
        val attrs = AppWindowBehaviorHooks.field(focus, "mAttributes")
        val gain = AppWindowBehaviorHooks.field(focus, "mFocusGainRequest")
        val manualFlags = AudioManager.FLAG_SHOW_UI or AppWindowBehaviorHooks.field(AudioManager::class.java, "FLAG_FROM_KEY").getInt(null)
        val handler = XposedHelpers.callStaticMethod(XposedHelpers.findClass("com.android.internal.os.BackgroundThread", loader), "getHandler") as Handler
        val runtime = VolumeRuntime(loader, service, handler)
        val handles = ArrayList<tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.CustomMethodUnhooker>()
        fun hook(name: String, types: Array<Class<*>>, callback: MethodHook) {
            handles.add(checkNotNull(ModuleHelper.hookMethod(AppWindowBehaviorHooks.method(focus, name, *types), callback)))
        }
        val gained = object : MethodHook() {
            override fun after(callback: AfterHookCallback) = ModuleHelper.guarded {
                if (callback.getThrowable() != null || callback.getArg(0) != AudioManager.AUDIOFOCUS_GAIN) return@guarded
                if (selected.isEmpty()) return@guarded
                val value = callback.getThisObject() ?: return@guarded
                val packageName = pkg.get(value) as String
                val callingUid = uid.getInt(value)
                val usage = (attrs.get(value) as AudioAttributes).usage
                val accepted = packageName in selected && callingUid / 100000 == 0 && gain.getInt(value) == AudioManager.AUDIOFOCUS_GAIN &&
                    (usage == AudioAttributes.USAGE_MEDIA || usage == AudioAttributes.USAGE_GAME)
                val next = session.gained(value, packageName, callingUid, accepted) ?: return@guarded
                val version = session.version
                handler.post { ModuleHelper.guarded {
                    if (session.canRestore(next, version) && next.pkg in selected) runtime.restore(next, version)
                } }
            }
        }
        try {
            hook("handleFocusGainFromRequest", arrayOf(Int::class.javaPrimitiveType!!), gained)
            hook("handleFocusGain", arrayOf(Int::class.javaPrimitiveType!!), gained)
            val lost = object : MethodHook() {
                override fun after(callback: AfterHookCallback) = ModuleHelper.guarded {
                    if (callback.getThrowable() == null) session.released(callback.getThisObject())
                }
            }
            hook("handleFocusLoss", arrayOf(Int::class.javaPrimitiveType!!, focus, Boolean::class.javaPrimitiveType!!), lost)
            hook("release", emptyArray(), lost)
            handles.add(checkNotNull(ModuleHelper.hookMethod(AppWindowBehaviorHooks.method(service, "sendVolumeUpdate",
                *Array(5) { Int::class.javaPrimitiveType!! }), object : MethodHook() {
                override fun after(callback: AfterHookCallback) = ModuleHelper.guarded {
                    if (callback.getThrowable() != null || callback.getArg(0) != AudioManager.STREAM_MUSIC ||
                        callback.getArg(1) == callback.getArg(2) || (callback.getArg(3) as Int and manualFlags) == 0) return@guarded
                    val current = owner ?: return@guarded
                    session.manuallyChanged() // Supersedes even an already queued focus restore.
                    val device = callback.getArg(4) as Int
                    handler.post { ModuleHelper.guarded {
                        if (owner === current && current.pkg in selected) runtime.remember(current, device)
                    } }
                }
            })))
        } catch (error: Throwable) {
            handles.asReversed().forEach { ModuleHelper.guarded { it.unhook() } }
            throw error
        }
        selected = AppBehaviorPolicy.read(MainModule.mPrefs).volume
        ModuleHelper.observePreferenceChange(object : ModuleHelper.PreferenceObserver {
            override fun onChange(key: String?) {
                if (key != null && key != AppBehaviorPolicy.PREFIX + "volume" && key != AppBehaviorPolicy.PREFIX + "volume_apps") return
                handler.post { ModuleHelper.guarded {
                    selected = AppBehaviorPolicy.read(MainModule.mPrefs).volume
                    session.configure(selected)
                } }
            }
        })
    }

    private class VolumeRuntime(loader: ClassLoader, private val service: Class<*>, private val handler: Handler) {
        private val getService = AppWindowBehaviorHooks.method(Class.forName("android.os.ServiceManager", false, loader), "getService", String::class.java)
        private val deviceForStream = AppWindowBehaviorHooks.method(service, "getDeviceForStream", Int::class.javaPrimitiveType!!)
        private val currentUser = AppWindowBehaviorHooks.method(android.app.ActivityManager::class.java, "getCurrentUser")
        private val playback = Class.forName("android.media.AudioPlaybackConfiguration", false, loader)
        private val active = AppWindowBehaviorHooks.method(playback, "isActive")
        private val clientUid = AppWindowBehaviorHooks.method(playback, "getClientUid")
        private var context: Context? = null
        private var manager: AudioManager? = null
        private var audio: Any? = null
        private var loaded = false
        private val memory = VolumeMemory()
        private val persist = Runnable { ModuleHelper.guarded { identity {
            val ctx = context ?: return@identity
            check(Settings.Secure.putString(ctx.contentResolver, STORE, memory.encode()))
        } } }

        private fun ready(target: VolumeFocusSession.Owner): Boolean {
            if (owner !== target || target.requester.get() == null || target.pkg !in selected || currentUser.invoke(null) != 0) return false
            val ctx = context ?: ContextResolver.findContext(null)?.also { context = it } ?: return false
            val am = manager ?: ctx.getSystemService(AudioManager::class.java)?.also { manager = it } ?: return false
            if (audio == null) audio = getService.invoke(null, "audio")?.takeIf(service::isInstance)
            if (audio == null || am.mode != AudioManager.MODE_NORMAL || am.isVolumeFixed) return false
            if (!loaded) {
                memory.decode(Settings.Secure.getString(ctx.contentResolver, STORE).orEmpty())
                loaded = true
            }
            return true
        }

        private fun otherMedia(target: VolumeFocusSession.Owner): Boolean = manager!!.activePlaybackConfigurations.any {
            active.invoke(it) == true && clientUid.invoke(it) != target.uid &&
                (it.audioAttributes.usage == AudioAttributes.USAGE_MEDIA || it.audioAttributes.usage == AudioAttributes.USAGE_GAME)
        }

        fun restore(target: VolumeFocusSession.Owner, version: Long, retry: Boolean = true): Unit = identity {
            if (!session.canRestore(target, version) || !ready(target)) return@identity
            // Native multi-audio focus may allow several media owners. Leave their shared stream alone.
            if (otherMedia(target)) {
                // Allow one finite focus handoff; never keep checking background playback.
                if (retry) handler.postDelayed({ ModuleHelper.guarded { restore(target, version, false) } }, 250L)
                return@identity
            }
            val am = manager ?: return@identity
            val route = deviceForStream.invoke(audio, AudioManager.STREAM_MUSIC) as Int
            if (route == 0) return@identity
            val saved = memory.get(target.pkg, route) ?: return@identity
            val actual = am.getStreamVolume(AudioManager.STREAM_MUSIC)
            val level = saved.coerceIn(am.getStreamMinVolume(AudioManager.STREAM_MUSIC), am.getStreamMaxVolume(AudioManager.STREAM_MUSIC))
            if (level != actual && session.canRestore(target, version) && target.pkg in selected &&
                deviceForStream.invoke(audio, AudioManager.STREAM_MUSIC) == route) {
                am.setStreamVolume(AudioManager.STREAM_MUSIC, level, 0) // Native safe-volume/DND/device policy stays authoritative.
            }
        }

        fun remember(target: VolumeFocusSession.Owner, route: Int) = identity {
            if (!ready(target) || otherMedia(target) || route == 0 || deviceForStream.invoke(audio, AudioManager.STREAM_MUSIC) != route) return@identity
            val value = manager!!.getStreamVolume(AudioManager.STREAM_MUSIC)
            if (memory.put(target.pkg, route, value)) {
                handler.removeCallbacks(persist)
                handler.postDelayed(persist, 1000L) // Finite coalesced write; no polling or dedicated thread.
            }
        }

        private inline fun <T> identity(block: () -> T): T {
            val token = Binder.clearCallingIdentity()
            try { return block() } finally { Binder.restoreCallingIdentity(token) }
        }
    }
}

/** Supersession tokens keep delayed work from crossing focus owners or manual intervention. */
internal class VolumeFocusSession {
    class Owner(val requester: WeakReference<Any>, val pkg: String, val uid: Int)
    @Volatile var owner: Owner? = null
        private set
    private val manualVersion = AtomicLong()
    val version get() = manualVersion.get()

    fun gained(requester: Any, pkg: String, uid: Int, accepted: Boolean): Owner? {
        if (owner?.requester?.get() === requester) return null
        owner = null
        if (!accepted) return null
        return Owner(WeakReference(requester), pkg, uid).also { owner = it }
    }
    fun released(requester: Any?) { if (owner?.requester?.get() === requester) owner = null }
    fun configure(selected: Set<String>) { if (owner?.pkg !in selected) owner = null }
    fun manuallyChanged() { manualVersion.incrementAndGet() }
    fun canRestore(target: Owner, version: Long): Boolean = owner === target && target.requester.get() != null && this.version == version
}

/** Bounded by 256 package/route entries. The store is private to system_server's primary user. */
internal class VolumeMemory {
    private val entries = LinkedHashMap<String, Int>()
    fun get(pkg: String, route: Int): Int? = entries["$pkg@$route"]
    fun put(pkg: String, route: Int, value: Int): Boolean {
        val key = "$pkg@$route"
        if (entries[key] == value) return false
        entries.remove(key)
        entries[key] = value
        if (entries.size > 256) entries.remove(entries.keys.first())
        return true
    }
    fun encode(): String = entries.entries.joinToString(";") { "${it.key}=${it.value}" }
    fun decode(value: String) {
        entries.clear()
        value.take(32768).split(';').takeLast(256).forEach { entry ->
            val at = entry.lastIndexOf('=')
            if (at > 0 && '@' in entry.substring(0, at)) entry.substring(at + 1).toIntOrNull()?.takeIf { it in 0..1000 }?.let {
                entries[entry.substring(0, at)] = it
            }
        }
    }
}
