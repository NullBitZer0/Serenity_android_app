package com.example.serenity_mad_le3.ui.habits

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.serenity_mad_le3.R
import com.example.serenity_mad_le3.data.Prefs
import com.example.serenity_mad_le3.model.Habit
import com.google.android.material.floatingactionbutton.FloatingActionButton
import java.util.UUID

class HabitsFragment : Fragment() {
    private lateinit var prefs: Prefs
    private lateinit var adapter: HabitAdapter
    private lateinit var habits: MutableList<Habit>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View? = inflater.inflate(R.layout.fragment_habits, container, false)

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        prefs = Prefs(requireContext())
        habits = prefs.getHabits() // Reset is handled in Prefs.getHabits()
        adapter = HabitAdapter(
            habits,
            onIncrement = { habit ->
                val target = habit.targetCount.coerceAtLeast(1)
                if (habit.currentCount < target) {
                    habit.currentCount += 1
                }
                val wasCompleted = habit.completedToday
                habit.completedToday = habit.currentCount >= target
                
                // Record completion in history if the habit just got completed
                if (!wasCompleted && habit.completedToday) {
                    prefs.recordHabitCompletion(habit.id, true)
                }
                
                prefs.saveHabits(habits)
                notifyChangeFor(habit)
            },
            onToggle = { habit, checked ->
                val target = habit.targetCount.coerceAtLeast(1)
                val wasCompleted = habit.completedToday
                if (checked) {
                    habit.currentCount = target
                    habit.completedToday = true
                } else {
                    habit.currentCount = 0
                    habit.completedToday = false
                }
                
                // Record completion in history
                if (wasCompleted != habit.completedToday) {
                    prefs.recordHabitCompletion(habit.id, habit.completedToday)
                }
                
                prefs.saveHabits(habits)
                notifyChangeFor(habit)
            },
            onEdit = { habit -> showHabitDialog(habit) },
            onDelete = { habit ->
                val index = habits.indexOf(habit)
                if (index >= 0) {
                    habits.removeAt(index)
                    prefs.saveHabits(habits)
                    adapter.notifyItemRemoved(index)
                } else {
                    habits.remove(habit)
                    prefs.saveHabits(habits)
                    adapter.notifyDataSetChanged()
                }
            },
            onCalendar = { habit -> 
                // Navigate to calendar fragment with habit ID
                val action = HabitsFragmentDirections.actionHabitsToCalendar(habit.id)
                findNavController().navigate(action)
            }
        )

        val rv = view.findViewById<RecyclerView>(R.id.recyclerHabits)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = adapter

        view.findViewById<FloatingActionButton>(R.id.fabAddHabit).setOnClickListener { showHabitDialog() }
    }

    private fun showHabitDialog(habit: Habit? = null) {
        val dialogView = layoutInflater.inflate(R.layout.dialog_habit, null)
        val titleInput = dialogView.findViewById<EditText>(R.id.inputHabitTitle)
        val descriptionInput = dialogView.findViewById<EditText>(R.id.inputHabitDescription)
        val targetInput = dialogView.findViewById<EditText>(R.id.inputHabitTarget)
        val currentInput = dialogView.findViewById<EditText>(R.id.inputHabitCurrent)
        val emojiInput = dialogView.findViewById<EditText>(R.id.inputHabitEmoji)

        if (habit != null) {
            titleInput.setText(habit.title)
            descriptionInput.setText(habit.description.orEmpty())
            targetInput.setText(habit.targetCount.coerceAtLeast(1).toString())
            currentInput.setText(habit.currentCount.coerceAtLeast(0).toString())
            emojiInput.setText(habit.emoji?.takeIf { it.isNotBlank() } ?: "")
        } else {
            targetInput.setText(DEFAULT_TARGET.toString())
            currentInput.setText(DEFAULT_CURRENT.toString())
        }

        val titleRes = if (habit == null) R.string.add_habit else R.string.edit_habit
        AlertDialog.Builder(requireContext())
            .setTitle(titleRes)
            .setView(dialogView)
            .setPositiveButton(R.string.save) { d, _ ->
                val title = titleInput.text.toString().trim()
                if (title.isNotEmpty()) {
                    val description = descriptionInput.text.toString().trim()
                    val emoji = emojiInput.text.toString().trim().ifEmpty { habit?.emoji ?: DEFAULT_EMOJI }
                    val target = targetInput.text.toString().toIntOrNull()?.takeIf { it > 0 }
                        ?: (habit?.targetCount?.takeIf { it > 0 } ?: DEFAULT_TARGET)
                    val currentRaw = currentInput.text.toString().toIntOrNull() ?: (habit?.currentCount ?: DEFAULT_CURRENT)
                    val current = currentRaw.coerceIn(0, target)
                    val wasCompleted = habit?.completedToday ?: false

                    if (habit == null) {
                        val newHabit = Habit(
                            id = UUID.randomUUID().toString(),
                            title = title,
                            description = description,
                            emoji = emoji,
                            currentCount = current,
                            targetCount = target,
                            completedToday = current >= target
                        )
                        habits.add(newHabit)
                    } else {
                        habit.title = title
                        habit.description = description
                        habit.emoji = emoji
                        habit.targetCount = target
                        habit.currentCount = current
                        habit.completedToday = current >= target
                        
                        // Record completion in history if state changed
                        if (wasCompleted != habit.completedToday) {
                            prefs.recordHabitCompletion(habit.id, habit.completedToday)
                        }
                    }
                    prefs.saveHabits(habits)
                    adapter.update(habits)
                }
                d.dismiss()
            }
            .setNegativeButton(R.string.cancel) { d, _ -> d.dismiss() }
            .show()
    }

    private fun notifyChangeFor(habit: Habit) {
        val index = habits.indexOf(habit)
        if (index != -1) {
            adapter.notifyItemChanged(index)
        } else {
            adapter.notifyDataSetChanged()
        }
    }

    companion object {
        private const val DEFAULT_TARGET = 1
        private const val DEFAULT_CURRENT = 0
        private const val DEFAULT_EMOJI = Habit.DEFAULT_EMOJI
    }
}