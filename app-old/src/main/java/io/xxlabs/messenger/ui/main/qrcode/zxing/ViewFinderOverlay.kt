package io.xxlabs.messenger.ui.main.qrcode.zxing

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.View
import androidx.core.content.ContextCompat
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.util.Utils
import timber.log.Timber

class ViewFinderOverlay(context: Context, attrs: AttributeSet) : View(context, attrs) {
    private val boxPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.background)
        style = Paint.Style.STROKE
        strokeWidth = context.resources.getDimensionPixelOffset(R.dimen.spacing_4).toFloat()
    }

    private var scrimPaint: Paint = Paint().apply {
        color = ContextCompat.getColor(context, R.color.neutral_active)
    }

    private val eraserPaint: Paint = Paint().apply {
        strokeWidth = boxPaint.strokeWidth
        xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
    }

    private val boxCornerRadius: Float =
        context.resources.getDimensionPixelOffset(R.dimen.spacing_8).toFloat()

    fun setOverlayPaint(newColor: Int) {
        scrimPaint = Paint().apply {
            color = ContextCompat.getColor(context, newColor)
        }
    }

    var boxRect: RectF? = null

    fun setViewFinder() {
        tryDrawing()
    }

    private fun tryDrawing() {
        Timber.v("[CAMERA] tryDrawing")
        val overlayWidth = width.toFloat()
        val overlayHeight = height.toFloat()
        val boxWidth = overlayWidth * 85 / 100
        val boxHeight = overlayHeight * 60 / 100
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        boxRect = RectF(
            cx - boxWidth / 2,
            cy - boxHeight / 2,
            cx + boxWidth / 2,
            cy + boxHeight / 2
        )
        invalidate()
    }

    override fun draw(canvas: Canvas) {
        super.draw(canvas)
        Timber.v("[CAMERA] draw")
        drawOverlay(canvas)
    }

    private fun drawOverlay(canvas: Canvas) {
        val time = Utils.getCurrentTimeStamp()
        Timber.v("[CAMERA] onDraw")
        boxRect?.let {
            // Draws the dark background scrim and leaves the box area clear.
            canvas.drawRect(0f, 0f, width.toFloat(), height.toFloat(), scrimPaint)
            // As the stroke is always centered, so erase twice with FILL and STROKE respectively to clear
            // all area that the box rect would occupy.
            eraserPaint.style = Paint.Style.FILL
            canvas.drawRoundRect(it, boxCornerRadius, boxCornerRadius, eraserPaint)
            eraserPaint.style = Paint.Style.STROKE
            canvas.drawRoundRect(it, boxCornerRadius, boxCornerRadius, eraserPaint)
            // Draws the box.
            canvas.drawRoundRect(it, boxCornerRadius, boxCornerRadius, boxPaint)

            Timber.v("[CAMERA] final time: ${Utils.getCurrentTimeStamp() - time}")
        }
    }
}

