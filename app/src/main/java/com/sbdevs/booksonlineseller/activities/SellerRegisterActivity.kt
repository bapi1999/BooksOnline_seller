package com.sbdevs.booksonlineseller.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.ActivitySellerRegisterBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SellerRegisterActivity : AppCompatActivity() {

    private lateinit var binding:ActivitySellerRegisterBinding

    private lateinit var termAndPolicyBox: CheckBox
    private lateinit var privacyPolicyBox: CheckBox
    private lateinit var returnPolicyBox: CheckBox

    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private val loadingDialog = LoadingDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySellerRegisterBinding.inflate(layoutInflater)
        setContentView(binding.root)

        termAndPolicyBox = binding.checkBox6
        privacyPolicyBox = binding.checkBox8
        returnPolicyBox = binding.checkBox9

        binding.button4.setOnClickListener {
            loadingDialog.show(supportFragmentManager,"show")
            checkAllBox()
        }
    }

    override fun onStart() {
        super.onStart()
        termAndPolicyBox.setOnCheckedChangeListener { buttonView, isChecked ->
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.teal_200)
        }

        privacyPolicyBox.setOnCheckedChangeListener { buttonView, isChecked ->
            privacyPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.teal_200)
        }

        returnPolicyBox.setOnCheckedChangeListener { buttonView, isChecked ->
            returnPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.teal_200)
        }
    }


    private fun checkTermsAndPolicyBox(): Boolean {
        return if (termAndPolicyBox.isChecked) {
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.teal_200)
            true
        } else {
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.red_700)
            false
        }
    }

    private fun checkPrivacyPolicyBox(): Boolean {
        return if (privacyPolicyBox.isChecked) {
            privacyPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.teal_200)
            true
        } else {
            privacyPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.red_700)
            false
        }
    }
    private fun checkReturnPolicyBox(): Boolean {
        return if (returnPolicyBox.isChecked) {
            returnPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.teal_200)
            true
        } else {
            returnPolicyBox.buttonTintList = AppCompatResources.getColorStateList(this,R.color.red_700)
            false
        }
    }

    private fun checkAllBox (){
        if (!checkTermsAndPolicyBox() or !checkPrivacyPolicyBox() or !checkReturnPolicyBox()){
            Toast.makeText(this,"check all box", Toast.LENGTH_SHORT).show()
            loadingDialog.dismiss()
        }else{
            lifecycleScope.launch(Dispatchers.IO) {
                retrieveUserToken()
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
            Toast.makeText(this@SellerRegisterActivity, "Successfully Registered", Toast.LENGTH_SHORT).show()

            val newIntent = Intent(this@SellerRegisterActivity,AddBusinessDetailsActivity::class.java)
            startActivity(newIntent)
            loadingDialog.dismiss()
        }


    }


    private fun retrieveUserToken(){

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token:String = task.result
                val userId:String = FirebaseAuth.getInstance().currentUser!!.uid

                FirebaseDatabase.getInstance().getReference("Seller_Tokens")
                    .child(userId)
                    .setValue(token).addOnSuccessListener {
                        Log.d("Token:", "saved")
                    }.addOnFailureListener {
                        Log.e("Token:", "${it.message}")
                    }

            }else{
                Log.e("error","${task.exception?.message}")
            }

        }

    }



}