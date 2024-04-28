package com.pocket_sight.fragments.recurring_acts

import android.annotation.SuppressLint
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentRecurringActsBinding
import com.pocket_sight.fragments.home.HomeAdapter
import com.pocket_sight.fragments.home.HomeFragmentDirections
import com.pocket_sight.parseMonthYearArrayToText
import com.pocket_sight.types.Act
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.displayed.DisplayedAccount
import com.pocket_sight.types.displayed.RecurringDisplayedAccount
import com.pocket_sight.types.displayed.RecurringDisplayedAccountDao
import com.pocket_sight.types.displayed.RecurringDisplayedAccountDatabase
import com.pocket_sight.types.recurring.RecurringAct
import com.pocket_sight.types.recurring.RecurringTransactionsDao
import com.pocket_sight.types.recurring.RecurringTransactionsDatabase
import com.pocket_sight.types.recurring.RecurringTransferDao
import com.pocket_sight.types.recurring.RecurringTransferDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime

class RecurringActsFragment : Fragment() {

    private var _binding: FragmentRecurringActsBinding? = null

    private val binding get() = _binding!!

    var fabIsExpanded = false
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

    lateinit var recurringActsRv: RecyclerView
    lateinit var adapter: RecurringActsAdapter

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    lateinit var displayedAccountButton: Button
    var recurringDisplayedAccountNumber: Int? = null

    lateinit var accountsDatabase: AccountsDao
    lateinit var recurringDisplayedAccountsDatabase: RecurringDisplayedAccountDao
    lateinit var recurringTransactionsDatabase: RecurringTransactionsDao
    lateinit var recurringTransfersDatabase: RecurringTransferDao

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentRecurringActsBinding.inflate(inflater, container, false)

        recurringActsRv = binding.rvRecurringActs

        accountsDatabase = AccountsDatabase.getInstance(this.requireContext()).accountsDao
        recurringDisplayedAccountsDatabase = RecurringDisplayedAccountDatabase.getInstance(this.requireContext()).recurringDisplayedAccountDao
        recurringTransactionsDatabase = RecurringTransactionsDatabase.getInstance(this.requireContext()).recurringTransactionsDao
        recurringTransfersDatabase = RecurringTransferDatabase.getInstance(this.requireContext()).recurringTransferDao

        displayedAccountButton = binding.recurringActsFragmentDisplayedAccountButton
        displayedAccountButton.setOnClickListener {view: View ->

        }

        buildFragmentInfo()

        binding.mainRecurringActsFab.setOnClickListener {
            when (fabIsExpanded) {
                true -> shrinkFab()
                false -> expandFab()
            }
        }
        createSecondaryFabsListeners()
        handleTouchWhenFabExpanded()

        return binding.root
    }


    private fun buildFragmentInfo() {

        uiScope.launch {

            val mainAccountNumber: Int? = withContext(Dispatchers.IO) {
                accountsDatabase.getMainAccountNumber()
            }

            val recurringDisplayedAccountList: List<RecurringDisplayedAccount> = withContext(Dispatchers.IO) {
                recurringDisplayedAccountsDatabase.getAllRecurringDisplayedAccount()
            }

            if (recurringDisplayedAccountList.isEmpty() && mainAccountNumber != null) {
                recurringDisplayedAccountNumber = mainAccountNumber
            }

            if (recurringDisplayedAccountList.isNotEmpty()) {
                recurringDisplayedAccountNumber = recurringDisplayedAccountList[0].displayedAccountNumber
            } else if (mainAccountNumber != null) {
                recurringDisplayedAccountNumber = mainAccountNumber
            }

            val accountNumber = recurringDisplayedAccountNumber
            if (accountNumber == null) {
                displayedAccountButton.text = "None"
                return@launch
            }

            // set buttons with correct info
            val displayedAccountName = withContext(Dispatchers.IO) {
                accountsDatabase.getNameFromAccountNumber(accountNumber)
            }
            displayedAccountButton.text = displayedAccountName


            val recurringTransactionsList = withContext(Dispatchers.IO) {
                recurringTransactionsDatabase.getAllRecurringTransactionsFromAccount(accountNumber)
            }
            val recurringTransfersList= withContext(Dispatchers.IO) {
                recurringTransfersDatabase.getAllRecurringTransfers()
            }

            val transfersListWithDisplayedAccount = recurringTransfersList.filter {
                it.accountReceivingNumber == recurringDisplayedAccountNumber || it.accountSendingNumber == recurringDisplayedAccountNumber
            }

            val recurringActsList: List<RecurringAct> = (recurringTransactionsList + transfersListWithDisplayedAccount).sortedByDescending {
                it.getId()
            }
            adapter = RecurringActsAdapter(this@RecurringActsFragment.requireContext(), recurringActsList, recurringDisplayedAccountNumber)

            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL

            recurringActsRv.adapter = adapter
            recurringActsRv.layoutManager = layoutManager
        }
    }





    private fun createSecondaryFabsListeners() {
        binding.addRecurringExpenseFab.setOnClickListener{view: View ->
            val accountNumber = recurringDisplayedAccountNumber
            if (accountNumber == null) {
                Toast.makeText(
                    this@RecurringActsFragment.context,
                    "No Account Selected",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                view.findNavController().navigate(RecurringActsFragmentDirections.actionRecurringActsFragmentToAddRecurringExpenseFragment(
                    System.currentTimeMillis(),
                    "-1",
                    "",
                    accountNumber,
                    -1,
                    -1,
                    -1,
                    ""
                ))
            }
        }

        binding.addRecurringIncomeFab.setOnClickListener{view: View ->
            val accountNumber = recurringDisplayedAccountNumber
            if (accountNumber == null) {
                Toast.makeText(
                    this@RecurringActsFragment.context,
                    "No Account Selected",
                    Toast.LENGTH_SHORT
                ).show()
            } else {
                view.findNavController().navigate(RecurringActsFragmentDirections.actionRecurringActsFragmentToAddRecurringIncomeFragment(
                    System.currentTimeMillis(),
                    "-1",
                    "",
                    accountNumber,
                    -1,
                    -1,
                    -1,
                    ""
                ))
            }
        }

        binding.addRecurringTransferFab.setOnClickListener{view: View ->

        }

    }



    private fun shrinkFab() {
        binding.recurringActsFragmentGreyView.visibility = View.GONE
        binding.mainRecurringActsFab.startAnimation(rotateAntiClockwiseFabAnim)
        binding.addRecurringExpenseFab.startAnimation(toBottomFabAnim)
        binding.addRecurringIncomeFab.startAnimation(toBottomFabAnim)
        binding.addRecurringTransferFab.startAnimation(toBottomFabAnim)
        binding.addRecurringExpenseTextView.startAnimation(toBottomFabAnim)
        binding.addRecurringIncomeTextView.startAnimation(toBottomFabAnim)
        binding.addRecurringTransferTextView.startAnimation(toBottomFabAnim)

        fabIsExpanded = !fabIsExpanded
    }

    private fun expandFab() {
        binding.recurringActsFragmentGreyView.visibility = View.VISIBLE
        binding.mainRecurringActsFab.startAnimation(rotateClockwiseFabAnim)
        binding.addRecurringExpenseFab.startAnimation(fromBottomFabAnim)
        binding.addRecurringIncomeFab.startAnimation(fromBottomFabAnim)
        binding.addRecurringTransferFab.startAnimation(fromBottomFabAnim)
        binding.addRecurringExpenseTextView.startAnimation(fromBottomFabAnim)
        binding.addRecurringIncomeTextView.startAnimation(fromBottomFabAnim)
        binding.addRecurringTransferTextView.startAnimation(fromBottomFabAnim)

        fabIsExpanded = !fabIsExpanded
    }

    @SuppressLint("ClickableViewAccessibility")
    fun handleTouchWhenFabExpanded() {
        val view = binding.recurringActsFragmentGreyView
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
                binding.recurringActsFragmentFabConstraintLayout.getGlobalVisibleRect(outRect)
                if (!outRect.contains(touchCoordinates[0].toInt(), touchCoordinates[1].toInt())) {
                    shrinkFab()
                }
            }
        }
        view.setOnTouchListener(touchListener)
        view.setOnClickListener(clickListener)
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
