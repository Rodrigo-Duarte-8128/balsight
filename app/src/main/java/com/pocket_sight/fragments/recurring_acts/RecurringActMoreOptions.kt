package com.pocket_sight.fragments.recurring_acts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.pocket_sight.databinding.FragmentRecurringActMoreOptionsBinding
import com.pocket_sight.types.transactions.convertTimeMillisToLocalDateTime
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class RecurringActMoreOptions: Fragment() {
    private var _binding: FragmentRecurringActMoreOptionsBinding? = null
    val binding get() = _binding!!

    var timeMillis: Long = 0L
    var accountNumber: Int = 0

    lateinit var args: RecurringActMoreOptionsArgs

    lateinit var dateEditText: EditText
    lateinit var noteEditText: EditText

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRecurringActMoreOptionsBinding.inflate(inflater, container, false)

        args = RecurringActMoreOptionsArgs.fromBundle(requireArguments())
        timeMillis = args.timeMillis
        accountNumber = args.accountNumber


        dateEditText = binding.recurringActMoreOptionsDateEditText
        noteEditText = binding.recurringActMoreOptionsNoteEditText

        buildFragmentInfo()

        val confirmChangesButton: Button = binding.recurringActMoreOptionsConfirmButton
        confirmChangesButton.setOnClickListener {view: View ->
            confirmChanges(view)
        }


        return binding.root
    }

    private fun buildFragmentInfo() {
        val dateTime: LocalDateTime = convertTimeMillisToLocalDateTime(timeMillis)
        var dayString = dateTime.dayOfMonth.toString()
        if (dayString.length == 1) {
            dayString = "0$dayString"
        }
        var monthString = dateTime.monthValue.toString()
        if (monthString.length == 1) {
            monthString = "0$monthString"
        }

        dateEditText.setText("$dayString/$monthString/${dateTime.year}")
        dateEditText.setOnClickListener {
            RecurringActMoreOptionsDatePicker(dateTime.dayOfMonth, dateTime.monthValue - 1, dateTime.year, this).show(this.parentFragmentManager, "Pick Date")
        }
        noteEditText.setText(args.note)
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

    private fun confirmChanges(view: View) {
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

        val dateTime = LocalDateTime.of(LocalDate.of(yearInt, monthInt, dayInt), LocalTime.of(1, 1))
        val newTimeMillis: Long = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()


        if (args.from == "add_recurring_expense_fragment") {
            view.findNavController().navigate(RecurringActMoreOptionsDirections.actionRecurringActMoreOptionsFragmentToAddRecurringExpenseFragment(
                newTimeMillis,
                args.valueString,
                note,
                args.accountNumber,
                args.selectedCategoryNumber,
                args.selectedSubcategoryNumber,
                args.monthDay,
                args.name
            ))
        }
    }
}

