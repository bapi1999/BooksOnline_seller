package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.adapters.NotificationAdapter
import com.sbdevs.booksonlineseller.adapters.OrderAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentOrdersBinding
import com.sbdevs.booksonlineseller.models.NotificationModel
import com.sbdevs.booksonlineseller.models.OrderModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.tasks.await

class OrdersFragment : Fragment() {
    private var _binding:FragmentOrdersBinding? = null
    private val binding get () = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private var orderList:MutableList<OrderModel> = ArrayList()

    private var orderIdLList:ArrayList<String> = ArrayList()
    private lateinit var orderAdapter:OrderAdapter
    private var statusString: String = "new"
    private val args:OrdersFragmentArgs by navArgs()
    private val loadingDialog = LoadingDialog()
    private lateinit var typeChipGroup: ChipGroup

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)

        typeChipGroup = binding.typeChipGroup



        val orderStatus = args.orderStatus
        loadingDialog.show(childFragmentManager,"SHow")
        viewLifecycleOwner.lifecycleScope.launch(Dispatchers.Main) {

            if (orderStatus != null){
                statusString = orderStatus
                getOrdersByTags(statusString)
                setSelectedChip(orderStatus)
            }else{
                getOrdersByTags(statusString)
            }

        }




        val recyclerView = binding.orderRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)

        orderAdapter = OrderAdapter(orderList,orderIdLList)
        recyclerView.adapter = orderAdapter



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        chipListenerForType(typeChipGroup)
    }


    private fun chipListenerForType(chipGroup: ChipGroup) {

        chipGroup.setOnCheckedChangeListener { group, checkedId ->

            //val chip: Chip = group.findViewById(checkedId) as Chip

            when (checkedId) {
                R.id.type_chip1 -> {
                    statusString = "new"
                    loadingDialog.show(childFragmentManager,"Show")
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        getOrdersByTags(statusString)
                    }
                }
                R.id.type_chip2 -> {
                    statusString = "accepted"
                    loadingDialog.show(childFragmentManager,"Show")
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        getOrdersByTags(statusString)
                    }
                }
                R.id.type_chip3 -> {
                    statusString = "packed"
                    loadingDialog.show(childFragmentManager,"Show")
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        getOrdersByTags(statusString)
                    }
                }
                R.id.type_chip4 -> {
                    statusString = "shipped"
                    loadingDialog.show(childFragmentManager,"Show")
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        getOrdersByTags(statusString)
                    }
                }
                R.id.type_chip5 -> {
                    statusString = "delivered"
                    loadingDialog.show(childFragmentManager,"Show")
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        getOrdersByTags(statusString)
                    }
                }
                R.id.type_chip6 -> {
                    statusString = "returned"
                    loadingDialog.show(childFragmentManager,"Show")
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        getOrdersByTags(statusString)
                    }
                }
                R.id.type_chip7 -> {
                    statusString = "canceled"
                    loadingDialog.show(childFragmentManager,"Show")
                    viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                        getOrdersByTags(statusString)
                    }
                }
                else -> {
                    statusString = "new"

                }
            }


        }

    }

    private fun setSelectedChip( stype:String){
        when(stype){
            "new"->{
//                typeChipGroup.check(R.id.type_chip1)
                //
            }
            "accepted"->{
                typeChipGroup.check(R.id.type_chip2)
            }
            "packed"->{
                typeChipGroup.check(R.id.type_chip3)
            }
            "shipped"->{
                typeChipGroup.check(R.id.type_chip4)
            }

        }
    }


    private suspend fun getOrdersByTags(status:String){
        firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("ORDERS")
            .whereEqualTo("status",status)
            .orderBy("Time_ordered")
            .get().addOnSuccessListener {
                val allDocumentSnapshot = it.documents
                for (item in allDocumentSnapshot){
                    orderIdLList.add(item.id)

                }

                orderAdapter.orderIdList = orderIdLList
                orderList = it.toObjects(OrderModel::class.java)
                if (orderList.isEmpty()){
                    binding.emptyContainer.visibility = View.VISIBLE
                    binding.orderRecycler.visibility = View.GONE
                }else{
                    binding.emptyContainer.visibility = View.GONE
                    binding.orderRecycler.visibility = View.VISIBLE

                    orderAdapter.list = orderList
                    orderAdapter.notifyDataSetChanged()
                }
                loadingDialog.dismiss()
            }.addOnFailureListener {
                Log.e("Load orders","${it.message}")
                loadingDialog.dismiss()
            }.await()
    }


    private fun getOrdersByDate(dateString: String){
        firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("ORDERS")
            .whereEqualTo("order_day",dateString)
            .orderBy("orderTime")
            .get().addOnSuccessListener {
                val allDocumentSnapshot = it.documents
                for (item in allDocumentSnapshot){
                    orderIdLList.add(item.id)

                }

                orderAdapter.orderIdList = orderIdLList
                orderList = it.toObjects(OrderModel::class.java)
                if (orderList.isEmpty()){
                    binding.emptyContainer.visibility = View.VISIBLE
                    binding.orderRecycler.visibility = View.GONE
                }else{
                    binding.emptyContainer.visibility = View.GONE
                    binding.orderRecycler.visibility = View.VISIBLE

                    orderAdapter.list = orderList
                    orderAdapter.notifyDataSetChanged()
                }
            }.addOnFailureListener {  }
    }


}