package com.sbdevs.booksonlineseller.adapters


import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.sbdevs.booksonlineseller.R
import com.sbdevs.booksonlineseller.models.EarningModel
import java.util.*

class EarningAdapter(var list: List<EarningModel>): RecyclerView.Adapter<EarningAdapter.ViewHolder> () {


    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_earning,parent,false)
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

        fun bind(item:EarningModel){
            val amount = item.PRICE_SELLING_TOTAL
            val timeDelivered = item.Time_delivered
            val timePeriod = item.Time_period

            amountTextView.text = amount.toString()

            val days7lay = Date(timeDelivered!!.time +(1000 * 60 * 60 * 24*timePeriod))
           // val days7lay = Date(timeDelivered!!.time +(1000 * 60 * 60 * 24*2))
            val cal = Calendar.getInstance()
            cal.time = days7lay
            cal[Calendar.HOUR_OF_DAY] = 23
            cal[Calendar.MINUTE] = 59
            cal[Calendar.SECOND] = 50
            cal[Calendar.MILLISECOND] = 0
            val dd:Date = cal.time

            val difDate = Date( dd.time - Date().time)

            timeTextView.text = msToTimeAgo(itemView.context,difDate.time)

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
                seconds < 604800 -> {
                    val days = seconds / 86400f
                    context.resources.getQuantityString(R.plurals.days_to_go, days.toInt(), days.toInt())
                }
//                seconds < 2_628_000 -> {
//
//                }
                else -> {
                    val weeks = seconds / 604800f
                    context.resources.getQuantityString(R.plurals.weeks_to_go, weeks.toInt(), weeks.toInt())
                }
            }
        }

        private fun time1(){
            val newDate = Date()
            val calendar: Calendar = Calendar.getInstance()
            calendar.time = newDate

            val unroundedMinutes: Int = calendar.get(Calendar.MINUTE)
            val mod = unroundedMinutes % 15
            calendar.add(Calendar.MINUTE, if (mod < 8) -mod else 15 - mod)
            calendar.set(Calendar.SECOND, 0)



            val dateWithoutTime: Date = Date()
            val cal = Calendar.getInstance()
            cal.time = dateWithoutTime

            cal[Calendar.HOUR_OF_DAY] = 11
            cal[Calendar.MINUTE] = 59
            cal[Calendar.SECOND] = 50
            cal[Calendar.MILLISECOND] = 0
            val dd:Date = cal.time

        }

    }

}