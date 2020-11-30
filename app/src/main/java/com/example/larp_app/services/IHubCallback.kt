package com.example.larp_app.services

//needed for using funs from Activity in Service
interface IHubCallback {
    fun loginSuccess(roomList: String)
    fun showDialog(title: String, message: String)
    fun hideDialog()
    fun showToast(text: String)
    fun goToLogin2()
    fun startGameActivity()
    fun getChatMessage(message: String)
}