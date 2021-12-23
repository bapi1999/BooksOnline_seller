package com.sbdevs.booksonlineseller.activities

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.TextView
import androidx.activity.viewModels
import com.google.android.material.bottomnavigation.BottomNavigationView
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
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
    private lateinit var orderBadge: BadgeDrawable
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

            withContext(Dispatchers.IO){
                firebaseFirestore.collection("USERS")
                    .document(user!!.uid)
                    .collection("SELLER_DATA")
                    .document("SELLER_DATA").
                    get().addOnSuccessListener {
                        timeStamp = it.getTimestamp("new_notification")!!
                        getNotificationForOptionMenu(timeStamp,notificationBadgeText)
                    }.addOnFailureListener {
                        Log.e("get Notification time","${it.message}")
                    }.await()
            }
            withContext(Dispatchers.IO){
                getNewOrder()

            }

            withContext(Dispatchers.Main){
                delay(1000)
                loadingDialog.dismiss()
            }
        }

        val navView: BottomNavigationView = binding.navView

        navController = findNavController(R.id.nav_host_fragment_activity_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        val appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.dashboardFragment, R.id.ordersFragment,R.id.myProductFragment,R.id.myEarningFragment
            )
        )
        setSupportActionBar(binding.toolbar)
        setupActionBarWithNavController(navController, appBarConfiguration)

        navView.setupWithNavController(navController)


        orderBadge = navView.getOrCreateBadge(R.id.ordersFragment)
        orderBadge.backgroundColor = Color.BLUE
        orderBadge.badgeTextColor = Color.WHITE
        orderBadge.maxCharacterCount = 3
//        orderBadge.isVisible = true

    }

    override fun onStart() {
        super.onStart()
       //getNotificationForOptionMenu()

        binding.layNotify.notificationContainer.setOnClickListener {
            updateNotificationForOptionMenu()
            notificationBadgeText.visibility = View.GONE
            //navController.navigateUp() // to clear previous navigation history
            navController.navigate(R.id.notificationsFragment)
        }
    }



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_option_menu, menu)

        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        if (item.itemId == R.id.profileMenuFragment) {
            val menuIntent = Intent(this,MenuActivity::class.java)
            //menuIntent.putExtra("newOrder",)
            startActivity(menuIntent)
        }

        return super.onOptionsItemSelected(item)

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

    private fun getNewOrder(){
        firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("ORDERS")
            .whereEqualTo("status","new")
            .orderBy("Time_ordered")
            .get()
            .addOnSuccessListener{

                productlist = it.toObjects(MyProductModel::class.java) as ArrayList<MyProductModel>
                newOrder = productlist.size.toString()

                if (productlist.size == 0){
                    orderBadge.isVisible = false
                }else{
                    orderBadge.number = productlist.size
                    orderBadge.isVisible = true
                }
                fragmentViewModel.setNewOrder(newOrder)

            }.addOnFailureListener {
                Log.e("New order snapshot","${it.message}")
                fragmentViewModel.setNewOrder("0")
                orderBadge.isVisible = false
            }

    }


//    private fun getNewOrder(){
//        firebaseFirestore.collection("USERS")
//            .document(user!!.uid)
//            .collection("SELLER_DATA")
//            .document("SELLER_DATA")
//            .collection("ORDERS")
//            .whereEqualTo("status","new")
//            .orderBy("Time_ordered")
//            .addSnapshotListener { value, error ->
//            error?.let {
//                Log.e("New order snapshot","${it.message}")
//                fragmentViewModel.setData("0")
//                orderBadge.isVisible = false
//                return@addSnapshotListener
//            }
//            value?.let {
//
//
//                productlist = it.toObjects(MyProductModel::class.java) as ArrayList<MyProductModel>
//                newOrder = productlist.size.toString()
//
//                if (productlist.size == 0){
//                    orderBadge.isVisible = false
//                }else{
//                    orderBadge.number = productlist.size
//                    orderBadge.isVisible = true
//                }
//                fragmentViewModel.setData(newOrder)
//
//            }
//        }
//
//    }


}