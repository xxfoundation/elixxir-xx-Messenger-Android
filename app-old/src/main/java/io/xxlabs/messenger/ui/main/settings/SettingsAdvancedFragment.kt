package io.xxlabs.messenger.ui.main.settings

import android.content.Intent
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CompoundButton
import androidx.core.content.ContextCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.data.SimpleRequestState
import io.xxlabs.messenger.support.extensions.setInsets
import io.xxlabs.messenger.support.extensions.toast
import io.xxlabs.messenger.support.util.DialogUtils
import io.xxlabs.messenger.support.util.FileUtils
import io.xxlabs.messenger.support.view.LooperCircularProgressBar
import io.xxlabs.messenger.ui.base.BaseFragment
import io.xxlabs.messenger.ui.main.MainViewModel
import kotlinx.android.synthetic.main.component_toolbar_generic.*
import kotlinx.android.synthetic.main.fragment_settings_advanced.*
import timber.log.Timber
import java.io.File
import java.io.FileWriter
import java.io.IOException
import javax.inject.Inject

class SettingsAdvancedFragment : BaseFragment() {
    @Inject
    lateinit var viewModelFactory: ViewModelProvider.Factory
    lateinit var settingsViewModel: SettingsViewModel
    lateinit var mainViewModel: MainViewModel
    lateinit var loadingProgress: LooperCircularProgressBar

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        settingsViewModel =
            ViewModelProvider(this, viewModelFactory).get(SettingsViewModel::class.java)

        mainViewModel =
            ViewModelProvider(
                requireActivity(),
                viewModelFactory
            ).get(MainViewModel::class.java)

        loadingProgress = LooperCircularProgressBar(requireContext(), false)

        return inflater.inflate(R.layout.fragment_settings_advanced, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        initComponents(view)
    }

    fun initComponents(root: View) {
        setupToolbar()
        bindCrashReport()
        bindDebug()
    }

    private fun setupToolbar() {
        toolbarGeneric.setInsets(topMask = WindowInsetsCompat.Type.systemBars())
        toolbarGenericTitle.text = getString(R.string.advanced_settings_title)
        toolbarGenericBackBtn.setOnClickListener {
            findNavController().navigateUp()
        }
    }

    private fun bindCrashReport() {
        val isCrashReportOn = settingsViewModel.isCrashReportOn()
        settingsAdvancedCrashReportSwitch.isChecked = isCrashReportOn

        val bindSwitcherListener = CompoundButton.OnCheckedChangeListener { switch, isChecked ->
            switch.setOnCheckedChangeListener(null)
            settingsViewModel.enableCrashReport(isChecked)
        }

        settingsViewModel.enableCrashReport.observe(viewLifecycleOwner, Observer {
            showCrashReportDialog()
            setCrashTransparency()
            settingsAdvancedCrashReportSwitch.setOnCheckedChangeListener(bindSwitcherListener)
        })

        settingsAdvancedCrashReportSwitch.setOnCheckedChangeListener(bindSwitcherListener)
        setCrashTransparency()
    }

    private fun showCrashReportDialog() {
        DialogUtils.createPopupDialogFragment(
            "Alert",
            icon = ContextCompat.getDrawable(requireContext(), R.drawable.ic_alert_rounded),
            "Crash reports will continue to be sent automatically until you quit the app. You can close and quit now, or it will apply the next time the app is restarted",
            "Ok"
        ).show(childFragmentManager, "crashReportPopup")
    }

    private fun setCrashTransparency() {
        settingsAdvancedCrashReportTitle.isEnabled = settingsAdvancedCrashReportSwitch.isChecked
    }

    private fun bindDebug() {
        settingsAdvancedDebugExport.setOnClickListener {
            settingsViewModel.exportLatestLog(requireContext())
        }

        val areLogsOn = settingsViewModel.areDebugLogsOn()
        settingsAdvancedDebugSwitch.isChecked = areLogsOn
        settingsAdvancedDebugSize.text = settingsViewModel.getLogSize(requireContext())

        if (areLogsOn) {
            settingsAdvancedDebugSize.visibility = View.VISIBLE
        } else {
            settingsAdvancedDebugSize.visibility = View.INVISIBLE
        }

        val bindSwitcherListener = CompoundButton.OnCheckedChangeListener { switch, isChecked ->
            switch.setOnCheckedChangeListener(null)
            if (isChecked) {
                FileUtils.checkPermissionDo(
                    this,
                    REQUEST_CODE_WRITE_EXTERNAL_STORAGE, { onWriteGranted() })
            } else {
                settingsViewModel.enableDebug(requireContext(), false)
            }
        }

        settingsViewModel.enableDebug.observe(viewLifecycleOwner, Observer { result ->
            loadingProgress.hide()
            settingsAdvancedDebugSize.text = settingsViewModel.getLogSize(requireContext())

            if (result is SimpleRequestState.Success) {
                settingsAdvancedDebugSwitch.isChecked = result.value
                settingsAdvancedDebugSize.visibility = if (result.value) {
                    View.VISIBLE
                } else {
                    View.INVISIBLE
                }
            } else if (result is SimpleRequestState.Error) {
                settingsViewModel.areDebugLogsOn()
                result.error?.let { showError(it) }
            }

            setDebugTransparency()
            settingsAdvancedDebugSwitch.setOnCheckedChangeListener(bindSwitcherListener)
        })

        setDebugTransparency()
        settingsAdvancedDebugSwitch.setOnCheckedChangeListener(bindSwitcherListener)
    }

    private fun setDebugTransparency() {
        settingsAdvancedDebugTitle.isEnabled = settingsAdvancedDebugSwitch.isChecked
    }

    private fun onWriteGranted() {
        loadingProgress.show()
        settingsViewModel.enableDebug(requireContext(), true)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        if (requestCode == REQUEST_CODE_SELECT_FOLDER) {
            val treeUri = data?.data
            Timber.v("TreePath: $treeUri")
            val path = FileUtils.getDirectoryPath(requireContext(), treeUri)
            Timber.v("Path: $path")
            if (treeUri != null || path != null) {
                val chosenDir = File(path!!)
                Timber.v("Is directory: ${chosenDir.isDirectory}")
                Timber.v("Is writeable: ${chosenDir.canWrite()}")
                Timber.v("Is readable: ${chosenDir.canRead()}")
                Timber.v("Is rooted: ${chosenDir.isRooted}")

                val file = File(chosenDir, "elixxir_session.txt")
                Timber.v("Is directory 2: ${file.isDirectory}")
                Timber.v("Is writeable 2: ${file.canWrite()}")
                Timber.v("Is readable 2: ${file.canRead()}")
                Timber.v("Is rooted 2: ${file.isRooted}")

                try {
                    if (file.exists()) {
                        file.delete()
                    }
                    val out = FileWriter(file)
                    out.write("################")
                    val usernameByteArray =
                        preferences.name.toByteArray(Charsets.UTF_8)
                    out.write(
                        Base64.encodeToString(
                            usernameByteArray,
                            Base64.NO_WRAP
                        )
                    )
                    out.close()
                    context?.toast("Exported with success!")
                } catch (e: IOException) {
                    showError("The chosen directory is not writable")
                    Timber.e(e.localizedMessage)
                }

            } else {
                showError("Folder was not selected")
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == REQUEST_CODE_WRITE_EXTERNAL_STORAGE) {
            if ((grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED)) {
                onWriteGranted()
            } else {
                showError("This permission is required in order to activate this feature.")
                settingsViewModel.enableDebug(requireContext(), false)
            }
            return
        }
    }

    companion object {
        private const val REQUEST_CODE_WRITE_EXTERNAL_STORAGE = 100
        private const val REQUEST_CODE_SELECT_FOLDER = 200
    }
}