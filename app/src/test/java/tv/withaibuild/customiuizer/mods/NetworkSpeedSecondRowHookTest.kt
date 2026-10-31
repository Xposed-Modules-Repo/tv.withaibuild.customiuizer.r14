package tv.withaibuild.customiuizer.mods

import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Executable
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import tv.withaibuild.customiuizer.mods.utils.StatusBarNetworkSpeedDispatcher.LatestDelivery
import tv.withaibuild.customiuizer.mods.utils.StatusBarNetworkSpeedDispatcher.NetworkSpeedPayload

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = android.app.Application::class)
class NetworkSpeedSecondRowHookTest {
    class NativeState {
        @JvmField var networkSpeedNumber = ""
        @JvmField var networkSpeedUnit = "KB/s"
        @JvmField var visible = false
    }

    @Test fun nativeCallsRemainExactOnceWhileThirtyQueuedMirrorUpdatesBecomeOne() {
        val tasks = ArrayDeque<Runnable>()
        val applied = mutableListOf<NetworkSpeedPayload>()
        val hook = SystemUIStatusBarHooks.createNetSpeedSecondRowHook(LatestDelivery({ tasks.addLast(it); true }) { p, _ -> applied += p })
        val state = NativeState()
        repeat(30) { index ->
            val chain = Chain(state) { state.networkSpeedNumber = "$index"; state.visible = index != 0 }
            assertSame(chain.result, hook.intercept(chain))
            assertEquals(1, chain.nativeCalls)
            assertEquals(1, chain.argumentReads)
        }
        assertEquals(1, tasks.size)
        tasks.removeFirst().run()
        assertEquals(listOf(NetworkSpeedPayload("29", "KB/s", true)), applied)
        assertTrue(tasks.isEmpty())
    }

    @Test fun nativeFailureDoesNotReadUnfinishedStateOrScheduleMirrorWork() {
        for (failure in listOf(IllegalStateException("native setter"), OutOfMemoryError("native fatal"))) {
            var posts = 0
            val hook = SystemUIStatusBarHooks.createNetSpeedSecondRowHook(LatestDelivery({ posts++; true }) { _, _ -> fail("no delivery") })
            val chain = Chain(NativeState()) { throw failure }
            assertSame(failure, assertThrows(Throwable::class.java) { hook.intercept(chain) })
            assertEquals(1, chain.nativeCalls)
            assertEquals(0, chain.argumentReads)
            assertEquals(0, posts)
        }
    }

    @Test fun steadyUpdatesPreserveNumberUnitAndControllerVisibilityIncludingHiddenValues() {
        val tasks = ArrayDeque<Runnable>()
        val applied = mutableListOf<NetworkSpeedPayload>()
        val hook = SystemUIStatusBarHooks.createNetSpeedSecondRowHook(LatestDelivery({ tasks.addLast(it); true }) { p, _ -> applied += p })
        val state = NativeState()
        val values = listOf(NetworkSpeedPayload("1", "KB/s", true), NetworkSpeedPayload("2", "MB/s", false), NetworkSpeedPayload("2", "MB/s", true))
        for (value in values) {
            val chain = Chain(state) {
                state.networkSpeedNumber = value.number as String
                state.networkSpeedUnit = value.unit as String
                state.visible = value.visible as Boolean
            }
            hook.intercept(chain)
            assertEquals(1, chain.nativeCalls)
            tasks.removeFirst().run()
            assertTrue(tasks.isEmpty())
        }
        assertEquals(values, applied)
    }

    private class Chain(private val state: Any, private val native: () -> Unit) : XposedInterface.Chain {
        val result = Any()
        var nativeCalls = 0; var argumentReads = 0
        override fun getExecutable(): Executable = error("unused")
        override fun getThisObject(): Any = error("owner must not be captured")
        override fun getArgs(): List<Any?> = error("arguments must not be copied")
        override fun getArg(index: Int): Any { assertEquals(0, index); argumentReads++; return state }
        override fun proceed(): Any { nativeCalls++; native(); return result }
        override fun proceed(args: Array<Any?>): Any? = error("unused")
        override fun proceedWith(receiver: Any): Any? = error("unused")
        override fun proceedWith(receiver: Any, args: Array<Any>): Any? = error("unused")
    }
}
