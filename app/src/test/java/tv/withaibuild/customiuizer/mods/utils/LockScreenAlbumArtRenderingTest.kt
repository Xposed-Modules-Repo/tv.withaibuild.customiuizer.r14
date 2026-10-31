package tv.withaibuild.customiuizer.mods.utils

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import java.io.File
import org.junit.After
import org.junit.Assert.*
import org.junit.Test
import org.junit.runner.RunWith
import org.robolectric.RobolectricTestRunner
import org.robolectric.annotation.Config
import org.robolectric.annotation.GraphicsMode

@RunWith(RobolectricTestRunner::class)
@Config(sdk = [34], application = Application::class)
@GraphicsMode(GraphicsMode.Mode.NATIVE)
class LockScreenAlbumArtRenderingTest {
    private val owned = mutableListOf<Bitmap>()
    private val draw = LockScreenAlbumArtController::class.java.getDeclaredMethod(
        "drawAlbumArt", Bitmap::class.java, Int::class.javaPrimitiveType,
        Boolean::class.javaPrimitiveType, Int::class.javaPrimitiveType, Int::class.javaPrimitiveType,
    ).apply { isAccessible = true }

    @After fun releaseBitmaps() {
        owned.forEach { if (!it.isRecycled) it.recycle() }
    }

    @Test fun fitCentersLandscapeWhenUpscaling() {
        val output = render(solid(80, 40), 2, 200, 200, "fit-landscape-up")
        assertEquals(Color.TRANSPARENT, output.getPixel(100, 25))
        assertEquals(Color.MAGENTA, output.getPixel(100, 75))
        assertEquals(Color.MAGENTA, output.getPixel(100, 125))
        assertEquals(Color.TRANSPARENT, output.getPixel(100, 175))
    }

    @Test fun fitCentersLandscapeWhenDownscaling() {
        val output = render(solid(400, 200), 2, 100, 100, "fit-landscape-down")
        assertEquals(Color.TRANSPARENT, output.getPixel(50, 10))
        assertEquals(Color.MAGENTA, output.getPixel(50, 40))
        assertEquals(Color.MAGENTA, output.getPixel(50, 70))
        assertEquals(Color.TRANSPARENT, output.getPixel(50, 90))
    }

    @Test fun fitCentersPortraitWhenUpscaling() {
        val output = render(solid(40, 80), 2, 200, 200, "fit-portrait-up")
        assertEquals(Color.TRANSPARENT, output.getPixel(25, 100))
        assertEquals(Color.MAGENTA, output.getPixel(75, 100))
        assertEquals(Color.MAGENTA, output.getPixel(125, 100))
        assertEquals(Color.TRANSPARENT, output.getPixel(175, 100))
    }

    @Test fun fitCentersPortraitWhenDownscaling() {
        val output = render(solid(200, 400), 2, 100, 100, "fit-portrait-down")
        assertEquals(Color.TRANSPARENT, output.getPixel(10, 50))
        assertEquals(Color.MAGENTA, output.getPixel(40, 50))
        assertEquals(Color.MAGENTA, output.getPixel(70, 50))
        assertEquals(Color.TRANSPARENT, output.getPixel(90, 50))
    }

    @Test fun defaultCropKeepsTheMiddleOfAnUpscaledLandscape() {
        val output = render(stripes(80, 40), 1, 200, 200, "crop-landscape-up")
        assertEquals(Color.GREEN, output.getPixel(20, 100))
        assertEquals(Color.BLUE, output.getPixel(180, 100))
        assertCovered(output)
    }

    @Test fun explicitCropKeepsTheMiddleOfADownscaledLandscape() {
        val output = render(stripes(400, 200), 3, 100, 100, "crop-landscape-down")
        assertEquals(Color.GREEN, output.getPixel(10, 50))
        assertEquals(Color.BLUE, output.getPixel(90, 50))
        assertCovered(output)
    }

    @Test fun explicitCropKeepsTheMiddleOfAnUpscaledPortrait() {
        val output = render(stripes(40, 80, vertical = true), 3, 200, 200, "crop-portrait-up")
        assertEquals(Color.GREEN, output.getPixel(100, 20))
        assertEquals(Color.BLUE, output.getPixel(100, 180))
        assertCovered(output)
    }

    @Test fun defaultCropKeepsTheMiddleOfADownscaledPortrait() {
        val output = render(stripes(200, 400, vertical = true), 1, 100, 100, "crop-portrait-down")
        assertEquals(Color.GREEN, output.getPixel(50, 10))
        assertEquals(Color.BLUE, output.getPixel(50, 90))
        assertCovered(output)
    }

    @Test fun squareCoverFillsAPortraitLockscreen() {
        val output = render(solid(64, 64), 1, 108, 240, "square-cover-portrait-screen")
        assertEquals(Color.MAGENTA, output.getPixel(54, 120))
        assertCovered(output)
    }

    @Test fun unitScaleKeepsTheExistingCenteredCrop() {
        val output = render(stripes(200, 100), 1, 100, 100, "unit-scale-control")
        assertEquals(Color.GREEN, output.getPixel(10, 50))
        assertEquals(Color.BLUE, output.getPixel(90, 50))
        assertCovered(output)
    }

    @Test fun grayscaleDoesNotChangeGeometryOrMutateTheSourceAndClearsOnTheNextColorFrame() {
        val source = solid(80, 40)
        val gray = render(source, 2, 200, 200, "grayscale-fit", grayscale = true)
        val pixel = gray.getPixel(100, 100)
        assertEquals(255, Color.alpha(pixel))
        assertEquals(Color.red(pixel), Color.green(pixel))
        assertEquals(Color.green(pixel), Color.blue(pixel))
        val color = render(source, 2, 200, 200, "color-after-grayscale")
        assertEquals(Color.MAGENTA, color.getPixel(100, 100))
        assertEquals(Color.MAGENTA, source.getPixel(40, 20))
        assertFalse(source.isRecycled)
        assertNotSame(source, color)
    }

    private fun solid(width: Int, height: Int) = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        .also { owned.add(it); it.eraseColor(Color.MAGENTA) }

    private fun stripes(width: Int, height: Int, vertical: Boolean = false): Bitmap {
        val bitmap = solid(width, height)
        val canvas = Canvas(bitmap)
        val paint = Paint()
        for ((i, color) in listOf(Color.RED, Color.GREEN, Color.BLUE, Color.YELLOW).withIndex()) {
            paint.color = color
            if (vertical) canvas.drawRect(0f, i * height / 4f, width.toFloat(), (i + 1) * height / 4f, paint)
            else canvas.drawRect(i * width / 4f, 0f, (i + 1) * width / 4f, height.toFloat(), paint)
        }
        return bitmap
    }

    private fun render(source: Bitmap, mode: Int, width: Int, height: Int, name: String, grayscale: Boolean = false): Bitmap {
        val output = draw.invoke(LockScreenAlbumArtController, source, mode, grayscale, width, height) as Bitmap
        owned.add(output)
        assertEquals(width, output.width)
        assertEquals(height, output.height)
        java.lang.System.getenv("CUSTOMIUIZER_ALBUM_ART_PREVIEW_DIR")?.let { directory ->
            File(directory).mkdirs()
            File(directory, "$name.png").outputStream().use { output.compress(Bitmap.CompressFormat.PNG, 100, it) }
        }
        return output
    }

    private fun assertCovered(bitmap: Bitmap) {
        for (x in listOf(0, bitmap.width / 2, bitmap.width - 1)) {
            for (y in listOf(0, bitmap.height / 2, bitmap.height - 1)) {
                assertEquals("uncovered crop pixel at $x,$y", 255, Color.alpha(bitmap.getPixel(x, y)))
            }
        }
    }
}
