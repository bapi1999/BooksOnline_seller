package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomsheet.BottomSheetDialog
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

    private val loadingDialog = LoadingDialog()

    private lateinit var bottomSheetDialog:BottomSheetDialog

    private var orderList:MutableList<OrderModel> = ArrayList()

    private var orderIdLList:ArrayList<String> = ArrayList()
    private lateinit var orderAdapter:OrderAdapter


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentOrdersBinding.inflate(inflater, container, false)

        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view: View = layoutInflater.inflate(R.layout.ar_order_filter_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        binding.imageButton.setOnClickListener {
            bottomSheetDialog.show()
        }

        getOrders("new")

        val recyclerView = binding.orderRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)

        orderAdapter = OrderAdapter(orderList,orderIdLList)
        recyclerView.adapter = orderAdapter



        return binding.root
    }

    private fun getOrders(status:String){
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



}