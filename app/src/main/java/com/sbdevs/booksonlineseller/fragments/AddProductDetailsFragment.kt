package com.sbdevs.booksonlineseller.fragments

import android.annotation.SuppressLint
import android.app.Activity
import android.database.Cursor
import android.net.Uri
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.*
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.Dimension
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentAddProductDetailsBinding
import java.util.*
import kotlin.collections.ArrayList

import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sbdevs.booksonlineseller.adapters.UploadImageAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime


class AddProductDetailsFragment : Fragment(), UploadImageAdapter.MyOnItemClickListener {

    private var _binding: FragmentAddProductDetailsBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private val storage = Firebase.storage
    private val storageReference = storage.reference

    private lateinit var bookName: TextInputLayout
    private lateinit var publisherName: TextInputLayout
    private lateinit var writerName: TextInputLayout
    private lateinit var language: TextInputLayout
    private lateinit var pageCount: TextInputLayout
    private lateinit var isbnNo: TextInputLayout
    private lateinit var description: TextInputLayout
    private lateinit var tagInput: TextInputLayout

    private lateinit var bookPrice: TextInputLayout
    private lateinit var discountPrice: TextInputLayout
    private lateinit var printDateInput: TextInputLayout
    private lateinit var dimensionWidth: TextInputLayout
    private lateinit var dimensionLength: TextInputLayout
    private lateinit var dimensionHeight: TextInputLayout
    private lateinit var myOwnCategoryText: EditText

    private var bookStateRadio: RadioButton? = null
    private var bookConditionRadio: RadioButton? = null
    private lateinit var stockQuantity: EditText


    private var printDateMandatory: Boolean = false
    private val categoryList: MutableList<String> = ArrayList()
    private var tagList: MutableList<String> = ArrayList()

    var thumbUri: Uri? = null
    private var fileUri: Uri? = null
    private var uriList: ArrayList<Uri> = ArrayList()
    var nameList: ArrayList<String> = ArrayList()
    lateinit var adapterUpload: UploadImageAdapter
    private var downloadUriList: MutableList<String> = ArrayList()
    private lateinit var docname: String

    private val loadingDialog = LoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductDetailsBinding.inflate(inflater, container, false)


        val lay1 = binding.lay1
        val lay2 = binding.lay2
        val lay3 = binding.lay3

        bookName = lay1.bookName
        publisherName = lay1.publisherName
        writerName = lay1.writerName
        language = lay1.bookLanguage
        pageCount = lay1.pageCount
        isbnNo = lay1.isbnNumber
        dimensionWidth = lay1.bookDimensionWidth
        dimensionLength = lay1.bookDimensionLength
        dimensionHeight = lay1.bookDimensionHeight

        description = lay1.bookDescription
        bookPrice = lay2.bookPrice
        discountPrice = lay2.discountPrice
        printDateInput = lay2.printDateInput
        myOwnCategoryText = lay3.myOwnCategoryEditText
        tagInput = lay3.editTags
        stockQuantity = lay2.stockQuantity


        val productThumbnail: ImageView = binding.lay4.productThumbnail

        val recyclerView = binding.lay4.uploadImageRecycler
        recyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL, false
        )
        adapterUpload = UploadImageAdapter(uriList, this)

        val startForThumbnail =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data

                when (resultCode) {
                    Activity.RESULT_OK -> {
                        //Image Uri will not be null for RESULT_OK
                        thumbUri = data?.data!!

                        val namefile = getFileName(thumbUri!!)
                        binding.lay4.errorMessageText.text = namefile
                        Glide.with(this).load(thumbUri)
                            .placeholder(R.drawable.as_square_placeholder).into(productThumbnail)

                        loadingDialog.dismiss()
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            ImagePicker.getError(data),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("StartForProductImage", "${ImagePicker.getError(data)}")
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT)
                            .show()
                    }
                }
            }

        val startForProductImages =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data

                when (resultCode) {
                    Activity.RESULT_OK -> {
                        //Image Uri will not be null for RESULT_OK
                        fileUri = data?.data!!
                        val names = getFileName(fileUri!!)
                        uriList.add(fileUri!!)
                        adapterUpload.notifyDataSetChanged()
                        nameList.add(names)

                        binding.lay4.textView44.text = "url ${uriList.size}"

                        loadingDialog.dismiss()
//                        Glide.with(this).load(fileUri).placeholder(R.drawable.as_square_placeholder).into(image)
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(
                            requireContext(),
                            ImagePicker.getError(data),
                            Toast.LENGTH_SHORT
                        ).show()
                        Log.e("StartForProductImage", "${ImagePicker.getError(data)}")
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT)
                            .show()
                        Log.e("StartForProductImage", "Task Cancelled")
                    }
                }
            }


        productThumbnail.setOnClickListener {
            ImagePicker.with(this)
                .compress(100)
                .maxResultSize(
                    500,
                    500
                ) //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForThumbnail.launch(intent)
                    loadingDialog.show(childFragmentManager, "Show")
                }
        }
        binding.lay4.selectImageBtn.setOnClickListener {
            ImagePicker.with(this)
                .compress(600)
                .maxResultSize(
                    900,
                    900
                ) //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    loadingDialog.show(childFragmentManager, "Show")
                    startForProductImages.launch(intent)
                }
        }


        val indicator = binding.lay4.recyclerviewPagerIndicator
        recyclerView.adapter = adapterUpload
        indicator.attachToRecyclerView(recyclerView)


        return binding.root
    }

    override fun onStart() {
        super.onStart()
        val lay1 = binding.lay1
        val lay2 = binding.lay2
        val lay3 = binding.lay3


        binding.nextBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager, "Show")
            checkAllDetails(it)

//            val action = AddProductDetailsFragmentDirections.actionAddProductDetailsFragmentToAddProductImageFragment("Product_testing_1")
//            findNavController().navigate(action)

        }

        lay2.bookStateToggle.setOnCheckedChangeListener { group, checkedId ->
            bookStateRadio = group.findViewById(checkedId)
            when (checkedId) {
                R.id.new_radioButton -> {
                    printDateInput.editText?.hint = "Printed Year *"
                    printDateMandatory = true
                }
                R.id.used_radioButton, R.id.refurb_radioButton -> {
                    printDateInput.editText?.hint = "Printed Year(if any)"
                    printDateMandatory = false
                }
            }
        }

        lay2.stockDownBtn.setOnClickListener {
            val quantity = stockQuantity.text.toString().toInt()
            if (quantity > 0) {
                val num = (quantity - 1).toString()
                stockQuantity.setText(num)
            } else {
                Toast.makeText(requireContext(), " can't be less than 0", Toast.LENGTH_SHORT)
                    .show()
            }
        }
        lay2.stockUpBtn.setOnClickListener {
            val quantity = stockQuantity.text.toString().toInt()
            val num = (quantity + 1).toString()
            stockQuantity.setText(num)
        }

        lay2.bookConditionToggle.setOnCheckedChangeListener { group, checkedId ->
            bookConditionRadio = group.findViewById(checkedId)

        }

        chipListenerForCategory(lay3.categoryChipGroup)

        lay3.autoTagBtn.setOnClickListener {
            autoTagging()
        }
    }

    private fun chipListenerForCategory(chipGroup: ChipGroup) {
        for (index in 0 until chipGroup.childCount) {
            val chip: Chip = chipGroup.getChildAt(index) as Chip

            // Set the chip checked change listener
            chip.setOnCheckedChangeListener { view, isChecked ->
                if (isChecked) {
                    categoryList.add(view.tag.toString())

                } else {
                    categoryList.remove(view.tag.toString())
                }

                if (view.id == R.id.categoryChip37) {
                    myOwnCategoryText.visibility = View.VISIBLE
                    binding.lay3.textView36.visibility = View.VISIBLE
                }

            }
        }
    }


    private fun checkName(): Boolean {
        val nameString: String = bookName.editText?.text.toString().trim()
        return if (nameString.isEmpty()) {
            bookName.isErrorEnabled = true
            bookName.error = "Field can't be empty"
            false
        } else {
            bookName.isErrorEnabled = false
            bookName.error = null
            true
        }
    }

    private fun checkPublisher(): Boolean {
        val publisherString: String = publisherName.editText?.text.toString().trim()
        return if (publisherString.isEmpty()) {
            publisherName.isErrorEnabled = true
            publisherName.error = "Field can't be empty"
            false
        } else {
            publisherName.isErrorEnabled = false
            publisherName.error = null
            true
        }
    }

    private fun checkWriter(): Boolean {
        val writerNameString: String = writerName.editText?.text.toString().trim()
        return if (writerNameString.isEmpty()) {
            writerName.isErrorEnabled = true
            writerName.error = "Field can't be empty"
            false
        } else {
            writerName.isErrorEnabled = false
            writerName.error = null
            true
        }
    }

    private fun checkLanguage(): Boolean {
        val languageString: String = language.editText?.text.toString().trim()
        return if (languageString.isEmpty()) {
            language.isErrorEnabled = true
            language.error = "Field can't be empty"
            false
        } else {
            language.isErrorEnabled = false
            language.error = null
            true
        }
    }

    private fun checkPageCount(): Boolean {
        val nameInput: String = pageCount.editText?.text.toString().trim()
        return if (nameInput.isEmpty()) {
            pageCount.isErrorEnabled = true
            pageCount.error = "Field can't be empty"
            false
        } else {
            pageCount.isErrorEnabled = false
            pageCount.error = null
            true
        }
    }

    private fun checkIsbn(): Boolean {
        val nameInput: String = isbnNo.editText?.text.toString().trim()
        return if (nameInput.isEmpty()) {
            isbnNo.isErrorEnabled = true
            isbnNo.error = "Field can't be empty"
            false
        } else {
            isbnNo.error = null
            true
        }
    }

    private fun checkDimensionWidth(): Boolean {
        val input: String = dimensionWidth.editText?.text.toString().trim()
        return if (input.isEmpty()) {
            dimensionWidth.isErrorEnabled = true
            dimensionWidth.error = "Field can't be empty"
            false
        } else {
            dimensionWidth.isErrorEnabled = false
            dimensionWidth.error = null
            true
        }
    }

    private fun checkDimensionLength(): Boolean {
        val input: String = dimensionLength.editText?.text.toString().trim()
        return if (input.isEmpty()) {
            dimensionLength.isErrorEnabled = true
            dimensionLength.error = "Field can't be empty"
            false
        } else {
            dimensionLength.isErrorEnabled = false
            dimensionLength.error = null
            true
        }
    }

    private fun checkDimensionHeight(): Boolean {
        val input: String = dimensionHeight.editText?.text.toString().trim()
        return if (input.isEmpty()) {
            dimensionHeight.isErrorEnabled = true
            dimensionHeight.error = "Field can't be empty"
            false
        } else {
            dimensionHeight.isErrorEnabled = false
            dimensionHeight.error = null
            true
        }
    }

    private fun checkDescription(): Boolean {
        val input: String = description.editText?.text.toString().trim()
        return if (input.isEmpty()) {
            description.isErrorEnabled = true
            description.error = "Field can't be empty in description"
            false
        } else {
            description.isErrorEnabled = false
            description.error = null
            true
        }
    }

    private fun checkPrice(): Boolean {
        val input: String = bookPrice.editText?.text.toString().trim()

        return if (input.isEmpty()) {
            bookPrice.isErrorEnabled = true
            bookPrice.error = "Field can't be empty"
            false
        } else {
            val num = input.toInt()

            bookPrice.isErrorEnabled = false
            bookPrice.error = null
            true
        }
    }

    private fun checkType(): Boolean {
        return if (bookStateRadio == null) {
            binding.lay2.bookStateLayout.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.red)
            false
        } else {
            binding.lay2.bookStateLayout.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.white)
            true
        }
    }

    private fun checkCondition(): Boolean {
        return if (bookConditionRadio == null) {
            binding.lay2.bookConditionLayout.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.red)
            false
        } else {
            binding.lay2.bookConditionLayout.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.white)
            true
        }
    }

    private fun checkCategory(): Boolean {
        val categoryContainer = binding.lay3.categoryLayout

        return when {

            (categoryList.size == 0) -> {
                categoryContainer.backgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.red)
                false
            }
            (categoryList.size != 0) -> {
                categoryContainer.backgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.white)
                true
            }


            else -> {
                false
            }
        }
    }

    private fun checkTags(): Boolean {
        val tags: String = tagInput.editText?.text.toString()
        return if (tags.isEmpty()) {
            tagInput.isErrorEnabled = true
            tagInput.error = "Field can't be empty"
            false
        } else {
            tagInput.isErrorEnabled = false
            tagInput.error = null
            true
        }
    }

    private fun checkStock(): Boolean {
        val stockQuantityString = stockQuantity.text.toString()
        return if (stockQuantityString.isEmpty()) {
            binding.lay2.stockquantityLayout.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.red)
            false
        } else {
            if (stockQuantityString.toInt() < 0) {
                binding.lay2.stockquantityLayout.backgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.red)
                false
            } else {
                binding.lay2.stockquantityLayout.backgroundTintList =
                    AppCompatResources.getColorStateList(requireContext(), R.color.white)
                true
            }

        }
    }


    private fun checkPrintDate(): Boolean {
        val printDateString = printDateInput.editText?.text.toString()
        return if (printDateMandatory) {
            if (printDateString.isEmpty()) {
                printDateInput.isErrorEnabled = true
                printDateInput.error = "Field can't be empty"
                false
            } else {
                printDateInput.error = null
                true
            }
        } else {
            true
        }
    }


    private fun checkThumbnail(): Boolean {
        return if (thumbUri == null) {
            binding.lay4.errorMessageText.text = "Select image"
            binding.lay4.errorMessageText.visibility = View.VISIBLE
            false
        } else {
            binding.lay4.errorMessageText.text = ""
            binding.lay4.errorMessageText.visibility = View.GONE
            true
        }
    }

    private fun checkProductImage(): Boolean {
        val selectBtn = binding.lay4.selectImageBtn
        return if (uriList.size == 0) {
            selectBtn.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.red)
            selectBtn.requestFocus()
            false
        } else {
            selectBtn.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.purple_500)
            true
        }
    }


    private fun checkAllDetails(v: View?) {
        val documentName: String = "Product_testing_1"
        if (!checkName() or !checkPublisher() or !checkWriter() or !checkLanguage() or !checkPageCount()
            or !checkDimensionWidth() or !checkDimensionLength() or !checkDimensionHeight()
            or !checkDescription() or !checkPrice() or !checkType() or !checkCondition() or !checkCategory()
            or !checkTags() or !checkStock() or !checkPrintDate() or !checkThumbnail() or !checkProductImage()
        ) {
            loadingDialog.dismiss()
            Snackbar.make(v!!, "Fill all fields", Snackbar.LENGTH_SHORT).show()
            return
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    addNewProduct(v, documentName)
                    delay(1000)
                }
                withContext(Dispatchers.IO) {
                    uploadThumbnail(documentName)
                }
                withContext(Dispatchers.IO) {
                    uploadProductImage(documentName)
                    delay(1000)
                }
            }

        }
    }

    private fun addNewProduct(v: View?, documentName: String) {
        tagsToList()
        myOwnCategory()

        val width: String = dimensionWidth.editText?.text.toString().trim()
        val length: String = dimensionLength.editText?.text.toString().trim()
        val height: String = dimensionHeight.editText?.text.toString().trim()
        val dimBuilder: StringBuilder = StringBuilder()
        dimBuilder.append(width).append(" x ").append(length).append(" x ").append(height)


        val addProductMap: MutableMap<String, Any> = HashMap()
        addProductMap["book_title"] = bookName.editText!!.text.toString()
        addProductMap["book_writer"] = writerName.editText!!.text.toString()
        addProductMap["book_publisher"] = publisherName.editText!!.text.toString()
        addProductMap["book_details"] = description.editText!!.text.toString()
        addProductMap["book_ISBN"] = isbnNo.editText!!.text.toString()
        addProductMap["book_language"] = language.editText!!.text.toString()
        addProductMap["book_pageCount"] = pageCount.editText!!.text.toString()
        addProductMap["book_dimension"] = dimBuilder.toString()

        if (discountPrice.editText!!.text.toString().isEmpty()) {
            addProductMap["price_selling"] = bookPrice.editText!!.text.toString().toLong()
            addProductMap["price_original"] = 0L

        } else {
            addProductMap["price_selling"] = discountPrice.editText!!.text.toString().toLong()
            addProductMap["price_original"] = bookPrice.editText!!.text.toString().toLong()
        }




        addProductMap["book_condition"] = bookConditionRadio?.tag.toString().trim()
        addProductMap["book_type"] = bookStateRadio?.tag.toString().trim()
        addProductMap["product_thumbnail"] = ""
        addProductMap["process_complete"] = true

        if (printDateMandatory) {
            addProductMap["book_printed_ON"] = printDateInput.editText?.text.toString().trim()
        } else {
            if (printDateInput.editText?.text.toString().isNotEmpty()) {
                addProductMap["book_printed_ON"] = printDateInput.editText?.text.toString().trim()
            } else {
                addProductMap["book_printed_ON"] = "Not available"
            }
        }

        addProductMap["hide_this_product"] = false
        addProductMap["in_stock_quantity"] = stockQuantity.text.toString().toLong()
        addProductMap["categories"] = categoryList
        addProductMap["tags"] = tagList
        addProductMap["rating_total"] = 0L
        addProductMap["rating_avg"] = ""
        addProductMap["rating_Star_5"] = 0L
        addProductMap["rating_Star_4"] = 0L
        addProductMap["rating_Star_3"] = 0L
        addProductMap["rating_Star_2"] = 0L
        addProductMap["rating_Star_1"] = 0L
        addProductMap["PRODUCT_CREATED_ON"] = FieldValue.serverTimestamp()
        addProductMap["PRODUCT_SELLER_ID"] = user!!.uid
        //loadingDialog.show()
        firebaseFirestore.collection("PRODUCTS").document(documentName).set(addProductMap)
            .addOnSuccessListener {
                Log.i("CreateProductToFirebase", "Product successfully added")
                Snackbar.make(v!!, "Product successfully added", Snackbar.LENGTH_SHORT).show()

            }.addOnFailureListener {
                Log.e("CreateProductToFirebase", "${it.message}")
                Snackbar.make(v!!, "Failed to add product", Snackbar.LENGTH_SHORT).show()
            }
    }

    private fun uploadThumbnail(productID: String) {
        val mRef: StorageReference =
            storageReference.child("image/" + user!!.uid + "/").child(getFileName(thumbUri!!))
        mRef.putFile(thumbUri!!)
            .addOnCompleteListener {
                mRef.downloadUrl.addOnSuccessListener {
                    val uploadThumbMap: MutableMap<String, Any> = HashMap()
                    uploadThumbMap["product_thumbnail"] = it.toString()
                    firebaseFirestore.collection("PRODUCTS").document(productID)
                        .update(uploadThumbMap)
                        .addOnSuccessListener {
                            Log.i("Update Product Thumbnail", "Successfully updated")
                        }.addOnFailureListener { e ->
                            Log.e("get Product Thumbnail url", "${e.message}")
                        }
                }.addOnFailureListener {
                    Log.e("uploadThumbnail", "${it.message}")
                }
            }
    }

    private fun uploadProductImage(productID: String) {
        for (i in 0 until uriList.size) {
            val allRef: StorageReference =
                storageReference.child("image/" + user!!.uid + "/").child(nameList[i])
            allRef.putFile(uriList[i])
                .addOnCompleteListener {
                    allRef.downloadUrl.addOnSuccessListener {
                        downloadUriList.add(it.toString())
                        when (i) {
                            (uriList.size - 1) -> {
                                binding.lay4.textView44.text =
                                    "${uriList.size} and ${downloadUriList.size} and itaretion $i"
                                updateProductImageLIST(productID, downloadUriList)
                            }
                            else -> {
                                Log.i("list size", "not reached the last position")
                            }
                        }
                    }.addOnFailureListener {
                        Log.e("Get Product Image download url", "${it.message}")
                    }
                }.addOnFailureListener {
                    Log.e("Upload Product Image", "${it.message}")
                }
        }

    }

    private fun updateProductImageLIST(productID: String, uriList: MutableList<String>) {
        val allMap: MutableMap<String, Any> = HashMap()
        allMap["productImage_List"] = uriList

        firebaseFirestore.collection("PRODUCTS").document(productID).update(allMap)
            .addOnSuccessListener {
                Log.i("Update Product Image", "Successfully updated")
                Toast.makeText(requireContext(), "Product is successfully added", Toast.LENGTH_LONG)
                    .show()
                loadingDialog.dismiss()
                activity!!.finish()
            }.addOnFailureListener {
                Log.e("Update Product Image", "${it.message}")
            }
    }

    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String { // for image names
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? =
                requireContext().contentResolver.query(uri, null, null, null, null)

            cursor.use { cursor ->
                if (cursor != null && cursor.moveToFirst()) {
                    result = cursor.getString(cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }

        if (result == null) {
            result = uri.path
            val cut = result!!.lastIndexOf('/')
            if (cut != -1) {
                result = result!!.substring(cut + 1)
            }
        }
        return result as String
    }


    private fun tagsToList(): MutableList<String> {
        val tag: String = tagInput.editText?.text.toString().trim().lowercase()
        //val tagArray = tag.split(",").toTypedArray()

        val firstFilterTag = tag.replace(".", ",").replace(":", ",")
            .replace("/", ",").replace(" ", ",")

        val secondFilterTag = firstFilterTag
            .replace(",,", ",")
            .replace(",,,", ",")
            .replace(",,,,", ",")

        val tagArray: List<String> = secondFilterTag.split(",")


        tagList.addAll(tagArray)

        binding.lay3.autoTagText.text = "${tagList[0]} and ${tagList.size}"

        binding.lay3.autoTagText.visibility = View.VISIBLE

        return tagList
    }

    private fun myOwnCategory(): MutableList<String> {
        val category: String = myOwnCategoryText.text.toString().trim().lowercase()
        //val tagArray = tag.split(",").toTypedArray()
        return if (category.isNotEmpty()) {
            val firstFilterTag = category.replace("-", ",").replace(" ", ",")
            val secondFilterTag = firstFilterTag.replace(",,", ",")

            val categoryArray: List<String> = secondFilterTag.split(",")
            categoryList.addAll(categoryArray)

//        binding.lay3.autoTagText.text = "${tagList[0]} and ${tagList.size}"

            binding.lay3.autoTagText.visibility = View.VISIBLE

            categoryList
        } else {
            categoryList
        }


    }

    private fun autoTagging() {
        if (!checkName() or !checkPublisher() or !checkWriter() or !checkLanguage()
            or !checkType() or !checkCondition() or !checkCategory()
        ) {
            return
        } else {

            myOwnCategory()
            var categoryString = ""

            val nameInput: String = bookName.editText?.text.toString().trim()
            val writerNameString: String = writerName.editText?.text.toString().trim()
            val publisherString: String = publisherName.editText?.text.toString().trim()
            val languageString: String = language.editText?.text.toString().trim()
            val bookCondition = bookConditionRadio?.tag.toString().trim()
            val bookType = bookStateRadio?.tag.toString().trim()


            for (category in categoryList) {
                categoryString += "$category,"
            }

            val docBuilder: StringBuilder = StringBuilder()
            docBuilder.append(nameInput).append(",")
                .append(writerNameString).append(",")
                .append(publisherString).append(",")
                .append(languageString).append(",")
                .append(categoryString).append(",")
                .append(bookCondition).append(",")
                .append(bookType)

            val docName = docBuilder.toString().replace(".", ",").replace(":", ",")
                .replace("/", ",").replace(" ", ",")

            val filterTags = docName
                .replace(",,", ",")
                .replace(",,,", ",")
                .replace(",,,,", ",")

            tagInput.editText?.setText(filterTags)

        }
    }

    private fun generateDocName(): String {


        val timeString = LocalDateTime.now().toString()
        val userString = user!!.uid
        val docBuilder: StringBuilder = StringBuilder()
        docBuilder.append(timeString).append(userString)
        return docBuilder.toString().replace(".", "_").replace("-", "_").replace(":", "_")
    }

    override fun onItemClick(position: Int) {
        uriList.removeAt(position)
        nameList.removeAt(position)
        adapterUpload.notifyItemRemoved(position)
        binding.lay4.textView44.text = "url ${uriList.size}"
    }

}