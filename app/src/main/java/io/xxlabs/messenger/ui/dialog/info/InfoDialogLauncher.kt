package io.xxlabs.messenger.ui.dialog.info

import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModel
import io.xxlabs.messenger.support.appContext

/**
 * Launches an InfoDialog with a neutral button.
 */
@Deprecated("Fragments should receive the DialogUI from their ViewModel.")
fun Fragment.showInfoDialog(
    title: Int,
    body: Int,
    linkTextToUrlMap: Map<String, String>? = null
) {
    var spans: MutableList<SpanConfig>? = null
    linkTextToUrlMap?.apply {
        spans = mutableListOf()
        for (entry in keys) {
            val spanConfig = SpanConfig.create(
                entry,
                this[entry],
            )
            spans?.add(spanConfig)
        }
    }
    val ui = InfoDialogUI.create(
        title = getString(title),
        body = getString(body),
        spans = spans,
    )
    InfoDialog.newInstance(ui)
        .show(requireActivity().supportFragmentManager, null)
}

fun ViewModel.createInfoDialog(
    title: Int,
    body: Int,
    linkTextToUrlMap: Map<String, String>? = null
) : InfoDialogUI {
    var spans: MutableList<SpanConfig>? = null
    linkTextToUrlMap?.apply {
        spans = mutableListOf()
        for (entry in keys) {
            val spanConfig = SpanConfig.create(
                entry,
                this[entry],
            )
            spans?.add(spanConfig)
        }
    }

    return InfoDialogUI.create(
        title = appContext().getString(title),
        body = appContext().getString(body),
        spans = spans,
    )
}

/**
 * Launches an InfoDialog with a positive and negative button.
 */
@Deprecated("Fragments should receive the DialogUI from their ViewModel.")
fun Fragment.showTwoButtonInfoDialog(
    title: Int,
    body: Int,
    linkTextToUrlMap: Map<String, String>? = null,
    positiveClick: ()-> Unit,
    negativeClick: (()-> Unit)? = null,
    onDismiss: ()-> Unit = { },
) {
    var spans: MutableList<SpanConfig>? = null
    linkTextToUrlMap?.apply {
        spans = mutableListOf()
        for (entry in keys) {
            val spanConfig = SpanConfig.create(
                entry,
                this[entry],
            )
            spans?.add(spanConfig)
        }
    }
    val infoDialogUI = InfoDialogUI.create(
        title = getString(title),
        body = getString(body),
        spans = spans,
        onDismiss
    )
    val twoButtonUI = TwoButtonInfoDialogUI.create(
        infoDialogUI,
        onPositiveClick = positiveClick,
        onNegativeClick = negativeClick
    )
    TwoButtonInfoDialog.newInstance(twoButtonUI)
        .show(parentFragmentManager, null)
}

fun ViewModel.createTwoButtonDialogUi(
    title: Int,
    body: Int,
    linkTextToUrlMap: Map<String, String>? = null,
    positiveClick: ()-> Unit,
    negativeClick: (()-> Unit)? = null,
    onDismiss: ()-> Unit = { },
) : TwoButtonInfoDialogUI {
    var spans: MutableList<SpanConfig>? = null
    linkTextToUrlMap?.apply {
        spans = mutableListOf()
        for (entry in keys) {
            val spanConfig = SpanConfig.create(
                entry,
                this[entry],
            )
            spans?.add(spanConfig)
        }
    }
    val infoDialogUI = InfoDialogUI.create(
        title = appContext().getString(title),
        body = appContext().getString(body),
        spans = spans,
        onDismiss
    )

    return TwoButtonInfoDialogUI.create(
        infoDialogUI,
        onPositiveClick = positiveClick,
        onNegativeClick = negativeClick
    )
}
