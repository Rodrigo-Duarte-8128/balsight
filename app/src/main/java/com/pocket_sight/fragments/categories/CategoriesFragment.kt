
package com.pocket_sight.fragments.categories

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentCategoriesBinding
import com.pocket_sight.fragments.accounts.AccountsAdapter
import com.pocket_sight.types.categories.CategoriesDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class CategoriesFragment: Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    val binding get() = _binding!!

    lateinit var database: CategoriesDao

    lateinit var adapter: CategoriesAdapter

    val uiScope = CoroutineScope(Dispatchers.Main + Job())
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)


        database = CategoriesDatabase.getInstance(this.requireContext()).categoriesDatabaseDao
        val categoriesRecyclerView = binding.rvCategories
        buildFragmentInfo(this.requireContext(), categoriesRecyclerView)


        val addCategoryButton = binding.addCategoryFab
        addCategoryButton.setOnClickListener {view: View ->
            view.findNavController().navigate(R.id.add_category_fragment)
        }

        return binding.root
    }


    private fun buildFragmentInfo(context: Context, recyclerView: RecyclerView) {
        uiScope.launch {
            val categoriesList = getCategoriesList()
            adapter = CategoriesAdapter(context, categoriesList)

            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL

            recyclerView.adapter = adapter
            recyclerView.layoutManager = layoutManager
        }
    }

    private suspend fun getCategoriesList(): List<Category> {
        return withContext(Dispatchers.IO) {
            database.getAllCategories()
        }
    }
}
