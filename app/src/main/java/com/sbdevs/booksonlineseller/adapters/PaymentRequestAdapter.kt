package com.sbdevs.booksonlineseller.adapters

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.models.PaymentRequestModel
import java.util.*

class PaymentRequestAdapter (var list: MutableList<PaymentRequestModel>): RecyclerView.Adapter<PaymentRequestAdapter.ViewHolder> () {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.le_earning_item,parent,false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class ViewHolder(itemView: View):RecyclerView.ViewHolder(itemView) {
        private val amountTextView: TextView = itemView.findViewById(R.id.amount_Text)
        private val timeTextView: TextView = itemView.findViewById(R.id.time_text)

        fun bind(item: PaymentRequestModel){
            val amount = item.amount
            val timeDate = item.time


            amountTextView.text = amount.toString()

            //val days7lay = Date(timeDelivered!!.time +(1000 * 60 * 60 * 24*timePeriod))
             val hours24Late = Date(timeDate.time +(1000 * 60 * 60 * 24))

            val difDate = Date( hours24Late.time - Date().time)

            timeTextView.text = msToTimeAgo(itemView.context,difDate.time)

        }

    }

    private fun msToTimeAgo(context: Context, diffLong:Long): String {
        val seconds =  diffLong/ 1000f

        return when (true) {
            seconds < 60 -> context.resources.getQuantityString(R.plurals.seconds_to_go, seconds.toInt(), seconds.toInt())
            seconds < 3600 -> {
                val minutes = seconds / 60f
                context.resources.getQuantityString(R.plurals.minutes_to_go, minutes.toInt(), minutes.toInt())
            }
            seconds < 86400 -> {
                val hours = seconds / 3600f
                context.resources.getQuantityString(R.plurals.hours_to_go, hours.toInt(), hours.toInt())
            }

            else -> {
                val days = seconds / 86400f
                context.resources.getQuantityString(R.plurals.days_to_go, days.toInt(), days.toInt())
            }
        }
    }

}