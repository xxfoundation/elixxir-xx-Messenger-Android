package io.xxlabs.messenger.support.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.xxlabs.messenger.R
import timber.log.Timber

class PhotoSelectorDialog(
    val onClickDelete: (() -> Unit)? = null,
    val onClickTakePhoto: () -> Unit = { },
    val onClickChoose: () -> Unit = { },
    val onClickCancel: () -> Unit = { }
) : BottomSheetDialogFragment() {
    lateinit var root: View
    lateinit var photoSelectorMenuDelete: LinearLayout
    lateinit var photoSelectorMenuTakePhoto: LinearLayout
    lateinit var photoSelectorMenuChoose: LinearLayout
    lateinit var photoSelectorMenuCancel: LinearLayout

    lateinit var mBehavior: BottomSheetBehavior<FrameLayout>
    private val mBottomSheetBehaviorCallback = object : BottomSheetBehavior.BottomSheetCallback() {
        override fun onSlide(sheet: View, p1: Float) {}

        override fun onStateChanged(sheet: View, state: Int) {
            Timber.v("ON STATE CHANGED $state")
            when (state) {
                BottomSheetBehavior.STATE_COLLAPSED -> {
                    mBehavior.state = BottomSheetBehavior.STATE_HIDDEN
                }
                BottomSheetBehavior.STATE_DRAGGING -> {
                    Timber.v("DRAGGING")
                }
                BottomSheetBehavior.STATE_SETTLING -> {
                    Timber.v("SETTLING")
                }

                else -> {}
            }
        }
    }

    @Nullable
    override fun onCreateView(
        @NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.component_photo_selection_menu, container, false)
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.XxBottomSheetDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            dialog.window!!.setLayout(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.MATCH_PARENT
            )
            dialog.window!!.setGravity(Gravity.BOTTOM)

            setBehavior()
            bindListeners()
        }
        return dialog
    }

    private fun bindListeners() {
        if (onClickDelete == null) {
            photoSelectorMenuDelete.visibility = View.GONE
        } else {
            photoSelectorMenuDelete.setOnClickListener {
                dismiss()
                onClickDelete.invoke()
            }
        }

        photoSelectorMenuTakePhoto.setOnClickListener {
            dismiss()
            onClickTakePhoto()
        }
        photoSelectorMenuChoose.setOnClickListener {
            dismiss()
            onClickChoose()
        }

        photoSelectorMenuCancel.setOnClickListener {
            dismiss()
            onClickCancel()
        }
    }

    private fun setBehavior() {
        mBehavior = BottomSheetBehavior.from(root.parent as FrameLayout)
        mBehavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)
        mBehavior.isHideable = true
        mBehavior.peekHeight = 0
        mBehavior.skipCollapsed = true
        root.parent.requestLayout()
        mBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        photoSelectorMenuDelete = view.findViewById(R.id.photoSelectorDeletePhoto)
        photoSelectorMenuTakePhoto = view.findViewById(R.id.photoSelectorTakePhoto)
        photoSelectorMenuChoose = view.findViewById(R.id.photoSelectorChoosePhoto)
        photoSelectorMenuCancel = view.findViewById(R.id.photoSelectorCancel)
    }

    companion object {
        fun getInstance(
            onClickDelete: (() -> Unit)? = null,
            onClickTakePhoto: () -> Unit = { },
            onClickChoose: () -> Unit = { },
            onClickCancel: () -> Unit = { },
        ): PhotoSelectorDialog {
            return PhotoSelectorDialog(
                onClickDelete,
                onClickTakePhoto,
                onClickChoose,
                onClickCancel,
            )
        }
    }
}