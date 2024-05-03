package com.pocket_sight.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentEditTransferBinding
import com.pocket_sight.types.accounts.Account
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
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

class EditTransferFragment: Fragment(), RemoveTransferDialogFragment.RemoveTransferDialogListener {
    private var _binding: FragmentEditTransferBinding? = null
    val binding get() = _binding!!

    lateinit var accountsDatabase: AccountsDao
    private lateinit var transfersDatabase: TransfersDao

    lateinit var transfer: Transfer

    private var originalTimeMillis = 0L
    private var originalValue = 0.0
    private var originalAccountSendingNumber: Int? = null
    private var originalAccountReceivingNumber: Int? = null


    private lateinit var accountSendingSpinner: Spinner
    private lateinit var accountReceivingSpinner: Spinner
    lateinit var valueEditText: EditText
    private lateinit var noteEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText

    lateinit var args: EditTransferFragmentArgs

    private lateinit var accountsStringsArray: Array<String>


    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentEditTransferBinding.inflate(inflater, container, false)

        args = EditTransferFragmentArgs.fromBundle(requireArguments())

        originalTimeMillis = args.originalTimeMillis
        originalValue = args.valueString.toDouble()

        if (args.accountReceivingNumber != -1) {
            originalAccountReceivingNumber = args.accountReceivingNumber
        }
        if (args.accountSendingNumber != -1) {
            originalAccountSendingNumber = args.accountSendingNumber
        }


        val menuHost: MenuHost = requireActivity()
        val menuProvider = EditTransferMenuProvider(this.requireContext(), this)
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)


        accountsDatabase = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao
        transfersDatabase = TransfersDatabase.getInstance(requireNotNull(this.activity).application).transfersDao

        accountSendingSpinner = binding.editTransferAccountSendingSpinner
        accountReceivingSpinner = binding.editTransferAccountReceivingSpinner
        valueEditText = binding.editTransferValueEditText
        noteEditText = binding.editTransferNoteEditText
        dateEditText = binding.editTransferDateEditText
        timeEditText = binding.editTransferTimeEditText


        buildFragmentInfo(this)

        val confirmChangesButton: Button = binding.confirmEditTransferButton
        confirmChangesButton.setOnClickListener {view: View ->
            confirmChanges(view)
        }


        return binding.root
    }

    private fun buildFragmentInfo(fragment: EditTransferFragment) {
        uiScope.launch {
            transfer = withContext(Dispatchers.IO) {
                transfersDatabase.get(originalTimeMillis)
            }


            val accountsList: MutableList<Account> = withContext(Dispatchers.IO) {
                accountsDatabase.getAllAccounts()
            }
            val accountsStringsList = accountsList.map {
                "${it.number}. ${it.name}"
            }.toMutableList()
            accountsStringsList.add("Another")
            fragment.accountsStringsArray = accountsStringsList.toTypedArray()

            val arrayAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
                fragment.requireContext(),
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
                if (accountString.split(".")[0].toInt() == originalAccountSendingNumber) {
                    accountSendingSpinnerSelectionPosition = index
                }
                if (accountString.split(".")[0].toInt() == originalAccountReceivingNumber) {
                    accountReceivingSpinnerSelectionPosition = index
                }
            }
            accountSendingSpinner.setSelection(accountSendingSpinnerSelectionPosition)
            accountReceivingSpinner.setSelection(accountReceivingSpinnerSelectionPosition)

            valueEditText.setText(originalValue.toString())
            noteEditText.setText(transfer.note)


            val dateTime: LocalDateTime = convertTimeMillisToLocalDateTime(originalTimeMillis)
            var dayString = dateTime.dayOfMonth.toString()
            var monthString = dateTime.monthValue.toString()
            if (dayString.length == 1) {
                dayString = "0$dayString"
            }
            if (monthString.length == 1) {
                monthString = "0$monthString"
            }
            dateEditText.setText("${dayString}/${monthString}/${dateTime.year}")

            dateEditText.setOnClickListener {
                EditTransferDatePicker(dateTime.dayOfMonth, dateTime.monthValue - 1, dateTime.year, this@EditTransferFragment).show(this@EditTransferFragment.parentFragmentManager, "Pick Date")
            }

            var hourString = dateTime.hour.toString()
            var minuteString = dateTime.minute.toString()
            if (hourString.length == 1) {
                hourString = "0$hourString"
            }
            if (minuteString.length == 1) {
                minuteString = "0$minuteString"
            }
            timeEditText.setText("${hourString}:${minuteString}")
            timeEditText.setOnClickListener {
                EditTransferTimePicker(dateTime.minute, dateTime.hour, this@EditTransferFragment).show(this@EditTransferFragment.parentFragmentManager, "Pick Time")
            }
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
        dateEditText.setText("$dayString/$monthString/$year")
    }

    fun setTime(minute: Int, hour: Int) {
        var hourString = hour.toString()
        var minuteString = minute.toString()
        if (hourString.length == 1) {
            hourString = "0$hourString"
        }
        if (minuteString.length == 1) {
            minuteString = "0$minuteString"
        }
        timeEditText.setText("${hourString}:${minuteString}")
    }


    private fun confirmChanges(view: View) {
        uiScope.launch {
            val newAccountSendingString = accountSendingSpinner.selectedItem.toString()
            val newAccountReceivingString = accountReceivingSpinner.selectedItem.toString()

            if (newAccountSendingString == "Another" && newAccountReceivingString == "Another") {
                Toast.makeText(this@EditTransferFragment.requireContext(), "No Accounts Selected", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (newAccountSendingString == newAccountReceivingString) {
                Toast.makeText(this@EditTransferFragment.requireContext(), "The Two Accounts Are Equal", Toast.LENGTH_SHORT).show()
                return@launch
            }

            var newAccountSendingNumber: Int? = null
            var newAccountReceivingNumber: Int? = null

            if (newAccountSendingString != "Another") {
                newAccountSendingNumber = newAccountSendingString.split(".")[0].toInt()
            }
            if (newAccountReceivingString != "Another") {
                newAccountReceivingNumber = newAccountReceivingString.split(".")[0].toInt()
            }

            val valueString = valueEditText.text.toString()
            var newValue: Double
            try {
                newValue = valueString.toDouble()
                newValue = newValue.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            } catch (e: Exception) {
                valueEditText.error = "Invalid Value"
                return@launch
            }


            val dateStringList: List<String> = dateEditText.text.toString().split("/")

            var dayInt = 0
            var monthInt = 0
            var yearInt = 0

            try {
                dayInt = dateStringList[0].toInt()
                monthInt = dateStringList[1].toInt()
                yearInt = dateStringList[2].toInt()
            } catch (e: Exception) {
                dateEditText.error = "Invalid Date"
                return@launch
            }
            val timeString = timeEditText.text.toString()
            val timeStringList = timeString.split(":")

            var hourInt = 0
            var minuteInt = 0

            try {
                hourInt = timeStringList[0].toInt()
                minuteInt = timeStringList[1].toInt()
            } catch (e: Exception) {
                timeEditText.error = "Invalid Time"
                return@launch
            }

            val dateTime = LocalDateTime.of(LocalDate.of(yearInt, monthInt, dayInt), LocalTime.of(hourInt, minuteInt))
            val newTimeMillis: Long = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

            val newTimeMillisInDatabase = withContext(Dispatchers.IO) {
                transfersDatabase.idInDatabase(newTimeMillis)
            }

            if (newTimeMillis != originalTimeMillis && newTimeMillisInDatabase) {
                Toast.makeText(this@EditTransferFragment.requireContext(), "Date and Time Taken by Another Transfer.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            withContext(Dispatchers.IO) {
                transfersDatabase.delete(transfer)
                val newTransfer = Transfer(
                    newTimeMillis,
                    dateTime.minute,
                    dateTime.hour,
                    dateTime.dayOfMonth,
                    dateTime.monthValue,
                    dateTime.year,
                    newValue,
                    noteEditText.text.toString(),
                    newAccountSendingNumber,
                    newAccountReceivingNumber
                )
                transfersDatabase.insert(newTransfer)
            }

            // update account balances
            var oldAccountSending: Account? = null
            var oldAccountReceiving: Account? = null
            var newAccountSending: Account? = null
            var newAccountReceiving: Account? = null

            // get old account objects
            if (originalAccountSendingNumber != null) {
                oldAccountSending = withContext(Dispatchers.IO) {
                    accountsDatabase.get(originalAccountSendingNumber!!)
                }
            }
            if (originalAccountReceivingNumber != null) {
                oldAccountReceiving = withContext(Dispatchers.IO) {
                    accountsDatabase.get(originalAccountReceivingNumber!!)
                }
            }

            // update old account balances
            if (oldAccountSending != null) {
                withContext(Dispatchers.IO) {
                    var newBalance = oldAccountSending.balance + originalValue
                    newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                    accountsDatabase.updateBalance(oldAccountSending.number, newBalance)
                }
            }

            if (oldAccountReceiving != null) {
                withContext(Dispatchers.IO) {
                    var newBalance = oldAccountReceiving.balance - originalValue
                    newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                    accountsDatabase.updateBalance(oldAccountReceiving.number, newBalance)
                }
            }

            // get new account objects
            if (newAccountSendingNumber != null) {
                newAccountSending = withContext(Dispatchers.IO) {
                    accountsDatabase.get(newAccountSendingNumber)
                }
            }
            if (newAccountReceivingNumber != null) {
                newAccountReceiving = withContext(Dispatchers.IO) {
                    accountsDatabase.get(newAccountReceivingNumber)
                }
            }

            // update new account balances
            if (newAccountSending != null) {
                withContext(Dispatchers.IO) {
                    var newBalance = newAccountSending.balance - newValue
                    newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                    accountsDatabase.updateBalance(newAccountSending.number, newBalance)
                }
            }

            if (newAccountReceiving != null) {
                withContext(Dispatchers.IO) {
                    var newBalance = newAccountReceiving.balance + newValue
                    newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                    accountsDatabase.updateBalance(newAccountReceiving.number, newBalance)
                }
            }

            view.findNavController().navigate(EditTransferFragmentDirections.actionEditTransferFragmentToHomeFragment())
        }
    }


    fun showRemoveTransferDialog() {
        RemoveTransferDialogFragment(this).show(this.parentFragmentManager, "RemoveTransferDialog")
    }

    override fun onRemoveTransferDialogPositiveClick(dialog: DialogFragment) {
        uiScope.launch {
            // remove transfer from database
            withContext(Dispatchers.IO) {
                transfersDatabase.delete(transfer)
            }

            // update associated accounts balances
            if (originalAccountSendingNumber != null) {
                withContext(Dispatchers.IO) {
                    val oldBalance = accountsDatabase.get(originalAccountSendingNumber!!).balance
                    val newBalance = (oldBalance + originalValue).toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                    accountsDatabase.updateBalance(originalAccountSendingNumber!!, newBalance)
                }
            }
            if (originalAccountReceivingNumber != null) {
                withContext(Dispatchers.IO) {
                    val oldBalance = accountsDatabase.get(originalAccountReceivingNumber!!).balance
                    val newBalance = (oldBalance - originalValue).toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                    accountsDatabase.updateBalance(originalAccountReceivingNumber!!, newBalance)
                }
            }
        }

        NavHostFragment.findNavController(this).navigate(
            EditTransferFragmentDirections.actionEditTransferFragmentToHomeFragment()
        )
    }
}

