package com.pocket_sight.fragments.home

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.Spinner
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentChooseMonthBinding
import com.pocket_sight.types.displayed.DisplayedMonthYear
import com.pocket_sight.types.displayed.DisplayedMonthYearDao
import com.pocket_sight.types.displayed.DisplayedMonthYearDatabase
import com.pocket_sight.types.transactions.convertTimeMillisToLocalDateTime
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseMonthFragment: Fragment() {
    private var _binding: FragmentChooseMonthBinding? = null
    private val binding get() = _binding!!

    lateinit var args: ChooseMonthFragmentArgs


    lateinit var displayedMonthYearDatabase: DisplayedMonthYearDao

    lateinit var monthSpinner: Spinner
    lateinit var yearSpinner: Spinner
    lateinit var yearStringsArray: Array<String>

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseMonthBinding.inflate(inflater, container, false)

        args = ChooseMonthFragmentArgs.fromBundle(requireArguments())

        displayedMonthYearDatabase = DisplayedMonthYearDatabase.getInstance(this.requireContext()).monthYearDao

        monthSpinner = binding.chooseMonthSpinner
        yearSpinner = binding.chooseYearSpinner

        buildFragmentInfo()

        val confirmChoiceButton: Button = binding.confirmMonthChoiceButton
        confirmChoiceButton.setOnClickListener {view: View ->
            confirmChoice(view)
        }



        return binding.root
    }

    private fun buildFragmentInfo() {
        uiScope.launch {

            // set array for month spinner
            ArrayAdapter.createFromResource(
                this@ChooseMonthFragment.requireContext(),
                R.array.months_array,
                R.layout.category_kind_spinner
            ).also { adapter ->
                // Specify the layout to use when the list of choices appears.
                adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
                // Apply the adapter to the spinner.
                monthSpinner.adapter = adapter
            }

            // set array for year spinner
            // start by creating an array of years from 2000 to the present year

            val millis = System.currentTimeMillis()
            val dateTime = convertTimeMillisToLocalDateTime(millis)
            val currentYear = dateTime.year

            val yearsMutableList: MutableList<Int> = mutableListOf()
            for (year in currentYear downTo 2000) {
                yearsMutableList.add(year)
            }
            val yearsStringsList = yearsMutableList.map { it.toString() }
            val yearsArray = yearsStringsList.toTypedArray()

            val arrayAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
                this@ChooseMonthFragment.requireContext(),
                R.layout.category_kind_spinner,
                yearsArray
            )
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            yearSpinner.adapter = arrayAdapter

            // set default selection for spinner
            var defaultYearSelectionPosition = 0

            val displayedMonthYearList = withContext(Dispatchers.IO) {
                displayedMonthYearDatabase.getAllDisplayedMonthYear()
            }

            if (displayedMonthYearList.isNotEmpty()) {
                val displayedMonthInt = displayedMonthYearList[0].month
                val displayedYear = displayedMonthYearList[0].year

                monthSpinner.setSelection(displayedMonthInt - 1)

                yearsArray.forEachIndexed {index, yearString ->
                    if (yearString.toInt() == displayedYear) {
                        defaultYearSelectionPosition = index
                    }
                }
                yearSpinner.setSelection(defaultYearSelectionPosition)
            } else {
                // in this case just set the default to the current month and year
                monthSpinner.setSelection(dateTime.monthValue - 1)

                yearsArray.forEachIndexed {index, yearString ->
                    if (yearString.toInt() == dateTime.year) {
                        defaultYearSelectionPosition = index
                    }
                }
                yearSpinner.setSelection(defaultYearSelectionPosition)
            }
        }
    }

    private fun confirmChoice(view: View) {
        uiScope.launch {
            val monthStringChosen = monthSpinner.selectedItem.toString()
            val yearStringChosen = yearSpinner.selectedItem.toString()

            val monthInt = when(monthStringChosen) {
                "January" -> 1
                "February" -> 2
                "March" -> 3
                "April" -> 4
                "May" -> 5
                "June" -> 6
                "July" -> 7
                "August" -> 8
                "September" -> 9
                "October" -> 10
                "November" -> 11
                else -> 12
            }

            withContext(Dispatchers.IO) {
                displayedMonthYearDatabase.clear()
                val displayedMonthYear = DisplayedMonthYear(
                    1,
                    monthInt,
                    yearStringChosen.toInt()
                )
                displayedMonthYearDatabase.insert(displayedMonthYear)
            }

            if (args.from == "home_fragment") {
                view.findNavController().navigate(
                    ChooseMonthFragmentDirections.actionChooseMonthFragmentToHomeFragment()
                )
            }

            if (args.from == "stats_fragment") {
                view.findNavController().navigate(
                    ChooseMonthFragmentDirections.actionChooseMonthFragmentToStatsFragment()
                )
            }

        }
    }

}
