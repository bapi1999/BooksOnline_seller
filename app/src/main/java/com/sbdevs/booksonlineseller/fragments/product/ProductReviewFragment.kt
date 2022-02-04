package com.sbdevs.booksonlineseller.fragments.product

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.adapters.ProductReviewAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentProductReviewBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import com.sbdevs.booksonlineseller.models.ProductReviewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await


class ProductReviewFragment : Fragment() {
    private var _binding:FragmentProductReviewBinding ? =null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore

    private lateinit var reviewRecyclerView: RecyclerView
    private var reviewList: MutableList<ProductReviewModel> = ArrayList()
    private var reviewAdapter: ProductReviewAdapter = ProductReviewAdapter(reviewList)

    private var lastResult:DocumentSnapshot ? =null
    private lateinit var times:Timestamp
    private var lastRating:Long = -1L
    private val loadingDialog = LoadingDialog()
    private var isReachLast = false

    private lateinit var reviewContainer:LinearLayout
    private lateinit var emptyText:TextView
    private lateinit var progressView:ProgressBar

    private val gone  = View.GONE
    private val visible = View.VISIBLE



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
       _binding = FragmentProductReviewBinding.inflate(inflater, container, false)

        Log.e("check click","checked")

        val productID = arguments?.getString("productID")
        reviewContainer = binding.linearLayout2
        emptyText =binding.emptyText
        reviewRecyclerView =binding.reviewRecycler
        reviewRecyclerView.layoutManager = LinearLayoutManager(requireContext())
        progressView = binding.progressBar6

        lifecycleScope.launch(Dispatchers.IO) {
            loadingDialog.show(childFragmentManager,"show")
            if (productID != null) {
                getReview(productID)
                Log.e("check Product Id","$productID")
            }
            else{
                reviewContainer.visibility  = gone
                emptyText.visibility = visible
                Log.e("check Product Id","$productID")
            }
        }

        reviewRecyclerView.adapter = reviewAdapter



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        reviewRecyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)


                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {

                    if (isReachLast){
                        Log.w("Query item","Last item is reached already")
                        progressView.visibility = gone

                    }else{
                        progressView.visibility = visible

                        Log.e("last query", "${lastResult.toString()}")

                    }

                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

            }

        })



    }

    private suspend fun getReview(productID: String) {
        var newreviewList:MutableList<ProductReviewModel> = ArrayList()
        newreviewList.clear()

        val query = if (lastResult == null){
            firebaseFirestore.collection("PRODUCTS")
                .document(productID)
                .collection("PRODUCT_REVIEW")
                .whereEqualTo("is_review_available",true)
                .orderBy("rating",Query.Direction.DESCENDING)
                .orderBy("review_Date", Query.Direction.DESCENDING)
        }else{
            firebaseFirestore.collection("PRODUCTS")
                .document(productID)
                .collection("PRODUCT_REVIEW")
                .whereEqualTo("is_review_available",true)
                .orderBy("rating",Query.Direction.DESCENDING)
                .orderBy("review_Date", Query.Direction.DESCENDING)
                .startAfter(lastRating,times)
        }

        query.limit(13)
            .get().addOnSuccessListener {

                val allDocumentSnapshot = it.documents

                if (allDocumentSnapshot.isNotEmpty()){

                    isReachLast = allDocumentSnapshot.size < 10
                    newreviewList = it.toObjects(ProductReviewModel::class.java)

                }else{
                    isReachLast = true

                }


                reviewList.addAll(newreviewList)
                reviewAdapter.list = reviewList

                if (reviewList.size == 0){
                    reviewContainer.visibility  = gone
                    emptyText.visibility = visible
                }else{
                    reviewContainer.visibility  = visible
                    emptyText.visibility = gone

                    if (lastResult == null ){
                        reviewAdapter.notifyItemRangeInserted(0,newreviewList.size)
                    }else{
                        reviewAdapter.notifyItemRangeInserted(reviewList.size-1,newreviewList.size)
                    }


                    val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                    lastResult = lastR
                    lastRating = lastR.getLong("rating")!!.toLong()
                    times = lastR.getTimestamp("review_Date")!!
                }
                loadingDialog.dismiss()
                progressView.visibility = gone
            }.addOnFailureListener{
                Log.e("Review","${it.message}",it.cause)
                loadingDialog.dismiss()
                progressView.visibility = gone
            }.await()

    }

}