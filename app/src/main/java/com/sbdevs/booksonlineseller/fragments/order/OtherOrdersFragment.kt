package com.sbdevs.booksonlineseller.fragments.order

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.adapters.OrderAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentOtherOrdersBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import com.sbdevs.booksonlineseller.models.OrderModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList


class OtherOrdersFragment : Fragment(),OrderAdapter.OrderItemClickListener {
    private var _binding:FragmentOtherOrdersBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private var orderList:MutableList<OrderModel> = ArrayList()
    private lateinit var orderAdapter: OrderAdapter
    private lateinit var recyclerView: RecyclerView

    private var statusString = "shipped"
    private var orderByString = "Time_shipped"


    private val loadingDialog = LoadingDialog()

    private var lastResult: DocumentSnapshot? = null
    private lateinit var times: Timestamp
    private var isReachLast:Boolean = false

    val gone = View.GONE
    val visible = View.VISIBLE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOtherOrdersBinding.inflate(inflater, container, false)

        loadingDialog.show(childFragmentManager,"SHow")
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {
            getOrdersByTags(statusString,orderByString)

//            firebaseFirestore
//                .collection("ORDERS")
//                .whereEqualTo("ID_Of_SELLER",user!!.uid)
//                .whereEqualTo("status","shipped")
//                .orderBy("Time_shipped", Query.Direction.DESCENDING).get().addOnSuccessListener {
//                }
//            firebaseFirestore
//                .collection("ORDERS")
//                .whereEqualTo("ID_Of_SELLER",user!!.uid)
//                .whereEqualTo("status","delivered")
//                .orderBy("Time_delivered", Query.Direction.DESCENDING).get().addOnSuccessListener {
//                }
//            firebaseFirestore
//                .collection("ORDERS")
//                .whereEqualTo("ID_Of_SELLER",user!!.uid)
//                .whereEqualTo("status","returned")
//                .orderBy("Time_returned", Query.Direction.DESCENDING).get().addOnSuccessListener {
//                }
//            firebaseFirestore
//                .collection("ORDERS")
//                .whereEqualTo("ID_Of_SELLER",user!!.uid)
//                .whereEqualTo("is_order_canceled",true)
//                .orderBy("Time_canceled", Query.Direction.DESCENDING).get().addOnSuccessListener {  }
        }

        recyclerView = binding.orderRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)

        orderAdapter = OrderAdapter(orderList,this)
        recyclerView.adapter = orderAdapter


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (isReachLast){
                        Log.w("Query item","Last item is reached already")
                        binding.progressBar2.visibility = View.GONE
                    }else{

                        binding.progressBar2.visibility = View.VISIBLE

                        lifecycleScope.launch(Dispatchers.IO) {
                            getOrdersByTags(statusString,orderByString)
                        }


                    }
                }
            }
        })



        binding.orderTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.radioButton1->{
                    statusString = "shipped"
                    orderByString = "Time_shipped"
                    changeOrderMethod(statusString,orderByString)
                }
                R.id.radioButton2->{
                    statusString = "delivered"
                    orderByString = "Time_delivered"
                    changeOrderMethod(statusString,orderByString)
                }
                R.id.radioButton3->{
                    statusString = "returned"
                    orderByString = "Time_returned"
                    changeOrderMethod(statusString,orderByString)
                }
                R.id.radioButton4->{

                    orderList.clear()
                    orderAdapter.notifyDataSetChanged()
                    lastResult = null
                    isReachLast = true
                    statusString = "canceled"
                    orderByString = "Time_canceled"
                    loadingDialog.show(childFragmentManager,"Show")
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        getCanceledOrders()
                    }
                }
            }
        }
    }

    private fun changeOrderMethod(status:String, orderTimeType:String){

        orderList.clear()
        orderAdapter.notifyDataSetChanged()
        lastResult = null
        isReachLast = false

        loadingDialog.show(childFragmentManager,"Show")
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
            getOrdersByTags(status,orderTimeType)
        }
    }




    private suspend fun getOrdersByTags(status:String, orderTimeType:String){

        val resultList :ArrayList<OrderModel> = ArrayList()
        resultList.clear()

        var query: Query = if (lastResult == null){

            firebaseFirestore
                .collection("ORDERS")
                .whereEqualTo("ID_Of_SELLER",user!!.uid)
                .whereEqualTo("status",status)
                .orderBy(orderTimeType, Query.Direction.DESCENDING)
        }else{
            firebaseFirestore.collection("ORDERS")
                .whereEqualTo("ID_Of_SELLER",user!!.uid)
                .whereEqualTo("status",status)
                .orderBy(orderTimeType,Query.Direction.DESCENDING)
                .startAfter(times)
        }



        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){
                for (item in allDocumentSnapshot){

                    val orderId = item.id
                    val imageUrl =  item.getString("productThumbnail").toString()
                    val productName =  item.getString("productTitle").toString()
                    val statusString =  item.getString("status").toString()
                    val orderedQty =  item.getLong("ordered_Qty")!!
                    val price =  item.getLong("PRICE_TOTAL")!!
                    val buyerId =  item.getString("ID_Of_BUYER").toString()
                    val already_paid:Boolean = item.getBoolean("already_paid")!!
                    val orderTime: Date = item.getTimestamp("Time_ordered")!!.toDate()
                    val acceptedTime= item.getTimestamp("Time_accepted")?.toDate()
                    val packedTime= item.getTimestamp("Time_packed")?.toDate()
                    val shippedTime= item.getTimestamp("Time_shipped")?.toDate()
                    val deliveredTime= item.getTimestamp("Time_delivered")?.toDate()
                    val returnedTime= item.getTimestamp("Time_returned")?.toDate()
                    val canceledTime= item.getTimestamp("Time_canceled")?.toDate()
                    val address:MutableMap<String,Any> = item.get("address") as MutableMap<String,Any>

                    resultList.add(OrderModel(orderId,imageUrl,productName,statusString, buyerId,orderedQty,
                        price,already_paid,address,orderTime,acceptedTime,packedTime,
                        shippedTime,deliveredTime,returnedTime,canceledTime))

                }

                isReachLast = allDocumentSnapshot.size < 10

            }else{
                isReachLast = true

            }

            orderList.addAll(resultList)


            if (orderList.isEmpty()){
                binding.emptyContainer.visibility = visible
                binding.orderRecycler.visibility = gone
            }else{
                binding.emptyContainer.visibility = gone
                binding.orderRecycler.visibility = visible

                orderAdapter.list = orderList

                if (lastResult == null ){
                    orderAdapter.notifyItemRangeInserted(0,resultList.size)
                }else{
                    orderAdapter.notifyItemRangeInserted(orderList.size-1,resultList.size)
                }

                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp(orderTimeType)!!

            }
            binding.progressBar2.visibility = gone

            loadingDialog.dismiss()
        }.addOnFailureListener {
            Log.e("Load orders","${it.message}")
            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone
        }.await()
    }


    private suspend fun getCanceledOrders(){

        val resultList :ArrayList<OrderModel> = ArrayList()
        resultList.clear()

        val query: Query = firebaseFirestore
                .collection("ORDERS")
                .whereEqualTo("ID_Of_SELLER",user!!.uid)
                .whereEqualTo("is_order_canceled",true)
                .orderBy("Time_canceled", Query.Direction.DESCENDING)



        query.limit(15L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){
                for (item in allDocumentSnapshot){

                    val orderId = item.id
                    val imageUrl =  item.getString("productThumbnail").toString()
                    val productName =  item.getString("productTitle").toString()
                    val statusString =  item.getString("status").toString()
                    val orderedQty =  item.getLong("ordered_Qty")!!
                    val price =  item.getLong("PRICE_TOTAL")!!
                    val buyerId =  item.getString("ID_Of_BUYER").toString()
                    val already_paid:Boolean = item.getBoolean("already_paid")!!
                    val orderTime: Date = item.getTimestamp("Time_ordered")!!.toDate()
                    val acceptedTime= item.getTimestamp("Time_accepted")?.toDate()
                    val packedTime= item.getTimestamp("Time_packed")?.toDate()
                    val shippedTime= item.getTimestamp("Time_shipped")?.toDate()
                    val deliveredTime= item.getTimestamp("Time_delivered")?.toDate()
                    val returnedTime= item.getTimestamp("Time_returned")?.toDate()
                    val canceledTime= item.getTimestamp("Time_canceled")?.toDate()
                    val address:MutableMap<String,Any> = item.get("address") as MutableMap<String,Any>

                    resultList.add(OrderModel(orderId,imageUrl,productName,statusString, buyerId,orderedQty,
                        price,already_paid,address,orderTime,acceptedTime,packedTime,
                        shippedTime,deliveredTime,returnedTime,canceledTime))
                }

            }else{
                isReachLast = true

            }
            orderList.addAll(resultList)

            if (orderList.isEmpty()){
                binding.emptyContainer.visibility = visible
                binding.orderRecycler.visibility = gone
            }else{
                binding.emptyContainer.visibility = gone
                binding.orderRecycler.visibility = visible

                orderAdapter.list = orderList
                orderAdapter.notifyDataSetChanged()
            }
            binding.progressBar2.visibility = gone

            loadingDialog.dismiss()
        }.addOnFailureListener {
            Log.e("Load orders","${it.message}")
            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone
        }.await()
    }

    override fun acceptClickListener(position: Int) {
//        TODO("Not yet implemented")
    }

    override fun rejectClickListener(position: Int) {
//        TODO("Not yet implemented")
    }

    override fun shipClickListener(position: Int) {
//        TODO("Not yet implemented")
    }

    override fun cancelClickListener(position: Int) {
//        TODO("Not yet implemented")
    }


}