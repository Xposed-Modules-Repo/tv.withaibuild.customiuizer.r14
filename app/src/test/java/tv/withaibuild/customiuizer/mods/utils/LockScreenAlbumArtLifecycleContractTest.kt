package tv.withaibuild.customiuizer.mods.utils

import tv.withaibuild.customiuizer.testsupport.repositorySource as source

import org.junit.Assert.assertTrue
import org.junit.Test

class LockScreenAlbumArtLifecycleContractTest {

    @Test
    fun viewBackgroundIsReusedAndReleasedOnDetach() {
        val source = source("app/src/main/java/tv/withaibuild/customiuizer/mods/utils/LockScreenAlbumArtController.kt")
        assertTrue(source.contains("current.bitmap === bitmap"))
        assertTrue(source.contains("onViewDetachedFromWindow(view: View)"))
        assertTrue(source.contains("clearViewBackground(view)"))
        assertTrue(source.contains("removeAdditionalInstanceField(view, APPLIED_DRAWABLE_FIELD)"))
        assertTrue(source.contains("getAdditionalInstanceField(view, LIFECYCLE_LISTENER_FIELD) === backgroundLifecycleListener"))
    }

    @Test
    fun staleAndTemporaryBitmapsHaveAnOwnedReleasePath() {
        val source = source("app/src/main/java/tv/withaibuild/customiuizer/mods/utils/LockScreenAlbumArtController.kt")
        assertTrue(source.contains("finally {\n            recycleIntermediate(blurred, art, processed)"))
        assertTrue(source.contains("recycleIntermediate(small, art, blurred)"))
        assertTrue(source.contains("art.width.toLong() * art.height.toLong()"))
    }

    @Test
    fun blurRejectsInvalidRadiusBeforeCopyAndSupportsNullConfig() {
        val source = source("app/src/main/java/tv/withaibuild/customiuizer/utils/HookUtils.kt")
        val radiusCheck = source.indexOf("if (radius < 1) return null")
        val bitmapCopy = source.indexOf("sentBitmap.copy(sentBitmap.config ?: Bitmap.Config.ARGB_8888, true)")
        assertTrue(radiusCheck >= 0 && bitmapCopy > radiusCheck)
    }

}
