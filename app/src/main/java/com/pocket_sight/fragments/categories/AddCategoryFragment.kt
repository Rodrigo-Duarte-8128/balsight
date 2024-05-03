package com.pocket_sight.fragments.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentAddCategoryBinding
import com.pocket_sight.types.categories.CategoriesDao
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.Category
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddCategoryFragment: Fragment() {
    private var _binding: FragmentAddCategoryBinding? = null
    val binding get() = _binding!!

    lateinit var database: CategoriesDao

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        database = CategoriesDatabase.getInstance(requireNotNull(this.activity).application).categoriesDatabaseDao

        val nameEditText: EditText = binding.categoryNameEditText
        val kindSpinner: Spinner = binding.categoryKindSpinner



        ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.kinds_array,
            R.layout.category_kind_spinner
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            kindSpinner.adapter = adapter
        }


        val addCategoryButton = binding.addCategoryButton
        addCategoryButton.setOnClickListener {view: View ->
            addCategoryClicked(
                view,
                nameEditText,
                kindSpinner
            )
        }


        return binding.root
    }


    private fun addCategoryClicked(view: View, nameEditText: EditText, kindSpinner: Spinner) {
        uiScope.launch {
            val categoryName = nameEditText.text.toString()
            val categoryKind = kindSpinner.selectedItem.toString()
            val accountNumber = getMaxAccountNumber() + 1

            val nameInDatabase: Boolean = withContext(Dispatchers.IO) {
                database.nameInDatabase(categoryName)
            }

            if (nameInDatabase) {
                nameEditText.error = "Category Name Already Exists"
                return@launch
            }

            val newCategory = Category(accountNumber, categoryName, categoryKind)

            insertInDatabase(newCategory)

            view.findNavController().navigate(R.id.categories_fragment)
        }
    }

    private suspend fun insertInDatabase(category: Category) {
        withContext(Dispatchers.IO) {
            database.insert(category)
        }
    }

    private suspend fun getMaxAccountNumber(): Int {
        return withContext(Dispatchers.IO) {
            database.getMaxNumber()
        }
    }

}
