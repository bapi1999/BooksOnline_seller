package com.sbdevs.booksonlineseller.models

import java.util.*

data class MyProductModel (
    val productId:String = "",
    val book_title: String = "",
    val product_thumbnail: String = "",
    val price_selling: Long = 0,
    val price_original: Long = 0,
    val rating_avg: String = "",
    val rating_total:Long = 0,
    val in_stock_quantity: Long = 0,
    val PRODUCT_UPDATE_ON :Date =  Date()


    )