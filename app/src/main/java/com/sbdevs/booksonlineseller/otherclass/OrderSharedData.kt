package com.sbdevs.booksonlineseller.otherclass

import com.sbdevs.booksonlineseller.models.OrderModel

class OrderSharedData {

    companion object{
        var newOrderList:MutableList<OrderModel> = ArrayList()
        var acceptOrderList:MutableList<OrderModel> = ArrayList()
        var packOrderList:MutableList<OrderModel> = ArrayList()
    }
}