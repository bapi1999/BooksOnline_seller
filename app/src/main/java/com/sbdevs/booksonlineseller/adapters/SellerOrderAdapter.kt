package com.sbdevs.booksonlineseller.adapters

import android.annotation.SuppressLint
import android.content.Intent
import android.icu.text.SimpleDateFormat
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.google.firebase.appindexing.builders.TimerBuilder
import com.google.firebase.auth.ktx.auth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.activities.OrderDetailsActivity
import com.sbdevs.booksonlineseller.models.SellerOrderModel
import com.sbdevs.booksonlineseller.otherclass.TimeDateAgo
import java.util.*

class SellerOrderAdapter(var list:List<SellerOrderModel>, val listener:OrderItemClickListener) :RecyclerView.Adapter<SellerOrderAdapter.ViewHolder> (){


    interface OrderItemClickListener{
        fun acceptClickListener(position: Int)
        fun shipClickListener(position: Int)
        fun cancelClickListener(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SellerOrderAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_order,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: SellerOrderAdapter.ViewHolder, position: Int) {
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
        private val orderIdText:TextView = itemView.findViewById(R.id.order_id)

        private val buttonContainer:LinearLayout = itemView.findViewById(R.id.button_container)

        private val acceptBtn:Button = itemView.findViewById(R.id.accept_order_btn)
        private val viewOrderBtn:Button = itemView.findViewById(R.id.view_order_btn)
        private val cancelBtn:Button = itemView.findViewById(R.id.cancel_order_btn)
        private val shipBtn:Button = itemView.findViewById(R.id.ship_order_btn)



        val gone = View.GONE
        val visible = View.VISIBLE


        fun bind(item:SellerOrderModel){
            val orderId = item.documentId
            itemView.setOnClickListener {
                val orderIntent = Intent(itemView.context,OrderDetailsActivity::class.java)
                orderIntent.putExtra("documentId",orderId)
                itemView.context.startActivity(orderIntent)
            }

            viewOrderBtn.setOnClickListener {
                val orderIntent = Intent(itemView.context,OrderDetailsActivity::class.java)
                orderIntent.putExtra("documentId",orderId)
                itemView.context.startActivity(orderIntent)
            }

            acceptBtn.setOnClickListener {
                listener.acceptClickListener(adapterPosition)
            }


            shipBtn.setOnClickListener {
                listener.shipClickListener(adapterPosition)
            }

            cancelBtn.setOnClickListener {
                listener.cancelClickListener(adapterPosition)
            }



            val status:String = item.status
            val onlinePayment = item.onlinePayment
            val address:MutableMap<String,Any> = item.address
            val buyerId:String = item.buyerId
            val orderTimed:Date = item.Time_ordered

            productName.text = item.productTitle
            orderPrice.text = item.price.toString()
            orderQTY.text ="${item.ordered_Qty}"
            orderStatus.text = status


            orderIdText.text ="Order Id: ${item.documentId}"


            Glide.with(itemView.context)
                .load(item.productThumbnail)
                .placeholder(R.drawable.as_square_placeholder)
                .into(productImage)


            if (!onlinePayment){

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







            when(status){
                "new" ->{

                    val dateFormat = TimeDateAgo().msToTimeAgo(itemView.context,item.Time_ordered)
                    orderTime.text = getDateTime(item.Time_ordered)
                    orderTimeType.text = "Ordered"

                    buttonContainer.visibility = visible
                    acceptBtn.visibility = visible
                    viewOrderBtn.visibility = gone
                    shipBtn.visibility = gone

                    orderStatus.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.amber_600)
                    orderStatus.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.grey_900))

                }

                "accepted" ->{
                    val dateFormat = TimeDateAgo().msToTimeAgo(itemView.context,item.Time_accepted!!)
                    orderTime.text = getDateTime(item.Time_accepted)
                    orderTimeType.text = "Accepted"
                    buttonContainer.visibility = visible
                    acceptBtn.visibility = gone
                    viewOrderBtn.visibility = visible
                    shipBtn.visibility = gone


                    orderStatus.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.successGreen)
                    orderStatus.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.black))


                }

                "packed" ->{

                    val dateFormat = TimeDateAgo().msToTimeAgo(itemView.context,item.Time_packed!!)
                    orderTime.text = getDateTime(item.Time_packed)
                    orderTimeType.text = "Packed"
                    buttonContainer.visibility = visible
                    acceptBtn.visibility = gone
                    viewOrderBtn.visibility = gone
                    shipBtn.visibility = visible

                    orderStatus.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.viewAll)
                    orderStatus.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.white))


                }

                "shipped"->{
                   buttonContainer.visibility = gone
                    val dateFormat = TimeDateAgo().msToTimeAgo(itemView.context,item.Time_shipped!!)
                    orderTime.text = dateFormat
                    orderTimeType.text = "Shipped"

                    orderStatus.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,R.color.indigo_700)
                    orderStatus.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.white))

                    autoCancelText.visibility = gone
                }
                "delivered"->{
                    buttonContainer.visibility = gone
                    val dateFormat = TimeDateAgo().msToTimeAgo(itemView.context,item.Time_delivered!!)
                    orderTime.text = dateFormat
                    orderTimeType.text = "Delivered"

                    orderStatus.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,
                        R.color.teal_700
                    )
                    orderStatus.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.white))

                    autoCancelText.visibility = gone
                }
                "returned"->{
                    buttonContainer.visibility = gone
                    val dateFormat = TimeDateAgo().msToTimeAgo(itemView.context,item.Time_returned!!)
                    orderTime.text = dateFormat
                    orderTimeType.text = "Returned"
                    autoCancelText.visibility = gone
                    orderStatus.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,
                        R.color.amber_900
                    )
                    orderStatus.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.white))

                }
                "canceled"->{
                    buttonContainer.visibility = gone
                    val dateFormat = TimeDateAgo().msToTimeAgo(itemView.context,item.Time_canceled!!)
                    orderTime.text = dateFormat
                    orderTimeType.text = "Canceled"
                    autoCancelText.visibility = gone

                    orderStatus.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,
                        R.color.red_a700
                    )
                    orderStatus.setTextColor( AppCompatResources.getColorStateList(itemView.context,R.color.white))


                }
                else ->{
                    val dateFormat = TimeDateAgo().msToTimeAgo(itemView.context,item.Time_ordered)
                    orderTime.text = dateFormat
                }
            }

        }

        @SuppressLint("SimpleDateFormat")
        private fun getDateTime(date: Date): String? {
            return try {

                val t:TimerBuilder
                //t.setExpireTime()

                val sdf = SimpleDateFormat("hh:mm a dd/MM/yyyy")
//                val netDate = Date(tm.)
                sdf.format(date)
            } catch (e: Exception) {
                e.toString()
            }
        }


    }

}