package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentWithdrawalBinding
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime

class WithdrawalFragment : Fragment() {

    private var _binding:FragmentWithdrawalBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentWithdrawalBinding.inflate(inflater, container, false)




        return binding.root
    }



}