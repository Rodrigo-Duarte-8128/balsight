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
import com.pocket_sight.types.accounts.Account
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.recurring.RecurringTransactionsDao
import com.pocket_sight.types.recurring.RecurringTransactionsDatabase
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
        //NavigationUI.setupActionBarWithNavController(this, navController, drawerLayout)
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
        //return NavigationUI.navigateUp(navController, drawerLayout)
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

            for (recurringTransaction in recurringTransactionsList) {
                val startDate = LocalDate.of(
                    recurringTransaction.year,
                    recurringTransaction.month,
                    recurringTransaction.day
                )

                var instantiated = false
                if (recurringTransaction.lastInstantiationYear != null && recurringTransaction.lastInstantiationMonthInt != null && recurringTransaction.lastInstantiationDay != null) {
                    instantiated = true
                }

                if (!instantiated) {
                    if (currentDateTime.dayOfMonth >= recurringTransaction.monthDay && dateAfter(currentDate, startDate)) {
                        val transactionDate = LocalDate.of(currentDate.year, currentDate.monthValue, recurringTransaction.monthDay)
                        val transactionDateTime = convertDateAndIdToDateTime(transactionDate, recurringTransaction.recurringTransactionId)
                        val transactionId = convertDateAndIdToTimeMillis(transactionDate, recurringTransaction.recurringTransactionId)
                        val newTransaction = Transaction(
                            transactionId,
                            transactionDateTime.minute,
                            transactionDateTime.hour,
                            transactionDateTime.dayOfMonth,
                            transactionDateTime.monthValue,
                            transactionDateTime.year,
                            recurringTransaction.value,
                            recurringTransaction.accountNumber,
                            recurringTransaction.categoryNumber,
                            recurringTransaction.subcategoryNumber,
                            recurringTransaction.note,
                            recurringTransaction.oldSubcategoryName,
                            recurringTransaction.oldCategoryName
                        )
                        withContext(Dispatchers.IO) {
                            transactionsDatabase.insert(newTransaction)
                        }

                        // update account balance
                        val account = withContext(Dispatchers.IO) {
                            accountsDatabase.get(recurringTransaction.accountNumber)
                        }

                        var newBalance = account.balance + recurringTransaction.value
                        newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                        withContext(Dispatchers.IO) {
                            accountsDatabase.updateBalance(account.number, newBalance)
                        }



                        // update recurringTransaction last instantiation date
                        withContext(Dispatchers.IO) {
                            recurringTransactionsDatabase.updateInstantiationDate(
                                recurringTransaction.recurringTransactionId,
                                transactionDate.dayOfMonth,
                                transactionDate.monthValue,
                                transactionDate.year
                            )
                        }

                    }
                    continue
                }


                // At this point we know the recurring transaction has been instantiated in the past

                val lastInstantiationDate = LocalDate.of(
                    recurringTransaction.lastInstantiationYear!!,
                    recurringTransaction.lastInstantiationMonthInt!!,
                    recurringTransaction.lastInstantiationDay!!
                )

                var monthYear = arrayOf(lastInstantiationDate.monthValue, lastInstantiationDate.year)
                val currentMonthYear = arrayOf(currentDate.monthValue, currentDate.year)


                while (!monthYear.contentEquals(currentMonthYear)) {
                    // we start by incrementing month year
                    if (monthYear[0] == 1) {
                        monthYear = arrayOf(12, monthYear[1] - 1)
                    } else {
                        monthYear = arrayOf(monthYear[0] - 1, monthYear[1])
                    }

                    if (monthYear.contentEquals(currentMonthYear)) {
                        if (currentDateTime.dayOfMonth >= recurringTransaction.monthDay && dateAfter(currentDate, startDate)) {
                            val transactionDate = LocalDate.of(
                                currentDate.year,
                                currentDate.monthValue,
                                recurringTransaction.monthDay
                            )
                            val transactionDateTime = convertDateAndIdToDateTime(
                                transactionDate,
                                recurringTransaction.recurringTransactionId
                            )
                            val transactionId = convertDateAndIdToTimeMillis(
                                transactionDate,
                                recurringTransaction.recurringTransactionId
                            )
                            val newTransaction = Transaction(
                                transactionId,
                                transactionDateTime.minute,
                                transactionDateTime.hour,
                                transactionDateTime.dayOfMonth,
                                transactionDateTime.monthValue,
                                transactionDateTime.year,
                                recurringTransaction.value,
                                recurringTransaction.accountNumber,
                                recurringTransaction.categoryNumber,
                                recurringTransaction.subcategoryNumber,
                                recurringTransaction.note,
                                recurringTransaction.oldSubcategoryName,
                                recurringTransaction.oldCategoryName
                            )
                            withContext(Dispatchers.IO) {
                                transactionsDatabase.insert(newTransaction)
                            }
                            // update account balance
                            val account = withContext(Dispatchers.IO) {
                                accountsDatabase.get(recurringTransaction.accountNumber)
                            }

                            var newBalance = account.balance + recurringTransaction.value
                            newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                            withContext(Dispatchers.IO) {
                                accountsDatabase.updateBalance(account.number, newBalance)
                            }
                        }
                    } else {
                        // in this case we always build a transaction
                        val transactionDate = LocalDate.of(monthYear[1], monthYear[0], recurringTransaction.monthDay)
                        val transactionDateTime = convertDateAndIdToDateTime(transactionDate, recurringTransaction.recurringTransactionId)
                        val transactionId = convertDateAndIdToTimeMillis(transactionDate, recurringTransaction.recurringTransactionId)
                        val newTransaction = Transaction(
                            transactionId,
                            transactionDateTime.minute,
                            transactionDateTime.hour,
                            transactionDateTime.dayOfMonth,
                            transactionDateTime.monthValue,
                            transactionDateTime.year,
                            recurringTransaction.value,
                            recurringTransaction.accountNumber,
                            recurringTransaction.categoryNumber,
                            recurringTransaction.subcategoryNumber,
                            recurringTransaction.note,
                            recurringTransaction.oldSubcategoryName,
                            recurringTransaction.oldCategoryName
                        )
                        withContext(Dispatchers.IO) {
                            transactionsDatabase.insert(newTransaction)
                        }
                        // update account balance
                        val account = withContext(Dispatchers.IO) {
                            accountsDatabase.get(recurringTransaction.accountNumber)
                        }

                        var newBalance = account.balance + recurringTransaction.value
                        newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                        withContext(Dispatchers.IO) {
                            accountsDatabase.updateBalance(account.number, newBalance)
                        }
                    }
                }

                // while loop exited, need to update last instantiation date
                withContext(Dispatchers.IO) {
                    recurringTransactionsDatabase.updateInstantiationDate(
                        recurringTransaction.recurringTransactionId,
                        recurringTransaction.monthDay,
                        currentMonthYear[0],
                        currentMonthYear[1]
                    )
                }
            }


            val recurringTransfersList = withContext(Dispatchers.IO) {
                recurringTransfersDatabase.getAllRecurringTransfers()
            }

            for (recurringTransfer in recurringTransfersList) {
                val startDate = LocalDate.of(
                    recurringTransfer.year,
                    recurringTransfer.month,
                    recurringTransfer.day
                )

                var instantiated = false
                if (recurringTransfer.lastInstantiationYear != null && recurringTransfer.lastInstantiationMonthInt != null && recurringTransfer.lastInstantiationDay != null) {
                    instantiated = true
                }

                if (!instantiated) {
                    if (currentDateTime.dayOfMonth >= recurringTransfer.monthDay && dateAfter(currentDate, startDate)) {
                        val transferDate = LocalDate.of(currentDate.year, currentDate.monthValue, recurringTransfer.monthDay)
                        val transferDateTime = convertDateAndIdToDateTime(transferDate, recurringTransfer.recurringTransferId)
                        val transferId = convertDateAndIdToTimeMillis(transferDate, recurringTransfer.recurringTransferId)
                        val newTransfer = Transfer(
                            transferId,
                            transferDateTime.minute,
                            transferDateTime.hour,
                            transferDateTime.dayOfMonth,
                            transferDateTime.monthValue,
                            transferDateTime.year,
                            recurringTransfer.value,
                            recurringTransfer.note,
                            recurringTransfer.accountSendingNumber,
                            recurringTransfer.accountReceivingNumber
                        )
                        withContext(Dispatchers.IO) {
                            transfersDatabase.insert(newTransfer)
                        }

                        // update account balance
                        var accountSending: Account? = null
                        var accountReceiving: Account? = null

                        if (recurringTransfer.accountSendingNumber != null) {
                            accountSending = withContext(Dispatchers.IO) {
                                accountsDatabase.get(recurringTransfer.accountSendingNumber!!)
                            }
                        }
                        if (recurringTransfer.accountReceivingNumber != null) {
                            accountReceiving = withContext(Dispatchers.IO) {
                                accountsDatabase.get(recurringTransfer.accountReceivingNumber!!)
                            }
                        }

                        if (accountSending != null) {
                            var newBalance = accountSending.balance - recurringTransfer.value
                            newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                            withContext(Dispatchers.IO) {
                                accountsDatabase.updateBalance(accountSending!!.number, newBalance)
                            }
                        }
                        if (accountReceiving != null) {
                            var newBalance = accountReceiving.balance + recurringTransfer.value
                            newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                            withContext(Dispatchers.IO) {
                                accountsDatabase.updateBalance(accountReceiving!!.number, newBalance)
                            }
                        }


                        // update recurringTransfer last instantiation date
                        withContext(Dispatchers.IO) {
                            recurringTransfersDatabase.updateInstantiationDate(
                                recurringTransfer.recurringTransferId,
                                transferDate.dayOfMonth,
                                transferDate.monthValue,
                                transferDate.year
                            )
                        }

                    }
                    continue
                }


                // At this point we know the recurring transfer has been instantiated in the past

                val lastInstantiationDate = LocalDate.of(
                    recurringTransfer.lastInstantiationYear!!,
                    recurringTransfer.lastInstantiationMonthInt!!,
                    recurringTransfer.lastInstantiationDay!!
                )

                var monthYear = arrayOf(lastInstantiationDate.monthValue, lastInstantiationDate.year)
                val currentMonthYear = arrayOf(currentDate.monthValue, currentDate.year)


                while (!monthYear.contentEquals(currentMonthYear)) {
                    // we start by incrementing month year
                    if (monthYear[0] == 1) {
                        monthYear = arrayOf(12, monthYear[1] - 1)
                    } else {
                        monthYear = arrayOf(monthYear[0] - 1, monthYear[1])
                    }

                    if (monthYear.contentEquals(currentMonthYear)) {
                        if (currentDateTime.dayOfMonth >= recurringTransfer.monthDay && dateAfter(currentDate, startDate)) {
                            val transferDate = LocalDate.of(
                                currentDate.year,
                                currentDate.monthValue,
                                recurringTransfer.monthDay
                            )
                            val transferDateTime = convertDateAndIdToDateTime(
                                transferDate,
                                recurringTransfer.recurringTransferId
                            )
                            val transferId = convertDateAndIdToTimeMillis(
                                transferDate,
                                recurringTransfer.recurringTransferId
                            )
                            val newTransfer = Transfer(
                                transferId,
                                transferDateTime.minute,
                                transferDateTime.hour,
                                transferDateTime.dayOfMonth,
                                transferDateTime.monthValue,
                                transferDateTime.year,
                                recurringTransfer.value,
                                recurringTransfer.note,
                                recurringTransfer.accountSendingNumber,
                                recurringTransfer.accountReceivingNumber
                            )
                            withContext(Dispatchers.IO) {
                                transfersDatabase.insert(newTransfer)
                            }

                            var accountSending: Account? = null
                            var accountReceiving: Account? = null

                            if (recurringTransfer.accountSendingNumber != null) {
                                accountSending = withContext(Dispatchers.IO) {
                                    accountsDatabase.get(recurringTransfer.accountSendingNumber!!)
                                }
                            }
                            if (recurringTransfer.accountReceivingNumber != null) {
                                accountReceiving = withContext(Dispatchers.IO) {
                                    accountsDatabase.get(recurringTransfer.accountReceivingNumber!!)
                                }
                            }

                            if (accountSending != null) {
                                var newBalance = accountSending.balance - recurringTransfer.value
                                newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                                withContext(Dispatchers.IO) {
                                    accountsDatabase.updateBalance(accountSending!!.number, newBalance)
                                }
                            }
                            if (accountReceiving != null) {
                                var newBalance = accountReceiving.balance + recurringTransfer.value
                                newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                                withContext(Dispatchers.IO) {
                                    accountsDatabase.updateBalance(accountReceiving!!.number, newBalance)
                                }
                            }
                        }
                    } else {
                        // in this case we always build a transfer
                        val transferDate = LocalDate.of(monthYear[1], monthYear[0], recurringTransfer.monthDay)
                        val transferDateTime = convertDateAndIdToDateTime(transferDate, recurringTransfer.recurringTransferId)
                        val transferId = convertDateAndIdToTimeMillis(transferDate, recurringTransfer.recurringTransferId)
                        val newTransfer = Transfer(
                            transferId,
                            transferDateTime.minute,
                            transferDateTime.hour,
                            transferDateTime.dayOfMonth,
                            transferDateTime.monthValue,
                            transferDateTime.year,
                            recurringTransfer.value,
                            recurringTransfer.note,
                            recurringTransfer.accountSendingNumber,
                            recurringTransfer.accountReceivingNumber
                        )
                        withContext(Dispatchers.IO) {
                            transfersDatabase.insert(newTransfer)
                        }
                        // update account balance
                        var accountSending: Account? = null
                        var accountReceiving: Account? = null

                        if (recurringTransfer.accountSendingNumber != null) {
                            accountSending = withContext(Dispatchers.IO) {
                                accountsDatabase.get(recurringTransfer.accountSendingNumber!!)
                            }
                        }
                        if (recurringTransfer.accountReceivingNumber != null) {
                            accountReceiving = withContext(Dispatchers.IO) {
                                accountsDatabase.get(recurringTransfer.accountReceivingNumber!!)
                            }
                        }

                        if (accountSending != null) {
                            var newBalance = accountSending.balance - recurringTransfer.value
                            newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                            withContext(Dispatchers.IO) {
                                accountsDatabase.updateBalance(accountSending.number, newBalance)
                            }
                        }
                        if (accountReceiving != null) {
                            var newBalance = accountReceiving.balance + recurringTransfer.value
                            newBalance = newBalance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
                            withContext(Dispatchers.IO) {
                                accountsDatabase.updateBalance(accountReceiving.number, newBalance)
                            }
                        }
                    }
                }

                // while loop exited, need to update last instantiation date
                withContext(Dispatchers.IO) {
                    recurringTransfersDatabase.updateInstantiationDate(
                        recurringTransfer.recurringTransferId,
                        recurringTransfer.monthDay,
                        currentMonthYear[0],
                        currentMonthYear[1]
                    )
                }
            }

        }
    }


}
