package io.xxlabs.messenger.ui.main.chats.newConnections

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.databinding.ListItemNewConnectionBinding

class NewConnectionsAdapter :
    ListAdapter<NewConnectionUI, NewConnectionViewHolder>(NewConnectionDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewConnectionViewHolder =
        NewConnectionViewHolder.create(parent)

    override fun onBindViewHolder(holder: NewConnectionViewHolder, position: Int) =
        holder.onBind(currentList[position])
}

class NewConnectionViewHolder(
    private val binding: ListItemNewConnectionBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(ui: NewConnectionUI) {
        binding.ui = ui
    }

    companion object {
        fun create(parent: ViewGroup): NewConnectionViewHolder {
            val binding = ListItemNewConnectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return NewConnectionViewHolder(binding)
        }
    }
}

class NewConnectionDiffCallback : DiffUtil.ItemCallback<NewConnectionUI>() {
    override fun areItemsTheSame(oldItem: NewConnectionUI, newItem: NewConnectionUI): Boolean =
        oldItem.contact.id == newItem.contact.id

    override fun areContentsTheSame(oldItem: NewConnectionUI, newItem: NewConnectionUI): Boolean =
        oldItem.contact.id == newItem.contact.id
}