package com.sbdevs.booksonlineseller.models

import java.util.*

data class OrderModel (
    val orderTime:Date = Date(),
    val productThumbnail:String = "",
    val productTitle:String = "",
    val status:String = "",
    val ordered_Qty:Long = 0L,
    val price:String = ""

        )