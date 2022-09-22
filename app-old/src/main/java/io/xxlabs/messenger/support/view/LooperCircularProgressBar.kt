package io.xxlabs.messenger.support.view

import android.app.Dialog
import android.content.Context
import android.os.Bundle
import android.view.View
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.extensions.formatBold
import kotlinx.android.synthetic.main.component_progress_bar_circular.*

class LooperCircularProgressBar(
    context: Context,
    isCancellable: Boolean = true
) : Dialog(context, R.style.XxLoadingDialog) {
    init {
        setCancelable(isCancellable)
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.component_progress_bar_circular)
    }

    override fun hide() {
        setMsg(null)
        setSecondaryMsg(null)
        if (isShowing) {
            super.dismiss()
        }
    }

    fun setMsg(msg: String?) {
        progressMsg?.apply {
            if (msg.isNullOrEmpty()) {
                visibility = View.INVISIBLE
            } else {
                visibility = View.VISIBLE
                msg.let {
                    text = it
                }
            }
        }
    }

    private fun setSecondaryMsg(msg: String?) {
        progressSecondaryMsg?.apply {
            if (msg.isNullOrEmpty()) {
                visibility = View.INVISIBLE
            } else {
                visibility = View.VISIBLE
                msg.let {
                    text = it
                }
            }
            formatBold()
        }
    }
}
