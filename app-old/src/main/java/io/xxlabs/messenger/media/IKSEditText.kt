package io.xxlabs.messenger.media

import android.annotation.SuppressLint
import android.content.Context
import android.util.AttributeSet
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputConnection
import android.widget.EditText
import androidx.core.os.BuildCompat
import androidx.core.view.inputmethod.EditorInfoCompat
import androidx.core.view.inputmethod.InputConnectionCompat
import androidx.core.view.inputmethod.InputConnectionCompat.OnCommitContentListener
import androidx.core.view.inputmethod.InputContentInfoCompat

@SuppressLint("AppCompatCustomView")
class IKSEditText : EditText {

    constructor(context: Context?) : super(context)
    constructor(context: Context?, attrs: AttributeSet?) : super(context, attrs)
    constructor(context: Context?, attrs: AttributeSet?, defStyleAttr: Int) : super(
        context,
        attrs,
        defStyleAttr
    )

    private var listener: IKSEditTextListener? = null

    override fun onCreateInputConnection(editorInfo: EditorInfo): InputConnection {
        val ic = super.onCreateInputConnection(editorInfo)
        EditorInfoCompat.setContentMimeTypes(editorInfo, arrayOf("image/*"))
        val callback = OnCommitContentListener { inputContentInfo, flags, _ ->
            // read and display inputContentInfo asynchronously
            if (BuildCompat.isAtLeastNMR1() && flags and
                InputConnectionCompat.INPUT_CONTENT_GRANT_READ_URI_PERMISSION != 0
            ) {
                try {
                    inputContentInfo.requestPermission()
                } catch (e: Exception) {
                    return@OnCommitContentListener false // return false if failed
                }
            }

            // read and display inputContentInfo asynchronously.
            // call inputContentInfo.releasePermission() as needed.
            listener?.receivedContent(inputContentInfo)
            true // return true if succeeded
        }
        return InputConnectionCompat.createWrapper(ic, editorInfo, callback)
    }

    fun setListener(listener: IKSEditTextListener?) {
        this.listener = listener
    }

    interface IKSEditTextListener {
        fun receivedContent(contentInfo: InputContentInfoCompat)
    }
}