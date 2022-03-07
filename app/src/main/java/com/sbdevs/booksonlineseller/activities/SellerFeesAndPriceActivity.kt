package com.sbdevs.booksonlineseller.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.Button
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.messaging.FirebaseMessaging
import com.google.gson.Gson
import com.sbdevs.booksonlineseller.databinding.ActivitySellerFeesAndPriceBinding
import com.sbdevs.booksonlineseller.otherclass.NotificationData
import com.sbdevs.booksonlineseller.otherclass.PushNotification
import com.sbdevs.booksonlineseller.otherclass.RetrofitInstance
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

const val TOPIC = "/topics/myTopic2"

class SellerFeesAndPriceActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySellerFeesAndPriceBinding
    private lateinit var enterPriceInput:TextInputLayout
    private lateinit var calculateBtn:Button
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerFeesAndPriceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enterPriceInput = binding.lay1.enterPriceInputLayout
        calculateBtn = binding.lay1.calculateBtn

        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        calculateBtn.setOnClickListener {
            //checkInput()

            val title = "etTitle.text.toString()"
            val message = "etMessage.text.toString()"
            //val recipientToken = etToken.text.toString()
            if(title.isNotEmpty() && message.isNotEmpty() ) {
                PushNotification(
                    NotificationData(title, message),
                    TOPIC
                ).also {
                    sendNotification(it)
                }
            }

        }


    }

    private fun checkInput(){
        val value = enterPriceInput.editText!!.text.toString().trim()
        if (value.isEmpty()){
            enterPriceInput.error = "Field can't be empty"

        }else{
            enterPriceInput.error = ""
           calculateProfit(value.toInt())
        }
    }

    private fun calculateProfit(sellingPrice:Int){
        val lay2 = binding.lay2
        val platformCharge = sellingPrice/10F
        val pickupCharge = 30F
        val profit:Float = sellingPrice - platformCharge-pickupCharge

        lay2.sellingPrice.text = sellingPrice.toString()
        lay2.commissionFee.text = platformCharge.toString()
        lay2.deliveryFee.text = pickupCharge.toString()
        lay2.totalProfit.text = profit.toString()

    }

    private fun sendNotification(notification: PushNotification) = CoroutineScope(Dispatchers.IO).launch {
        try {
            val response = RetrofitInstance.api.postNotification(notification)
            if(response.isSuccessful) {
                Log.d("TAG", "Response: ${Gson().toJson(response)}")
            } else {
                Log.e("error2", response.errorBody().toString())
            }
        } catch(e: Exception) {
            Log.e("error1", e.toString())
        }
    }


}