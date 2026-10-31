package tv.withaibuild.customiuizer.mods

import android.app.Activity
import android.app.Application
import android.content.SharedPreferences
import android.os.Looper
import android.util.TypedValue
import android.widget.TextView
import io.github.libxposed.api.XposedInterface
import java.lang.reflect.Executable
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.Robolectric
import org.robolectric.RobolectricTestRunner
import org.robolectric.Shadows.shadowOf
import org.robolectric.android.controller.ActivityController
import org.robolectric.annotation.Config
import org.robolectric.annotation.LooperMode
import tv.withaibuild.customiuizer.MainModule
import tv.withaibuild.customiuizer.mods.utils.HookerClassHelper.MethodHook
import tv.withaibuild.customiuizer.mods.utils.PreferenceObserverRegistry
import tv.withaibuild.customiuizer.mods.utils.PreferenceBootstrap
import tv.withaibuild.customiuizer.mods.utils.XposedHelpers
import tv.withaibuild.customiuizer.utils.FakeSharedPreferences

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@LooperMode(LooperMode.Mode.PAUSED)
class ChargingInfoViewLifecycleTest {
    private lateinit var host: ActivityController<Activity>
    private lateinit var view: TextView
    private var savedPrefs: Map<String, Any> = emptyMap()

    @Before fun setUp() {
        savedPrefs = MainModule.mPrefs.getAll()
        MainModule.mPrefs.replaceSnapshot(customPrefs())
        host = Robolectric.buildActivity(Activity::class.java).setup().visible()
        view = TextView(host.get()).apply {
            setTextSize(TypedValue.COMPLEX_UNIT_PX, 17f)
            setSingleLine(true)
        }
        host.get().setContentView(view)
    }

    @After fun tearDown() {
        PreferenceObserverRegistry.unregisterPreferenceObserver(view)
        MainModule.mPrefs.replaceSnapshot(savedPrefs)
        shadowOf(Looper.getMainLooper()).idle()
        host.pause().stop().destroy()
    }

    @Test fun bulkResetRestoresTheExistingViewsNativeStyle() {
        inflate()
        assertEquals(customSize(20), view.textSize, 0.001f)
        assertFalse(view.isSingleLine)

        MainModule.mPrefs.replaceSnapshot(emptyMap<String, Any>())
        PreferenceObserverRegistry.handlePreferenceChanged(null)
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals("bulk reset must release the custom font", 17f, view.textSize, 0.001f)
        assertTrue("bulk reset must restore native single-line state", view.isSingleLine)
    }

    @Test fun bulkRestoreAppliesTheNewSnapshotWithoutAnotherNativeIndication() {
        inflate()
        MainModule.mPrefs.replaceSnapshot(customPrefs(size = 30, mode = "2"))
        PreferenceObserverRegistry.handlePreferenceChanged(null)
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(customSize(30), view.textSize, 0.001f)
        assertTrue(view.isSingleLine)
    }

    @Test fun bulkResetFlowsFromTheRegisteredRemoteListenerToTheExistingView() {
        val stored = FakeSharedPreferences().apply {
            for ((key, value) in customPrefs()) put("pref_key_" + key, value)
        }
        lateinit var listener: SharedPreferences.OnSharedPreferenceChangeListener
        val remote = object : SharedPreferences by stored {
            override fun registerOnSharedPreferenceChangeListener(next: SharedPreferences.OnSharedPreferenceChangeListener) {
                listener = next
            }
        }
        val bootstrap = PreferenceBootstrap.create(MainModule.mPrefs, { remote },
            PreferenceObserverRegistry::handlePreferenceChanged)
        assertTrue(bootstrap.bootstrap())
        inflate()

        stored.edit().clear().apply()
        listener.onSharedPreferenceChanged(remote, null)
        shadowOf(Looper.getMainLooper()).idle()

        assertFalse(MainModule.mPrefs.getBoolean("system_charginginfo"))
        assertEquals(17f, view.textSize, 0.001f)
        assertTrue(view.isSingleLine)
    }

    @Test fun failedInflationKeepsTheNativeFailureWithoutStylingOrRegisteringTheView() {
        val failure = IllegalStateException("native inflation failed")
        val chain = Chain(view) { throw failure }

        assertSame(failure, assertThrows(Throwable::class.java) {
            SystemLockScreenHooks.chargingInfoInflationHook().intercept(chain)
        })

        assertEquals(1, chain.calls)
        assertEquals(17f, view.textSize, 0.001f)
        assertTrue(view.isSingleLine)
        assertNull(XposedHelpers.getAdditionalInstanceField(view, "customiuizer_prefObserver"))
        assertNull(XposedHelpers.getAdditionalInstanceField(view, "charging_info_original_text_size"))
    }

    @Test fun failedIndicationKeepsTheNativeFailureAndItsPartialViewState() {
        inflate()
        val failure = IllegalStateException("native indication failed")
        val chain = Chain(view) {
            // The native call changed its appearance before failing; the module must leave it alone.
            view.setTextSize(TypedValue.COMPLEX_UNIT_PX, 19f)
            view.setSingleLine(true)
            throw failure
        }

        assertSame(failure, assertThrows(Throwable::class.java) {
            SystemLockScreenHooks.chargingInfoIndicationHook().intercept(chain)
        })

        assertEquals(1, chain.calls)
        assertEquals(19f, view.textSize, 0.001f)
        assertTrue(view.isSingleLine)
    }

    @Test fun successfulInflationRegistersOneOwnerAndSingleKeyChangesStillApply() {
        inflate()
        val observer = XposedHelpers.getAdditionalInstanceField(view, "customiuizer_prefObserver")
        assertNotNull(observer)
        MainModule.mPrefs.put("system_charginginfo_fontsize", 24)
        PreferenceObserverRegistry.handlePreferenceChanged("system_charginginfo_fontsize")
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(customSize(24), view.textSize, 0.001f)
        assertSame(observer, XposedHelpers.getAdditionalInstanceField(view, "customiuizer_prefObserver"))
    }

    @Test fun successfulIndicationReappliesTheCurrentStyleAfterNativeAppearance() {
        inflate()
        MainModule.mPrefs.put("system_charginginfo_fontsize", 26)
        val chain = Chain(view) { view.setTextSize(TypedValue.COMPLEX_UNIT_PX, 19f); null }

        assertNull(SystemLockScreenHooks.chargingInfoIndicationHook().intercept(chain))

        assertEquals(1, chain.calls)
        assertEquals(customSize(26), view.textSize, 0.001f)
    }

    @Test fun queuedPreferenceWorkReadsTheLatestSnapshot() {
        inflate()
        MainModule.mPrefs.put("system_charginginfo_fontsize", 30)
        PreferenceObserverRegistry.handlePreferenceChanged("system_charginginfo_fontsize")
        MainModule.mPrefs.replaceSnapshot(emptyMap<String, Any>())
        shadowOf(Looper.getMainLooper()).idle()

        assertEquals(17f, view.textSize, 0.001f)
        assertTrue(view.isSingleLine)
    }

    @Test fun fatalNativeFailuresEscapeBothHooksWithoutTouchingTheView() {
        for (hook: MethodHook in listOf(SystemLockScreenHooks.chargingInfoInflationHook(),
            SystemLockScreenHooks.chargingInfoIndicationHook())) {
            val failure = OutOfMemoryError("native fatal")
            val chain = Chain(view) { throw failure }
            assertSame(failure, assertThrows(OutOfMemoryError::class.java) { hook.intercept(chain) })
            assertEquals(1, chain.calls)
            assertEquals(17f, view.textSize, 0.001f)
            assertTrue(view.isSingleLine)
            assertNull(XposedHelpers.getAdditionalInstanceField(view, "customiuizer_prefObserver"))
        }
    }

    private fun inflate() {
        val chain = Chain(view) { null }
        assertNull(SystemLockScreenHooks.chargingInfoInflationHook().intercept(chain))
        assertEquals(1, chain.calls)
    }

    private fun customSize(raw: Int) = TypedValue.applyDimension(
        TypedValue.COMPLEX_UNIT_SP, raw / 2f, view.resources.displayMetrics)

    private fun customPrefs(size: Int = 20, mode: String = "1") = mapOf(
        "system_charginginfo" to true,
        "system_charginginfo_fontsize" to size,
        "system_charginginfo_view" to mode,
    )

    private class Chain(private val owner: TextView, private val native: () -> Any?) : XposedInterface.Chain {
        var calls = 0
        override fun getExecutable(): Executable = error("no metadata needed")
        override fun getThisObject(): Any = owner
        override fun getArgs(): List<Any?> = error("no argument copying needed")
        override fun getArg(index: Int): Any? = error("no arguments needed")
        override fun proceed(): Any? { calls++; return native() }
        override fun proceed(args: Array<Any?>): Any? = error("no rewritten arguments")
        override fun proceedWith(receiver: Any): Any? = error("unused")
        override fun proceedWith(receiver: Any, args: Array<Any>): Any? = error("unused")
    }
}
