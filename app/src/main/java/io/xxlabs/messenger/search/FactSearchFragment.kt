package io.xxlabs.messenger.search

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.textfield.TextInputEditText
import io.xxlabs.messenger.databinding.FragmentFactSearchBinding
import io.xxlabs.messenger.di.utils.Injectable
import io.xxlabs.messenger.requests.ui.RequestsViewModel
import io.xxlabs.messenger.requests.ui.list.adapter.RequestItem
import io.xxlabs.messenger.requests.ui.list.adapter.RequestsAdapter
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch
import javax.inject.Inject

/**
 * Superclass for Fragments that look up users by Fact (username, phone, etc.)
 */
abstract class FactSearchFragment : Fragment(), Injectable {

    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    protected val requestsViewModel: RequestsViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { viewModelFactory }
    )
    protected val searchViewModel: UserSearchViewModel by viewModels(
        ownerProducer = { requireParentFragment() },
        factoryProducer = { viewModelFactory }
    )

    private val resultsAdapter: RequestsAdapter by lazy {
        RequestsAdapter(requestsViewModel)
    }
    private lateinit var binding: FragmentFactSearchBinding

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = FragmentFactSearchBinding.inflate(inflater, container, false)
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                getResults().collect { results ->
                    resultsAdapter.submitList(results)
                }
            }
        }
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initComponents()
    }

    private fun initComponents() {
        binding.searchResultsRV.apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = resultsAdapter
        }
        binding.searchTextInputEditText.apply {
            initKeyboardSearchButton()
            initFocusListener()
        }
        binding.ui = getSearchTabUi()
    }

    private fun TextInputEditText.initKeyboardSearchButton() {
        setOnEditorActionListener { v, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                try {
                    onSearchClicked(v.text.toString())
                    return@setOnEditorActionListener true
                } catch (e: Exception) {
                    return@setOnEditorActionListener false
                }

            } else {
                return@setOnEditorActionListener false
            }
        }
    }

    private fun TextInputEditText.initFocusListener() {
        setOnFocusChangeListener { view, hasFocus ->
            if (hasFocus) searchViewModel.onUserInput("")
        }
    }

    abstract suspend fun getResults(): Flow<List<RequestItem>>

    abstract fun onSearchClicked(query: String?)

    abstract fun getSearchTabUi(): FactSearchUi
}

class UsernameSearchFragment : FactSearchFragment() {
    override suspend fun getResults(): Flow<List<RequestItem>> =
        searchViewModel.usernameResults

    override fun onSearchClicked(query: String?) {
        searchViewModel.onUsernameSearch(query)
    }

    override fun getSearchTabUi(): FactSearchUi = searchViewModel.usernameSearchUi
}

class EmailSearchFragment : FactSearchFragment() {
    override suspend fun getResults(): Flow<List<RequestItem>> =
        searchViewModel.emailResults

    override fun onSearchClicked(query: String?) {
        searchViewModel.onEmailSearch(query)
    }

    override fun getSearchTabUi(): FactSearchUi = searchViewModel.emailSearchUi
}

class PhoneSearchFragment : FactSearchFragment() {
    override suspend fun getResults(): Flow<List<RequestItem>> =
        searchViewModel.phoneResults

    override fun onSearchClicked(query: String?) {
        searchViewModel.onPhoneSearch(query)
    }

    override fun getSearchTabUi(): FactSearchUi = searchViewModel.phoneSearchUi
}