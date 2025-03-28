package com.example.servicebookingapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.servicebookingapp.R
import com.example.servicebookingapp.dataClass.ServiceData
import com.google.firebase.firestore.FirebaseFirestore

class AdapterProvider(private val serviceList : ArrayList<ServiceData> , private val db: FirebaseFirestore,
                      private val userDocumentId: String) : RecyclerView.Adapter<AdapterProvider.MyViewHolder>() {
    class MyViewHolder(itemView : View) : RecyclerView.ViewHolder(itemView){
        val serviceNameProvider : TextView = itemView.findViewById(R.id.serviceNameProvider)
        val serviceDTProvider : TextView = itemView.findViewById(R.id.serviceDTProvider)
        val serviceAddNotesProvider : TextView = itemView.findViewById(R.id.serviceAddNotesProvider)
        val btnAccept : Button = itemView.findViewById(R.id.btnAccept)
        val btnCompleted : Button = itemView.findViewById(R.id.btnCompleted)
    }
    override fun onCreateViewHolder(parent: ViewGroup,viewType: Int): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.service_provider_list, parent , false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: MyViewHolder,position: Int) {
        val service = serviceList[position]
        holder.serviceNameProvider.text = serviceList[position].serviceType
        holder.serviceDTProvider.text = serviceList[position].dateTime
        holder.serviceAddNotesProvider.text = serviceList[position].notes

        if (service.provider.isNullOrEmpty()){
            holder.btnAccept.isEnabled = true
            holder.btnCompleted.isEnabled = false
        }else if (service.provider == userDocumentId){
            holder.btnAccept.isEnabled = false
            holder.btnCompleted.isEnabled = true
        }else{
            holder.btnAccept.isEnabled = false
            holder.btnCompleted.isEnabled = false
        }

        holder.btnAccept.setOnClickListener {
            val serviceRef = db.collection("Users").document(userDocumentId)
                .collection("serviceDetails").document(service.documentId!!)
            Log.d("Document :" , "${userDocumentId.toString()} ${service.documentId.toString()}" )

            serviceRef.update("provider", userDocumentId)
                .addOnSuccessListener {
                    holder.btnAccept.isEnabled = false
                    holder.btnCompleted.isEnabled = true
                    service.provider = userDocumentId
                    serviceRef.update("status","Accepted")
                }
                .addOnFailureListener { e ->
                    // Handle error
                }
        }

        holder.btnCompleted.setOnClickListener {
            val serviceRef = db.collection("Users").document(userDocumentId)
                .collection("serviceDetails").document(service.documentId!!)
            serviceRef.update("status" , "Completed")
                .addOnSuccessListener {
                    holder.btnCompleted.isEnabled = false
                }
        }

    }

    override fun getItemCount(): Int {
        return serviceList.size
    }


}