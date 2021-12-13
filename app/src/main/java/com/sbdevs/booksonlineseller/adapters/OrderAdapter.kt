package com.sbdevs.booksonlineseller.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.activities.OrderDetailsActivity
import com.sbdevs.booksonlineseller.models.OrderModel
import com.sbdevs.booksonlineseller.otherclass.FireStoreData

class OrderAdapter(var list:List<OrderModel>, var orderIdList:ArrayList<String>) :RecyclerView.Adapter<OrderAdapter.ViewHolder> (){

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

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        private val productImage:ImageView = itemView.findViewById(R.id.product_image)
        private val productName:TextView = itemView.findViewById(R.id.title_txt)
        private val orderPrice:TextView = itemView.findViewById(R.id.price_txt)
        private val orderQTY:TextView = itemView.findViewById(R.id.order_qty)
        private val orderStatus:TextView = itemView.findViewById(R.id.status_txt)
        private val orderTime:TextView = itemView.findViewById(R.id.order_time)

        fun bind(item:OrderModel){
            itemView.setOnClickListener {
                val orderIntent = Intent(itemView.context,OrderDetailsActivity::class.java)
                itemView.context.startActivity(orderIntent)
            }
            productName.text = item.productTitle
            orderPrice.text = item.price
            orderQTY.text = item.ordered_Qty.toString()
            orderStatus.text = item.status

            val dateFormat = FireStoreData().msToTimeAgo(itemView.context,item.orderTime)
            orderTime.text = dateFormat

            Glide.with(itemView.context)
                .load(item.productThumbnail)
                .placeholder(R.drawable.as_square_placeholder)
                .into(productImage)

        }
    }

}