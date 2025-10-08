package com.example.serenity_mad_le3.ui.mood

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.serenity_mad_le3.R
import com.example.serenity_mad_le3.model.Mood
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class MoodHistoryAdapter(private var items: MutableList<Mood>) : RecyclerView.Adapter<MoodHistoryAdapter.VH>() {
    class VH(v: View) : RecyclerView.ViewHolder(v) {
        val emoji: TextView = v.findViewById(R.id.textEmoji)
        val note: TextView = v.findViewById(R.id.textNote)
        val time: TextView = v.findViewById(R.id.textTime)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_mood, parent, false)
        return VH(v)
    }

    override fun getItemCount(): Int = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        val item = items[position]
        holder.emoji.text = item.emoji
        holder.note.text = item.note ?: ""
        val sdf = SimpleDateFormat("EEE, h:mm a", Locale.getDefault())
        holder.time.text = sdf.format(Date(item.timestamp))
    }

    fun update(newItems: MutableList<Mood>) {
        items = newItems
        notifyDataSetChanged()
    }
}

