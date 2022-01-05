package com.sbdevs.booksonlineseller.fragments.register


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
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
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
    lateinit var phone:TextInputLayout
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
        phone = binding.signupLay.mobileInput
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


    private fun checkPhone(): Boolean {
        val phoneInput: String = phone.editText?.text.toString().trim()
        return if (phoneInput.isEmpty()) {
            phone.isErrorEnabled = true
            phone.error = "Field can't be empty"
            false
        } else {
            if(phoneInput.length==10){
                phone.error = null
                true
            }else{
                phone.isErrorEnabled = true
                phone.error = "Must be 10 digit number"
                false
            }

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

        val container = binding.signupLay.linearLayout11

        return if (termAndPolicyBox.isChecked) {
            container.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.white)
            true
        } else {
            container.backgroundTintList = AppCompatResources.getColorStateList(requireContext(),R.color.red_500)
            false
        }
    }

    private fun checkAllDetails() {
        if (!checkMail() or !checkPhone() or !checkPassword() or !checkConfirmPassword() or !checkTermsAndPolicyBox()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            loadingDialog.dismiss()
            return
        } else {
            lifecycleScope.launch(Dispatchers.IO){
                try {
                    firebaseAuth.createUserWithEmailAndPassword(email.editText?.text.toString().trim(),pass.editText?.text.toString()).await()

                    delay(1000L)

                    withContext(Dispatchers.IO){
                        createPaths()
                    }


                }catch (e:Exception){
                    withContext(Dispatchers.Main){
                        errorTxt.visibility = View.VISIBLE
                        Toast.makeText(context,e.message, Toast.LENGTH_LONG).show()
                        loadingDialog.dismiss()
                    }
                }
            }

        }
    }
    private suspend  fun createPaths(){


        val timstamp1 = FieldValue.serverTimestamp()

        val userMap: MutableMap<String, Any> = HashMap()
        userMap["name"] = ""
        userMap["email"] = email.editText?.text.toString().trim()
        userMap["Is_user"] = true
        userMap["Is_seller"] = true
        userMap["signup_date"] = timstamp1
        userMap["mobile_No"] = phone.editText?.text.toString().trim()
        userMap["profile"] = ""


        val sellerDataMap: MutableMap<String, Any> = HashMap()
        sellerDataMap["new_notification"] = timstamp1

        val listSizeMap: MutableMap<String, Any> = HashMap()
        listSizeMap["listSize"] = 0L

        val businessDetailsMap: MutableMap<String, Any> = HashMap()
        businessDetailsMap["Business_name"] = ""
        businessDetailsMap["Business_type"] = ""
        businessDetailsMap["Is_BusinessDetail_Added"] = false


        val bankDetailsMap: MutableMap<String, Any> = HashMap()
        bankDetailsMap["Bank_account_number"] = ""
        bankDetailsMap["Bank_ifsc_code"] = ""
        bankDetailsMap["UPI_Type"] = ""
        bankDetailsMap["UPI_id"] =""
        bankDetailsMap["Is_BankDetail_Added"] = false


        val welcomeNoti: String = getString(R.string.welcome_notification_for_seller)







            if (firebaseAuth.currentUser!=null){
                val currentUser = firebaseAuth.currentUser!!.uid

                firebaseFirestore.collection("USERS").document(currentUser).set(userMap).await()


                val docRef = firebaseFirestore.collection("USERS")
                    .document(currentUser).collection("SELLER_DATA")

                docRef.document("BANK_DETAILS").set(bankDetailsMap).await()
                docRef.document("BUSINESS_DETAILS").set(businessDetailsMap).await()
                docRef.document("PAYMENT_REQUESTS").set(listSizeMap).await()

                val sellerRef = docRef.document("SELLER_DATA")
                sellerRef.set(sellerDataMap).await()

                val notificationMap: MutableMap<String, Any> = HashMap()
                notificationMap["date"] = FieldValue.serverTimestamp()
                notificationMap["description"] = welcomeNoti
                notificationMap["image"] = ""
                notificationMap["order_id"] = ""
                notificationMap["seen"] = false


                sellerRef.collection("NOTIFICATION").add(notificationMap)
                    .addOnSuccessListener { Log.i("Notification","Successfully added") }
                    .addOnFailureListener { Log.e("Notification","${it.message}") }.await()


//                SELLER_DATA ->document

//todo                sub collections
//                    1)ORDERS
//                    2)NOTIFICATION
//                    3)SALES_REPORT
//                    4)TRANSACTION_HISTORY

//                sub collections will be created automatically when a document added

                withContext(Dispatchers.Main){
                    Toast.makeText(context, "Successfully login", Toast.LENGTH_SHORT).show()

                    val action = SignUpFragmentDirections.actionSignUpFragmentToAddBusinessDetailsFragment(null)
                    findNavController().navigate(action)
                    loadingDialog.dismiss()
                }
            }





    }



}