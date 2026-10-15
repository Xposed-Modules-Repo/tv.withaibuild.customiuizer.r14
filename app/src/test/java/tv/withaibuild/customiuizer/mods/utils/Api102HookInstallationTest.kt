package tv.withaibuild.customiuizer.mods.utils

import io.github.libxposed.api.XposedInterface
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import java.lang.reflect.InvocationTargetException
import java.lang.reflect.Proxy

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34])
class Api102HookInstallationTest {
    private val target = Target::class.java.getDeclaredMethod("init", Boolean::class.javaPrimitiveType)
    private val callback = object : HookerClassHelper.MethodHook(73) {}
    private var previousModule: XposedInterface? = null

    @Before
    fun prepare() {
        previousModule = XposedHelpers.moduleInst
        XposedApiCapabilities.initialize(101)
    }

    @After
    fun restore() {
        XposedHelpers.moduleInst = previousModule
        XposedApiCapabilities.initialize(101)
    }

    @Test
    fun api101InstallsThroughLegacyBuilderWithoutCallingSetId() {
        val builder = bind(101)
        builder.idFailure = AbstractMethodError("API 101 has no setId")
        val unhooker = install()
        assertNotNull(unhooker)
        assertEquals(listOf("priority", "mode", "intercept"), builder.calls)
        assertNull(builder.id)
        assertSame(callback, builder.callback)
        unhooker!!.unhook()
        assertEquals(1, builder.unhookCalls)
    }

    @Test
    fun api102PreservesPriorityModeCallbackAndUnhookWithStableId() {
        val builder = bind(102)
        val unhooker = install()
        assertNotNull(unhooker)
        assertEquals(listOf("priority", "mode", "id", "intercept"), builder.calls)
        assertEquals(73, builder.priority)
        assertSame(XposedInterface.ExceptionMode.PASSTHROUGH, builder.mode)
        assertEquals("systemui.init", builder.id)
        assertSame(callback, builder.callback)
        unhooker!!.unhook()
        assertEquals(1, builder.unhookCalls)
    }

    @Test
    fun missingOptionalApiFallsBackBeforeSingleIntercept() {
        val builder = bind(102)
        builder.idFailure = AbstractMethodError("missing implementation")
        assertNotNull(install())
        assertEquals(1, builder.calls.count { it == "id" })
        assertEquals(1, builder.calls.count { it == "intercept" })
        assertNull(builder.id)
        assertSame(callback, builder.callback)
    }

    @Test
    fun ordinaryIdFailureStillInstallsOriginalCallbackOnce() {
        val builder = bind(102)
        builder.idFailure = IllegalArgumentException("unsupported ID")
        assertNotNull(install())
        assertEquals(1, builder.calls.count { it == "intercept" })
        assertSame(callback, builder.callback)
    }

    @Test
    fun interceptFailureIsNotRetried() {
        val builder = bind(102)
        builder.installFailure = IllegalStateException("installation failed")
        assertNull(install())
        assertEquals(1, builder.calls.count { it == "intercept" })
    }

    @Test
    fun fatalIdErrorsPreventInstallationAndPropagate() {
        for (failure in listOf(OutOfMemoryError("test"), ThreadDeath(), StackOverflowError("test"))) {
            val builder = bind(102)
            builder.idFailure = failure
            try {
                install()
                fail("Fatal error must propagate")
            } catch (actual: Throwable) {
                assertSame(failure, actual)
            }
            assertFalse(builder.calls.contains("intercept"))
        }
    }

    @Test
    fun wrappedFatalIdErrorIsUnwrappedBeforeFallback() {
        val builder = bind(102)
        val failure = OutOfMemoryError("wrapped test")
        builder.idFailure = InvocationTargetException(failure)
        try {
            install()
            fail("Wrapped fatal error must propagate")
        } catch (actual: Throwable) {
            assertSame(failure, actual)
        }
        assertFalse(builder.calls.contains("intercept"))
    }

    private fun install() = HookInstallerFacade.findAndHookMethodWithId(
        Target::class.java.name, Target::class.java.classLoader, "init", "systemui.init",
        Boolean::class.javaPrimitiveType, callback,
    )

    private fun bind(api: Int): Builder {
        XposedApiCapabilities.initialize(api)
        val builder = Builder()
        val framework = Proxy.newProxyInstance(
            XposedInterface::class.java.classLoader, arrayOf(XposedInterface::class.java),
        ) { _, method, args ->
            when (method.name) {
                "hook" -> {
                    assertEquals(target, args!![0])
                    builder
                }
                "getApiVersion" -> api
                else -> throw UnsupportedOperationException(method.name)
            }
        }
        XposedHelpers.moduleInst = framework as XposedInterface
        return builder
    }

    private inner class Builder : XposedInterface.HookBuilder {
        val calls = ArrayList<String>()
        var priority = 0
        var mode: XposedInterface.ExceptionMode? = null
        var id: String? = null
        var callback: XposedInterface.Hooker? = null
        var idFailure: Throwable? = null
        var installFailure: Throwable? = null
        var unhookCalls = 0
        private val handle = Proxy.newProxyInstance(
            XposedInterface.HookHandle::class.java.classLoader, arrayOf(XposedInterface.HookHandle::class.java),
        ) { _, method, _ ->
            when (method.name) {
                "unhook" -> { unhookCalls++; null }
                "getExecutable" -> target
                "getId" -> id
                else -> throw UnsupportedOperationException(method.name)
            }
        } as XposedInterface.HookHandle

        override fun setPriority(priority: Int): XposedInterface.HookBuilder {
            calls.add("priority")
            this.priority = priority
            return this
        }

        override fun setExceptionMode(mode: XposedInterface.ExceptionMode): XposedInterface.HookBuilder {
            calls.add("mode")
            this.mode = mode
            return this
        }

        override fun setId(id: String?): XposedInterface.HookBuilder {
            calls.add("id")
            idFailure?.let { throw it }
            this.id = id
            return this
        }

        override fun intercept(hooker: XposedInterface.Hooker): XposedInterface.HookHandle {
            calls.add("intercept")
            installFailure?.let { throw it }
            callback = hooker
            return handle
        }
    }

    private class Target { fun init(@Suppress("UNUSED_PARAMETER") restarting: Boolean) = Unit }
}
