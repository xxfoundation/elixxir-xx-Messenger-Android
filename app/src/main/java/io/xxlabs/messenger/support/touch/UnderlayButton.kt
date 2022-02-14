package io.xxlabs.messenger.support.touch

import android.content.Context
import android.graphics.*
import android.graphics.drawable.BitmapDrawable
import android.text.TextPaint
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.graphics.drawable.toBitmap
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.support.util.Utils


class UnderlayButton(
    private val context: Context,
    private val text: String?,
    private val imgResId: Int?,
    private val bgColorTint: Int,
    private val iconColorTint: Int? = null,
    private val clickListener: ButtonClickListener
) {
    private var pos: Int = 0
    private var clickRegion: RectF? = null

    fun onClick(x: Float, y: Float): Boolean {
        if (clickRegion != null && clickRegion!!.contains(x, y)) {
            clickListener.onClick(pos)
            return true
        }

        return false
    }

    fun onDraw(c: Canvas, container: RectF, pos: Int) {
        val bgPaint = Paint().apply {
            isAntiAlias = true
            color = ContextCompat.getColor(XxMessengerApplication.instance, bgColorTint)
            style = Paint.Style.FILL
        }

        // Draw background rectangle
        c.drawRect(container, bgPaint)

        if (imgResId != null) {
            generateIconOnly(container, c, imgResId)
        } else {
            generateTextOnly(container, c)
        }

        clickRegion = container
        this.pos = pos
    }

    private fun generateTextOnly(container: RectF, c: Canvas) {
        // Draw Text
        val textPaint = TextPaint().apply {
            color = Color.WHITE
            textSize = Utils.dpToPx(16).toFloat()
            textAlign = Paint.Align.LEFT
            typeface = ResourcesCompat.getFont(context, R.font.gotham_medium)
        }

        val textContainer = Rect()

        val cHeight = container.height()
        val cWidth = container.width()

        text?.let {
            textPaint.getTextBounds(text, 0, text.length, textContainer)

            val tx = (cWidth - textContainer.width()) / 2f - textContainer.left.toFloat()
            val ty = (cHeight + textContainer.height()) / 2f - textContainer.bottom

            c.drawText(it, container.left + tx, container.top + ty, textPaint) }
    }

    private fun generateIconOnly(
        container: RectF,
        c: Canvas,
        imgResId: Int?
    ) {
        val imgDrawable = imgResId?.let {
            ContextCompat.getDrawable(
                context,
                it
            )
        }

        val newDrawable = BitmapDrawable(
            context.resources, imgDrawable?.toBitmap()?.let {
                Bitmap.createScaledBitmap(
                    it, Utils.dpToPx(22), Utils.dpToPx(24), true
                )
            }
        )

        val cHeight = container.height()
        val cWidth = container.width()
        var iy = 0f

        newDrawable.let {
            val iconPaint = Paint().apply {
                if (iconColorTint != null) {
                    color = iconColorTint
                }
                colorFilter = PorterDuffColorFilter(Color.WHITE, PorterDuff.Mode.SRC_IN)
            }

            val imgBitmap = Utils.drawableToBitmap(it)
            val ix = (cWidth - imgBitmap.width) / 2f
            iy = if (text != null) {
                    (cHeight / 1.5f - imgBitmap.height) / 2f
            } else {
                (cHeight - imgBitmap.height) / 2f
            }

            c.drawBitmap(
                imgBitmap,
                container.left + ix,
                container.top + iy,
                iconPaint
            )
        }

        // Draw Text
        val textPaint = Paint().apply {
            color = Color.WHITE
            textSize = Utils.dpToPx(12).toFloat()
            textAlign = Paint.Align.LEFT
        }

        c.drawColor(Color.TRANSPARENT)

        if (text != null) {
            val textContainer = Rect()
            textPaint.getTextBounds(text, 0, text.length, textContainer)
            val tx = (cWidth - textContainer.width()) / 2f - textContainer.left.toFloat()
            val ty = (cHeight + textContainer.height()) / 2f - textContainer.bottom
            c.drawText(text, container.left + tx, container.top + ty + iy / 3, textPaint)
        }
    }

    interface ButtonClickListener {
        fun onClick(pos: Int)
    }
}
