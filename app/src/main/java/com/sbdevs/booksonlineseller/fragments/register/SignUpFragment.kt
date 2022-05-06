package com.sbdevs.booksonlineseller.fragments.register


import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.activities.AddBusinessDetailsActivity
import com.sbdevs.booksonlineseller.activities.MainActivity
import com.sbdevs.booksonlineseller.activities.PoliciesActivity
import com.sbdevs.booksonlineseller.databinding.FragmentSignUpBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext

class SignUpFragment : Fragment() {
    private var _binding: FragmentSignUpBinding? = null
    private val binding get() = _binding!!
    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth

    private lateinit var email: TextInputLayout
    lateinit var userName:TextInputLayout
    lateinit var pass: TextInputLayout
    lateinit var confirmPass:TextInputLayout
    private lateinit var termAndPolicyBox:CheckBox
    lateinit var errorTxt: TextView
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+"

    private val loadingDialog = LoadingDialog()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSignUpBinding.inflate(inflater, container, false)

        email = binding.signupLay.emailInput
        userName = binding.signupLay.userNameInput
        pass = binding.signupLay.passwordInput
        confirmPass = binding.signupLay.confirmPassInput
        termAndPolicyBox = binding.signupLay.checkBox6

        binding.signupLay.loginText.setOnClickListener {
            val action = SignUpFragmentDirections.actionSignUpFragmentToLoginFragment()
            findNavController().navigate(action)
        }

        binding.signupLay.signupBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager,"show")
            checkAllDetails()

        }

        termAndPolicyBox.setOnCheckedChangeListener { buttonView, isChecked ->
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.teal_200)
        }


        binding.signupLay.termConditionText.setOnClickListener {
            val myIntent = Intent(requireContext(), PoliciesActivity::class.java)
            myIntent.putExtra("PolicyCode",1)// 1 = Terms and services
            startActivity(myIntent)
        }
        binding.signupLay.privacyPolicyText.setOnClickListener {
            val myIntent = Intent(requireContext(), PoliciesActivity::class.java)
            myIntent.putExtra("PolicyCode",2)// 2 = Privacy Policy
            startActivity(myIntent)
        }
        binding.signupLay.returnPolicyText.setOnClickListener {
            val myIntent = Intent(requireContext(), PoliciesActivity::class.java)
            myIntent.putExtra("PolicyCode",3)//3 = Return Policy
            startActivity(myIntent)
        }

        return binding.root
    }

    override fun onResume() {
        super.onResume()

    }

    private fun checkMail(): Boolean {
        val emailInput: String = email.editText?.text.toString().trim()
        return if (emailInput.isEmpty()) {
            email.isErrorEnabled = true
            email.error = "Field can't be empty"
            false
        } else {
            if(emailInput.matches(emailPattern.toRegex())){

                email.error = null
                true
            }else{
                email.isErrorEnabled = true
                email.error = "Please enter a valid email address"
                false
            }

        }
    }


    private fun checkUserName(): Boolean {
        val userNameInput: String = userName.editText?.text.toString().trim()
        return if (userNameInput.isEmpty()) {
            userName.isErrorEnabled = true
            userName.error = "Field can't be empty"
            false
        } else {
            userName.error = null
            true
        }
    }


    private fun checkPassword(): Boolean {
        val passInput: String = pass.editText?.text.toString().trim()
        return if (passInput.isEmpty()) {
            pass.isErrorEnabled = true
            pass.error = "Field can't be empty"
            false
        } else {
            if (passInput.length<8){
                pass.isErrorEnabled = true
                pass.error = "must be at least 8 character"
                false
            }else{
                pass.error = null
                true
            }

        }
    }
    private fun checkConfirmPassword(): Boolean {
        val passInput: String = pass.editText?.text.toString().trim()
        val confirmPassInput: String = confirmPass.editText?.text.toString().trim()
        return if (passInput.isEmpty()) {
            confirmPass.isErrorEnabled = true
            confirmPass.error = "Field can't be empty"
            false
        } else {
            if (confirmPassInput == passInput){
                confirmPass.error = null
                true
            }else{
                confirmPass.isErrorEnabled = true
                confirmPass.error = "Doesn't match with password"
                false
            }

        }
    }

    private fun checkTermsAndPolicyBox(): Boolean {
        return if (termAndPolicyBox.isChecked) {
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.teal_200)
            true
        } else {
            termAndPolicyBox.buttonTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_700)
            false
        }
    }


    private fun checkAllDetails() {
        if (!checkMail() or !checkUserName() or !checkPassword()
            or !checkConfirmPassword() or !checkTermsAndPolicyBox() ) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            loadingDialog.dismiss()
            return
        } else {
            firebaseAuth.createUserWithEmailAndPassword(email.editText?.text.toString().trim(),pass.editText?.text.toString())
                .addOnSuccessListener {
                    Log.i("Creat User","user created successfully")
                    lifecycleScope.launch(Dispatchers.IO){
                        retrieveUserToken()
                        createPaths()
                    }

                }
                .addOnFailureListener {
                    Log.e("Create User","${it.message}")
                    binding.signupLay.errorMessageText.visibility = View.VISIBLE
                    binding.signupLay.errorMessageText.text = "${it.message}"
                    loadingDialog.dismiss()
                }


        }
    }


    private suspend  fun createPaths(){

        val timstamp1 = FieldValue.serverTimestamp()

        val userMap: MutableMap<String, Any> = HashMap()
        userMap["name"] = userName.editText?.text.toString().trim()
        userMap["email"] = email.editText?.text.toString().trim()
        userMap["Is_user"] = true
        userMap["Is_seller"] = true
        userMap["signup_date"] = timstamp1
        userMap["seller_register_date"] = timstamp1
        userMap["profile"] = ""
        userMap["new_notification_user"] = timstamp1

        userMap["TotalSeals"] = 0L
        userMap["TotalProfit"] = 0L
        userMap["OrdersDelivered"] = 0L
        userMap["OrdersCanceled"] = 0L
        userMap["new_notification_seller"] = timstamp1

        userMap["LastDeliveredOrderTime"] = timstamp1
        userMap["LastCanceledOrderTime"] = timstamp1
        userMap["LastProductAddedTime"] = timstamp1
        userMap["LastTimeSealsChecked"] = timstamp1



        val paymentMap: MutableMap<String, Any> = HashMap()
        paymentMap["current_amount"] = 0L

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
        //0 = non clickable

        val listSizeMap: MutableMap<String, Any> = HashMap()
        listSizeMap["listSize"] = 0L

        val addressMap: MutableMap<String, Any> = HashMap()
        addressMap["select_No"] = 0L

            if (firebaseAuth.currentUser!=null){

                val currentUser = firebaseAuth.currentUser!!.uid
                val docRef = firebaseFirestore.collection("USERS").document(currentUser)

                docRef.set(userMap).await()
                docRef.collection("USER_NOTIFICATIONS").add(listSizeMap).await()
                docRef.collection("SELLER_NOTIFICATIONS").add(notificationMap).await()

                val sellerRef = docRef.collection("SELLER_DATA")
                sellerRef.document("BANK_DETAILS").set(bankDetailsMap).await()
                sellerRef.document("BUSINESS_DETAILS").set(businessDetailsMap).await()
                sellerRef.document("MY_EARNING").set(paymentMap).await()

                val userRef =  docRef.collection("USER_DATA")
                userRef.document("MY_ADDRESSES").set(addressMap).await()
                userRef.document("MY_CART").set(listSizeMap).await()
                userRef.document("MY_WISHLIST").set(listSizeMap).await()



                withContext(Dispatchers.Main){
                    Toast.makeText(context, "Successfully Signup", Toast.LENGTH_SHORT).show()
                    loadingDialog.dismiss()
                    val intent = Intent(context, AddBusinessDetailsActivity::class.java)
                    startActivity(intent)
                    activity?.finish()
                }
            }

    }


    private fun retrieveUserToken(){

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token:String = task.result
                val userId:String = FirebaseAuth.getInstance().currentUser!!.uid

                FirebaseDatabase.getInstance().getReference("Seller_Tokens")
                    .child(userId)
                    .setValue(token)

            }

        }

    }


}