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

import kotlin.collections.ArrayList

import com.google.firebase.storage.StorageReference
import com.google.firebase.storage.ktx.storage
import com.sbdevs.booksonlineseller.adapters.UploadImageAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentAddProductInstructionBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.collections.HashMap


class AddProductInstructionFragment : Fragment() {
    private var _binding:FragmentAddProductInstructionBinding? = null

    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private val storage = Firebase.storage
    private val storageReference = storage.reference


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
        _binding = FragmentAddProductInstructionBinding.inflate(inflater, container, false)


        return binding.root
    }




}