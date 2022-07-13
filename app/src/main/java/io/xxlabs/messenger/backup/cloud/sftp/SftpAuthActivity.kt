package io.xxlabs.messenger.backup.cloud.sftp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import io.xxlabs.messenger.databinding.ActivitySftpAuthBinding
import io.xxlabs.messenger.support.extensions.toast

class SftpAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySftpAuthBinding
    private val sftpViewModel: SshAuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySftpAuthBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        observeUi()
    }

    private fun observeUi() {
        sftpViewModel.sftpLoginUi.observe(this) { ui ->
            ui?.let { binding.ui = it }
        }

        sftpViewModel.loginSuccess.observe(this) { credentials ->
            credentials?.let { onLoginSuccess(credentials) }
        }

        sftpViewModel.loginError.observe(this) { error ->
            error?.let { toast(error) }
        }
    }

    private fun onLoginSuccess(credentials: SshCredentials) {
        val intent = Intent(SFTP_AUTH_INTENT).apply {
            putExtra(EXTRA_SFTP_CREDENTIAL, credentials)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        const val SFTP_AUTH_INTENT = "sftp_auth"
        const val EXTRA_SFTP_CREDENTIAL = "sftp_credential"
    }
}