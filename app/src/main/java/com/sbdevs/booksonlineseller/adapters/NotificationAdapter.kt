package com.sbdevs.booksonlineseller.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
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
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_notification_item_lay, parent, false)
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

        private val indicator:View = itemView.findViewById(R.id.indicator)
        private val notificationDescription:TextView = itemView.findViewById(R.id.notification_description)
        private val notificationImage:ImageView = itemView.findViewById(R.id.notification_image)
        private val notificationTime:TextView = itemView.findViewById(R.id.notification_time)

        fun bind(item: NotificationModel, docName:String){
            val image = item.image.trim()
            val seen:Boolean = item.seen

//            itemView.setOnClickListener {
//                val orderActivityIntent = Intent(itemView.context, OrderDetailsActivity::class.java)
//                orderActivityIntent.putExtra("order_id",item.order_id)
//                //todo - sellerId is also needed
                //todo welcome notification does not have any seller id or OrderID
//                itemView.context.startActivity(orderActivityIntent)
//                indicator.backgroundTintList =  ContextCompat.getColorStateList(itemView.context!!, R.color.white)
//                updateViewStatusInNotification(docName)
//            }


             if (!seen){
                 indicator.backgroundTintList =  ContextCompat.getColorStateList(itemView.context!!, R.color.blueLink)
             }else{
                 indicator.backgroundTintList =  ContextCompat.getColorStateList(itemView.context!!, R.color.white)
             }
            notificationDescription.text = item.description

            Glide.with(itemView.context).load(image)
                .placeholder(R.drawable.as_square_placeholder)
                .into(notificationImage)
            val dateFormat = FireStoreData()
//            val dayAgo = dateFormat.durationFromNow(item.date)
            val msAgo = dateFormat.msToTimeAgo(itemView.context,item.date)
            notificationTime.text = msAgo
        }


        fun updateViewStatusInNotification(notificationID:String){

            val updateMap:MutableMap<String,Any> = HashMap()
            updateMap["seen"] = true
            firebaseFirestore.collection("USERS").document(user!!.uid)
                .collection("SELLER_DATA")
                .document("MY_NOTIFICATION")
                .collection("NOTIFICATION")
                .document(notificationID)
                .update(updateMap)

        }
    }

}