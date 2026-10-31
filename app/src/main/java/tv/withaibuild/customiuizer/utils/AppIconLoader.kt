package tv.withaibuild.customiuizer.utils

import android.content.ComponentName
import android.content.Context
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.Canvas
import android.util.Log
import kotlinx.coroutines.Dispatchers

/** Visible settings rows decode bounded icons off the main thread; the existing LRU owns bitmaps. */
internal object AppIconLoader {
    val dispatcher = Dispatchers.IO.limitedParallelism(4)
    private const val TAG = "Pengeek.IconLoader"
    fun loadIcon(ctx: Context, ad: AppData): Bitmap? {
        var icon: android.graphics.drawable.Drawable? = null

        val iconKey = ad.iconKey
        if (iconKey.isEmpty()) return null

        // If another loader finished while we were queued, return the cached bitmap
        val existing = Helpers.memoryCache.get(iconKey)
        if (existing != null) return existing

        try {
            if (ad.pkgName.isEmpty() && ad.actName.isEmpty()) return null

            val pkgMgr = ctx.packageManager
            if (ad.actName.isNotEmpty() && ad.actName != "-") {
                val component = ComponentName(ad.pkgName, ad.actName)
                try {
                    if (pkgMgr.getActivityInfo(component, PackageManager.MATCH_ALL).icon != 0) {
                        icon = pkgMgr.getActivityIcon(component)
                    }
                } catch (ignored: PackageManager.NameNotFoundException) {
                }
            }
            if (icon == null) {
                // The String overload queries ApplicationInfo again. Keep its shared-library
                // metadata flag so resource loading receives the same library paths.
                val application = pkgMgr.getApplicationInfo(ad.pkgName,
                    PackageManager.MATCH_DISABLED_COMPONENTS or PackageManager.GET_SHARED_LIBRARY_FILES)
                if (application.icon != 0) icon = pkgMgr.getApplicationIcon(application)
            }
        } catch (t: Throwable) {
            Log.w(TAG, "Unable to load app icon", t)
        }
        if (icon == null) return null

        var bitmap: Bitmap? = null
        try {
            val size = ctx.resources.getDimensionPixelSize(android.R.dimen.app_icon_size).coerceIn(1, 256)
            bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)
            icon.setBounds(0, 0, size, size)
            icon.draw(Canvas(bitmap))
            Helpers.memoryCache.put(iconKey, bitmap)
            return bitmap
        } catch (error: OutOfMemoryError) {
            // An optional icon must keep the old loader's allocation-failure fallback.
            bitmap?.recycle()
            return null
        } catch (error: RuntimeException) {
            bitmap?.recycle()
            Log.w(TAG, "Unable to draw app icon", error)
            return null
        }
    }

}
