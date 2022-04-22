package io.xxlabs.messenger.ui.dialog.info

import androidx.fragment.app.Fragment

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
