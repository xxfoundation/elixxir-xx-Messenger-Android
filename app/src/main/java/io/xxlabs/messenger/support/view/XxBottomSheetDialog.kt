package io.xxlabs.messenger.support.view

import android.os.Bundle
import android.view.View
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.xxlabs.messenger.R

/**
 * Superclass for BottomSheetDialogFragments that launch already expanded.
 */
abstract class XxBottomSheetDialog : BottomSheetDialogFragment() {

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (dialog as? BottomSheetDialog)?.behavior?.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun getTheme(): Int = R.style.RoundedModalBottomSheetDialog

    override fun onPause() {
        super.onPause()
        dismiss()
    }
}