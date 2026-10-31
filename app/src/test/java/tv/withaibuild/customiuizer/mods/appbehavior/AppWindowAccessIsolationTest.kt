package tv.withaibuild.customiuizer.mods.appbehavior

import android.os.Handler
import android.os.Looper
import io.github.libxposed.api.XposedInterface
import org.junit.Assert.*
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TemporaryFolder
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.PreferenceObserverRegistry
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import java.net.URLClassLoader
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Method
import java.lang.reflect.Proxy
import javax.tools.ToolProvider

/** Real reflective preparation against deliberately incomplete, isolated ROM declarations. */
@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class AppWindowAccessIsolationTest {
    @get:Rule val temporary = TemporaryFolder()

    private fun fixture(rotationOnly: Boolean, block: (ClassLoader) -> Unit) {
        val directory = temporary.newFolder()
        val common = "public String packageName; public int mUserId; public boolean inMultiWindowMode(){return false;} public int getDisplayId(){return 0;}"
        val sources = linkedMapOf(
            "ActivityRecord" to (common + if (rotationOnly) "public Object mWmService; public int getOrientation(int x){return x;}" else "public Object info;"),
            "WindowManagerService" to ("public Object mGlobalLock = new Object(); public void requestTraversal(){}" +
                if (rotationOnly) "public void updateRotation(boolean a, boolean b){}" else ""),
        )
        if (!rotationOnly) {
            sources["WindowState"] = "public Object mActivityRecord; public Object mAttrs;"
            sources["RootWindowContainer"] = "public Object mWmService; public boolean handleNotObscuredLocked(WindowState w, boolean a, boolean b){return true;}"
        }
        val files = sources.map { (name, body) ->
            directory.resolve("$name.java").apply {
                writeText("package com.android.server.wm; public class $name { $body }")
            }
        } + directory.resolve("BackgroundThread.java").apply {
            writeText("package com.android.internal.os; public class BackgroundThread { public static Object handler; public static Object getHandler(){return handler;} }")
        }
        val compiler = checkNotNull(ToolProvider.getSystemJavaCompiler())
        assertEquals(0, compiler.run(null, null, null, "-proc:none", "-d", directory.path,
            *files.map { it.path }.toTypedArray()))
        object : URLClassLoader(arrayOf(directory.toURI().toURL()), javaClass.classLoader) {
            override fun loadClass(name: String, resolve: Boolean): Class<*> {
                if (!name.startsWith("com.android.server.wm.") && name != "com.android.internal.os.BackgroundThread")
                    return super.loadClass(name, resolve)
                synchronized(this) {
                    val loaded = findLoadedClass(name) ?: findClass(name)
                    if (resolve) resolveClass(loaded)
                    return loaded
                }
            }
        }.use { loader ->
            loader.loadClass("com.android.internal.os.BackgroundThread").getField("handler").set(null, Handler(Looper.getMainLooper()))
            block(loader)
        }
    }

    private fun verify(rotationOnly: Boolean) = fixture(rotationOnly) { loader ->
        val type = AppWindowBehaviorHooks::class.java.declaredClasses.single { it.simpleName == "WindowAccess" }
        val commonType = AppWindowBehaviorHooks::class.java.declaredClasses.single { it.simpleName == "CommonAccess" }
        val common = commonType.getDeclaredConstructor(ClassLoader::class.java).apply { isAccessible = true }.newInstance(loader)
        val constructor = type.getDeclaredConstructor(ClassLoader::class.java, Boolean::class.javaPrimitiveType, commonType)
            .apply { isAccessible = true }
        val access = constructor.newInstance(loader, rotationOnly, common)
        for (name in listOf("orientation", "recordWm"))
            assertEquals(rotationOnly, type.getDeclaredField(name).apply { isAccessible = true }.get(access) != null)
        for (name in listOf("activity", "attrs", "handleWindow", "info"))
            assertEquals(!rotationOnly, type.getDeclaredField(name).apply { isAccessible = true }.get(access) != null)
        val failure = assertThrows(InvocationTargetException::class.java) { constructor.newInstance(loader, !rotationOnly, common) }
        assertTrue(failure.cause is ReflectiveOperationException)
        // Failure preparing the other feature does not poison this feature's interfaces.
        assertNotNull(constructor.newInstance(loader, rotationOnly, common))
    }

    @Test fun rotationDoesNotResolveAnyBrightnessWindowApi() = verify(true)
    @Test fun brightnessDoesNotResolveAnyRotationApi() = verify(false)

    private fun installWithMissingOtherGroup(rotationOnly: Boolean) = fixture(rotationOnly) { loader ->
        val previous = XposedHelpers.moduleInst
        val prefs = MainModule.mPrefs.getAll()
        val observers = PreferenceObserverRegistry.observers.toSet()
        val installed = ArrayList<Method>()
        try {
            MainModule.mPrefs.clear()
            MainModule.mPrefs.put(AppBehaviorPolicy.PREFIX + "rotation", true)
            MainModule.mPrefs.put(AppBehaviorPolicy.PREFIX + "brightness", true)
            XposedHelpers.moduleInst = Proxy.newProxyInstance(XposedInterface::class.java.classLoader,
                arrayOf(XposedInterface::class.java)) { _, call, args ->
                when (call.name) {
                    "log" -> null
                    "hook" -> object : XposedInterface.HookBuilder {
                        override fun setPriority(priority: Int) = this
                        override fun setExceptionMode(mode: XposedInterface.ExceptionMode) = this
                        override fun setId(id: String?): XposedInterface.HookBuilder = error("No new stable ID")
                        override fun intercept(hooker: XposedInterface.Hooker): XposedInterface.HookHandle {
                            installed.add(args!![0] as Method)
                            return Proxy.newProxyInstance(XposedInterface.HookHandle::class.java.classLoader,
                                arrayOf(XposedInterface.HookHandle::class.java)) { _, method, _ ->
                                error("The surviving Hook must not be retired: ${method.name}")
                            } as XposedInterface.HookHandle
                        }
                    }
                    else -> error(call.name)
                }
            } as XposedInterface
            AppWindowBehaviorHooks.install(loader)
            assertEquals(listOf(if (rotationOnly) "getOrientation" else "handleNotObscuredLocked"), installed.map { it.name })
            assertEquals(1, (PreferenceObserverRegistry.observers - observers).size)
        } finally {
            PreferenceObserverRegistry.observers.retainAll(observers)
            MainModule.mPrefs.replaceSnapshot(prefs)
            XposedHelpers.moduleInst = previous
        }
    }

    @Test fun missingBrightnessApiStillInstallsTheEnabledRotationHook() = installWithMissingOtherGroup(true)
    @Test fun missingRotationApiStillInstallsTheEnabledBrightnessHook() = installWithMissingOtherGroup(false)
}
