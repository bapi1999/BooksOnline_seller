package com.sbdevs.booksonlineseller.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sbdevs.booksonlineseller.R

class UploadImageAdapter(var list:ArrayList<Uri>, val listener: MyOnItemClickListener):RecyclerView.Adapter<UploadImageAdapter.ViewHolder>() {

    interface MyOnItemClickListener{
        fun onItemClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): UploadImageAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.le_upload_image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: UploadImageAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val imageView:ImageView = itemView.findViewById(R.id.product_image)

        fun bind(url:Uri){
            imageView.setOnClickListener {
                listener.onItemClick(adapterPosition)
            }

            Glide.with(itemView.context).load(url)
                .placeholder(R.drawable.as_square_placeholder)
                .into(imageView)
        }

    }

}