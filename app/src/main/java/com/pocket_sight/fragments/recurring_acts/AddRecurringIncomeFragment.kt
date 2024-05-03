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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.databinding.FragmentAddRecurringIncomeBinding
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

class AddRecurringIncomeFragment: Fragment() {
    private var _binding: FragmentAddRecurringIncomeBinding? = null
    val binding get() = _binding!!

    private lateinit var categoriesDatabase: CategoriesDao
    lateinit var subcategoriesDatabase: SubcategoriesDao
    private lateinit var recurringTransactionsDatabase: RecurringTransactionsDao
    lateinit var accountsDatabase: AccountsDao

    private lateinit var categoriesAdapter: AddRecurringIncomeCategoriesAdapter
    private lateinit var subcategoriesAdapter: AddRecurringIncomeSubcategoriesAdapter

    lateinit var recyclerView: RecyclerView

    lateinit var nameEditText: EditText
    lateinit var chooseTextView: TextView
    lateinit var categoryTextView: TextView
    lateinit var subcategoryTextView: TextView
    lateinit var valueEditText: EditText
    lateinit var monthDayEditText: EditText

    lateinit var args: AddRecurringIncomeFragmentArgs

    private var selectedCategoryNumber: Int? = null
    private var selectedSubcategoryNumber: Int? = null

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddRecurringIncomeBinding.inflate(inflater, container, false)

        args = AddRecurringIncomeFragmentArgs.fromBundle(requireArguments())


        val menuHost: MenuHost = requireActivity()
        val menuProvider = AddRecurringIncomeMenuProvider(this.requireContext(), this)
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        categoriesDatabase = CategoriesDatabase.getInstance(requireNotNull(this.activity).application).categoriesDatabaseDao
        subcategoriesDatabase = SubcategoriesDatabase.getInstance(requireNotNull(this.activity).application).subcategoriesDatabaseDao
        recurringTransactionsDatabase = RecurringTransactionsDatabase.getInstance(requireNotNull(this.activity).application).recurringTransactionsDao
        accountsDatabase = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao

        nameEditText = binding.addRecurringIncomeNameEditText
        valueEditText = binding.addRecurringIncomeValueEditText
        monthDayEditText = binding.addRecurringIncomeMonthDayEditText

        chooseTextView = binding.addRecurringIncomeChooseTextView
        categoryTextView = binding.addRecurringIncomeCategoryTextView
        subcategoryTextView = binding.addRecurringIncomeSubcategoryTextView

        recyclerView = binding.addRecurringIncomeRv

        buildFragmentInfo(this, this.requireContext(), recyclerView)

        val addRecurringIncomeButton: Button = binding.addRecurringIncomeButton
        addRecurringIncomeButton.setOnClickListener {view: View ->
            addRecurringIncome(this.requireContext(), view, valueEditText)
        }


        return binding.root
    }

    private fun buildFragmentInfo(fragment: AddRecurringIncomeFragment, context: Context, recyclerView: RecyclerView) {
        uiScope.launch {
            val valueString = args.valueString
            if (valueString != "-1") {
                valueEditText.setText(valueString)
            }
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

            nameEditText.setText(args.name)
            val monthDayInt = args.monthDay
            if (monthDayInt in 1..28) {
                monthDayEditText.setText(monthDayInt.toString())
            }

            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = layoutManager

            if (showingSubcategories) {
                val subcategories = withContext(Dispatchers.IO) {
                    subcategoriesDatabase.getSubcategoriesWithParent(selectedCategoryNumber!!)
                }
                subcategoriesAdapter = AddRecurringIncomeSubcategoriesAdapter(fragment, context, subcategories)
                recyclerView.adapter = subcategoriesAdapter

                categoryTextView.text = withContext(Dispatchers.IO) {
                    categoriesDatabase.get(selectedCategoryNumber!!).name
                }
            } else {
                val categories = withContext(Dispatchers.IO) {
                    categoriesDatabase.getAllCategories().filter {
                        it.kind == "Income"
                    }
                }

                categoriesAdapter = AddRecurringIncomeCategoriesAdapter(fragment, context, categories)
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
        }
    }

    fun categoryClicked(fragment: AddRecurringIncomeFragment, context: Context, category: Category) {
        uiScope.launch {
            chooseTextView.text = "Choose Subcategory"
            categoryTextView.text = category.name
            selectedCategoryNumber = category.number
            subcategoryTextView.text = ""
            selectedSubcategoryNumber = null
            val subcategories = withContext(Dispatchers.IO) {
                subcategoriesDatabase.getSubcategoriesWithParent(category.number)
            }
            subcategoriesAdapter = AddRecurringIncomeSubcategoriesAdapter(fragment, context, subcategories)
            recyclerView.adapter = subcategoriesAdapter
        }
    }

    fun subcategoryClicked(fragment: AddRecurringIncomeFragment, context: Context, subcategory: Subcategory) {
        uiScope.launch {
            chooseTextView.text = "Choose Category"
            subcategoryTextView.text = subcategory.name
            selectedSubcategoryNumber = subcategory.number
            val categories = withContext(Dispatchers.IO) {
                categoriesDatabase.getAllCategories().filter {
                    it.kind == "Income"
                }
            }
            categoriesAdapter = AddRecurringIncomeCategoriesAdapter(fragment, context, categories)
            recyclerView.adapter = categoriesAdapter
        }
    }

    fun moreOptionsClicked() {
        val valueString = if (valueEditText.text.toString() == "") {
            "-1"
        } else {
            valueEditText.text.toString()
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
        var monthDayInt = -1
        if (monthDayString != "") {
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
        }

        this.findNavController().navigate(AddRecurringIncomeFragmentDirections.actionAddRecurringIncomeFragmentToRecurringActMoreOptionsFragment(
            "add_recurring_income_fragment",
            args.timeMillis,
            args.timeMillis,
            args.accountNumber,
            valueString,
            selectedCatInt,
            selectedSubcatInt,
            args.note,
            monthDayInt,
            nameEditText.text.toString()
        ))
    }

    private fun addRecurringIncome(context: Context, view: View, valueEditText: EditText) {
        uiScope.launch {
            val valueString = valueEditText.text.toString()
            var value: Double
            val monthDayString = monthDayEditText.text.toString()

            try {
                value = valueString.toDouble()
                value = value.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            } catch (e: Exception) {
                valueEditText.error = "Invalid Value"
                return@launch
            }

            if (selectedCategoryNumber == null) {
                Toast.makeText(context, "No Category Selected", Toast.LENGTH_SHORT).show()
                return@launch
            }

            val monthDayInt: Int
            try {
                monthDayInt = monthDayString.toInt()

            } catch (e: Exception) {
                monthDayEditText.error = "Invalid Month Day"
                return@launch
            }

            if (monthDayInt < 1 || monthDayInt > 28) {
                monthDayEditText.error = "Invalid Month Day"
                return@launch
            }

            withContext(Dispatchers.IO) {
                val idsList = recurringTransactionsDatabase.getAllIds()
                val maxId: Int = idsList.maxOrNull() ?: 0
                var firstAvailableId: Int = 1

                for (num in 1..maxId + 1) {
                    if (num !in idsList) {
                        firstAvailableId = num
                    }
                }
                val millis = args.timeMillis
                val dateTime = convertTimeMillisToLocalDateTime(millis)

                val newRecurringTransaction = RecurringTransaction(
                    firstAvailableId,
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
                    null,
                    null,
                    null
                )
                recurringTransactionsDatabase.insert(newRecurringTransaction)
            }
            view.findNavController().navigate(AddRecurringIncomeFragmentDirections.actionAddRecurringIncomeFragmentToRecurringActsFragment())
        }
    }
}
