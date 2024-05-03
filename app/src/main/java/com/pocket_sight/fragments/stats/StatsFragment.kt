package com.pocket_sight.fragments.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.pocket_sight.databinding.FragmentStatsBinding
import com.pocket_sight.parseMonthYearArrayToText
import com.pocket_sight.recurringActOccursThisMonthYear
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.displayed.DisplayedAccount
import com.pocket_sight.types.displayed.DisplayedAccountDao
import com.pocket_sight.types.displayed.DisplayedAccountDatabase
import com.pocket_sight.types.displayed.DisplayedMonthYearDao
import com.pocket_sight.types.displayed.DisplayedMonthYearDatabase
import com.pocket_sight.types.recurring.RecurringTransaction
import com.pocket_sight.types.recurring.RecurringTransactionsDao
import com.pocket_sight.types.recurring.RecurringTransactionsDatabase
import com.pocket_sight.types.recurring.RecurringTransfer
import com.pocket_sight.types.recurring.RecurringTransferDao
import com.pocket_sight.types.recurring.RecurringTransferDatabase
import com.pocket_sight.types.transactions.Transaction
import com.pocket_sight.types.transactions.TransactionsDao
import com.pocket_sight.types.transactions.TransactionsDatabase
import com.pocket_sight.types.transfers.Transfer
import com.pocket_sight.types.transfers.TransfersDao
import com.pocket_sight.types.transfers.TransfersDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode
import java.time.LocalDateTime
import kotlin.math.roundToInt

class StatsFragment : Fragment() {

    private var _binding: FragmentStatsBinding? = null

    private val binding get() = _binding!!

    private lateinit var displayedAccountDatabase: DisplayedAccountDao
    private lateinit var displayedMonthYearDatabase: DisplayedMonthYearDao
    lateinit var accountsDatabase: AccountsDao
    private lateinit var transactionsDatabase: TransactionsDao
    private lateinit var transfersDatabase: TransfersDao
    private lateinit var recurringTransfersDatabase: RecurringTransferDao
    private lateinit var recurringTransactionsDatabase: RecurringTransactionsDao

    var displayedAccountNumber: Int? = null
    private var displayedMonthYearArray: Array<Int>? = null


    private lateinit var initialBudgetTextView: TextView
    private lateinit var currentBudgetTextView: TextView
    private lateinit var recurringInTextView: TextView
    private lateinit var recurringOutTextView: TextView
    private lateinit var totalInTextView: TextView
    private lateinit var totalOutTextView: TextView
    private lateinit var progressBar: ProgressBar
    private lateinit var budgetRatioTextView: TextView

    private lateinit var displayedAccountStatsButton: Button
    private lateinit var displayedMonthYearStatsButton: Button

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentStatsBinding.inflate(inflater, container, false)


        transactionsDatabase = TransactionsDatabase.getInstance(this.requireContext()).transactionsDao
        transfersDatabase = TransfersDatabase.getInstance(this.requireContext()).transfersDao
        displayedMonthYearDatabase = DisplayedMonthYearDatabase.getInstance(this.requireContext()).monthYearDao
        accountsDatabase = AccountsDatabase.getInstance(this.requireContext()).accountsDao
        displayedAccountDatabase = DisplayedAccountDatabase.getInstance(this.requireContext()).displayedAccountDao
        recurringTransfersDatabase = RecurringTransferDatabase.getInstance(this.requireContext()).recurringTransferDao
        recurringTransactionsDatabase = RecurringTransactionsDatabase.getInstance(this.requireContext()).recurringTransactionsDao

        initialBudgetTextView = binding.initialBudgetTextView
        currentBudgetTextView = binding.currentBudgetTextView
        recurringInTextView = binding.recurringInTextView
        recurringOutTextView = binding.recurringOutTextView
        totalInTextView = binding.totalInTextView
        totalOutTextView = binding.totalOutTextView
        progressBar = binding.statsProgressBar
        budgetRatioTextView = binding.budgetRatioTextView

        displayedAccountStatsButton = binding.displayedAccountStatsButton
        displayedAccountStatsButton.setOnClickListener {view: View ->
            view.findNavController().navigate(
                StatsFragmentDirections.actionStatsFragmentToChooseAccountFragment(
                    "stats_fragment"
                )
            )
        }

        displayedMonthYearStatsButton = binding.displayedMonthStatsButton
        displayedMonthYearStatsButton.setOnClickListener {view: View ->
            view.findNavController().navigate(
                StatsFragmentDirections.actionStatsFragmentToChooseMonthFragment(
                    "stats_fragment"
                )
            )
        }

        buildFragmentInfo()

        return binding.root
    }


    private fun buildFragmentInfo() {
        uiScope.launch {
            displayedMonthYearArray = arrayOf(
                LocalDateTime.now().monthValue,
                LocalDateTime.now().year
            )  //default to current month year

            val displayedMonthYearList = withContext(Dispatchers.IO) {
                displayedMonthYearDatabase.getAllDisplayedMonthYear()
            }

            if (displayedMonthYearList.isNotEmpty()) {
                displayedMonthYearArray = arrayOf(
                    displayedMonthYearList[0].month,
                    displayedMonthYearList[0].year
                )
            }

            val mainAccountNumber: Int? = withContext(Dispatchers.IO) {
                accountsDatabase.getMainAccountNumber()
            }

            val displayedAccountList: List<DisplayedAccount> = withContext(Dispatchers.IO) {
                displayedAccountDatabase.getAllDisplayedAccount()
            }

            if (displayedAccountList.isNotEmpty()) {
                displayedAccountNumber = displayedAccountList[0].displayedAccountNumber
            } else if (mainAccountNumber != null) {
                displayedAccountNumber = mainAccountNumber
            }

            val accountNumber = displayedAccountNumber
            if (accountNumber == null) {
                displayedAccountStatsButton.text = "None"
                return@launch
            }

            // set buttons with correct info
            val displayedAccountName = withContext(Dispatchers.IO) {
                accountsDatabase.getNameFromAccountNumber(accountNumber)
            }
            displayedAccountStatsButton.text = displayedAccountName
            displayedMonthYearStatsButton.text = parseMonthYearArrayToText(displayedMonthYearArray!!)

            // grab relevant acts
            val relevantTransactionsList = withContext(Dispatchers.IO) {
                transactionsDatabase.getTransactionsFromMonthYear(displayedMonthYearArray!![0], displayedMonthYearArray!![1], displayedAccountNumber!!)
            }
            val transfersList = withContext(Dispatchers.IO) {
                transfersDatabase.getTransfersFromMonthYear(displayedMonthYearArray!![0], displayedMonthYearArray!![1])
            }

            val relevantTransfersList = transfersList.filter {
                it.accountReceivingNumber == displayedAccountNumber || it.accountSendingNumber == displayedAccountNumber
            }

            val recurringTransactionsList = withContext(Dispatchers.IO) {
                recurringTransactionsDatabase.getAllRecurringTransactionsFromAccount(displayedAccountNumber!!)
            }

            // eliminate recurring transactions which haven't started yet
            val relevantRecurringTransactionsList = recurringTransactionsList.filter {
                recurringActOccursThisMonthYear(
                    displayedMonthYearArray!![0],
                    displayedMonthYearArray!![1],
                    it.day,
                    it.month,
                    it.year,
                    it.monthDay
                )
            }

            val recurringTransfersList = withContext(Dispatchers.IO) {
                recurringTransfersDatabase.getAllRecurringTransfers()
            }

            val recurringTransfersListWithAccount = recurringTransfersList.filter {
                it.accountReceivingNumber == displayedAccountNumber || it.accountSendingNumber == displayedAccountNumber
            }

            val relevantRecurringTransfersList = recurringTransfersListWithAccount.filter {
                recurringActOccursThisMonthYear(
                    displayedMonthYearArray!![0],
                    displayedMonthYearArray!![1],
                    it.day,
                    it.month,
                    it.year,
                    it.monthDay
                )
            }
            var (recurringIn, recurringOut, totalIn, totalOut) = computeValues(
                relevantTransactionsList,
                relevantTransfersList,
                relevantRecurringTransactionsList,
                relevantRecurringTransfersList
            )
            recurringIn = recurringIn.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            recurringOut = recurringOut.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            totalIn = totalIn.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            totalOut = totalOut.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()


            recurringInTextView.text = if (recurringIn == recurringIn.toInt().toDouble()) {
                "\u20ac ${recurringIn.toInt()}"
            } else {
                "\u20ac ${recurringIn}"
            }
            recurringOutTextView.text = if (recurringOut == recurringOut.toInt().toDouble()) {
                "\u20ac ${recurringOut.toInt()}"
            } else {
                "\u20ac $recurringOut"
            }
            totalInTextView.text = if (totalIn == totalIn.toInt().toDouble()) {
                "\u20ac ${totalIn.toInt()}"
            } else {
                "\u20ac $totalIn"
            }
            totalOutTextView.text = if (totalOut == totalOut.toInt().toDouble()) {
                "\u20ac ${totalOut.toInt()}"
            } else {
                "\u20ac $totalOut"
            }

            var initialBudget = recurringIn - recurringOut
            initialBudget = initialBudget.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            initialBudgetTextView.text = if (initialBudget == initialBudget.toInt().toDouble()) {
                "\u20ac ${initialBudget.toInt()}"
            } else {
                "\u20ac $initialBudget"
            }

            var currentBudget = totalIn - totalOut
            currentBudget = currentBudget.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            currentBudgetTextView.text = if (currentBudget == currentBudget.toInt().toDouble()){
                "\u20ac ${currentBudget.toInt()}"
            } else {
                "\u20ac $currentBudget"
            }

            val currentBudgetString = if (currentBudget == currentBudget.toInt().toDouble()) {
                "\u20ac ${currentBudget.toInt()}"
            } else {
                "\u20ac $currentBudget"
            }

            val initialBudgetString = if (initialBudget == initialBudget.toInt().toDouble()) {
                "\u20ac ${initialBudget.toInt()}"
            } else {
                "\u20ac $initialBudget"
            }

            val totalInString = if (totalIn == totalIn.toInt().toDouble()) {
                "\u20ac ${totalIn.toInt()}"
            } else {
                "\u20ac $totalIn"
            }

            budgetRatioTextView.text = if (initialBudget > 0.0) {
                "$currentBudgetString / $initialBudgetString"
            } else {
                "$currentBudgetString / $totalInString"
            }

            val budgetProgress = getProgressBarPercentage(recurringIn, recurringOut, totalIn, totalOut)
            progressBar.progress = budgetProgress
        }

    }

    private fun computeValues(
        transactionsList: List<Transaction>,
        transfersList: List<Transfer>,
        recurringTransactionsList: List<RecurringTransaction>,
        recurringTransfersList: List<RecurringTransfer>
    ): Array<Double> {
        val currentDateTime = LocalDateTime.now()
        val currentDay = currentDateTime.dayOfMonth
        var recurringIn = 0.0
        var recurringOut = 0.0
        var totalIn = 0.0 // careful not to double count recurring acts
        var totalOut = 0.0

        for (transaction in transactionsList) {
            if (transaction.value >= 0) {
                totalIn += transaction.value
            } else {
                totalOut -= transaction.value
            }
        }

        for (transfer in transfersList) {
            if (transfer.accountSendingNumber == displayedAccountNumber) {
                totalOut += transfer.value
            } else {
                totalIn += transfer.value
            }
        }

        for (recurringTransaction in recurringTransactionsList) {
            if (recurringTransaction.value >= 0) {
                recurringIn += recurringTransaction.value

                if (displayedMonthYearArray!![0] == currentDateTime.monthValue &&
                    displayedMonthYearArray!![1] == currentDateTime.year &&
                    recurringTransaction.monthDay > currentDay)
                {
                    totalIn += recurringTransaction.value
                }

            } else {
                recurringOut -= recurringTransaction.value

                if (displayedMonthYearArray!![0] == currentDateTime.monthValue &&
                    displayedMonthYearArray!![1] == currentDateTime.year &&
                    recurringTransaction.monthDay > currentDay)
                {
                    totalOut -= recurringTransaction.value
                }
            }
        }

        for (recurringTransfer in recurringTransfersList) {
            if (recurringTransfer.accountSendingNumber == displayedAccountNumber) {
                recurringOut += recurringTransfer.value

                if (displayedMonthYearArray!![0] == currentDateTime.monthValue &&
                    displayedMonthYearArray!![1] == currentDateTime.year &&
                    recurringTransfer.monthDay > currentDay)
                {
                    totalOut += recurringTransfer.value
                }
            } else {
                recurringIn += recurringTransfer.value

                if (displayedMonthYearArray!![0] == currentDateTime.monthValue &&
                    displayedMonthYearArray!![1] == currentDateTime.year &&
                    recurringTransfer.monthDay > currentDay) {
                    totalIn += recurringTransfer.value
                }
            }
        }

        return arrayOf(recurringIn, recurringOut, totalIn, totalOut)
    }


    private fun getProgressBarPercentage(
        recurringIn: Double,
        recurringOut: Double,
        totalIn: Double,
        totalOut: Double
    ): Int {
        if (recurringIn <= recurringOut) {
            return if (totalIn <= 0.0) {
                0
            } else {
                // here totalIn > 0
                if (totalOut >= totalIn) {
                    0
                } else {
                    (100 * (totalIn - totalOut)/totalIn).roundToInt()
                }
            }
        } else {
            return if (totalIn <= totalOut) {
                0
            } else if ((totalIn - totalOut) > (recurringIn - recurringOut)){
                100
            } else {
                (100 * ((totalIn - totalOut) / (recurringIn - recurringOut))).roundToInt()
            }
        }
    }




    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}
