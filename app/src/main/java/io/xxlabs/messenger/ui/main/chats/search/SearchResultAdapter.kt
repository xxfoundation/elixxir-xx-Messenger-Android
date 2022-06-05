package io.xxlabs.messenger.ui.main.chats.search

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.databinding.ListItemSearchResultBinding
import io.xxlabs.messenger.databinding.ListItemSectionHeaderBinding
import io.xxlabs.messenger.ui.main.chat.setVisibility
import java.lang.ClassCastException

class SearchResultAdapter :
    ListAdapter<SearchResultItem, RecyclerView.ViewHolder>(SearchResultDiffUtil()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        return when (viewType) {
            VIEW_TYPE_SECTION_HEADER -> SectionHeaderViewHolder.create(parent)
            else ->  SearchResultViewHolder.create(parent)
        }
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        when (holder) {
            is SearchResultViewHolder -> {
                val ui = currentList[position] as SearchResult
                holder.onBind(ui)
            }
            is SectionHeaderViewHolder -> {
                val label = (currentList[position] as SectionHeader).label
                holder.onBind(label, position)
            }
            else -> throw ClassCastException("Unknown data")
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (currentList[position]) {
            is SectionHeader -> VIEW_TYPE_SECTION_HEADER
            is ConnectionResult -> VIEW_TYPE_CONTACT
            is PrivateChatResult -> VIEW_TYPE_PRIVATE_CHAT
            is GroupChatResult -> VIEW_TYPE_GROUP_CHAT
        }
    }

    companion object {
        private const val VIEW_TYPE_SECTION_HEADER = -1
        private const val VIEW_TYPE_CONTACT = 1
        private const val VIEW_TYPE_PRIVATE_CHAT = 2
        private const val VIEW_TYPE_GROUP_CHAT = 3
    }
}

private class SearchResultDiffUtil : DiffUtil.ItemCallback<SearchResultItem>() {
    override fun areItemsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem): Boolean =
        oldItem.id == newItem.id

    override fun areContentsTheSame(oldItem: SearchResultItem, newItem: SearchResultItem): Boolean =
        oldItem == newItem
}

private class SearchResultViewHolder(
    private val binding: ListItemSearchResultBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(ui: SearchResult) {
        binding.ui = ui
    }

    companion object {
        fun create(parent: ViewGroup): SearchResultViewHolder {
            val binding = ListItemSearchResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SearchResultViewHolder(binding)
        }
    }
}

private class SectionHeaderViewHolder(
    private val binding: ListItemSectionHeaderBinding
) : RecyclerView.ViewHolder(binding.root) {

    fun onBind(label: String, position: Int) {
        binding.header = label
        binding.dividerLayout.divider.setVisibility(position > 0)
    }

    companion object {
        fun create(parent: ViewGroup): SectionHeaderViewHolder {
            val binding = ListItemSectionHeaderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return SectionHeaderViewHolder(binding)
        }
    }
}