package com.pocket_sight.fragments.categories

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.types.accounts.Account
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.Category
import com.pocket_sight.types.categories.SubcategoriesDatabase
import com.pocket_sight.types.categories.Subcategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


class SubcategoriesAdapter(val context: Context, val subcategories: List<Subcategory>): RecyclerView.Adapter<SubcategoriesAdapter.ViewHolder>() {

    private val database = SubcategoriesDatabase.getInstance(context).subcategoriesDatabaseDao
    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val subcategoryNameView: TextView = itemView.findViewById(R.id.row_subcategory_name_text_view)
        val rowLayout: LinearLayout = itemView.findViewById(R.id.subcategories_rv_row_layout)

        init {
            rowLayout.setOnClickListener() {
                val position = adapterPosition
                val subcategoryNumber: Int = subcategories[position].number
                //itemView.findNavController().navigate(
                //    CategoriesFragmentDirections.actionCategoriesFragmentToEditCategoryFragment(categoryNumber)
                //)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val actView = inflater.inflate(R.layout.subcategories_rv_row, parent, false)
        return ViewHolder(actView)
    }

    override fun getItemCount(): Int {
        return subcategories.size
    }

    override fun onBindViewHolder(viewHolder: SubcategoriesAdapter.ViewHolder, position: Int) {
        val subcategory = subcategories[position]
        viewHolder.subcategoryNameView.text = subcategory.name
    }
}
