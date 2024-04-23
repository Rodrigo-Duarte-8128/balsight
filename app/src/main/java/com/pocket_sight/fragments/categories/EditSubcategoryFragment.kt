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
import com.pocket_sight.databinding.FragmentEditSubcategoryBinding
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
import kotlin.properties.Delegates


class EditSubcategoryFragment: Fragment() {
    private var _binding: FragmentEditSubcategoryBinding? = null
    val binding get() = _binding!!

    lateinit var database: ProvisionalSubcategoriesDao

    private var provisionalSubcategoryNumber: Int = 0

    private lateinit var provisionalSubcategory: ProvisionalSubcategory

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditSubcategoryBinding.inflate(inflater, container, false)
        database = ProvisionalSubcategoriesDatabase.getInstance(requireNotNull(this.activity).application).provisionalSubcategoriesDatabaseDao


        val args = EditSubcategoryFragmentArgs.fromBundle(requireArguments())

        provisionalSubcategoryNumber = args.provisionalSubcategoryNumber


        val nameEditText: EditText = binding.editSubcategoryNameEditText
        setInfo(nameEditText)

        val confirmChangesButton = binding.confirmEditSubcategoryButton
        confirmChangesButton.setOnClickListener {view: View ->
            handleConfirmChanges(
                view,
                nameEditText,
                args.parentCategoryNumber
            )
        }


        return binding.root
    }


    private fun setInfo(nameEditText: EditText) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                provisionalSubcategory = database.get(provisionalSubcategoryNumber)
            }
            nameEditText.setText(provisionalSubcategory.name)
        }
    }

    private fun handleConfirmChanges(view: View, nameEditText: EditText, parentCategoryNumber: Int) {
        uiScope.launch {
            var newNameInDatabase = false
            withContext(Dispatchers.IO) {
                newNameInDatabase = database.nameInDatabase(nameEditText.text.toString())
            }

            if (newNameInDatabase) {
                nameEditText.error = "Subcategory Name Already Exists"
                return@launch
            }

            withContext(Dispatchers.IO) {
                database.delete(provisionalSubcategory)
                val newProvisionalSubcategory = ProvisionalSubcategory(
                    database.getMaxNumber() + 1,
                    nameEditText.text.toString(),
                    provisionalSubcategory.parentNumber
                )
                database.insert(newProvisionalSubcategory)
            }
            view.findNavController().navigate(
                EditSubcategoryFragmentDirections.actionEditSubcategoryFragmentToEditCategoryFragment(
                    provisionalSubcategory.parentNumber,
                    false
                )
            )
        }
    }





}

