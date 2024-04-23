package com.pocket_sight.fragments.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.convertMonthIntToString
import com.pocket_sight.types.transactions.Transaction

class HomeAdapter(val context: Context, val acts: List<Transaction>): RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val dayMonthTextView: TextView = itemView.findViewById(R.id.row_act_day_month_text_view)
        val timeTextView: TextView = itemView.findViewById(R.id.row_act_time_text_view)
        val categoryView: TextView = itemView.findViewById(R.id.row_act_category_text_view)
        val noteView: TextView = itemView.findViewById(R.id.row_act_note_text_view)
        val valueView: TextView = itemView.findViewById(R.id.row_act_value_text_view)
        val rowLayout: LinearLayout = itemView.findViewById(R.id.acts_rv_row_layout)

        init {
            rowLayout.setOnClickListener {
                Toast.makeText(context, "act clicked!", Toast.LENGTH_SHORT).show()
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val actView = inflater.inflate(R.layout.acts_rv_row, parent, false)
        return ViewHolder(actView)
    }

    override fun getItemCount(): Int {
        return acts.size
    }

    override fun onBindViewHolder(viewHolder: HomeAdapter.ViewHolder, position: Int) {
        val act = acts[position]

        viewHolder.dayMonthTextView.text = "${act.day} ${convertMonthIntToString(act.month)}"
        viewHolder.timeTextView.text = "${act.hour}:${act.minutes}"
        viewHolder.categoryView.text = ""
        viewHolder.noteView.text = act.note
        val value = act.value
        val valueString = if (value >= 0) {
            viewHolder.valueView.setTextColor(ContextCompat.getColor(context, R.color.green))
            if (value == value.toInt().toDouble()) {
                "+ ${act.value.toInt()} \u20ac"
            } else {
                "+ ${act.value} \u20ac"
            }
        } else {
            viewHolder.valueView.setTextColor(ContextCompat.getColor(context, R.color.red))
            if (value == value.toInt().toDouble()) {
                "- ${act.value.toInt()} \u20ac"
            } else {
                "- ${act.value} \u20ac"
            }
        }
        viewHolder.valueView.text = valueString
    }
}