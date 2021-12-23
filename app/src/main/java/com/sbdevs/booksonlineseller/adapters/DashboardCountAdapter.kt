package com.sbdevs.booksonlineseller.adapters

import android.graphics.Color.parseColor
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.models.DashboardCountModel

class DashboardCountAdapter(var list: MutableList<DashboardCountModel>):RecyclerView.Adapter<DashboardCountAdapter.ViewHolder>() {


    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): DashboardCountAdapter.ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_dashboard_counter_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: DashboardCountAdapter.ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    class ViewHolder(itemView:View):RecyclerView.ViewHolder(itemView) {
        val imageView:ImageView = itemView.findViewById(R.id.item_icon)
        private val imageContainer:LinearLayout = itemView.findViewById(R.id.linearLayout10)
        private val itemTitle:TextView = itemView.findViewById(R.id.item_name)
        private val itemCount:TextView = itemView.findViewById(R.id.item_count)

        fun bind(item:DashboardCountModel) {
            itemTitle.text = item.title
            itemCount.text = item.count
            imageContainer.backgroundTintList = AppCompatResources.getColorStateList(itemView.context,item.backgroundColor)
            Glide.with(itemView.context).load(item.icon).into(imageView)
        }

    }
}