package com.sbdevs.booksonlineseller.fragments.register

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentNewSellerRegisterBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class NewSellerRegisterFragment : Fragment() {
    private var _binding:FragmentNewSellerRegisterBinding? = null
    private val binding get() = _binding!!

    private lateinit var termAndPolicyBox: CheckBox
    private lateinit var privacyPolicyBox:CheckBox
    private lateinit var returnPolicyBox:CheckBox

    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private val loadingDialog = LoadingDialog()



    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentNewSellerRegisterBinding.inflate(inflater,container,false)

        termAndPolicyBox = binding.checkBox6
        privacyPolicyBox = binding.checkBox8
        returnPolicyBox = binding.checkBox9

        binding.button4.setOnClickListener {
            loadingDialog.show(childFragmentManager,"show")
            checkAllBox()
        }
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        termAndPolicyBox.setOnCheckedChangeListener { buttonView, isChecked ->
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.teal_200)
        }

        privacyPolicyBox.setOnCheckedChangeListener { buttonView, isChecked ->
            privacyPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.teal_200)
        }

        returnPolicyBox.setOnCheckedChangeListener { buttonView, isChecked ->
            returnPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.teal_200)
        }
    }

    private fun checkTermsAndPolicyBox(): Boolean {
        return if (termAndPolicyBox.isChecked) {
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.teal_200) //ColorStateList.valueOf(R.color.red_500)
            true
        } else {
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)
            false
        }
    }

    private fun checkPrivacyPolicyBox(): Boolean {
        return if (privacyPolicyBox.isChecked) {
            privacyPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.teal_200) //ColorStateList.valueOf(R.color.red_500)
            true
        } else {
            privacyPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)
            false
        }
    }
    private fun checkReturnPolicyBox(): Boolean {
        return if (returnPolicyBox.isChecked) {
            returnPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.teal_200) //ColorStateList.valueOf(R.color.red_500)
            true
        } else {
            returnPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)
            false
        }
    }

    private fun checkAllBox (){
        if (!checkTermsAndPolicyBox() or !checkPrivacyPolicyBox() or !checkReturnPolicyBox()){
            Toast.makeText(requireContext(),"check all box",Toast.LENGTH_SHORT).show()
            loadingDialog.dismiss()
        }else{
            lifecycleScope.launch(Dispatchers.IO) {
                createPaths()

            }

        }
    }

    private suspend  fun createPaths(){


        val timstamp1 = FieldValue.serverTimestamp()

        val userMap: MutableMap<String, Any> = HashMap()
        userMap["Is_seller"] = true
        userMap["seller_register_date"] = timstamp1
        userMap["TotalSeals"] = 0L
        userMap["TotalProfit"] = 0L
        userMap["OrdersDelivered"] = 0L
        userMap["OrdersCanceled"] = 0L
        userMap["new_notification_seller"] = timstamp1

        userMap["LastDeliveredOrderTime"] = timstamp1
        userMap["LastCanceledOrderTime"] = timstamp1
        userMap["LastProductAddedTime"] = timstamp1
        userMap["LastTimeSealsChecked"] = timstamp1

        val earningMap: MutableMap<String, Any> = HashMap()
        earningMap["current_amount"] = 0L


        val businessDetailsMap: MutableMap<String, Any> = HashMap()
        businessDetailsMap["Business_name"] = ""
        businessDetailsMap["Business_type"] = ""
        businessDetailsMap["Is_BusinessDetail_Added"] = false
        businessDetailsMap["is_address_verified"] = false

        val bankDetailsMap: MutableMap<String, Any> = HashMap()
        bankDetailsMap["UPI_id"] =""
        bankDetailsMap["Is_BankDetail_Added"] = false

        val welcomeNoti: String = getString(R.string.welcome_notification_for_seller)
        val notificationMap: MutableMap<String, Any> = HashMap()
        notificationMap["date"] = FieldValue.serverTimestamp()
        notificationMap["description"] = welcomeNoti
        notificationMap["image"] = ""
        notificationMap["NOTIFICATION_CODE"]=0L
        notificationMap["order_id"] = ""
        notificationMap["seen"] = false

        val currentUser = firebaseAuth.currentUser!!.uid

        firebaseFirestore.collection("USERS").document(currentUser).update(userMap).await()

        val docRef = firebaseFirestore.collection("USERS").document(currentUser)

        docRef.set(userMap).await()
        docRef.collection("SELLER_NOTIFICATIONS").add(notificationMap).await()

        val sellerRef = docRef.collection("SELLER_DATA")
        sellerRef.document("BANK_DETAILS").set(bankDetailsMap).await()
        sellerRef.document("BUSINESS_DETAILS").set(businessDetailsMap).await()
        sellerRef.document("MY_EARNING").set(earningMap).await()

        withContext(Dispatchers.Main){
            Toast.makeText(context, "Successfully Registered", Toast.LENGTH_SHORT).show()

            val action = NewSellerRegisterFragmentDirections.actionNewSellerRegisterFragmentToAddBusinessDetailsFragment(null)
            findNavController().navigate(action)
            loadingDialog.dismiss()
        }


    }


}