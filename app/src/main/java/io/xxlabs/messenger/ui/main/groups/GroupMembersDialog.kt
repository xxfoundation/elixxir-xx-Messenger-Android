package io.xxlabs.messenger.ui.main.groups

import android.app.Dialog
import android.graphics.drawable.GradientDrawable
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.annotation.NonNull
import androidx.annotation.Nullable
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.core.view.WindowInsetsCompat
import androidx.fragment.app.DialogFragment
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputEditText
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.AvatarWrapper
import io.xxlabs.messenger.support.extensions.afterTextChanged
import io.xxlabs.messenger.support.extensions.setInsets

class GroupMembersDialog : DialogFragment() {
    lateinit var root: View
    lateinit var groupMembersRecyclerView: RecyclerView
    lateinit var searchInput: TextInputEditText
    private lateinit var membersAdapter: GroupMembersAdapter
    lateinit var backBtn: ImageView
    var currentList: List<AvatarWrapper> = listOf()

    init {
        isCancelable = true
    }

    @Nullable
    override fun onCreateView(
        @NonNull inflater: LayoutInflater, @Nullable container: ViewGroup?,
        @Nullable savedInstanceState: Bundle?
    ): View {
        root = inflater.inflate(R.layout.component_group_chat_members_fullscreen, container, false)
        root.setInsets(bottomMask = WindowInsetsCompat.Type.systemBars() + WindowInsetsCompat.Type.ime())

        bindViews(root)
        addGroupAvatar()
        bindListeners()
        return root
    }

    private fun bindViews(root: View) {
        groupMembersRecyclerView = root.findViewById(R.id.groupMembersRecyclerView)
        searchInput = root.findViewById(R.id.groupMembersInput)
        backBtn = root.findViewById(R.id.toolbarGenericBackBtn)
        root.findViewById<TextView>(R.id.toolbarGenericTitle).text = "Group Members"
        val toolbar = root.findViewById<ViewGroup>(R.id.toolbarGeneric)
        toolbar.setInsets(topMask = WindowInsetsCompat.Type.systemBars())
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setStyle(STYLE_NO_TITLE, R.style.XxFullscreenDialog)
    }

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        val dialog = super.onCreateDialog(savedInstanceState)
        dialog.setOnShowListener {
            WindowCompat.setDecorFitsSystemWindows(dialog.window!!, false)
            membersAdapter.updateAvatars(currentList)
        }
        return dialog
    }

    private fun bindListeners() {
        backBtn.setOnClickListener {
            dismiss()
        }

        searchInput.afterTextChanged {
            (groupMembersRecyclerView.adapter as GroupMembersAdapter).filter(it)
        }
    }

    fun addAvatars(list: List<AvatarWrapper>) {
        currentList = list
    }

    private fun addGroupAvatar() {
        membersAdapter = GroupMembersAdapter()
        val layoutManager = LinearLayoutManager(requireContext())

        groupMembersRecyclerView.layoutManager = layoutManager
        groupMembersRecyclerView.adapter = membersAdapter

        val itemDecoration =
            DividerItemDecoration(context, layoutManager.orientation)
        val color = ContextCompat.getColor(requireContext(), R.color.toolbarBarColor)
        val drawable = GradientDrawable(
            GradientDrawable.Orientation.BOTTOM_TOP,
            intArrayOf(color, color)
        )
        drawable.setSize(1, 1)
        itemDecoration.setDrawable(drawable)

        groupMembersRecyclerView.addItemDecoration(itemDecoration)
    }

    companion object {
        fun getInstance(): GroupMembersDialog {
            return GroupMembersDialog()
        }
    }
}