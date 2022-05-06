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

    private var productImgList: ArrayList<String> = ArrayList()
    private lateinit var alreadyAddedAdapter: AlreadyUploadedImageAdapter
    private lateinit var alreadyAddedImgRecyclerView: RecyclerView

    private lateinit var newAddedAdapter: NewUploadImageAdapter
    private lateinit var newAddedImgRecyclerView: RecyclerView
    private var deleteedImageList: ArrayList<String> = ArrayList()

    private lateinit var updateMessageText: TextView

    private var fileUri: Uri? = null
    private var uriList: ArrayList<Uri> = ArrayList()
    var nameList: ArrayList<String> = ArrayList()
    private var downloadUriList: MutableList<String> = ArrayList()

    private lateinit var productId: String
    private var currentYear:Int = 0
    private var changeInPosition = false

    private val loadingDialog = LoadingDialog()
    private val gone = View.GONE
    private val visible = View.VISIBLE


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

        updateMessageText = binding.updateMessage

        alreadyAddedImgRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL, false
        )

        newAddedImgRecyclerView.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL, false
        )

        newAddedAdapter = NewUploadImageAdapter(uriList, this)



        productImgList = arguments?.getStringArrayList("image_list") as ArrayList<String>

        alreadyAddedAdapter = AlreadyUploadedImageAdapter(productImgList,this)

        alreadyAddedImgRecyclerView.adapter = alreadyAddedAdapter
        val itemTouchHelper = ItemTouchHelper(simpleCallback1)
        itemTouchHelper.attachToRecyclerView(alreadyAddedImgRecyclerView)

        currentYear = Year.now().value

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

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


        binding.lay4.selectImageBtn.setOnClickListener {
            ImagePicker.with(this)
                .crop()
                .compress(700)
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
    }


    private fun checkProductImage(): Boolean {
        val selectBtn = binding.lay4.selectImageBtn
        return if (uriList.isEmpty() and productImgList.isEmpty()) {
            selectBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.red_a700)
            binding.lay4.errorMessageText.visibility =visible
            binding.lay4.errorMessageText.text  = "No image found"
            selectBtn.requestFocus()
            false

        } else {

            if ((uriList.size + productImgList.size)>2){
                selectBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.red_a700)
                binding.lay4.errorMessageText.visibility =visible
                binding.lay4.errorMessageText.text = "Maximum 2 images only"
                selectBtn.requestFocus()
                false

            }else{
                binding.lay4.errorMessageText.visibility = gone
                selectBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(), R.color.grey_200)
                true

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
                            updateImagePosition(productId)
                            //Snackbar.make(v!!, "change position", Snackbar.LENGTH_SHORT).show()
                        }
                        !changeInPosition and uriList.isNotEmpty() -> {
                            uploadProductImage(productId)
                            //Snackbar.make(v!!, "new image added", Snackbar.LENGTH_SHORT).show()
                        }
                        else -> {
//                            Snackbar.make(v!!, "nothing change", Snackbar.LENGTH_SHORT).show()
                            Log.w("clicked", "nothing happened")
                            loadingDialog.dismiss()
                        }
                    }
                }
            }

        }
    }


    private suspend fun uploadProductImage(productID: String) {

        for (i in 0 until uriList.size) {
            val allRef: StorageReference =
                storageReference.child("image/" + user!!.uid + "/")
                    .child("products/")
                    .child(nameList[i])

            allRef.putFile(uriList[i])
                .addOnCompleteListener {
                    allRef.downloadUrl.addOnSuccessListener {
                        downloadUriList.add(it.toString())
                        when (i) {
                            (uriList.size - 1) -> {
                                viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                                    updateProductImage(productID, downloadUriList)
                                }

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


    private suspend fun updateProductImage(productID: String, urlList: MutableList<String>){

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
                    updateMessageText.setTextColor(AppCompatResources.getColorStateList(requireContext(), R.color.red_500))
                    loadingDialog.dismiss()
                }.await()
        }


    private fun updateImagePosition(productID: String) {

        val allMap: MutableMap<String, Any> = HashMap()
        allMap["productImage_List"] = productImgList

        changeInPosition = false

        firebaseFirestore.collection("PRODUCTS")
            .document(productID).update(allMap)
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

    override fun onOldImageDeleteClick(position: Int) {
        deleteedImageList.add(productImgList[position])
        changeInPosition = true
        productImgList.removeAt(position)
        alreadyAddedAdapter.notifyItemRemoved(position)
    }


    override fun onNewImageDeleteClick(position: Int) {
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