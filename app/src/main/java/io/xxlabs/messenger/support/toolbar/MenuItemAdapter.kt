package io.xxlabs.messenger.support.toolbar

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.databinding.ListItemIconMenuItemBinding
import io.xxlabs.messenger.databinding.ListItemLabelMenuItemBinding

abstract class MenuItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun onBind(ui: ToolbarMenuItem)
}

class IconMenuItemViewHolder(
    private val binding: ListItemIconMenuItemBinding
) : MenuItemViewHolder(binding.root) {

    override fun onBind(ui: ToolbarMenuItem) {
        binding.ui = ui
    }

    companion object {
        fun create(parent: ViewGroup): IconMenuItemViewHolder {
            val binding = ListItemIconMenuItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return IconMenuItemViewHolder(binding)
        }
    }
}
class LabelMenuItemViewHolder(
    private val binding: ListItemLabelMenuItemBinding
): MenuItemViewHolder(binding.root) {

    override fun onBind(ui: ToolbarMenuItem) {
        binding.ui = ui
    }

    companion object {
        fun create(parent: ViewGroup): LabelMenuItemViewHolder {
            val binding = ListItemLabelMenuItemBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return LabelMenuItemViewHolder(binding)
        }
    }
}

class MenuItemAdapter : ListAdapter<ToolbarMenuItem, MenuItemViewHolder>(MenuItemDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MenuItemViewHolder =
        when (viewType) {
            VIEWTYPE_ICON -> IconMenuItemViewHolder.create(parent)
            else -> LabelMenuItemViewHolder.create(parent)
        }

    override fun onBindViewHolder(holder: MenuItemViewHolder, position: Int) =
        holder.onBind(currentList[position])

    override fun getItemViewType(position: Int): Int =
        with (currentList[position]) {
            when {
                icon != null -> VIEWTYPE_ICON
                else -> VIEWTYPE_LABEL
            }
        }

    companion object {
        private const val VIEWTYPE_ICON = 1
        private const val VIEWTYPE_LABEL = 10
    }
}

class MenuItemDiffCallback : DiffUtil.ItemCallback<ToolbarMenuItem>() {
    override fun areItemsTheSame(oldItem: ToolbarMenuItem, newItem: ToolbarMenuItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: ToolbarMenuItem, newItem: ToolbarMenuItem): Boolean =
        oldItem == newItem
}