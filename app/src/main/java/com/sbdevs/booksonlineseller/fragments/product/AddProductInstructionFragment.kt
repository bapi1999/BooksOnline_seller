package com.sbdevs.booksonlineseller.fragments.product

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase

import kotlin.collections.ArrayList

import com.google.firebase.storage.ktx.storage
import com.sbdevs.booksonlineseller.adapters.NewUploadImageAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentAddProductInstructionBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog


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
    lateinit var adapterNewUpload: NewUploadImageAdapter
    private var downloadUriList:MutableList<String> = ArrayList()

    private val loadingDialog = LoadingDialog()
    private lateinit var docname:String

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        _binding = FragmentAddProductInstructionBinding.inflate(inflater, container, false)


        return binding.root
    }




}