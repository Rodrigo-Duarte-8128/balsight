package com.pocket_sight.fragments.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.databinding.FragmentAddExpenseBinding
import com.pocket_sight.databinding.FragmentMoreOptionsBinding
import com.pocket_sight.fragments.accounts.AccountsAdapter
import com.pocket_sight.fragments.categories.EditCategoryFragmentArgs
import com.pocket_sight.types.categories.CategoriesDao
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.Category
import com.pocket_sight.types.categories.SubcategoriesDao
import com.pocket_sight.types.categories.SubcategoriesDatabase
import com.pocket_sight.types.categories.Subcategory
import com.pocket_sight.types.transactions.TransactionsDao
import com.pocket_sight.types.transactions.TransactionsDatabase
import com.pocket_sight.types.transactions.convertTimeMillisToLocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
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

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentMoreOptionsBinding.inflate(inflater, container, false)

        args = MoreOptionsFragmentArgs.fromBundle(requireArguments())
        timeMillis = args.timeMillis
        accountNumber = args.accountNumber


        val dateEditText = binding.moreOptionsDateEditText
        val timeEditText = binding.moreOptionsTimeEditText
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
        timeEditText.setText("${dateTime.hour}:${dateTime.minute}")
        noteEditText.setText(args.note)
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



        view.findNavController().navigate(MoreOptionsFragmentDirections.actionMoreOptionsFragmentToAddExpenseFragment(
            accountNumber,
            newTimeMillis,
            note,
            args.valueString,
            args.selectedCategoryNumber,
            args.selectedSubcategoryNumber
        ))
    }


}

