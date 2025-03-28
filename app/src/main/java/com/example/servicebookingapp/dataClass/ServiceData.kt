package com.example.servicebookingapp.dataClass

data class ServiceData (
    val customerName : String? = null ,
    val customerPhone : String? = null ,
    val serviceType : String? = null ,
    val dateTime : String? = null ,
    val notes : String? = null ,
    var documentId : String? = null,
    var status : String? = null,
    var provider : String? = null
)