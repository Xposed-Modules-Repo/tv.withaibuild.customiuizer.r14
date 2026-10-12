package tv.withaibuild.customiuizer.mods

import io.github.libxposed.api.XposedInterface
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertSame
import org.junit.Assert.assertTrue
import org.junit.Test
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException

class NavBarHomeLongClickGuardTest {
    private val savedConfig = Controls.controlsConfig
    private val pinning = field("mScreenPinningActive")
    private val disabledFlags = field("mDisabledFlags1")
    private val provisionedController = field("mDeviceProvisionedController")
    private val isDeviceProvisioned = Controller::class.java.getDeclaredMethod("isDeviceProvisioned").apply {
        isAccessible = true
    }

    @After
    fun restoreConfig() { Controls.controlsConfig = savedConfig }

    @Test
    fun homeOnlyDoesNotEnableExtraButtons() {
        assertFalse(Controls.hasExtraNavBarButtons(Controls.ControlsSnapshot(homeLongAction = 2)))
        assertTrue(Controls.hasExtraNavBarButtons(Controls.ControlsSnapshot(navbarRightLongAction = 2)))
    }

    @Test
    fun missingHomeAbiStillAllowsExistingExtraButtonsToInstall() {
        val config = Controls.ControlsSnapshot(homeLongAction = 2, navbarRightLongAction = 2)
        assertTrue(Controls.prepareNavBarButtons(config) { throw NoSuchMethodException("HOME ABI missing") })
        assertFalse(Controls.prepareNavBarButtons(Controls.ControlsSnapshot(homeLongAction = 2)) {
            throw NoSuchMethodException("HOME ABI missing")
        })
    }

    @Test
    fun disabledHomeDoesNotAttemptGuardInstallation() {
        assertTrue(Controls.prepareNavBarButtons(Controls.ControlsSnapshot(navbarRightLongAction = 2)) {
            error("disabled HOME guard installed")
        })
    }

    @Test
    fun fatalGuardInstallationErrorIsNotConvertedIntoAnExtraButtonFallback() {
        val fatal = OutOfMemoryError("guard OOM")
        try {
            Controls.prepareNavBarButtons(Controls.ControlsSnapshot(homeLongAction = 2, navbarRightAction = 2)) {
                throw InvocationTargetException(fatal)
            }
            throw AssertionError("wrapped fatal error was swallowed")
        } catch (actual: Throwable) {
            assertSame(fatal, actual)
        }
    }

    @Test
    fun configuredHomeSkipsTheSecondAssistantAndDisablingRestoresTheSameOriginal() {
        val chain = FakeChain(NavigationBar())
        Controls.controlsConfig = Controls.ControlsSnapshot(homeLongAction = 2)
        assertEquals(true, intercept(chain))
        assertEquals(0, chain.proceedCount)
        Controls.controlsConfig = Controls.ControlsSnapshot(homeLongAction = 1)
        assertEquals(false, intercept(chain))
        assertEquals(1, chain.proceedCount)
    }

    @Test
    fun disabledHomeDoesNotReadTheOwner() {
        Controls.controlsConfig = Controls.ControlsSnapshot()
        val chain = FakeChain(ownerFailure = AssertionError("disabled home accessed owner"))
        assertEquals(false, intercept(chain))
        assertEquals(1, chain.proceedCount)
    }

    @Test
    fun screenPinningDelegatesToTheNativeWithoutInspectingSetup() {
        Controls.controlsConfig = Controls.ControlsSnapshot(homeLongAction = 2)
        val controller = Controller(failure = AssertionError("pinning inspected setup"))
        val chain = FakeChain(NavigationBar(pinning = true, controller = controller))
        assertEquals(false, intercept(chain))
        assertEquals(1, chain.proceedCount)
        assertEquals(0, controller.calls)
    }

    @Test
    fun disabledSearchAndIncompleteSetupKeepTheirNativeRestrictions() {
        Controls.controlsConfig = Controls.ControlsSnapshot(homeLongAction = 2)
        for (owner in listOf(NavigationBar(flags = 0x02000000), NavigationBar(controller = Controller(false)))) {
            val chain = FakeChain(owner)
            assertEquals(false, intercept(chain))
            assertEquals(1, chain.proceedCount)
        }
    }

    @Test
    fun ordinaryControllerFailureProceedsOnce() {
        Controls.controlsConfig = Controls.ControlsSnapshot(homeLongAction = 2)
        val chain = FakeChain(NavigationBar(controller = Controller(failure = IllegalStateException("setup failure"))))
        assertEquals(false, intercept(chain))
        assertEquals(1, chain.proceedCount)
    }

    @Test
    fun wrappedFatalControllerErrorsPropagateWithoutEnteringTheNative() {
        Controls.controlsConfig = Controls.ControlsSnapshot(homeLongAction = 2)
        for (fatal in listOf(OutOfMemoryError("oom"), ThreadDeath(), InternalError("vm"))) {
            val chain = FakeChain(NavigationBar(controller = Controller(failure = fatal)))
            try {
                intercept(chain)
                throw AssertionError("fatal error was swallowed")
            } catch (actual: Throwable) {
                assertSame(fatal, actual)
            }
            assertEquals(0, chain.proceedCount)
        }
    }

    @Test
    fun originalFailurePropagatesExactlyOnce() {
        Controls.controlsConfig = Controls.ControlsSnapshot()
        val failure = IllegalStateException("native failure")
        val chain = FakeChain(proceedFailure = failure)
        try {
            intercept(chain)
            throw AssertionError("native error was swallowed")
        } catch (actual: Throwable) {
            assertSame(failure, actual)
        }
        assertEquals(1, chain.proceedCount)
    }

    private fun field(name: String) = NavigationBar::class.java.getDeclaredField(name).apply { isAccessible = true }
    private fun intercept(chain: FakeChain): Any? = Controls.interceptNavBarHomeLongClick(
        chain, pinning, disabledFlags, provisionedController, isDeviceProvisioned,
    )

    private class NavigationBar(
        pinning: Boolean = false,
        flags: Int = 0,
        controller: Controller = Controller(),
    ) {
        @JvmField val mScreenPinningActive = pinning
        @JvmField val mDisabledFlags1 = flags
        @JvmField val mDeviceProvisionedController = controller
    }

    private class Controller(private val ready: Boolean = true, private val failure: Throwable? = null) {
        var calls = 0
        fun isDeviceProvisioned(): Boolean { calls++; failure?.let { throw it }; return ready }
    }

    private class FakeChain(
        private val owner: Any? = null,
        private val ownerFailure: Throwable? = null,
        private val proceedFailure: Throwable? = null,
    ) : XposedInterface.Chain {
        var proceedCount = 0
        override fun getExecutable(): Executable = error("not used")
        override fun getThisObject(): Any? { ownerFailure?.let { throw it }; return owner }
        override fun getArgs(): List<Any?> = emptyList()
        override fun getArg(index: Int): Any? = error("not used")
        override fun proceed(): Any? { proceedCount++; proceedFailure?.let { throw it }; return false }
        override fun proceed(args: Array<Any?>): Any? = error("not used")
        override fun proceedWith(p0: Any): Any? = error("not used")
        override fun proceedWith(p0: Any, p1: Array<Any>): Any? = error("not used")
    }
}
