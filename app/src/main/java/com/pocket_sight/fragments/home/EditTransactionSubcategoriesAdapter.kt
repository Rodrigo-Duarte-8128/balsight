package com.pocket_sight.fragments.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.types.categories.SubcategoriesDatabase
import com.pocket_sight.types.categories.Subcategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


class EditTransactionSubcategoriesAdapter(val fragment: EditTransactionFragment, val context: Context, val subcategories: List<Subcategory>): RecyclerView.Adapter<EditTransactionSubcategoriesAdapter.ViewHolder>() {

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val nameView: TextView = itemView.findViewById(R.id.row_cat_or_subcat_name_text_view)
        val rowLayout: LinearLayout = itemView.findViewById(R.id.cat_or_subcat_rv_layout)

        init {
            rowLayout.setOnClickListener() {
                val position = adapterPosition
                fragment.subcategoryClicked(fragment, context, subcategories[position])
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val actView = inflater.inflate(R.layout.cat_or_subcat_rv_row, parent, false)
        return ViewHolder(actView)
    }

    override fun getItemCount(): Int {
        return subcategories.size
    }

    override fun onBindViewHolder(viewHolder: EditTransactionSubcategoriesAdapter.ViewHolder, position: Int) {
        val subcategory = subcategories[position]
        viewHolder.nameView.text = subcategory.name
    }
}
