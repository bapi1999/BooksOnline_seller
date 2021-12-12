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
import androidx.lifecycle.ViewModel
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.ActivityMainBinding

import com.google.android.material.badge.BadgeDrawable
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import com.sbdevs.booksonlineseller.models.MyProductModel
import com.sbdevs.booksonlineseller.otherclass.FireStoreData
import com.sbdevs.booksonlineseller.otherclass.MainViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.ArrayList
import kotlin.properties.Delegates


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding
    private lateinit var navController:NavController
    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    lateinit var notificationBadgeText: TextView
    var productlist: ArrayList<MyProductModel> = ArrayList()
    private lateinit var newOrder :String
    private lateinit var orderBadge: BadgeDrawable
    private val fragmentViewModel:MainViewModel by viewModels()

    private val loadingDialog = LoadingDialog()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        loadingDialog.show(supportFragmentManager,"show")

        lifecycleScope.launch {
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



    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.main_option_menu, menu)

        val notificationMenu = menu.findItem(R.id.notificationsFragment)



        val notifyActionView = notificationMenu!!.actionView
        notificationBadgeText = notifyActionView!!.findViewById(R.id.notification_badge_counter)
        getNotificationForOptionMenu(notificationBadgeText)
        notifyActionView.setOnClickListener {
            onOptionsItemSelected(notificationMenu)
        }



        return true
    }


    override fun onOptionsItemSelected(item: MenuItem): Boolean {

        if (item.itemId == R.id.notificationsFragment) {
            updateNotificationForOptionMenu()
            //navController.navigateUp() // to clear previous navigation history
            navController.navigate(R.id.notificationsFragment)
        }
        if (item.itemId == R.id.profileMenuFragment) {
            val menuIntent = Intent(this,MenuActivity::class.java)
            //menuIntent.putExtra("newOrder",)
            startActivity(menuIntent)
        }

        return super.onOptionsItemSelected(item)

    }

    private fun getNotificationForOptionMenu(textView: TextView) {
        if (user != null){
            val ref = firebaseFirestore.collection("USERS")
                .document(user.uid)
                .collection("SELLER_DATA")
                .document("SELLER_DATA")

            ref.addSnapshotListener { value, error ->
                error?.let {
                    Log.e("Notification","can not load notification",it.cause)
                    textView.visibility = View.GONE
                    return@addSnapshotListener
                }
                value?.let {
                    val newNotification = it.getLong("new_notification")
                    if (newNotification == 0L) {
                        textView.visibility = View.GONE
                    } else {
                        textView.text = newNotification.toString()
                        textView.visibility = View.VISIBLE
                    }

                }
            }
        }else{
            Log.w("Notification","User not logged in")
        }

    }

    private fun updateNotificationForOptionMenu() {
        if (user!= null){
            val ref = firebaseFirestore.collection("USERS")
                .document(user.uid)
                .collection("SELLER_DATA")
                .document("SELLER_DATA")

            val notiMAp: MutableMap<String, Any> = HashMap()
            notiMAp["new_notification"] = 0L
            ref.update(notiMAp)
        }


    }


    private fun getNewOrder(){
        FireStoreData.firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_DATA")
            .document("SELLER_DATA")
            .collection("ORDERS")
            .whereEqualTo("state","new")
            .orderBy("date")
            .addSnapshotListener { value, error ->
            error?.let {
                Log.e("New order snapshot","${it.message}")
                fragmentViewModel.setData("0")
                orderBadge.isVisible = false
                return@addSnapshotListener
            }
            value?.let {


                productlist = it.toObjects(MyProductModel::class.java) as ArrayList<MyProductModel>
                newOrder = productlist.size.toString()

                if (productlist.size == 0){
                    orderBadge.isVisible = false
                }else{
                    orderBadge.number = productlist.size
                    orderBadge.isVisible = true
                }
                fragmentViewModel.setData(newOrder)

            }
        }

    }


}