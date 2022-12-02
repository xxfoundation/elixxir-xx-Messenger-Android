package io.xxlabs.messenger.backup.cloud.crust.login.ui

import android.content.Intent
import android.os.Bundle
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import io.xxlabs.messenger.databinding.ActivityCrustLoginBinding

class CrustLoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityCrustLoginBinding
    private val crustViewModel: CrustLoginViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityCrustLoginBinding.inflate(layoutInflater)
        binding.lifecycleOwner = this
        setContentView(binding.root)
    }

    override fun onStart() {
        super.onStart()
        observeUi()
    }

    private fun observeUi() {
        crustViewModel.crustLoginUi.observe(this) { ui ->
            ui?.let { binding.ui = it }
        }

        crustViewModel.username.observe(this) { username ->
            username?.let { onLoginSuccess(username) }
        }
    }

    private fun onLoginSuccess(username: String) {
        val intent = Intent(CRUST_AUTH_INTENT).apply {
            putExtra(EXTRA_CRUST_USERNAME, username)
        }
        setResult(RESULT_OK, intent)
        finish()
    }

    companion object {
        const val CRUST_AUTH_INTENT = "crust_auth"
        const val EXTRA_CRUST_USERNAME = "crust_credential"
    }
}