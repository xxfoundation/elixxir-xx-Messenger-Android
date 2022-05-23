package io.xxlabs.messenger.requests.ui.details.group.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.databinding.ListItemGroupMemberBinding

class MemberViewHolder(
    private val binding: ListItemGroupMemberBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(ui: MemberItem) {
        binding.ui = ui
    }

    companion object {
        fun create(parent: ViewGroup): MemberViewHolder {
            val binding = ListItemGroupMemberBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return MemberViewHolder(binding)
        }
    }
}