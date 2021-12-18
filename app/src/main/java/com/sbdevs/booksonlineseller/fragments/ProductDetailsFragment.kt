package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.adapters.ProductImgAdapter
import com.sbdevs.booksonlineseller.adapters.ProductReviewAdapter
import com.sbdevs.booksonlineseller.models.ProductReviewModel
import com.sbdevs.booksonlineseller.databinding.FragmentProductDetailsBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductDetailsFragment : Fragment(),ProductImgAdapter.MyOnItemClickListener {
    private var _binding:FragmentProductDetailsBinding? =null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private val user = firebaseAuth.currentUser


    private val gone = View.GONE
    private val visible = View.VISIBLE
    private var reviewList: List<ProductReviewModel> = ArrayList()
    private lateinit var reviewAdapter: ProductReviewAdapter

    private lateinit var productImgViewPager: ViewPager2

    private var productImgList: ArrayList<String> = ArrayList()
    private lateinit var productId: String

    var dbStockQty = 0

    private val loadingDialog = LoadingDialog()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        loadingDialog.show(childFragmentManager,"show")

        productImgViewPager = binding.lay1.productImgViewPager

        val intent = requireActivity().intent
        productId = intent.getStringExtra("productId").toString().trim()

        lifecycleScope.launch{


            getProductData(productId)

            withContext(Dispatchers.IO) {
                getReview(productId)
            }
            withContext(Dispatchers.Main) {
                loadingDialog.dismiss()
            }
        }

        val layoutManager = LinearLayoutManager(context)
        val reviewRecyclerView = binding.layRating.reviewRecycler
        reviewRecyclerView.layoutManager = layoutManager

        reviewAdapter = ProductReviewAdapter(reviewList)
        reviewRecyclerView.adapter = reviewAdapter


        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.layRating.viewAllButton.setOnClickListener {
            val action =
                ProductDetailsFragmentDirections.actionProductDetailsFragmentToProductReviewFragment()
            findNavController().navigate(action)
        }
    }

    @Suppress("UNCHECKED_CAST")
    private fun getProductData(productId: String) = CoroutineScope(Dispatchers.IO).launch {
        val lay1 = binding.lay1
        val lay2 = binding.lay2
        val lay3 = binding.lay3
        val lay5 = binding.lay5
        val lay6 = binding.lay6
        val layR = binding.layRating
        firebaseFirestore.collection("PRODUCTS").document(productId).get()
            .addOnSuccessListener {
                if (it.exists()) {
//                    lifecycleScope.launch(Dispatchers.IO) {
//
//                    }
                    var categoryString = ""
                    var tagsString = ""

                    val productName = it.getString("book_title")!!

                    val priceOriginal = it.getLong("price_original")!!.toLong()
                    val priceSelling = it.getLong("price_selling")!!.toLong()

                    val avgRating = it.getString("rating_avg")!!
                    val sellerId = it.getString("PRODUCT_SELLER_ID")!!
                    val totalRating: Int = it.getLong("rating_total")!!.toInt()
                    val stock = it.getLong("in_stock_quantity")!!
                    val description = it.getString("book_details")!!
                    val categoryList: ArrayList<String> = it.get("categories") as ArrayList<String>
                    val tagList: ArrayList<String> = it.get("tags") as ArrayList<String>
                    val url = it.get("product_thumbnail").toString().trim()

                    dbStockQty = stock.toInt()

                    for (catrgorys in categoryList) {
                        categoryString += "$catrgorys,  "
                    }

                    for (tag in tagList) {
                        tagsString += "#$tag  "
                    }


                    productImgList = it.get("productImage_List") as ArrayList<String>

                    val adapter = ProductImgAdapter(productImgList,this@ProductDetailsFragment)

                    productImgViewPager.adapter = adapter
                    binding.lay1.dotsIndicator.setViewPager2(productImgViewPager)

                    lay2.productName.text = productName


                    if (priceOriginal == 0L) {
                        lay2.productPrice.text = priceSelling.toString()
                        lay2.strikeThroughPrice.visibility = gone
                        lay2.percentOff.visibility = gone

                    } else {
                        val percent =
                            100 * (priceOriginal.toInt() - priceSelling.toInt()) / (priceOriginal.toInt())

                        lay2.productPrice.text = priceSelling.toString()
                        lay2.strikeThroughPrice.text = priceOriginal.toString()
                        lay2.percentOff.text = "${percent}% off"

                    }


                    lay2.productState.text = it.getString("book_type")!!
                    lay2.miniProductRating.text = avgRating
                    lay2.miniTotalNumberOfRatings.text = "(${totalRating} ratings)"

                    if (stock > 5) {
                        lay2.stockState.visibility = gone
                        lay2.stockQuantity.visibility = gone
                    } else if (stock in 1..5) {
                        lay2.stockState.text = "low"
                        lay2.stockQuantity.text = "only $stock available in stock"
                    } else {
                        lay2.stockState.text = "out of stock"
                        lay2.stockQuantity.visibility = gone

                    }

// todo layout 2

                    lay3.productDetailsText.text = description

                    //todo layout 3
                    lay5.writerName.text = it.getString("book_writer")
                    lay5.publisherName.text = it.getString("book_publisher")
                    lay5.bookLanguage.text = it.getString("book_language")
                    lay5.printDate.text = it.getString("book_printed_ON")
                    lay5.bookCondition.text = it.getString("book_condition")
                    lay5.pageCount.text = it.getString("book_pageCount")
                    lay5.isbnNumber.text = it.getString("book_ISBN")
//                    lay3.bookDimension.text = it.getString("")

                    //todo layout 4
                    lay6.categoryText.text = categoryString
                    lay6.tagsText.text = tagsString

                    //todo rating
                    layR.averageRatingText.text = avgRating
                    layR.totalRating.text = totalRating.toString()

                    for (x in 0..4) {
                        var ratingtxt: TextView =
                            layR.ratingsNumberContainer.getChildAt(x) as TextView
                        ratingtxt.text = (it.get("rating_Star_" + (5 - x)).toString())
                        val progressBar: ProgressBar =
                            layR.ratingBarContainter.getChildAt(x) as ProgressBar
                        val maxProgress: Int = it.getLong("rating_total")!!.toInt()
                        progressBar.max = maxProgress
                        val perccing: String = it.get("rating_Star_" + (5 - x)).toString()
                        val progress = Integer.valueOf(perccing)
                        progressBar.progress = progress
                    }


                }
            }.addOnFailureListener {
                Log.e("Product","${it.message}",it.cause)
            }.await()
    }

    private fun getReview(productID: String) = CoroutineScope(Dispatchers.IO).launch {
        firebaseFirestore.collection("PRODUCTS").document(productID)
            .collection("PRODUCT_REVIEW")
            .orderBy("review_Date", Query.Direction.DESCENDING).limit(7)
            .get().addOnSuccessListener {
                reviewList = it.toObjects(ProductReviewModel::class.java)
                reviewAdapter.list = reviewList
                reviewAdapter.notifyDataSetChanged()


            }.addOnFailureListener{
                Log.e("Review","${it.message}",it.cause)
            }.await()

    }

    override fun onItemClicked(position: Int, url: String) {
        val action = ProductDetailsFragmentDirections.actionProductDetailsFragmentToProductImageZoomFragment(url)
        findNavController().navigate(action)
    }

}
