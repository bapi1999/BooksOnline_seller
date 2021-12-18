package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.view.*
import android.widget.TextView
import androidx.fragment.app.Fragment
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

class OrdersFragment : Fragment() {
    private var _binding:FragmentOrdersBinding? = null
    private val binding get () = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser


    private lateinit var bottomSheetDialog:BottomSheetDialog

    private var orderList:MutableList<OrderModel> = ArrayList()

    private var orderIdLList:ArrayList<String> = ArrayList()
    private lateinit var orderAdapter:OrderAdapter
    private lateinit var statusString: String


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)

        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view: View = layoutInflater.inflate(R.layout.ar_order_filter_bottom_sheet,null)
        bottomSheetDialog.setContentView(view)
        dialogFunction(bottomSheetDialog)

        getOrdersByTags("new")

        val recyclerView = binding.orderRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)

        orderAdapter = OrderAdapter(orderList,orderIdLList)
        recyclerView.adapter = orderAdapter



        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.filterBtn.setOnClickListener {
            bottomSheetDialog.show()
        }
    }

    private fun dialogFunction(dialog: BottomSheetDialog) {

        val applyBtn: TextView = dialog.findViewById(R.id.apply_btn)!!
        val typeChipGroup: ChipGroup = dialog.findViewById(R.id.type_chipGroup)!!

        chipListenerForType(typeChipGroup)

        applyBtn.setOnClickListener {
            getOrdersByTags(statusString)

            dialog.dismiss()
        }


    }

    private fun chipListenerForType(chipGroup: ChipGroup) {
        for (index in 0 until chipGroup.childCount) {
            val chip: Chip = chipGroup.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener { view, isChecked ->

                statusString = if (isChecked) {
                    view.tag.toString()

                } else {
                    "new"
                }

            }
        }

//        chipGroup.setOnCheckedChangeListener { group, checkedId ->
//
//            val chip: Chip = group.findViewById(checkedId) as Chip
//
//            when (checkedId) {
//                R.id.type_chip1 -> {
//                    statusString = "new"
//                }
//                R.id.type_chip2 -> {
//                    statusString = "accepted"
//                }
//                R.id.type_chip3 -> {
//                    statusString = "packed"
//                }
//                R.id.type_chip4 -> {
//                    statusString = "shipped"
//                }
//                R.id.type_chip5 -> {
//                    statusString = "delivered"
//                }
//                R.id.type_chip6 -> {
//                    statusString = "returned"
//                }
//                R.id.type_chip7 -> {
//                    statusString = "canceled"
//                }
//                else -> {
//                    statusString = ""
//                }
//            }
//
//
//        }

    }


    private fun getOrdersByTags(status:String){
        firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("ORDERS")
            .whereEqualTo("status",status)
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

    private fun getOrdersByTagAndDate(status:String,dateString: String){
        firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("ORDERS")
            .whereEqualTo("status",status)
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