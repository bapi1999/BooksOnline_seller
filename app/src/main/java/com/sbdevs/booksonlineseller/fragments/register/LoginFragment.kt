package com.sbdevs.booksonlineseller.fragments.register

import android.app.Dialog
import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.android.gms.tasks.OnCompleteListener
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.ktx.auth
import com.google.firebase.database.FirebaseDatabase
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.google.firebase.messaging.FirebaseMessaging
import com.google.firebase.messaging.ktx.messaging
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.activities.MainActivity
import com.sbdevs.booksonlineseller.databinding.FragmentLoginBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext


class LoginFragment : Fragment() {

    private var _binding: FragmentLoginBinding? =null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    private val firebaseAuth = Firebase.auth
    private val firebaseMessaging = Firebase.messaging

    lateinit var email: TextInputLayout
    lateinit var pass: TextInputLayout
    lateinit var errorTxt: TextView

    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+"

    private  val loadingDialog  = LoadingDialog()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentLoginBinding.inflate(inflater,container, false)

        errorTxt  = binding.loginLay.errorMessageText
        errorTxt.visibility =View.GONE
        email = binding.loginLay.emailInput
        pass = binding.loginLay.passwordInput



        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)


        binding.loginLay.signupText.setOnClickListener {
            val action  = LoginFragmentDirections.actionLoginFragmentToSignUpFragment()
            findNavController().navigate(action)
        }

        binding.loginLay.forgotPassword.setOnClickListener {
            val action1 = LoginFragmentDirections.actionLoginFragmentToForgotPasswordFragment()
            findNavController().navigate(action1)
        }


        binding.loginLay.loginBtn.setOnClickListener {
            loadingDialog.show(childFragmentManager,"show")
            checkAllDetails()
        }
    }

    private fun checkMail(): Boolean {
        val emailInput: String = email.editText?.text.toString().trim()
        return if (emailInput.isEmpty()) {
            email.isErrorEnabled = true
            email.error = "Field can't be empty"
            false
        } else {
            if(emailInput.matches(emailPattern.toRegex())){
                email.isErrorEnabled = false
                email.error = null
                true
            }else{
                email.isErrorEnabled = true
                email.error = "Please enter a valid email address"
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
                pass.isErrorEnabled = false
                pass.error = null
                true
            }

        }
    }


    private fun checkAllDetails() {
        if (!checkMail() or !checkPassword()) {
            Toast.makeText(context, "Please fill all fields", Toast.LENGTH_SHORT).show()
            loadingDialog.dismiss()
            return
        } else {

            firebaseAuth.signInWithEmailAndPassword(email.editText?.text.toString().trim(),pass.editText?.text.toString())
                .addOnSuccessListener {
                    retrieveUserToken()
                    checkIsSeller()

                }
                .addOnFailureListener {
                    loadingDialog.dismiss()
                    Log.e("Login user","error: ${it.message}")
                    errorTxt.visibility = View.VISIBLE
                }

        }
    }

    private fun checkIsSeller(){
        firebaseFirestore.collection("USERS")
            .document(firebaseAuth.currentUser!!.uid).get()
            .addOnSuccessListener {
                val isSeller:Boolean = it.getBoolean("Is_seller")!!
                if (isSeller){

                    loadingDialog.dismiss()
                    Toast.makeText(context, "Successfully login", Toast.LENGTH_SHORT).show()
                    val intent = Intent(context, MainActivity::class.java)
                    startActivity(intent)
                    activity?.finish()

                }else{
                    val action = LoginFragmentDirections.actionLoginFragmentToNewSellerRegisterFragment()
                    findNavController().navigate(action)
                    loadingDialog.dismiss()
                }
            }.addOnFailureListener {
                loadingDialog.dismiss()
                errorTxt.visibility = View.VISIBLE
                errorTxt.text = "Error: Can not fetch user details"
            }
    }

    private fun retrieveUserToken(){

        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (task.isSuccessful) {
                val token:String = task.result
                val userId:String = FirebaseAuth.getInstance().currentUser!!.uid

                FirebaseDatabase.getInstance().getReference("Tokens")
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
