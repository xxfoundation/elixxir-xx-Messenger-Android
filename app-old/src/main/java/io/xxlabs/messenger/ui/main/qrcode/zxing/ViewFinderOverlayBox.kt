package io.xxlabs.messenger.ui.main.qrcode.zxing

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.view.SurfaceHolder
import android.view.SurfaceView
import androidx.core.content.ContextCompat
import io.xxlabs.messenger.R
import timber.log.Timber

class ViewFinderOverlayBox(context: Context, attrs: AttributeSet) : SurfaceView(context, attrs), SurfaceHolder.Callback {
    var surfaceHolder: SurfaceHolder = holder

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
        tryDrawing(holder)
    }

    private fun tryDrawing(holder: SurfaceHolder) {
        val overlayWidth = width.toFloat()
        val overlayHeight = height.toFloat()
        val boxWidth = overlayWidth * 80 / 100
        val boxHeight = overlayHeight * 50 / 100
        val cx = overlayWidth / 2
        val cy = overlayHeight / 2
        boxRect =
            RectF(cx - boxWidth / 2, cy - boxHeight / 2, cx + boxWidth / 2, cy + boxHeight / 2)

        val canvas = holder.lockCanvas()
        if (canvas == null) {
            Timber.e("Cannot draw onto the canvas as it's null");
        } else {
            drawOverlay(canvas)
            holder.unlockCanvasAndPost(canvas)
        }
    }

    private fun drawOverlay(canvas: Canvas) {
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
        }
    }

    override fun surfaceCreated(holder: SurfaceHolder) {
        tryDrawing(holder)
    }

    override fun surfaceChanged(holder: SurfaceHolder, format: Int, width: Int, height: Int) {
        tryDrawing(holder)
    }

    override fun surfaceDestroyed(holder: SurfaceHolder) {
        tryDrawing(holder)
    }
}