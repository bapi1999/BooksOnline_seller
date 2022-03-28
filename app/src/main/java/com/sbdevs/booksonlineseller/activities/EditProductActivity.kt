package com.sbdevs.booksonlineseller.activities


import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.*
import androidx.appcompat.content.res.AppCompatResources
import androidx.core.content.ContentProviderCompat.requireContext
import androidx.lifecycle.lifecycleScope
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.ActivityEditProductBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.util.*
import kotlin.collections.ArrayList

class EditProductActivity : AppCompatActivity() {


    private lateinit var binding: ActivityEditProductBinding

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

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
    private lateinit var categoryInput: TextInputLayout


    private var bookStateRadio: RadioButton? = null
    private var bookConditionRadio: RadioButton? = null
    private lateinit var stockQuantity: EditText

    private lateinit var productReturnRadioTogole: RadioGroup
    private var productReturnRadio: RadioButton? = null
    private var replacementPolicy = ""



    private var printDateMandatory: Boolean = false
    private val categoryList: MutableList<String> = ArrayList()
    private var tagList: MutableList<String> = ArrayList()
    private lateinit var updateMessageText:TextView

    private lateinit var productId: String

    private val loadingDialog = LoadingDialog()



    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityEditProductBinding.inflate(layoutInflater)
        setContentView(binding.root)


        productId = intent.getStringExtra("productId")!!

        val lay1 = binding.lay1
        val lay2 = binding.lay2
        val lay21 = binding.lay21
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
        categoryInput = lay3.categoryInput
        tagInput = lay3.editTags
        stockQuantity = lay2.stockQuantity
        productReturnRadioTogole = lay21.productReturnToggle
        updateMessageText = binding.updateMessage


        //productThumbnail = binding.lay4.productThumbnail

        lifecycleScope.launch(Dispatchers.Main) {
            loadingDialog.show(supportFragmentManager, "Show")
            withContext(Dispatchers.IO) {
                getProductData(productId)
            }
        }


    }

    override fun onStart() {
        super.onStart()

        val lay2 = binding.lay2


        binding.publishProductBtn.setOnClickListener {
            loadingDialog.show(supportFragmentManager,"show")
            checkAllDetails(it,productId)
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

        lay2.bookConditionToggle.setOnCheckedChangeListener { group, checkedId ->
            bookConditionRadio = group.findViewById(checkedId)
        }

        productReturnRadioTogole.setOnCheckedChangeListener { group, checkedId ->
            productReturnRadio = group.findViewById(checkedId)
            when (checkedId) {
                R.id.return_radio1 -> {
                    replacementPolicy = "7 days Replacement Policy"
                }
                R.id.return_radio2 -> {
                    replacementPolicy = "No Replacement Policy"
                }
            }
        }

    }

    @Suppress("UNCHECKED_CAST")
    private fun getProductData(productId: String) = CoroutineScope(Dispatchers.IO).launch {

        firebaseFirestore.collection("PRODUCTS")
            .document(productId).get()
            .addOnSuccessListener {

                var categoryString = ""
                var tagsString = ""

                val productName = it.getString("book_title")!!

                val priceOriginal = it.getLong("price_original")!!.toLong()
                val priceSelling = it.getLong("price_selling")!!.toLong()

                val stock = it.getLong("in_stock_quantity")!!
                val bookDescription = it.getString("book_details")!!
                val categoryList: ArrayList<String> = it.get("categories") as ArrayList<String>
                val tagList: ArrayList<String> = it.get("tags") as ArrayList<String>


                val bookWriter = it.getString("book_writer")
                val bookPublisherName = it.getString("book_publisher")
                val bookLanguage = it.getString("book_language")
                val productState = it.getString("book_type")!!
                val bookPrintDate = it.getLong("book_printed_ON")
                val bookCondition = it.getString("book_condition")
                val bookPageCount = it.getString("book_pageCount")
                val isbnNumber = it.getString("book_ISBN")
                val bookDimension = it.getString("book_dimension")
                val dimensionArray: List<String> = bookDimension!!.split("x")
                replacementPolicy = it.getString("Replacement_policy")!!

                stockQuantity.setText(stock.toString())

                for (categories in categoryList) {
                    categoryString += "$categories,"
                }

                for (tag in tagList) {
                    tagsString += "$tag,"
                }

                bookName.editText?.setText(productName)





                description.editText?.setText(bookDescription)
                writerName.editText?.setText(bookWriter)
                publisherName.editText?.setText(bookPublisherName)


                if (bookPrintDate == 0L){
                    printDateInput.editText?.setText("0")
                }else{
                    printDateInput.editText?.setText(bookPrintDate.toString())
                }

                language.editText?.setText(bookLanguage)
                pageCount.editText?.setText(bookPageCount)
                isbnNo.editText?.setText(isbnNumber)

                dimensionWidth.editText?.setText(dimensionArray[0].trim())
                dimensionLength.editText?.setText(dimensionArray[1].trim())
                dimensionHeight.editText?.setText(dimensionArray[2].trim())


                when(productState){
                    "new"->{
                        binding.lay2.bookStateToggle.check(R.id.new_radioButton)
                        bookStateRadio = binding.lay2.newRadioButton
                    }
                    "used"->{
                        binding.lay2.bookStateToggle.check(R.id.used_radioButton)
                        bookStateRadio = binding.lay2.usedRadioButton
                    }
                    "refurbished"->{
                        binding.lay2.bookStateToggle.check(R.id.refurb_radioButton)
                        bookStateRadio = binding.lay2.refurbRadioButton
                    }
                }

                when (replacementPolicy){
                    "7 days Replacement Policy" ->{
                        binding.lay21.productReturnToggle.check(R.id.return_radio1)
                        productReturnRadio = binding.lay21.returnRadio1
                    }
                    "No Replacement Policy" ->{
                        binding.lay21.productReturnToggle.check(R.id.return_radio2)
                        productReturnRadio = binding.lay21.returnRadio1
                    }
                }

                when(bookCondition){

                    "good"->{
                        binding.lay2.bookConditionToggle.check(R.id.cradioButton1)
                        bookConditionRadio = binding.lay2.cradioButton1
                    }
                    "almost_new"->{
                        binding.lay2.bookConditionToggle.check(R.id.cradioButton2)
                                bookConditionRadio = binding.lay2.cradioButton2
                    }
                    "half_bad"->{
                        binding.lay2.bookConditionToggle.check(R.id.cradioButton3)
                        bookConditionRadio = binding.lay2.cradioButton3
                    }
                    "bad"->{
                        binding.lay2.bookConditionToggle.check(R.id.cradioButton4)
                        bookConditionRadio = binding.lay2.cradioButton4
                    }

                }



                if (priceOriginal == 0L) {
                    bookPrice.editText?.setText(priceSelling.toString())

                } else {
                    bookPrice.editText?.setText(priceOriginal.toString())
                    discountPrice.editText?.setText(priceSelling.toString())
                }

                categoryInput.editText?.setText(categoryString)

                tagInput.editText?.setText(tagsString)

                loadingDialog.dismiss()


            }.addOnFailureListener {
                Log.e("Product", "${it.message}", it.cause)

                loadingDialog.dismiss()

            }.await()
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
                AppCompatResources.getColorStateList(this, R.color.red_a700)
            false
        } else {
            binding.lay2.bookStateLayout.backgroundTintList =
                AppCompatResources.getColorStateList(this, R.color.white)
            true
        }
    }

    private fun checkCondition(): Boolean {
        return if (bookConditionRadio == null) {
            binding.lay2.bookConditionLayout.backgroundTintList =
                AppCompatResources.getColorStateList(this, R.color.red_a700)
            false
        } else {
            binding.lay2.bookConditionLayout.backgroundTintList =
                AppCompatResources.getColorStateList(this, R.color.white)
            true
        }
    }

    private fun checkCategory(): Boolean {
        val categories = categoryInput.editText?.text.toString()

        return if (categories.isEmpty()) {
            categoryInput.isErrorEnabled = true
            categoryInput.error = "Field can't be empty"
            false
        } else {
            categoryInput.isErrorEnabled = false
            categoryInput.error = null
            true
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
            stockQuantity.backgroundTintList =
                AppCompatResources.getColorStateList(this, R.color.red_a700)
            false
        } else {
            if (stockQuantityString.toInt() < 0) {
                stockQuantity.backgroundTintList =
                    AppCompatResources.getColorStateList(this, R.color.red_a700)
                false
            } else {
                stockQuantity.backgroundTintList =
                    AppCompatResources.getColorStateList(this, R.color.white)
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

    private fun checkProductReturnState():Boolean{
        return if (productReturnRadio == null) {
            binding.lay21.returnContainer.backgroundTintList =
                AppCompatResources.getColorStateList(this, R.color.red_a700)
            false
        } else {
            binding.lay21.returnContainer.backgroundTintList =
                AppCompatResources.getColorStateList(this, R.color.white)
            true
        }
    }





    private fun checkAllDetails(v: View?, productID:String) {

        if (!checkName() or !checkPublisher() or !checkWriter() or !checkLanguage() or !checkPageCount()
            or !checkDimensionWidth() or !checkDimensionLength() or !checkDimensionHeight()
            or !checkDescription() or !checkPrice() or !checkType() or !checkCondition() or !checkCategory()
            or !checkTags() or !checkStock() or !checkPrintDate() or !checkProductReturnState()
        ) {
            loadingDialog.dismiss()
            Snackbar.make(v!!, "Fill all fields", Snackbar.LENGTH_SHORT).show()
            return
        } else {
            lifecycleScope.launch {

                withContext(Dispatchers.IO) {
                    tagsToList()
                    myOwnCategory()
                    delay(100)
                    updateProduct( productID)

                }

            }

        }
    }

    private fun updateProduct( documentName: String) {


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

        if (printDateMandatory) {
            addProductMap["book_printed_ON"] = printDateInput.editText?.text.toString().trim().toLong()
        } else {
            if (printDateInput.editText?.text.toString().isNotEmpty()) {
                addProductMap["book_printed_ON"] = printDateInput.editText?.text.toString().trim().toLong()
            } else {
                addProductMap["book_printed_ON"] = 0L
            }
        }

        addProductMap["in_stock_quantity"] = stockQuantity.text.toString().toLong()
        addProductMap["categories"] = categoryList
        addProductMap["tags"] = tagList

        addProductMap["PRODUCT_UPDATE_ON"] = FieldValue.serverTimestamp()
        addProductMap["Replacement_policy"] = replacementPolicy

        firebaseFirestore.collection("PRODUCTS").document(documentName).update(addProductMap)
            .addOnSuccessListener {
                Log.i("CreateProductToFirebase", "Product successfully added")
                Toast.makeText(this,"Updated successfully",Toast.LENGTH_LONG).show()

                updateMessageText.text  = getString(R.string.updated_successfully)
                updateMessageText.setTextColor(AppCompatResources.getColorStateList(this,R.color.indigo_500))
                loadingDialog.dismiss()

            }.addOnFailureListener {
                Log.e("CreateProductToFirebase", "${it.message}")
                Toast.makeText(this,"Failed to update product",Toast.LENGTH_LONG).show()

                updateMessageText.text  = getString(R.string.failed_to_update)
                updateMessageText.setTextColor(AppCompatResources.getColorStateList(this,R.color.red_500))

                loadingDialog.dismiss()
            }
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



        return tagList
    }

    private fun myOwnCategory() {
        val category: String = categoryInput.editText?.text.toString().trim().lowercase()
        //val tagArray = tag.split(",").toTypedArray()

        val firstFilterTag = category.replace("-", ",").replace(" ", ",").replace(",,", ",")

        val categoryArray: List<String> = firstFilterTag.split(",")
        categoryList.addAll(categoryArray)

        categoryList



    }


}