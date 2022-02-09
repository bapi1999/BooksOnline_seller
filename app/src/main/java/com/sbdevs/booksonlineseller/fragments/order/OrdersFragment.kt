package com.sbdevs.booksonlineseller.fragments.order

import android.app.Dialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Button
import android.widget.RadioGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.adapters.OrderAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentOrdersBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import com.sbdevs.booksonlineseller.models.OrderModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class OrdersFragment : Fragment(),OrderAdapter.OrderItemClickListener {
    private var _binding:FragmentOrdersBinding? = null
    private val binding get () = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private var paginateOrderList:MutableList<OrderModel> = ArrayList()
    private lateinit var orderAdapter:OrderAdapter
    private lateinit var recyclerView:RecyclerView

    private var newOrderList:MutableList<OrderModel> = ArrayList()
    private var acceptOrderList:MutableList<OrderModel> = ArrayList()
    private var packOrderList:MutableList<OrderModel> = ArrayList()


    private var statusString: String = "new"
    private var orderByString: String = "Time_ordered"

    private val loadingDialog = LoadingDialog()

    private var lastResult: DocumentSnapshot? = null
    private lateinit var times: Timestamp
    private var isReachLast:Boolean = false

    val gone = View.GONE
    val visible = View.VISIBLE

    private var divident = 0
    private var extra = 0
    private var lowerPoint = 0
    private var upperPoint=9
    var st = ""

    lateinit var reasons:Array<String>


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)
        Toast.makeText(requireContext(),"on CreateView",Toast.LENGTH_SHORT).show()

        loadingDialog.show(childFragmentManager,"SHow")
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {

            getNewOrders()
            getAcceptedOrders()
            getPackedOrders()

        }




        recyclerView = binding.orderRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)

        orderAdapter = OrderAdapter(paginateOrderList,this)
        recyclerView.adapter = orderAdapter

        reasons =resources.getStringArray(R.array.order_cancel_reasons)


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        Toast.makeText(requireContext(),"on ViewCreated",Toast.LENGTH_SHORT).show()
        //chipListenerForType(typeChipGroup)

        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)

                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (isReachLast){
                        Log.w("Query item","Last item is reached already")
                        Toast.makeText(requireContext(),"last by True",Toast.LENGTH_SHORT).show()
                        binding.progressBar2.visibility = View.GONE
                    }else{

                        when(statusString){

                            "new"->{
                                if (newOrderList.size == paginateOrderList.size){
                                    Log.e("last query", "reached last")
                                    isReachLast = true
                                    binding.progressBar2.visibility = View.GONE
                                    Toast.makeText(requireContext(),"last by equal",Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    binding.progressBar2.visibility = View.VISIBLE
                                    paginateData(newOrderList,divident,extra)
                                }
                            }

                            "accepted"->{
                                if (acceptOrderList.size == paginateOrderList.size){
                                    Log.e("last query", "reached last")
                                    isReachLast = true
                                    binding.progressBar2.visibility = View.GONE
                                    Toast.makeText(requireContext(),"last by equal",Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    binding.progressBar2.visibility = View.VISIBLE
                                    paginateData(acceptOrderList,divident,extra)
                                }
                            }

                            "packed"->{
                                if (packOrderList.size == paginateOrderList.size){
                                    Log.e("last query", "reached last")
                                    isReachLast = true
                                    binding.progressBar2.visibility = View.GONE
                                    Toast.makeText(requireContext(),"last by equal",Toast.LENGTH_SHORT).show()
                                }
                                else{
                                    binding.progressBar2.visibility = View.VISIBLE
                                    paginateData(packOrderList,divident,extra)
                                }
                            }

                        }

                    }
                }
            }
        })



        binding.orderTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){
                R.id.radioButton1->{
                    statusString = "new"
                    orderTypeChangingMethod(newOrderList)
                }
                R.id.radioButton2->{
                    statusString = "accepted"
                    orderTypeChangingMethod(acceptOrderList)
                }
                R.id.radioButton3->{
                    statusString = "packed"
                    orderTypeChangingMethod(packOrderList)
                }

            }
        }

        binding.otherOrderBtn.setOnClickListener {
            val otherOrdersFragment = OtherOrdersFragment()
            val fragmentManager =
                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(R.id.main_frame_layout,otherOrdersFragment)
                    addToBackStack("imageView")
                }
        }

    }


//    override fun onStart() {
//        super.onStart()
//        Toast.makeText(requireContext(),"on start",Toast.LENGTH_SHORT).show()
//    }
//    override fun onResume() {
//        super.onResume()
//        Toast.makeText(requireContext(),"on resume",Toast.LENGTH_SHORT).show()
//    }
//    override fun onPause() {
//        super.onPause()
//        Toast.makeText(requireContext(),"on pause",Toast.LENGTH_SHORT).show()
//    }
//    override fun onDestroy() {
//        super.onDestroy()
//        Toast.makeText(requireContext(),"on destroy",Toast.LENGTH_SHORT).show()
//    }


    private fun getNewOrders(){

        val resultList :ArrayList<OrderModel> = ArrayList()
        resultList.clear()

        val newDate = Date();
        val cancellation= Date(newDate.time - (1000 * 60 * 60 * 24))



        val query:Query =  firebaseFirestore.collection("ORDERS")
            .whereEqualTo("ID_Of_SELLER",user!!.uid)
            .whereEqualTo("status","new")
            //.whereGreaterThan("Time_ordered","")
            .orderBy("Time_ordered",Query.Direction.ASCENDING)

        query.addSnapshotListener { value, error ->
            error?.let {
                Log.e("Load orders","${it.message}")
                loadingDialog.dismiss()
                binding.progressBar2.visibility = gone
                return@addSnapshotListener
            }

            value?.let {

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



                newOrderList.addAll(resultList)

                binding.radioButton1.text = "New(${newOrderList.size})"

                if (newOrderList.isEmpty()){
                    binding.emptyContainer.visibility = visible
                    binding.orderRecycler.visibility = gone
                }else{
                    binding.emptyContainer.visibility = gone
                    binding.orderRecycler.visibility = visible


                    val totalItem = newOrderList.size
                    divident = totalItem/10
                    extra = totalItem%10

                    st += "$totalItem => div= $divident / mod= $extra \n"

                    if (totalItem <= 10){
                        paginateOrderList.addAll(newOrderList)
                        orderAdapter.list = paginateOrderList
                        orderAdapter.notifyDataSetChanged()
                        isReachLast = true
                    }else{
                        paginateData(newOrderList,divident,extra)
                    }


                    val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                    lastResult = lastR
                    times = lastR.getTimestamp("Time_ordered")!!

                }
                binding.progressBar2.visibility = gone

                loadingDialog.dismiss()
            }


        }
    }


    private fun getAcceptedOrders(){

        val resultList :ArrayList<OrderModel> = ArrayList()
        resultList.clear()

        var query:Query =  firebaseFirestore.collection("ORDERS")
            .whereEqualTo("ID_Of_SELLER",user!!.uid)
            .whereEqualTo("status","accepted")
            .orderBy("Time_accepted",Query.Direction.ASCENDING)

        query.get().addOnSuccessListener {
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

            acceptOrderList.addAll(resultList)
            binding.radioButton2.text = "Accepted(${acceptOrderList.size})"

            binding.progressBar2.visibility = gone

            loadingDialog.dismiss()
        }.addOnFailureListener {
            Log.e("Load accepted orders","${it.message}")
            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone
        }
    }


    private fun getPackedOrders(){

        val resultList :ArrayList<OrderModel> = ArrayList()
        resultList.clear()

        var query:Query =  firebaseFirestore.collection("ORDERS")
            .whereEqualTo("ID_Of_SELLER",user!!.uid)
            .whereEqualTo("status","packed")
            .orderBy("Time_packed",Query.Direction.ASCENDING)

        query.get().addOnSuccessListener {
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

            acceptOrderList.addAll(resultList)
            binding.radioButton2.text = "Accepted(${acceptOrderList.size})"

            binding.progressBar2.visibility = gone

            loadingDialog.dismiss()
        }.addOnFailureListener {
            Log.e("Load accepted orders","${it.message}")
            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone
        }
    }

    private fun paginateData( orderList:MutableList<OrderModel>,divCount:Int,extra:Int){

        val kh: MutableList<OrderModel> = ArrayList()

        if (divCount == 0 && extra > 0){
            for (i in lowerPoint until (lowerPoint+extra)){
                kh.add(orderList[i])
            }

            paginateOrderList.addAll(kh)
            orderAdapter.list = paginateOrderList
            orderAdapter.notifyItemRangeInserted(lowerPoint,extra)


            st += "method 1 ${orderList.size} => div= $divCount / mod= $extra / listsize = ${kh.size} \n"

        }else if (divCount>0){

            for (i in lowerPoint .. upperPoint){
                kh.add(orderList[i])
            }
            paginateOrderList.addAll(kh)
            orderAdapter.list = paginateOrderList
            orderAdapter.notifyItemRangeInserted(lowerPoint,10)

            lowerPoint+=10
            upperPoint+=10

            divident-=1

            st += "method 2 ${orderList.size} => div= $divCount / mod= $extra / listsize = ${kh.size} \n"

        }else{
            //Toast.makeText(requireContext()," divident gone -ve",Toast.LENGTH_SHORT).show()
            Log.w("divident","divident cant be -ve")
        }

        binding.progressBar2.visibility = gone
        //binding.textView90.text = st


    }



    private fun orderTypeChangingMethod(list: MutableList<OrderModel>){
        //orderList.clear()
        paginateOrderList.clear()
        lowerPoint = 0
        upperPoint = 9
        divident =0
        extra = 0
        orderAdapter.notifyDataSetChanged()
        lastResult = null
        isReachLast = false

        if (list.isEmpty()){
            binding.emptyContainer.visibility = visible
            binding.orderRecycler.visibility = gone
        }else{
            binding.emptyContainer.visibility = gone
            binding.orderRecycler.visibility = visible

            val totalItem = list.size
            divident = totalItem/10
            extra = totalItem%10

            if (totalItem <= 10){
                paginateOrderList.addAll(list)
                orderAdapter.list = paginateOrderList
                orderAdapter.notifyDataSetChanged()
                isReachLast = true

            }else{
                paginateData(list,divident,extra)
            }
        }

        //loadingDialog.show(childFragmentManager,"Show")

    }




    private fun updateOrder(orderId: String, status:String){

        val orderMap:MutableMap<String,Any> = HashMap()
        orderMap["status"] = status
        orderMap["Time_$status"] = FieldValue.serverTimestamp()

        firebaseFirestore
            .collection("ORDERS")
            .document(orderId).update(orderMap)
            .addOnSuccessListener {
                Log.i("$status order","successful")
                loadingDialog.dismiss()
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Log.e("$status order","${it.message}")
                Toast.makeText(requireContext(),"Failed to update order",Toast.LENGTH_LONG).show()
            }

    }

    private fun cancelOrder(orderId: String,reason:String){

        val orderMap:MutableMap<String,Any> = HashMap()
        orderMap["status"] = "canceled"
        orderMap["is_order_canceled"] = true
        orderMap["order_canceled_by"] = "seller"
        orderMap["cancellation_reason"] = reason
        orderMap["Time_canceled"] = FieldValue.serverTimestamp()


        firebaseFirestore
            .collection("ORDERS")
            .document(orderId).update(orderMap)
            .addOnSuccessListener {
                Log.i("canceled order","successful")
                loadingDialog.dismiss()
            }
            .addOnFailureListener {
                loadingDialog.dismiss()
                Log.e("canceled order","${it.message}")
            }

    }


    override fun acceptClickListener(position: Int) {

        loadingDialog.show(childFragmentManager,"show")
        val orderId = paginateOrderList[position].orderId
        Toast.makeText(requireContext(),"size${paginateOrderList.size} / $orderId",Toast.LENGTH_LONG).show()
        updateOrder(orderId,"accepted")
        orderAdapter.notifyItemRemoved(position)


    }

    override fun rejectClickListener(position: Int) {
        val orderId = paginateOrderList[position].orderId
        dialogOption(orderId)
        Toast.makeText(requireContext(),"size${paginateOrderList.size} / $orderId",Toast.LENGTH_LONG).show()
    }

    override fun shipClickListener(position: Int) {
        loadingDialog.show(childFragmentManager,"show")
        val orderId = paginateOrderList[position].orderId
        updateOrder(orderId,"shipped")
        orderAdapter.notifyItemRemoved(position)
    }

    override fun cancelClickListener(position: Int) {
        val orderId = paginateOrderList[position].orderId
        dialogOption(orderId)
    }

    private fun dialogOption(orderId: String){
        val qtyDialog = Dialog(requireContext())
        qtyDialog.window!!.setBackgroundDrawable(
            ColorDrawable(Color.argb(58, 0, 0, 0))
            //AppCompatResources.getDrawable(requireContext(), R.drawable.s_shape_bg_2)
        )
        qtyDialog.requestWindowFeature(1)

        qtyDialog.setContentView(R.layout.le_order_cancellation_lay_1)
        qtyDialog.setCancelable(true)
        qtyDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)

        qtyDialog.show()

        val radioGroup: RadioGroup = qtyDialog.findViewById(R.id.reason_radio_group)
        val submitBtn: Button = qtyDialog.findViewById(R.id.submit_btn)
        val txt: TextView = qtyDialog.findViewById(R.id.textView91)

        var reason = reasons[0]

        radioGroup.setOnCheckedChangeListener { group, checkedId ->
            when(checkedId){

                R.id.reason1->{
                    reason = reasons[0]
                }
                R.id.reason2->{
                    reason = reasons[1]
                }
                R.id.reason3->{
                    reason = reasons[2]
                }
                R.id.reason4->{
                    reason = reasons[3]
                }
                R.id.reason5->{
                    reason = reasons[4]
                }

            }
        }

        submitBtn.setOnClickListener {
            txt.text = reason
//            cancelOrder(orderId,reason)
            qtyDialog.dismiss()
        }

    }

    private fun others(){

    }


}