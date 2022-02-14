package io.xxlabs.messenger.support.view

import android.content.Context
import android.graphics.Canvas
import android.graphics.Path
import android.util.AttributeSet
import android.widget.FrameLayout
import androidx.core.content.ContextCompat
import io.xxlabs.messenger.R

class RoundedCornerLayout : FrameLayout {
    private val path = Path()

    constructor(context: Context?) : super(context!!) {
        defineBg()
    }

    constructor(context: Context?, attrs: AttributeSet?) : super(
        context!!, attrs
    ) {
        defineBg()
    }

    constructor(context: Context?, attrs: AttributeSet?, defStyle: Int) : super(
        context!!, attrs, defStyle
    ) {
        defineBg()
    }

    private fun defineBg() {
        background = ContextCompat.getDrawable(context, R.drawable.component_circular_bg)
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        // compute the path
        val halfWidth = w / 2f
        val halfHeight = h / 2f
        path.reset()
        path.addCircle(
            halfWidth,
            halfHeight,
            halfWidth.coerceAtMost(halfHeight),
            Path.Direction.CW
        )
        path.close()
    }

    override fun dispatchDraw(canvas: Canvas) {
        val save = canvas.save()
        canvas.clipPath(path)
        super.dispatchDraw(canvas)
        canvas.restoreToCount(save)
    }
}