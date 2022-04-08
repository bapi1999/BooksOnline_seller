package com.sbdevs.booksonlineseller.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.ortiz.touchview.TouchImageView
import com.sbdevs.booksonlineseller.R

class ProductZoomImageAdapter(var productImgList: ArrayList<String>) :
    RecyclerView.Adapter<ProductZoomImageAdapter.ViewHolder>() {

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ):ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_image_zoom,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder:ViewHolder, position: Int) {
        holder.bind(productImgList[position])
    }

    override fun getItemCount(): Int {
        return productImgList.size
    }


    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val productImg: TouchImageView = itemView.findViewById(R.id.touch_image)

        fun bind(url:String) {

            Glide.with(itemView.context).load(url)
                .apply(RequestOptions().placeholder(R.drawable.as_square_placeholder))
                .into(productImg)
//            Picasso.get()
//                .load(url)
//                .placeholder(R.drawable.as_square_placeholder)
//                .resize(300, 300)
//                .centerCrop()
//                .into(productImage)
        }

    }
}