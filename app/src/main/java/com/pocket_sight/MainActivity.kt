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
import com.pocket_sight.types.categories.CategoriesDao
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.Category
import com.pocket_sight.types.categories.SubcategoriesDao
import com.pocket_sight.types.categories.SubcategoriesDatabase
import com.pocket_sight.types.categories.Subcategory
import com.pocket_sight.types.first_run_tracker.FirstRunTracker
import com.pocket_sight.types.first_run_tracker.FirstRunTrackerDao
import com.pocket_sight.types.first_run_tracker.FirstRunTrackerDatabase
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
    private lateinit var categoriesDatabase: CategoriesDao
    private lateinit var subcategoriesDatabase: SubcategoriesDao
    private lateinit var firstRunTrackerDatabase: FirstRunTrackerDao

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
        firstRunTrackerDatabase = FirstRunTrackerDatabase.getInstance(requireNotNull(this).application).firstRunTrackerDao
        categoriesDatabase = CategoriesDatabase.getInstance(requireNotNull(this).application).categoriesDatabaseDao
        subcategoriesDatabase = SubcategoriesDatabase.getInstance(requireNotNull(this).application).subcategoriesDatabaseDao

        buildMissingRecurringActs()

        buildInitialAccountAndCategories()


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

                if (currentDate.dayOfMonth < recurringTransaction.monthDay) {
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

                if (monthYear.contentEquals(currentMonthYear)) {
                    continue
                }

                while (!monthYear.contentEquals(currentMonthYear)) {
                    // we start by incrementing month year
                    monthYear = if (monthYear[0] == 12) {
                        arrayOf(1, monthYear[1] + 1)
                    } else {
                        arrayOf(monthYear[0] + 1, monthYear[1])
                    }

                    // if monthYear is after currentMonthYear break
                    if (monthYear[1] > currentMonthYear[1] || (monthYear[1] == currentMonthYear[1] && monthYear[0] > currentMonthYear[0])) {
                        break
                    }

                    if (monthYear.contentEquals(currentMonthYear)) {
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

                if (currentDate.dayOfMonth < recurringTransfer.monthDay) {
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

                    if (monthYear[1] > currentMonthYear[1] ||(monthYear[1] == currentMonthYear[1] && monthYear[0] > currentMonthYear[0])){
                        break
                    }

                    if (monthYear.contentEquals(currentMonthYear)) {
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

    private fun buildInitialAccountAndCategories() {
        uiScope.launch {
            val trackersList = withContext(Dispatchers.IO) {
                firstRunTrackerDatabase.getAllTrackers()
            }

            if (trackersList.isNotEmpty()) {
                return@launch
            }

            // if this runs, then we are running the app for the first time
            // create first run tracker and add to database
            withContext(Dispatchers.IO) {
                val firstRunTracker = FirstRunTracker(
                    1,
                    true
                )
                firstRunTrackerDatabase.insert(firstRunTracker)

                // create default account
                val currentAccount = Account(
                    1,
                    "Current",
                    0.0,
                    true
                )
                accountsDatabase.insert(currentAccount)

                // create default categories and subcategories

                val entertainmentCat = Category(
                    1,
                    "Entertainment",
                    "Expense"
                )
                categoriesDatabase.insert(entertainmentCat)
                val booksSubcat = Subcategory(
                    1,
                    "Books",
                    1
                )
                subcategoriesDatabase.insert(booksSubcat)
                val gamesSubcat = Subcategory(
                    2,
                    "Games",
                    1
                )
                subcategoriesDatabase.insert(gamesSubcat)
                val cinemaSubcat = Subcategory(
                    3,
                    "Cinema",
                    1
                )
                subcategoriesDatabase.insert(cinemaSubcat)
                val phonesSubcat = Subcategory(
                    4,
                    "Phones",
                    1
                )
                subcategoriesDatabase.insert(phonesSubcat)
                val socialSubcat = Subcategory(
                    5,
                    "Social Activities",
                    1
                )
                subcategoriesDatabase.insert(socialSubcat)
                val subscriptionsSubcat = Subcategory(
                    6,
                    "Subscriptions",
                    1
                )
                subcategoriesDatabase.insert(subscriptionsSubcat)
                val tvSubcat = Subcategory(
                    7,
                    "TV, Internet",
                    1
                )
                subcategoriesDatabase.insert(tvSubcat)



                val financialCat = Category(
                    2,
                    "Financial Expenses",
                    "Expense"
                )
                categoriesDatabase.insert(financialCat)

                val feesSubcat = Subcategory(
                    8,
                    "Fees",
                    2
                )
                subcategoriesDatabase.insert(feesSubcat)

                val finesSubcat = Subcategory(
                    9,
                    "Fines",
                    2
                )
                subcategoriesDatabase.insert(finesSubcat)


                val foodCat = Category(
                    3,
                    "Food",
                    "Expense"
                )
                categoriesDatabase.insert(foodCat)

                val barSubcat = Subcategory(
                    10,
                    "Bar, Cafe",
                    3
                )
                subcategoriesDatabase.insert(barSubcat)

                val outSubcat = Subcategory(
                    11,
                    "Eating Out",
                    3
                )
                subcategoriesDatabase.insert(outSubcat)

                val groceriesSubcat = Subcategory(
                    12,
                    "Groceries",
                    3
                )
                subcategoriesDatabase.insert(groceriesSubcat)


                val healthCat = Category(
                    4,
                    "Health",
                    "Expense"
                )
                categoriesDatabase.insert(healthCat)

                val dentistSubcat = Subcategory(
                    13,
                    "Dentist",
                    4
                )
                subcategoriesDatabase.insert(dentistSubcat)

                val doctorSubcat = Subcategory(
                    14,
                    "Healthcare, Doctor",
                    4
                )
                subcategoriesDatabase.insert(doctorSubcat)

                val insuranceSubcat = Subcategory(
                    15,
                    "Insurance",
                    4
                )
                subcategoriesDatabase.insert(insuranceSubcat)

                val pharmacySubcat = Subcategory(
                    16,
                    "Pharmacy",
                    4
                )
                subcategoriesDatabase.insert(pharmacySubcat)


                val houseCat = Category(
                    5,
                    "House",
                    "Expense"
                )
                categoriesDatabase.insert(houseCat)

                val condoSubcat = Subcategory(
                    17,
                    "Condo",
                    5
                )
                subcategoriesDatabase.insert(condoSubcat)

                val decorationSubcat = Subcategory(
                    18,
                    "Decoration, Furniture",
                    5
                )
                subcategoriesDatabase.insert(decorationSubcat)

                val electricitySubcat = Subcategory(
                    19,
                    "Electricity",
                    5
                )
                subcategoriesDatabase.insert(electricitySubcat)

                val imiSubcat= Subcategory(
                    20,
                    "IMI",
                    5
                )
                subcategoriesDatabase.insert(imiSubcat)

                val lifeInsuranceSubcat = Subcategory(
                    21,
                    "Life Insurance",
                    5
                )
                subcategoriesDatabase.insert(lifeInsuranceSubcat)

                val houseInsuranceSubcat = Subcategory(
                    22,
                    "House Insurance",
                    5
                )
                subcategoriesDatabase.insert(houseInsuranceSubcat)

                val maintenanceSubcat = Subcategory(
                    23,
                    "Maintenance",
                    5
                )
                subcategoriesDatabase.insert(maintenanceSubcat)

                val mortgageSubcat = Subcategory(
                    24,
                    "Mortgage",
                    5
                )
                subcategoriesDatabase.insert(mortgageSubcat)

                val waterSubcat = Subcategory(
                    25,
                    "Water",
                    5
                )
                subcategoriesDatabase.insert(waterSubcat)


                val lifestyleCat = Category(
                    6,
                    "Lifestyle",
                    "Expense"
                )
                categoriesDatabase.insert(lifestyleCat)

                val charitySubcat = Subcategory(
                    26,
                    "Charity",
                    6
                )
                subcategoriesDatabase.insert(charitySubcat)

                subcategoriesDatabase.insert(
                    Subcategory(
                        27,
                        "Cultural Events",
                        6
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        28,
                        "Education",
                        6
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        29,
                        "Gym",
                        6
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        30,
                        "Hobbies",
                        6
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        31,
                        "Life Events",
                        6
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        32,
                        "Music",
                        6
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        33,
                        "Sports",
                        6
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        34,
                        "Trips, Hotels",
                        6
                    )
                )


                categoriesDatabase.insert(
                    Category(
                        7,
                        "Shopping",
                        "Expense"
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        35,
                        "Clothes, Shoes",
                        7
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        36,
                        "Electronics",
                        7
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        37,
                        "Gifts",
                        7
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        38,
                        "Hair Cut",
                        7
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        39,
                        "Office",
                        7
                    )
                )


                categoriesDatabase.insert(
                    Category(
                        8,
                        "Transportation",
                        "Expense"
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        40,
                        "Bus",
                        8
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        41,
                        "Ride Sharing",
                        8
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        42,
                        "Plane",
                        8
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        43,
                        "Public Transport",
                        8
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        44,
                        "Taxi",
                        8
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        45,
                        "Train",
                        8
                    )
                )


                categoriesDatabase.insert(
                    Category(
                        9,
                        "Vehicle",
                        "Expense"
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        46,
                        "Fuel",
                        9
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        47,
                        "Insurance",
                        9
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        48,
                        "Maintenance",
                        9
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        49,
                        "Parking",
                        9
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        50,
                        "Tolls",
                        9
                    )
                )

                categoriesDatabase.insert(
                    Category(
                        10,
                        "Other Expense",
                        "Expense"
                    )
                )

                categoriesDatabase.insert(
                    Category(
                        11,
                        "Salary",
                        "Income"
                    )
                )

                subcategoriesDatabase.insert(
                    Subcategory(
                        51,
                        "Company Name",
                        11
                    )
                )

                categoriesDatabase.insert(
                    Category(
                        12,
                        "Other Income",
                        "Income"
                    )
                )
            }


        }
    }
}
