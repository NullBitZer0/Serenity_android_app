package com.example.serenity_mad_le3.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.GridView
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.navArgs
import com.example.serenity_mad_le3.R
import com.example.serenity_mad_le3.data.Prefs
import com.example.serenity_mad_le3.model.Habit
import java.text.SimpleDateFormat
import java.util.*

class CalendarFragment : Fragment() {
    private val args: CalendarFragmentArgs by navArgs()
    private lateinit var prefs: Prefs
    private var habit: Habit? = null
    private lateinit var gridView: GridView
    private lateinit var textHabitTitle: TextView
    private lateinit var calendarAdapter: CalendarAdapter
    private val calendar = Calendar.getInstance()
    private lateinit var dateFormat: SimpleDateFormat

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        val view = inflater.inflate(R.layout.fragment_calendar, container, false)
        gridView = view.findViewById(R.id.calendarGrid)
        textHabitTitle = view.findViewById(R.id.textHabitTitle)
        dateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
        return view
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        
        prefs = Prefs(requireContext())
        
        // Find the habit by ID
        val habits = prefs.getHabits()
        habit = habits.find { it.id == args.habitId }
        
        if (habit == null) {
            // Handle case where habit is not found
            requireActivity().onBackPressed()
            return
        }
        
        // Set habit title
        textHabitTitle.text = habit?.title
        
        // Set up the calendar
        setupCalendar()
    }
    
    private fun setupCalendar() {
        // CalendarAdapter shades days using the stored completion history for this habit.
        calendarAdapter = CalendarAdapter(
            requireContext(),
            calendar,
            habit?.completionHistory ?: mutableMapOf(),
            habit?.completedToday ?: false,
            dateFormat.format(Date())
        )
        gridView.adapter = calendarAdapter
        
        // Set up previous month button
        view?.findViewById<View>(R.id.btnPrevious)?.setOnClickListener {
            calendar.add(Calendar.MONTH, -1)
            updateCalendar()
        }
        
        // Set up next month button
        view?.findViewById<View>(R.id.btnNext)?.setOnClickListener {
            calendar.add(Calendar.MONTH, 1)
            updateCalendar()
        }
        
        // Set up month/year display
        updateMonthYearDisplay()
    }
    
    private fun updateCalendar() {
        // Refresh habit data to get current completion status
        val habits = prefs.getHabits()
        habit = habits.find { it.id == args.habitId }
        
        calendarAdapter.updateCalendar(
            calendar,
            habit?.completionHistory ?: mutableMapOf(),
            habit?.completedToday ?: false,
            dateFormat.format(Date())
        )
        updateMonthYearDisplay()
    }
    
    private fun updateMonthYearDisplay() {
        // Show the active month so users always know which segment of their streak they are viewing.
        val monthYearFormat = SimpleDateFormat("MMMM yyyy", Locale.getDefault())
        view?.findViewById<TextView>(R.id.textMonthYear)?.text = monthYearFormat.format(calendar.time)
    }
}
