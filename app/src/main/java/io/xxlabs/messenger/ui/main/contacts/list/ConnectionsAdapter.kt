package io.xxlabs.messenger.ui.main.contacts.list

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.databinding.ListItemConnectionBinding

class ConnectionsAdapter : RecyclerView.Adapter<ConnectionViewHolder>() {
    private var connectionsList: List<Connection> = mutableListOf()

    fun submitList(connections: List<Connection>) {
        connectionsList = connections
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConnectionViewHolder =
        ConnectionViewHolder.create(parent)

    override fun onBindViewHolder(holder: ConnectionViewHolder, position: Int) =
        holder.onBind(connectionsList[position])

    override fun getItemCount(): Int = connectionsList.size
}

class ConnectionViewHolder(
    private val binding: ListItemConnectionBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(ui: Connection) {
        binding.ui = ui
    }

    companion object {
        fun create(parent: ViewGroup): ConnectionViewHolder {
            val binding = ListItemConnectionBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ConnectionViewHolder(binding)
        }
    }
}