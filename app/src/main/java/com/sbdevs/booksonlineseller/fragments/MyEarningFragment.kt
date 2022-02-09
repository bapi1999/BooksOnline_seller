package com.sbdevs.booksonlineseller.fragments

import android.content.Context
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.adapters.EarningAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentMyEarningBinding
import com.sbdevs.booksonlineseller.models.EarningModel
import com.sbdevs.booksonlineseller.models.OrderModel
import com.squareup.okhttp.Dispatcher
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap


class MyEarningFragment : Fragment() {
    private var _binding: FragmentMyEarningBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser
    private val loadingDialog = LoadingDialog()
    private var orderList: MutableList<EarningModel> = ArrayList()
    private lateinit var recyclerView: RecyclerView
    private val earningAdapter = EarningAdapter(orderList)
    private lateinit var accountBalanceText: TextView
    private lateinit var upcomingPaymentText: TextView
    private var accountBalance = 0L
    private lateinit var withdrawalBtn:Button
    private val gone = View.GONE
    private val visible = View.VISIBLE

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMyEarningBinding.inflate(inflater, container, false)
        recyclerView = binding.earningRecycler
        recyclerView.layoutManager = LinearLayoutManager(requireContext())


        loadingDialog.show(childFragmentManager, "show")
        getAccountBalance()

        getDeliveredProduct()

        accountBalanceText = binding.lay1.accountBalanceText
        upcomingPaymentText = binding.lay1.upcomingPaymentText
        withdrawalBtn = binding.lay1.withdrawalBtn


        recyclerView.adapter = earningAdapter


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        withdrawalBtn.setOnClickListener {
            if (accountBalance>=500){
                val action = MyEarningFragmentDirections.actionMyEarningFragmentToWithdrawalFragment()
                findNavController().navigate(action)
            }else{
                binding.lay1.errorMessageText.visibility = visible
                binding.lay1.errorMessageText.text = "Minimum balance to withdraw is rs.500"
            }

        }


    }

    private fun getDeliveredProduct() {

        firebaseFirestore.collection("ORDERS")
            .whereEqualTo("ID_Of_SELLER", user!!.uid)
            .whereEqualTo("status", "delivered")
//            .whereEqualTo("eligible_for_credit",false)
            .whereEqualTo("already_credited", false)
            .orderBy("Time_delivered", Query.Direction.ASCENDING)
            .limit(10L).get().addOnSuccessListener {
                val allDocumentSnapshot = it.documents

                orderList = it.toObjects(EarningModel::class.java)

                if (orderList.isEmpty()) {
                    Toast.makeText(requireContext(), "List is empty", Toast.LENGTH_SHORT).show()
                    upcomingPaymentText.text = "no payment found"
                } else {
                    earningAdapter.list = orderList
                    earningAdapter.notifyDataSetChanged()

                    upcomingPaymentText.text =
                        "Rs.${orderList[0].PRICE_SELLING_TOTAL}/-  will be added in next ${
                            durationFromNow(
                                orderList[0].Time_delivered!!,
                                orderList[0].Time_period
                            )
                        }"
                }

                loadingDialog.dismiss()
            }.addOnFailureListener {
                Log.e("Load orders", "${it.message}")
                loadingDialog.dismiss()

            }
    }

    private fun getAccountBalance() {
        firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("PAYMENT_REQUESTS")
            .addSnapshotListener { value, error ->
                error?.let {
                    Log.e("Get Account balanceError", "${it.message}")
                    return@addSnapshotListener
                }
                value?.let {
                    accountBalance = it.getLong("current_amount")!!.toLong()
                    accountBalanceText.text = accountBalance.toString()
                }
            }
    }

    private fun compairQuery() {
        //THIS IS FOR BACKEND
        val query1 = firebaseFirestore.collection("ORDERS")
            .whereEqualTo("ID_Of_SELLER", user!!.uid)
            .whereEqualTo("status", "delivered")
            .whereEqualTo("eligible_for_credit", true)
            .whereEqualTo("already_credited", false)
            .orderBy("Time_delivered", Query.Direction.ASCENDING)
            .limit(10L)

        //FOR FRONT END
        val query2 = firebaseFirestore.collection("ORDERS")
            .whereEqualTo("ID_Of_SELLER", user!!.uid)
            .whereEqualTo("status", "delivered")
            //.whereEqualTo("eligible_for_credit",false)
            .whereEqualTo("already_credited", false)
            .orderBy("Time_delivered", Query.Direction.ASCENDING)
            .limit(10L)


    }

    private fun durationFromNow(timeDelivered: Date, timePeriod: Long): String {

//            val days7lay = Date(timeDelivered!!.time +(1000 * 60 * 60 * 24*timePeriod))

        val days7lay = Date(timeDelivered!!.time + (1000 * 60 * 60 * 24))
        val cal = Calendar.getInstance()
        cal.time = days7lay
        cal[Calendar.HOUR_OF_DAY] = 23
        cal[Calendar.MINUTE] = 59
        cal[Calendar.SECOND] = 50
        cal[Calendar.MILLISECOND] = 0
        val dd: Date = cal.time

        val difDate = Date(dd.time - Date().time)

        var different: Long = difDate.time

        val secondsInMilli: Long = 1000
        val minutesInMilli = secondsInMilli * 60
        val hoursInMilli = minutesInMilli * 60
        val daysInMilli = hoursInMilli * 24


        val elapsedDays = different / daysInMilli
        different %= daysInMilli
        val elapsedHours = different / hoursInMilli
        different %= hoursInMilli
        val elapsedMinutes = different / minutesInMilli
        different %= minutesInMilli
        val elapsedSeconds = different / secondsInMilli
        var output = ""
        if (elapsedDays > 0) output += elapsedDays.toString() + "days "
        if (elapsedDays > 0 || elapsedHours > 0) output += "$elapsedHours hours "
        if (elapsedHours > 0 || elapsedMinutes > 0) output += "$elapsedMinutes minutes "
//        if (elapsedMinutes > 0 || elapsedSeconds > 0) output += "$elapsedSeconds seconds"
        return output
    }


}