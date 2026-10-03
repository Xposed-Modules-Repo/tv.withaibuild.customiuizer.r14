package tv.withaibuild.customiuizer.mods.duostatusbar

import android.content.Context
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.view.View
import tv.withaibuild.customiuizer.mods.utils.FatalErrors

/** Native vectors only. Drawing allocates no paths, paints, arrays, strings, or animation frames. */
internal class DuoStatusBarView(context: Context, private val config: DuoConfig,
    private val onFailure: (Throwable) -> Unit,
) : View(context) {
    private val stroke = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        style = Paint.Style.STROKE
        strokeCap = Paint.Cap.ROUND
        strokeJoin = Paint.Join.ROUND
    }
    private val fill = Paint(Paint.ANTI_ALIAS_FLAG)
    private val text = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        textAlign = Paint.Align.CENTER
        textSize = 30f
        isFakeBoldText = true
    }
    private val bolt = Path().apply {
        moveTo(64f, 0f); lineTo(47f, 20f); lineTo(58f, 20f)
        lineTo(54f, 34f); lineTo(72f, 13f); lineTo(61f, 13f); close()
    }
    private val plane = Path().apply {
        moveTo(60f, 43f); lineTo(65f, 59f); lineTo(83f, 69f); lineTo(83f, 75f)
        lineTo(64f, 70f); lineTo(64f, 81f); lineTo(71f, 87f); lineTo(71f, 91f)
        lineTo(60f, 88f); lineTo(49f, 91f); lineTo(49f, 87f); lineTo(56f, 81f)
        lineTo(56f, 70f); lineTo(37f, 75f); lineTo(37f, 69f); lineTo(55f, 59f); close()
    }
    private val headphones = Path().apply {
        moveTo(37f, 72f); lineTo(37f, 62f)
        cubicTo(37f, 34f, 82f, 34f, 82f, 62f); lineTo(82f, 72f)
    }
    // Resolve once on attachment. Resource reads recur only on configuration changes.
    private val nativeHeightId = resources.getIdentifier("status_bar_icon_height", "dimen", "com.android.systemui")
    var iconSizePx = 0
        private set
    var binding: DuoBinding? = null
    private var level = -1
    private var charging = false
    private var saver = false
    private var foreground = -1
    private var network = 0
    private var glyph = DuoAudioState.WIFI_GLYPH
    private var label = ""
    private var failed = false

    init {
        importantForAccessibility = IMPORTANT_FOR_ACCESSIBILITY_NO
        isClickable = false
        isFocusable = false
        updateSize()
    }

    private fun updateSize() {
        val nativeHeight = if (nativeHeightId == 0) 0 else resources.getDimensionPixelSize(nativeHeightId)
        iconSizePx = DuoSizing.pixels(config, nativeHeight, resources.displayMetrics.density)
    }

    override fun onConfigurationChanged(newConfig: Configuration) {
        super.onConfigurationChanged(newConfig)
        if (failed) return
        try {
            updateSize()
            layoutParams?.let { it.width = iconSizePx }
            requestLayout()
        } catch (t: Throwable) {
            FatalErrors.unwrapAndRethrowIfFatal(t)
            failed = true
            onFailure(t)
        }
    }

    fun render(level: Int, charging: Boolean, saver: Boolean, foreground: Int, network: Int, audio: Int) {
        val glyph = DuoAudioState.glyph(network, if (config.showAudio) audio else 0)
        if (this.level == level && this.charging == charging && this.saver == saver &&
            this.foreground == foreground && this.network == network && this.glyph == glyph) return
        if (this.level != level && config.showPercent) label = level.toString()
        this.level = level
        this.charging = charging
        this.saver = saver
        this.foreground = foreground
        this.network = network
        this.glyph = glyph
        invalidate()
    }

    override fun onMeasure(widthMeasureSpec: Int, heightMeasureSpec: Int) {
        // A wrap-content battery host must retain a height after its original children are hidden.
        val desired = iconSizePx
        setMeasuredDimension(resolveSize(desired, widthMeasureSpec), resolveSize(desired, heightMeasureSpec))
    }

    override fun onDraw(canvas: Canvas) {
        if (failed || level < 0 || width <= 0 || height <= 0) return
        val saved = canvas.save()
        try {
            val scale = minOf(width, height) / 120f
            canvas.translate((width - scale * 120f) / 2f, (height - scale * 120f) / 2f)
            canvas.scale(scale, scale)
            drawIcon(canvas)
        } catch (t: Throwable) {
            FatalErrors.unwrapAndRethrowIfFatal(t)
            failed = true
            onFailure(t)
        } finally {
            canvas.restoreToCount(saved)
        }
    }

    private fun drawIcon(canvas: Canvas) {
        val gap = DuoGeometry.topGap(config.showPercent, charging)
        val side = DuoGeometry.sideSweep(gap)
        val rightStart = 270f + gap / 2f
        val track = (foreground and 0x00ffffff) or (56 shl 24)
        val tint = DuoGeometry.batteryColor(foreground, level, charging, saver)
        stroke.strokeWidth = if (config.bold) 10f else 8f
        stroke.color = track
        canvas.drawArc(8f, 10f, 111f, 113f, DuoGeometry.RING_START, side, false, stroke)
        canvas.drawArc(8f, 10f, 111f, 113f, rightStart, side, false, stroke)
        stroke.color = tint
        val left = DuoGeometry.leftFill(level, side)
        val right = DuoGeometry.rightFill(level, side)
        if (left > 0f) canvas.drawArc(8f, 10f, 111f, 113f, DuoGeometry.RING_START, left, false, stroke)
        if (right > 0f) canvas.drawArc(8f, 10f, 111f, 113f, rightStart, right, false, stroke)

        if (charging) {
            fill.color = tint
            canvas.drawPath(bolt, fill)
        } else if (config.showPercent) {
            text.color = tint
            canvas.drawText(label, 59.5f, 26f, text)
        }

        val wifi = DuoNetworkState.wifiLevel(network)
        val wifiOn = network and DuoNetworkState.WIFI_CONNECTED != 0
        if (glyph == DuoAudioState.PLANE_GLYPH) {
            fill.color = foreground
            canvas.drawPath(plane, fill)
        } else if (glyph == DuoAudioState.WIRED_GLYPH || glyph == DuoAudioState.BLUETOOTH_GLYPH) {
            val tint = if (glyph == DuoAudioState.BLUETOOTH_GLYPH) 0xff0a84ff.toInt() else foreground
            stroke.strokeWidth = if (config.bold) 9f else 7f
            stroke.color = tint
            canvas.drawPath(headphones, stroke)
            fill.color = tint
            canvas.drawRoundRect(31f, 62f, 44f, 83f, 5f, 5f, fill)
            canvas.drawRoundRect(75f, 62f, 88f, 83f, 5f, 5f, fill)
        } else {
            stroke.strokeWidth = if (config.bold) 9f else 7f
            stroke.color = if (wifiOn && wifi >= 3) foreground else track
            canvas.drawArc(28.5f, 47.5f, 90.5f, 109.5f, 225f, 90f, false, stroke)
            stroke.color = if (wifiOn && wifi >= 1) foreground else track
            canvas.drawArc(41f, 60f, 78f, 97f, 225f, 90f, false, stroke)
            fill.color = if (wifiOn) foreground else track
            canvas.drawCircle(59.5f, 78.5f, 5f, fill)
            if (network and DuoNetworkState.WIFI_UNVALIDATED != 0) {
                text.color = foreground
                text.textSize = 24f
                canvas.drawText("!", 91f, 84f, text)
                text.textSize = 30f
            }
        }
        val cell = DuoNetworkState.cellLevel(network)
        fill.color = if (cell >= 1) foreground else track
        canvas.drawCircle(33f, 104.2f, 5.5f, fill)
        fill.color = if (cell >= 2) foreground else track
        canvas.drawCircle(50.5f, 111.2f, 5.5f, fill)
        fill.color = if (cell >= 3) foreground else track
        canvas.drawCircle(68.5f, 111.7f, 5.5f, fill)
        fill.color = if (cell >= 4) foreground else track
        canvas.drawCircle(86f, 105.8f, 5.5f, fill)
    }
}
