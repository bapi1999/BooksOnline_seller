package com.sbdevs.booksonlineseller.otherclass

import com.sbdevs.booksonlineseller.models.SellerOrderModel

class OrderSharedData {

    companion object{
        var newSellerOrderList:MutableList<SellerOrderModel> = ArrayList()
        var acceptSellerOrderList:MutableList<SellerOrderModel> = ArrayList()
        var packSellerOrderList:MutableList<SellerOrderModel> = ArrayList()
    }
}