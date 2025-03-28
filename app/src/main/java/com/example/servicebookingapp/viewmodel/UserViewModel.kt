package com.example.servicebookingapp.viewmodel
import androidx.lifecycle.ViewModel
import com.google.firebase.auth.FirebaseUser

class UserViewModel : ViewModel() {
    var user : FirebaseUser? = null

}

val User = UserViewModel()