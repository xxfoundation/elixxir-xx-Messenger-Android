package io.xxlabs.messenger.support.view

import android.content.Context
import android.util.AttributeSet
import android.view.View.OnClickListener
import androidx.appcompat.widget.AppCompatButton
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView

/**
 * A debounced [AppCompatButton] subclass that can only be clicked once.
 */
class SingleClickButton : AppCompatButton {

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    constructor(
        context: Context?, attrs: AttributeSet?, defStyle: Int
    ) : super(context!!, attrs, defStyle)

    private var listener: OnClickListener? = null
    private var debouncedListener = OnClickListener {
        listener?.let{
            isEnabled = false
            it.onClick(this)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        listener = l
        super.setOnClickListener(debouncedListener)
    }
}

/**
 * A debounced [AppCompatImageView] subclass that can only be clicked once.
 */
class SingleClickImageView : AppCompatImageView {

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    constructor(
        context: Context?, attrs: AttributeSet?, defStyle: Int
    ) : super(context!!, attrs, defStyle)

    private var listener: OnClickListener? = null
    private var debouncedListener = OnClickListener {
        listener?.let{
            isEnabled = false
            it.onClick(this)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        listener = l
        super.setOnClickListener(debouncedListener)
    }
}

/**
 * A debounced [AppCompatTextView] subclass that can only be clicked once.
 */
class SingleClickTextView : AppCompatTextView {

    constructor(context: Context?) : super(context!!)

    constructor(context: Context?, attrs: AttributeSet?) : super(context!!, attrs)

    constructor(
        context: Context?, attrs: AttributeSet?, defStyle: Int
    ) : super(context!!, attrs, defStyle)

    private var listener: OnClickListener? = null
    private var debouncedListener = OnClickListener {
        listener?.let{
            isEnabled = false
            it.onClick(this)
        }
    }

    override fun setOnClickListener(l: OnClickListener?) {
        listener = l
        super.setOnClickListener(debouncedListener)
    }
}