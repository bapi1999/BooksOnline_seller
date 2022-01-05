package com.sbdevs.booksonlineseller.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.Toast
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R

import com.sbdevs.booksonlineseller.activities.AddProductActivity
import com.sbdevs.booksonlineseller.adapters.MyProductAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentMyProductBinding
import com.sbdevs.booksonlineseller.models.MyProductModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

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
    private var dateModified = Query.Direction.DESCENDING
    private var searchCode:Int = 0

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding = FragmentMyProductBinding.inflate(inflater,container, false)
        loadingDialog.show(childFragmentManager,"Show")

        bottomSheetDialog = BottomSheetDialog(requireContext(), R.style.CustomBottomSheetDialog)
        val view: View = layoutInflater.inflate(R.layout.ar_product_filter_bottom_sheet, null)
        bottomSheetDialog.setContentView(view)
        dialogFunction(bottomSheetDialog)

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

        getMyProduct(dateModified)

        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.filterBtn.setOnClickListener {
            bottomSheetDialog.show()
        }
    }

    private fun dialogFunction(dialog: BottomSheetDialog) {
        val applyBtn:AppCompatButton = dialog.findViewById(R.id.apply_btn)!!
        val typeChipGroup: ChipGroup = dialog.findViewById(R.id.type_chipGroup)!!
        val dateModifiedChipGroup: ChipGroup = dialog.findViewById(R.id.relevance_chipGroup)!!

        chipListenerForDateModified(dateModifiedChipGroup)
        chipListenerForType(typeChipGroup)

        applyBtn.setOnClickListener {
            Toast.makeText(requireContext(),"clicked",Toast.LENGTH_SHORT).show()

            //getOutOfStockProduct(dateModified)

            when(searchCode){
                0 ->{
                    getMyProduct(dateModified)
                }
                1 -> {
                    //out of stock = 1
                    getOutOfStockProduct(dateModified)
                }
                2 -> {
                    //low in stock = 2
                    getLowStockProduct(dateModified)
                }
                3 -> {
                    //hidden = 3
                    getHiddenProduct(dateModified)
                }
                else -> {
                    //all = 0
                    getMyProduct(dateModified)
                }
            }
            bottomSheetDialog.dismiss()
        }




    }



    private fun chipListenerForDateModified(chipGroup: ChipGroup) {

        chipGroup.setOnCheckedChangeListener { group, checkedId ->

            //val chip: Chip = group.findViewById(checkedId) as Chip

            dateModified = when (checkedId) {
                R.id.relevance_chip1 -> {
                    Query.Direction.DESCENDING

                }
                R.id.relevance_chip2 -> {
                    Query.Direction.ASCENDING
                }
                else -> {
                    Query.Direction.DESCENDING
                }
            }
            binding.textView83.text = dateModified.toString()


        }

    }

    //todo - add click listener to type chip

    private fun chipListenerForType(chipGroup: ChipGroup) {

        chipGroup.setOnCheckedChangeListener { group, checkedId ->

            //val chip: Chip = group.findViewById(checkedId) as Chip

            when (checkedId) {
                R.id.type_chip1 -> {
                    //all = 0
                   searchCode = 0
                }
                R.id.type_chip2 -> {
                   //out of stock = 1
                    searchCode = 1
                }
                R.id.type_chip3 -> {
                    //low in stock = 2
                    searchCode = 2
                }
                R.id.type_chip4 -> {
                    //hidden = 3
                    searchCode = 3
                }
                else -> {
                    //all = 0
                    searchCode = 0
                }
            }


        }

    }





    private fun getMyProduct(direction:Query.Direction){
        firebaseFirestore.collection("PRODUCTS")
            .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
            .whereEqualTo("hide_this_product",false)
            .orderBy("PRODUCT_UPDATE_ON",direction)
            .get().addOnSuccessListener {
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
                loadingDialog.dismiss()
            }else{
                binding.emptyContainer.visibility = View.GONE
                binding.myProductRecycler.visibility = View.VISIBLE
                productAdapter.notifyDataSetChanged()
                loadingDialog.dismiss()
            }
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")

            loadingDialog.dismiss()

        }

    }

    private fun getOutOfStockProduct(direction:Query.Direction){
        firebaseFirestore.collection("PRODUCTS")
            .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
            .whereEqualTo("in_stock_quantity",0L)
            .orderBy("PRODUCT_UPDATE_ON",direction)
            .get().addOnSuccessListener {
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
                    loadingDialog.dismiss()
                }else{
                    binding.emptyContainer.visibility = View.GONE
                    binding.myProductRecycler.visibility = View.VISIBLE
                    productAdapter.notifyDataSetChanged()
                    loadingDialog.dismiss()
                }
            }.addOnFailureListener {
                Log.e("MyProducts","${it.message}")

                loadingDialog.dismiss()

            }

    }

    private fun getLowStockProduct(direction:Query.Direction){
        firebaseFirestore.collection("PRODUCTS")
            .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
            .whereLessThan("in_stock_quantity",5L)
            .whereGreaterThan("in_stock_quantity",0L)
            .orderBy("in_stock_quantity")
            .orderBy("PRODUCT_UPDATE_ON",direction)
            .get().addOnSuccessListener {
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
                    loadingDialog.dismiss()
                }else{
                    binding.emptyContainer.visibility = View.GONE
                    binding.myProductRecycler.visibility = View.VISIBLE
                    productAdapter.notifyDataSetChanged()
                    loadingDialog.dismiss()
                }
            }.addOnFailureListener {
                Log.e("MyProducts","${it.message}")

                loadingDialog.dismiss()

            }

    }

    private fun getHiddenProduct(direction:Query.Direction){
        firebaseFirestore.collection("PRODUCTS")
            .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
            .whereEqualTo("hide_this_product",true)
            .orderBy("PRODUCT_UPDATE_ON",direction)
            .get().addOnSuccessListener {
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
                    loadingDialog.dismiss()
                }else{
                    binding.emptyContainer.visibility = View.GONE
                    binding.myProductRecycler.visibility = View.VISIBLE
                    productAdapter.notifyDataSetChanged()
                    loadingDialog.dismiss()
                }
            }.addOnFailureListener {
                Log.e("MyProducts","${it.message}")

                loadingDialog.dismiss()

            }

    }


}