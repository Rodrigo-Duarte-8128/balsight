package com.pocket_sight.fragments.home

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
import java.time.LocalDateTime

class AddTransferFragment: Fragment() {
    private var _binding: FragmentAddTransferBinding? = null
    val binding get() = _binding!!

    lateinit var accountsDatabase: AccountsDao

    var timeMillis = 0L

    lateinit var accountSendingSpinner: Spinner
    lateinit var accountReceivingSpinner: Spinner
    lateinit var valueEditText: EditText
    lateinit var noteEditText: EditText
    lateinit var dateEditText: EditText
    lateinit var timeEditText: EditText

    lateinit var args: AddTransferFragmentArgs

    lateinit var accountsStringsArray: Array<String>


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

        accountSendingSpinner = binding.addTransferAccountSendingSpinner
        //accountSendingSpinner.onItemSelectedListener = this
        accountReceivingSpinner = binding.addTransferAccountReceivingSpinner
        //accountReceivingSpinner.onItemSelectedListener = this
        valueEditText = binding.addTransferValueEditText
        noteEditText = binding.addTransferNoteEditText
        dateEditText = binding.addTransferDateEditText
        timeEditText = binding.addTransferTimeEditText


        buildFragmentInfo(this)

        val addTransferButton: Button = binding.addTransferButton
        addTransferButton.setOnClickListener {
            addTransfer()
        }



        return binding.root
    }

    private fun buildFragmentInfo(fragment: AddTransferFragment) {
        uiScope.launch {
            val accountsList: MutableList<Account> = withContext(Dispatchers.IO) {
                accountsDatabase.getAllAccounts()
            }
            var accountsStringsList = accountsList.map {
                "${it.number}. ${it.name}"
            }.toMutableList()
            accountsStringsList.add("Another")
            fragment.accountsStringsArray = accountsStringsList.toTypedArray()

            //Log.i("TAG", accountsStringsArray.joinToString { ", " })
            val arrayAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
                fragment.requireContext(),
                R.layout.category_kind_spinner,
                accountsStringsArray
            )
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            accountSendingSpinner.adapter = arrayAdapter
            accountReceivingSpinner.adapter = arrayAdapter
        }

//        ArrayAdapter.createFromResource(
//            this.requireContext(),
//            R.array.kinds_array,
//            R.layout.category_kind_spinner
//        ).also { adapter ->
//            // Specify the layout to use when the list of choices appears.
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
//            // Apply the adapter to the spinner.
//            accountSendingSpinner.adapter = adapter
//            accountReceivingSpinner.adapter = adapter
//        }

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

    fun addTransfer() {

    }


}

