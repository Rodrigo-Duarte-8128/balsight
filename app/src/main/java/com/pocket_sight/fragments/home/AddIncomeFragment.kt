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
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.databinding.FragmentAddIncomeBinding
import com.pocket_sight.fragments.accounts.AccountsAdapter
import com.pocket_sight.fragments.categories.EditCategoryFragmentArgs
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

class AddIncomeFragment: Fragment() {
    private var _binding: FragmentAddIncomeBinding? = null
    val binding get() = _binding!!

    lateinit var categoriesDatabase: CategoriesDao
    lateinit var subcategoriesDatabase: SubcategoriesDao
    lateinit var transactionsDatabase: TransactionsDao
    lateinit var accountsDatabase: AccountsDao

    lateinit var categoriesAdapter: AddIncomeCategoriesAdapter
    lateinit var subcategoriesAdapter: AddIncomeSubcategoriesAdapter

    lateinit var recyclerView: RecyclerView

    lateinit var chooseTextView: TextView
    lateinit var categoryTextView: TextView
    lateinit var subcategoryTextView: TextView
    lateinit var valueEditText: EditText

    lateinit var args: AddIncomeFragmentArgs

    private var selectedCategoryNumber: Int? = null
    private var selectedSubcategoryNumber: Int? = null

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddIncomeBinding.inflate(inflater, container, false)

        args = AddIncomeFragmentArgs.fromBundle(requireArguments())


        val menuHost: MenuHost = requireActivity()
        val menuProvider = AddIncomeMenuProvider(this.requireContext(), this)
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)

        categoriesDatabase = CategoriesDatabase.getInstance(requireNotNull(this.activity).application).categoriesDatabaseDao
        subcategoriesDatabase = SubcategoriesDatabase.getInstance(requireNotNull(this.activity).application).subcategoriesDatabaseDao
        transactionsDatabase = TransactionsDatabase.getInstance(requireNotNull(this.activity).application).transactionsDao
        accountsDatabase = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao

        valueEditText = binding.addIncomeValueEditText

        chooseTextView = binding.addIncomeChooseTextView
        categoryTextView = binding.addIncomeCategoryTextView
        subcategoryTextView = binding.addIncomeSubcategoryTextView

        recyclerView = binding.addIncomeRv

        buildFragmentInfo(this, this.requireContext(), recyclerView)

        val addIncomeButton: Button = binding.addIncomeButton
        addIncomeButton.setOnClickListener {view: View ->
            addIncome(this.requireContext(), view, valueEditText)
        }


        return binding.root
    }

    private fun buildFragmentInfo(fragment: AddIncomeFragment, context: Context, recyclerView: RecyclerView) {
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

            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL
            recyclerView.layoutManager = layoutManager

            if (showingSubcategories) {
                val subcategories = withContext(Dispatchers.IO) {
                    subcategoriesDatabase.getSubcategoriesWithParent(selectedCategoryNumber!!)
                }
                subcategoriesAdapter = AddIncomeSubcategoriesAdapter(fragment, context, subcategories)
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

                categoriesAdapter = AddIncomeCategoriesAdapter(fragment, context, categories)
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

    fun categoryClicked(fragment: AddIncomeFragment, context: Context, category: Category) {
        uiScope.launch {
            chooseTextView.text = "Choose Subcategory"
            categoryTextView.text = category.name
            selectedCategoryNumber = category.number
            subcategoryTextView.text = ""
            selectedSubcategoryNumber = null
            val subcategories = withContext(Dispatchers.IO) {
                subcategoriesDatabase.getSubcategoriesWithParent(category.number)
            }
            subcategoriesAdapter = AddIncomeSubcategoriesAdapter(fragment, context, subcategories)
            recyclerView.adapter = subcategoriesAdapter
        }
    }

    fun subcategoryClicked(fragment: AddIncomeFragment, context: Context, subcategory: Subcategory) {
        uiScope.launch {
            chooseTextView.text = "Choose Category"
            subcategoryTextView.text = subcategory.name
            selectedSubcategoryNumber = subcategory.number
            val categories = withContext(Dispatchers.IO) {
                categoriesDatabase.getAllCategories().filter {
                    it.kind == "Income"
                }
            }
            categoriesAdapter = AddIncomeCategoriesAdapter(fragment, context, categories)
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
        this.findNavController().navigate(AddIncomeFragmentDirections.actionAddIncomeFragmentToMoreOptionsFragment(
            "add_income_fragment",
            args.timeMillis,
            args.timeMillis,
            args.accountNumber,
            valueString,
            selectedCatInt,
            selectedSubcatInt,
            args.note
        ))
    }

    fun addIncome(context: Context, view: View, valueEditText: EditText) {
        uiScope.launch {
            val valueString = valueEditText.text.toString()
            var value: Double

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

            val millis = args.timeMillis
            val dateTime = convertTimeMillisToLocalDateTime(millis)

            val timeInDatabase = withContext(Dispatchers.IO) {
                transactionsDatabase.idInDatabase(millis)
            }

            if (timeInDatabase) {
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
                transactionsDatabase.insert(newTransaction)
            }

            //update associated account balance
            val account = withContext(Dispatchers.IO) {
                accountsDatabase.get(args.accountNumber)
            }
            var newBalance = account.balance + value
            newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            withContext(Dispatchers.IO) {
                accountsDatabase.updateBalance(args.accountNumber, newBalance)
            }

            view.findNavController().navigate(AddIncomeFragmentDirections.actionAddIncomeFragmentToHomeFragment())
        }
    }
}
