package com.sbdevs.booksonlineseller.adapters

import android.net.Uri
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sbdevs.booksonlineseller.R

class NewUploadImageAdapter(var list:ArrayList<Uri>, val listener: MyOnItemClickListener):RecyclerView.Adapter<NewUploadImageAdapter.ViewHolder>() {

    interface MyOnItemClickListener{
        fun onNewImageDeleteClick(position: Int)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NewUploadImageAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.item_upload_already_image, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: NewUploadImageAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }


    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        val imageView:ImageView = itemView.findViewById(R.id.product_image)
        val deleteImageBtn: LinearLayout = itemView.findViewById(R.id.delete_image_button)

        fun bind(url:Uri){
            deleteImageBtn.setOnClickListener {
                listener.onNewImageDeleteClick(adapterPosition)
            }

            Glide.with(itemView.context).load(url)
                .placeholder(R.drawable.as_square_placeholder)
                .into(imageView)
        }

    }

}