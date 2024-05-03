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
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentAddTransferBinding
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

class AddTransferFragment: Fragment() {
    private var _binding: FragmentAddTransferBinding? = null
    val binding get() = _binding!!

    lateinit var accountsDatabase: AccountsDao
    private lateinit var transfersDatabase: TransfersDao

    var timeMillis = 0L

    private lateinit var accountSendingSpinner: Spinner
    private lateinit var accountReceivingSpinner: Spinner
    lateinit var valueEditText: EditText
    private lateinit var noteEditText: EditText
    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText

    lateinit var args: AddTransferFragmentArgs

    private lateinit var accountsStringsArray: Array<String>


    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddTransferBinding.inflate(inflater, container, false)

        args = AddTransferFragmentArgs.fromBundle(requireArguments())

        timeMillis = args.timeMillis


        accountsDatabase = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao
        transfersDatabase = TransfersDatabase.getInstance(requireNotNull(this.activity).application).transfersDao

        accountSendingSpinner = binding.addTransferAccountSendingSpinner
        accountReceivingSpinner = binding.addTransferAccountReceivingSpinner
        valueEditText = binding.addTransferValueEditText
        noteEditText = binding.addTransferNoteEditText
        dateEditText = binding.addTransferDateEditText
        timeEditText = binding.addTransferTimeEditText


        buildFragmentInfo(this)

        val addTransferButton: Button = binding.addTransferButton
        addTransferButton.setOnClickListener {view: View ->
            addTransfer(view)
        }



        return binding.root
    }

    private fun buildFragmentInfo(fragment: AddTransferFragment) {
        uiScope.launch {
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
        dateEditText.setText("${dayString}/${monthString}/${dateTime.year}")

        dateEditText.setOnClickListener {
            AddTransferDatePicker(dateTime.dayOfMonth, dateTime.monthValue - 1, dateTime.year, this).show(this.parentFragmentManager, "Pick Date")
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
            AddTransferTimePicker(dateTime.minute, dateTime.hour, this).show(this.parentFragmentManager, "Pick Time")
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

    private fun addTransfer(view: View) {
        uiScope.launch {
            val accountSendingString = accountSendingSpinner.selectedItem.toString()
            val accountReceivingString = accountReceivingSpinner.selectedItem.toString()

            if (accountSendingString == "Another" && accountReceivingString == "Another") {
                Toast.makeText(this@AddTransferFragment.requireContext(), "No Accounts Selected", Toast.LENGTH_SHORT).show()
                return@launch
            }

            if (accountSendingString == accountReceivingString) {
                Toast.makeText(this@AddTransferFragment.requireContext(), "The Two Accounts Are Equal", Toast.LENGTH_SHORT).show()
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

            if (newTimeMillisInDatabase) {
                Toast.makeText(this@AddTransferFragment.requireContext(), "Date and Time Taken by Another Transfer.", Toast.LENGTH_SHORT).show()
                return@launch
            }

            withContext(Dispatchers.IO) {
                val newTransfer = Transfer(
                    newTimeMillis,
                    dateTime.minute,
                    dateTime.hour,
                    dateTime.dayOfMonth,
                    dateTime.monthValue,
                    dateTime.year,
                    value,
                    noteEditText.text.toString(),
                    accountSendingNumber,
                    accountReceivingNumber
                )
                transfersDatabase.insert(newTransfer)
            }

            // update account balances
            var accountSending: Account? = null
            var accountReceiving: Account? = null

            if (accountSendingNumber != null) {
                accountSending = withContext(Dispatchers.IO) {
                    accountsDatabase.get(accountSendingNumber)
                }
            }
            if (accountReceivingNumber != null) {
                accountReceiving = withContext(Dispatchers.IO) {
                    accountsDatabase.get(accountReceivingNumber)
                }
            }

            if (accountSending != null) {
                withContext(Dispatchers.IO) {
                    var newBalance = accountSending.balance - value
                    newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                    accountsDatabase.updateBalance(accountSending.number, newBalance)

                }
            }

            if (accountReceiving != null) {
                withContext(Dispatchers.IO) {
                    var newBalance = accountReceiving.balance + value
                    newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                    accountsDatabase.updateBalance(accountReceiving.number, newBalance)
                }
            }


            view.findNavController().navigate(AddTransferFragmentDirections.actionAddTransferFragmentToHomeFragment())
        }


    }


}

