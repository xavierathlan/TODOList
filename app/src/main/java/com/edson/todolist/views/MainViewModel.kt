package com.edson.todolist.views

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class MainViewModel: ViewModel() {
    private var mUser = MutableLiveData<FirebaseUser>()

    fun getUser(): FirebaseUser? = mUser.value
    fun setUser(value: FirebaseUser) { mUser.value = value }

    fun observeUser(callback: (FirebaseUser) -> Unit) {
        mUser.observeForever(callback)
    }
}