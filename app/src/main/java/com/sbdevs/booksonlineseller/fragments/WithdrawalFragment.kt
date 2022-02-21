package com.sbdevs.booksonlineseller.fragments

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.adapters.PaymentRequestAdapter
import com.sbdevs.booksonlineseller.databinding.FragmentWithdrawalBinding
import com.sbdevs.booksonlineseller.models.PaymentRequestModel
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.util.*
import kotlin.collections.ArrayList
import kotlin.collections.HashMap

class WithdrawalFragment : Fragment() {

    private var _binding:FragmentWithdrawalBinding? = null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var amounrInput:TextInputLayout
    private lateinit var withdrawBtn:Button

    private val args:WithdrawalFragmentArgs by navArgs()
    private var accountBalance:Int = 0

    private var upiAvailable = false

    private val gone = View.GONE
    private val visible = View.VISIBLE
    private val loadingDialog = LoadingDialog()
    private var st = ""
    private var upiId=""
    private var qrCode = ""
    private lateinit var requestMessage:TextView
    private lateinit var recyclerViewNew:RecyclerView
    private lateinit var recyclerViewOld:RecyclerView

    private var listNew:MutableList<PaymentRequestModel> = ArrayList()
    private var listOld:MutableList<PaymentRequestModel> = ArrayList()

    private  var newRequestAdapter:PaymentRequestAdapter = PaymentRequestAdapter(listNew)
    private  var oldRequestAdapter:PaymentRequestAdapter = PaymentRequestAdapter(listOld)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentWithdrawalBinding.inflate(inflater, container, false)

        getBankDetails()
        getPaymentRequest()
        accountBalance = args.accountBalance
        binding.lay2.accountBalanceText.text = accountBalance.toString()
        withdrawBtn = binding.lay2.withdrawBtn
        requestMessage = binding.lay2.requestText
        amounrInput = binding.lay2.amountInputLayout
        withdrawBtn = binding.lay2.withdrawBtn
        recyclerViewOld = binding.lay3.paymentOldRecycler
        recyclerViewNew = binding.lay3.paymentNewRecycler

        if (accountBalance <= 0 ){
            withdrawBtn.isEnabled = false
            withdrawBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.grey_500)
            binding.lay2.errorMessageText.visibility = visible
            st="Problem in fetching account balance"
            binding.lay2.errorMessageText.text = st

        }else{

            withdrawBtn.isEnabled = true
            withdrawBtn.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.purple_500)
            binding.lay2.errorMessageText.visibility = gone

        }

        amounrInput.editText?.setText(accountBalance.toString())

        recyclerViewOld.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewOld.adapter = oldRequestAdapter

        recyclerViewNew.layoutManager = LinearLayoutManager(requireContext())
        recyclerViewNew.adapter = newRequestAdapter


        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.lay1.addNewUpi.setOnClickListener {

            val action = WithdrawalFragmentDirections.actionWithdrawalFragmentToAddBankDetailsFragment()
            findNavController().navigate(action)

        }

        withdrawBtn.setOnClickListener {

            checkInput()

        }
    }

    private fun checkInput(){
        val amountString: String = amounrInput.editText?.text.toString().trim()
        if (amountString.isEmpty()) {
            amounrInput.error = "Field can't be empty"

        } else {
            when {
                amountString.toInt() < 200 -> {
                    amounrInput.error = "minimum withdrawal amount Rs.200"

                }
                amountString.toInt() > accountBalance -> {
                    amounrInput.error = "greater then account balance"

                }
                else -> {

                    amounrInput.error = null
                    if (upiAvailable){
                        loadingDialog.show(childFragmentManager,"show")
                        sendPaymentRequest()
                    }else{
                        st="\n UPI id not found"
                        binding.lay2.errorMessageText.text = st
                        binding.lay2.errorMessageText.visibility = visible
                    }


                }
            }

        }
    }



    private fun getBankDetails(){
        firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("BANK_DETAILS").get()
            .addOnSuccessListener {
                upiAvailable = it.getBoolean("Is_BankDetail_Added")!!

                if (upiAvailable){
                    binding.lay1.upiContainer.visibility = visible
                    binding.lay1.addNewUpi.visibility = gone
                    binding.lay1.warningBankText.visibility =gone

                    upiId = it.getString("UPI_id").toString()
                    qrCode = it.getString("UPI_qrCode").toString().trim()
                    binding.lay1.uipIdText.text = upiId


                }else{

                    binding.lay1.upiContainer.visibility = gone
                    binding.lay1.addNewUpi.visibility = visible
                    binding.lay1.warningBankText.visibility =visible
                }
            }
    }


    private fun sendPaymentRequest(){
        //Toast.makeText(requireContext(),"request sent",Toast.LENGTH_SHORT).show()

        //PAYMENT_REQUEST
        val amountString: String = amounrInput.editText?.text.toString().trim()

        val requestMap:MutableMap<String,Any> = HashMap()
        requestMap["UPI_ID"] = upiId
        requestMap["UPI_QR_CODE"] = qrCode
        requestMap["amount"] = amountString.toLong()
        requestMap["is_paid"] = false
        requestMap["seller_id"] = user!!.uid
        requestMap["time"] = FieldValue.serverTimestamp()

        firebaseFirestore.collection("PAYMENT_REQUEST").add(requestMap)
            .addOnSuccessListener{
                requestMessage.visibility = visible
                requestMessage.text = "Rs.$amountString will be added to your bank within 24 hours"
                requestMessage.setTextColor(AppCompatResources.getColorStateList(requireContext(),R.color.indigo_700))
                updateAccountBalance(amountString.toLong())
                accountBalance-=amountString.toInt()
                binding.lay2.accountBalanceText.text = accountBalance.toString()
            }
            .addOnFailureListener {
                Log.e("sending payment request error","${it.message}")
                requestMessage.visibility = visible
                requestMessage.text = "Failed to send payment request"
                requestMessage.setTextColor(AppCompatResources.getColorStateList(requireContext(),R.color.red_700))
                loadingDialog.dismiss()
            }




    }


    private fun updateAccountBalance(withdrawalBalance:Long){
        val newmap:MutableMap<String,Any> = HashMap()
        newmap["current_amount"] =  accountBalance.toLong() - withdrawalBalance

        firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("MY_EARNING")
            .update(newmap).addOnSuccessListener {
                loadingDialog.dismiss()
                listNew.add(PaymentRequestModel(false, Date(),withdrawalBalance))
                newRequestAdapter.notifyItemInserted(listNew.size-1)

                binding.lay3.newPayContainer.visibility =visible

            }.addOnFailureListener {
                //crashlitics
            }

    }

    private fun getPaymentRequest(){
        firebaseFirestore.collection("PAYMENT_REQUEST")
            .whereEqualTo("seller_id",user!!.uid)
            .whereEqualTo("is_paid",false)
            .orderBy("time").limit(10L)
            .get()
            .addOnSuccessListener {
                val alldocument = it.documents

                listOld = it.toObjects(PaymentRequestModel::class.java)
                if (listOld.isEmpty()){
                    binding.lay3.oldPayContainer.visibility =gone
                }else{
                    binding.lay3.oldPayContainer.visibility =visible
                    oldRequestAdapter.list = listOld
                    oldRequestAdapter.notifyDataSetChanged()
                }


            }
            .addOnFailureListener {
                Log.e("error","${it.message}")
            }
    }


}