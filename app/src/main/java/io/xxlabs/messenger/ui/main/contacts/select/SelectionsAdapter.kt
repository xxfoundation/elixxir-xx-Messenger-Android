package io.xxlabs.messenger.ui.main.contacts.select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.databinding.ListItemSelectedContactBinding

class SelectionsAdapter :
    ListAdapter<SelectedContactUI, SelectedContactViewHolder>(SelectedContactDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectedContactViewHolder =
        SelectedContactViewHolder.create(parent)

    override fun onBindViewHolder(holder: SelectedContactViewHolder, position: Int) =
        holder.onBind(currentList[position])
}

class SelectedContactViewHolder(
    private val binding: ListItemSelectedContactBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(ui: SelectedContactUI) {
        binding.ui = ui
    }

    companion object {
        fun create(parent: ViewGroup): SelectedContactViewHolder {
            val binding = ListItemSelectedContactBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SelectedContactViewHolder(binding)
        }
    }
}

class SelectedContactDiffCallback : DiffUtil.ItemCallback<SelectedContactUI>() {
    override fun areItemsTheSame(oldItem: SelectedContactUI, newItem: SelectedContactUI): Boolean =
        oldItem.contact.id == newItem.contact.id

    override fun areContentsTheSame(oldItem: SelectedContactUI, newItem: SelectedContactUI): Boolean =
        oldItem.contact.id == newItem.contact.id
}