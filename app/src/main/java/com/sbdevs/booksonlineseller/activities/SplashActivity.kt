package com.sbdevs.booksonlineseller.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import androidx.navigation.fragment.findNavController
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.ActivitySplashBinding
import com.sbdevs.booksonlineseller.fragments.register.LoginFragmentDirections
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

    private val firebaseFirestore = Firebase.firestore
    val currentUser = Firebase.auth.currentUser
    private lateinit var binding:ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)
        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

    }

    override fun onStart() {
        super.onStart()

        if (currentUser == null) {
            val loginintent = Intent(this@SplashActivity, RegisterActivity::class.java)
            startActivity(loginintent)
            finish()
        } else {
            checkIsSeller()
        }

    }

    private fun checkIsSeller(){
        firebaseFirestore.collection("USERS")
            .document(currentUser!!.uid).get()
            .addOnSuccessListener {
                val isSeller:Boolean = it.getBoolean("Is_seller")!!
                if (isSeller){

                    val mainintent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(mainintent)
                    finish()

                }else{
                    val newRegisterIntent = Intent(this@SplashActivity, SellerRegisterActivity::class.java)
                    startActivity(newRegisterIntent)
                    finish()
                }
            }
//            .addOnFailureListener {
//                errorTxt.visibility = View.VISIBLE
//                errorTxt.text = "Error: Can not fetch user details"
//            }
    }

}