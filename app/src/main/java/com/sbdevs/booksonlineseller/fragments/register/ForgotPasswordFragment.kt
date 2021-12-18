package com.sbdevs.booksonlineseller.fragments.register


import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.FragmentForgotPasswordBinding


class ForgotPasswordFragment : Fragment() {

    private var _binding:FragmentForgotPasswordBinding ?=null
    private val binding get() = _binding!!

    private val firebaseFirestore = Firebase.firestore
    val firebaseAuth = Firebase.auth
    private val currentUser = firebaseAuth.currentUser
    private val emailPattern = "[a-zA-Z0-9._-]+@[a-z]+.[a-z]+"

    lateinit var recoveryMassege:TextView
    lateinit var progress: ProgressBar
    lateinit var emailEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {

        _binding = FragmentForgotPasswordBinding.inflate(inflater,container,false)
        recoveryMassege = binding.textView46
        progress = binding.recoveryProgressBar


        emailEditText = binding.recoveryEmailAddress


        binding.linearLayout2.setOnClickListener {
            validateEmail()
        }

        return binding.root
    }

    fun validateEmail(){
        val email:String =  emailEditText.text.toString().trim()
        if (email.isEmpty()){
            emailEditText.backgroundTintList = ContextCompat.getColorStateList(context!!, R.color.red_a700)
            emailEditText.requestFocus()
        } else {
            Toast.makeText(context,email,Toast.LENGTH_SHORT).show()
            if(email.matches(emailPattern.toRegex())){
                progress.visibility =View.VISIBLE
                emailEditText.backgroundTintList = ContextCompat.getColorStateList(context!!, R.color.purple_500)
                forgotPassWord(email)

            }else{
                emailEditText.backgroundTintList = ContextCompat.getColorStateList(context!!, R.color.red_a700)
                emailEditText.requestFocus()
            }

        }
    }


    fun forgotPassWord(email:String){

        firebaseAuth.sendPasswordResetEmail(email)
            .addOnCompleteListener {
                if (it.isSuccessful){
                    progress.visibility =View.GONE
                    recoveryMassege.visibility = View.VISIBLE
                    Toast.makeText(context,"Successful",Toast.LENGTH_SHORT).show()
                }else{
                    progress.visibility =View.GONE
//                    recoveryMassege.visibility = View.VISIBLE
//                    recoveryMassege.text = "Faild to send email"
//                    recoveryMassege.setDrawa
                    Toast.makeText(context,"Fail",Toast.LENGTH_SHORT).show()
                }


            }
    }



}