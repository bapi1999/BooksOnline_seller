package com.sbdevs.booksonlineseller.fragments

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.appcompat.widget.SearchView
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await

class MyProductFragment : Fragment() {

    private var _binding :FragmentMyProductBinding?=null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var recyclerView:RecyclerView
    private lateinit var productAdapter: MyProductAdapter
    private var productlist:ArrayList<MyProductModel> = ArrayList()

    private lateinit var searchContainer :LinearLayout
    private lateinit var searchView: androidx.appcompat.widget.SearchView
    private lateinit var searchImageIcon:ImageView

    private val loadingDialog = LoadingDialog()
    private var dateModified = Query.Direction.DESCENDING
    private var searchCode:Int = 0
    private var searchBarVisiBle = false

    private val gone = View.GONE
    private val visible = View.VISIBLE

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

        searchContainer = binding.searchContainer
        searchView = binding.searchView
        searchImageIcon = binding.searchImageIcon

        binding.addNewProduct.setOnClickListener {
            val intent = Intent(context,AddProductActivity::class.java)
            startActivity(intent)
        }

        recyclerView = binding.myProductRecycler
        recyclerView.layoutManager = LinearLayoutManager(requireContext())
        productAdapter = MyProductAdapter(productlist)
        recyclerView.adapter = productAdapter
        getMyProduct(dateModified)



        searchImageIcon.setOnClickListener {
            if (searchBarVisiBle){

                searchBarVisiBle = false
                searchContainer.visibility = gone
                searchImageIcon.setImageResource(R.drawable.ic_search_24)
                binding.productTypeContainer.visibility = visible
                changeProductType()
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

            }else{
                searchBarVisiBle = true
                searchContainer.visibility = visible
                searchImageIcon.setImageResource(R.drawable.ic_close_24)
                binding.productTypeContainer.visibility = gone
            }
        }

        binding.backButton.setOnClickListener {
            val fragmentAction = MyProductFragmentDirections.actionMyProductFragmentToProfileMenuFragment2()
            findNavController().navigate(fragmentAction)
        }



        return binding.root
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)


                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    // end scrolling: do what you want here and after calling the function change the value of boolean

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

        })



        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener{
            override fun onQueryTextSubmit(query: String?): Boolean {
                if (!query.isNullOrEmpty()){

                    changeProductType()
                    getProductBySKU(query)
                }

                return false
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return false
            }
        })






        binding.productTypeRadioGroup.setOnCheckedChangeListener { group, checkedId ->

            when (checkedId) {
                R.id.radioButton1 -> {
                    //all = 0
                    searchCode = 0
                    changeProductType()
                    getMyProduct(dateModified)
                }
                R.id.radioButton2 -> {
                    //out of stock = 1
                    searchCode = 1
                    changeProductType()
                    getOutOfStockProduct(dateModified)
                }
                R.id.radioButton3 -> {
                    //low in stock = 2
                    searchCode = 2
                    changeProductType()
                    getLowStockProduct(dateModified)
                }
                R.id.radioButton4 -> {
                    //hidden = 3
                    searchCode = 3
                    changeProductType()
                    getHiddenProduct(dateModified)
                }
            }
        }



    }

    private fun changeProductType(){

        productlist.clear()
        productAdapter.notifyDataSetChanged()
        lastResult = null
        isReachLast = false
        loadingDialog.show(childFragmentManager,"Show")

    }




    private fun getMyProduct(direction:Query.Direction){
        val resultList:ArrayList<MyProductModel> = ArrayList()

        val query:Query = if (lastResult == null){
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

                for (item in allDocumentSnapshot){
                    val documentId = item.id
                    val sku = item.getString("SKU").toString()
                    val productName = item.getString("book_title").toString()
                    val productImgList:ArrayList<String> = item.get("productImage_List") as ArrayList<String>
                    val stockQty: Long = item.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = item.getString("rating_avg")!!
                    val totalRatings: Long = item.getLong("rating_total")!!
                    val priceOriginal = item.getLong("price_original")!!.toLong()
                    val priceSelling = item.getLong("price_selling")!!.toLong()

                    val updateDate = item.getTimestamp("PRODUCT_UPDATE_ON")!!.toDate()

                    resultList.add(MyProductModel(documentId,sku,productName,productImgList,priceSelling,priceOriginal,avgRating,totalRatings,stockQty,updateDate))
                }

                isReachLast = allDocumentSnapshot.size < 10

            }else{
                isReachLast = true

            }




            productlist.addAll(resultList)


            if (productlist.isEmpty()){
                binding.emptyContainer.visibility = visible
                binding.myProductRecycler.visibility = gone
            }else{
                binding.emptyContainer.visibility = gone
                binding.myProductRecycler.visibility = visible

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
            binding.progressBar2.visibility = gone
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")
            binding.progressBar2.visibility = gone
            loadingDialog.dismiss()

        }

    }

    private fun getOutOfStockProduct(direction:Query.Direction){
        val resultList:ArrayList<MyProductModel> = ArrayList()

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

                for (item in allDocumentSnapshot){
                    val documentId = item.id
                    val sku = item.getString("SKU").toString()
                    val productName = item.getString("book_title").toString()
                    val productImgList:ArrayList<String> = item.get("productImage_List") as ArrayList<String>
                    val stockQty: Long = item.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = item.getString("rating_avg")!!
                    val totalRatings: Long = item.getLong("rating_total")!!
                    val priceOriginal = item.getLong("price_original")!!.toLong()
                    val priceSelling = item.getLong("price_selling")!!.toLong()

                    val updateDate = item.getTimestamp("PRODUCT_UPDATE_ON")!!.toDate()

                    resultList.add(MyProductModel(documentId,sku,productName,productImgList,priceSelling,priceOriginal,avgRating,totalRatings,stockQty,updateDate))
                }

                isReachLast = allDocumentSnapshot.size < 10

            }else{
                isReachLast = true

            }


            productlist.addAll(resultList)




            if (productlist.isEmpty()){
                binding.emptyContainer.visibility = visible
                binding.myProductRecycler.visibility = gone
            }else{

                binding.emptyContainer.visibility = gone
                binding.myProductRecycler.visibility = visible

                if (lastResult == null ){

                    productAdapter.list = productlist

                    productAdapter.notifyItemRangeInserted(0,resultList.size)
                }else{
                    productAdapter.notifyItemRangeInserted(productlist.size-1,resultList.size)
                }

                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("PRODUCT_UPDATE_ON")!!


            }

            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")
            loadingDialog.dismiss()

        }

    }

    private fun getLowStockProduct(direction:Query.Direction){
        val resultList:ArrayList<MyProductModel> = ArrayList()

        val query:Query = if (lastResult == null){
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereLessThan("in_stock_quantity",5L)
                .orderBy("in_stock_quantity")
                .orderBy("PRODUCT_UPDATE_ON",direction)
        }else{
            firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereLessThan("in_stock_quantity",5L)
                .orderBy("in_stock_quantity")
                .orderBy("PRODUCT_UPDATE_ON",direction)
                .startAfter(inStockOrder,times)

        }


        query.limit(10L).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents
            if (allDocumentSnapshot.isNotEmpty()){

                for (item in allDocumentSnapshot){
                    val documentId = item.id
                    val sku = item.getString("SKU").toString()
                    val productName = item.getString("book_title").toString()
                    val productImgList:ArrayList<String> = item.get("productImage_List") as ArrayList<String>
                    val stockQty: Long = item.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = item.getString("rating_avg")!!
                    val totalRatings: Long = item.getLong("rating_total")!!
                    val priceOriginal = item.getLong("price_original")!!.toLong()
                    val priceSelling = item.getLong("price_selling")!!.toLong()

                    val updateDate = item.getTimestamp("PRODUCT_UPDATE_ON")!!.toDate()

                    resultList.add(MyProductModel(documentId,sku,productName,productImgList,priceSelling,priceOriginal,avgRating,totalRatings,stockQty,updateDate))
                }

                isReachLast = allDocumentSnapshot.size < 10
            }else{
                isReachLast = true

            }


            productlist.addAll(resultList)


            if (productlist.isEmpty()){
                binding.emptyContainer.visibility = visible
                binding.myProductRecycler.visibility = gone
            }else{

                binding.emptyContainer.visibility = gone
                binding.myProductRecycler.visibility =visible

                productAdapter.list = productlist

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
            binding.progressBar2.visibility = gone
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")

            loadingDialog.dismiss()

        }

    }

    private fun getHiddenProduct(direction:Query.Direction){
        val resultList:ArrayList<MyProductModel> = ArrayList()

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

                for (item in allDocumentSnapshot){
                    val documentId = item.id
                    val sku = item.getString("SKU").toString()
                    val productName = item.getString("book_title").toString()
                    val productImgList:ArrayList<String> = item.get("productImage_List") as ArrayList<String>
                    val stockQty: Long = item.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = item.getString("rating_avg")!!
                    val totalRatings: Long = item.getLong("rating_total")!!
                    val priceOriginal = item.getLong("price_original")!!.toLong()
                    val priceSelling = item.getLong("price_selling")!!.toLong()
                    val updateDate = item.getTimestamp("PRODUCT_UPDATE_ON")!!.toDate()
                    resultList.add(MyProductModel(documentId,sku,productName,productImgList,priceSelling,priceOriginal,avgRating,totalRatings,stockQty,updateDate))
                }

                isReachLast = allDocumentSnapshot.size < 10


            }else{
                isReachLast = true

            }

            productlist.addAll(resultList)

            if (productlist.isEmpty()){
                binding.emptyContainer.visibility = visible
                binding.myProductRecycler.visibility = gone
            }else{
                binding.emptyContainer.visibility =gone
                binding.myProductRecycler.visibility = visible

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
            binding.progressBar2.visibility =gone
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")
            loadingDialog.dismiss()

        }

    }


    private fun getProductBySKU(sku:String){
        val resultList:ArrayList<MyProductModel> = ArrayList()

        val query:Query = firebaseFirestore.collection("PRODUCTS")
                .whereEqualTo("PRODUCT_SELLER_ID",user!!.uid)
                .whereEqualTo("SKU",sku)

        query.get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents


            if (allDocumentSnapshot.isNotEmpty()){

                for (item in allDocumentSnapshot){
                    val documentId = item.id
                    val sku = item.getString("SKU").toString()
                    val productName = item.getString("book_title").toString()
                    val productImgList:ArrayList<String> = item.get("productImage_List") as ArrayList<String>
                    val stockQty: Long = item.getLong("in_stock_quantity")!!.toLong()
                    val avgRating = item.getString("rating_avg")!!
                    val totalRatings: Long = item.getLong("rating_total")!!
                    val priceOriginal = item.getLong("price_original")!!.toLong()
                    val priceSelling = item.getLong("price_selling")!!.toLong()

                    val updateDate = item.getTimestamp("PRODUCT_UPDATE_ON")!!.toDate()

                    resultList.add(MyProductModel(documentId,sku,productName,productImgList,priceSelling,priceOriginal,avgRating,totalRatings,stockQty,updateDate))
                }

                isReachLast = true

            }else{
                isReachLast = true

            }


            productlist.addAll(resultList)


            if (productlist.isEmpty()){
                binding.emptyContainer.visibility = visible
                binding.myProductRecycler.visibility = gone
            }else{

                binding.emptyContainer.visibility =gone
                binding.myProductRecycler.visibility = visible

                if (lastResult == null ){

                    productAdapter.list = productlist

                    productAdapter.notifyItemRangeInserted(0,resultList.size)
                }else{
                    productAdapter.notifyItemRangeInserted(productlist.size-1,resultList.size)
                }



            }

            loadingDialog.dismiss()
            binding.progressBar2.visibility = gone
        }.addOnFailureListener {
            Log.e("MyProducts","${it.message}")
            loadingDialog.dismiss()

        }

    }


}