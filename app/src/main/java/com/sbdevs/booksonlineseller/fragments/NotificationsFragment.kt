package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.adapters.NotificationAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentNotificationsBinding
import com.sbdevs.booksonlineseller.models.NotificationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class NotificationsFragment : Fragment() {
    private var _binding: FragmentNotificationsBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var notificationAdapter: NotificationAdapter
    private var notificationList:List<NotificationModel> = ArrayList()

    var notificationDocIdList:ArrayList<String> = ArrayList()

    private val loadingDialog  = LoadingDialog()


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        loadingDialog.show(childFragmentManager,"show")


        if (user != null){
            viewLifecycleOwner.lifecycleScope.launch {
                withContext(Dispatchers.IO){
                    getNotificationFormDB()
                }
                withContext(Dispatchers.Main){
                    delay(1000)
                    loadingDialog.dismiss()
                }


            }
        }else{
            binding.emptyContainer.visibility = View.VISIBLE
            binding.notificationRecycler.visibility = View.GONE
            loadingDialog.dismiss()
        }




        val recyclerView = binding.notificationRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationAdapter = NotificationAdapter(notificationList,notificationDocIdList)
        recyclerView.adapter = notificationAdapter

        return binding.root
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getNotificationFormDB(){
        firebaseFirestore.collection("USERS").document(user!!.uid)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("NOTIFICATION")
            .orderBy("date", Query.Direction.DESCENDING)
            .get().addOnSuccessListener {
                val allDocumentSnapshot = it.documents
                for (item in allDocumentSnapshot){
                    notificationDocIdList.add(item.id)

                }
                notificationAdapter.docNameList = notificationDocIdList
                notificationList = it.toObjects(NotificationModel::class.java)
                if (notificationList.isEmpty()){
                    binding.emptyContainer.visibility = View.VISIBLE
                    binding.notificationRecycler.visibility = View.GONE
                }else{
                    binding.emptyContainer.visibility = View.GONE
                    binding.notificationRecycler.visibility = View.VISIBLE

                    notificationAdapter.list = notificationList
                    notificationAdapter.notifyDataSetChanged()
                }


            }.addOnFailureListener{
                Log.e("NotificationFragment","${it.message}")
                binding.emptyContainer.visibility = View.VISIBLE
                binding.notificationRecycler.visibility = View.GONE
            }

    }

}