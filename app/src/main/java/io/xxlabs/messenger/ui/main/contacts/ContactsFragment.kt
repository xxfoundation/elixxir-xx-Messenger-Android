package io.xxlabs.messenger.ui.main.contacts

import android.os.Bundle
import android.view.MotionEvent.ACTION_MOVE
import android.view.MotionEvent.ACTION_UP
import android.view.View
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.main.contacts.list.ConnectionsAdapter
import io.xxlabs.messenger.ui.main.contacts.list.ConnectionsListScrollHandler
import timber.log.Timber

abstract class ContactsFragment : BaseFragment() {
    protected abstract val scrollHandler: ConnectionsListScrollHandler
    protected abstract val connectionsAdapter: RecyclerView.Adapter<*>
    protected abstract val connectionsRecyclerView: RecyclerView
    protected abstract val lettersScrollbar: View

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initRecyclerView()
    }

    private fun initRecyclerView() {
        connectionsRecyclerView.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = connectionsAdapter
        }
    }

    override fun onStart() {
        super.onStart()
        detectScrollGesture()
        observeUI()
    }

    private fun detectScrollGesture() {
        lettersScrollbar.apply {
            setOnTouchListener { view, motionEvent ->
                view.performClick()
                when (motionEvent.action) {
                    ACTION_MOVE -> {
                        scrollHandler.onLettersScrolled(top, bottom, motionEvent.y)
                        true
                    }
                    ACTION_UP -> {
                        scrollHandler.onScrollStopped()
                        true
                    }
                    else -> false
                }
            }
        }
    }

    private fun observeUI() {
        scrollHandler.scrollToPosition.observe(viewLifecycleOwner) { position ->
            position?.let { scrollToConnectionListPosition(it) }
        }
    }

    private fun scrollToConnectionListPosition(position: Int) {
        try {
            connectionsRecyclerView.smoothScrollToPosition(position)
        } catch (e: Exception) {
            Timber.d("An exception was thrown: ${e.message}")
        }
    }
}