package com.example.serenity_mad_le3.ui.habits

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.example.serenity_mad_le3.R
import java.text.SimpleDateFormat
import java.util.*

class CalendarAdapter(
    private val context: Context,
    private val calendar: Calendar,
    private val completionHistory: Map<String, Boolean>,
    private val isTodayCompleted: Boolean,
    private val todayDate: String
) : BaseAdapter() {
    
    private val days = mutableListOf<DayInfo>()
    private val dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
    
    init {
        updateCalendar(calendar, completionHistory, isTodayCompleted, todayDate)
    }
    
    fun updateCalendar(
        newCalendar: Calendar,
        newCompletionHistory: Map<String, Boolean>,
        newIsTodayCompleted: Boolean,
        newTodayDate: String
    ) {
        days.clear()
        
        // Set calendar to first day of month
        val cal = newCalendar.clone() as Calendar
        cal.set(Calendar.DAY_OF_MONTH, 1)
        
        // Get day of week for first day (Sunday = 1, Monday = 2, etc.)
        val firstDayOfWeek = cal.get(Calendar.DAY_OF_WEEK)
        
        // Add empty cells for days before the first day of the month
        for (i in 1 until firstDayOfWeek) {
            days.add(DayInfo(0, false, false))
        }
        
        // Add days of the month
        val daysInMonth = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        for (day in 1..daysInMonth) {
            val dayCal = cal.clone() as Calendar
            dayCal.set(Calendar.DAY_OF_MONTH, day)
            val dateStr = dateFormat.format(dayCal.time)
            
            // Check if this is today's date
            val isToday = dateStr == newTodayDate
            val isCompleted = if (isToday) {
                // Use current completion status for today
                newIsTodayCompleted
            } else {
                // Use historical data for other days
                newCompletionHistory[dateStr] ?: false
            }
            
            days.add(DayInfo(day, true, isCompleted))
        }
        
        notifyDataSetChanged()
    }
    
    override fun getCount(): Int = days.size
    
    override fun getItem(position: Int): Any = days[position]
    
    override fun getItemId(position: Int): Long = position.toLong()
    
    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val holder: DayViewHolder
        
        if (convertView == null) {
            view = LayoutInflater.from(context).inflate(R.layout.item_calendar_day, parent, false)
            holder = DayViewHolder(view)
            view.tag = holder
        } else {
            view = convertView
            holder = view.tag as DayViewHolder
        }
        
        val dayInfo = days[position]
        
        if (dayInfo.isInMonth) {
            holder.textView.text = dayInfo.day.toString()
            holder.textView.visibility = View.VISIBLE
            
            // Set background based on completion status
            when {
                dayInfo.isCompleted -> {
                    // Completed - green background
                    holder.textView.setBackgroundResource(R.drawable.bg_calendar_completed)
                }
                else -> {
                    // Not completed - light gray background
                    holder.textView.setBackgroundResource(R.drawable.bg_calendar_not_completed)
                }
            }
        } else {
            holder.textView.visibility = View.INVISIBLE
        }
        
        return view
    }
    
    data class DayInfo(
        val day: Int,
        val isInMonth: Boolean,
        val isCompleted: Boolean
    )
    
    class DayViewHolder(view: View) {
        val textView: TextView = view.findViewById(R.id.textDay)
    }
}
