package com.sbdevs.booksonlineseller.models

import java.util.*

data class NotificationModel(
    val NOTIFICATION_CODE:Long = 0L,
    val date: Date =  Date(),
    val description:String = "",
    val image:String = "",
    val order_id:String = "",
    val seen:Boolean = false
    )