package io.xxlabs.messenger.ui.main.chat.viewholders

import android.app.Dialog
import android.content.DialogInterface
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.webkit.WebView
import android.widget.TextView
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.DialogFragment
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ComponentWebviewDialogBinding
import io.xxlabs.messenger.support.extensions.setInsets

class WebViewDialog(private val dialogUI: WebViewDialogUI) : DialogFragment() {

    private lateinit var binding: ComponentWebviewDialogBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.component_webview_dialog,
            container,
            false
        )

        binding.webViewToolbar.toolbarGenericBackBtn.setOnClickListener {
            dismiss()
            dialogUI.onDismissed?.invoke()
        }

        binding.webviewDialogWebView.apply {
            isVerticalScrollBarEnabled = true
            isScrollbarFadingEnabled = false
            scrollBarStyle = WebView.SCROLLBARS_OUTSIDE_OVERLAY
            settings.javaScriptEnabled = true
            settings.domStorageEnabled = true
            loadUrl(dialogUI.url)
        }
        binding.ui = dialogUI
        binding.root.setInsets(
            bottomMask = WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.ime()
        )
        return binding.root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.XxFullscreenDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            WindowCompat.setDecorFitsSystemWindows(dialog.window!!, true)
        }
        return dialog
    }
//
//    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
//        super.onViewCreated(view, savedInstanceState)
//        binding.webViewToolbar.toolbarGeneric.setInsets(
//            topMask = WindowInsetsCompat.Type.systemBars()
//        )
//    }

    override fun onDismiss(dialog: DialogInterface) {
        dialogUI.onDismissed?.invoke()
        super.onDismiss(dialog)
    }
}