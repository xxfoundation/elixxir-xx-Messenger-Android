package io.xxlabs.messenger.support.selection

import android.content.Context
import android.view.View
import androidx.recyclerview.widget.RecyclerView
import timber.log.Timber

class CustomSelectionTracker(
    private val recyclerView: RecyclerView,
    private val keyProvider: BaseKeyProvider<Long>
) {
    val selection = mutableListOf<Long>()
    private var adapter: RecyclerView.Adapter<RecyclerView.ViewHolder>
    private var isInChoiceMode = false
    private lateinit var observer: BaseSelectionObserver<Long>
    private var touchListener: RecyclerTouchListener

    init {
        val context = recyclerView.context
        adapter = recyclerView.adapter!!
        touchListener = recyclerTouchListener(context, recyclerView)
        enableTracker()
    }

    private fun recyclerTouchListener(
        context: Context?,
        recyclerView: RecyclerView
    ) = RecyclerTouchListener(
        context,
        recyclerView, object : TrackerClickListener {
            override fun onClick(view: View, position: Int) {
                Timber.v("Single press at position: $position")
                if (isInChoiceMode) {
                    switchSelectedState(position)
                    observer.onSelectionChanged(selection)
                } else {
                    view.callOnClick()
                }
            }

            override fun onLongClick(view: View, position: Int) {
                Timber.v("Long press at position: $position")
                if (!isInChoiceMode) {
                    isInChoiceMode = true
                }
                switchSelectedState(position)
                observer.onSelectionChanged(selection)
            }
        })

    fun disableTracker() {
        recyclerView.removeOnItemTouchListener(touchListener)
    }

    fun enableTracker() {
        recyclerView.addOnItemTouchListener(touchListener)
    }

    fun isSelected(key: Long): Boolean {
        return selection.contains(key)
    }

    fun isSelected(position: Int): Boolean {
        val key = keyProvider.getKey(position)
        return selection.contains(key)
    }

    private fun switchSelectedState(position: Int) {
        val key = keyProvider.getKey(position)
        if (selection.contains(key)) {
            selection.remove(key)
        } else {
            selection.add(key)
        }

        adapter.notifyItemChanged(position)
    }

    fun removeSelection(position: Int, itemCount: Int) {
        for (n in 0 until itemCount) {
            val key = keyProvider.getKey(position + n)
            if (key != RecyclerView.NO_ID && selection.contains(key)) {
                Timber.v("Removing key: $key")
                selection.remove(key)
                observer.onItemStateChanged(key, false)
            }
        }

        observer.onSelectionChanged(selection)
    }

    fun removeSelection(idsList: List<Long>) {
        selection.removeAll(idsList)
        observer.onSelectionChanged(selection)
    }

    fun setItemsSelected(idsList: List<Long>) {
        selection.clear()
        selection.addAll(idsList)
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
        observer.onSelectionChanged(selection)
    }

    fun clearSelection(choiceMode: Boolean = false) {
        selection.clear()
        isInChoiceMode = choiceMode
        adapter.notifyItemRangeChanged(0, adapter.itemCount)
        observer.onSelectionCleared()
    }

    fun getSelectedCount(): Int {
        return selection.size
    }

    fun addObserver(
        onSelectionChanged: ((List<Long>) -> Unit),
        onSelectionCleared: (() -> Unit)
    ) {
        observer = object : BaseSelectionObserver<Long> {
            override fun onSelectionChanged(list: List<Long>) {
                super.onSelectionChanged(selection)
                onSelectionChanged(list)
            }

            override fun onSelectionCleared() {
                super.onSelectionCleared()
                onSelectionCleared()
            }
        }
    }
}