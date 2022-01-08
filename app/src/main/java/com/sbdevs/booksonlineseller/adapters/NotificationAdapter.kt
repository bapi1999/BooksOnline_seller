package com.sbdevs.booksonlineseller.adapters

import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.content.res.AppCompatResources
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R

import com.sbdevs.booksonlineseller.models.NotificationModel
import com.sbdevs.booksonlineseller.otherclass.FireStoreData

class NotificationAdapter(var list:List<NotificationModel>, var docNameList:ArrayList<String>): RecyclerView.Adapter<NotificationAdapter.ViewHolder>() {



    interface MyonItemClickListener{
        fun onItemClick(position: Int)
    }


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_notification_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position],docNameList[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder (itemView: View):RecyclerView.ViewHolder(itemView) {
        private val firebaseFirestore = Firebase.firestore
        private val user = FirebaseAuth.getInstance().currentUser

        private val indicator:ConstraintLayout = itemView.findViewById(R.id.notification_container)
        private val notificationDescription:TextView = itemView.findViewById(R.id.notification_description)
        private val notificationImage:ImageView = itemView.findViewById(R.id.notification_image)
        private val notificationTime:TextView = itemView.findViewById(R.id.notification_time)

        fun bind(item: NotificationModel, docName:String){
            val image = item.image.trim()
            val seen:Boolean = item.seen

            itemView.setOnClickListener {
//                val orderActivityIntent = Intent(itemView.context, OrderDetailsActivity::class.java)
//                orderActivityIntent.putExtra("order_id",item.order_id)
//                //todo - sellerId is also needed
                //todo welcome notification does not have any seller id or OrderID
//                itemView.context.startActivity(orderActivityIntent)
//                indicator.backgroundTintList =  ContextCompat.getColorStateList(itemView.context!!, R.color.white)
                updateViewStatus(docName)
                indicator.backgroundTintList =  AppCompatResources.getColorStateList(itemView.context!!, R.color.white)
            }


             if (!seen){
                 indicator.backgroundTintList =  AppCompatResources.getColorStateList(itemView.context!!, R.color.noti)
             }else{
                 indicator.backgroundTintList =  AppCompatResources.getColorStateList(itemView.context!!, R.color.white)
             }
            notificationDescription.text = item.description

            Glide.with(itemView.context).load(image).placeholder(R.drawable.as_notification_holder)
                .into(notificationImage)

            val dateFormat = FireStoreData()
            val msAgo = dateFormat.msToTimeAgo(itemView.context,item.date)
            notificationTime.text = msAgo
        }


        private fun updateViewStatus(notificationID:String){

            val updateMap:MutableMap<String,Any> = HashMap()
            updateMap["seen"] = true
            firebaseFirestore.collection("USERS").document(user!!.uid)
                .collection("SELLER_DATA")
                .document("SELLER_DATA")
                .collection("NOTIFICATION")
                .document(notificationID.trim())
                .update(updateMap).addOnSuccessListener {
                    Log.i("update status","successful")
                }.addOnFailureListener {
                    Log.e("update status","${it.message}")
                }

        }
    }

}