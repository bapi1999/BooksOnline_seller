package com.sbdevs.booksonlineseller.otherclass

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel:ViewModel() {
    val newOrderData = MutableLiveData<String>()

    fun setNewOrder(newData:String){
        newOrderData.value = newData
    }

}