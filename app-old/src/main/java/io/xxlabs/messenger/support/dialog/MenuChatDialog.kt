package io.xxlabs.messenger.support.dialog

import android.app.Dialog
import android.os.Bundle
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import com.google.android.material.bottomsheet.BottomSheetBehavior
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import io.xxlabs.messenger.R
import io.xxlabs.messenger.support.extensions.setOnSingleClickListener
import timber.log.Timber

class MenuChatDialog constructor(
    val viewTitle: String? = null,
    val searchTitle: String? = null,
    val muteTitle: String? = null,
    val clearTitle: String? = null,
    val deleteTitle: String? = null,
    val onClickPositive: () -> Unit = { },
    val onClickSearch: () -> Unit = { },
    val onClickMute: () -> Unit = { },
    val onClickClearChat: () -> Unit = { },
    val onClickDeleteContact: () -> Unit = { }
) : BottomSheetDialogFragment() {
    lateinit var root: View
    lateinit var menuChatPopupViewContact: LinearLayout
    lateinit var menuChatPopupSearch: LinearLayout
    lateinit var menuChatPopupMute: LinearLayout
    lateinit var menuChatPopupClearChat: LinearLayout
    lateinit var menuChatPopupDeleteContact: LinearLayout

    lateinit var menuChatPopupViewContactTitle: TextView
    lateinit var menuChatPopupSearchTitle: TextView
    lateinit var menuChatPopupMuteTitle: TextView
    lateinit var menuChatPopupClearChatTitle: TextView
    lateinit var menuChatPopupDeleteContactTitle: TextView

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

                else -> {
                }
            }
        }
    }

    @Nullable
    override fun onCreateView(
        @NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.component_dots_menu_chat, container, false)
        return root
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NORMAL, R.style.XxBottomSheetDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            dialog.window!!.setGravity(Gravity.BOTTOM)

            setBehavior()
            bindTitles()
            bindListeners()
        }
        return dialog
    }

    private fun bindTitles() {
        viewTitle?.apply {
            menuChatPopupViewContactTitle.text = this
        }

        searchTitle?.apply {
            menuChatPopupSearchTitle.text = this
        }

        muteTitle?.apply {
            menuChatPopupMuteTitle.text = this
        }

        clearTitle?.apply {
            menuChatPopupClearChatTitle.text = this
        }

        deleteTitle?.apply {
            menuChatPopupDeleteContact.visibility = View.VISIBLE
            menuChatPopupDeleteContactTitle.text = this
        }
    }

    private fun bindListeners() {
        menuChatPopupViewContact.setOnSingleClickListener {
            dismiss()
            onClickPositive()
        }
        menuChatPopupSearch.setOnSingleClickListener {
            dismiss()
            onClickSearch()
        }
        menuChatPopupMute.setOnSingleClickListener {
            dismiss()
            onClickMute()
            dismiss()
        }
        menuChatPopupClearChat.setOnSingleClickListener {
            dismiss()
            onClickClearChat()
        }

        menuChatPopupDeleteContact.setOnSingleClickListener {
            dismiss()
            onClickDeleteContact()
        }
    }

    private fun setBehavior() {
        mBehavior = BottomSheetBehavior.from(root.parent as FrameLayout)
        mBehavior.addBottomSheetCallback(mBottomSheetBehaviorCallback)
        mBehavior.isHideable = true
        mBehavior.peekHeight = 0
        mBehavior.skipCollapsed = true
        mBehavior.state = BottomSheetBehavior.STATE_EXPANDED
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        menuChatPopupViewContact = view.findViewById(R.id.menuChatPopupViewContact)
        menuChatPopupSearch = view.findViewById(R.id.menuChatPopupSearch)
        menuChatPopupMute = view.findViewById(R.id.menuChatPopupMute)
        menuChatPopupClearChat = view.findViewById(R.id.menuChatPopupClearChat)
        menuChatPopupDeleteContact = view.findViewById(R.id.menuChatPopupDeleteContact)

        menuChatPopupViewContactTitle = view.findViewById(R.id.menuChatPopupViewContactTitle)
        menuChatPopupSearchTitle = view.findViewById(R.id.menuChatPopupSearchTitle)
        menuChatPopupMuteTitle = view.findViewById(R.id.menuChatPopupMuteTitle)
        menuChatPopupClearChatTitle = view.findViewById(R.id.menuChatPopupClearChatTitle)
        menuChatPopupDeleteContactTitle = view.findViewById(R.id.menuChatPopupDeleteContactTitle)
    }

    companion object {
        fun getInstance(
            viewTitle: String? = null,
            searchTitle: String? = null,
            muteTitle: String? = null,
            clearTitle: String? = null,
            deleteTitle: String? = null,
            onClickViewContact: () -> Unit = { },
            onClickSearch: () -> Unit = { },
            onClickMute: () -> Unit = { },
            onClickClearChat: () -> Unit = { },
            onClickDeleteContact: () -> Unit = { }
        ): MenuChatDialog {
            return MenuChatDialog(
                viewTitle,
                searchTitle,
                muteTitle,
                clearTitle,
                deleteTitle,
                onClickViewContact,
                onClickSearch,
                onClickMute,
                onClickClearChat,
                onClickDeleteContact
            )
        }
    }
}