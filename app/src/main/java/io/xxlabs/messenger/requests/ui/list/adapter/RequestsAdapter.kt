package io.xxlabs.messenger.requests.ui.list.adapter

import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import io.xxlabs.messenger.requests.ui.list.adapter.RequestsAdapter.ViewType.*

class RequestsAdapter(
    private val listener: RequestItemListener
): ListAdapter<RequestItem, RequestItemViewHolder>(RequestsDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RequestItemViewHolder {
        return when (ViewType.from(viewType)) {
            CONNECTION, UD_SEARCH_RESULT -> ConnectionViewHolder.create(parent)
            REQUEST, INVITE, REQUEST_SEARCH_RESULT -> RequestViewHolder.create(parent)
            PLACEHOLDER -> Placeholder.create(parent)
            SWITCH -> HiddenRequestToggle.create(parent)
            DIVIDER -> ConnectionsSectionDivider.create(parent)
            OTHER -> InvalidViewType.create(parent)
        }
    }

    override fun onBindViewHolder(holder: RequestItemViewHolder, position: Int) {
        with(currentList[position]) {
            holder.onBind(this, listener)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return with (currentList[position]) {
            val status = request.requestStatus.value
            val model = when (this) {
                is ContactRequestItem -> REQUEST.value
                is GroupInviteItem -> INVITE.value
                is EmptyPlaceholderItem -> PLACEHOLDER.value
                is HiddenRequestToggleItem -> SWITCH.value
                is AcceptedConnectionItem -> CONNECTION.value
                is SearchResultItem -> UD_SEARCH_RESULT.value
                is ConnectionsDividerItem -> DIVIDER.value
                is ContactRequestSearchResultItem -> REQUEST_SEARCH_RESULT.value
                else -> OTHER.value
            }
            status + model
        }
    }

    private enum class ViewType(val value: Int) {
        REQUEST(100),
        INVITE(200),
        PLACEHOLDER(300),
        SWITCH(400),
        CONNECTION(500),
        UD_SEARCH_RESULT(600),
        DIVIDER(700),
        REQUEST_SEARCH_RESULT(800),
        OTHER(900);

        companion object {
            fun from(value: Int): ViewType {
                return values().firstOrNull {
                    (value / 100) * 100 == it.value
                } ?: OTHER
            }
        }
    }
}

class RequestsDiffCallback : DiffUtil.ItemCallback<RequestItem>() {
    override fun areItemsTheSame(oldItem: RequestItem, newItem: RequestItem): Boolean =
        oldItem.id.contentEquals(newItem.id)

    override fun areContentsTheSame(oldItem: RequestItem, newItem: RequestItem): Boolean =
        oldItem == newItem
}