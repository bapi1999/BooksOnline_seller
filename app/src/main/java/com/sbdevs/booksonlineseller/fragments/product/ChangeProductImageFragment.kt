package com.sbdevs.booksonlineseller.fragments.product

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
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.ItemTouchHelper
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.adapters.AlreadyUploadedImageAdapter
import com.sbdevs.booksonlineseller.adapters.NewUploadImageAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentChangeProductImageBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import kotlinx.coroutines.*
import kotlinx.coroutines.tasks.await
import java.time.Year
import java.util.*
import kotlin.collections.ArrayList

class ChangeProductImageFragment : Fragment(), NewUploadImageAdapter.MyOnItemClickListener,
    AlreadyUploadedImageAdapter.AlreadyAddedImageClickListener {
    private var _binding: FragmentChangeProductImageBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private val storage = Firebase.storage
    private val storageReference = storage.reference

    private lateinit var productThumbnail: ImageView

    private var productImgList: ArrayList<String> = ArrayList()
    private var alreadyAddedAdapter: AlreadyUploadedImageAdapter =
        AlreadyUploadedImageAdapter(productImgList, this)
    private lateinit var alreadyAddedImgRecyclerView: RecyclerView

    private lateinit var newAddedAdapter: NewUploadImageAdapter
    private lateinit var newAddedImgRecyclerView: RecyclerView

    private var deleteedImageList: ArrayList<String> = ArrayList()

    private val loadingDialog = LoadingDialog()

    private lateinit var updateMessageText: TextView

    var thumbUri: Uri? = null
    private var fileUri: Uri? = null
    private var uriList: ArrayList<Uri> = ArrayList()
    var nameList: ArrayList<String> = ArrayList()

    private var downloadUriList: MutableList<String> = ArrayList()

    private lateinit var productId: String
    private var currentYear:Int = 0
    private val args: ChangeProductImageFragmentArgs by navArgs()
    private var changeInPosition = false

    private val simpleCallback1 =
        object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.START or ItemTouchHelper.END, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                Collections.swap(productImgList, fromPosition, toPosition)

                alreadyAddedAdapter.notifyItemMoved(fromPosition, toPosition)

                changeInPosition = true

                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //TODO("Not yet implemented")
            }
        }

    private val simpleCallback2 =
        object : ItemTouchHelper.SimpleCallback(ItemTouchHelper.START or ItemTouchHelper.END, 0) {
            override fun onMove(
                recyclerView: RecyclerView,
                viewHolder: RecyclerView.ViewHolder,
                target: RecyclerView.ViewHolder
            ): Boolean {
                val fromPosition = viewHolder.adapterPosition
                val toPosition = target.adapterPosition
                Collections.swap(uriList, fromPosition, toPosition)

                newAddedAdapter.notifyItemMoved(fromPosition, toPosition)

                return false
            }

            override fun onSwiped(viewHolder: RecyclerView.ViewHolder, direction: Int) {
                //TODO("Not yet implemented")
            }
        }





    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        _binding = FragmentChangeProductImageBinding.inflate(inflater, container, false)
        alreadyAddedImgRecyclerView = binding.lay4.alreadyUploadImageRecycler
        newAddedImgRecyclerView = binding.lay4.newUploadImageRecycler

        productId = args.productId
        productThumbnail = binding.lay4.productThumbnail
        updateMessageText = binding.updateMessage

        newAddedImgRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL, false
        )
        newAddedAdapter = NewUploadImageAdapter(uriList, this)

        lifecycleScope.launch(Dispatchers.Main) {
            loadingDialog.show(childFragmentManager, "Show")
            withContext(Dispatchers.IO) {
                getProductData(productId)
            }
        }

        currentYear = Year.now().value

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        val startForThumbnail =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result: ActivityResult ->
                val resultCode = result.resultCode
                val data = result.data

                when (resultCode) {
                    Activity.RESULT_OK -> {
                        //Image Uri will not be null for RESULT_OK
                        thumbUri = data?.data!!

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
                        loadingDialog.dismiss()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT)
                            .show()
                        loadingDialog.dismiss()

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
                        newAddedAdapter.notifyDataSetChanged()
                        nameList.add(names)

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
                        loadingDialog.dismiss()
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT)
                            .show()
                        Log.e("StartForProductImage", "Task Cancelled")
                        loadingDialog.dismiss()
                    }
                }
            }


        binding.lay4.selectThumbnail.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(100)
                .maxResultSize(500, 500) //Final image resolution will be less than 1080 x 1080
                .createIntent { intent ->
                    startForThumbnail.launch(intent)
                    loadingDialog.show(childFragmentManager, "Show")
                }
        }
        binding.lay4.selectImageBtn.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(600)
                .maxResultSize(900, 900) //Final image resolution will be less than 1080 x 1080
                .createIntent { intent ->
                    loadingDialog.show(childFragmentManager, "Show")
                    startForProductImages.launch(intent)
                }
        }


        val indicator = binding.lay4.recyclerviewPagerIndicator2
        newAddedImgRecyclerView.adapter = newAddedAdapter
        indicator.attachToRecyclerView(newAddedImgRecyclerView)

        val itemTouchHelper2 = ItemTouchHelper(simpleCallback2)
        itemTouchHelper2.attachToRecyclerView(newAddedImgRecyclerView)




        binding.lay4.updateImageBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager, "Show")
            verificationForProductImage(it)
        }

        binding.lay4.updatdeThumnailBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager, "Show")
            verificationForThumbnail(it)
        }
    }


    @Suppress("UNCHECKED_CAST")
    private fun getProductData(productId: String) = CoroutineScope(Dispatchers.IO).launch {

        firebaseFirestore.collection("PRODUCTS")
            .document(productId).get()
            .addOnSuccessListener {


                val url = it.get("product_thumbnail").toString()

                Glide.with(requireContext()).load(url).into(productThumbnail)

                productImgList = it.get("productImage_List") as ArrayList<String>


                alreadyAddedAdapter.list = productImgList
                alreadyAddedImgRecyclerView.adapter = alreadyAddedAdapter
                alreadyAddedAdapter.notifyDataSetChanged()

                val itemTouchHelper = ItemTouchHelper(simpleCallback1)
                itemTouchHelper.attachToRecyclerView(alreadyAddedImgRecyclerView)


                loadingDialog.dismiss()


            }.addOnFailureListener {
                Log.e("Product", "${it.message}", it.cause)

                loadingDialog.dismiss()

            }.await()
    }

    private fun checkProductImage(): Boolean {
        val selectBtn = binding.lay4.selectImageBtn
        return if (uriList.isEmpty() and productImgList.isEmpty()) {
            selectBtn.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.red_a700)

            selectBtn.requestFocus()
            false
        } else {

            selectBtn.backgroundTintList =
                AppCompatResources.getColorStateList(requireContext(), R.color.grey_200)
            true

        }
    }

    //todo - flag to check whether or not user swipd the position of items or select a new image

    private fun checkThumbnail(): Boolean {
        return if (thumbUri == null) {
            loadingDialog.dismiss()
            binding.lay4.errorMessageText.visibility = View.VISIBLE
            binding.lay4.errorMessageText.text = "Select new thumbnail"
            false
        } else {

            binding.lay4.errorMessageText.visibility = View.GONE
            binding.lay4.errorMessageText.text = ""

            true
        }
    }

    private fun verificationForThumbnail(v: View?) {
        if (!checkThumbnail()) {
            loadingDialog.dismiss()
            Snackbar.make(v!!, "Thumbnail is not changed", Snackbar.LENGTH_SHORT).show()
            return
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    uploadThumbnail(productId)

                }
            }

        }
    }


    private fun verificationForProductImage(v: View?) {
        if (!checkProductImage()) {
            loadingDialog.dismiss()
            Log.w("clicked", "nothing happened")
            //Snackbar.make(v!!, "Fill all fields", Snackbar.LENGTH_SHORT).show()
            return
        } else {
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO) {
                    deleteImage()
                    when {
                        changeInPosition and uriList.isNotEmpty() -> {
                            uploadProductImage(productId)
                            //Snackbar.make(v!!, "both happen", Snackbar.LENGTH_SHORT).show()
                        }
                        changeInPosition and uriList.isEmpty() -> {
                            updateChangeInPosition(productId)
                            //Snackbar.make(v!!, "change position", Snackbar.LENGTH_SHORT).show()
                        }
                        !changeInPosition and uriList.isNotEmpty() -> {
                            uploadProductImage(productId)
                            //Snackbar.make(v!!, "new image added", Snackbar.LENGTH_SHORT).show()
                        }
                        else -> {
//                            Snackbar.make(v!!, "nothing change", Snackbar.LENGTH_SHORT).show()
                            Log.w("clicked", "nothing happened")
                        }
                    }
                }
            }

        }
    }


    private suspend fun uploadThumbnail(productID: String) {
        val mRef: StorageReference =
            storageReference.child("image/" + user!!.uid + "/")
                .child("$currentYear/")
                .child("$productID/")
                .child(productID + "thumb")
        mRef.putFile(thumbUri!!)
            .addOnCompleteListener {
                mRef.downloadUrl.addOnSuccessListener {
                    val uploadThumbMap: MutableMap<String, Any> = HashMap()
                    uploadThumbMap["product_thumbnail"] = it.toString()
                    firebaseFirestore.collection("PRODUCTS").document(productID)
                        .update(uploadThumbMap)
                        .addOnSuccessListener {
                            Log.i("Update Product Thumbnail", "Successfully updated")
                            Toast.makeText(requireContext(), "Thumbnail updated successfully", Toast.LENGTH_LONG).show()
                            updateMessageText.text = getString(R.string.updated_successfully)
                            updateMessageText.setTextColor(AppCompatResources.getColorStateList(requireContext(), R.color.indigo_500))
                            loadingDialog.dismiss()
                        }.addOnFailureListener { e ->
                            Log.e("get Product Thumbnail url", "${e.message}")
                            updateMessageText.text = getString(R.string.failed_to_update)
                            updateMessageText.setTextColor(AppCompatResources.getColorStateList(requireContext(), R.color.red_500))
                            loadingDialog.dismiss()
                        }
                }.addOnFailureListener {
                    Log.e("uploadThumbnail", "${it.message}")
                }
            }
    }

    private suspend fun uploadProductImage(productID: String) {
        for (i in 0 until uriList.size) {
            val allRef: StorageReference =
                storageReference.child("image/" + user!!.uid + "/")
                    .child("$currentYear/")
                    .child("$productID/")
                    .child(nameList[i])
            allRef.putFile(uriList[i])
                .addOnCompleteListener {
                    allRef.downloadUrl.addOnSuccessListener {
                        downloadUriList.add(it.toString())
                        when (i) {
                            (uriList.size - 1) -> {
                                //binding.lay4.textView44.text = "${uriList.size} and ${downloadUriList.size} and itaretion $i"
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
                }.await()
        }

    }


    private fun updateProductImageLIST(productID: String, urlList: MutableList<String>) =
        CoroutineScope(Dispatchers.IO).launch {


            productImgList.addAll(urlList)
            changeInPosition = false

            val allMap: MutableMap<String, Any> = HashMap()
            allMap["productImage_List"] = productImgList

            firebaseFirestore.collection("PRODUCTS").document(productID).update(allMap)
                .addOnSuccessListener {
                    Log.i("Update Product Image", "Updated successfully")
                    Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_LONG)
                        .show()
                    updateMessageText.text = getString(R.string.updated_successfully)
                    updateMessageText.setTextColor(
                        AppCompatResources.getColorStateList(
                            requireContext(),
                            R.color.indigo_500
                        )
                    )

                    uriList.clear()
                    newAddedAdapter.notifyDataSetChanged()

                    alreadyAddedAdapter.notifyDataSetChanged()

                    loadingDialog.dismiss()
                }.addOnFailureListener {
                    Log.e("Update Product Image", "${it.message}")
                    updateMessageText.text = getString(R.string.failed_to_update)
                    updateMessageText.setTextColor(
                        AppCompatResources.getColorStateList(
                            requireContext(),
                            R.color.red_500
                        )
                    )
                    loadingDialog.dismiss()
                }.await()
            delay(500)
            withContext(Dispatchers.Main) {

            }
        }

    private fun updateChangeInPosition(productID: String) {

        val allMap: MutableMap<String, Any> = HashMap()
        allMap["productImage_List"] = productImgList

        changeInPosition = false

        firebaseFirestore.collection("PRODUCTS").document(productID).update(allMap)
            .addOnSuccessListener {
                Log.i("Update Product Image", "updated successfully")

                Toast.makeText(requireContext(), "Updated successfully", Toast.LENGTH_LONG).show()
                updateMessageText.text = getString(R.string.updated_successfully)
                updateMessageText.setTextColor(
                    AppCompatResources.getColorStateList(
                        requireContext(),
                        R.color.indigo_500
                    )
                )
                loadingDialog.dismiss()

                deleteedImageList.clear()

            }.addOnFailureListener {
                Log.e("Update Product Image", "${it.message}")

                Toast.makeText(requireContext(), "Failed to update", Toast.LENGTH_LONG).show()
                updateMessageText.text = getString(R.string.failed_to_update)
                updateMessageText.setTextColor(
                    AppCompatResources.getColorStateList(
                        requireContext(),
                        R.color.red_500
                    )
                )
                loadingDialog.dismiss()
            }
    }

    override fun onImageClick(position: Int) {

        deleteedImageList.add(productImgList[position])
        updateMessageText.text = "Update images before continue"
        updateMessageText.setTextColor(
            AppCompatResources.getColorStateList(
                requireContext(),
                R.color.amber_900
            )
        )
        changeInPosition = true
        productImgList.removeAt(position)
        alreadyAddedAdapter.notifyItemRemoved(position)



    }

    override fun onItemClick(position: Int) {
        uriList.removeAt(position)
        nameList.removeAt(position)
        newAddedAdapter.notifyItemRemoved(position)
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

    private fun clearUri(){
        uriList.clear()
        newAddedAdapter.notifyDataSetChanged()
    }


    private suspend fun deleteImage() {
        if (deleteedImageList.isNotEmpty()) {

            for (item in deleteedImageList) {

                val ref: StorageReference = storage.getReferenceFromUrl(item)
                ref.delete().addOnSuccessListener {
                    Log.w("delete","$item deleted")
                }.addOnFailureListener {
                    Log.w("delete","failed")
                }.await()
            }
        }else{
            Log.w("delete list","list is empty")
        }


    }

}