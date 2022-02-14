package io.xxlabs.messenger.ui.base

import android.graphics.Color
import android.os.Bundle
import android.os.PersistableBundle
import android.view.MotionEvent
import android.view.Window
import android.view.WindowManager
import android.widget.EditText
import androidx.appcompat.app.AppCompatActivity
import bindings.Bindings
import dagger.android.AndroidInjection
import dagger.android.AndroidInjector
import dagger.android.DispatchingAndroidInjector
import dagger.android.HasAndroidInjector
import io.xxlabs.messenger.R
import io.xxlabs.messenger.application.XxMessengerApplication
import io.xxlabs.messenger.bindings.wrapper.bindings.bindingsErrorMessage
import io.xxlabs.messenger.repository.PreferencesRepository
import io.xxlabs.messenger.support.dialog.PopupActionDialog
import io.xxlabs.messenger.support.util.DialogUtils
import io.xxlabs.messenger.support.util.Utils
import javax.inject.Inject


/**
 * A base model for [AppCompatActivity] integration with Lokalise
 */
abstract class BaseInjectorActivity : AppCompatActivity(), HasAndroidInjector {
    @Inject
    protected lateinit var preferencesRepository: PreferencesRepository
    protected var currentDialog: PopupActionDialog? = null

    @Inject
    lateinit var activityInjector: DispatchingAndroidInjector<Any>
    override fun androidInjector(): AndroidInjector<Any> = activityInjector

    override fun onCreate(savedInstanceState: Bundle?, persistentState: PersistableBundle?) {
        AndroidInjection.inject(this)
        super.onCreate(savedInstanceState, persistentState)
        transparentStatusAndNavigation()
    }

    open fun transparentStatusAndNavigation() {
        val window: Window = window
        var windowManager = WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS
        windowManager = windowManager or WindowManager.LayoutParams.FLAG_TRANSLUCENT_NAVIGATION
        setWindowFlag(window, windowManager, false)
        window.statusBarColor = Color.TRANSPARENT
        window.navigationBarColor = Color.TRANSPARENT
    }

    private fun setWindowFlag(window: Window, bits: Int, on: Boolean = true) {
        val win: Window = window
        val winParams: WindowManager.LayoutParams = win.attributes
        if (on) {
            winParams.flags = winParams.flags or bits
        } else {
            winParams.flags = winParams.flags and bits.inv()
        }
        win.attributes = winParams
    }

    override fun onResume() {
        super.onResume()
        XxMessengerApplication.activityResumed()
    }

    override fun onPause() {
        super.onPause()
        XxMessengerApplication.activityPaused()
    }

    fun showError(exception: Throwable, isBindingError: Boolean = false) {
        val newString = bindingsErrorMessage(exception)
        currentDialog?.dismiss()
        currentDialog = DialogUtils.createErrorPopupDialog(
            this,
            Exception(newString),
            preferencesRepository.areDebugLogsOn
        )
        currentDialog!!.show()
    }

    fun showError(text: String) {
        showError(Exception(text))
    }

    override fun dispatchTouchEvent(ev: MotionEvent): Boolean {
        if (ev.action == MotionEvent.ACTION_DOWN) {
            val v = currentFocus
            if (v is EditText) {
                if (v.id == R.id.chatMsgInput) {
                    return super.dispatchTouchEvent(ev)
                }

                Utils.hideKeyboardGlobal(this, v, ev)
            }
        }
        return super.dispatchTouchEvent(ev)
    }
}