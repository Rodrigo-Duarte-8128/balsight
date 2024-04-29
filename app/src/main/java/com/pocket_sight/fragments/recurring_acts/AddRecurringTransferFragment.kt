package com.pocket_sight.fragments.recurring_acts

import android.content.Context
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentAddExpenseBinding
import com.pocket_sight.databinding.FragmentAddRecurringTransferBinding
import com.pocket_sight.databinding.FragmentAddTransferBinding
import com.pocket_sight.fragments.accounts.AccountsAdapter
import com.pocket_sight.fragments.categories.EditCategoryFragmentArgs
import com.pocket_sight.types.accounts.Account
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.categories.CategoriesDao
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.Category
import com.pocket_sight.types.categories.SubcategoriesDao
import com.pocket_sight.types.categories.SubcategoriesDatabase
import com.pocket_sight.types.categories.Subcategory
import com.pocket_sight.types.recurring.RecurringTransaction
import com.pocket_sight.types.recurring.RecurringTransfer
import com.pocket_sight.types.recurring.RecurringTransferDao
import com.pocket_sight.types.recurring.RecurringTransferDatabase
import com.pocket_sight.types.transactions.Transaction
import com.pocket_sight.types.transactions.TransactionsDao
import com.pocket_sight.types.transactions.TransactionsDatabase
import com.pocket_sight.types.transactions.convertTimeMillisToLocalDateTime
import com.pocket_sight.types.transfers.Transfer
import com.pocket_sight.types.transfers.TransfersDao
import com.pocket_sight.types.transfers.TransfersDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class AddRecurringTransferFragment: Fragment() {
    private var _binding: FragmentAddRecurringTransferBinding? = null
    val binding get() = _binding!!

    lateinit var accountsDatabase: AccountsDao
    lateinit var recurringTransfersDatabase: RecurringTransferDao

    var timeMillis = 0L

    lateinit var accountSendingSpinner: Spinner
    lateinit var accountReceivingSpinner: Spinner
    lateinit var nameEditText: EditText
    lateinit var valueEditText: EditText
    lateinit var noteEditText: EditText
    lateinit var startDateEditText: EditText
    lateinit var monthDayEditText: EditText

    lateinit var args: AddRecurringTransferFragmentArgs

    lateinit var accountsStringsArray: Array<String>


    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddRecurringTransferBinding.inflate(inflater, container, false)

        args = AddRecurringTransferFragmentArgs.fromBundle(requireArguments())

        timeMillis = args.timeMillis


        accountsDatabase = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao
        recurringTransfersDatabase = RecurringTransferDatabase.getInstance(requireNotNull(this.activity).application).recurringTransferDao

        accountSendingSpinner = binding.addRecurringTransferAccountSendingSpinner
        accountReceivingSpinner = binding.addRecurringTransferAccountReceivingSpinner
        nameEditText = binding.addRecurringTransferNameEditText
        valueEditText = binding.addRecurringTransferValueEditText
        noteEditText = binding.addRecurringTransferNoteEditText
        startDateEditText = binding.addRecurringTransferStartDateEditText
        monthDayEditText = binding.addRecurringTransferMonthDayEditText


        buildFragmentInfo()

        val addRecurringTransferButton: Button = binding.addRecurringTransferButton
        addRecurringTransferButton.setOnClickListener {view: View ->
            addRecurringTransfer(view)
        }

        return binding.root
    }

    private fun buildFragmentInfo() {
        uiScope.launch {
            val accountsList: MutableList<Account> = withContext(Dispatchers.IO) {
                accountsDatabase.getAllAccounts()
            }
            val accountsStringsList = accountsList.map {
                "${it.number}. ${it.name}"
            }.toMutableList()
            accountsStringsList.add("Another")
            this@AddRecurringTransferFragment.accountsStringsArray = accountsStringsList.toTypedArray()

            val arrayAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
                this@AddRecurringTransferFragment.requireContext(),
                R.layout.category_kind_spinner,
                accountsStringsArray
            )
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            accountSendingSpinner.adapter = arrayAdapter
            accountReceivingSpinner.adapter = arrayAdapter
        }


        val dateTime: LocalDateTime = convertTimeMillisToLocalDateTime(timeMillis)
        var dayString = dateTime.dayOfMonth.toString()
        var monthString = dateTime.monthValue.toString()
        if (dayString.length == 1) {
            dayString = "0$dayString"
        }
        if (monthString.length == 1) {
            monthString = "0$monthString"
        }
        startDateEditText.setText("${dayString}/${monthString}/${dateTime.year}")

        startDateEditText.setOnClickListener {
            AddRecurringTransferDatePicker(dateTime.dayOfMonth, dateTime.monthValue - 1, dateTime.year, this).show(this.parentFragmentManager, "Pick Date")
        }
    }

    fun setDate(year: Int, month: Int, day: Int) {
        var dayString = day.toString()
        var monthString = (month + 1).toString()
        if (dayString.length == 1) {
            dayString = "0$dayString"
        }
        if (monthString.length == 1) {
            monthString = "0$monthString"
        }
        startDateEditText.setText("$dayString/$monthString/$year")
    }


    fun addRecurringTransfer(view: View) {
        uiScope.launch {
            val accountSendingString = accountSendingSpinner.selectedItem.toString()
            val accountReceivingString = accountReceivingSpinner.selectedItem.toString()

            if (accountSendingString == "Another" && accountReceivingString == "Another") {
                Toast.makeText(this@AddRecurringTransferFragment.requireContext(), "No Accounts Selected", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (accountSendingString == accountReceivingString) {
                Toast.makeText(this@AddRecurringTransferFragment.requireContext(), "The Two Accounts Are Equal", Toast.LENGTH_SHORT).show()
                return@launch
            }

            var accountSendingNumber: Int? = null
            var accountReceivingNumber: Int? = null

            if (accountSendingString != "Another") {
                accountSendingNumber = accountSendingString.split(".")[0].toInt()
            }
            if (accountReceivingString != "Another") {
                accountReceivingNumber = accountReceivingString.split(".")[0].toInt()
            }

            val valueString = valueEditText.text.toString()
            var value: Double
            try {
                value = valueString.toDouble()
                value = value.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            } catch (e: Exception) {
                valueEditText.error = "Invalid Value"
                return@launch
            }

            val monthDayString = monthDayEditText.text.toString()
            var monthDayInt: Int
            try {
                monthDayInt = monthDayString.toInt()
            } catch (e: Exception) {
                monthDayEditText.error = "Invalid Month Day"
                return@launch
            }

            if (monthDayInt < 1 || monthDayInt > 28) {
                monthDayEditText.error = "Month Day is between 1 and 28"
                return@launch
            }

            val dateStringList: List<String> = startDateEditText.text.toString().split("/")

            var dayInt = 0
            var monthInt = 0
            var yearInt = 0

            try {
                dayInt = dateStringList[0].toInt()
                monthInt = dateStringList[1].toInt()
                yearInt = dateStringList[2].toInt()
            } catch (e: Exception) {
                startDateEditText.error = "Invalid Date"
                return@launch
            }


            withContext(Dispatchers.IO) {
                val idsList = recurringTransfersDatabase.getAllIds()
                val maxId: Int = idsList.maxOrNull() ?: 0
                var firstAvailableId = 1

                for (num in 1..maxId + 1) {
                    if (num !in idsList) {
                        firstAvailableId = num
                    }
                }

                val dateTime = LocalDateTime.of(LocalDate.of(yearInt, monthInt, dayInt), LocalTime.of(1, 1))

                val newRecurringTransfer = RecurringTransfer(
                    firstAvailableId,
                    nameEditText.text.toString(),
                    monthDayInt,
                    value,
                    noteEditText.text.toString(),
                    accountSendingNumber,
                    accountReceivingNumber,
                    dateTime.dayOfMonth,
                    dateTime.monthValue,
                    dateTime.year,
                    null,
                    null,
                    null
                )

                recurringTransfersDatabase.insert(newRecurringTransfer)
            }
            view.findNavController().navigate(AddRecurringTransferFragmentDirections.actionAddRecurringTransferFragmentToRecurringActsFragment())
        }
    }
}




