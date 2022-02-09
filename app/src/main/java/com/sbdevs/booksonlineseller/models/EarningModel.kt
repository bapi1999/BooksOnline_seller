package com.sbdevs.booksonlineseller.models

import java.util.*

data class EarningModel(
    val PRICE_SELLING_UNIT: Long = 0L,
    val PRICE_SELLING_TOTAL: Long = 0L,
    val PRICE_TOTAL: Long = 0L,
    val Time_period: Long = 0L,
    val Time_delivered: Date? = Date(),

)