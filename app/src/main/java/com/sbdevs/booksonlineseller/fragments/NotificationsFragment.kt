package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.lifecycleScope
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.adapters.NotificationAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentNotificationsBinding
import com.sbdevs.booksonlineseller.models.MyProductModel
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
    private var notificationList:MutableList<NotificationModel> = ArrayList()

    private var notificationDocIdList:ArrayList<String> = ArrayList()
    private lateinit var recyclerView:RecyclerView

    private val loadingDialog  = LoadingDialog()

    private var lastResult: DocumentSnapshot? = null
    private lateinit var times: Timestamp
    private var isReachLast:Boolean = false


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentNotificationsBinding.inflate(inflater, container, false)

        loadingDialog.show(childFragmentManager,"show")


        if (user != null){
            viewLifecycleOwner.lifecycleScope.launch(Dispatchers.IO) {
                getNotificationFormDB()
            }
        }else{
            binding.emptyContainer.visibility = View.VISIBLE
            binding.notificationRecycler.visibility = View.GONE
            loadingDialog.dismiss()
        }




        recyclerView = binding.notificationRecycler
        recyclerView.layoutManager = LinearLayoutManager(context)

        notificationAdapter = NotificationAdapter(notificationList,notificationDocIdList)
        recyclerView.adapter = notificationAdapter

        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        recyclerView.addOnScrollListener(object : RecyclerView.OnScrollListener(){

            override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
                super.onScrollStateChanged(recyclerView, newState)


                if (!recyclerView.canScrollVertically(RecyclerView.FOCUS_DOWN) && recyclerView.scrollState == RecyclerView.SCROLL_STATE_IDLE) {
                    // end scrolling: do what you want here and after calling the function change the value of boolean

                    if (isReachLast){
                        Log.w("Query item","Last item is reached already")
                        binding.progressBar2.visibility = View.GONE

                    }else{
                        binding.progressBar2.visibility = View.VISIBLE

                        Log.e("last query", "${lastResult.toString()}")

                        getNotificationFormDB()
                    }

                }

            }

            override fun onScrolled(recyclerView: RecyclerView, dx: Int, dy: Int) {
                super.onScrolled(recyclerView, dx, dy)

            }

        })

    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    private fun getNotificationFormDB(){

        val query:Query = if (lastResult == null){
            firebaseFirestore.collection("USERS").document(user!!.uid)
                .collection("SELLER_NOTIFICATIONS")
                .orderBy("date", Query.Direction.DESCENDING)
        }else{
            firebaseFirestore.collection("USERS").document(user!!.uid)
                .collection("SELLER_NOTIFICATIONS")
                .orderBy("date", Query.Direction.DESCENDING)
                .startAfter(times)

        }

        query.limit(10).get().addOnSuccessListener {
            val allDocumentSnapshot = it.documents
            Toast.makeText(requireContext(),"${allDocumentSnapshot.size}",Toast.LENGTH_SHORT).show()
            if (allDocumentSnapshot.isNotEmpty()){
                isReachLast = allDocumentSnapshot.size != 10
                for (item in allDocumentSnapshot){
                    notificationDocIdList.add(item.id)

                }
                val lastR = allDocumentSnapshot[allDocumentSnapshot.size - 1]
                lastResult = lastR
                times = lastR.getTimestamp("date")!!

            }else{
                isReachLast = true

            }



            val resultList = it.toObjects(NotificationModel::class.java)
            notificationList.addAll(resultList)


            if (notificationList.isEmpty()){
                binding.emptyContainer.visibility = View.VISIBLE
                binding.notificationRecycler.visibility = View.GONE
            }else{
                binding.emptyContainer.visibility = View.GONE
                binding.notificationRecycler.visibility = View.VISIBLE
                notificationAdapter.docNameList = notificationDocIdList
                notificationAdapter.list = notificationList

                if (lastResult == null ){
                    notificationAdapter.notifyItemRangeInserted(0,resultList.size)
                }else{
                    notificationAdapter.notifyItemRangeInserted(notificationList.size-1,resultList.size)
                }


            }
            loadingDialog.dismiss()

            binding.progressBar2.visibility = View.GONE

        }.addOnFailureListener{
            Log.e("NotificationFragment","${it.message}")
            binding.emptyContainer.visibility = View.VISIBLE
            binding.notificationRecycler.visibility = View.GONE
            loadingDialog.dismiss()
        }

    }

}