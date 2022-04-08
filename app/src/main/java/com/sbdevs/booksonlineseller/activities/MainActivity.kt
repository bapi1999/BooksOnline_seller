package com.sbdevs.booksonlineseller.activities

import android.content.Intent
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.fragment.app.commit
import androidx.lifecycle.lifecycleScope
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.databinding.ActivityMainBinding
import com.google.firebase.Timestamp
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.fragments.LoadingDialog
import com.sbdevs.booksonlineseller.fragments.NotificationsFragment
import com.sbdevs.booksonlineseller.fragments.order.OrdersFragment
import com.sbdevs.booksonlineseller.models.NotificationModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.util.*
import kotlin.collections.HashMap


class MainActivity : AppCompatActivity() {

    private lateinit var binding: ActivityMainBinding

    private val firebaseFirestore = Firebase.firestore
    private val user = Firebase.auth.currentUser

    private lateinit var notificationBadgeText: TextView
    private lateinit var timeStamp: Timestamp
    private var notificationList:List<NotificationModel> = ArrayList()
    private val loadingDialog = LoadingDialog()
    private lateinit var bottomSheetDialog: BottomSheetDialog

    private val gone = View.GONE
    private val visible = View.VISIBLE

    //for bottom message dialog
    private lateinit var applyBtn: Button
    private lateinit var normalMessage: TextView
    private lateinit var warningMessage: TextView
    //=========================

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        notificationBadgeText  = binding.layNotify.notificationBadgeCounter

//        loadingDialog.show(supportFragmentManager,"show")

        bottomSheetDialog = BottomSheetDialog(this, R.style.CustomBottomSheetDialog)
        val view: View = layoutInflater.inflate(R.layout.sl_first_message_bottom_dialog, null)
        bottomSheetDialog.setContentView(view)
        bottomSheetDialog.setCancelable(false)

        applyBtn= bottomSheetDialog.findViewById(R.id.dismiss_btn)!!
        normalMessage= bottomSheetDialog.findViewById(R.id.message_notrmal)!!
        warningMessage= bottomSheetDialog.findViewById(R.id.message_warning)!!
        applyBtn.setOnClickListener {
            bottomSheetDialog.dismiss()
        }

        lifecycleScope.launch(Dispatchers.Main) {
            isUserVerified()
            getTimeStamp()
        }

        val orderFragment = OrdersFragment()
        supportFragmentManager.beginTransaction().replace(R.id.main_frame_layout,orderFragment).commit()

    }

    override fun onStart() {
        super.onStart()
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


    private fun dialogFunction(dialog: BottomSheetDialog,message:String,messageType:Long) {
        val applyBtn: AppCompatButton = dialog.findViewById(R.id.dismiss_btn)!!
        val normalMessage: TextView = dialog.findViewById(R.id.message_notrmal)!!
        val warningMessage: TextView = dialog.findViewById(R.id.message_warning)!!


    }


    private fun getNotificationForOptionMenu(timeStamp1:Timestamp,textView: TextView) {

        val ref = firebaseFirestore.collection("USERS")
            .document(user!!.uid)
            .collection("SELLER_NOTIFICATIONS")
            .whereGreaterThan("date",timeStamp1)

        ref.addSnapshotListener { value, error ->
            error?.let {
                Log.e("Notification","can not load notification",it.cause)
                textView.visibility = View.GONE
            }

            value?.let {

                notificationList = it.toObjects(NotificationModel::class.java)

//                binding.layNotify.notificationBadgeCounter.text= notificationList.size.toString()
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
        Log.e("click","auto clicked")
        if (user!= null){
            val ref = firebaseFirestore.collection("USERS").document(user.uid)

            val newDate =Date()

            val fixedTimestamp:Timestamp = Timestamp(newDate)

            val notiMAp: MutableMap<String, Any> = HashMap()
            notiMAp["new_notification_seller"] = fixedTimestamp

            ref.update(notiMAp).addOnSuccessListener {
                timeStamp = fixedTimestamp
                //Toast.makeText(this,"$timeStamp",Toast.LENGTH_LONG).show()
            }
        }


    }

    private suspend fun getTimeStamp(){
        firebaseFirestore.collection("USERS").document(user!!.uid)
            .get().addOnSuccessListener {
                timeStamp = it.getTimestamp("new_notification_seller")!! as Timestamp

                getNotificationForOptionMenu(timeStamp,notificationBadgeText)

            }.addOnFailureListener {
                Log.e("get Notification time","${it.message}")
            }.await()
    }


    private fun isUserVerified(){
        firebaseFirestore.collection("USERS")
            .document(user!!.uid).collection("SELLER_DATA")
            .document("BUSINESS_DETAILS").get().addOnSuccessListener {
                val isBusinessAdded = it.getBoolean("Is_BusinessDetail_Added")!!
                val isVerified = it.getBoolean("is_address_verified")!!

                if (isBusinessAdded){

                    if (isVerified){
                        warningMessage.visibility = gone
                        normalMessage.visibility = gone
                        bottomSheetDialog.dismiss()
                    }else{
                        warningMessage.visibility = visible
                        val st = getString(R.string.seller_address_not_verified)
                        warningMessage.text = st
                        bottomSheetDialog.show()
                    }


                }else{
                    warningMessage.visibility = visible
                    val st = getString(R.string.you_are_not_a_verified_seller)
                    warningMessage.text = st
                    bottomSheetDialog.show()
                }


            }

    }


}