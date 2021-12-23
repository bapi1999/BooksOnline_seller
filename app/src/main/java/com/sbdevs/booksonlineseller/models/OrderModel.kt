package com.sbdevs.booksonlineseller.models

import com.google.firebase.Timestamp
import java.util.*

data class OrderModel (
    val orderTime:Date = Date(),
    val productThumbnail:String = "",
    val productTitle:String = "",
    val status:String = "",
    val ordered_Qty:Long = 0L,
    val price:Long = 0L,

    val Time_ordered:Date = Date(),
    val Time_accepted:Date = Date(),
    val Time_packed:Date = Date(),
    val Time_shipped:Date = Date(),
    val Time_delivered:Date = Date(),
    val Time_returned:Date = Date(),
    val Time_canceled:Date = Date(),

        )