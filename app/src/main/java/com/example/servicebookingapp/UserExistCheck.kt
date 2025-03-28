package com.example.servicebookingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.example.servicebookingapp.viewmodel.User
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore

class UserExistCheck : AppCompatActivity() {
    private lateinit var documentId : String
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_user_exist_check)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        val db = Firebase.firestore
        val user = User.user


        db.collection("Users")
            .whereEqualTo("phone", user?.phoneNumber)
            .get()
            .addOnSuccessListener { documents ->
                if (!documents.isEmpty) {
                    val firstDoc = documents.documents.first()
                    documentId = firstDoc.id  // Assign the document ID

                    // Now fetch the user type using the correct documentId
                    db.collection("Users")
                        .document(documentId)
                        .get()
                        .addOnSuccessListener { document ->
                            if (document.exists()) {
                                val userType = document.getString("type")
                                if (userType == "Customer") {
                                    val intent = Intent(this, ServiceRequest::class.java)
                                    startActivity(intent)
                                } else {
                                    val intent = Intent(this, ServiceProvider::class.java)
                                    startActivity(intent)
                                }
                            }
                        }
                        .addOnFailureListener { e ->
                            Log.e("FirestoreError", "Error fetching user type: ${e.message}")
                        }
                } else {
                    Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show()
                    val intent = Intent(this, RegisterUser::class.java)
                    startActivity(intent)
                }
            }
            .addOnFailureListener { e ->
                Log.e("FirestoreError", "Error checking user existence: ${e.message}")
            }

    }
}