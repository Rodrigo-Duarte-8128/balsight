package com.pocket_sight.fragments.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.databinding.ActsRvRowBinding
import com.pocket_sight.types.Transaction

class HomeAdapter(val context: Context, val acts: List<Transaction>): RecyclerView.Adapter<HomeAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val dateView: TextView = itemView.findViewById(R.id.row_date_view)
        val categoryView: TextView = itemView.findViewById(R.id.row_category_button)
        val valueView: TextView = itemView.findViewById(R.id.row_value_view)
        val rowLayout: LinearLayout = itemView.findViewById(R.id.row_layout)

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
        viewHolder.dateView.text = act.date.dayOfMonth.toString()
        viewHolder.categoryView.text = act.category
        viewHolder.valueView.text = act.value.toString()
    }
}