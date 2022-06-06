package io.xxlabs.messenger.ui.main.qrcode.scan

import android.content.Context
import android.graphics.*
import android.os.Build
import android.os.Build.VERSION_CODES.S
import android.util.AttributeSet
import android.view.SurfaceView
import android.view.View
import androidx.core.graphics.toRectF
import androidx.databinding.BindingAdapter
import io.xxlabs.messenger.R
import kotlin.math.abs

/**
 * A [SurfaceView] with a cutout rounded square inside.
 */
class RoundedSquareMaskView : SurfaceView {

    constructor(context: Context) : super(context)

    constructor(context: Context, attrs: AttributeSet) : super(context, attrs) {
        applyAttributes(attrs)
    }

    constructor(
        context: Context, attrs: AttributeSet, defStyle: Int
    ) : super(context, attrs, defStyle) {
        applyAttributes(attrs)
    }

    constructor(
        context: Context, attrs: AttributeSet, defStyle: Int, defStyleRes: Int
    ) : super(context, attrs, defStyle, defStyleRes) {
        applyAttributes(attrs)
    }

    var strokeColor = context.getColor(R.color.brand_default)
        set(value) {
            field = value
            onValueChanged()
        }

    var strokeWidth: Float = 4F
        set(value) {
            field = value
            onValueChanged()
        }

    var cornerRadius: Float = 32F
        set(value) {
            field = value
            onValueChanged()
        }

    private fun onValueChanged() {
        invalidate()
        requestLayout()
    }

    private fun applyAttributes(attrs: AttributeSet) {
        context.theme.obtainStyledAttributes(
            attrs, R.styleable.RoundedSquareMaskLayout, 0, 0
        ).apply {
            try {
                strokeColor = getColor(
                    R.styleable.RoundedSquareMaskLayout_strokeColor,
                    context.getColor(R.color.brand_default)
                )
                strokeWidth = getDimension(
                    R.styleable.RoundedSquareMaskLayout_strokeWidth,
                    4F
                )
                cornerRadius = getDimension(
                    R.styleable.RoundedSquareMaskLayout_cornerRadius,
                    32F
                )
            } finally {
                recycle()
            }
        }
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        clipOutRoundedSquare(canvas)
    }

    private fun clipOutRoundedSquare(canvas: Canvas) {
        val fill = Paint().apply {
            isAntiAlias = true
            style = Paint.Style.FILL
            xfermode = PorterDuffXfermode(PorterDuff.Mode.CLEAR)
        }

        val outline = Paint().apply {
            isAntiAlias = true
            color = strokeColor
            strokeWidth = this@RoundedSquareMaskView.strokeWidth
            style = Paint.Style.STROKE
        }

        val centeredRoundedSquarePath = Path()
        val left = width * .15F
        val right = width * .85F
        val top = height * .15F
        val bottom = top + abs(left - right)

        centeredRoundedSquarePath.addRoundRect(
            left,
            top,
            right,
            bottom,
            cornerRadius,
            cornerRadius,
            Path.Direction.CW
        )

        canvas.apply {
            drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, fill)
            drawRoundRect(left, top, right, bottom, cornerRadius, cornerRadius, outline)
        }
    }
}

@BindingAdapter("outlineColor")
fun RoundedSquareMaskView.outlineColor(colorId: Int) {
    strokeColor = context.getColor(colorId)
}