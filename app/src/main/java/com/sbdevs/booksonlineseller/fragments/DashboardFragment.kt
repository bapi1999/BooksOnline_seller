package com.sbdevs.booksonlineseller.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.activity.viewModels
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.activities.RegisterActivity
import com.sbdevs.booksonlineseller.adapters.DashboardCountAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentDashboardBinding
import com.sbdevs.booksonlineseller.models.DashboardCountModel
import com.sbdevs.booksonlineseller.models.MyProductModel
import com.sbdevs.booksonlineseller.otherclass.FireStoreData
import com.sbdevs.booksonlineseller.otherclass.MainViewModel

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    public val firebaseFirestore = Firebase.firestore
    val firebaseAuth = Firebase.auth

    private var orderList:MutableList<DashboardCountModel> = ArrayList()

    private val viewModel: MainViewModel by activityViewModels()
    private lateinit var adapter:DashboardCountAdapter
    private lateinit var recyclerView:RecyclerView
    private lateinit var newOrder:String

    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        recyclerView = binding.layOrder.orderDashboardRecycler
        recyclerView.layoutManager = GridLayoutManager(requireContext(),2)


        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view: View = layoutInflater.inflate(R.layout.ar_date_filter_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)










        return binding.root
    }

    override fun onStart() {
        super.onStart()
        orderList.clear()

        binding.lay1.calenderImageButton.setOnClickListener {
            bottomSheetDialog.show()
        }

        viewModel.data.observe(viewLifecycleOwner,{
            newOrder = it.toString()
            orderList.add(DashboardCountModel(R.drawable.ic_add_24,"#FFC107","New",newOrder))
            adapter.notifyDataSetChanged()

        })
        orderList.add(DashboardCountModel(R.drawable.ic_check,"#0AAE59","Accepted","0"))
        orderList.add(DashboardCountModel(R.drawable.ic_add_24,"#0288D1","Packed","0"))
        orderList.add(DashboardCountModel(R.drawable.ic_add_24,"#FF3D00","Shipped","0"))
        adapter = DashboardCountAdapter(orderList)

        recyclerView.adapter = adapter
    }
    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}