package com.example.larp_app.ui.validity

/**
 * Data validation state of the login form.
 */
data class LoginPassFormState(
    val emailError: Int? = null,
    val usernameError: Int? = null,
    val passwordError: Int? = null,
    val isDataValid: Boolean = false
)