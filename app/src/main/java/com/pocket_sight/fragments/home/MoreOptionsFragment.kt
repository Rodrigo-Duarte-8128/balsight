package com.pocket_sight.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.pocket_sight.databinding.FragmentMoreOptionsBinding
import com.pocket_sight.types.transactions.convertTimeMillisToLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class MoreOptionsFragment: Fragment() {
    private var _binding: FragmentMoreOptionsBinding? = null
    val binding get() = _binding!!

    var timeMillis: Long = 0L
    var accountNumber: Int = 0

    lateinit var args: MoreOptionsFragmentArgs

    private lateinit var dateEditText: EditText
    private lateinit var timeEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMoreOptionsBinding.inflate(inflater, container, false)

        args = MoreOptionsFragmentArgs.fromBundle(requireArguments())
        timeMillis = args.timeMillis
        accountNumber = args.accountNumber


        dateEditText = binding.moreOptionsDateEditText
        timeEditText = binding.moreOptionsTimeEditText
        val noteEditText = binding.moreOptionsNoteEditText

        buildFragmentInfo(timeMillis, dateEditText, timeEditText, noteEditText)

        val confirmChangesButton: Button = binding.moreOptionsConfirmButton
        confirmChangesButton.setOnClickListener {view: View ->
            confirmChanges(view, dateEditText, timeEditText, noteEditText)
        }


        return binding.root
    }

    private fun buildFragmentInfo(
        timeMillis: Long,
        dateEditText: EditText,
        timeEditText: EditText,
        noteEditText: EditText
    ) {
        val dateTime: LocalDateTime = convertTimeMillisToLocalDateTime(timeMillis)
        dateEditText.setText("${dateTime.dayOfMonth}/${dateTime.monthValue}/${dateTime.year}")
        dateEditText.setOnClickListener {
            MoreOptionsDatePicker(dateTime.dayOfMonth, dateTime.monthValue - 1, dateTime.year, this).show(this.parentFragmentManager, "Pick Date")
        }
        val hour = dateTime.hour
        val minute = dateTime.minute
        var hourString = hour.toString()
        var minuteString = minute.toString()
        if (hourString.length == 1) {
            hourString = "0$hourString"
        }
        if (minuteString.length == 1) {
            minuteString = "0$minuteString"
        }
        timeEditText.setText("${hourString}:${minuteString}")
        timeEditText.setOnClickListener {
            MoreOptionsTimePicker(dateTime.minute, dateTime.hour, this).show(this.parentFragmentManager, "Pick Time")
        }
        noteEditText.setText(args.note)
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

    private fun confirmChanges(view: View, dateEditText: EditText, timeEditText: EditText, noteEditText: EditText) {
        val note = noteEditText.text.toString()
        val dateString = dateEditText.text.toString()
        val dateStringList: List<String> = dateString.split("/")

        var dayInt = 0
        var monthInt = 0
        var yearInt = 0

        try {
            dayInt = dateStringList[0].toInt()
            monthInt = dateStringList[1].toInt()
            yearInt = dateStringList[2].toInt()
        } catch (e: Exception) {
            dateEditText.error = "Invalid Date"
            return
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
            return
        }

        val dateTime = LocalDateTime.of(LocalDate.of(yearInt, monthInt, dayInt), LocalTime.of(hourInt, minuteInt))
        val newTimeMillis: Long = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()


        if (args.from == "edit_transaction_fragment") {
            view.findNavController().navigate(MoreOptionsFragmentDirections.actionMoreOptionsFragmentToEditTransactionFragment(
                args.originalTimeMillis,
                newTimeMillis,
                args.accountNumber,
                args.valueString,
                args.selectedCategoryNumber,
                args.selectedSubcategoryNumber,
                note
            ))
        }

        if (args.from == "add_expense_fragment") {
            view.findNavController().navigate(MoreOptionsFragmentDirections.actionMoreOptionsFragmentToAddExpenseFragment(
                accountNumber,
                newTimeMillis,
                note,
                args.valueString,
                args.selectedCategoryNumber,
                args.selectedSubcategoryNumber
            ))
        }

        if (args.from == "add_income_fragment") {
            view.findNavController().navigate(MoreOptionsFragmentDirections.actionMoreOptionsFragmentToAddIncomeFragment(
                newTimeMillis,
                accountNumber,
                note,
                args.valueString,
                args.selectedCategoryNumber,
                args.selectedSubcategoryNumber
            ))
        }
    }
}

