package com.example.larp_app.ui.login

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.ViewModelProvider
import com.example.larp_app.MainActivity
import com.example.larp_app.R
import com.example.larp_app.ui.validity.LoginPassViewModel

class RegisterFragment : Fragment() {

    private lateinit var loginPassViewModel: LoginPassViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        return inflater.inflate(R.layout.fragment_register, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        loginPassViewModel = ViewModelProvider(this)
            .get(LoginPassViewModel::class.java)

        val usernameEditText = view.findViewById<EditText>(R.id.loginRegister)
        val emailEditText = view.findViewById<EditText>(R.id.emailRegister)
        val passwordEditText = view.findViewById<EditText>(R.id.passwordRegister)
        val registerButton = view.findViewById<Button>(R.id.register)

        registerButton.isEnabled = false

        loginPassViewModel.loginPassFormState.observe(
            viewLifecycleOwner,
            Observer { loginFormState ->
                if (loginFormState == null) {
                    return@Observer
                }
                registerButton.isEnabled = loginFormState.isDataValid
                loginFormState.emailError?.let {
                    emailEditText.error = getString(it)
                }
                loginFormState.usernameError?.let {
                    usernameEditText.error = getString(it)
                }
                loginFormState.passwordError?.let {
                    passwordEditText.error = getString(it)
                }
            })

        val afterTextChangedListener = object : TextWatcher {
            override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) { }

            override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) { }

            override fun afterTextChanged(s: Editable) {
                loginPassViewModel.loginDataChanged(
                    usernameEditText.text.toString(),
                    passwordEditText.text.toString(),
                    emailEditText.text.toString()
                )
            }
        }
        emailEditText.addTextChangedListener(afterTextChangedListener)
        usernameEditText.addTextChangedListener(afterTextChangedListener)
        passwordEditText.addTextChangedListener(afterTextChangedListener)

        registerButton.setOnClickListener {
            //invoke fun from MainActivity
            (activity as MainActivity).register(
                usernameEditText.text.toString(),
                emailEditText.text.toString(),
                passwordEditText.text.toString()
            )
        }
    }
}