package io.xxlabs.messenger.backup.ui.restore

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.ViewDataBinding
import androidx.fragment.app.Fragment
import io.xxlabs.messenger.backup.data.restore.RestoreLog
import io.xxlabs.messenger.databinding.FragmentRestoreReadyBinding
import io.xxlabs.messenger.databinding.FragmentRestoreStartedBinding
import io.xxlabs.messenger.databinding.FragmentRestoreSuccessBinding

open class RestoreStateFragment : Fragment() {

    protected val state: RestoreState by lazy {
        requireArguments().getSerializable(ARG_UI) as RestoreState
    }
    protected lateinit var binding: ViewDataBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = when (state) {
            is RestoreReady -> FragmentRestoreReadyBinding.inflate(inflater).apply {
                ui = state as RestoreReady
            }
            is RestoreStarted -> FragmentRestoreStartedBinding.inflate(inflater).apply {
                ui = state as RestoreStarted
            }
            is RestoreSuccess -> FragmentRestoreSuccessBinding.inflate(inflater).apply {
                ui = state as RestoreSuccess
            }
        }

        return binding.root
    }

    companion object Factory {
        const val ARG_UI = "ui"

        fun newInstance(ui: RestoreState): RestoreStateFragment =
            when (ui) {
                is RestoreStarted -> RestoreStartedFragment()
                else -> RestoreStateFragment()
            }.apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_UI, ui)
                }
            }
    }
}

class RestoreStartedFragment : RestoreStateFragment() {

    private val ui: FragmentRestoreStartedBinding by lazy {
        binding as FragmentRestoreStartedBinding
    }
    private val restoreLog: RestoreLog? by lazy {
        (state as RestoreStarted).restoreLog
    }

    override fun onStart() {
        super.onStart()
        observeProgress()
    }

    private fun observeProgress() {
        restoreLog?.data?.observe(viewLifecycleOwner) { events ->
            if (events.isNotEmpty()) ui.restoreProgressText.text = events[events.size-1]
        }
    }
}