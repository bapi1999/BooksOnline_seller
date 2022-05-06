package com.sbdevs.booksonlineseller.activities

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.webkit.WebView
import android.webkit.WebViewClient
import com.google.firebase.database.ktx.database
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.ActivityPoliciesBinding
import com.sbdevs.booksonlineseller.fragments.LoadingDialog

class PoliciesActivity : AppCompatActivity() {
    private lateinit var wevView: WebView
    private lateinit var binding:ActivityPoliciesBinding
    private val firebaseDatabase = Firebase.database.reference

    private var policyCode = 0
    private val loadingDialog = LoadingDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityPoliciesBinding.inflate(layoutInflater)
        setContentView(binding.root)
        loadingDialog.show(supportFragmentManager,"show")
        policyCode = intent.getIntExtra("PolicyCode",0)

        wevView = binding.webView
        wevView.webViewClient = WebViewClient()

        getAll()
    }

    private fun getAll(){
        firebaseDatabase.child("Policies").get()
            .addOnSuccessListener {
                val privacyPolicy:String = it.child("privacy").value as String
                val returnPolicy:String = it.child("return").value as String
                val termsCondition:String = it.child("terms_condition").value as String

                when(policyCode){
                    0->{
                        wevView.loadUrl("termsCondition")
                        Log.e("Policy error","No policy found")
                    }
                    1->{
                        wevView.loadUrl(termsCondition)
                    }
                    2->{
                        wevView.loadUrl(privacyPolicy)
                    }
                    3->{
                        wevView.loadUrl(returnPolicy)
                    }
                }

                loadingDialog.dismiss()

            }.addOnFailureListener {
                Log.e("Get Policy error :","${it.message}")
                loadingDialog.dismiss()
            }

    }

}