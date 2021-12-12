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
import android.widget.ImageView
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.github.dhaval2404.imagepicker.ImagePicker
import com.google.android.material.snackbar.Snackbar
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentAddProductImageBinding
import kotlin.collections.ArrayList

import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sbdevs.booksonlineseller.adapters.UploadImageAdapter
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.HashMap


class AddProductImageFragment : Fragment(),UploadImageAdapter.MyOnItemClickListener {
    private var _binding:FragmentAddProductImageBinding ? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private val storage = Firebase.storage
    private val storageReference = storage.reference


    private val args:AddProductImageFragmentArgs by navArgs()
    var thumbUri: Uri? = null
    private var fileUri: Uri? = null
    private var uriList:ArrayList<Uri> = ArrayList()
    var nameList:ArrayList<String> = ArrayList()
    lateinit var adapterUpload: UploadImageAdapter
    private var downloadUriList:MutableList<String> = ArrayList()

    private val loadingDialog = LoadingDialog()
    private lateinit var docname:String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductImageBinding.inflate(inflater, container, false)

        val productThumbnail: ImageView = binding.lay4.productThumbnail
        val productID = args.productId
        docname = args.productId.toString()

        val recyclerView = binding.lay4.uploadImageRecycler
        recyclerView.layoutManager = LinearLayoutManager(requireContext(),LinearLayoutManager.HORIZONTAL,false)
        adapterUpload =UploadImageAdapter(uriList,this)

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
                        Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                        Log.e("StartForProductImage","${ImagePicker.getError(data)}")
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
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

//                        Glide.with(this).load(fileUri).placeholder(R.drawable.as_square_placeholder).into(image)
                    }
                    ImagePicker.RESULT_ERROR -> {
                        Toast.makeText(requireContext(), ImagePicker.getError(data), Toast.LENGTH_SHORT).show()
                        Log.e("StartForProductImage","${ImagePicker.getError(data)}")
                    }
                    else -> {
                        Toast.makeText(requireContext(), "Task Cancelled", Toast.LENGTH_SHORT).show()
                        Log.e("StartForProductImage","Task Cancelled")
                    }
                }
            }


        productThumbnail.setOnClickListener {
            ImagePicker.with(this)
                .compress(100)
                .maxResultSize(500, 500) //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForThumbnail.launch(intent)
                    loadingDialog.show(childFragmentManager,"Show")
                }
        }
        binding.lay4.selectImageBtn.setOnClickListener {
            ImagePicker.with(this)
                .compress(600)
                .maxResultSize(900, 900) //Final image resolution will be less than 1080 x 1080(Optional)
                .createIntent { intent ->
                    startForProductImages.launch(intent)
                }
        }


//        val indicator = binding.lay4.recyclerviewPagerIndicator
        recyclerView.adapter  = adapterUpload


        activity?.onBackPressedDispatcher?.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                val intent = Intent(context,MainActivity::class.java)
//                startActivity(intent)
                Toast.makeText(requireContext(),"Saved as draft",Toast.LENGTH_LONG).show()
                activity!!.finish()

            }
        })

        return binding.root
    }

    override fun onStart() {
        super.onStart()

        binding.publishBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager,"Show")
            checkAllDetails(it,docname)
        }

    }

    private fun checkThumbnail(): Boolean {
        return if (thumbUri == null) {
            binding.lay4.errorMessageText.text ="Select image"
            binding.lay4.errorMessageText.visibility =View.VISIBLE
            false
        } else {
            binding.lay4.errorMessageText.text =""
            binding.lay4.errorMessageText.visibility =View.GONE
            true
        }
    }

    private fun checkProductImage(): Boolean {
        val  selectBtn = binding.lay4.selectImageBtn
        return if (uriList.size == 0) {
            selectBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.red)
            selectBtn.requestFocus()
            false
        } else {
            selectBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.purple_500)
            true
        }
    }

    private fun checkAllDetails(v: View?,productID: String) {
        if ( !checkThumbnail() or !checkProductImage()) {
            loadingDialog.dismiss()
            Snackbar.make(v!!, "Fill all fields", Snackbar.LENGTH_SHORT).show()
            return

        } else {
            lifecycleScope.launch {

                withContext(Dispatchers.IO){
                    uploadThumbnail(productID)
                }
                withContext(Dispatchers.IO){
                    uploadProductImage(productID)
                    delay(1000)
                }

            }

        }
    }

    private fun uploadThumbnail(productID: String) {
        val mRef: StorageReference = storageReference.child("image/" + user!!.uid + "/").child(getFileName(thumbUri!!))
        mRef.putFile(thumbUri!!)
            .addOnCompleteListener {
                mRef.downloadUrl.addOnSuccessListener {
                    val uploadThumbMap: MutableMap<String, Any> = HashMap()
                    uploadThumbMap["product_thumbnail"] = it.toString()
                    firebaseFirestore.collection("PRODUCTS").document(productID).update(uploadThumbMap)
                        .addOnSuccessListener {
                            Log.i("Update Product Thumbnail","Successfully updated")
                        }.addOnFailureListener {e->
                            Log.e("get Product Thumbnail url","${e.message}")
                        }
                }.addOnFailureListener {
                    Log.e("uploadThumbnail","${it.message}")
                }
            }
    }

    private fun uploadProductImage(productID: String){
        for (i in 0 until uriList.size){
            val allRef: StorageReference = storageReference.child("image/" + user!!.uid + "/").child(nameList[i])
            allRef.putFile(uriList[i])
                .addOnCompleteListener {
                    allRef.downloadUrl.addOnSuccessListener {
                        downloadUriList.add(it.toString())
                        when(i){
                            uriList.size-1 ->{
                                updateProductImageLIST(productID,downloadUriList)
                            }
                            else->{
                                Log.i("list size","not reached the last position")
                            }
                        }
                    }.addOnFailureListener {
                        Log.e("Get Product Image download url","${it.message}")
                    }
                }.addOnFailureListener{
                    Log.e("Upload Product Image","${it.message}")
                }
        }

    }
    private fun updateProductImageLIST(productID: String,uriList:MutableList<String>){
        val allMap:MutableMap<String,Any> = HashMap()
        allMap["productImage_List"] = uriList

        firebaseFirestore.collection("PRODUCTS").document(productID).update(allMap)
            .addOnSuccessListener {
                Log.i("Update Product Image","Successfully updated")
                Toast.makeText(requireContext(),"Product is successfully added",Toast.LENGTH_LONG).show()
                loadingDialog.dismiss()
                activity!!.finish()
            }.addOnFailureListener {
                Log.e("Update Product Image","${it.message}")
            }
    }


    @SuppressLint("Range")
    private fun getFileName(uri: Uri): String { // for image names
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor: Cursor? = requireContext().contentResolver.query(uri, null, null, null, null)

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

    override fun onItemClick(position: Int) {
        uriList.removeAt(position)
        nameList.removeAt(position)
        adapterUpload.notifyItemRemoved(position)
    }


}