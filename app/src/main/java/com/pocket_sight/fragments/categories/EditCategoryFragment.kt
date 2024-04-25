package com.pocket_sight.fragments.categories

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.fragment.findNavController
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
import com.pocket_sight.types.transactions.TransactionsDao
import com.pocket_sight.types.transactions.TransactionsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode


class EditCategoryFragment: Fragment(), RemoveCategoryDialogFragment.RemoveCategoryDialogListener {

    private lateinit var category: Category

    private lateinit var subcategoriesList: List<Subcategory>

    private lateinit var provisionalSubcategoriesList: List<ProvisionalSubcategory>

    private var _binding: FragmentEditCategoryBinding? = null
    val binding get() = _binding!!

    private lateinit var categoriesDatabase: CategoriesDao
    private lateinit var subcategoriesDatabase: SubcategoriesDao
    private lateinit var provisionalSubcategoriesDatabase: ProvisionalSubcategoriesDao
    private lateinit var transactionsDatabase: TransactionsDao
    private lateinit var accountsDatabase: AccountsDao

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
        transactionsDatabase =
            TransactionsDatabase.getInstance(requireNotNull(this.activity).application).transactionsDao
        accountsDatabase=
            AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao

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

        val confirmEditCategoryButton: Button = binding.confirmEditCategoryButton
        confirmEditCategoryButton.setOnClickListener {view: View ->
            handleConfirmChanges(
                view,
                editNameEditText,
                kindSpinner
            )
        }

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
                            subcategory.parentNumber,
                            subcategory.number
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

    private fun handleConfirmChanges(
        view: View,
        editNameEditText: EditText,
        kindSpinner: Spinner
    ) {
        uiScope.launch {
            var newCategoryNameInDatabase: Boolean
            val newName = editNameEditText.text.toString()
            withContext(Dispatchers.IO) {
                newCategoryNameInDatabase = categoriesDatabase.nameInDatabase(newName)
            }

            if (newCategoryNameInDatabase && newName != category.name) {
                editNameEditText.error = "Category Name Already Exists"
                return@launch
            }

            // if subcategories were removed, need to update associated transactions
            val oldSubcategories = withContext(Dispatchers.IO) {
                subcategoriesDatabase.getSubcategoriesWithParent(category.number)
            }
            val newSubcategoriesNames = provisionalSubcategoriesList.map {
                it.name
            }

            // update subcategories database
            withContext(Dispatchers.IO) {
                subcategoriesDatabase.clearSubcategoriesWithParent(category.number)
                for (provisionalSubcategory in provisionalSubcategoriesList) {
                    val subcatNumber = if (provisionalSubcategory.associatedSubcategoryNumber == null) {
                        subcategoriesDatabase.getMaxNumber() + 1
                    } else {
                        provisionalSubcategory.associatedSubcategoryNumber!!
                    }
                    val newSubcategory = Subcategory(
                        //subcategoriesDatabase.getMaxNumber() + 1,
                        subcatNumber,
                        provisionalSubcategory.name,
                        provisionalSubcategory.parentNumber
                    )
                    subcategoriesDatabase.insert(newSubcategory)
                }
                // clear provisional subcategories
                provisionalSubcategoriesDatabase.clearProvisionalSubcategories()
            }

            val newSubcategoriesNumbers = withContext(Dispatchers.IO) {
                subcategoriesDatabase.getSubcategoriesWithParent(category.number)
            }.map {
                it.number
            }

            for (subcategory in oldSubcategories) {
                if (subcategory.number !in newSubcategoriesNumbers) {
                    // this subcategory has been removed
                    // need to update subcategoryNumber of associated transactions
                    withContext(Dispatchers.IO) {
                        transactionsDatabase.updateTransactionsWithRemovedSubcategory(subcategory.number, subcategory.name)
                    }
                }
            }


            // update category database
            withContext(Dispatchers.IO) {
                categoriesDatabase.delete(category)
                val newCategory = Category(
                    //categoriesDatabase.getMaxNumber() + 1,
                    category.number,
                    editNameEditText.text.toString(),
                    kindSpinner.selectedItem.toString()
                )
                categoriesDatabase.insert(newCategory)
            }

            // if kind changed, update value of associated transactions
            if (category.kind != kindSpinner.selectedItem.toString()) {
                // update account balances
                val associatedTransactions = withContext(Dispatchers.IO) {
                    transactionsDatabase.getTransactionsFromCategory(category.number)
                }
                withContext(Dispatchers.IO) {
                    for (transaction in associatedTransactions) {
                        val value = transaction.value
                        val account = accountsDatabase.get(transaction.accountNumber)
                        val oldBalance = account.balance
                        var newBalance = oldBalance - 2*value
                        newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                        accountsDatabase.updateBalance(transaction.accountNumber, newBalance)
                    }
                }

                // update transactions in database
                withContext(Dispatchers.IO) {
                    transactionsDatabase.changeValuesOfCategory(category.number)
                }
            }

            // move fragment
            view.findNavController().navigate(
                EditCategoryFragmentDirections.actionEditCategoryFragmentToCategoriesFragment()
            )
        }
    }


    fun addSubcategoryClicked() {
        NavHostFragment.findNavController(this).navigate(EditCategoryFragmentDirections.actionEditCategoryFragmentToAddSubcategoryFragment(category.number))
    }

    fun showRemoveCategoryDialog() {
        RemoveCategoryDialogFragment(this).show(this.parentFragmentManager, "RemoveCategoryDialog")
    }

    override fun onRemoveCategoryDialogPositiveClick(dialog: DialogFragment) {
        Toast.makeText(
            this.context,
            "Category Removed. Set category of associated transactions to null.",
            Toast.LENGTH_SHORT
        ).show()
        uiScope.launch {
            withContext(Dispatchers.IO) {

                // set category number of associated transactions to null
                val associatedTransactions = transactionsDatabase.getTransactionsFromCategory(category.number)
                for (transaction in associatedTransactions) {
                    val subcategoryNumber = transaction.subcategory
                    var oldSubcategoryName: String? = null
                    if (subcategoryNumber != null) {
                        oldSubcategoryName = subcategoriesDatabase.get(subcategoryNumber).name
                    }

                    transactionsDatabase.updateTransaction(
                        transaction.transactionId,
                        transaction.value,
                        transaction.accountNumber,
                        null,
                        null,
                        transaction.note,
                        oldSubcategoryName,
                        category.name
                    )
                }

                // delete category and associated subcategories from databases
                categoriesDatabase.delete(category)
                subcategoriesDatabase.clearSubcategoriesWithParent(category.number)
                provisionalSubcategoriesDatabase.clearProvisionalSubcategories()
            }
        }


        dialog.findNavController().navigate(R.id.categories_fragment)
        dialog.dismiss()
    }

}
