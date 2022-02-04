package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.adapters.DashboardCountAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentDashboardBinding
import com.sbdevs.booksonlineseller.models.DashboardCountModel
import com.sbdevs.booksonlineseller.models.MyProductModel
import com.sbdevs.booksonlineseller.otherclass.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class DashboardFragment : Fragment() {

    private var _binding: FragmentDashboardBinding? = null
    private val binding get() = _binding!!

    val firebaseFirestore = Firebase.firestore
    val user = Firebase.auth.currentUser


    private val viewModel: MainViewModel by activityViewModels()

    private lateinit var newOrder:String

    private lateinit var bottomSheetDialog: BottomSheetDialog

    private var acceptedOrdersNumber:Int = 0
    private var packedOrdersNumber:Int = 0
    private var shippedOrdersNumber:Int = 0
    private val loadingDialog = LoadingDialog()

    private val gone = View.GONE
    private val visible = View.VISIBLE

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDashboardBinding.inflate(inflater, container, false)

        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view: View = layoutInflater.inflate(R.layout.ar_date_filter_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)

        getAllTypesOfOrders()


        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.newOrderData.observe(viewLifecycleOwner,{
            newOrder = it.toString()
            binding.layOrder.newItemCount.text = newOrder
        })


    }

    private fun getAllTypesOfOrders(){

        val ref = firebaseFirestore.collection("USERS").document(user!!.uid)
            .collection("SELLER_DATA").document("SELLER_DATA")
            .collection("ORDERS")

        ref.whereEqualTo("status","accepted")
            .get().addOnSuccessListener{
                val productList = it.toObjects(MyProductModel::class.java)
                acceptedOrdersNumber  = productList.size
                binding.layOrder.acceptItemCount.text = acceptedOrdersNumber.toString()

            }.addOnFailureListener {
                Log.e("Accepted order snapshot","${it.message}")
            }

        ref.whereEqualTo("status","packed")
            .get().addOnSuccessListener{
                val productList = it.toObjects(MyProductModel::class.java)


                packedOrdersNumber  = productList.size
                binding.layOrder.packedItemCount.text = packedOrdersNumber.toString()


            }.addOnFailureListener {
                Log.e("Packed order snapshot","${it.message}")

            }

        ref.whereEqualTo("status","shipped")
            .get().addOnSuccessListener{
                val productList = it.toObjects(MyProductModel::class.java)

                binding.layOrder.progressBar4.visibility = gone
                binding.layOrder.shippedItemCount.visibility = visible

                shippedOrdersNumber  = productList.size
                binding.layOrder.shippedItemCount.text = shippedOrdersNumber.toString()

            }.addOnFailureListener {
                Log.e("shipped order snapshot","${it.message}")

                binding.layOrder.progressBar4.visibility = visible
                binding.layOrder.shippedItemCount.visibility = gone
            }
    }



}