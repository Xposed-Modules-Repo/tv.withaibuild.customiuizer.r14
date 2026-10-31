package tv.withaibuild.customiuizer.mods.utils

import android.app.Application
import android.app.Activity
import android.app.WallpaperColors
import android.content.ContextWrapper
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.graphics.drawable.ColorDrawable
import android.os.Looper
import android.view.View
import android.widget.FrameLayout
import kotlinx.coroutines.Job
import org.junit.After
import org.junit.Assert.*
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.Robolectric
import org.robolectric.RuntimeEnvironment
import org.robolectric.Shadows.shadowOf
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode
import org.robolectric.annotation.Implementation
import org.robolectric.annotation.Implements
import org.robolectric.util.ReflectionHelpers
import java.util.concurrent.CountDownLatch
import java.util.concurrent.TimeUnit
import java.util.concurrent.atomic.AtomicLong

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class, shadows = [AlbumArtColorsShadow::class])
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LockScreenAlbumArtRequestTest {
    private val context = RuntimeEnvironment.getApplication()
    private val view = View(context).apply { layout(0, 0, 32, 32) }
    private val sources = ArrayList<Bitmap>()
    private val theme = TestTheme::class.java
    private val controller = LockScreenAlbumArtController

    @Before fun prepare() {
        controller.clear()
        controller.setMiuiThemeUtilsClass(theme)
        XposedHelpers.setAdditionalStaticField(theme, "mAlbumArtSource", null)
        XposedHelpers.setAdditionalStaticField(theme, "mAlbumArt", null)
        controller.setAod(false)
        AlbumArtColorsShadow.beforeColors = null
        controller.applyTo(view)
    }

    @After fun release() {
        AlbumArtColorsShadow.beforeColors = null
        controller.clear()
        XposedHelpers.removeAdditionalStaticField(theme, "mAlbumArtSource")
        XposedHelpers.removeAdditionalStaticField(theme, "mAlbumArt")
        sources.forEach { if (!it.isRecycled) it.recycle() }
    }

    @Test fun grayscaleChangeOnTheSameSourceReplacesTheRenderedPixels() {
        val source = source(Color.RED)
        update(source)
        val first = result()!!
        update(source, gray = true)
        val gray = result()!!
        val pixel = gray.getPixel(16, 16)
        assertEquals(Color.red(pixel), Color.green(pixel))
        assertEquals(Color.green(pixel), Color.blue(pixel))
        assertNotSame(first, gray)
        assertEquals("The host artwork is never edited", Color.RED, source.getPixel(4, 4))
    }

    @Test fun fitChangeOnTheSameSourceUpdatesPadding() {
        val source = source(Color.RED)
        update(source)
        assertEquals(Color.RED, result()!!.getPixel(16, 1))
        update(source, scale = 2)
        assertEquals("Fit retains centered transparent padding", Color.TRANSPARENT, result()!!.getPixel(16, 1))
        assertEquals(Color.RED, result()!!.getPixel(16, 16))
    }

    @Test fun blurChangeOnTheSameSourceCreatesANewProcessedResult() {
        val source = source(Color.RED)
        update(source)
        val first = result()!!
        update(source, blur = 5)
        assertNotSame("New blur input must not be discarded by source-identity reuse", first, result())
        assertFalse(source.isRecycled)
    }

    @Test fun identicalMetadataDoesNotRestartTheWorkerOrReplaceTheResult() {
        val source = source(Color.RED)
        update(source)
        val first = result()
        val generation = generation()
        repeat(30) { update(source) }
        assertSame(first, result())
        assertEquals(generation, generation())
    }

    @Test fun pixelsChangedInsideTheSameBitmapInvalidateTheCurrentResult() {
        val source = source(Color.RED)
        update(source)
        source.eraseColor(Color.BLUE)
        update(source)
        assertEquals("A reused Bitmap can carry new artwork pixels", Color.BLUE, result()!!.getPixel(16, 16))
    }

    @Test fun returningToAMutatedSourceCannotHitItsOldPixelCache() {
        val source = source(Color.RED)
        update(source)
        update(source(Color.GREEN))
        source.eraseColor(Color.BLUE)
        update(source)
        assertEquals("The cache distinguishes Bitmap content generations", Color.BLUE, result()!!.getPixel(16, 16))
    }

    @Test fun deferredArtworkReplacementPreventsAnOlderWorkerFromPublishing() {
        val entered = CountDownLatch(1)
        val finish = CountDownLatch(1)
        AlbumArtColorsShadow.beforeColors = {
            entered.countDown()
            check(finish.await(3, TimeUnit.SECONDS))
        }
        try {
            controller.updateMediaMetaData(context, source(Color.RED), 0, 1, false)
            val oldJob = job()!!
            assertTrue("The old worker reached the post-render stage", entered.await(2, TimeUnit.SECONDS))
            controller.setAod(true)
            val next = source(Color.BLUE)
            controller.updateMediaMetaData(context, next, 0, 1, false)
            finish.countDown()
            await(oldJob, requireSuccess = false)
            assertNull("An older visible request cannot publish after a deferred replacement", result())
            assertSame(next, XposedHelpers.getAdditionalStaticField(theme, "mAlbumArtSource"))
            AlbumArtColorsShadow.beforeColors = null
            controller.setAod(false)
            await(job())
            assertEquals(Color.BLUE, result()!!.getPixel(16, 16))
        } finally { finish.countDown() }
    }

    @Test fun absentArtworkClearsPendingAndProcessedState() {
        update(source(Color.RED))
        controller.updateMediaMetaData(context, null, 0, 1, false)
        assertNull(result())
        assertNull(XposedHelpers.getAdditionalStaticField(theme, "mAlbumArtSource"))
        controller.setAod(true)
        controller.setAod(false)
        assertNull(result())
    }

    @Test fun repeatedViewBindingSharesOneInFlightArtworkRequest() {
        val entered = CountDownLatch(1)
        val finish = CountDownLatch(1)
        AlbumArtColorsShadow.beforeColors = {
            entered.countDown()
            check(finish.await(3, TimeUnit.SECONDS))
        }
        var first: Job? = null
        try {
            controller.updateMediaMetaData(context, source(Color.RED), 0, 1, false)
            first = job()!!
            assertTrue(entered.await(2, TimeUnit.SECONDS))
            val generation = generation()
            repeat(30) { assertFalse(controller.applyTo(view)) }
            assertSame("Repeated native binding must keep the same worker", first, job())
            assertEquals("Equivalent binding cannot invalidate useful work", generation, generation())
            finish.countDown()
            await(first)
            assertEquals(Color.RED, result()!!.getPixel(16, 16))
        } finally { finish.countDown(); await(first, requireSuccess = false); await(job(), requireSuccess = false) }
    }

    @Test fun enteringAodSuspendsAnExistingWorkerWithoutNewMetadata() {
        val entered = CountDownLatch(1)
        val finish = CountDownLatch(1)
        AlbumArtColorsShadow.beforeColors = {
            entered.countDown()
            check(finish.await(3, TimeUnit.SECONDS))
        }
        var first: Job? = null
        try {
            controller.updateMediaMetaData(context, source(Color.RED), 0, 1, false)
            first = job()!!
            assertTrue(entered.await(2, TimeUnit.SECONDS))
            controller.setAod(true)
            finish.countDown()
            await(first, requireSuccess = false)
            assertTrue("Native AOD retires the old request", first.isCancelled)
            assertNull("AOD cannot publish the previous visible request", result())
            AlbumArtColorsShadow.beforeColors = null
            controller.setAod(false)
            await(job())
            assertEquals(Color.RED, result()!!.getPixel(16, 16))
        } finally { finish.countDown(); await(first, requireSuccess = false) }
    }

    @Test fun bindingDeferredAodArtworkDoesNotStartAnyWorker() {
        controller.setAod(true)
        controller.updateMediaMetaData(context, source(Color.BLUE), 0, 1, false)
        val generation = generation()
        repeat(30) { assertFalse(controller.applyTo(view)) }
        assertNull("View binding cannot bypass native AOD", job())
        assertEquals(generation, generation())
        assertNull(result())
        controller.setAod(false)
        await(job())
        assertEquals(Color.BLUE, result()!!.getPixel(16, 16))
    }

    @Test fun aodBindingDoesNotRedisplayPreparedArtwork() {
        update(source(Color.RED))
        assertTrue(controller.applyTo(view))
        assertNotNull(view.background)
        controller.setAod(true)
        assertFalse("Lifecycle binding respects native AOD", controller.applyTo(view))
        assertNull("Release this View's drawable while preserving the ready result", view.background)
        assertNotNull(result())
    }

    @Test fun wakingWithAReadyResultDoesNotRepeatProcessing() {
        update(source(Color.RED))
        val prepared = result()
        controller.setAod(true)
        controller.setAod(false)
        assertNull("Ready artwork needs no new worker or color extraction on wake", job())
        assertSame(prepared, result())
        assertTrue(controller.applyTo(view))
    }

    @Test fun aRealSizeChangeStillSupersedesTheInFlightRequest() {
        val entered = CountDownLatch(1)
        val finish = CountDownLatch(1)
        AlbumArtColorsShadow.beforeColors = {
            entered.countDown()
            check(finish.await(3, TimeUnit.SECONDS))
        }
        var first: Job? = null
        try {
            controller.updateMediaMetaData(context, source(Color.RED), 0, 1, false)
            first = job()!!
            assertTrue(entered.await(2, TimeUnit.SECONDS))
            view.layout(0, 0, 64, 48)
            assertFalse(controller.applyTo(view))
            assertNotSame("Geometry is part of in-flight request identity", first, job())
            assertTrue(first.isCancelled)
            finish.countDown()
            await(job())
            assertEquals(64, result()!!.width)
            assertEquals(48, result()!!.height)
        } finally { finish.countDown(); await(first, requireSuccess = false); await(job(), requireSuccess = false) }
    }

    @Test fun nativeWakeCanResumeBeforeTheSharedScreenBroadcast() {
        controller.setAod(true)
        controller.updateMediaMetaData(context, source(Color.BLUE), 0, 1, false)
        ReflectionHelpers.setStaticField(ScreenStateController::class.java, "screenOn", false)
        try {
            // The existing native linkageViewAnim(true) is an authoritative wake event.
            controller.setAod(false)
            await(job())
            assertEquals(Color.BLUE, result()!!.getPixel(16, 16))
        } finally { ReflectionHelpers.setStaticField(ScreenStateController::class.java, "screenOn", true) }
    }

    @Test fun controllerClearDropsStaticSourceAndProcessedArt() {
        update(source(Color.RED))
        assertNotNull(result())
        controller.clear()
        assertNull("Clear releases the static processed bitmap", result())
        assertNull("Clear releases the static raw source", XposedHelpers.getAdditionalStaticField(theme, "mAlbumArtSource"))
        assertNull(ReflectionHelpers.getField<Any?>(controller, "albumArtCache"))
        assertNull(ReflectionHelpers.getField<Any?>(controller, "pendingSource"))
    }

    @Test fun controllerClearReleasesItsAppliedDrawableAndOwnerReferences() {
        update(source(Color.RED))
        assertTrue(controller.applyTo(view))
        assertNotNull(view.background)
        controller.clear()
        assertNull("The View cannot retain an otherwise-cleared processed bitmap", view.background)
        assertNull(applied(view))
        assertNull(ReflectionHelpers.getField<Any?>(controller, "lastViewRef"))
        assertNull(ReflectionHelpers.getField<Any?>(controller, "lastContextRef"))
    }

    @Test fun controllerClearPreservesAHostReplacementBackground() {
        update(source(Color.RED))
        assertTrue(controller.applyTo(view))
        val native = ColorDrawable(Color.BLUE)
        view.background = native
        controller.clear()
        assertSame("Only the drawable still owned by this module may be removed", native, view.background)
        assertNull("Drop the hidden module drawable reference too", applied(view))
    }

    @Test fun aReplacementViewReleasesThePreviousModuleDrawable() {
        update(source(Color.RED))
        assertTrue(controller.applyTo(view))
        val prepared = result()
        val generation = generation()
        val next = View(context).apply { layout(0, 0, 32, 32) }
        assertFalse("The previous consumer was never mounted", view.isAttachedToWindow)
        assertTrue(controller.applyTo(next))
        assertNull("An unmounted previous consumer cannot retain the module drawable", view.background)
        assertNull(applied(view))
        assertSame(prepared, (next.background as BitmapDrawable).bitmap)
        assertEquals("A new View of the same size reuses the existing result", generation, generation())
    }

    @Test fun aReplacementViewDoesNotChangeThePreviousHostBackground() {
        update(source(Color.RED))
        assertTrue(controller.applyTo(view))
        val native = ColorDrawable(Color.BLUE)
        view.background = native
        val next = View(context).apply { layout(0, 0, 32, 32) }
        assertTrue(controller.applyTo(next))
        assertSame(native, view.background)
        assertNull("Retiring the module owner must still release its hidden drawable", applied(view))
        assertNotNull(next.background)
    }

    @Test fun clearingAnInFlightRequestCannotResurrectItsSourceOnWake() {
        val entered = CountDownLatch(1)
        val finish = CountDownLatch(1)
        AlbumArtColorsShadow.beforeColors = {
            entered.countDown()
            check(finish.await(3, TimeUnit.SECONDS))
        }
        var first: Job? = null
        try {
            controller.updateMediaMetaData(context, source(Color.RED), 0, 1, false)
            first = job()!!
            assertTrue(entered.await(2, TimeUnit.SECONDS))
            controller.clear()
            finish.countDown()
            await(first, requireSuccess = false)
            assertNull(result())
            controller.setAod(true)
            controller.setAod(false)
            assertNull("Wake cannot restart artwork explicitly released by clear", job())
            assertNull(result())
        } finally { finish.countDown(); await(first, requireSuccess = false); await(job(), requireSuccess = false) }
    }

    @Test fun newMetadataAfterClearCanBindAndProcessAgain() {
        update(source(Color.RED))
        assertTrue(controller.applyTo(view))
        controller.clear()
        controller.applyTo(view)
        update(source(Color.BLUE))
        assertTrue(controller.applyTo(view))
        assertEquals(Color.BLUE, (view.background as BitmapDrawable).bitmap.getPixel(16, 16))
    }

    @Test fun aLateDetachOfThePreviousViewCannotClearTheCurrentDrawable() {
        update(source(Color.RED))
        assertTrue(controller.applyTo(view))
        val next = View(context).apply { layout(0, 0, 32, 32) }
        assertTrue(controller.applyTo(next))
        val currentDrawable = next.background
        val listener = ReflectionHelpers.getField<View.OnAttachStateChangeListener>(controller, "backgroundLifecycleListener")
        listener.onViewDetachedFromWindow(view)
        assertSame(currentDrawable, next.background)
        assertSame(result(), (next.background as BitmapDrawable).bitmap)
    }

    @Test fun anAttachedPreviousViewKeepsItsDrawableUntilNativeDetach() {
        val native = Robolectric.buildActivity(Activity::class.java).setup().visible()
        try {
            val host = FrameLayout(native.get())
            native.get().setContentView(host)
            val outgoing = View(native.get())
            host.addView(outgoing, FrameLayout.LayoutParams(32, 32))
            outgoing.layout(0, 0, 32, 32)
            assertTrue(outgoing.isAttachedToWindow)
            update(source(Color.RED))
            assertTrue(controller.applyTo(outgoing))
            val outgoingDrawable = outgoing.background
            val next = View(context).apply { layout(0, 0, 32, 32) }
            assertTrue(controller.applyTo(next))
            assertSame("A binding alone cannot preempt an attached native transition", outgoingDrawable, outgoing.background)
            host.removeView(outgoing)
            assertNull("Native detach releases the outgoing module drawable", outgoing.background)
            assertNull(applied(outgoing))
            assertSame(result(), (next.background as BitmapDrawable).bitmap)
        } finally { native.pause().stop().destroy() }
    }

    @Test fun aResizeWithoutRebindingCannotPublishThePreviousSize() {
        val entered = CountDownLatch(1)
        val finish = CountDownLatch(1)
        val publications = ArrayList<Pair<Int, Int>>()
        AlbumArtColorsShadow.beforeColors = {
            entered.countDown()
            check(finish.await(3, TimeUnit.SECONDS))
        }
        var first: Job? = null
        try {
            controller.updateMediaMetaData(recordingContext(publications), source(Color.RED), 0, 1, false)
            first = job()!!
            assertTrue(entered.await(2, TimeUnit.SECONDS))
            view.layout(0, 0, 64, 48) // native layout, without another module bind
            finish.countDown()
            await(first, requireSuccess = false)
            await(job())
            assertEquals("Only the current size may reach the update broadcast", listOf(64 to 48), publications)
            assertEquals(64, result()!!.width)
            assertEquals(48, result()!!.height)
        } finally { finish.countDown(); await(first, requireSuccess = false); await(job(), requireSuccess = false) }
    }

    @Test fun aReplacementLaidOutAfterBindingReceivesOnlyItsFinalSize() {
        val entered = CountDownLatch(1)
        val finish = CountDownLatch(1)
        val publications = ArrayList<Pair<Int, Int>>()
        AlbumArtColorsShadow.beforeColors = {
            entered.countDown()
            check(finish.await(3, TimeUnit.SECONDS))
        }
        var first: Job? = null
        try {
            controller.updateMediaMetaData(recordingContext(publications), source(Color.BLUE), 0, 1, false)
            first = job()!!
            assertTrue(entered.await(2, TimeUnit.SECONDS))
            val next = View(context)
            assertFalse(controller.applyTo(next))
            assertSame("An unmeasured replacement cannot yet start a sized request", first, job())
            next.layout(0, 0, 48, 64)
            finish.countDown()
            await(first, requireSuccess = false)
            await(job())
            assertEquals(listOf(48 to 64), publications)
            assertTrue(controller.applyTo(next))
            assertEquals(Color.BLUE, (next.background as BitmapDrawable).bitmap.getPixel(24, 32))
        } finally { finish.countDown(); await(first, requireSuccess = false); await(job(), requireSuccess = false) }
    }

    @Test fun aSizeThatReturnsToTheOriginalBeforeCompletionReusesTheWorker() {
        val entered = CountDownLatch(1)
        val finish = CountDownLatch(1)
        val publications = ArrayList<Pair<Int, Int>>()
        AlbumArtColorsShadow.beforeColors = {
            entered.countDown()
            check(finish.await(3, TimeUnit.SECONDS))
        }
        var first: Job? = null
        try {
            controller.updateMediaMetaData(recordingContext(publications), source(Color.RED), 0, 1, false)
            first = job()!!
            assertTrue(entered.await(2, TimeUnit.SECONDS))
            view.layout(0, 0, 64, 48)
            view.layout(0, 0, 32, 32)
            finish.countDown()
            await(first)
            assertSame("Matching final dimensions need no replacement request", first, job())
            assertEquals(listOf(32 to 32), publications)
        } finally { finish.countDown(); await(first, requireSuccess = false); await(job(), requireSuccess = false) }
    }

    @Test fun anUnmeasuredConsumerStillAllowsFallbackPreparation() {
        val publications = ArrayList<Pair<Int, Int>>()
        view.layout(0, 0, 0, 0)
        controller.updateMediaMetaData(recordingContext(publications), source(Color.RED), 0, 1, false)
        val first = job()!!
        await(first)
        assertSame(first, job())
        val prepared = result()!!
        assertTrue(prepared.width > 0 && prepared.height > 0)
        assertEquals(listOf(prepared.width to prepared.height), publications)
        assertEquals(Color.RED, prepared.getPixel(prepared.width / 2, prepared.height / 2))
    }

    private fun recordingContext(publications: MutableList<Pair<Int, Int>>) = object : ContextWrapper(context) {
        override fun sendBroadcast(intent: Intent) {
            if (intent.action?.endsWith("UPDATE_LS_ALBUM_ART") == true) {
                result()?.let { publications.add(it.width to it.height) }
            }
            super.sendBroadcast(intent)
        }
    }

    private fun applied(target: View) = XposedHelpers.getAdditionalInstanceField(target, "custo_lock_screen_album_art_drawable")

    private fun source(color: Int) = Bitmap.createBitmap(16, 8, Bitmap.Config.ARGB_8888).apply {
        eraseColor(color)
        sources.add(this)
    }

    private fun update(source: Bitmap, blur: Int = 0, scale: Int = 1, gray: Boolean = false) {
        controller.updateMediaMetaData(context, source, blur, scale, gray)
        await(job())
    }

    private fun result() = XposedHelpers.getAdditionalStaticField(theme, "mAlbumArt") as? Bitmap
    private fun job() = ReflectionHelpers.getField<Job?>(controller, "generationJob")
    private fun generation() = ReflectionHelpers.getField<AtomicLong>(controller, "requestGeneration").get()

    private fun await(job: Job?, requireSuccess: Boolean = true) {
        val deadline = System.nanoTime() + TimeUnit.SECONDS.toNanos(5)
        while (job?.isCompleted == false && System.nanoTime() < deadline) {
            shadowOf(Looper.getMainLooper()).idle()
            Thread.sleep(1)
        }
        shadowOf(Looper.getMainLooper()).idle()
        assertTrue("The artwork worker completed", job?.isCompleted != false)
        if (requireSuccess) assertFalse("Generation did not fail or get cancelled", job?.isCancelled == true)
    }

    class TestTheme
}

@Implements(WallpaperColors::class)
class AlbumArtColorsShadow {
    companion object {
        @Volatile var beforeColors: (() -> Unit)? = null

        @Implementation @JvmStatic fun fromBitmap(bitmap: Bitmap): WallpaperColors? {
            beforeColors?.invoke()
            return null
        }
    }
}
