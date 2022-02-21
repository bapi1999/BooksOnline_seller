package com.sbdevs.booksonlineseller.models

import com.google.firebase.Timestamp
import java.util.*


data class PaymentRequestModel(
    val is_paid:Boolean = false,
//    val time: Timestamp = Timestamp(Date()),
    val time: Date = Date(),
    val amount:Long = 0
)