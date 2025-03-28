package com.example.servicebookingapp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.servicebookingapp.adapter.AdapterProvider
import com.example.servicebookingapp.dataClass.ServiceData
import com.example.servicebookingapp.viewmodel.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

class ServiceProvider : AppCompatActivity() {

    private lateinit var userNameProvider : TextView
    private lateinit var serviceRequestCard : CardView
    private lateinit var providerRecycler: RecyclerView
    private lateinit var db : FirebaseFirestore
    private lateinit var user : FirebaseUser
    private lateinit var documentId : String
    private lateinit var serviceList : ArrayList<ServiceData>
    private lateinit var logOut : Button



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_service_provider)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        logOut = findViewById(R.id.btnLogOut)
        userNameProvider = findViewById(R.id.tvUserNameProvider)
        serviceRequestCard = findViewById(R.id.serviceRequestsCard)
        providerRecycler = findViewById(R.id.providerRecycler)
        providerRecycler.layoutManager = LinearLayoutManager(this)
        db = Firebase.firestore
        user = User.user!!
        serviceList = arrayListOf()


//
        lifecycleScope.launch {
            documentId = getDocId() ?: return@launch

            db.collection("Users").document(documentId).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name")
                    userNameProvider.text = name
                }

            db.collection("Users")
                .whereEqualTo("type" , "Customer")
                .get()
                .addOnSuccessListener { documents ->
                    Toast.makeText(baseContext, "Data Loaded", Toast.LENGTH_SHORT).show()
                    serviceList.clear()

                    if (documents.isEmpty) {
                        serviceRequestCard.visibility = View.VISIBLE
                        return@addOnSuccessListener
                    }

                    var totalTasks = documents.size()
                    var completedTasks = 0

                    for (document in documents) {
                        val documentId = document.id
                        db.collection("Users").document(documentId)
                            .collection("serviceDetails")
                            .get()
                            .addOnSuccessListener { serviceDocument ->
                                if (!serviceDocument.isEmpty) {
                                    for (subDoc in serviceDocument) {
                                        val service: ServiceData? = subDoc.toObject(ServiceData::class.java)
                                        val provider = subDoc.getString("provider") ?: ""
                                        if (service != null) {
                                            service.documentId = subDoc.id
                                            if (provider.isEmpty() || provider == documentId){
                                                serviceList.add(service)
                                            }
                                        }
                                    }
                                }

                                completedTasks++
                                if (completedTasks == totalTasks) {
                                    Log.d("ServiceList", "Size: ${serviceList.size}, Data: $serviceList")
                                    providerRecycler.adapter = AdapterProvider(serviceList,db,documentId)
                                    serviceRequestCard.visibility =
                                        if (serviceList.isEmpty()) View.VISIBLE else View.GONE
                                }
                            }
                    }
                }
        }
        logOut.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }

    }


    private suspend fun getDocId(): String? {
        return try {
            val documents = db.collection("Users")
                .whereEqualTo("phone", user.phoneNumber)
                .get()
                .await()

            documents.documents.firstOrNull()?.id // Return first document ID or null
        } catch (e: Exception) {
            Log.e("FirestoreError", "Error getting document ID: ${e.message}")
            null
        }
    }


}