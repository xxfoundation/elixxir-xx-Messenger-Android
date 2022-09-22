package io.xxlabs.messenger.ui.main.contacts.select

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.databinding.ListItemSelectableConnectionBinding
import io.xxlabs.messenger.ui.main.contacts.list.SelectableContact

class SelectableContactsAdapter : RecyclerView.Adapter<SelectableViewHolder>(){
    private var connectionsList: List<SelectableContact> = mutableListOf()

    fun submitList(connections: List<SelectableContact>) {
        connectionsList = connections
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SelectableViewHolder =
        SelectableViewHolder.create(parent)

    override fun onBindViewHolder(holder: SelectableViewHolder, position: Int) =
        holder.onBind(connectionsList[position])

    override fun getItemCount(): Int = connectionsList.size
}

open class SelectableViewHolder(
    private val binding: ListItemSelectableConnectionBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(ui: SelectableContact) {
        binding.ui = ui
    }

    companion object {
        fun create(parent: ViewGroup): SelectableViewHolder {
            val binding = ListItemSelectableConnectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SelectableViewHolder(binding)
        }
    }
}