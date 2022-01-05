package com.sbdevs.booksonlineseller.activities

import android.content.Intent
import android.content.pm.ActivityInfo
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.lifecycleScope
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.ActivitySplashBinding
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {

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

            lifecycleScope.launch(Dispatchers.IO){
                try {
                    val mainintent = Intent(this@SplashActivity, MainActivity::class.java)
                    startActivity(mainintent)
                    finish()

                }catch (e:Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@SplashActivity,e.message,Toast.LENGTH_LONG).show()
                    }
                }


            }

        }

    }
}