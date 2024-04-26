package com.pocket_sight.fragments.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentHomeBinding
import com.pocket_sight.fragments.categories.CategoriesAdapter
import com.pocket_sight.parseMonthYearArrayToText
import com.pocket_sight.parseMonthYearText
import com.pocket_sight.types.Act
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.categories.CategoriesDao
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.SubcategoriesDao
import com.pocket_sight.types.categories.SubcategoriesDatabase
import com.pocket_sight.types.displayed.DisplayedAccount
import com.pocket_sight.types.displayed.DisplayedAccountDao
import com.pocket_sight.types.displayed.DisplayedAccountDatabase
import com.pocket_sight.types.displayed.DisplayedMonthYearDao
import com.pocket_sight.types.displayed.DisplayedMonthYearDatabase
import com.pocket_sight.types.transactions.Transaction
import com.pocket_sight.types.transactions.TransactionsDao
import com.pocket_sight.types.transactions.TransactionsDatabase
import com.pocket_sight.types.transfers.TransfersDao
import com.pocket_sight.types.transfers.TransfersDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    var fabIsExpanded = false

    lateinit var transactionsDatabase: TransactionsDao
    lateinit var transfersDatabase: TransfersDao
    lateinit var displayedMonthYearDatabase: DisplayedMonthYearDao
    lateinit var accountsDatabase: AccountsDao
    lateinit var displayedAccountDatabase: DisplayedAccountDao

    lateinit var subcategoriesDatabase: SubcategoriesDao

    lateinit var adapter: HomeAdapter

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    var displayedMonthYear: Array<Int>? = null
    var displayedAccountNumber: Int? = null

    private val fromBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.from_bottom_fab)
    }
    private val toBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.to_bottom_fab)
    }
    private val rotateClockwiseFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.rotate_clockwise)
    }
    private val rotateAntiClockwiseFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.rotate_anti_clockwise)
    }
    private val fromBottomBgAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.from_bottom_anim)
    }
    private val toBottomBgAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.to_bottom_anim)
    }

    private var touchCoordinates: Array<Float> = arrayOf(0.0f, 0.0f)


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)



        transactionsDatabase = TransactionsDatabase.getInstance(this.requireContext()).transactionsDao
        transfersDatabase = TransfersDatabase.getInstance(this.requireContext()).transfersDao
        displayedMonthYearDatabase = DisplayedMonthYearDatabase.getInstance(this.requireContext()).monthYearDao
        accountsDatabase = AccountsDatabase.getInstance(this.requireContext()).accountsDao
        displayedAccountDatabase = DisplayedAccountDatabase.getInstance(this.requireContext()).displayedAccountDao


        subcategoriesDatabase = SubcategoriesDatabase.getInstance(this.requireContext()).subcategoriesDatabaseDao



        val actsRV = binding.rvActs
        val displayedMonthYearButton: Button = binding.displayedMonthButton
        val displayedAccountButton: Button = binding.displayedAccountButton
        buildFragmentInfo(
            this.requireContext(),
            actsRV,
            displayedMonthYearButton,
            displayedAccountButton
        )


        //val menuHost: MenuHost = requireActivity()
        //val menuProvider = HomeMenuProvider(this.requireContext(), this)
        //menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)



        // handle fab click
        binding.mainHomeFab.setOnClickListener {
            when (fabIsExpanded) {
                true -> shrinkFab()
                false -> expandFab()
            }
        }
        createSecondaryFabsListeners()
        handleTouchWhenFabExpanded()



        return binding.root
    }

    private fun buildFragmentInfo(
        context: Context,
        actsRV: RecyclerView,
        displayedMonthYearButton: Button,
        displayedAccountButton: Button
    ) {
        uiScope.launch {

            //withContext(Dispatchers.IO) {
            //    transactionsDatabase.clear()
            //    subcategoriesDatabase.clear()
            //}

            var displayedMonthYearArray = arrayOf(
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

            //var displayedAccountNumber: Int? = null

            var mainAccountNumber: Int? = withContext(Dispatchers.IO) {
                accountsDatabase.getMainAccountNumber()
            }

            var displayedAccountList: List<DisplayedAccount> = withContext(Dispatchers.IO) {
                displayedAccountDatabase.getAllDisplayedAccount()
            }

            if (displayedAccountList.isEmpty() && mainAccountNumber != null) {
                displayedAccountNumber = mainAccountNumber
            }

            if (displayedAccountList.isNotEmpty()) {
                displayedAccountNumber = displayedAccountList[0].displayedAccountNumber
            } else if (mainAccountNumber != null) {
                displayedAccountNumber = mainAccountNumber
            }

            val accountNumber = displayedAccountNumber
            if (accountNumber == null) {
                displayedAccountButton.text = "None"
                //adapter = HomeAdapter(context, listOf())
                return@launch
            }

            // set buttons with correct info
            val displayedAccountName = withContext(Dispatchers.IO) {
                accountsDatabase.getNameFromAccountNumber(accountNumber)
            }
            displayedAccountButton.text = displayedAccountName
            displayedMonthYearButton.text = parseMonthYearArrayToText(displayedMonthYearArray)


            val transactionsList = withContext(Dispatchers.IO) {
                transactionsDatabase.getTransactionsFromMonthYear(
                    displayedMonthYearArray[0],
                    displayedMonthYearArray[1],
                    accountNumber
                )
            }
            val transfersListFromMonthYear = withContext(Dispatchers.IO) {
                transfersDatabase.getTransfersFromMonthYear(
                    displayedMonthYearArray[0],
                    displayedMonthYearArray[1]
                )
            }

            val transfersListWithDisplayedAccount = transfersListFromMonthYear.filter {
                it.accountReceivingNumber == displayedAccountNumber || it.accountSendingNumber == displayedAccountNumber
            }

            val actsList: List<Act> = (transactionsList + transfersListWithDisplayedAccount).sortedByDescending {
                it.getId()
            }
            adapter = HomeAdapter(context, actsList, displayedAccountNumber)

            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL

            actsRV.adapter = adapter
            actsRV.layoutManager = layoutManager
        }
    }


    private fun createSecondaryFabsListeners() {
        binding.addExpenseFab.setOnClickListener{view: View ->
            val accountNumber = displayedAccountNumber
            if (accountNumber == null) {
                Toast.makeText(this.context, "No Account Selected", Toast.LENGTH_SHORT).show()
            } else {
                view.findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToAddExpenseFragment(
                        accountNumber,
                        System.currentTimeMillis(),
                        "",
                        "-1",
                        -1,
                        -1
                    )
                )
            }

        }

        binding.addIncomeFab.setOnClickListener{view: View ->
            val accountNumber = displayedAccountNumber
            if (accountNumber == null) {
                Toast.makeText(this.context, "No Account Selected", Toast.LENGTH_SHORT).show()
            } else {
                view.findNavController().navigate(
                    HomeFragmentDirections.actionHomeFragmentToAddIncomeFragment(
                        System.currentTimeMillis(),
                        accountNumber,
                        "",
                        "-1",
                        -1,
                        -1
                    )
                )
            }
        }

        binding.addTransferFab.setOnClickListener{view: View ->
            view.findNavController().navigate(
                HomeFragmentDirections.actionHomeFragmentToAddTransferFragment(
                    System.currentTimeMillis()
                )
            )
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        fabIsExpanded = false

    }

    fun shrinkFab() {
        binding.homeScreenGreyView.visibility = View.GONE
        binding.mainHomeFab.startAnimation(rotateAntiClockwiseFabAnim)
        binding.addExpenseFab.startAnimation(toBottomFabAnim)
        binding.addIncomeFab.startAnimation(toBottomFabAnim)
        binding.addTransferFab.startAnimation(toBottomFabAnim)
        binding.addExpenseTextView.startAnimation(toBottomFabAnim)
        binding.addIncomeTextView.startAnimation(toBottomFabAnim)
        binding.addTransferTextView.startAnimation(toBottomFabAnim)


        fabIsExpanded = !fabIsExpanded
    }

    fun expandFab() {
        binding.homeScreenGreyView.visibility = View.VISIBLE
        binding.mainHomeFab.startAnimation(rotateClockwiseFabAnim)
        binding.addExpenseFab.startAnimation(fromBottomFabAnim)
        binding.addIncomeFab.startAnimation(fromBottomFabAnim)
        binding.addTransferFab.startAnimation(fromBottomFabAnim)
        binding.addExpenseTextView.startAnimation(fromBottomFabAnim)
        binding.addIncomeTextView.startAnimation(fromBottomFabAnim)
        binding.addTransferTextView.startAnimation(fromBottomFabAnim)


        fabIsExpanded = !fabIsExpanded
    }

    @SuppressLint("ClickableViewAccessibility")
    fun handleTouchWhenFabExpanded() {
        val view = binding.homeScreenGreyView
        val touchListener = View.OnTouchListener { v, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                touchCoordinates[0] = event.x
                touchCoordinates[1] = event.y
            }
            return@OnTouchListener false
        }

        val clickListener = View.OnClickListener {
            if (fabIsExpanded) {
                val outRect = Rect()
                binding.fabConstraintLayout.getGlobalVisibleRect(outRect)
                if (!outRect.contains(touchCoordinates[0].toInt(), touchCoordinates[1].toInt())) {
                    shrinkFab()
                }
            }
        }

        view.setOnTouchListener(touchListener)
        view.setOnClickListener(clickListener)
    }


}



