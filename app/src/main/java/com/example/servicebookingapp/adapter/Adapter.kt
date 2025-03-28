package com.example.servicebookingapp.adapter

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.servicebookingapp.R
import com.example.servicebookingapp.dataClass.ServiceData
import com.google.firebase.firestore.FirebaseFirestore


class Adapter (private val serviceList : ArrayList<ServiceData> , private val db: FirebaseFirestore,
               private val userDocumentId: String) : RecyclerView.Adapter<Adapter.MyViewHolder>() {
    class MyViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView){
        val serviceName : TextView = itemView.findViewById(R.id.serviceName)
        val serviceDT : TextView = itemView.findViewById(R.id.serviceDT)
        val serviceAddNote : TextView = itemView.findViewById(R.id.serviceAddNotes)
        val serviceStatus : TextView = itemView.findViewById(R.id.tvServiceStatus)
        val deleteButton : ImageButton = itemView.findViewById(R.id.imageButton)
    }


    override fun onCreateViewHolder( parent: ViewGroup, viewType: Int ): MyViewHolder {
        val itemView = LayoutInflater.from(parent.context).inflate(R.layout.service_list,parent,false)
        return MyViewHolder(itemView)
    }

    override fun onBindViewHolder( holder: MyViewHolder, position: Int) {
        val service = serviceList[position]
        holder.serviceName.text = serviceList[position].serviceType
        holder.serviceDT.text = serviceList[position].dateTime
        holder.serviceAddNote.text = serviceList[position].notes
        holder.serviceStatus.text = serviceList[position].status

        if (service.status != "Pending"){
            holder.deleteButton.isEnabled = false
            holder.deleteButton.visibility = View.GONE
        }

        holder.deleteButton.setOnClickListener {
            service.documentId?.let { docId ->
                db.collection("Users").document(userDocumentId)
                    .collection("serviceDetails")
                    .document(docId)
                    .delete()
                    .addOnSuccessListener {
                        serviceList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, serviceList.size)
                    }
                    .addOnFailureListener { e ->
                        Log.d("Delete ","Failed ${e}")
                    }
            }
        }

    }

    override fun getItemCount(): Int {
        return serviceList.size
    }


}