package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentMyEarningBinding
import com.sbdevs.booksonlineseller.models.OrderModel
import com.squareup.okhttp.Dispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MyEarningFragment : Fragment() {
    private var _binding:FragmentMyEarningBinding? =null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private val loadingDialog = LoadingDialog()
    private var orderList:MutableList<OrderModel> = ArrayList()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyEarningBinding.inflate(inflater, container, false)

        binding.lay1.withdrawalBtn.setOnClickListener {
            val action = MyEarningFragmentDirections.actionMyEarningFragmentToWithdrawalFragment()
            findNavController().navigate(action)

//
        }

        return binding.root
    }

    private fun getDeliveredProduct(){

        firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("ORDERS")
            .whereEqualTo("status","new")
            .whereEqualTo("eligible_for_credit",true)
            .whereEqualTo("already_credited",false)
            .orderBy("Time_ordered", Query.Direction.DESCENDING)
            .limit(10L).get().addOnSuccessListener {
                val allDocumentSnapshot = it.documents

                if (allDocumentSnapshot.isNotEmpty()){
                    for (item in allDocumentSnapshot){

                        val orderId = item.id
                        val imageUrl =  item.getString("productThumbnail").toString()
                        val productName =  item.getString("productTitle").toString()
                        val statusString =  item.getString("status").toString()
                        val orderedQty =  item.getLong("ordered_Qty")!!
                        val price =  item.getLong("price")!!
                        val buyerId =  item.getString("buyerId").toString()
                        val already_paid:Boolean = item.getBoolean("already_paid")!!
                        val orderTime: Date = item.getTimestamp("Time_ordered")!!.toDate()
                        val acceptedTime= item.getTimestamp("Time_accepted")?.toDate()
                        val packedTime= item.getTimestamp("Time_packed")?.toDate()
                        val shippedTime= item.getTimestamp("Time_shipped")?.toDate()
                        val deliveredTime= item.getTimestamp("Time_delivered")?.toDate()
                        val returnedTime= item.getTimestamp("Time_returned")?.toDate()
                        val canceledTime= item.getTimestamp("Time_canceled")?.toDate()
                        val address:MutableMap<String,Any> = item.get("address") as MutableMap<String,Any>

                        orderList.add(
                            OrderModel(orderId,imageUrl,productName,statusString, buyerId,orderedQty,
                            price,already_paid,address,orderTime,acceptedTime,packedTime,
                            shippedTime,deliveredTime,returnedTime,canceledTime)
                        )

                    }

                }

                if (orderList.isEmpty()){

                }else{


                }

                loadingDialog.dismiss()
            }.addOnFailureListener {
                Log.e("Load orders","${it.message}")
                loadingDialog.dismiss()

            }
    }




    private suspend fun create30Notification(){
        for(i in 0..30){
            val timeString = LocalDateTime.now().toString()
            val notificationMap: MutableMap<String, Any> = HashMap()
            notificationMap["date"] = FieldValue.serverTimestamp()
            notificationMap["description"] = "welcomeNotification $i-th $timeString"
            notificationMap["image"] = ""
            notificationMap["order_id"] = ""
            notificationMap["seen"] = false

            firebaseFirestore.collection("USERS").document(user!!.uid).collection("SELLER_DATA")
                .document("SELLER_DATA").collection("NOTIFICATION").add(notificationMap)
                .addOnSuccessListener { Log.i("Notification","Successfully added") }
                .addOnFailureListener { Log.e("Notification","${it.message}") }.await()
        }
    }

}