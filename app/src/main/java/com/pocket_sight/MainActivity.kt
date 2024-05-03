package com.pocket_sight

import android.os.Bundle
import com.google.android.material.navigation.NavigationView
import androidx.navigation.findNavController
import androidx.navigation.ui.AppBarConfiguration
import androidx.navigation.ui.navigateUp
import androidx.navigation.ui.setupActionBarWithNavController
import androidx.navigation.ui.setupWithNavController
import androidx.drawerlayout.widget.DrawerLayout
import androidx.appcompat.app.AppCompatActivity
import com.pocket_sight.databinding.ActivityMainBinding
import com.pocket_sight.fragments.home.HomeFragment
import com.pocket_sight.types.Act
import com.pocket_sight.types.accounts.Account
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.recurring.RecurringTransaction
import com.pocket_sight.types.recurring.RecurringTransactionsDao
import com.pocket_sight.types.recurring.RecurringTransactionsDatabase
import com.pocket_sight.types.recurring.RecurringTransfer
import com.pocket_sight.types.recurring.RecurringTransferDao
import com.pocket_sight.types.recurring.RecurringTransferDatabase
import com.pocket_sight.types.transactions.Transaction
import com.pocket_sight.types.transactions.TransactionsDao
import com.pocket_sight.types.transactions.TransactionsDatabase
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

class MainActivity : AppCompatActivity() {

    private lateinit var appBarConfiguration: AppBarConfiguration
    private lateinit var binding: ActivityMainBinding

    private lateinit var transactionsDatabase: TransactionsDao
    private lateinit var transfersDatabase: TransfersDao
    private lateinit var recurringTransactionsDatabase: RecurringTransactionsDao
    private lateinit var recurringTransfersDatabase: RecurringTransferDao
    private lateinit var accountsDatabase: AccountsDao

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding = ActivityMainBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setSupportActionBar(binding.appBarMain.toolbar)

        val drawerLayout: DrawerLayout = binding.drawerLayout
        val navView: NavigationView = binding.navView
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        // Passing each menu ID as a set of Ids because each
        // menu should be considered as top level destinations.
        appBarConfiguration = AppBarConfiguration(
            setOf(
                R.id.home_fragment,
                R.id.stats_fragment,
                R.id.recurring_acts_fragment,
                R.id.accounts_fragment,
                R.id.categories_fragment
            ), drawerLayout
        )
        setupActionBarWithNavController(navController, appBarConfiguration)
        navView.setupWithNavController(navController)


        transactionsDatabase = TransactionsDatabase.getInstance(requireNotNull(this).application).transactionsDao
        transfersDatabase = TransfersDatabase.getInstance(requireNotNull(this).application).transfersDao
        recurringTransactionsDatabase = RecurringTransactionsDatabase.getInstance(requireNotNull(this).application).recurringTransactionsDao
        recurringTransfersDatabase = RecurringTransferDatabase.getInstance(requireNotNull(this).application).recurringTransferDao
        accountsDatabase = AccountsDatabase.getInstance(requireNotNull(this).application).accountsDao

        buildMissingRecurringActs()
    }


    override fun onSupportNavigateUp(): Boolean {
        val navController = findNavController(R.id.nav_host_fragment_content_main)
        return navController.navigateUp(appBarConfiguration) || super.onSupportNavigateUp()
    }

    override fun onBackPressed() {
        val homeFragment = supportFragmentManager.findFragmentById(R.id.home_fragment)
        if (homeFragment is HomeFragment) {
            if (homeFragment.fabIsExpanded) {
                homeFragment.shrinkFab()
            } else  {
                super.onBackPressed()
            }
        } else {
            super.onBackPressed()
        }
    }

    private fun buildMissingRecurringActs() {
        uiScope.launch {
            val currentTimeMillis = System.currentTimeMillis()
            val currentDateTime = convertTimeMillisToLocalDateTime(currentTimeMillis)
            val currentDate = LocalDate.of(currentDateTime.year, currentDateTime.monthValue, currentDateTime.dayOfMonth)

            val recurringTransactionsList = withContext(Dispatchers.IO) {
                recurringTransactionsDatabase.getAllRecurringTransactions()
            }

            buildMissingRecurringTransactions(currentDate, recurringTransactionsList)


            val recurringTransfersList = withContext(Dispatchers.IO) {
                recurringTransfersDatabase.getAllRecurringTransfers()
            }

            buildMissingRecurringTransfers(currentDate, recurringTransfersList)
        }
    }

    private suspend fun buildMissingRecurringTransactions(currentDate: LocalDate, recurringTransactionsList: List<RecurringTransaction>) {
        withContext(Dispatchers.IO) {
            for (recurringTransaction in recurringTransactionsList) {
                val startDate = LocalDate.of(
                    recurringTransaction.year,
                    recurringTransaction.month,
                    recurringTransaction.day
                )

                if (!dateAfter(currentDate, startDate)) {
                    continue
                }
                // from now on we know that currentDate is after startDate

                var instantiated = false
                if (recurringTransaction.lastInstantiationYear != null && recurringTransaction.lastInstantiationMonthInt != null && recurringTransaction.lastInstantiationDay != null) {
                    instantiated = true
                }

                if (!instantiated && currentDate.dayOfMonth >= recurringTransaction.monthDay) {
                    val transactionDate = LocalDate.of(currentDate.year, currentDate.monthValue, recurringTransaction.monthDay)
                    val newTransactionTimeMillis = getFirstAvailableTimeMillis(
                        "Transaction",
                        transactionDate.dayOfMonth,
                        transactionDate.monthValue,
                        transactionDate.year
                    )
                    val newTransactionDateTime = convertTimeMillisToLocalDateTime(newTransactionTimeMillis)
                    val newTransaction = Transaction(
                        newTransactionTimeMillis,
                        newTransactionDateTime.minute,
                        newTransactionDateTime.hour,
                        newTransactionDateTime.dayOfMonth,
                        newTransactionDateTime.monthValue,
                        newTransactionDateTime.year,
                        recurringTransaction.value,
                        recurringTransaction.accountNumber,
                        recurringTransaction.categoryNumber,
                        recurringTransaction.subcategoryNumber,
                        recurringTransaction.note,
                        recurringTransaction.oldSubcategoryName,
                        recurringTransaction.oldCategoryName
                    )
                    transactionsDatabase.insert(newTransaction)

                    // update account balance
                    val account = accountsDatabase.get(recurringTransaction.accountNumber)

                    var newBalance = account.balance + recurringTransaction.value
                    newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                    accountsDatabase.updateBalance(account.number, newBalance)

                    // update recurringTransaction last instantiation date
                    recurringTransactionsDatabase.updateInstantiationDate(
                        recurringTransaction.recurringTransactionId,
                        transactionDate.dayOfMonth,
                        transactionDate.monthValue,
                        transactionDate.year
                    )
                    continue
                }

                if (!instantiated && currentDate.dayOfMonth < recurringTransaction.monthDay) {
                    continue
                }

                // at this point we know that the recurring transaction has been instantiated in the past

                val lastInstantiationDate = LocalDate.of(
                    recurringTransaction.lastInstantiationYear!!,
                    recurringTransaction.lastInstantiationMonthInt!!,
                    recurringTransaction.lastInstantiationDay!!
                )

                var monthYear = arrayOf(lastInstantiationDate.monthValue, lastInstantiationDate.year)
                val currentMonthYear = arrayOf(currentDate.monthValue, currentDate.year)


                while (!monthYear.contentEquals(currentMonthYear)) {
                    // we start by incrementing month year
                    monthYear = if (monthYear[0] == 12) {
                        arrayOf(1, monthYear[1] + 1)
                    } else {
                        arrayOf(monthYear[0] + 1, monthYear[1])
                    }

                    if (monthYear.contentEquals(currentMonthYear) && currentDate.dayOfMonth >= recurringTransaction.monthDay) {
                        val transactionDate = LocalDate.of(
                            currentDate.year,
                            currentDate.monthValue,
                            recurringTransaction.monthDay
                        )
                        val newTransactionTimeMillis = getFirstAvailableTimeMillis(
                            "Transaction",
                            transactionDate.dayOfMonth,
                            transactionDate.monthValue,
                            transactionDate.year
                        )
                        val newTransactionDateTime = convertTimeMillisToLocalDateTime(newTransactionTimeMillis)
                        val newTransaction = Transaction(
                            newTransactionTimeMillis,
                            newTransactionDateTime.minute,
                            newTransactionDateTime.hour,
                            newTransactionDateTime.dayOfMonth,
                            newTransactionDateTime.monthValue,
                            newTransactionDateTime.year,
                            recurringTransaction.value,
                            recurringTransaction.accountNumber,
                            recurringTransaction.categoryNumber,
                            recurringTransaction.subcategoryNumber,
                            recurringTransaction.note,
                            recurringTransaction.oldSubcategoryName,
                            recurringTransaction.oldCategoryName
                        )
                        transactionsDatabase.insert(newTransaction)

                        // update account balance
                        val account = accountsDatabase.get(recurringTransaction.accountNumber)

                        var newBalance = account.balance + recurringTransaction.value
                        newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                        accountsDatabase.updateBalance(account.number, newBalance)
                    }
                    if (!monthYear.contentEquals(currentMonthYear)) {
                        // in this case we always build a transaction
                        val transactionDate = LocalDate.of(monthYear[1], monthYear[0], recurringTransaction.monthDay)

                        val newTransactionTimeMillis = getFirstAvailableTimeMillis(
                            "Transaction",
                            transactionDate.dayOfMonth,
                            transactionDate.monthValue,
                            transactionDate.year
                        )
                        val newTransactionDateTime = convertTimeMillisToLocalDateTime(newTransactionTimeMillis)
                        val newTransaction = Transaction(
                            newTransactionTimeMillis,
                            newTransactionDateTime.minute,
                            newTransactionDateTime.hour,
                            newTransactionDateTime.dayOfMonth,
                            newTransactionDateTime.monthValue,
                            newTransactionDateTime.year,
                            recurringTransaction.value,
                            recurringTransaction.accountNumber,
                            recurringTransaction.categoryNumber,
                            recurringTransaction.subcategoryNumber,
                            recurringTransaction.note,
                            recurringTransaction.oldSubcategoryName,
                            recurringTransaction.oldCategoryName
                        )
                        transactionsDatabase.insert(newTransaction)

                        // update account balance
                        val account = accountsDatabase.get(recurringTransaction.accountNumber)

                        var newBalance = account.balance + recurringTransaction.value
                        newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                        accountsDatabase.updateBalance(account.number, newBalance)
                    }
                }


                // while loop exited, need to update last instantiation date
                recurringTransactionsDatabase.updateInstantiationDate(
                    recurringTransaction.recurringTransactionId,
                    recurringTransaction.monthDay,
                    currentMonthYear[0],
                    currentMonthYear[1]
                )
            }
        }
    }


    private suspend fun buildMissingRecurringTransfers(currentDate: LocalDate, recurringTransfersList: List<RecurringTransfer>) {
        withContext(Dispatchers.IO) {
            for (recurringTransfer in recurringTransfersList) {
                val startDate = LocalDate.of(
                    recurringTransfer.year,
                    recurringTransfer.month,
                    recurringTransfer.day
                )

                if (!dateAfter(currentDate, startDate)) {
                    continue
                }
                // from now on we know that currentDate is after startDate

                var instantiated = false
                if (recurringTransfer.lastInstantiationYear != null && recurringTransfer.lastInstantiationMonthInt != null && recurringTransfer.lastInstantiationDay != null) {
                    instantiated = true
                }

                if (!instantiated && currentDate.dayOfMonth >= recurringTransfer.monthDay) {
                    val transferDate = LocalDate.of(currentDate.year, currentDate.monthValue, recurringTransfer.monthDay)
                    val newTransferTimeMillis = getFirstAvailableTimeMillis(
                        "Transfer",
                        transferDate.dayOfMonth,
                        transferDate.monthValue,
                        transferDate.year
                    )
                    val newTransferDateTime = convertTimeMillisToLocalDateTime(newTransferTimeMillis)
                    val newTransfer = Transfer(
                        newTransferTimeMillis,
                        newTransferDateTime.minute,
                        newTransferDateTime.hour,
                        newTransferDateTime.dayOfMonth,
                        newTransferDateTime.monthValue,
                        newTransferDateTime.year,
                        recurringTransfer.value,
                        recurringTransfer.note,
                        recurringTransfer.accountSendingNumber,
                        recurringTransfer.accountReceivingNumber
                    )
                    transfersDatabase.insert(newTransfer)

                    // update account balance

                    var accountSending: Account? = null
                    var accountReceiving: Account? = null

                    if (recurringTransfer.accountSendingNumber != null) {
                        accountSending = accountsDatabase.get(recurringTransfer.accountSendingNumber!!)
                    }
                    if (recurringTransfer.accountReceivingNumber != null) {
                        accountReceiving = accountsDatabase.get(recurringTransfer.accountReceivingNumber!!)
                    }

                    if (accountSending != null) {
                        var newBalance = accountSending.balance - recurringTransfer.value
                        newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                        accountsDatabase.updateBalance(accountSending.number, newBalance)
                    }
                    if (accountReceiving != null) {
                        var newBalance = accountReceiving.balance + recurringTransfer.value
                        newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                        accountsDatabase.updateBalance(accountReceiving.number, newBalance)
                    }


                    // update recurringTransfer last instantiation date
                    recurringTransfersDatabase.updateInstantiationDate(
                        recurringTransfer.recurringTransferId,
                        transferDate.dayOfMonth,
                        transferDate.monthValue,
                        transferDate.year
                    )
                    continue
                }

                if (!instantiated && currentDate.dayOfMonth < recurringTransfer.monthDay) {
                    continue
                }

                // at this point we know that the recurring transfer has been instantiated in the past

                val lastInstantiationDate = LocalDate.of(
                    recurringTransfer.lastInstantiationYear!!,
                    recurringTransfer.lastInstantiationMonthInt!!,
                    recurringTransfer.lastInstantiationDay!!
                )

                var monthYear = arrayOf(lastInstantiationDate.monthValue, lastInstantiationDate.year)
                val currentMonthYear = arrayOf(currentDate.monthValue, currentDate.year)


                while (!monthYear.contentEquals(currentMonthYear)) {
                    // we start by incrementing month year
                    monthYear = if (monthYear[0] == 12) {
                        arrayOf(1, monthYear[1] + 1)
                    } else {
                        arrayOf(monthYear[0] + 1, monthYear[1])
                    }

                    if (monthYear.contentEquals(currentMonthYear) && currentDate.dayOfMonth >= recurringTransfer.monthDay) {
                        val transferDate = LocalDate.of(
                            currentDate.year,
                            currentDate.monthValue,
                            recurringTransfer.monthDay
                        )
                        val newTransferTimeMillis = getFirstAvailableTimeMillis(
                            "Transfer",
                            transferDate.dayOfMonth,
                            transferDate.monthValue,
                            transferDate.year
                        )
                        val newTransferDateTime = convertTimeMillisToLocalDateTime(newTransferTimeMillis)
                        val newTransfer = Transfer(
                            newTransferTimeMillis,
                            newTransferDateTime.minute,
                            newTransferDateTime.hour,
                            newTransferDateTime.dayOfMonth,
                            newTransferDateTime.monthValue,
                            newTransferDateTime.year,
                            recurringTransfer.value,
                            recurringTransfer.note,
                            recurringTransfer.accountSendingNumber,
                            recurringTransfer.accountReceivingNumber
                        )
                        transfersDatabase.insert(newTransfer)

                        // update account balance

                        var accountSending: Account? = null
                        var accountReceiving: Account? = null

                        if (recurringTransfer.accountSendingNumber != null) {
                            accountSending = accountsDatabase.get(recurringTransfer.accountSendingNumber!!)
                        }
                        if (recurringTransfer.accountReceivingNumber != null) {
                            accountReceiving = accountsDatabase.get(recurringTransfer.accountReceivingNumber!!)
                        }

                        if (accountSending != null) {
                            var newBalance = accountSending.balance - recurringTransfer.value
                            newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                            accountsDatabase.updateBalance(accountSending.number, newBalance)
                        }
                        if (accountReceiving != null) {
                            var newBalance = accountReceiving.balance + recurringTransfer.value
                            newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                            accountsDatabase.updateBalance(accountReceiving.number, newBalance)
                        }
                    }


                    if (!monthYear.contentEquals(currentMonthYear)) {
                        // in this case we always build a transfer
                        val transferDate = LocalDate.of(monthYear[1], monthYear[0], recurringTransfer.monthDay)

                        val newTransferTimeMillis = getFirstAvailableTimeMillis(
                            "Transfer",
                            transferDate.dayOfMonth,
                            transferDate.monthValue,
                            transferDate.year
                        )
                        val newTransferDateTime = convertTimeMillisToLocalDateTime(newTransferTimeMillis)
                        val newTransfer = Transfer(
                            newTransferTimeMillis,
                            newTransferDateTime.minute,
                            newTransferDateTime.hour,
                            newTransferDateTime.dayOfMonth,
                            newTransferDateTime.monthValue,
                            newTransferDateTime.year,
                            recurringTransfer.value,
                            recurringTransfer.note,
                            recurringTransfer.accountSendingNumber,
                            recurringTransfer.accountReceivingNumber
                        )
                        transfersDatabase.insert(newTransfer)

                        // update account balance

                        var accountSending: Account? = null
                        var accountReceiving: Account? = null

                        if (recurringTransfer.accountSendingNumber != null) {
                            accountSending = accountsDatabase.get(recurringTransfer.accountSendingNumber!!)
                        }
                        if (recurringTransfer.accountReceivingNumber != null) {
                            accountReceiving = accountsDatabase.get(recurringTransfer.accountReceivingNumber!!)
                        }

                        if (accountSending != null) {
                            var newBalance = accountSending.balance - recurringTransfer.value
                            newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                            accountsDatabase.updateBalance(accountSending.number, newBalance)
                        }
                        if (accountReceiving != null) {
                            var newBalance = accountReceiving.balance + recurringTransfer.value
                            newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                            accountsDatabase.updateBalance(accountReceiving.number, newBalance)
                        }
                    }
                }


                // while loop exited, need to update last instantiation date
                recurringTransfersDatabase.updateInstantiationDate(
                    recurringTransfer.recurringTransferId,
                    recurringTransfer.monthDay,
                    currentMonthYear[0],
                    currentMonthYear[1]
                )
            }
        }
    }


    private suspend fun getFirstAvailableTimeMillis(type: String, day: Int, month: Int, year: Int): Long {
        // here type is either "Transaction" or "Transfer"
        // this function should return millis with millis - startMillis smaller than 86.400.000
        // otherwise we are changing the day

        val date = LocalDate.of(year, month, day)
        val dateTime = LocalDateTime.of(date, LocalTime.of(0, 0))
        val startMillis = convertLocalDateTimeToMillis(dateTime)
        var millis = startMillis + 1
        withContext(Dispatchers.IO) {

            val actsListFromDay: List<Act> = if (type == "Transaction") {
                transactionsDatabase.getTransactionsFromDay(day, month, year)
            } else {
                transfersDatabase.getTransfersFromDay(day, month, year)
            }

            val takenMillisList: List<Long> = actsListFromDay.map {act ->
                if (act is Transaction) {
                    return@map act.transactionId
                }
                if (act is Transfer) {
                    return@map act.transferId
                }
                return@map 0L
            }

            while (millis in takenMillisList) {
                millis++
            }
        }
        return millis
    }
}
