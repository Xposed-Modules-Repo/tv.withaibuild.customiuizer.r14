package tv.withaibuild.customiuizer.mods

import io.github.libxposed.api.XposedInterface
import org.junit.After
import org.junit.Before
import org.junit.Assert.*
import org.junit.Test
import java.lang.reflect.Executable

class LockAuthenticationCommitTest {
    private val names = listOf("isUnlockedWithStrong", "isUnlockedWithFingerprint", "isUnlockedInnerCall")
    private lateinit var saved: List<Boolean>
    private val savedConfig = SystemLockScreenHooks.noScreenLockConfig
    private fun get(name: String) = field(name).getBoolean(SystemLockScreenHooks)
    private fun set(name: String, value: Boolean) = field(name).setBoolean(SystemLockScreenHooks, value)
    private fun field(name: String) = SystemLockScreenHooks::class.java.getDeclaredField(name).apply { isAccessible = true }
    @Before fun before() { saved = names.map(::get); names.forEach { set(it, false) } }
    @After fun after() { names.zip(saved).forEach { (name, value) -> set(name, value) }; SystemLockScreenHooks.noScreenLockConfig = savedConfig }
    private fun authAllowed(req: Int): Boolean {
        SystemLockScreenHooks.noScreenLockConfig = savedConfig.copy(req = req)
        return SystemLockScreenHooks::class.java.getDeclaredMethod("isAuthOnce").apply { isAccessible = true }.invoke(SystemLockScreenHooks) as Boolean
    }
    @Test fun failedNativeDoneCannotSatisfyTheStrongAuthenticationRequirement() {
        val failure = IllegalStateException("native done failed"); val chain = Chain(failure)
        assertSame(failure, assertThrows(IllegalStateException::class.java) { SystemLockScreenHooks.keyguardDoneAuthenticationHook().intercept(chain) })
        assertEquals(1, chain.calls); assertFalse(get("isUnlockedWithStrong")); assertFalse(authAllowed(3))
    }
    @Test fun failedNativeFingerprintCannotSatisfyTheFirstAuthenticationRequirement() {
        val failure = IllegalStateException("native fingerprint failed"); val chain = Chain(failure)
        assertSame(failure, assertThrows(IllegalStateException::class.java) { SystemLockScreenHooks.fingerprintAuthenticationHook().intercept(chain) })
        assertEquals(1, chain.calls); assertFalse(get("isUnlockedWithFingerprint")); assertFalse(authAllowed(2))
    }
    @Test fun nativeFatalDoneCannotCommitAuthentication() {
        val failure = OutOfMemoryError("native done fatal"); val chain = Chain(failure)
        assertSame(failure, assertThrows(OutOfMemoryError::class.java) { SystemLockScreenHooks.keyguardDoneAuthenticationHook().intercept(chain) })
        assertEquals(1, chain.calls); assertFalse(authAllowed(3))
    }
    @Test fun nativeFatalFingerprintCannotCommitAuthentication() {
        val failure = OutOfMemoryError("native fingerprint fatal"); val chain = Chain(failure)
        assertSame(failure, assertThrows(OutOfMemoryError::class.java) { SystemLockScreenHooks.fingerprintAuthenticationHook().intercept(chain) })
        assertEquals(1, chain.calls); assertFalse(authAllowed(2))
    }
    @Test fun failedInternalDonePreservesItsPendingMarkerAndExistingGrant() {
        set("isUnlockedInnerCall", true); set("isUnlockedWithStrong", true)
        val failure = IllegalStateException("native done failed"); val chain = Chain(failure)
        assertSame(failure, assertThrows(IllegalStateException::class.java) { SystemLockScreenHooks.keyguardDoneAuthenticationHook().intercept(chain) })
        assertEquals(1, chain.calls); assertTrue(get("isUnlockedInnerCall")); assertTrue(get("isUnlockedWithStrong"))
    }
    @Test fun successfulNativeDoneRetainsItsResultAndExistingStrongPolicy() {
        val chain = Chain(); assertSame(chain.result, SystemLockScreenHooks.keyguardDoneAuthenticationHook().intercept(chain))
        assertEquals(1, chain.calls); assertTrue(authAllowed(3))
    }
    @Test fun successfulFingerprintRetainsItsResultAndOnlyItsExistingGrant() {
        val chain = Chain(); assertSame(chain.result, SystemLockScreenHooks.fingerprintAuthenticationHook().intercept(chain))
        assertEquals(1, chain.calls); assertTrue(authAllowed(2)); assertFalse(authAllowed(3))
    }
    @Test fun successfulInternalDoneConsumesMarkerWithoutGrantingStrongAuthentication() {
        set("isUnlockedInnerCall", true); val chain = Chain()
        assertSame(chain.result, SystemLockScreenHooks.keyguardDoneAuthenticationHook().intercept(chain))
        assertEquals(1, chain.calls); assertFalse(get("isUnlockedInnerCall")); assertFalse(authAllowed(3))
    }
    private class Chain(val failure: Throwable? = null) : XposedInterface.Chain {
        val result = Any(); var calls = 0
        override fun getExecutable(): Executable = error("No reflective method access")
        override fun getThisObject(): Any = error("No owner access required")
        override fun getArgs(): List<Any?> = error("No argument copies required")
        override fun getArg(index: Int): Any? = error("No argument reads required")
        override fun proceed(): Any? { calls++; failure?.let { throw it }; return result }
        override fun proceed(args: Array<Any?>): Any? = error("No argument rewriting")
        override fun proceedWith(receiver: Any): Any? = error("No receiver rewriting")
        override fun proceedWith(receiver: Any, args: Array<Any>): Any? = error("No receiver rewriting")
    }
}
