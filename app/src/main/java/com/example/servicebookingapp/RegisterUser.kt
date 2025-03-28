package com.example.servicebookingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.servicebookingapp.viewmodel.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class RegisterUser : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register_user)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val db = Firebase.firestore

        val userName : EditText = findViewById(R.id.etName)
        val userType : Spinner = findViewById(R.id.spinnerType)
        val btnSubmit : Button = findViewById(R.id.btnSubmit)



        btnSubmit.setOnClickListener {
            val user = hashMapOf(
                "name" to userName.text.toString(),
                "phone" to User.user?.phoneNumber.toString(),
                "type" to userType.selectedItem.toString()
            )
            db.collection("Users")
                .add(user)
                .addOnSuccessListener { documentReference ->
                    Log.d("User added : ", "DocumentSnapshot added with ID: ${documentReference.id}")
                    val intent = Intent(this, MainActivity::class.java)
                    startActivity(intent)
            }
            .addOnFailureListener { e ->
                Log.w("User not added : ", "Error adding document", e)

            }
        }


    }
}