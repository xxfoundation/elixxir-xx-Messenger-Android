package io.xxlabs.messenger.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.ViewModelProvider
import io.xxlabs.messenger.databinding.FragmentUserSearchBinding
import io.xxlabs.messenger.ui.base.BaseFragment
import javax.inject.Inject

class UserSearchFragment : BaseFragment() {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    private val searchViewModel: UserSearchViewModel by viewModels { viewModelFactory }

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