package com.pocket_sight.fragments.recurring_acts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentEditRecurringTransferBinding
import com.pocket_sight.types.accounts.Account
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.recurring.RecurringTransfer
import com.pocket_sight.types.recurring.RecurringTransferDao
import com.pocket_sight.types.recurring.RecurringTransferDatabase
import com.pocket_sight.types.transactions.convertTimeMillisToLocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime

class EditRecurringTransferFragment: Fragment() {
    private var _binding: FragmentEditRecurringTransferBinding? = null
    val binding get() = _binding!!

    lateinit var accountsDatabase: AccountsDao
    lateinit var recurringTransfersDatabase: RecurringTransferDao


    lateinit var accountSendingSpinner: Spinner
    lateinit var accountReceivingSpinner: Spinner
    lateinit var nameEditText: EditText
    lateinit var valueEditText: EditText
    lateinit var noteEditText: EditText
    lateinit var startDateEditText: EditText
    lateinit var monthDayEditText: EditText

    lateinit var args: EditRecurringTransferFragmentArgs

    lateinit var accountsStringsArray: Array<String>

    lateinit var recurringTransfer: RecurringTransfer


    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditRecurringTransferBinding.inflate(inflater, container, false)

        args = EditRecurringTransferFragmentArgs.fromBundle(requireArguments())



        accountsDatabase = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao
        recurringTransfersDatabase = RecurringTransferDatabase.getInstance(requireNotNull(this.activity).application).recurringTransferDao

        accountSendingSpinner = binding.editRecurringTransferAccountSendingSpinner
        accountReceivingSpinner = binding.editRecurringTransferAccountReceivingSpinner
        nameEditText = binding.editRecurringTransferNameEditText
        valueEditText = binding.editRecurringTransferValueEditText
        noteEditText = binding.editRecurringTransferNoteEditText
        startDateEditText = binding.editRecurringTransferStartDateEditText
        monthDayEditText = binding.editRecurringTransferMonthDayEditText


        buildFragmentInfo()

        val confirmChangesButton: Button = binding.confirmRecurringTransferChangesButton
        confirmChangesButton.setOnClickListener {view: View ->
            confirmChanges(view)
        }

        return binding.root
    }

    private fun buildFragmentInfo() {
        uiScope.launch {
            recurringTransfer = withContext(Dispatchers.IO) {
                recurringTransfersDatabase.get(args.recurringTransferId)
            }

            nameEditText.setText(recurringTransfer.name)
            valueEditText.setText(recurringTransfer.value.toString())
            noteEditText.setText(recurringTransfer.note)
            monthDayEditText.setText(recurringTransfer.monthDay.toString())


            val accountsList: MutableList<Account> = withContext(Dispatchers.IO) {
                accountsDatabase.getAllAccounts()
            }
            val accountsStringsList = accountsList.map {
                "${it.number}. ${it.name}"
            }.toMutableList()
            accountsStringsList.add("Another")
            this@EditRecurringTransferFragment.accountsStringsArray = accountsStringsList.toTypedArray()

            val arrayAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
                this@EditRecurringTransferFragment.requireContext(),
                R.layout.category_kind_spinner,
                accountsStringsArray
            )
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            accountSendingSpinner.adapter = arrayAdapter
            accountReceivingSpinner.adapter = arrayAdapter

            var accountSendingSpinnerSelectionPosition = 0
            var accountReceivingSpinnerSelectionPosition = 0

            accountsStringsList.forEachIndexed {index, accountString ->
                if (accountString == "Another") {
                    return@forEachIndexed
                }
                if (accountString.split(".")[0].toInt() == recurringTransfer.accountSendingNumber) {
                    accountSendingSpinnerSelectionPosition = index
                }
                if (accountString.split(".")[0].toInt() == recurringTransfer.accountReceivingNumber) {
                    accountReceivingSpinnerSelectionPosition = index
                }
            }
            accountSendingSpinner.setSelection(accountSendingSpinnerSelectionPosition)
            accountReceivingSpinner.setSelection(accountReceivingSpinnerSelectionPosition)
        }


        val dateTime: LocalDateTime = convertTimeMillisToLocalDateTime(args.transferStartTimeMillis)
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
            EditRecurringTransferDatePicker(dateTime.dayOfMonth, dateTime.monthValue - 1, dateTime.year, this).show(this.parentFragmentManager, "Pick Date")
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


    fun confirmChanges(view: View) {
        uiScope.launch {
            val accountSendingString = accountSendingSpinner.selectedItem.toString()
            val accountReceivingString = accountReceivingSpinner.selectedItem.toString()

            if (accountSendingString == "Another" && accountReceivingString == "Another") {
                Toast.makeText(this@EditRecurringTransferFragment.requireContext(), "No Accounts Selected", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (accountSendingString == accountReceivingString) {
                Toast.makeText(this@EditRecurringTransferFragment.requireContext(), "The Two Accounts Are Equal", Toast.LENGTH_SHORT).show()
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

                recurringTransfersDatabase.delete(recurringTransfer)

                val newRecurringTransfer = RecurringTransfer(
                    recurringTransfer.recurringTransferId,
                    nameEditText.text.toString(),
                    monthDayInt,
                    value,
                    noteEditText.text.toString(),
                    accountSendingNumber,
                    accountReceivingNumber,
                    dateTime.dayOfMonth,
                    dateTime.monthValue,
                    dateTime.year,
                    recurringTransfer.lastInstantiationDay,
                    recurringTransfer.lastInstantiationMonthInt,
                    recurringTransfer.lastInstantiationYear
                )

                recurringTransfersDatabase.insert(newRecurringTransfer)
            }
            view.findNavController().navigate(
                EditRecurringTransferFragmentDirections.actionEditRecurringTransferFragmentToRecurringActsFragment()
            )
        }
    }
}




