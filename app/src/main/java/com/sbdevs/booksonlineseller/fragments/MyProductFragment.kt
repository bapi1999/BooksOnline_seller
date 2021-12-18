package com.sbdevs.booksonlineseller.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R

import com.sbdevs.booksonlineseller.activities.AddProductActivity
import com.sbdevs.booksonlineseller.adapters.MyProductAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentMyProductBinding
import com.sbdevs.booksonlineseller.models.MyProductModel

class MyProductFragment : Fragment() {

    private var _binding :FragmentMyProductBinding?=null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private val user = firebaseAuth.currentUser

    private lateinit var recyclerView:RecyclerView
    private lateinit var productAdapter: MyProductAdapter
    private var productIdsList:ArrayList<String> = ArrayList()
    private var productlist:ArrayList<MyProductModel> = ArrayList()
    private val loadingDialog = LoadingDialog()
    private lateinit var bottomSheetDialog: BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding = FragmentMyProductBinding.inflate(inflater,container, false)
        loadingDialog.show(childFragmentManager,"Show")

        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view: View = layoutInflater.inflate(R.layout.ar_product_filter_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)



        binding.addNewProduct.setOnClickListener {
            val intent = Intent(context,AddProductActivity::class.java)
            startActivity(intent)
        }

//        val myproducts = firbaseFirestore.collection("Products")


        recyclerView = binding.myProductRecycler
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

        firebaseFirestore.collection("PRODUCTS")



        productAdapter = MyProductAdapter(productIdsList,productlist)

        recyclerView.adapter = productAdapter

        getMyProduct()

        return binding.root
    }

    override fun onStart() {
        super.onStart()
        binding.filterBtn.setOnClickListener {
            bottomSheetDialog.show()
        }
    }


    private fun getMyProduct(){
        firebaseFirestore.collection("PRODUCTS").get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents
            for (item in allDocumentSnapshot){
                productIdsList.add(item.id)
            }
            productAdapter.productIdList = productIdsList

            productlist = it.toObjects(MyProductModel::class.java) as ArrayList<MyProductModel>
            productAdapter.list = productlist

            if (productlist.isEmpty()){
                binding.emptyContainer.visibility = View.VISIBLE
                binding.myProductRecycler.visibility = View.GONE
            }else{
                binding.emptyContainer.visibility = View.GONE
                binding.myProductRecycler.visibility = View.VISIBLE
                productAdapter.notifyDataSetChanged()
                loadingDialog.dismiss()
            }
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")
        }

    }


}