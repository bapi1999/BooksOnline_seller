package com.sbdevs.booksonlineseller.fragments.product

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.viewpager2.widget.ViewPager2
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.activities.EditProductActivity
import com.sbdevs.booksonlineseller.adapters.ProductImgAdapter
import com.sbdevs.booksonlineseller.adapters.ProductReviewAdapter
import com.sbdevs.booksonlineseller.models.ProductReviewModel
import com.sbdevs.booksonlineseller.databinding.FragmentProductDetailsBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import com.sbdevs.booksonlineseller.fragments.order.OtherOrdersFragment
import com.squareup.picasso.Picasso
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class ProductDetailsFragment : Fragment(),ProductImgAdapter.MyOnItemClickListener {
    private var _binding:FragmentProductDetailsBinding? =null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private val storage = Firebase.storage


    private val gone = View.GONE
    private val visible = View.VISIBLE
    private var reviewList: List<ProductReviewModel> = ArrayList()
    private lateinit var reviewAdapter: ProductReviewAdapter

    private lateinit var productImgViewPager: ViewPager2

    private var productImgList: ArrayList<String> = ArrayList()
    private lateinit var productId: String

    var dbStockQty = 0

    private val loadingDialog = LoadingDialog()

    private lateinit var enterStockQty:TextInputLayout
    private lateinit var productThumbnail:ImageView
    private lateinit var thumbnailUrl:String
    private lateinit var productDeleteWarningDialog : Dialog
    private lateinit var stockContainer:LinearLayout


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentProductDetailsBinding.inflate(inflater, container, false)
        loadingDialog.show(childFragmentManager,"show")

        productImgViewPager = binding.lay1.productImgViewPager
        productThumbnail = binding.lay1.productThumbnail

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

        enterStockQty = binding.lay4.enterStockEditText

        stockContainer = binding.lay4.stockContainer




        productDeleteWarningDialog = Dialog(requireContext())
        productDeleteWarningDialog.setContentView(R.layout.le_product_delete_warning_dialog)
        productDeleteWarningDialog.setCancelable(true)
        productDeleteWarningDialog.window!!.setBackgroundDrawable(AppCompatResources.getDrawable(requireActivity().applicationContext, R.drawable.s_shape_bg_2))
        productDeleteWarningDialog.window!!.setLayout(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT)
        dialogFunction(productDeleteWarningDialog)



        return binding.root
    }

    private fun dialogFunction(dialog: Dialog){
        val cancelBtn:TextView = dialog.findViewById(R.id.no_btn)
        val yesBtn:TextView = dialog.findViewById(R.id.yes_btn)


        cancelBtn.setOnClickListener {
            dialog.dismiss()
        }

        yesBtn.setOnClickListener {

            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                deleteProduct()
                dialog.dismiss()
                loadingDialog.show(childFragmentManager,"shoe")
            }

        }
    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lay4.updateStockBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager,"show")
            updateStock()
        }

        binding.lay4.hideProductBtn.setOnClickListener {
            hideOrShowProduct(true)
        }
        binding.lay4.unHideProductBtn.setOnClickListener {
            hideOrShowProduct(false)
        }

        binding.editProductBtn.setOnClickListener {
            val editIntent = Intent(requireContext(),EditProductActivity::class.java)
            editIntent.putExtra("productId",productId)
            startActivity(editIntent)
        }

        binding.layRating.viewAllButton.setOnClickListener {

            val productReviewFragment = ProductReviewFragment()
            val args = Bundle()
            args.putString("productID",productId)
            productReviewFragment.arguments = args

            val fragmentManager =
                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(R.id.product_host_fragment,productReviewFragment)
                    addToBackStack("review")
                }
        }

        binding.lay1.changeProductImageBtn.setOnClickListener {
            val changeProductImageFragment = ChangeProductImageFragment()


            val args = Bundle()
            args.putStringArrayList("image_list",productImgList)
            args.putString("thumbUrl",thumbnailUrl)

            changeProductImageFragment.arguments = args


            val fragmentManager =
                parentFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(R.id.product_host_fragment,changeProductImageFragment)
                    addToBackStack("change_image")
                }
        }


        binding.deleteProductBtn.setOnClickListener {
            productDeleteWarningDialog.show()
        }

    }

    @Suppress("UNCHECKED_CAST")
    private fun getProductData(productId: String) = CoroutineScope(Dispatchers.IO).launch {
        val lay1 = binding.lay1
        val lay2 = binding.lay2
        val lay3 = binding.lay3
        val lay4 = binding.lay4
        val lay5 = binding.lay5
        val lay6 = binding.lay6
        val layR = binding.layRating
        firebaseFirestore.collection("PRODUCTS").document(productId).get()
            .addOnSuccessListener {
                if (it.exists()) {
                    binding.deletedProductContainer.visibility = gone
                    binding.productDetailScroll.visibility = visible
                    binding.linearLayout7.visibility = visible

                    var categoryString = ""
                    var tagsString = ""

                    val productName = it.getString("book_title")!!

                    val priceOriginal = it.getLong("price_original")!!.toLong()
                    val priceSelling = it.getLong("price_selling")!!.toLong()

                    val avgRating = it.getString("rating_avg")!!
                    val totalRating: Int = it.getLong("rating_total")!!.toInt()
                    val stock = it.getLong("in_stock_quantity")!!
                    val description = it.getString("book_details")!!
                    val categoryList: ArrayList<String> = it.get("categories") as ArrayList<String>
                    val tagList: ArrayList<String> = it.get("tags") as ArrayList<String>
                    thumbnailUrl = it.getString("product_thumbnail").toString()
                    val hideProduct: Boolean = it.getBoolean("hide_this_product")!!

                    val bookWriter = it.getString("book_writer")
                    val bookPublisherName = it.getString("book_publisher")
                    val bookLanguage = it.getString("book_language")
                    val bookType = it.getString("book_type")!!
                    val bookPrintDate = it.getLong("book_printed_ON")
                    val bookCondition = it.getString("book_condition")
                    val bookPageCount = it.getString("book_pageCount")
                    val isbnNumber = it.getString("book_ISBN")
                    val bookDimension = it.getString("book_dimension")
                    val dimensionArray: List<String> = bookDimension!!.split("x")
                    productImgList = it.get("productImage_List") as ArrayList<String>

                    dbStockQty = stock.toInt()

                    for (categories in categoryList) {
                        categoryString += "$categories,  "
                    }

                    for (tag in tagList) {
                        tagsString += "#$tag  "
                    }



                    lay4.stockQuantity.text = stock.toString()


                    val adapter = ProductImgAdapter(productImgList,this@ProductDetailsFragment)

                    productImgViewPager.adapter = adapter
                    binding.lay1.dotsIndicator.setViewPager2(productImgViewPager)

                    lay2.productName.text = productName

                    if (hideProduct) {
                        binding.lay4.productHideStatus.text = "Product is hidden to user"
                        lay4.unHideProductBtn.visibility = visible
                        lay4.hideProductBtn.visibility = gone
                    } else {
                        binding.lay4.productHideStatus.text =
                            "this product is visible to user" // getString(R.string.product_is_visible_to_user)
                        lay4.unHideProductBtn.visibility = gone
                        lay4.hideProductBtn.visibility = visible
                    }


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

                    //Glide.with(requireContext()).load(url).placeholder(R.drawable.as_square_placeholder).into(productThumbnail)
                    Picasso.get().load(thumbnailUrl)
                        .placeholder(R.drawable.as_square_placeholder)
                        .into(productThumbnail)

                    lay2.productState.text = bookType
                    lay2.miniProductRating.text = avgRating
                    lay2.miniTotalNumberOfRatings.text = "(${totalRating} ratings)"

                    when {
                        stock > 5 -> {
                            stockContainer.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.indigo_700)
                        }
                        stock in 1..5 -> {
                            stockContainer.backgroundTintList =AppCompatResources.getColorStateList(requireContext(),R.color.amber_900)
                        }
                        stock == 0L -> {
                            stockContainer.backgroundTintList =AppCompatResources.getColorStateList(requireContext(),R.color.red_700)
                        }
                        else -> {
                            stockContainer.backgroundTintList =AppCompatResources.getColorStateList(requireContext(),R.color.indigo_700)

                        }
                    }

// todo layout 2

                    lay3.productDetailsText.text = description

                    //todo layout 3
                    lay5.writerName.text = bookWriter
                    lay5.publisherName.text = bookPublisherName
                    lay5.bookLanguage.text = bookLanguage


                    if (bookPrintDate == 0L) {
                        lay5.printDate.text = "Not available"
                    } else {
                        lay5.printDate.text = bookPrintDate.toString()
                    }

                    lay5.bookCondition.text = bookCondition
                    lay5.pageCount.text = bookPageCount
                    lay5.isbnNumber.text = isbnNumber
                    lay5.bookDimension.text = bookDimension

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
                        val maxProgress: Int = totalRating //it.getLong("rating_total")!!.toInt()
                        progressBar.max = maxProgress
                        val perccing: String = it.get("rating_Star_" + (5 - x)).toString()
                        val progress = Integer.valueOf(perccing)
                        progressBar.progress = progress
                    }
                }else{
                    binding.deletedProductContainer.visibility = visible
                    binding.productDetailScroll.visibility = gone
                    binding.linearLayout7.visibility = gone
                }

            }.addOnFailureListener {
                Log.e("Product","${it.message}",it.cause)
            }.await()
    }

    private fun getReview(productID: String) = CoroutineScope(Dispatchers.IO).launch {
        firebaseFirestore.collection("PRODUCTS")
            .document(productID)
            .collection("PRODUCT_REVIEW")
            .whereEqualTo("is_review_available",true)
            .orderBy("rating",Query.Direction.DESCENDING)
            .orderBy("review_Date", Query.Direction.DESCENDING).limit(5)
            .get().addOnSuccessListener {
                reviewList = it.toObjects(ProductReviewModel::class.java)
                reviewAdapter.list = reviewList
                reviewAdapter.notifyDataSetChanged()


            }.addOnFailureListener{
                Log.e("Review","${it.message}",it.cause)
            }.await()

    }



//    override fun onItemClicked(position: Int, url: String) {
//        val action = ProductDetailsFragmentDirections.actionProductDetailsFragmentToProductImageZoomFragment(url)
//        findNavController().navigate(action)
//    }

    private fun checkEnterStockQuantity(): Boolean {
        val input: String = enterStockQty.editText?.text.toString()
        return if (input.isEmpty()) {
            enterStockQty.isErrorEnabled = true
            enterStockQty.error = "Field can't be empty"
            false
        } else {
            enterStockQty.isErrorEnabled = false
            enterStockQty.error = null
            true
        }
    }

    private fun updateStock(){
        if (!checkEnterStockQuantity()){
            loadingDialog.dismiss()
            return
        }else{
            val stock = enterStockQty.editText?.text.toString().toLong()

            when {
                stock > 5 -> {
                    stockContainer.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.indigo_700)
                }
                stock in 1..5 -> {
                    stockContainer.backgroundTintList =AppCompatResources.getColorStateList(requireContext(),R.color.amber_900)
                }
                stock == 0L -> {
                    stockContainer.backgroundTintList =AppCompatResources.getColorStateList(requireContext(),R.color.red_700)
                }
                else -> {
                    stockContainer.backgroundTintList =AppCompatResources.getColorStateList(requireContext(),R.color.indigo_700)

                }
            }


            val updateStockMap:MutableMap<String,Any> = HashMap()
            updateStockMap["in_stock_quantity"] = stock.toLong()

            firebaseFirestore.collection("PRODUCTS").document(productId)
                .update(updateStockMap).addOnSuccessListener {
                    Log.i("Update Stock","Successful")
                    loadingDialog.dismiss()
                }.addOnFailureListener {
                    Log.e("Update Stock","${it.message}")
                    Toast.makeText(requireContext(),"Failed to update stock",Toast.LENGTH_LONG).show()
                    loadingDialog.dismiss()
                }
        }

    }


    private fun hideOrShowProduct(condition:Boolean){
        val map:MutableMap<String,Any> = HashMap()
        map["hide_this_product"] = condition
        firebaseFirestore.collection("PRODUCTS").document(productId)
            .update(map).addOnSuccessListener {}

    }

    private suspend fun deleteProduct(){
        for (item in productImgList){
            val ref: StorageReference = storage.getReferenceFromUrl(item)
            ref.delete().addOnSuccessListener {
                Log.w("delete","$item deleted")
            }.addOnFailureListener {
                Log.w("delete","failed")
            }.await()
        }

        val thumbRef:StorageReference = storage.getReferenceFromUrl(thumbnailUrl)
        thumbRef.delete().await()

        firebaseFirestore.collection("PRODUCTS").document(productId).delete()
            .addOnSuccessListener {

                binding.deletedProductContainer.visibility = visible
                binding.productDetailScroll.visibility = gone
                binding.linearLayout7.visibility = gone
                loadingDialog.dismiss()

            }
            .addOnFailureListener {
                Toast.makeText(requireContext(),"Failed to delete: ${it.message}",Toast.LENGTH_LONG).show()
                loadingDialog.dismiss()
            }
            .await()


    }

    override fun onItemClick(position: Int, url: String) {
        val productImageFragment = ProductImageZoomFragment()
        val args = Bundle()
        args.putStringArrayList("image_list",productImgList)

        productImageFragment.arguments = args

        parentFragmentManager.commit {
            setReorderingAllowed(true)
            add(R.id.product_host_fragment,productImageFragment)

            addToBackStack("imageView")
        }
    }

}
