package tv.withaibuild.customiuizer.mods

import android.app.Application
import android.content.Context
import android.content.ContextWrapper
import android.content.res.Resources
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import io.github.libxposed.api.XposedInterface
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.RuntimeEnvironment
import org.robolectric.annotation.Config
import org.robolectric.util.ReflectionHelpers
import tv.withaibuild.customiuizer.utils.HookUtils
import java.io.File
import java.lang.reflect.Executable
import java.lang.reflect.InvocationTargetException

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
class PinScrambleBoundaryTest {
    private val cache = ReflectionHelpers.getStaticField<MutableMap<String, Int>>(HookUtils::class.java, "resIdCache")
    private var savedIds = emptyMap<String, Int>()

    @Before fun prepare() { savedIds = cache.toMap(); cache.clear() }
    @After fun release() { cache.clear(); cache.putAll(savedIds) }

    @Test fun completeNativeKeypadRetainsAllDigitsAndFixedControls() {
        val pin = keypad()
        val before = snapshot(pin)
        val chain = Chain(pin)
        assertSame(chain.result, SystemLockScreenHooks.createScramblePinHook().intercept(chain))
        assertEquals(1, chain.calls)
        assertNotSame("The actual scramble callback was applied", before.rowArrays[1], pin.mViews[1])
        val moved = ArrayList<View>()
        for (row in 1..3) {
            assertEquals(3, pin.rows[row - 1].childCount)
            for (column in 0..2) {
                val key = pin.mViews[row][column]!!
                assertSame(key, pin.rows[row - 1].getChildAt(column))
                moved.add(key)
                assertTrue(key.performClick())
            }
        }
        assertSame(pin.mViews[4][1], pin.rows[3].getChildAt(1))
        moved.add(pin.mViews[4][1]!!)
        assertTrue(pin.mViews[4][1]!!.performClick())
        assertEquals(pin.digits.toSet(), moved.toSet())
        assertEquals(10, moved.toSet().size)
        assertEquals((0..9).toSet(), pin.clicks.toSet())
        assertSame(pin.spacer, pin.rows[3].getChildAt(0))
        assertSame(pin.delete, pin.rows[3].getChildAt(2))
        assertSame(pin.delete, pin.mViews[4][2])
    }

    @Test fun aNativeInflationFailureCannotReadOrMutateTheKeypad() {
        val pin = keypad(); val before = snapshot(pin)
        val failure = IllegalStateException("native inflation failed")
        val chain = Chain(pin, nativeFailure = failure)
        assertSame(failure, assertThrows(IllegalStateException::class.java) {
            SystemLockScreenHooks.createScramblePinHook().intercept(chain)
        })
        assertEquals(1, chain.calls)
        assertEquals("No module post-processing after native failure", 0, chain.ownerReads)
        assertUnchanged(pin, before)
    }

    @Test fun aNativeFatalErrorEscapesBeforeAnyModuleWork() {
        val pin = keypad(); val before = snapshot(pin)
        val failure = OutOfMemoryError("native fatal")
        val chain = Chain(pin, nativeFailure = failure)
        assertSame(failure, assertThrows(OutOfMemoryError::class.java) {
            SystemLockScreenHooks.createScramblePinHook().intercept(chain)
        })
        assertEquals(1, chain.calls)
        assertEquals(0, chain.ownerReads)
        assertUnchanged(pin, before)
    }

    @Test fun moduleFatalErrorsKeepTheirIdentity() {
        val fatal = OutOfMemoryError("module fatal")
        val chain = Chain(keypad(), ownerFailure = fatal)
        assertSame(fatal, assertThrows(OutOfMemoryError::class.java) {
            SystemLockScreenHooks.createScramblePinHook().intercept(chain)
        })
        assertEquals(1, chain.calls)
    }

    @Test fun wrappedModuleFatalErrorsStillEscapeThroughTheExistingLogger() {
        val fatal = OutOfMemoryError("wrapped module fatal")
        val chain = Chain(keypad(), ownerFailure = InvocationTargetException(fatal))
        assertSame(fatal, assertThrows(OutOfMemoryError::class.java) {
            SystemLockScreenHooks.createScramblePinHook().intercept(chain)
        })
        assertEquals(1, chain.calls)
    }

    @Test fun ordinaryModuleFailurePreservesTheNativeResult() {
        val chain = Chain(keypad(), ownerFailure = IllegalStateException("owner unavailable"))
        assertSame(chain.result, SystemLockScreenHooks.createScramblePinHook().intercept(chain))
        assertEquals(1, chain.calls)
    }

    @Test fun aMissingDigitCannotPartiallyRemoveNativeKeys() {
        val pin = keypad().apply { mViews[2][1] = null }
        assertSkippedWithoutMutation(pin)
    }

    @Test fun aMissingLaterRowCannotClearTheEarlierRows() {
        val pin = keypad().apply { removeView(rows[2]) }
        assertSkippedWithoutMutation(pin)
    }

    @Test fun duplicateDigitReferencesCannotPartiallyReparentKeys() {
        val pin = keypad().apply { mViews[1][1] = mViews[1][0] }
        assertSkippedWithoutMutation(pin)
    }

    @Test fun aDifferentNativeRowBindingRemainsUntouched() {
        val pin = keypad().apply {
            val first = mViews[1][0]
            mViews[1][0] = mViews[2][0]
            mViews[2][0] = first
        }
        assertSkippedWithoutMutation(pin)
    }

    @Test fun anIncompleteNativeMatrixRemainsUntouched() {
        val pin = keypad().apply { mViews = mViews.copyOfRange(0, 4) }
        assertSkippedWithoutMutation(pin)
    }

    @Test fun theInstallerUsesTheBehaviorallyTestedCallback() {
        var root = File(java.lang.System.getProperty("user.dir")).absoluteFile
        val relative = "app/src/main/java/tv/withaibuild/customiuizer/mods/SystemLockScreenHooks.kt"
        while (!File(root, relative).isFile) root = root.parentFile ?: error("Repository not found")
        val source = File(root, relative).readText().replace("\r\n", "\n")
        val start = source.indexOf("fun ScramblePINHook(")
        val end = source.indexOf("internal fun createScramblePinHook()", start)
        assertTrue(start >= 0 && end > start)
        assertTrue(source.substring(start, end).contains("\"onFinishInflate\", createScramblePinHook())"))
    }

    private fun assertSkippedWithoutMutation(pin: Keypad) {
        val before = snapshot(pin); val chain = Chain(pin)
        assertSame(chain.result, SystemLockScreenHooks.createScramblePinHook().intercept(chain))
        assertEquals(1, chain.calls)
        assertUnchanged(pin, before)
    }

    private data class Snapshot(val matrix: Array<Array<View?>>, val rowArrays: List<Array<View?>>, val cells: List<List<View?>>, val children: List<List<View>>)
    private fun snapshot(pin: Keypad) = Snapshot(pin.mViews, pin.mViews.toList(), pin.mViews.map { it.toList() }, children(pin))
    private fun children(pin: Keypad) = pin.rows.map { row -> (0 until row.childCount).map { row.getChildAt(it) } }
    private fun assertUnchanged(pin: Keypad, before: Snapshot) {
        assertSame(before.matrix, pin.mViews)
        assertEquals(before.rowArrays.size, pin.mViews.size)
        for (row in before.rowArrays.indices) assertSame("Native row array $row", before.rowArrays[row], pin.mViews[row])
        assertEquals(before.cells, pin.mViews.map { it.toList() })
        assertEquals("Every original child and position is retained", before.children, children(pin))
    }

    private fun keypad(): Keypad {
        val base = RuntimeEnvironment.getApplication()
        val resources = object : Resources(base.assets, base.resources.displayMetrics, base.resources.configuration) {
            override fun getIdentifier(name: String, defType: String?, defPackage: String?): Int {
                if (defType == "id" && defPackage == "com.android.systemui") {
                    val index = listOf("row1", "row2", "row3", "row4").indexOf(name)
                    if (index >= 0) return 0x341001 + index
                }
                return base.resources.getIdentifier(name, defType, defPackage)
            }
        }
        return Keypad(object : ContextWrapper(base) { override fun getResources(): Resources = resources })
    }

    private class Keypad(context: Context) : LinearLayout(context) {
        @JvmField var mViews = Array(5) { arrayOfNulls<View>(3) }
        val rows = List(4) { LinearLayout(context).apply { id = 0x341001 + it } }
        val clicks = ArrayList<Int>()
        val digits = List(10) { digit -> TextView(context).apply { text = digit.toString(); setOnClickListener { clicks.add(digit) } } }
        val spacer = View(context)
        val delete = View(context)
        init {
            orientation = VERTICAL
            rows.forEach(::addView)
            for (row in 1..3) for (column in 0..2) {
                val key = digits[(row - 1) * 3 + column + 1]
                mViews[row][column] = key; rows[row - 1].addView(key)
            }
            mViews[4][1] = digits[0]; mViews[4][2] = delete
            rows[3].addView(spacer); rows[3].addView(digits[0]); rows[3].addView(delete)
        }
    }

    private class Chain(private val owner: Any, private val nativeFailure: Throwable? = null, private val ownerFailure: Throwable? = null) : XposedInterface.Chain {
        val result = Any(); var calls = 0; var ownerReads = 0
        override fun getExecutable(): Executable = error("No reflection through the native chain")
        override fun getThisObject(): Any { ownerReads++; ownerFailure?.let { throw it }; return owner }
        override fun getArgs(): List<Any?> = error("No argument copies")
        override fun getArg(index: Int): Any? = error("No arguments")
        override fun proceed(): Any? { calls++; nativeFailure?.let { throw it }; return result }
        override fun proceed(args: Array<Any?>): Any? = error("No argument rewriting")
        override fun proceedWith(receiver: Any): Any? = error("No receiver rewriting")
        override fun proceedWith(receiver: Any, args: Array<Any>): Any? = error("No receiver rewriting")
    }
}
