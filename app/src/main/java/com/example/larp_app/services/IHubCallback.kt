package com.example.larp_app.services

//needed for using funs from Activity in Service
interface IHubCallback {
    fun loginSuccess()
    fun loginRegisterError(text: String)
    fun showDialog(title: String, message: String)
    fun hideDialog()
    fun goToLogin2()
}