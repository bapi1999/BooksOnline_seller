package com.sbdevs.booksonlineseller.otherclass

import android.content.Context
import com.google.firebase.Timestamp
import com.sbdevs.booksonlineseller.R
import java.util.*

class TimeDateAgo {



    // IMPORTANT METHOD
    private fun dateToTimeStamp():Timestamp{
        val newDate = Date();
        val cancellationData= Date(newDate.time - (1000 * 60 * 60 * 24))
        val cancellationTimestamp: Timestamp = Timestamp(cancellationData)
        return cancellationTimestamp
    }


    fun msToTimeAgo(context: Context,startDate:Date): String {
        val seconds = (System.currentTimeMillis() - startDate.time) / 1000f

        return when (true) {
            seconds < 60 -> context.resources.getQuantityString(R.plurals.seconds_ago, seconds.toInt(), seconds.toInt())
            seconds < 3600 -> {
                val minutes = seconds / 60f
                context.resources.getQuantityString(R.plurals.minutes_ago, minutes.toInt(), minutes.toInt())
            }
            seconds < 86400 -> {
                val hours = seconds / 3600f
                context.resources.getQuantityString(R.plurals.hours_ago, hours.toInt(), hours.toInt())
            }
            seconds < 604800 -> {
                val days = seconds / 86400f
                context.resources.getQuantityString(R.plurals.days_ago, days.toInt(), days.toInt())
            }
            seconds < 2_628_000 -> {
                val weeks = seconds / 604800f
                context.resources.getQuantityString(R.plurals.weeks_ago, weeks.toInt(), weeks.toInt())
            }
            seconds < 31_536_000 -> {
                val months = seconds / 2_628_000f
                context.resources.getQuantityString(R.plurals.months_ago, months.toInt(), months.toInt())
            }
            else -> {
                val years = seconds / 31_536_000f
                context.resources.getQuantityString(R.plurals.years_ago, years.toInt(), years.toInt())
            }
        }
    }



}