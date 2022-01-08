package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentMyEarningBinding
import com.squareup.okhttp.Dispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime


class MyEarningFragment : Fragment() {
    private var _binding:FragmentMyEarningBinding? =null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMyEarningBinding.inflate(inflater, container, false)

        binding.lay1.withdrawalBtn.setOnClickListener {
            val action = MyEarningFragmentDirections.actionMyEarningFragmentToWithdrawalFragment()
            findNavController().navigate(action)

//            lifecycleScope.launch(Dispatchers.IO) {
//                create30Notification()
//            }
        }

        return binding.root
    }

    private suspend fun create30Notification(){
        for(i in 0..30){
            val timeString = LocalDateTime.now().toString()

            val notificationMap: MutableMap<String, Any> = HashMap()
            notificationMap["date"] = FieldValue.serverTimestamp()
            notificationMap["description"] = "welcomeNotification $i-th $timeString"
            notificationMap["image"] = ""
            notificationMap["order_id"] = ""
            notificationMap["seen"] = false


            firebaseFirestore.collection("USERS")
                .document(user!!.uid)
                .collection("SELLER_DATA")
                .document("SELLER_DATA")
                .collection("NOTIFICATION")
                .add(notificationMap)
                .addOnSuccessListener { Log.i("Notification","Successfully added") }
                .addOnFailureListener { Log.e("Notification","${it.message}") }.await()
        }
    }

}