package com.pocket_sight.fragments.recurring_acts

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
import com.pocket_sight.databinding.FragmentEditRecurringTransactionBinding
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.categories.CategoriesDao
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.Category
import com.pocket_sight.types.categories.SubcategoriesDao
import com.pocket_sight.types.categories.SubcategoriesDatabase
import com.pocket_sight.types.categories.Subcategory
import com.pocket_sight.types.recurring.RecurringTransaction
import com.pocket_sight.types.recurring.RecurringTransactionsDao
import com.pocket_sight.types.recurring.RecurringTransactionsDatabase
import com.pocket_sight.types.transactions.convertTimeMillisToLocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode



class EditRecurringTransactionFragment: Fragment(), RemoveRecurringTransactionDialogFragment.RemoveRecurringTransactionDialogListener{
    private var _binding: FragmentEditRecurringTransactionBinding? = null
    val binding get() = _binding!!

    private lateinit var categoriesDatabase: CategoriesDao
    lateinit var subcategoriesDatabase: SubcategoriesDao
    private lateinit var recurringTransactionsDatabase: RecurringTransactionsDao
    lateinit var accountsDatabase: AccountsDao

    private lateinit var categoriesAdapter: EditRecurringTransactionCategoriesAdapter
    private lateinit var subcategoriesAdapter: EditRecurringTransactionSubcategoriesAdapter

    private lateinit var recyclerView: RecyclerView

    lateinit var nameEditText: EditText
    private lateinit var monthDayEditText: EditText
    lateinit var valueEditText: EditText
    private lateinit var chooseTextView: TextView
    private lateinit var categoryTextView: TextView
    private lateinit var subcategoryTextView: TextView

    lateinit var args: EditRecurringTransactionFragmentArgs

    private lateinit var recurringTransaction: RecurringTransaction
    var kind = "Expense"

    private var selectedCategoryNumber: Int? = null
    private var selectedSubcategoryNumber: Int? = null

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditRecurringTransactionBinding.inflate(inflater, container, false)

        args = EditRecurringTransactionFragmentArgs.fromBundle(requireArguments())

        val menuHost: MenuHost = requireActivity()
        val menuProvider = EditRecurringTransactionMenuProvider(this.requireContext(), this)
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        categoriesDatabase = CategoriesDatabase.getInstance(requireNotNull(this.activity).application).categoriesDatabaseDao
        subcategoriesDatabase = SubcategoriesDatabase.getInstance(requireNotNull(this.activity).application).subcategoriesDatabaseDao
        recurringTransactionsDatabase = RecurringTransactionsDatabase.getInstance(requireNotNull(this.activity).application).recurringTransactionsDao
        accountsDatabase = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao

        nameEditText = binding.editRecurringTransactionNameEditText
        valueEditText = binding.editRecurringTransactionValueEditText
        monthDayEditText = binding.editRecurringTransactionMonthDayEditText

        chooseTextView = binding.editRecurringTransactionChooseTextView
        categoryTextView = binding.editRecurringTransactionCategoryTextView
        subcategoryTextView = binding.editRecurringTransactionSubcategoryTextView

        recyclerView = binding.editRecurringTransactionRv

        buildFragmentInfo()

        val confirmChangesButton: Button = binding.editRecurringTransactionButton
        confirmChangesButton.setOnClickListener {view: View ->
            confirmChangesClicked(
                this.requireContext(),
                view,
                valueEditText
            )
        }

        return binding.root
    }



    private fun buildFragmentInfo() {
        uiScope.launch {
            nameEditText.setText(args.name)

            val monthDayInt = args.monthDayInt
            monthDayEditText.setText(monthDayInt.toString())


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
                subcategoriesAdapter = EditRecurringTransactionSubcategoriesAdapter(this@EditRecurringTransactionFragment, this@EditRecurringTransactionFragment.requireContext(), subcategories)
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

                categoriesAdapter = EditRecurringTransactionCategoriesAdapter(
                    this@EditRecurringTransactionFragment,
                    this@EditRecurringTransactionFragment.requireContext(),
                    categories
                )
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

            recurringTransaction = withContext(Dispatchers.IO) {
                recurringTransactionsDatabase.get(args.recurringTransactionId)
            }
        }
    }

    fun categoryClicked(fragment: EditRecurringTransactionFragment, context: Context, category: Category) {
        uiScope.launch {
            chooseTextView.text = "Choose Subcategory"
            categoryTextView.text = category.name
            selectedCategoryNumber = category.number
            subcategoryTextView.text = ""
            selectedSubcategoryNumber = null
            val subcategories = withContext(Dispatchers.IO) {
                subcategoriesDatabase.getSubcategoriesWithParent(category.number)
            }
            subcategoriesAdapter = EditRecurringTransactionSubcategoriesAdapter(fragment, context, subcategories)
            recyclerView.adapter = subcategoriesAdapter
        }
    }

    fun subcategoryClicked(fragment: EditRecurringTransactionFragment, context: Context, subcategory: Subcategory) {
        uiScope.launch {
            chooseTextView.text = "Choose Category"
            subcategoryTextView.text = subcategory.name
            selectedSubcategoryNumber = subcategory.number
            val categories = withContext(Dispatchers.IO) {
                categoriesDatabase.getAllCategories().filter {
                    it.kind == kind
                }
            }
            categoriesAdapter = EditRecurringTransactionCategoriesAdapter(fragment, context, categories)
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

        val monthDayString = monthDayEditText.text.toString()
        var monthDayInt = 0
        try {
            monthDayInt = monthDayString.toInt()
        } catch (e: Exception) {
            monthDayEditText.error = "Invalid Month Day"
            return
        }

        if (monthDayInt < 1 || monthDayInt > 28) {
            monthDayEditText.error = "Invalid Month Day"
            return
        }

        this.findNavController().navigate(EditRecurringTransactionFragmentDirections.actionEditRecurringTransactionFragmentToEditRecurringTransactionMoreOptions(
            recurringTransaction.recurringTransactionId,
            args.startDateTimeMillis,
            args.accountNumber,
            valueString,
            selectedCatInt,
            selectedSubcatInt,
            args.note,
            nameEditText.text.toString(),
            monthDayInt
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

            val monthDayString = monthDayEditText.text.toString()
            var monthDayInt = 0
            try {
                monthDayInt = monthDayString.toInt()
            } catch (e: Exception) {
                monthDayEditText.error = "Invalid Month Day"
                return@launch
            }

            if (monthDayInt < 1 || monthDayInt > 28) {
                monthDayEditText.error = "Month Day Needs to be Between 1 and 28"
                return@launch
            }


            val millis = args.startDateTimeMillis
            val dateTime = convertTimeMillisToLocalDateTime(millis)


            withContext(Dispatchers.IO) {
                val newRecurringTransaction = RecurringTransaction(
                    recurringTransaction.recurringTransactionId,
                    nameEditText.text.toString(),
                    monthDayInt,
                    value,
                    args.note,
                    selectedCategoryNumber,
                    selectedSubcategoryNumber,
                    null,
                    null,
                    args.accountNumber,
                    dateTime.dayOfMonth,
                    dateTime.monthValue,
                    dateTime.year,
                    recurringTransaction.lastInstantiationDay,
                    recurringTransaction.lastInstantiationMonthInt,
                    recurringTransaction.lastInstantiationYear
                )
                recurringTransactionsDatabase.delete(recurringTransaction)
                recurringTransactionsDatabase.insert(newRecurringTransaction)
            }

            view.findNavController().navigate(
                EditRecurringTransactionFragmentDirections.actionEditRecurringTransactionFragmentToRecurringActsFragment()
            )
        }
    }

    fun showRemoveTransactionDialog() {
        RemoveRecurringTransactionDialogFragment(this).show(this.parentFragmentManager, "RemoveTransactionDialog")
    }

    override fun onRemoveRecurringTransactionDialogPositiveClick(dialog: DialogFragment) {
        uiScope.launch {
            // delete transaction from database
            withContext(Dispatchers.IO) {
                recurringTransactionsDatabase.delete(recurringTransaction)
            }
        }

        NavHostFragment.findNavController(this).navigate(
            EditRecurringTransactionFragmentDirections.actionEditRecurringTransactionFragmentToRecurringActsFragment()
        )
    }

}
