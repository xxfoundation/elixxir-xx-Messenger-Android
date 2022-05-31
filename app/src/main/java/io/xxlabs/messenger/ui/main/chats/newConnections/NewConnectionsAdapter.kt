package io.xxlabs.messenger.ui.main.chats.newConnections

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.databinding.ListItemNewConnectionBinding

class NewConnectionsAdapter : RecyclerView.Adapter<NewConnectionsAdapter.NewConnectionViewHolder>() {

    private var newConnections = listOf<NewConnectionUI>()

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

    fun submitList(newConnections: List<NewConnectionUI>) {
        this.newConnections = newConnections
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewConnectionViewHolder =
        NewConnectionViewHolder.create(parent)

    override fun onBindViewHolder(holder: NewConnectionViewHolder, position: Int) =
        holder.onBind(newConnections[position])

    override fun getItemCount(): Int = newConnections.size
}