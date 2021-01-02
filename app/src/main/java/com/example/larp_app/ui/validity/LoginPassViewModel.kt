package com.example.larp_app.ui.validity

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

import com.example.larp_app.R

class LoginPassViewModel : ViewModel() {

    private val _loginForm = MutableLiveData<LoginPassFormState>()
    val loginPassFormState: LiveData<LoginPassFormState> = _loginForm

    fun loginDataChanged(username: String, password: String, email: String? = null) {
        if (email != null && !isValid(email)) {
            _loginForm.value = LoginPassFormState(emailError = R.string.invalid_username_password)
        } else if (!isValid(username)) {
            _loginForm.value =
                LoginPassFormState(usernameError = R.string.invalid_username_password)
        } else if (!isValid(password)) {
            _loginForm.value =
                LoginPassFormState(passwordError = R.string.invalid_username_password)
        } else {
            _loginForm.value = LoginPassFormState(isDataValid = true)
        }
    }

    // A placeholder username and password validation check
    private fun isValid(username: String): Boolean {
        return username.length > 3
    }
}