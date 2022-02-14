package io.xxlabs.messenger.support.util

import android.text.InputType
import android.text.TextUtils
import android.util.Patterns
import android.widget.EditText
import com.google.android.material.textfield.TextInputLayout
import com.google.i18n.phonenumbers.NumberParseException
import com.google.i18n.phonenumbers.PhoneNumberUtil
import io.xxlabs.messenger.R
import io.xxlabs.messenger.data.datatype.input.EmailFieldState
import io.xxlabs.messenger.data.datatype.input.PhoneFieldState
import io.xxlabs.messenger.data.datatype.input.UsernameFieldState
import timber.log.Timber


class ValidationUtils {
    companion object {
        fun arePasswordsEqual(target: EditText, target2: EditText, errMsg: String? = null): Boolean {
            val areEqual = target.text.toString() == target2.text.toString()

            if (areEqual && errMsg != null) {
                target.error = errMsg
            } else {
                target.error = null
            }

            return !areEqual
        }

        fun isNotEmpty(target: TextInputLayout, errMsg: String): Boolean {
            val isEmpty = TextUtils.isEmpty(target.editText?.text)

            if (!isEmpty) {
                target.error = null
            } else {
                target.error = errMsg
            }

            return !isEmpty
        }

        fun isPasswordValid(
            target: TextInputLayout,
            minLength: Int,
            errMsg: String? = null
        ): Boolean {
            return when (target.editText?.inputType) {
                InputType.TYPE_TEXT_VARIATION_PASSWORD + 1 -> {//Password
                    val text = target.editText?.text.toString()
                    val isNotEmpty = !TextUtils.isEmpty(text)
                    val isValid = text.length >= minLength

                    if (isValid) {
                        target.error = null
                    } else {
                        if (isNotEmpty) {
                            target.error = errMsg
                        } else {
                            target.error = null
                        }
                    }

                    isValid
                }

                else -> {
                    !TextUtils.isEmpty(target.editText?.text)
                }


            }
        }

        fun isUsernameValid(username: String): UsernameFieldState {
            val isEmpty = username.isEmpty()
            val hasMinChar = (username.length > 3)
            val lessThanMaximum = (username.length < 33)

            return if (isEmpty) {
                UsernameFieldState.EMPTY
            } else if (!hasMinChar) {
                UsernameFieldState.INVALID_LENGTH
            } else if (!lessThanMaximum) {
                UsernameFieldState.MAX_CHARACTERS
            } else {
                UsernameFieldState.VALID
            }
        }

        fun isUsernameValid(
            target: TextInputLayout,
            showError: Boolean = true
        ): UsernameFieldState {
            val text = target.editText?.text
            val isEmpty = text.isNullOrBlank()
            val hasMinChar = (text?.length ?: 0) > 3
            val isMaxCharactersReached = (text?.length ?: 0) > 32

            if (showError) {
                if (isEmpty) {
                    target.error = "username cannot be empty"
                } else {
                    target.error = null
                }
            }

            return if (isEmpty) {
                UsernameFieldState.EMPTY
            } else if (!hasMinChar) {
                UsernameFieldState.INVALID_LENGTH
            } else if (isMaxCharactersReached) {
                UsernameFieldState.MAX_CHARACTERS
            }  else {
                UsernameFieldState.VALID
            }
        }

        private fun validateUsernameRegex(username: String): Boolean {
           return username.contains(" ")
        }

        fun isEmailValid(email: String): EmailFieldState {
            val isEmpty = email.isEmpty()
            val isValid = validateEmail(email)

            return if (isEmpty) {
                EmailFieldState.EMPTY
            } else if (isValid) {
                EmailFieldState.VALID
            } else {
                EmailFieldState.INVALID_FORMAT
            }
        }

        private fun validateEmail(email: String): Boolean {
            return Patterns.EMAIL_ADDRESS.matcher(email).matches()
        }

        fun isPhoneValid(phone: String, countryCode: String?): PhoneFieldState {
            val isEmpty = phone.trim().isEmpty()
            val isValid = isPhoneNumberValid(phone, countryCode)
            Timber.v("is valid phone: $isValid")
            return when {
                isEmpty -> {
                    PhoneFieldState.EMPTY
                }
                isValid -> {
                    PhoneFieldState.VALID
                }
                else -> {
                    PhoneFieldState.INVALID_PHONE_FORMAT
                }
            }
        }

        fun isPhoneNumberValid(phoneNumber: String, countryCode: String?): Boolean {
            //NOTE: This should probably be a member variable.
            Timber.d("Final phone: $phoneNumber")
            val phoneUtil = PhoneNumberUtil.getInstance()
            try {
                val numberProto = phoneUtil.parse(phoneNumber, countryCode)
                Timber.d("Proto number: $numberProto")
                return phoneUtil.isValidNumber(numberProto)
            } catch (e: NumberParseException) {
                Timber.d("NumberParseException was thrown: $e")
            }
            return false
        }

        fun isValid(target: EditText): Boolean {
            val isValid: Boolean = when (target.inputType) {
                InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS + 1 -> { //Email
                    !TextUtils.isEmpty(target.text) && Patterns.EMAIL_ADDRESS.matcher(target.text)
                        .matches()
                }
                InputType.TYPE_TEXT_VARIATION_PASSWORD + 1 -> {//Password
                    !TextUtils.isEmpty(target.text) && target.text.length >= 6
                }
                else -> {
                    !TextUtils.isEmpty(target.text)
                }
            }

            if (isValid) {
                target.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
            } else {
                if (target.text.isNotEmpty()) {
                    target.setCompoundDrawablesWithIntrinsicBounds(
                        0,
                        0,
                        R.drawable.ic_error_red_24dp,
                        0
                    )
                } else {
                    target.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0)
                }
            }

            return isValid
        }
    }
}