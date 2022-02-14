package io.xxlabs.messenger.support.view

import android.content.Context
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.drawable.BitmapDrawable
import android.text.Editable
import android.text.Spannable
import android.text.SpannableStringBuilder
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.style.ClickableSpan
import android.text.style.ForegroundColorSpan
import android.text.style.ImageSpan
import android.text.style.TextAppearanceSpan
import android.util.AttributeSet
import android.util.TypedValue
import android.view.Gravity
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.widget.AppCompatEditText
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.util.Utils
import kotlin.math.min

class TagEditText(context: Context, attrs: AttributeSet?) : AppCompatEditText(context, attrs) {
    private var isViewMode: Boolean = false
    var textWatcher: TextWatcher? = null
    var lastString: String? = null
    var separator = ","
    var spaceSeparator = "   "
    var viewModeSeparator = "  "

    init {
        movementMethod = LinkMovementMethod.getInstance()
        textWatcher = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {}
            override fun afterTextChanged(s: Editable) {
                removeTextChangedListener(this)
                val thisString = s.toString()
                if (thisString.isNotEmpty() && thisString != lastString) {
                    format(separator)
                    if (isViewMode) {
                        text?.replace(separator.toRegex(), spaceSeparator)
                        format(spaceSeparator)
                    }
                }
                addTextChangedListener(this)
            }
        }

        addTextChangedListener(textWatcher)
    }

    private fun format(separator: String) {
        val sb = SpannableStringBuilder()
        var fullString = text.toString()
        val v1 = fullString.length - 1
        val v2 = separator.length
        val v3 = fullString.length - 1 - separator.length
        val v4 = fullString.length - 1 - separator.length * 2

        if (fullString.length >= (2 * separator.length) &&
            fullString.substring(v3, v1) == separator
            && fullString.substring(v4, v1) == separator
        ) {
            fullString = fullString.substring(0, fullString.length - 1)
        }

        if (fullString.startsWith(separator) && fullString.length > separator.length) {
            fullString = fullString.substring(1, fullString.length - 1)
        } else if (fullString.startsWith(separator) && fullString.length == separator.length) {
            fullString = ""
        }

        val strings = fullString.split(separator.toRegex()).toTypedArray()

        if (fullString.isNotEmpty()) {
            for (i in strings.indices) {
                val string = strings[i]
                sb.append(string)

                if (!fullString.endsWith(separator) && i == strings.size - 1) {
                    break
                }

                val startIdx = sb.length - string.length
                val endIdx = sb.length

                val bd = convertViewToDrawable(createTokenView(string)) as BitmapDrawable
                bd.setBounds(
                    0,
                    0,
                    bd.intrinsicWidth,
                    bd.intrinsicHeight
                )

                sb.setSpan(ImageSpan(bd), startIdx, endIdx, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE)

                if (i < strings.size - 1) {
                    if (isViewMode) {
                        sb.append(viewModeSeparator)
                    } else {
                        sb.append(separator)
                    }
                } else if (isViewMode) {
                    sb.append(viewModeSeparator)
                }
            }
        }

        lastString = sb.toString()
        text = sb
        setSelection(sb.length)
    }

    fun setVisibilityMode(isViewMode: Boolean) {
        this.isViewMode = isViewMode
        this.isFocusable = !isViewMode
        this.isFocusableInTouchMode = !isViewMode
    }

    fun createTokenView(text: String?): View {
        val l = LinearLayout(context)
        l.orientation = LinearLayout.HORIZONTAL
        l.gravity = Gravity.CENTER_VERTICAL
        l.setBackgroundResource(R.drawable.bg_rectangle_rounded_corners)
        val params = LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.WRAP_CONTENT,
            LinearLayout.LayoutParams.WRAP_CONTENT
        )
        params.setMargins(Utils.dpToPx(8), Utils.dpToPx(8), Utils.dpToPx(8), Utils.dpToPx(8))
        l.layoutParams = params
        l.setPadding(
            Utils.dpToPx(25),
            Utils.dpToPx(10),
            Utils.dpToPx(25),
            Utils.dpToPx(10)
        )

        val textView = TextView(context)
        textView.setTextColor(Color.WHITE)
        val sb = SpannableStringBuilder()
        sb.append(text)

        sb.setSpan(
            TextAppearanceSpan(context, R.style.XxTextStyle_Bold),
            0,
            sb.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        sb.setSpan(
            ForegroundColorSpan(Color.WHITE),
            0,
            sb.length,
            Spannable.SPAN_EXCLUSIVE_EXCLUSIVE
        )

        l.addView(textView)
        textView.text = sb
        textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 14f)
        return l
    }

    private fun convertViewToDrawable(view: View): Any {
        val spec = MeasureSpec.makeMeasureSpec(0, MeasureSpec.UNSPECIFIED)
        view.measure(spec, spec)
        view.layout(0, 0, view.measuredWidth, view.measuredHeight)
        val b =
            Bitmap.createBitmap(view.measuredWidth, view.measuredHeight, Bitmap.Config.ARGB_8888)
        val c = Canvas(b)
        c.translate(-view.scrollX.toFloat(), -view.scrollY.toFloat())
        view.draw(c)
        val cacheBmp = Utils.getBitmapFromView(view)
        val viewBmp = cacheBmp?.copy(Bitmap.Config.ARGB_8888, true)
        return BitmapDrawable(context.resources, viewBmp)
    }

    private inner class MyClickableSpan(var startIdx: Int, var endIdx: Int) : ClickableSpan() {
        override fun onClick(widget: View) {
            val s = text.toString()
            val s1 = s.substring(0, startIdx)
            val s2 = s.substring(min(endIdx + 1, s.length - 1), s.length)
            val s3 = s1 + s2
            this@TagEditText.setText(s3)
        }
    }
}