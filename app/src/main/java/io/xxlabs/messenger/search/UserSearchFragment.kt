package io.xxlabs.messenger.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import io.xxlabs.messenger.databinding.FragmentUserSearchBinding
import io.xxlabs.messenger.ui.base.BaseFragment

class UserSearchFragment : BaseFragment() {

    private lateinit var binding: FragmentUserSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = FragmentUserSearchBinding.inflate(
            inflater, container, false
        )
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onStart() {
        super.onStart()
        observeUi()
    }

    private fun observeUi() {

    }
}