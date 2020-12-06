package com.example.larp_app.services

//needed for using funs from Activity in Service
interface IHubCallback {
    //common funs
    fun showDialog(title: String, message: String)
    fun hideDialog()
    fun showToast(text: String)
    fun goToLogin()

    //MainActivity funs
    fun loginSuccess(roomList: String)
    fun startGameActivity()

    //GameActivity funs
    fun getChatMessage(message: String)
    fun showOnMap(coords: String)
}