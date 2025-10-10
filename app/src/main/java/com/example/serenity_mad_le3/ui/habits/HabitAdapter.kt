package com.example.serenity_mad_le3.ui.habits

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serenity_mad_le3.R
import com.example.serenity_mad_le3.model.Habit
import kotlin.math.roundToInt

class HabitAdapter(
    private var items: MutableList<Habit>,
    private val onIncrement: (Habit) -> Unit,
    private val onToggle: (Habit, Boolean) -> Unit,
    private val onEdit: (Habit) -> Unit,
    private val onDelete: (Habit) -> Unit,
    private val onCalendar: (Habit) -> Unit
) : RecyclerView.Adapter<HabitAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val emoji: TextView = view.findViewById(R.id.textEmoji)
        val title: TextView = view.findViewById(R.id.textTitle)
        val description: TextView = view.findViewById(R.id.textDescription)
        val check: CheckBox = view.findViewById(R.id.checkComplete)
        val progressValue: TextView = view.findViewById(R.id.textProgressValue)
        val progressPercent: TextView = view.findViewById(R.id.textProgressPercent)
        val progressBar: ProgressBar = view.findViewById(R.id.progressBar)
        val edit: ImageButton = view.findViewById(R.id.btnEdit)
        val delete: ImageButton = view.findViewById(R.id.btnDelete)
        val calendar: ImageButton = view.findViewById(R.id.btnCalendar)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        // Each card is inflated on demand; RecyclerView handles pooling for us.
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_habit, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.emoji.text = item.emoji?.takeIf { it.isNotBlank() } ?: Habit.DEFAULT_EMOJI
        holder.title.text = item.title

        val descriptionText = item.description?.trim().orEmpty()
        if (descriptionText.isEmpty()) {
            holder.description.visibility = View.GONE
        } else {
            holder.description.visibility = View.VISIBLE
            holder.description.text = descriptionText
        }

        val target = item.targetCount.coerceAtLeast(1)
        val current = item.currentCount.coerceIn(0, target)
        val percent = ((current.toFloat() / target) * 100).roundToInt().coerceIn(0, 100)
        val ctx = holder.itemView.context
        holder.progressValue.text = ctx.getString(R.string.habit_progress_format, current, target)
        holder.progressPercent.text = ctx.getString(R.string.habit_percent_format, percent)
        holder.progressBar.max = 100
        holder.progressBar.progress = percent

        holder.check.setOnCheckedChangeListener(null)
        holder.check.isChecked = item.completedToday
        // Delegate changes back to the fragment so it can persist and update history.
        holder.check.setOnCheckedChangeListener { _, isChecked -> onToggle(item, isChecked) }

        // Tap targets address the most common actions without opening overflow menus.
        holder.itemView.setOnClickListener { onIncrement(item) }
        holder.edit.setOnClickListener { onEdit(item) }
        holder.delete.setOnClickListener { onDelete(item) }
        holder.calendar.setOnClickListener { onCalendar(item) }
    }

    fun update(newItems: MutableList<Habit>) {
        items = newItems
        notifyDataSetChanged()
    }
}
