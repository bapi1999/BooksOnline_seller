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
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R

import com.sbdevs.booksonlineseller.activities.AddProductActivity
import com.sbdevs.booksonlineseller.adapters.MyProductAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentMyProductBinding
import com.sbdevs.booksonlineseller.models.MyProductModel
import com.sbdevs.booksonlineseller.models.ProductReviewModel
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MyProductFragment : Fragment() {

    private var _binding :FragmentMyProductBinding?=null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var recyclerView:RecyclerView
    private lateinit var productAdapter: MyProductAdapter
    private var productIdsList:ArrayList<String> = ArrayList()
    private var productlist:ArrayList<MyProductModel> = ArrayList()
    private val loadingDialog = LoadingDialog()
    private lateinit var bottomSheetDialog: BottomSheetDialog
    private var dateModified = Query.Direction.DESCENDING
    private var searchCode:Int = 0

    private var lastResult:DocumentSnapshot ? = null
    private lateinit var times:Timestamp
    private var inStockOrder:Long = 0L
    private var isReachLast:Boolean = false

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

        recyclerView = binding.myProductRecycler
        recyclerView.layoutManager = LinearLayoutManager(requireContext())

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




        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)


                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    // end scrolling: do what you want here and after calling the function change the value of boolean

                    //binding.textView83.text = isReachLast.toString()

                    if (isReachLast){
                        Log.w("Query item","Last item is reached already")
                        binding.progressBar2.visibility = View.GONE

                    }else{
                        binding.progressBar2.visibility = View.VISIBLE

                        Log.e("last query", "${lastResult.toString()}")
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
                    }

                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

            }

        })




    }

    private fun dialogFunction(dialog: BottomSheetDialog) {
        val applyBtn:AppCompatButton = dialog.findViewById(R.id.apply_btn)!!
        val typeChipGroup: ChipGroup = dialog.findViewById(R.id.type_chipGroup)!!
        val dateModifiedChipGroup: ChipGroup = dialog.findViewById(R.id.relevance_chipGroup)!!

        chipListenerForDateModified(dateModifiedChipGroup)
        chipListenerForType(typeChipGroup)

        applyBtn.setOnClickListener {
//            Toast.makeText(requireContext(),"clicked",Toast.LENGTH_SHORT).show()
            productAdapter.notifyItemRangeRemoved(0,productlist.size)
            lastResult = null
            productlist.clear()
            productIdsList.clear()


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
//            binding.textView83.text = dateModified.toString()


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


        var query:Query = if (lastResult == null){
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("hide_this_product",false)
                .orderBy("PRODUCT_UPDATE_ON",direction)
        }else{
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("hide_this_product",false)
                .orderBy("PRODUCT_UPDATE_ON",direction)
                .startAfter(times)

        }


        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){

                isReachLast = allDocumentSnapshot.size < 10

                for (item in allDocumentSnapshot){
                    productIdsList.add(item.id)
                }

            }else{
                isReachLast = true

            }



            val resultList = it.toObjects(MyProductModel::class.java) as ArrayList<MyProductModel>
            productlist.addAll(resultList)


            if (productlist.isEmpty()){
                binding.emptyContainer.visibility = View.VISIBLE
                binding.myProductRecycler.visibility = View.GONE
                loadingDialog.dismiss()
            }else{
                binding.emptyContainer.visibility = View.GONE
                binding.myProductRecycler.visibility = View.VISIBLE

                productAdapter.productIdList = productIdsList
                productAdapter.list = productlist

                if (lastResult == null ){
                    productAdapter.notifyItemRangeInserted(0,resultList.size)
                }else{
                    productAdapter.notifyItemRangeInserted(productlist.size-1,resultList.size)
                }


                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!

            }

            loadingDialog.dismiss()
            binding.progressBar2.visibility = View.GONE
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")
            binding.progressBar2.visibility = View.GONE
            loadingDialog.dismiss()

        }

    }




    private fun getOutOfStockProduct(direction:Query.Direction){


        val query:Query = if (lastResult == null){
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("in_stock_quantity",0L)
                .orderBy("PRODUCT_UPDATE_ON",direction)
        }else{
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("in_stock_quantity",0L)
                .orderBy("PRODUCT_UPDATE_ON",direction)
                .startAfter(times)

        }


        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents


            if (allDocumentSnapshot.isNotEmpty()){

                isReachLast = allDocumentSnapshot.size < 10 // limit is 7

                for (item in allDocumentSnapshot){
                    productIdsList.add(item.id)

                }

//                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
//                lastResult = lastR
//                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!

            }else{
                isReachLast = true

            }


            productAdapter.productIdList = productIdsList

            val resultList = it.toObjects(MyProductModel::class.java) as ArrayList<MyProductModel>
            productlist.addAll(resultList)
            productAdapter.list = productlist

            if (productlist.isEmpty()){
                binding.emptyContainer.visibility = View.VISIBLE
                binding.myProductRecycler.visibility = View.GONE
                loadingDialog.dismiss()
            }else{

                binding.emptyContainer.visibility = View.GONE
                binding.myProductRecycler.visibility = View.VISIBLE
                if (lastResult == null ){
                    productAdapter.notifyItemRangeInserted(0,resultList.size)
                }else{
                    productAdapter.notifyItemRangeInserted(productlist.size-1,resultList.size)
                }

                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!


            }

            loadingDialog.dismiss()
            binding.progressBar2.visibility = View.GONE
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")
            loadingDialog.dismiss()

        }

    }

    private fun getLowStockProduct(direction:Query.Direction){

        val query:Query = if (lastResult == null){
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereLessThan("in_stock_quantity",5L)
                .whereGreaterThan("in_stock_quantity",0L)
                .orderBy("in_stock_quantity")
                .orderBy("PRODUCT_UPDATE_ON",direction)
        }else{
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereLessThan("in_stock_quantity",5L)
                .whereGreaterThan("in_stock_quantity",0L)
                .orderBy("in_stock_quantity")
                .orderBy("PRODUCT_UPDATE_ON",direction)
                .startAfter(inStockOrder,times)

        }


        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents
            if (allDocumentSnapshot.isNotEmpty()){

                isReachLast = allDocumentSnapshot.size <10 // limit is 7

                for (item in allDocumentSnapshot){
                    productIdsList.add(item.id)

                }
//                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
//                lastResult = lastR
//                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
//                inStockOrder = lastR.getLong("in_stock_quantity")!!
            }else{
                isReachLast = true

            }


            productAdapter.productIdList = productIdsList

            val resultList = it.toObjects(MyProductModel::class.java) as ArrayList<MyProductModel>
            productlist.addAll(resultList)
            productAdapter.list = productlist

            if (productlist.isEmpty()){
                binding.emptyContainer.visibility = View.VISIBLE
                binding.myProductRecycler.visibility = View.GONE
                loadingDialog.dismiss()
            }else{

                binding.emptyContainer.visibility = View.GONE
                binding.myProductRecycler.visibility = View.VISIBLE
                if (lastResult == null ){
                    productAdapter.notifyItemRangeInserted(0,resultList.size)
                }else{
                    productAdapter.notifyItemRangeInserted(productlist.size-1,resultList.size)
                }


                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!
                inStockOrder = lastR.getLong("in_stock_quantity")!!

            }
            loadingDialog.dismiss()
            binding.progressBar2.visibility = View.GONE
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")

            loadingDialog.dismiss()

        }

    }

    private fun getHiddenProduct(direction:Query.Direction){


        val query:Query = if (lastResult == null){
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("hide_this_product",true)
                .orderBy("PRODUCT_UPDATE_ON",direction)
        }else{
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("hide_this_product",true)
                .orderBy("PRODUCT_UPDATE_ON",direction)
                .startAfter(times)

        }

        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents

            if (allDocumentSnapshot.isNotEmpty()){

                isReachLast = allDocumentSnapshot.size <10

                for (item in allDocumentSnapshot){
                    productIdsList.add(item.id)

                }
//                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
//                lastResult = lastR
//                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!

            }else{
                isReachLast = true

            }


            productAdapter.productIdList = productIdsList
            val resultList = it.toObjects(MyProductModel::class.java) as ArrayList<MyProductModel>
            productlist.addAll(resultList)
            productAdapter.list = productlist

            if (productlist.isEmpty()){
                binding.emptyContainer.visibility = View.VISIBLE
                binding.myProductRecycler.visibility = View.GONE
                loadingDialog.dismiss()
            }else{
                binding.emptyContainer.visibility = View.GONE
                binding.myProductRecycler.visibility = View.VISIBLE
                if (lastResult == null ){
                    productAdapter.notifyItemRangeInserted(0,resultList.size)
                }else{
                    productAdapter.notifyItemRangeInserted(productlist.size-1,resultList.size)
                }

                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!

            }
            loadingDialog.dismiss()
            binding.progressBar2.visibility = View.GONE
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")
            loadingDialog.dismiss()

        }

    }


}