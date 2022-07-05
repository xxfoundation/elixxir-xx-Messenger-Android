package io.xxlabs.messenger.backup.cloud.sftp

import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import androidx.activity.viewModels
import io.xxlabs.messenger.databinding.ActivitySftpAuthBinding

class SftpAuthActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySftpAuthBinding
    private val sftpViewModel: SftpAuthViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySftpAuthBinding.inflate(layoutInflater)
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
            credentials?.let {
                onLoginSuccess(credentials)
            }
        }
    }

    private fun onLoginSuccess(credentials: SftpCredentials) {
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