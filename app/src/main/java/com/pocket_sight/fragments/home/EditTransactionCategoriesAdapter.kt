package com.pocket_sight.fragments.home

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.types.accounts.Account
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


class EditTransactionCategoriesAdapter(val fragment: EditTransactionFragment, val context: Context, val categories: List<Category>): RecyclerView.Adapter<EditTransactionCategoriesAdapter.ViewHolder>() {

    private val database = CategoriesDatabase.getInstance(context).categoriesDatabaseDao
    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val nameView: TextView = itemView.findViewById(R.id.row_cat_or_subcat_name_text_view)
        val rowLayout: LinearLayout = itemView.findViewById(R.id.cat_or_subcat_rv_layout)

        init {
            rowLayout.setOnClickListener() {
                val position = adapterPosition
                val categoryNumber: Int = categories[position].number
                fragment.categoryClicked(fragment, context, categories[position])
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
        return categories.size
    }

    override fun onBindViewHolder(viewHolder: EditTransactionCategoriesAdapter.ViewHolder, position: Int) {
        val category = categories[position]
        viewHolder.nameView.text = category.name
    }
}
