package com.sbdevs.booksonlineseller.activities

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.lifecycle.lifecycleScope
import com.bumptech.glide.Glide
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.ActivityOrderDetailsBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import com.sbdevs.booksonlineseller.otherclass.FireStoreData
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap

class OrderDetailsActivity : AppCompatActivity() {

    private lateinit var binding: ActivityOrderDetailsBinding

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var viewProductBtn:AppCompatButton

    val visible= View.VISIBLE
    val gone = View.GONE

    private val loadingDialog = LoadingDialog()

    private lateinit var buyerId:String
    private lateinit var imageUrl:String
    private lateinit var productName: String

    private lateinit var orderId: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityOrderDetailsBinding.inflate(layoutInflater)
        setContentView(binding.root)
        orderId = intent.getStringExtra("orderId").toString().trim()
        viewProductBtn = binding.lay1.viewProductBtn
        loadingDialog.show(supportFragmentManager,"show")

        lifecycleScope.launch(Dispatchers.Main) {
            withContext(Dispatchers.IO){
                getOrderDetails(orderId)
            }

        }




    }


    override fun onStart() {
        super.onStart()

//        binding.lay1.viewProductBtn.setOnClickListener {
//
//        }

        binding.acceptOrderBtn.setOnClickListener {
            updateOrder(orderId,"accepted")
        }

        binding.rejectOrderBtn.setOnClickListener {
            updateOrder(orderId,"rejected")
        }


        binding.packedBtn.setOnClickListener {
            updateOrder(orderId,"packed")
        }


        binding.shippedBtn.setOnClickListener {
            //send notification may produce runtime exception
            updateOrder(orderId,"shipped")
            sendNotification(buyerId,productName,imageUrl,"Shipped")
        }

        binding.cancelOrderBtn.setOnClickListener {
            updateOrder(orderId,"canceled")
        }

    }


    private fun getOrderDetails(orderId:String)  = CoroutineScope(Dispatchers.IO).launch{
        val lay3 = binding.lay3

        firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("ORDERS")
            .document(orderId)
            .get().addOnSuccessListener {

                val orderTime: Date = it.getTimestamp("Time_ordered")!!.toDate()
                imageUrl =  it.getString("productThumbnail").toString()
                productName =  it.getString("productTitle").toString()
                val status =  it.getString("status").toString()
                val orderedQty =  it.getLong("ordered_Qty")
                val price =  it.getLong("price")

                buyerId =  it.getString("buyerId").toString()
                val productId =  it.getString("productId")
                val tracKingId =  it.getString("tracKingId")

                binding.statusTxt.text = status
                binding.orderIdTxt.text = orderId
                binding.trackingIdTxt.text = tracKingId


                binding.lay1.titleTxt.text = productName
                binding.lay1.priceTxt.text = price.toString()
                binding.lay1.productQuantity.text = orderedQty.toString()
                Glide.with(this@OrderDetailsActivity)
                    .load(imageUrl)
                    .placeholder(R.drawable.as_square_placeholder)
                    .into(binding.lay1.productImage)

                val timeAgo:String = FireStoreData().msToTimeAgo(this@OrderDetailsActivity,orderTime)
                binding.orderedTimeText.text = timeAgo







                val address:MutableMap<String,Any> = it.get("address") as MutableMap<String,Any>
                //val address:MutableMap<String,Any> = it.get("address") as MutableMap<String,Any>

                if (address.isEmpty()){

                    binding.orderWrongText.visibility = visible
                    binding.orderAddressContainer.visibility = gone


                }

                when(status){
                    "new" ->{
                        binding.acceptButtonContainer.visibility = visible
                        binding.buttonContainer2.visibility = gone
                        lay3.orderDate.text = getDateTime(orderTime)

                        lay3.orderImageButton.backgroundTintList = AppCompatResources
                            .getColorStateList(this@OrderDetailsActivity,R.color.amber_600)
                        lay3.orderImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)
                    }
                    "accepted" ->{
                        val acceptTime: Date = it.getTimestamp("Time_accepted")!!.toDate()

                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = visible
                        binding.packedBtn.visibility = visible
                        binding.shippedBtn.visibility =gone
                        //
                        binding.lay3.acceptDate.text = getDateTime(acceptTime)
                        lay3.acceptImageButton.backgroundTintList = AppCompatResources
                            .getColorStateList(this@OrderDetailsActivity,R.color.successGreen)
                        lay3.acceptImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)

                    }
                    "packed" ->{
                        val packTime: Date = it.getTimestamp("Time_packed")!!.toDate()
                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = visible
                        binding.packedBtn.visibility = gone
                        binding.shippedBtn.visibility =visible

                        binding.lay3.packedDate.text = getDateTime(packTime)
                        lay3.packedImageButton.backgroundTintList = AppCompatResources
                            .getColorStateList(this@OrderDetailsActivity,R.color.blueLink)
                        lay3.packedImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)

                    }

                    "shipped"->{
                        val shipTime: Date = it.getTimestamp("Time_shipped")!!.toDate()
                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = gone

                        binding.lay3.shippedDate.text = getDateTime(shipTime)
                        lay3.shippedImageButton.backgroundTintList = AppCompatResources
                            .getColorStateList(this@OrderDetailsActivity,R.color.indigo_500)
                        lay3.shippedImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)
                    }
                    "delivered"->{
                        val deliveredTime: Date = it.getTimestamp("Time_delivered")!!.toDate()
                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = gone

                        binding.lay3.deliveredDate.text = getDateTime(deliveredTime)
                        lay3.deliveredImageButton.backgroundTintList = AppCompatResources
                            .getColorStateList(this@OrderDetailsActivity,R.color.ratingGreen)
                        lay3.deliveredImageButton.setImageResource(R.drawable.ic_check_circle_outline_24)

                    }
                    "returned"->{
                        val returnTime: Date = it.getTimestamp("Time_return")!!.toDate()
                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = gone
                    }
                    "canceled"->{
                        val returnTime: Date = it.getTimestamp("Time_canceled")!!.toDate()
                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = gone
                        binding.orderCancelText.visibility = visible
                    }
                    else ->{
                        binding.acceptButtonContainer.visibility = gone
                        binding.buttonContainer2.visibility = gone
                    }
                }

                binding.lay1.viewProductBtn.setOnClickListener {
                    val productIntent = Intent(this@OrderDetailsActivity,ProductActivity::class.java)
                    productIntent.putExtra("productId",productId)
                    startActivity(productIntent)
                }

                loadingDialog.dismiss()

            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Log.e("Load Order details","${it.message}")
            }.await()
    }


    private fun updateOrder(orderId: String, status:String){

        val orderMap:MutableMap<String,Any> = HashMap()
        orderMap["status"] = status

        orderMap["Time_$status"] = FieldValue.serverTimestamp()
        firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("ORDERS")
            .document(orderId).update(orderMap)
            .addOnSuccessListener {
                Log.i("$status order","successful")
            }
            .addOnFailureListener {
                Log.e("$status order","${it.message}")
            }

    }

    private fun sendNotification(buyerId:String,productName:String,url:String,status: String){
        val ref = firebaseFirestore.collection("USERS").document(buyerId).collection("USER_DATA")
            .document("MY_NOTIFICATION").collection("NOTIFICATION")

        val notificationMap: MutableMap<String, Any> = HashMap()
        notificationMap["date"] = FieldValue.serverTimestamp()
        notificationMap["description"] = "$status:$productName"
        notificationMap["image"] = url
        notificationMap["order_id"] = orderId
        notificationMap["seller_id"] = user!!.uid
        notificationMap["seen"] = false


        ref.add(notificationMap)
            .addOnSuccessListener {

        }.addOnFailureListener {
            Log.e("get buyer notification","${it.message}")
        }
    }

    @SuppressLint("SimpleDateFormat")
    private fun getDateTime(date: Date): String? {
        return try {
            val sdf = SimpleDateFormat("dd MMMM yyyy hh:mm a")
            //val netDate = Date(s.toLong() * 1000)
            sdf.format(date)
        } catch (e: Exception) {
            e.toString()
        }
    }


}