package com.sbdevs.booksonlineseller.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.icu.util.TimeUnit
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.appcompat.widget.AppCompatButton
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.Timestamp
import com.google.firebase.appindexing.builders.TimerBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.activities.OrderDetailsActivity
import com.sbdevs.booksonlineseller.models.OrderModel
import com.sbdevs.booksonlineseller.otherclass.FireStoreData
import java.util.*
import kotlin.collections.ArrayList

class OrderAdapter(var list:List<OrderModel>, val listener:OrderItemClickListener) :RecyclerView.Adapter<OrderAdapter.ViewHolder> (){


    interface OrderItemClickListener{
        fun acctepClickListner(position: Int)
        fun rejectlClickLisner(position: Int)
        fun shipClickListner(position: Int)
        fun cancelClickLisner(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): OrderAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_order_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: OrderAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {

        private val firebaseFirestore = Firebase.firestore
        private val user = Firebase.auth.currentUser

        private val productImage:ImageView = itemView.findViewById(R.id.product_image)
        private val productName:TextView = itemView.findViewById(R.id.title_txt)
        private val orderPrice:TextView = itemView.findViewById(R.id.price_txt)
        private val orderQTY:TextView = itemView.findViewById(R.id.order_qty)
        private val orderStatus:TextView = itemView.findViewById(R.id.status_txt)
        private val orderTime:TextView = itemView.findViewById(R.id.order_time)
        private val orderTimeType:TextView = itemView.findViewById(R.id.time_type)

        private val paidStatusText:TextView = itemView.findViewById(R.id.paid_status_text)
        private val errorText:TextView = itemView.findViewById(R.id.error_message_text)
        private val autoCancelText:TextView = itemView.findViewById(R.id.auto_cancel_text)
        private val productIdText:TextView = itemView.findViewById(R.id.product_item_id)

        private val newButtonContainer:LinearLayout = itemView.findViewById(R.id.new_button_container)
        private val acceptButtonContainer:LinearLayout = itemView.findViewById(R.id.accept_button_container)
        private val packedButtonContainer:LinearLayout = itemView.findViewById(R.id.pack_button_container)

        private val rejectBtn:Button = itemView.findViewById(R.id.reject_order_btn)
        private val acceptBtn:Button = itemView.findViewById(R.id.accept_order_btn)
        private val viewOrderBtn:Button = itemView.findViewById(R.id.view_order_btn)
        private val cancelBtn:Button = itemView.findViewById(R.id.cancel_order_btn)
        private val shipBtn:Button = itemView.findViewById(R.id.ship_order_btn)



        val gone = View.GONE
        val visible = View.VISIBLE


        fun bind(item:OrderModel){
            val orderId = item.orderId
            itemView.setOnClickListener {
                val orderIntent = Intent(itemView.context,OrderDetailsActivity::class.java)
                orderIntent.putExtra("orderId",orderId)
                itemView.context.startActivity(orderIntent)
            }

            viewOrderBtn.setOnClickListener {
                val orderIntent = Intent(itemView.context,OrderDetailsActivity::class.java)
                orderIntent.putExtra("orderId",orderId)
                itemView.context.startActivity(orderIntent)
            }

            acceptBtn.setOnClickListener {
                listener.acctepClickListner(adapterPosition)
            }

            rejectBtn.setOnClickListener {
                listener.rejectlClickLisner(adapterPosition)
            }

            shipBtn.setOnClickListener {
                listener.shipClickListner(adapterPosition)
            }

            cancelBtn.setOnClickListener {
                listener.cancelClickLisner(adapterPosition)
            }



            val status:String = item.status
            val alreadyPaid = item.already_paid
            val address:MutableMap<String,Any> = item.address
            val buyerId:String = item.buyerId
            val orderTimed:Date = item.Time_ordered

            productName.text = item.productTitle
            orderPrice.text = item.price.toString()
            orderQTY.text ="${item.ordered_Qty}"
            orderStatus.text = status


            productIdText.text ="Item id hare"// not real productId or it can be hacked by


            Glide.with(itemView.context)
                .load(item.productThumbnail)
                .placeholder(R.drawable.as_square_placeholder)
                .into(productImage)


            if (!alreadyPaid){

                paidStatusText.text = "Not paid"
                paidStatusText.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.grey_400)
                paidStatusText.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.grey_800))

            }else{

                paidStatusText.text = "Paid"
                paidStatusText.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.indigo_700)
                paidStatusText.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.white))

            }


            val warning:String = itemView.context.getString(R.string.order_warning_1)

            if (address.isEmpty()){
                errorText.visibility = visible
                errorText.text = warning
                acceptBtn.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.grey_600)
                acceptBtn.isEnabled = false

            }else{
                errorText.visibility = gone
                errorText.text = null
                acceptBtn.isEnabled = true
                acceptBtn.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.amber_600)

            }

            val expireDate = getTimeExpire(item.Time_ordered)
            if (isTimeExpire(item.Time_ordered)) {
                autoCancelText.text = "Auto cancel on $expireDate"
                autoCancelText.setTextColor(AppCompatResources.getColorStateList(itemView.context,R.color.grey_800))
            }else{
                autoCancelText.text = "Already canceled on $expireDate"
                autoCancelText.setTextColor(AppCompatResources.getColorStateList(itemView.context,R.color.red_900))

            }




            when(status){
                "new" ->{

                    val dateFormat = FireStoreData().msToTimeAgo(itemView.context,item.Time_ordered)
                    orderTime.text = dateFormat
                    orderTimeType.text = "Ordered"
                    newButtonContainer.visibility = visible
                    acceptButtonContainer.visibility = gone
                    packedButtonContainer.visibility = gone

                    orderStatus.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.amber_600)
                    orderStatus.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.grey_900))

                }
                "accepted" ->{

                    val dateFormat = FireStoreData().msToTimeAgo(itemView.context,item.Time_accepted!!)
                    orderTime.text = dateFormat
                    orderTimeType.text = "Accepted"
                    newButtonContainer.visibility = gone
                    acceptButtonContainer.visibility = visible
                    packedButtonContainer.visibility = gone

                    orderStatus.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.amber_600)
                    orderStatus.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.black))
                }
                "packed" ->{

                    val dateFormat = FireStoreData().msToTimeAgo(itemView.context,item.Time_packed!!)
                    orderTime.text = dateFormat
                    orderTimeType.text = "Packed"
                    newButtonContainer.visibility = gone
                    acceptButtonContainer.visibility = gone
                    packedButtonContainer.visibility = visible

                    orderStatus.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.teal_700)
                    orderStatus.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.white))


                }

                "shipped"->{
                    newButtonContainer.visibility = gone
                    acceptButtonContainer.visibility = gone
                    packedButtonContainer.visibility = gone
                    val dateFormat = FireStoreData().msToTimeAgo(itemView.context,item.Time_shipped!!)
                    orderTime.text = dateFormat
                    orderTimeType.text = "Shipped"

                    orderStatus.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.indigo_700)
                    orderStatus.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.white))
                }
                "delivered"->{
                    newButtonContainer.visibility = gone
                    acceptButtonContainer.visibility = gone
                    packedButtonContainer.visibility = gone
                    val dateFormat = FireStoreData().msToTimeAgo(itemView.context,item.Time_delivered!!)
                    orderTime.text = dateFormat
                    orderTimeType.text = "Delivered"

                    orderStatus.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,
                        R.color.red_a700
                    )
                    orderStatus.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.white))
                }
                "returned"->{
                    newButtonContainer.visibility = gone
                    acceptButtonContainer.visibility = gone
                    packedButtonContainer.visibility = gone
                    val dateFormat = FireStoreData().msToTimeAgo(itemView.context,item.Time_returned!!)
                    orderTime.text = dateFormat
                    orderTimeType.text = "Returned"
                }
                "canceled"->{
                    newButtonContainer.visibility = gone
                    acceptButtonContainer.visibility = gone
                    packedButtonContainer.visibility = gone
                    val dateFormat = FireStoreData().msToTimeAgo(itemView.context,item.Time_canceled!!)
                    orderTime.text = dateFormat
                    orderTimeType.text = "Canceled"

                }
                else ->{
                    val dateFormat = FireStoreData().msToTimeAgo(itemView.context,item.Time_ordered)
                    orderTime.text = dateFormat
                }
            }

        }


        private fun cancelOrderMethod(orderId: String){

            val orderMap:MutableMap<String,Any> = HashMap()
            orderMap["status"] = "canceled"
            orderMap["is_order_canceled"] = true

            orderMap["Time_canceled"] = FieldValue.serverTimestamp()
            firebaseFirestore.collection("USERS")
                .document(user!!.uid)
                .collection("SELLER_DATA")
                .document("SELLER_DATA")
                .collection("ORDERS")
                .document(orderId).update(orderMap)
                .addOnSuccessListener {
                    Log.i("canceled order","successful")
                    //loadingDialog.dismiss()
                }
                .addOnFailureListener {
                    //loadingDialog.dismiss()
                    Log.e("canceled order","${it.message}")
                }
        }

        private fun sendNotification(buyerId:String,productName:String,url:String,status: String,orderId: String){

            val ref = firebaseFirestore.collection("USERS").document(buyerId).collection("USER_DATA")
                .document("MY_NOTIFICATION").collection("NOTIFICATION")

            val notificationMap: MutableMap<String, Any> = HashMap()
            notificationMap["date"] = FieldValue.serverTimestamp()
            notificationMap["description"] = "$status:$productName"
            notificationMap["image"] = url
            notificationMap["order_id"] = orderId
            notificationMap["seller_id"] = user!!.uid
            notificationMap["seen"] = false
//

            ref.add(notificationMap)
                .addOnSuccessListener {

                }.addOnFailureListener {
                    Log.e("get buyer notification","${it.message}")
                }
        }

        @SuppressLint("SimpleDateFormat")
        private fun getDateTime(date: Date): String? {
            return try {

                val t:TimerBuilder
                //t.setExpireTime()

                val sdf = SimpleDateFormat("dd MMMM yyyy hh:mm a")
//                val netDate = Date(tm.)
                sdf.format(date)
            } catch (e: Exception) {
                e.toString()
            }
        }


        private fun isTimeExpire(date: Date): Boolean {

            val expireDate = Date(date.time + (1000 * 60 * 60 * 24))
            val currentTime = Date()

            val diffDate:Long = expireDate.time - currentTime.time
            diffDate.toString()
            // false -> expired
            return diffDate > 0

        }

        private fun getTimeExpire(date: Date): String? {
            return try {

                val expireDate = Date(date.time + (1000 * 60 * 60 * 24))
//                val currentTime = Date()
                val sdf = SimpleDateFormat("dd MMMM yyyy , hh:mm a")
//                val diffDate:Long = expireDate.time - currentTime.time
//
//
//                val seconds = diffDate / 1000
//                val minutes = seconds / 60
//                val hours = minutes / 60
//                val days = hours / 24

                sdf.format(expireDate)
            } catch (e: Exception) {
                e.toString()
            }


        }


    }

}