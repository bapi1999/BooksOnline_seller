package com.sbdevs.booksonlineseller.activities

import android.app.Activity
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.CheckBox
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
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
                createPaths()

            }

        }
    }

    private suspend  fun createPaths(){


        val timstamp1 = FieldValue.serverTimestamp()

        val userMap: MutableMap<String, Any> = HashMap()
        userMap["Is_seller"] = true
        userMap["seller_register_date"] = timstamp1

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

        val currentUser = firebaseAuth.currentUser!!.uid

        firebaseFirestore.collection("USERS").document(currentUser).update(userMap).await()

        val docRef = firebaseFirestore.collection("USERS")
            .document(currentUser).collection("SELLER_DATA")

        docRef.document("BANK_DETAILS").set(bankDetailsMap).await()
        docRef.document("BUSINESS_DETAILS").set(businessDetailsMap).await()
        docRef.document("MY_EARNING").set(earningMap).await()

        withContext(Dispatchers.Main){
            Toast.makeText(this@SellerRegisterActivity, "Successfully Registered", Toast.LENGTH_SHORT).show()

            val mainintent = Intent(this@SellerRegisterActivity, MainActivity::class.java)
            startActivity(mainintent)
            finish()
            loadingDialog.dismiss()
        }


    }
}