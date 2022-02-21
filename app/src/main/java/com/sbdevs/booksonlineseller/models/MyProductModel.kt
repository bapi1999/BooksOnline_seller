package com.sbdevs.booksonlineseller.models

import java.util.*
import kotlin.collections.ArrayList

data class MyProductModel (
    val productId:String = "",
    val book_title: String = "",
    val product_thumbnail: String = "",
    val productImage_List:MutableList<String> = ArrayList(),
    val price_selling: Long = 0,
    val price_original: Long = 0,
    val rating_avg: String = "",
    val rating_total:Long = 0,
    val in_stock_quantity: Long = 0,
    val PRODUCT_UPDATE_ON :Date =  Date()


    )