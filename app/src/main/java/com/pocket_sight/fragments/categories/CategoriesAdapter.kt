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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


class CategoriesAdapter(val context: Context, val categories: List<Category>): RecyclerView.Adapter<CategoriesAdapter.ViewHolder>() {

    private val database = CategoriesDatabase.getInstance(context).categoriesDatabaseDao
    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val categoryNameView: TextView = itemView.findViewById(R.id.row_category_name_text_view)
        val categoryKindView: TextView = itemView.findViewById(R.id.row_category_kind_text_view)
        val rowLayout: LinearLayout = itemView.findViewById(R.id.categories_rv_row_layout)

        init {
            rowLayout.setOnClickListener() {
                val position = adapterPosition
                val categoryNumber: Int = categories[position].number
                //itemView.findNavController().navigate(
                    //CategoriesFragmentDirections.actionAccountsFragmentToEditAccountFragment(accountNumber)
                //)
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val actView = inflater.inflate(R.layout.categories_rv_row, parent, false)
        return ViewHolder(actView)
    }

    override fun getItemCount(): Int {
        return categories.size
    }

    override fun onBindViewHolder(viewHolder: CategoriesAdapter.ViewHolder, position: Int) {
        val category = categories[position]
        viewHolder.categoryNameView.text = category.name
        viewHolder.categoryKindView.text = category.kind
    }
}
