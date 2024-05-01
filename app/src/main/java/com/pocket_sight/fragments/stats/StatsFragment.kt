package com.pocket_sight.fragments.stats

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.pocket_sight.databinding.FragmentStatsBinding
import com.pocket_sight.dateAfter
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

    lateinit var displayedAccountDatabase: DisplayedAccountDao
    lateinit var displayedMonthYearDatabase: DisplayedMonthYearDao
    lateinit var accountsDatabase: AccountsDao
    lateinit var transactionsDatabase: TransactionsDao
    lateinit var transfersDatabase: TransfersDao
    lateinit var recurringTransfersDatabase: RecurringTransferDao
    lateinit var recurringTransactionsDatabase: RecurringTransactionsDao

    var displayedAccountNumber: Int? = null
    var displayedMonthYearArray: Array<Int>? = null


    lateinit var initialBudgetTextView: TextView
    lateinit var currentBudgetTextView: TextView
    lateinit var recurringInTextView: TextView
    lateinit var recurringOutTextView: TextView
    lateinit var totalInTextView: TextView
    lateinit var totalOutTextView: TextView
    lateinit var progressBar: ProgressBar
    lateinit var budgetRatioTextView: TextView

    lateinit var displayedAccountStatsButton: Button
    lateinit var displayedMonthYearStatsButton: Button

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
        displayedMonthYearStatsButton = binding.displayedMonthStatsButton


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


            recurringInTextView.text = "\u20ac $recurringIn"
            recurringOutTextView.text = "\u20ac $recurringOut"
            totalInTextView.text = "\u20ac $totalIn"
            totalOutTextView.text = "\u20ac $totalOut"

            var initialBudget = recurringIn - recurringOut
            initialBudget = initialBudget.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            initialBudgetTextView.text = "\u20ac $initialBudget"

            var currentBudget = totalIn - totalOut
            currentBudget = currentBudget.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            currentBudgetTextView.text = "\u20ac $currentBudget"

            budgetRatioTextView.text = if (initialBudget > 0.0) {
                "\u20ac $currentBudget / \u20ac $initialBudget"
            } else {
                "\u20ac $currentBudget / \u20ac $totalIn"
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
            return if (totalOut <= totalIn) {
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
