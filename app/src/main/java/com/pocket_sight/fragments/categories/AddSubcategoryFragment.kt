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
import com.pocket_sight.databinding.FragmentAddSubcategoryBinding
import com.pocket_sight.fragments.accounts.EditAccountFragmentArgs
import com.pocket_sight.types.categories.CategoriesDao
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.ProvisionalSubcategoriesDao
import com.pocket_sight.types.categories.ProvisionalSubcategoriesDatabase
import com.pocket_sight.types.categories.ProvisionalSubcategory
import com.pocket_sight.types.categories.SubcategoriesDao
import com.pocket_sight.types.categories.SubcategoriesDatabase
import com.pocket_sight.types.categories.Subcategory
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AddSubcategoryFragment: Fragment() {
    private var _binding: FragmentAddSubcategoryBinding? = null
    val binding get() = _binding!!

    lateinit var database: ProvisionalSubcategoriesDao

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddSubcategoryBinding.inflate(inflater, container, false)
        database = ProvisionalSubcategoriesDatabase.getInstance(requireNotNull(this.activity).application).provisionalSubcategoriesDatabaseDao


        val args = AddSubcategoryFragmentArgs.fromBundle(requireArguments())

        val nameEditText: EditText = binding.subcategoryNameEditText

        val addSubcategoryButton = binding.addSubcategoryButton
        addSubcategoryButton.setOnClickListener {view: View ->
            addSubcategoryClicked(
                view,
                nameEditText,
                args.parentCategoryNumber
            )
        }


        return binding.root
    }

    private fun addSubcategoryClicked(view: View, nameEditText: EditText, parentCategoryNumber: Int) {
        uiScope.launch {
            var nameInDatabase = false
            withContext(Dispatchers.IO) {
                nameInDatabase = database.nameInDatabase(nameEditText.text.toString())
            }
            if (nameInDatabase) {
                nameEditText.error = "Subcategory Name Already Exists"
                return@launch
            }

            withContext(Dispatchers.IO) {
                val provisionalSubcategoryNumber: Int = database.getMaxNumber() + 1
                val newProvisionalSubcategory = ProvisionalSubcategory(
                    provisionalSubcategoryNumber,
                    nameEditText.text.toString(),
                    parentCategoryNumber,
                    null
                )
                database.insert(newProvisionalSubcategory)
            }
            view.findNavController().navigate(
                AddSubcategoryFragmentDirections.actionAddSubcategoryFragmentToEditCategoryFragment(
                    categoryNumber = parentCategoryNumber,
                    fromCategoriesFragment = false
                )
            )
        }

    }

}

