package io.xxlabs.messenger.ui.main.groups.create

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import io.xxlabs.messenger.R
import io.xxlabs.messenger.databinding.ComponentCreateGroupDialogBinding
import io.xxlabs.messenger.support.view.XxBottomSheetDialog

class CreateGroupDialog :
    XxBottomSheetDialog(),
    CreateGroupDialogController
{
    private lateinit var binding: ComponentCreateGroupDialogBinding
    private val dialogUI: CreateGroupDialogUI by lazy {
        requireArguments().getSerializable(ARG_UI) as CreateGroupDialogUI
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(
            inflater,
            R.layout.component_create_group_dialog,
            container,
            false
        )
        binding.ui = this
        binding.actionDialogButton.apply {
            setOnClickListener {
                dialogUI.buttonClick(name, description)
                dismiss()
            }
        }

        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override val maxGroupNameLength: Int = MAX_GROUP_NAME_LENGTH
    override val maxDescriptionLength: Int =  MAX_DESCRIPTION_LENGTH

    override var name: String? = null
        set(value) {
            value?.length?.let {
                _createButtonEnabled.value = it > 0
            } ?: run { _createButtonEnabled.value = false }
            field = value
        }

    override var description: String? = null
    override val buttonClick get() = dialogUI.buttonClick
    override val infoClick get() = dialogUI.infoClick
    override val body get() = dialogUI.body

    override val nameError: LiveData<String?> get() = _nameError
    private val _nameError = MutableLiveData<String>()

    override val descriptionError: LiveData<String?> get() = _descriptionError
    private val _descriptionError = MutableLiveData<String>()

    override val createButtonEnabled: LiveData<Boolean> get() = _createButtonEnabled
    private val _createButtonEnabled = MutableLiveData(false)

    companion object {
        private const val MAX_GROUP_NAME_LENGTH = 20
        private const val MAX_DESCRIPTION_LENGTH = 64
        private const val ARG_UI: String = "ui"

        fun newInstance(dialogUI: CreateGroupDialogUI): CreateGroupDialog =
            CreateGroupDialog().apply {
                arguments = Bundle().apply {
                    putSerializable(ARG_UI, dialogUI)
                }
            }
    }
}