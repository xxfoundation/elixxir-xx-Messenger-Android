package io.xxlabs.messenger.support.extensions

import android.text.Editable
import android.text.InputType
import android.text.TextUtils
import android.text.TextWatcher
import android.text.method.LinkMovementMethod
import android.text.method.PasswordTransformationMethod
import android.util.Patterns
import android.widget.EditText
import android.widget.TextView
import androidx.core.text.HtmlCompat
import androidx.core.view.doOnLayout
import androidx.core.view.inputmethod.EditorInfoCompat
import com.google.android.material.textfield.TextInputLayout
import timber.log.Timber
import java.util.*
import java.util.regex.Pattern

fun TextView.formatBold() {
    //val pxValue = Util.dpToPx(24f)
    var formattedText: String = text.toString()

    val patternB = Pattern.compile(
        Pattern.quote("[B]")
                + "(.*?)"
                + Pattern.quote("[B]")
    )

    val matcherB = patternB.matcher(text)
    while (matcherB.find()) {
        Timber.d(matcherB.group(0))
        val replaceText =
            "<b>" + matcherB.group(1) + "</b>"
        formattedText = formattedText.replace(matcherB.group(0)!!, replaceText)
    }

    text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
}

fun TextView.formatTypedLink() {
    var formattedText: String = text.toString()

    val patternB = Pattern.compile(
        Pattern.quote("[L]")
                + "(.*?)"
                + Pattern.quote("[L]")
    )

    val matcherB = patternB.matcher(text)
    while (matcherB.find()) {
        Timber.d(matcherB.group(0))
        val replaceText =
            "<a href=\"https://" + matcherB.group(1) + "\">${matcherB.group(1)}</a>"
        formattedText = formattedText.replace(matcherB.group(0)!!, replaceText)
    }

    text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.formatLineBreak() {
    val formattedText: String = text.toString().replace("\n", "<br>")
    text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.formatUrlLink() {
    var formattedText: String = text.toString()

    val patternUrl = Patterns.WEB_URL

    val matcherUrl = patternUrl.matcher(text)
    while (matcherUrl.find()) {
        Timber.d(matcherUrl.group(0))
        val replaceText =
            "<font color=\"#fff\"><a href=\"http://" + matcherUrl.group(1) + "\">${
                matcherUrl.group(1)
            }</a></font>"
        formattedText = formattedText.replace(matcherUrl.group(0)!!, replaceText)
    }

    text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    movementMethod = LinkMovementMethod.getInstance()
}

fun TextView.formatEmail(mailto: String) {
    //val pxValue = Util.dpToPx(24f)
    var formattedText: String = text.toString()

    val patternB = Pattern.compile(
        Pattern.quote("[M]")
                + "(.*?)"
                + Pattern.quote("[M]")
    )

    val matcherM = patternB.matcher(text)
    while (matcherM.find()) {
        Timber.d(matcherM.group(0))
        val replaceText =
            "<a href=\"mailto:$mailto\"><u>" + matcherM.group(1) + "</u></a>"
        formattedText = formattedText.replace(matcherM.group(0)!!, replaceText)
    }

    text = HtmlCompat.fromHtml(formattedText, HtmlCompat.FROM_HTML_MODE_LEGACY)
    movementMethod = LinkMovementMethod.getInstance()
}

fun TextInputLayout.matches(anotherInput: TextInputLayout, errMsg: String? = null): Boolean {
    val currText = this.editText?.text.toString()
    val anotherText = anotherInput.editText?.text.toString()
    val areEqual = currText == anotherText

    if (areEqual) {
        this.error = null
    } else {
        if (currText.isNotEmpty()) {
            this.error = errMsg
        } else {
            this.error = null
        }

    }

    return areEqual
}

fun TextInputLayout.setHintStyle(id: Int) {
    doOnLayout {
        setHintTextAppearance(id)
    }
}

fun String.capitalizeWords(): String =
    split(" ").joinToString(" ") { it.lowercase(Locale.getDefault())
        .replaceFirstChar { if (it.isLowerCase()) it.titlecase(Locale.getDefault()) else it.toString() } }

/* EditText extension functions */
fun EditText.afterTextChanged(afterTextChanged: (String) -> Unit) {
    this.addTextChangedListener(object : TextWatcher {
        override fun afterTextChanged(s: Editable?) {
            afterTextChanged.invoke(s.toString())
        }

        override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}

        override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
    })
}

fun TextInputLayout.setEndIconOnClickListener() {
    val editText = editText
    // Store the current cursor position
    val selection = editText?.selectionEnd ?: 0

    // Check for existing password transformation
    val hasPasswordTransformation = editText?.transformationMethod is PasswordTransformationMethod
    if (hasPasswordTransformation) {
        editText?.transformationMethod = null
    } else {
        editText?.transformationMethod = PasswordTransformationMethod.getInstance()
    }

    // Restore the cursor position
    editText?.setSelection(selection)

    // Add additional functionality here
}

fun EditText.matches(anotherInput: EditText, errMsg: String? = null): Boolean {
    val areEqual = TextUtils.equals(this.text, anotherInput.text)

    if (areEqual) {
        this.error = null
    } else {
        this.error = errMsg
    }

    return areEqual
}

fun EditText.incognito(on: Boolean) {
    imeOptions = if (on) {
        imeOptions or EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING
    } else {
        imeOptions and EditorInfoCompat.IME_FLAG_NO_PERSONALIZED_LEARNING.inv()
    }

    inputType = if (on) {
        when (inputType) {
            (InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) -> {
                inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_MULTI_LINE -> {
                inputType or InputType.TYPE_TEXT_VARIATION_FILTER or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }
            InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS -> {
                inputType or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }

            InputType.TYPE_CLASS_TEXT or InputType.TYPE_CLASS_PHONE -> {
                inputType
            }

            else -> {
                InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD or InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS
            }
        }
    } else {
        if (inputType == InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD) {
            inputType
        } else {
            inputType and InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD.inv() and
                    InputType.TYPE_TEXT_VARIATION_FILTER.inv() and
                    InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS.inv()
        }
    }
}

fun EditText.setEditableMode(editable: Boolean) {
    if (editable) {
        isFocusableInTouchMode = true
        isFocusable = true
        isLongClickable = true
    } else {
        isFocusableInTouchMode = false
        isFocusable = false
        isLongClickable = false
    }
}

fun EditText.setViewableMode(isViewable: Boolean) {
    isFocusableInTouchMode = false
    isFocusable = false
    isLongClickable = false
    isClickable = !isViewable
}
fun EditText.removeStartPadding() {
    setPadding(0, paddingTop, paddingRight, paddingBottom)
}
