package com.sbdevs.booksonlineseller.otherclass

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class MainViewModel:ViewModel() {
    val data = MutableLiveData<String>()

    fun setData(newData:String){
        data.value = newData
    }
}