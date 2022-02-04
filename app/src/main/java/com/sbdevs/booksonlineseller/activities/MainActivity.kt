package com.sbdevs.booksonlineseller.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.ActivityMainBinding

import com.google.android.material.badge.BadgeDrawable
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import com.sbdevs.booksonlineseller.fragments.NotificationsFragment
import com.sbdevs.booksonlineseller.fragments.order.OrdersFragment
import com.sbdevs.booksonlineseller.models.DashboardCountModel
import com.sbdevs.booksonlineseller.models.MyProductModel
import com.sbdevs.booksonlineseller.models.NotificationModel
import com.sbdevs.booksonlineseller.otherclass.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.ArrayList


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController:NavController
    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var notificationBadgeText: TextView
    var productlist: ArrayList<MyProductModel> = ArrayList()
    private lateinit var newOrder :String
    private val fragmentViewModel:MainViewModel by viewModels()
    private lateinit var timeStamp: Timestamp
    private var notificationList:List<NotificationModel> = ArrayList()
    private val loadingDialog = LoadingDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationBadgeText  = binding.layNotify.notificationBadgeCounter

        loadingDialog.show(supportFragmentManager,"show")


        lifecycleScope.launch(Dispatchers.Main) {
            getTimeStamp()
            delay(1000)

            withContext(Dispatchers.Main){

                loadingDialog.dismiss()
            }
        }
        val orderFragment = OrdersFragment()

        supportFragmentManager.beginTransaction().replace(R.id.main_frame_layout,orderFragment).commit()


        //val navView: BottomNavigationView = binding.navView
//        val navHostFragment = supportFragmentManager.findFragmentById(
//            R.id.nav_host_fragment_activity_main
//        ) as NavHostFragment
//
//        navController = navHostFragment.navController // findNavController(R.id.nav_host_fragment_activity_main)
//
//        val appBarConfiguration = AppBarConfiguration(
//            setOf(
//                 R.id.ordersFragment2,R.id.myProductFragment2,R.id.myEarningFragment
//            )
//        )
//        setSupportActionBar(binding.toolbar)
//        setupActionBarWithNavController(navController, appBarConfiguration)

        //navView.setupWithNavController(navController)


    }






    override fun onStart() {
        super.onStart()
       //getNotificationForOptionMenu()

        binding.layNotify.notificationContainer.setOnClickListener {

            updateNotificationForOptionMenu()
            notificationBadgeText.visibility = View.GONE

            val notificationsFragment = NotificationsFragment()
            val fragmentManager = supportFragmentManager.commit {
                    setReorderingAllowed(true)
                    add(R.id.main_frame_layout,notificationsFragment)
                    addToBackStack("imageView")
                }

        }

        binding.menuBtn.setOnClickListener {
            val menuIntent = Intent(this,MenuActivity::class.java)
            startActivity(menuIntent)
            overridePendingTransition(R.anim.slide_in_left,R.anim.slide_out_right)
        }
    }


    private fun getNotificationForOptionMenu(timeStamp1:Timestamp,textView: TextView) {

        val ref = firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("NOTIFICATION")
            .whereGreaterThan("date",timeStamp1)

        ref.addSnapshotListener { value, error ->
            error?.let {
                Log.e("Notification","can not load notification",it.cause)
                textView.visibility = View.GONE
            }

            value?.let {

                notificationList = it.toObjects(NotificationModel::class.java)

                //binding.layNotify.notificationBadgeCounter.text= notificationList.size.toString()
                if (notificationList.isEmpty()){
                    textView.visibility = View.GONE
                }else{
                    textView.visibility = View.VISIBLE
                    textView.text = notificationList.size.toString()
                }
            }


        }

    }

    private fun updateNotificationForOptionMenu() {
        Toast.makeText(this,"time updated",Toast.LENGTH_LONG).show()
        Log.e("click","auto clicked")
        if (user!= null){
            val ref = firebaseFirestore.collection("USERS")
                .document(user.uid)
                .collection("SELLER_DATA")
                .document("SELLER_DATA")

            val notiMAp: MutableMap<String, Any> = HashMap()
            notiMAp["new_notification"] = FieldValue.serverTimestamp()
            ref.update(notiMAp)
        }


    }

    private suspend fun getTimeStamp(){
        firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .get().addOnSuccessListener {
                timeStamp = it.getTimestamp("new_notification")!!
                getNotificationForOptionMenu(timeStamp,notificationBadgeText)
            }.addOnFailureListener {
                Log.e("get Notification time","${it.message}")
            }.await()
    }

}