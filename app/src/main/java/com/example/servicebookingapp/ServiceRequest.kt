package com.example.servicebookingapp

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.AdapterView
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
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
import com.example.servicebookingapp.adapter.Adapter
import com.example.servicebookingapp.dataClass.ServiceData
import com.example.servicebookingapp.viewmodel.User
import com.google.firebase.Firebase
import com.google.firebase.auth.FirebaseUser
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.Calendar

class ServiceRequest : AppCompatActivity() {
    private lateinit var serviceType : Spinner
    private lateinit var otherService : EditText
    private lateinit var datePicker : TextView
    private lateinit var serviceNote : EditText
    private lateinit var userName : TextView
    private lateinit var recyclerView: RecyclerView
    private lateinit var serviceList : ArrayList<ServiceData>
    private lateinit var pendingRequestCard : CardView
    private lateinit var documentId : String
    private lateinit var db : FirebaseFirestore
    private lateinit var user : FirebaseUser
    private lateinit var logOut : Button
    private lateinit var submit : Button
    private var selectedDate: String = ""
    private var selectedTime: String = ""

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_service_request)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }
        db = Firebase.firestore
        user = User.user!!
        logOut = findViewById(R.id.btnLogOutServiceReq)
        serviceType = findViewById(R.id.spinnerServiceType)
        datePicker = findViewById(R.id.etDatePicker)
        serviceNote = findViewById(R.id.etServiceNotes)
        submit = findViewById(R.id.btnSubmitServcie)
        userName = findViewById(R.id.tvUserNameCustomer)
        pendingRequestCard = findViewById(R.id.pendingRequestCard)
        submit.isEnabled = false
        otherService = findViewById(R.id.etOtherService)


        recyclerView = findViewById(R.id.serviceRecycler)
        recyclerView.layoutManager = LinearLayoutManager(baseContext)
        serviceList = arrayListOf()


        lifecycleScope.launch {
            documentId = getDocId() ?: return@launch

            db.collection("Users").document(documentId).get()
                .addOnSuccessListener { doc ->
                    val name = doc.getString("name")
                    userName.text = name
                }

            db.collection("Users").document(documentId).collection("serviceDetails").get()
                .addOnSuccessListener {
                    Toast.makeText(baseContext,"Data Loaded", Toast.LENGTH_SHORT).show()
                    serviceList.clear()
                    if (!it.isEmpty){
                        for (data in it.documents){
                            Log.d("Documents ",it.documents.toString())
                            val service : ServiceData? = data.toObject(ServiceData::class.java)
                            if (service != null) {
                                service.documentId = data.id // Assign Firestore document ID
                                serviceList.add(service)
                            }
                        }
                        recyclerView.adapter = Adapter(serviceList, db , documentId)
                        pendingRequestCard.visibility = View.GONE
                    }
                    else{
                        pendingRequestCard.visibility = View.VISIBLE
                    }
                }
        }


        datePicker.setOnClickListener {
            showDatePicker()
        }

        serviceType.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(
                parent: AdapterView<*>?,
                view: View?,
                position: Int,
                id: Long
            ) {
                if (serviceType.selectedItem.toString() == "Other"){
                    otherService.visibility = View.VISIBLE
                }
                else{
                    otherService.visibility = View.GONE
                }
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {
                return
            }

        }


        submit.setOnClickListener {
            var service = serviceType.selectedItem.toString()
            if(serviceType.selectedItem.toString() == "Other"){
                service = otherService.text.toString()
            }

            val dateAndTime = "$selectedDate $selectedTime"
            Log.d("Date and Time " , dateAndTime)
            Log.d("Service ", service)

            val serviceData = hashMapOf(
                "serviceType" to service,
                "dateTime" to dateAndTime,
                "notes" to serviceNote.text.toString(),
                "status" to "Pending",
                "provider" to ""
            )

            db.collection("Users")
                .whereEqualTo("phone",user?.phoneNumber.toString())
                .get()
                .addOnSuccessListener { documents ->
                    for (document in documents){
                        val documentId = document.id
                        db.collection("Users")
                            .document(documentId)
                            .collection("serviceDetails")
                            .add(serviceData)
                            .addOnSuccessListener {
                                Toast.makeText(this,"${service} Request saved", Toast.LENGTH_SHORT).show()
                                clearFields()
                                fetchData()
                            }
                    }
                }
            }
        logOut.setOnClickListener {
            val intent = Intent(this, MainActivity::class.java)
            startActivity(intent)
        }
    }


    private fun showDatePicker() {
        val calendar = Calendar.getInstance()
        val year = calendar.get(Calendar.YEAR)
        val month = calendar.get(Calendar.MONTH)
        val day = calendar.get(Calendar.DAY_OF_MONTH)

        val datePicker = DatePickerDialog(this, { _, selectedYear, selectedMonth, selectedDay ->
            selectedDate = "$selectedDay-${selectedMonth + 1}-$selectedYear"
            showTimePicker()
        }, year, month, day)

        datePicker.show()
    }


    private fun showTimePicker() {
        val calendar = Calendar.getInstance()
        val hour = calendar.get(Calendar.HOUR_OF_DAY)
        val minute = calendar.get(Calendar.MINUTE)

        val timePicker = TimePickerDialog(this, { _, selectedHour, selectedMinute ->
            selectedTime = String.format("%02d:%02d", selectedHour, selectedMinute)
            updateResult()
        }, hour, minute, false)

        timePicker.show()
    }


    private fun updateResult() {
        datePicker.text = "Selected Date & Time: $selectedDate $selectedTime"
        submit.isEnabled = true
    }

    private fun clearFields() {
        serviceType.setSelection(0)  // Reset spinner to first item
        otherService.text.clear()     // Clear "Other Service" EditText
        otherService.visibility = View.GONE // Hide it
        serviceNote.text.clear()      // Clear service note EditText
        datePicker.text = "Select Date & Time" // Reset TextView
        selectedDate = ""             // Reset stored date
        selectedTime = ""             // Reset stored time
        submit.isEnabled = false      // Disable submit button
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


    private fun fetchData() {
        serviceList.clear() // Clear existing data
        db.collection("Users").document(documentId).collection("serviceDetails").get()
            .addOnSuccessListener { it ->
                if (!it.isEmpty){
                    for (data in it.documents){
                        Log.d("Documents ",it.documents.toString())
                        val service : ServiceData? = data.toObject(ServiceData::class.java)
                        if (service != null) {
                            service.documentId = data.id // Assign Firestore document ID
                            serviceList.add(service)
                        }
                    }
                    recyclerView.adapter = Adapter(serviceList, db , documentId)
                    pendingRequestCard.visibility = View.GONE
                }
                else{
                    pendingRequestCard.visibility = View.VISIBLE
                }
            }
    }



}



