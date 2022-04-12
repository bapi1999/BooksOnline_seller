package com.sbdevs.booksonlineseller.activities

import android.content.Context
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.android.volley.Request
import com.android.volley.RequestQueue
import com.android.volley.Response
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.messaging.FirebaseMessaging
import com.sbdevs.booksonlineseller.databinding.ActivitySellerFeesAndPriceBinding
import com.sbdevs.booksonlineseller.otherclass.Constants.Companion.BASE_URL
import com.sbdevs.booksonlineseller.otherclass.Constants.Companion.CONTENT_TYPE_FCM
import com.sbdevs.booksonlineseller.otherclass.Constants.Companion.SERVER_KEY
import com.sbdevs.booksonlineseller.otherclass.FirebaseService
import com.sbdevs.booksonlineseller.otherclass.FixedPriceClass
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.json.JSONException
import org.json.JSONObject

const val TOPIC = "/topics/myTopic2"

class SellerFeesAndPriceActivity : AppCompatActivity() {
    private lateinit var binding:ActivitySellerFeesAndPriceBinding
    private lateinit var enterPriceInput:TextInputLayout
    private lateinit var calculateBtn:Button
    val buyerToken = "dAQSAUqOT7SNZ8wRmm4CVE:APA91bHScwPLlHw8TngGYJuzlukMD_bY1gWu1TS4sBGxniae-FEpKGrC-VEZ4A6Ge528Hi-fHCk0i3HtHrJUWseHhW37visyxNbFbw_S4e0JJpEqpsxGYwOzKMyeFpkMgqZneSat3o_3"
    //

    private var showAndHideProfit = true



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerFeesAndPriceBinding.inflate(layoutInflater)
        setContentView(binding.root)

        enterPriceInput = binding.lay1.enterPriceInputLayout
        calculateBtn = binding.lay1.calculateBtn

        FirebaseService.sharedPref = getSharedPreferences("sharedPref", Context.MODE_PRIVATE)
        FirebaseMessaging.getInstance().subscribeToTopic(TOPIC)

        calculateBtn.setOnClickListener {
            checkInput()

        }

        binding.lay2.hideAndShoWText.visibility = View.VISIBLE

        binding.lay2.hideAndShoWText.setOnClickListener {

            if (showAndHideProfit){
                showAndHideProfit = false
                binding.lay2.hideAndShoWText.text = "Show"
                binding.lay2.allPriceContainer.visibility = View.GONE
            }else{
                showAndHideProfit = true
                binding.lay2.hideAndShoWText.text = "Hide"
                binding.lay2.allPriceContainer.visibility = View.VISIBLE
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
        val pickupCharge = FixedPriceClass.pickupCharge //change the pickup charge in fixedPriceClass
        val profit:Float = sellingPrice - platformCharge-pickupCharge

        lay2.sellingPrice.text = sellingPrice.toString()
        lay2.commissionFee.text = platformCharge.toString()
        lay2.deliveryFee.text = pickupCharge.toString()
        lay2.totalProfit.text = profit.toString()

    }


    //TODO-<<<<<<<<<<<<<=================  SEND NOTIFICATION ==============================================
    private fun sendNotificationStep1(title:String,message:String){
        val topic = "/topics/Enter_your_topic_name" //topic has to match what the receiver subscribed to

        val notification = JSONObject()
        val notifcationBody = JSONObject()

        try {
            notifcationBody.put("title", "Enter_title")
            notifcationBody.put("message", message)   //Enter your notification message
            notification.put("to", buyerToken)
            notification.put("data", notifcationBody)
            Log.e("TAG", "try")
        } catch (e: JSONException) {
            Log.e("TAG", "onCreate: " + e.message)
        }

        sendNotificationStep2(notification)

    }

    private fun sendNotificationStep2(notification: JSONObject) {
        Log.e("TAG", "sendNotification")
        val jsonObjectRequest = object : JsonObjectRequest(Request.Method.POST,BASE_URL, notification,
            Response.Listener<JSONObject> { response ->
                Log.i("sendNotificationStep2", "onResponse: $response")

            },
            Response.ErrorListener {
                Toast.makeText(this, "Request error", Toast.LENGTH_LONG).show()
                Log.i("sendNotificationStep2 error", "${it.message}")
            }) {

            override fun getHeaders(): Map<String, String> {
                val params = HashMap<String, String>()
                params["Authorization"] = SERVER_KEY
                params["Content-Type"] = CONTENT_TYPE_FCM
                return params
            }
        }
        requestQueue.add(jsonObjectRequest)
    }

    private val requestQueue: RequestQueue by lazy {
        Volley.newRequestQueue(this.applicationContext)
    }

    //TODO-==============================  SEND NOTIFICATION ==========================>>>>>>>>>>>>>>>>>>>>>
}


