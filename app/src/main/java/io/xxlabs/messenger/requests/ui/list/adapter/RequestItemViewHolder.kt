package io.xxlabs.messenger.requests.ui.list.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.crashlytics.FirebaseCrashlytics
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.*
import java.io.InvalidObjectException

abstract class RequestItemViewHolder(view: View) : RecyclerView.ViewHolder(view) {
    abstract fun onBind(ui: RequestItem, listener: RequestItemListener)
}

class RequestViewHolder(
    private val binding: ListItemRequestBinding
) : RequestItemViewHolder(binding.root) {

    override fun onBind(ui: RequestItem, listener: RequestItemListener) {
        binding.ui = ui
        binding.listener = listener
        listener.markAsSeen(ui)
    }

    companion object {
        fun create(parent: ViewGroup): RequestViewHolder {
            val binding = ListItemRequestBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return RequestViewHolder(binding)
        }
    }
}

/**
 * For Connection Requests being shown as search results on the Search screen.
 */
class RequestSearchResultViewHolder(
    private val binding: ListItemRequestSearchResultBinding
) : RequestItemViewHolder(binding.root) {

    override fun onBind(ui: RequestItem, listener: RequestItemListener) {
        binding.ui = ui as ContactRequestSearchResultItem
        binding.listener = listener
        listener.markAsSeen(ui)
    }

    companion object {
        fun create(parent: ViewGroup): RequestSearchResultViewHolder {
            val binding = ListItemRequestSearchResultBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return RequestSearchResultViewHolder(binding)
        }
    }
}

class ConnectionViewHolder(
    private val binding: ListItemRequestBinding
) : RequestItemViewHolder(binding.root) {

    override fun onBind(ui: RequestItem, listener: RequestItemListener) {
        binding.ui = ui
        binding.listener = listener
        binding.requestTimestamp.visibility = View.GONE
    }

    companion object {
        fun create(parent: ViewGroup): ConnectionViewHolder {
            val binding = ListItemRequestBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ConnectionViewHolder(binding)
        }
    }
}

class Placeholder(
    private val binding: ListItemEmptyPlaceholderBinding
) : RequestItemViewHolder(binding.root) {

    override fun onBind(ui: RequestItem, listener: RequestItemListener) {
        binding.ui = ui
    }

    companion object {
        fun create(parent: ViewGroup): Placeholder {
            val binding = ListItemEmptyPlaceholderBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return Placeholder(binding)
        }
    }
}

class HiddenRequestToggle(
    private val binding: ListItemHiddenRequestsToggleBinding
) : RequestItemViewHolder(binding.root) {

    override fun onBind(ui: RequestItem, listener: RequestItemListener) {
        binding.listener = listener
    }

    companion object {
        fun create(parent: ViewGroup): HiddenRequestToggle {
            val binding = ListItemHiddenRequestsToggleBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return HiddenRequestToggle(binding)
        }
    }
}

class ConnectionsSectionDivider(
    private val binding: ListItemSectionDividerBinding
) : RequestItemViewHolder(binding.root) {

    override fun onBind(ui: RequestItem, listener: RequestItemListener) {
        binding.ui = ui
    }

    companion object {
        fun create(parent: ViewGroup): ConnectionsSectionDivider {
            val binding = ListItemSectionDividerBinding.inflate(
                LayoutInflater.from(parent.context),
                parent,
                false
            )
            return ConnectionsSectionDivider(binding)
        }
    }
}

/**
 * Displays an invisible ViewHolder and logs the event to prevent
 * locking the screen in a permanent crash state.
 */
class InvalidViewType(view: View) : RequestItemViewHolder(view) {
    override fun onBind(ui: RequestItem, listener: RequestItemListener) {
        FirebaseCrashlytics.getInstance().recordException(
            InvalidObjectException("Attempted to show an invalid view type.")
        )
    }

    companion object {
        fun create(parent: ViewGroup): InvalidViewType {
            val view = LayoutInflater.from(parent.context).inflate(
                    R.layout.list_item_invalid,
                    parent,
                    false
                )
            return InvalidViewType(view)
        }
    }
}