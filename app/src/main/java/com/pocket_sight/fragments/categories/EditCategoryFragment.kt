package com.pocket_sight.fragments.categories

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import androidx.core.view.MenuHost
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentEditAccountBinding
import com.pocket_sight.databinding.FragmentEditCategoryBinding
import com.pocket_sight.fragments.accounts.EditAccountFragmentArgs
import com.pocket_sight.fragments.accounts.EditAccountMenuProvider
import com.pocket_sight.fragments.accounts.RemoveAccountDialogFragment
import com.pocket_sight.types.accounts.Account
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.categories.CategoriesDao
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.Category
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


class EditCategoryFragment: Fragment(), RemoveCategoryDialogFragment.RemoveCategoryDialogListener {

    private lateinit var category: Category

    private lateinit var subcategoriesList: List<Subcategory>

    private lateinit var provisionalSubcategoriesList: List<ProvisionalSubcategory>

    private var _binding: FragmentEditCategoryBinding? = null
    val binding get() = _binding!!

    private lateinit var categoriesDatabase: CategoriesDao
    private lateinit var subcategoriesDatabase: SubcategoriesDao
    private lateinit var provisionalSubcategoriesDatabase: ProvisionalSubcategoriesDao

    private lateinit var adapter: SubcategoriesAdapter

    private val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val args = EditCategoryFragmentArgs.fromBundle(requireArguments())
        val categoryNumber: Int = args.categoryNumber
        val fromCategoriesFragment: Boolean = args.fromCategoriesFragment

        _binding = FragmentEditCategoryBinding.inflate(inflater, container, false)
        categoriesDatabase=
            CategoriesDatabase.getInstance(requireNotNull(this.activity).application).categoriesDatabaseDao
        subcategoriesDatabase=
            SubcategoriesDatabase.getInstance(requireNotNull(this.activity).application).subcategoriesDatabaseDao
        provisionalSubcategoriesDatabase=
            ProvisionalSubcategoriesDatabase.getInstance(requireNotNull(this.activity).application).provisionalSubcategoriesDatabaseDao

        val subCategoriesRecyclerView = binding.rvSubcategories

        val menuHost: MenuHost = requireActivity()
        val menuProvider = EditCategoryMenuProvider(this.requireContext(), this)
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)


        val editNameEditText: EditText = binding.editCategoryNameEditText
        val kindSpinner: Spinner = binding.editCategoryKindSpinner


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


        setCategoryInfo(
            this.requireContext(),
            fromCategoriesFragment,
            subCategoriesRecyclerView,
            categoryNumber,
            editNameEditText,
            kindSpinner
        )



//        val confirmEditAccountButton: Button = binding.confirmEditAccountButton
//        confirmEditAccountButton.setOnClickListener { view: View ->
//            handleConfirmChanges(
//                editAccountNumberEditText,
//                editNameEditText,
//                editBalanceEditText,
//                editSwitch,
//                view
//            )
//        }


        return binding.root
    }

    private fun setCategoryInfo(
        context: Context,
        fromCategoriesFragment: Boolean,
        subcategoriesRecyclerView: RecyclerView,
        categoryNumber: Int,
        editNameEditText: EditText,
        kindSpinner: Spinner
    ) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                category = categoriesDatabase.get(categoryNumber)
            }
            setInfo(
                category,
                editNameEditText,
                kindSpinner
            )

            withContext(Dispatchers.IO) {
                subcategoriesList = subcategoriesDatabase.getSubcategoriesWithParent(categoryNumber)
                if (fromCategoriesFragment) {
                    provisionalSubcategoriesDatabase.clearProvisionalSubcategories()
                    for (subcategory in subcategoriesList) {
                        val provisionalSubcategory = ProvisionalSubcategory(
                            provisionalSubcategoriesDatabase.getMaxNumber() + 1,
                            subcategory.name,
                            subcategory.parentNumber
                        )
                        provisionalSubcategoriesDatabase.insert(provisionalSubcategory)
                    }
                }
                provisionalSubcategoriesList = provisionalSubcategoriesDatabase.getAllProvisionalSubcategories()
            }

            adapter = SubcategoriesAdapter(context, provisionalSubcategoriesList)

            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL

            subcategoriesRecyclerView.adapter = adapter
            subcategoriesRecyclerView.layoutManager = layoutManager
        }
    }

    private suspend fun setInfo(
        category: Category,
        editNameEditText: EditText,
        kindSpinner: Spinner
    ) {
        withContext(Dispatchers.Main) {
            editNameEditText.setText(category.name)
            when (category.kind) {
                "Expense" -> kindSpinner.setSelection(0)
                "Income" -> kindSpinner.setSelection(1)
            }
        }
    }

    private fun handleConfirmChanges() {

    }


    fun addSubcategoryClicked() {
        NavHostFragment.findNavController(this).navigate(EditCategoryFragmentDirections.actionEditCategoryFragmentToAddSubcategoryFragment(category.number))
    }

    fun showRemoveCategoryDialog() {

    }

    override fun onRemoveCategoryDialogPositiveClick(dialog: DialogFragment) {
        TODO("Not yet implemented")
    }
}
