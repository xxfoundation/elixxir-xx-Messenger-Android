package io.xxlabs.messenger.support.view

import android.content.Context
import android.util.AttributeSet
import android.view.Gravity
import android.widget.TextView
import com.google.android.material.textfield.TextInputLayout
import io.xxlabs.messenger.R

class TextInputLayoutXx(context: Context, attrs: AttributeSet) :
    TextInputLayout(context, attrs) {
    override fun setHelperText(helperText: CharSequence?) {
        super.setHelperText(helperText)
        helperText?.let {
            val helperTextView = findViewById<TextView>(R.id.textinput_helper_text)
            helperTextView.gravity = Gravity.END
        }
    }

    override fun setError(errorText: CharSequence?) {
        super.setError(errorText)
        errorText?.let {
            val errorTextView = findViewById<TextView>(R.id.textinput_error)
            errorTextView.gravity = Gravity.END
        }
    }

//    override fun setEndIconMode(endIconMode: Int) {
//        val drawableStart = this.editText?.compoundDrawables
//        super.setEndIconMode(endIconMode)
//        editText?.setCompoundDrawables()
//    }
}