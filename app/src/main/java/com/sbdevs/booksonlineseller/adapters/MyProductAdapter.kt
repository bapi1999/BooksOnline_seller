package com.sbdevs.booksonlineseller.adapters

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.activities.ProductActivity
import com.sbdevs.booksonlineseller.models.MyProductModel
import com.sbdevs.booksonlineseller.otherclass.FireStoreData

class MyProductAdapter (var productIdList:ArrayList<String>,var list:ArrayList<MyProductModel>):RecyclerView.Adapter<MyProductAdapter.ViewHolder>() {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {

        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.le_my_product_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(productIdList[position],list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }


    class ViewHolder (itemView: View):RecyclerView.ViewHolder(itemView) {

        private val productImage : ImageView = itemView.findViewById(R.id.product_image)
        private val productName: TextView = itemView.findViewById(R.id.product_name)
        private val productPrice:TextView = itemView.findViewById(R.id.product_price)
        private val productRealPrice:TextView = itemView.findViewById(R.id.product_real_price)
        private val priceOff:TextView = itemView.findViewById(R.id.percent_off)
        var ratingTotalTxt: TextView = itemView.findViewById(R.id.mini_totalNumberOf_ratings)
        var miniRatingTxt: TextView = itemView.findViewById(R.id.mini_product_rating)
        var bookSateTxt: TextView = itemView.findViewById(R.id.product_state)
        private var updatedTimeText: TextView = itemView.findViewById(R.id.updated_time_text)
        var stockText: TextView = itemView.findViewById(R.id.product_stock)


        fun bind(productId:String,item:MyProductModel){

            itemView.setOnClickListener {
                val productIntent = Intent(itemView.context,ProductActivity::class.java)
                productIntent.putExtra("productId",productId)
                itemView.context.startActivity(productIntent)
            }

            productName.text = item.book_title


            val url:String = item.product_thumbnail
            ratingTotalTxt.text = item.rating_total.toString()

            val priceOriginal = item.price_original
            val priceSelling = item.price_selling
            miniRatingTxt.text = item.rating_avg
            ratingTotalTxt.text = "( ${item.rating_total} ratings )"
            val stock = item.in_stock_quantity


            when {
                stock > 5 -> {
                    stockText.setTextColor(AppCompatResources.getColorStateList(itemView.context,R.color.grey_900))
                }
                stock in 1..5 -> {
                    stockText.setTextColor(AppCompatResources.getColorStateList(itemView.context,R.color.amber_900))
                }
                stock == 0L -> {
                    stockText.setTextColor(AppCompatResources.getColorStateList(itemView.context,R.color.red_700))
                }
                else -> {
                    stockText.setTextColor(AppCompatResources.getColorStateList(itemView.context,R.color.grey_900))

                }
            }

            if (priceOriginal == 0L){
                productPrice.text = priceSelling.toString()
                priceOff.text = "No offer Available"
                priceOff.setTextColor(AppCompatResources.getColorStateList(itemView.context,R.color.grey_500))
                productRealPrice.visibility = View.GONE

            }else{
                val percent:Int = (100* (priceOriginal.toInt() - priceSelling.toInt())) / ( priceOriginal.toInt() )

                productPrice.text = priceSelling.toString()
                productRealPrice.text = priceOriginal.toString()
                priceOff.text = "${percent}% off"

            }

            updatedTimeText.text = FireStoreData().msToTimeAgo(itemView.context,item.PRODUCT_UPDATE_ON)

//            Picasso.get()
//                .load(url)
//                .placeholder(R.drawable.as_square_placeholder)
//                .into(productImage)

            Glide.with(itemView.context).load(url).placeholder(R.drawable.as_square_placeholder).into(productImage);

        }

    }

}