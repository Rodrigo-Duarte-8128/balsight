package com.pocket_sight.fragments.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
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
import com.pocket_sight.databinding.FragmentEditTransactionBinding
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.categories.CategoriesDao
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.Category
import com.pocket_sight.types.categories.SubcategoriesDao
import com.pocket_sight.types.categories.SubcategoriesDatabase
import com.pocket_sight.types.categories.Subcategory
import com.pocket_sight.types.transactions.Transaction
import com.pocket_sight.types.transactions.TransactionsDao
import com.pocket_sight.types.transactions.TransactionsDatabase
import com.pocket_sight.types.transactions.convertTimeMillisToLocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode


class EditTransactionFragment: Fragment(), RemoveTransactionDialogFragment.RemoveTransactionDialogListener {
    private var _binding: FragmentEditTransactionBinding? = null
    val binding get() = _binding!!

    private lateinit var categoriesDatabase: CategoriesDao
    lateinit var subcategoriesDatabase: SubcategoriesDao
    private lateinit var transactionsDatabase: TransactionsDao
    lateinit var accountsDatabase: AccountsDao

    private lateinit var categoriesAdapter: EditTransactionCategoriesAdapter
    private lateinit var subcategoriesAdapter: EditTransactionSubcategoriesAdapter

    private lateinit var recyclerView: RecyclerView

    private lateinit var chooseTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var subcategoryTextView: TextView
    lateinit var valueEditText: EditText

    lateinit var args: EditTransactionFragmentArgs

    lateinit var transaction: Transaction
    var kind = "Expense"

    private var selectedCategoryNumber: Int? = null
    private var selectedSubcategoryNumber: Int? = null

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditTransactionBinding.inflate(inflater, container, false)

        args = EditTransactionFragmentArgs.fromBundle(requireArguments())




        val menuHost: MenuHost = requireActivity()
        val menuProvider = EditTransactionMenuProvider(this.requireContext(), this)
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        categoriesDatabase = CategoriesDatabase.getInstance(requireNotNull(this.activity).application).categoriesDatabaseDao
        subcategoriesDatabase = SubcategoriesDatabase.getInstance(requireNotNull(this.activity).application).subcategoriesDatabaseDao
        transactionsDatabase = TransactionsDatabase.getInstance(requireNotNull(this.activity).application).transactionsDao
        accountsDatabase = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao

        valueEditText = binding.editTransactionValueEditText

        chooseTextView = binding.editTransactionChooseTextView
        categoryTextView = binding.editTransactionCategoryTextView
        subcategoryTextView = binding.editTransactionSubcategoryTextView

        recyclerView = binding.editTransactionRv

        buildFragmentInfo(this, this.requireContext(), recyclerView)

        val confirmChangesButton: Button = binding.editTransactionConfirmChangesButton
        confirmChangesButton.setOnClickListener {view: View ->
            confirmChangesClicked(
                this.requireContext(),
                view,
                valueEditText
            )
        }


        return binding.root
    }




    private fun buildFragmentInfo(fragment: EditTransactionFragment, context: Context, recyclerView: RecyclerView) {
        uiScope.launch {
            val value = args.valueString.toDouble()
            kind = if (value >= 0) {
                "Income"
            } else {"Expense"}

            val absoluteValue = if (kind == "Income") {
                value
            } else {
                -value
            }
            valueEditText.setText(absoluteValue.toString())

            selectedCategoryNumber = if (args.selectedCategoryNumber == -1) {
                null
            } else {args.selectedCategoryNumber}

            selectedSubcategoryNumber = if (args.selectedSubcategoryNumber == -1) {
                null
            } else {args.selectedSubcategoryNumber}

            var showingSubcategories = false

            if (selectedCategoryNumber != null && selectedSubcategoryNumber == null) {
                showingSubcategories = true
            }

            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = layoutManager

            if (showingSubcategories) {
                val subcategories = withContext(Dispatchers.IO) {
                    subcategoriesDatabase.getSubcategoriesWithParent(selectedCategoryNumber!!)
                }
                subcategoriesAdapter = EditTransactionSubcategoriesAdapter(fragment, context, subcategories)
                recyclerView.adapter = subcategoriesAdapter

                categoryTextView.text = withContext(Dispatchers.IO) {
                    categoriesDatabase.get(selectedCategoryNumber!!).name
                }
                chooseTextView.text = "Choose Subcategory"
            } else {
                val categories = withContext(Dispatchers.IO) {
                    categoriesDatabase.getAllCategories().filter {
                        it.kind == kind
                    }
                }

                categoriesAdapter = EditTransactionCategoriesAdapter(fragment, context, categories)
                recyclerView.adapter = categoriesAdapter

            }

            if (selectedCategoryNumber != null && selectedSubcategoryNumber != null) {
                categoryTextView.text = withContext(Dispatchers.IO) {
                    categoriesDatabase.get(selectedCategoryNumber!!).name
                }
                subcategoryTextView.text = withContext(Dispatchers.IO) {
                    subcategoriesDatabase.get(selectedSubcategoryNumber!!).name
                }
            }

            transaction = withContext(Dispatchers.IO) {
                transactionsDatabase.get(args.originalTimeMillis)
            }
        }
    }

    fun categoryClicked(fragment: EditTransactionFragment, context: Context, category: Category) {
        uiScope.launch {
            chooseTextView.text = "Choose Subcategory"
            categoryTextView.text = category.name
            selectedCategoryNumber = category.number
            subcategoryTextView.text = ""
            selectedSubcategoryNumber = null
            val subcategories = withContext(Dispatchers.IO) {
                subcategoriesDatabase.getSubcategoriesWithParent(category.number)
            }
            subcategoriesAdapter = EditTransactionSubcategoriesAdapter(fragment, context, subcategories)
            recyclerView.adapter = subcategoriesAdapter
        }
    }

    fun subcategoryClicked(fragment: EditTransactionFragment, context: Context, subcategory: Subcategory) {
        uiScope.launch {
            chooseTextView.text = "Choose Category"
            subcategoryTextView.text = subcategory.name
            selectedSubcategoryNumber = subcategory.number
            val categories = withContext(Dispatchers.IO) {
                categoriesDatabase.getAllCategories().filter {
                    it.kind == kind
                }
            }
            categoriesAdapter = EditTransactionCategoriesAdapter(fragment, context, categories)
            recyclerView.adapter = categoriesAdapter
        }
    }

    fun moreOptionsClicked() {
        var valueString = "-1"
        if (valueEditText.text.toString() != "") {
            val valueDouble = valueEditText.text.toString().toDouble()
            valueString = if (kind == "Expense") {
                (-valueDouble).toString()
            } else {
                valueDouble.toString()
            }

        }

        val selectedCatInt: Int = if (selectedCategoryNumber != null) {
            selectedCategoryNumber!!
        } else {
            -1
        }
        val selectedSubcatInt: Int = if (selectedSubcategoryNumber != null) {
            selectedSubcategoryNumber!!
        } else {
            -1
        }
        this.findNavController().navigate(EditTransactionFragmentDirections.actionEditTransactionFragmentToMoreOptionsFragment(
            "edit_transaction_fragment",
            args.originalTimeMillis,
            args.timeMillis,
            args.accountNumber,
            valueString,
            selectedCatInt,
            selectedSubcatInt,
            args.note
        ))
    }

    fun confirmChangesClicked(context: Context, view: View, valueEditText: EditText) {
        uiScope.launch {
            val valueString = valueEditText.text.toString()
            var value: Double

            try {
                value = valueString.toDouble()
                value = value.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                if (kind == "Expense") {
                    value = -value
                }
            } catch (e: Exception) {
                valueEditText.error = "Invalid Value"
                return@launch
            }

            if (selectedCategoryNumber == null) {
                Toast.makeText(context, "No Category Selected", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val millis = args.timeMillis
            val dateTime = convertTimeMillisToLocalDateTime(millis)

            val timeInDatabase = withContext(Dispatchers.IO) {
                transactionsDatabase.idInDatabase(millis)
            }

            if (millis != args.originalTimeMillis && timeInDatabase) {
                Toast.makeText(context, "Time and Date Taken by Another Transaction.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            withContext(Dispatchers.IO) {
                val newTransaction = Transaction(
                    millis,
                    dateTime.minute,
                    dateTime.hour,
                    dateTime.dayOfMonth,
                    dateTime.monthValue,
                    dateTime.year,
                    value,
                    args.accountNumber,
                    selectedCategoryNumber,
                    selectedSubcategoryNumber,
                    args.note,
                    null,
                    null
                )
                transactionsDatabase.deleteByKey(args.originalTimeMillis)
                transactionsDatabase.insert(newTransaction)
            }

            //update associated account balance
            val account = withContext(Dispatchers.IO) {
                accountsDatabase.get(args.accountNumber)
            }
            var newBalance = account.balance - transaction.value + value
            newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            withContext(Dispatchers.IO) {
                accountsDatabase.updateBalance(args.accountNumber, newBalance)
            }

            view.findNavController().navigate(EditTransactionFragmentDirections.actionEditTransactionFragmentToHomeFragment())
        }
    }

    fun showRemoveTransactionDialog() {
        RemoveTransactionDialogFragment(this).show(this.parentFragmentManager, "RemoveTransactionDialog")
    }

    override fun onRemoveTransactionDialogPositiveClick(dialog: DialogFragment) {
        uiScope.launch {
            // delete transaction from database
            withContext(Dispatchers.IO) {
                transactionsDatabase.delete(transaction)
            }

            // update associated account balance
            val associatedAccount = withContext(Dispatchers.IO) {
                accountsDatabase.get(transaction.accountNumber)
            }

            var newBalance = associatedAccount.balance - transaction.value
            newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()

            withContext(Dispatchers.IO) {
                accountsDatabase.updateBalance(transaction.accountNumber, newBalance)
            }
        }

        NavHostFragment.findNavController(this).navigate(
            EditTransactionFragmentDirections.actionEditTransactionFragmentToHomeFragment()
        )
    }
}
