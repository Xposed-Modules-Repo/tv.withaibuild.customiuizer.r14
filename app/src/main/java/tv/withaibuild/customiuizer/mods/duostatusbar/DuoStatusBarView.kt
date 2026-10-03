package tv.withaibuild.customiuizer.mods.duostatusbar

import android.content.Context
import android.animation.ValueAnimator
import android.content.res.Configuration
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.Path
import android.os.SystemClock
import android.view.View
import tv.withaibuild.customiuizer.mods.utils.FatalErrors

/** Native vectors only. Short event-driven transitions reuse the same drawing state and paths. */
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
        moveTo(39f, 72f); lineTo(39f, 62f)
        cubicTo(39f, 36f, 80f, 36f, 80f, 62f); lineTo(80f, 72f)
    }
    private val earbuds = Path().apply {
        moveTo(42f, 47f)
        cubicTo(36f, 47f, 35f, 59f, 41f, 62f)
        cubicTo(46f, 66f, 52f, 60f, 52f, 55f)
        cubicTo(52f, 50f, 48f, 47f, 42f, 47f); close()
        moveTo(77f, 47f)
        cubicTo(83f, 47f, 84f, 59f, 78f, 62f)
        cubicTo(73f, 66f, 67f, 60f, 67f, 55f)
        cubicTo(67f, 50f, 71f, 47f, 77f, 47f); close()
    }
    // Wi-Fi marker geometry adapted from Status Trio (Apache-2.0).
    // Attribution and license: assets/licenses/status-trio.txt.
    private val wifiDot = Path().apply {
        moveTo(59.5f, 69.9f)
        cubicTo(61f, 69.9f, 65.2f, 70.8f, 66.5f, 73f)
        cubicTo(66.7f, 73.8f, 66.7f, 74.3f, 66.5f, 75f)
        cubicTo(63.8f, 78.8f, 61.15f, 80.95f, 59.5f, 80.95f)
        cubicTo(57.85f, 80.95f, 55.2f, 78.8f, 52.5f, 75f)
        cubicTo(52.3f, 74.3f, 52.3f, 73.8f, 52.5f, 73f)
        cubicTo(53.8f, 70.8f, 58f, 69.9f, 59.5f, 69.9f)
        close()
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
    private val transition = DuoTransition()
    private var offsetPx = 0f
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
        offsetPx = config.offsetDp * resources.displayMetrics.density
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
        val key = DuoCenter.key(network, audio, config)
        if (this.level == level && this.charging == charging && this.saver == saver &&
            this.foreground == foreground && this.network == network && transition.current == key) return
        val duration = if (this.level >= 0 && transition.current != key && config.transitions && !saver &&
            isShown && windowVisibility == VISIBLE && ValueAnimator.areAnimatorsEnabled())
            (180f * ValueAnimator.getDurationScale().coerceIn(0f, 2f)).toLong() else 0L
        transition.change(key, SystemClock.uptimeMillis(), duration)
        if (saver) stopTransition()
        if (this.level != level && config.showPercent) label = level.toString()
        this.level = level
        this.charging = charging
        this.saver = saver
        this.foreground = foreground
        this.network = network
        invalidate()
    }

    fun stopTransition() { transition.finish() }

    override fun onVisibilityAggregated(isVisible: Boolean) {
        super.onVisibilityAggregated(isVisible)
        if (!isVisible) stopTransition()
    }

    override fun onWindowVisibilityChanged(visibility: Int) {
        super.onWindowVisibilityChanged(visibility)
        if (visibility != VISIBLE) stopTransition()
    }

    override fun onDetachedFromWindow() {
        stopTransition()
        super.onDetachedFromWindow()
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
            val contentWidth = width - paddingLeft - paddingRight
            val contentHeight = height - paddingTop - paddingBottom
            val offset = DuoSizing.offset(contentHeight, offsetPx)
            val size = DuoSizing.drawingSize(contentWidth, contentHeight, offset)
            if (size <= 0f) return
            val scale = size / 120f
            canvas.translate(paddingLeft + (contentWidth - size) / 2f,
                paddingTop + (contentHeight - size) / 2f + offset)
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
        val track = DuoGeometry.dim(foreground)
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

        val progress = transition.fraction(SystemClock.uptimeMillis())
        val eased = progress * progress * (3f - 2f * progress)
        if (progress < 1f) drawCenter(canvas, transition.previous, ((1f - eased) * 255f).toInt(), 1f - 0.04f * eased)
        drawCenter(canvas, transition.current, (eased * 255f).toInt(), 0.96f + 0.04f * eased)
        if (progress < 1f && isShown && windowVisibility == VISIBLE) postInvalidateOnAnimation()

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

    private fun drawCenter(canvas: Canvas, key: Int, alpha: Int, scale: Float) {
        if (key < 0 || alpha <= 0) return
        val saved = canvas.save()
        try {
            canvas.scale(scale, scale, 59.5f, 65f)
            val foreground = DuoGeometry.withAlpha(this.foreground, (this.foreground ushr 24) * alpha / 255)
            drawCenterGlyph(canvas, key, foreground)
        } finally { canvas.restoreToCount(saved) }
    }

    private fun drawCenterGlyph(canvas: Canvas, key: Int, foreground: Int) {
        val glyph = DuoCenter.glyph(key)
        val value = DuoCenter.value(key)
        val track = DuoGeometry.dim(foreground)
        if (glyph == DuoAudioState.PLANE_GLYPH) {
            fill.color = foreground
            canvas.drawPath(plane, fill)
        } else if (glyph == DuoAudioState.WIRED_GLYPH || glyph == DuoAudioState.BLUETOOTH_GLYPH) {
            val tint = if (glyph == DuoAudioState.BLUETOOTH_GLYPH) DuoGeometry.bluetoothColor(foreground, value) else foreground
            val saved = canvas.save()
            try {
                canvas.scale(config.headphoneScale, config.headphoneScale, 59.5f, 65f)
                stroke.strokeWidth = if (config.bold) 9f else 7f
                stroke.color = tint
                fill.color = tint
                if (config.headphoneStyle == 3 || (config.headphoneStyle == 1 && glyph == DuoAudioState.BLUETOOTH_GLYPH)) {
                    canvas.drawPath(earbuds, fill)
                    canvas.drawRoundRect(40f, 58f, 47f, 83f, 3.5f, 3.5f, fill)
                    canvas.drawRoundRect(72f, 58f, 79f, 83f, 3.5f, 3.5f, fill)
                } else {
                    canvas.drawPath(headphones, stroke)
                    canvas.drawRoundRect(33f, 63f, 46f, 82f, 5f, 5f, fill)
                    canvas.drawRoundRect(73f, 63f, 86f, 82f, 5f, 5f, fill)
                }
            } finally { canvas.restoreToCount(saved) }
        } else if (glyph == DuoAudioState.CELLULAR_GLYPH) {
            stroke.color = foreground
            stroke.strokeWidth = if (config.bold) 6f else 5f
            canvas.drawLine(59.5f, 59f, 50.5f, 82f, stroke)
            canvas.drawLine(59.5f, 59f, 68.5f, 82f, stroke)
            canvas.drawLine(54.5f, 73f, 64.5f, 73f, stroke)
            canvas.drawArc(42.5f, 37f, 76.5f, 71f, 135f, 90f, false, stroke)
            canvas.drawArc(42.5f, 37f, 76.5f, 71f, 315f, 90f, false, stroke)
            fill.color = foreground
            canvas.drawCircle(59.5f, 54f, 3.5f, fill)
        } else if (glyph == DuoAudioState.OFFLINE_GLYPH) {
            stroke.color = foreground
            stroke.strokeWidth = if (config.bold) 7f else 5f
            canvas.drawLine(48.5f, 54f, 70.5f, 76f, stroke)
            canvas.drawLine(70.5f, 54f, 48.5f, 76f, stroke)
        } else {
            stroke.strokeWidth = if (config.bold) 9f else 7f
            stroke.color = if (value >= 3) foreground else track
            canvas.drawArc(28.5f, 47.3f, 90.5f, 109.3f, 227.35f, 85.3f, false, stroke)
            stroke.color = if (value >= 2) foreground else track
            canvas.drawArc(41f, 60.39f, 78f, 97.39f, 227.5f, 85f, false, stroke)
            fill.color = if (value >= 1) foreground else track
            canvas.drawPath(wifiDot, fill)
            if (key and DuoCenter.WARNING != 0) {
                text.color = foreground
                text.textSize = 24f
                canvas.drawText("!", 91f, 84f, text)
                text.textSize = 30f
            }
        }
    }
}
