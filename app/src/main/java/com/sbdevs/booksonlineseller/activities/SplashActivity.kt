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
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class SplashActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash)

        this.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_PORTRAIT

        val currentUser = Firebase.auth.currentUser
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

//                    FirebaseFirestore.getInstance().collection("USERS").document(currentUser.uid)
//                        .update("Last seen", FieldValue.serverTimestamp()).await()
//                    withContext(Dispatchers.Main){
//
//                    }
                }catch (e:Exception){
                    withContext(Dispatchers.Main){
                        Toast.makeText(this@SplashActivity,e.message,Toast.LENGTH_LONG).show()
                    }
                }


            }

        }

    }
}