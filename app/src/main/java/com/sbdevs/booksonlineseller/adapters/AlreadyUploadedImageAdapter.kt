package com.sbdevs.booksonlineseller.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sbdevs.booksonlineseller.R

class AlreadyUploadedImageAdapter (var list:ArrayList<String>, val listener:AlreadyAddedImageClickListener):
    RecyclerView.Adapter<AlreadyUploadedImageAdapter.ViewHolder>() {

    interface AlreadyAddedImageClickListener{
        fun onImageClick(position: Int)
    }

    inner class ViewHolder (itemView: View):RecyclerView.ViewHolder(itemView) {
        val imageView: ImageView = itemView.findViewById(R.id.product_image)
        val deleteImageBtn:LinearLayout = itemView.findViewById(R.id.delete_image_button)

        fun bind(url:String){

            deleteImageBtn.setOnClickListener {
                listener.onImageClick(adapterPosition)
            }

            Glide.with(itemView.context).load(url)
                .placeholder(R.drawable.as_square_placeholder)
                .into(imageView)
        }

    }

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): AlreadyUploadedImageAdapter.ViewHolder {
        val view: View = LayoutInflater.from(parent.context).inflate(R.layout.le_upload_already_image_item, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: AlreadyUploadedImageAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }
}