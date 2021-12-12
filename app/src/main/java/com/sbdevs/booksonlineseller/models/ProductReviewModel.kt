package com.sbdevs.booksonlineseller.models

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp
import java.util.*

data class ProductReviewModel(
    val buyer_ID: String = "",
    val buyer_name: String = "",
    val rating: Int = -1,
    val review: String ="",
    @ServerTimestamp
    val review_Date: Date? = null
)