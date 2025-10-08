package com.example.serenity_mad_le3.ui.mood

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.example.serenity_mad_le3.R
import com.example.serenity_mad_le3.data.Prefs
import com.github.mikephil.charting.charts.LineChart
import com.github.mikephil.charting.components.XAxis
import com.github.mikephil.charting.formatter.IndexAxisValueFormatter
import com.github.mikephil.charting.data.Entry
import com.github.mikephil.charting.data.LineData
import com.github.mikephil.charting.data.LineDataSet
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MoodChartFragment : Fragment() {
    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        return inflater.inflate(R.layout.fragment_mood_chart, container, false)
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        val chart = view.findViewById<LineChart>(R.id.lineChart)
        val prefs = Prefs(requireContext())
        val moods = prefs.getMoods()

        // Build last 7 days labels and values (average per day)
        val cal = Calendar.getInstance()
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)

        val dayMillis = 24 * 60 * 60 * 1000L
        val entries = ArrayList<Entry>()
        val labels = ArrayList<String>()
        val fmt = SimpleDateFormat("EEE", Locale.getDefault())

        for (i in 6 downTo 0) {
            val start = cal.timeInMillis - i * dayMillis
            val end = start + dayMillis
            val dayMoods = moods.filter { it.timestamp in start until end }
            val value = if (dayMoods.isEmpty()) 0f else dayMoods.map { it.score }.average().toFloat()
            entries.add(Entry((6 - i).toFloat(), value))
            labels.add(fmt.format(start))
        }

        val dataSet = LineDataSet(entries, getString(R.string.chart_label)).apply {
            color = resources.getColor(R.color.teal_700, null)
            setCircleColor(color)
            lineWidth = 2f
            circleRadius = 4f
            mode = LineDataSet.Mode.CUBIC_BEZIER
            valueTextSize = 10f
        }
        chart.data = LineData(dataSet)
        chart.axisLeft.axisMinimum = 1f
        chart.axisLeft.axisMaximum = 9f
        chart.axisRight.isEnabled = false
        chart.description.isEnabled = false
        chart.xAxis.position = XAxis.XAxisPosition.BOTTOM
        chart.xAxis.granularity = 1f
        chart.xAxis.valueFormatter = IndexAxisValueFormatter(labels)
        chart.xAxis.setDrawGridLines(false)
        chart.legend.isEnabled = false
        chart.invalidate()
    }
}