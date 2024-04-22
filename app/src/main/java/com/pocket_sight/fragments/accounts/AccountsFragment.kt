package com.pocket_sight.fragments.accounts

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentAccountsBinding
import com.pocket_sight.types.accounts.Account
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class AccountsFragment: Fragment() {
    //private lateinit var binding: FragmentAccountsBinding
    private var _binding: FragmentAccountsBinding? = null
    val binding get() = _binding!!

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    lateinit var database: AccountsDao

    var accountsList: MutableList<Account> = mutableListOf()
    lateinit var adapter: AccountsAdapter


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAccountsBinding.inflate(inflater, container, false)

        //val database = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao
        database = AccountsDatabase.getInstance(this.requireContext()).accountsDao
        val accountsRecyclerView = binding.rvAccounts
        val totalWealthTextView = binding.totalWealthValueTextView
        buildFragmentInfo(this.requireContext(), accountsRecyclerView, totalWealthTextView)
        //buildRecyclerView(this.requireContext(), accountsRecyclerView)
        //val accounts: List<Account> = database.getAllAccounts()

        //val adapter = AccountsAdapter(this.requireContext(), accounts)
        //adapter = AccountsAdapter(this.requireContext(), accountsList)

        //val layoutManager = LinearLayoutManager(this.context)
        //layoutManager.orientation = LinearLayoutManager.VERTICAL

        //accountsRecyclerView.adapter = adapter
        //accountsRecyclerView.layoutManager = layoutManager

        //setTotalWealth(totalWealthTextView)



        // set click behaviour for fab
        val addAccountFab = binding.addAccountFab
        addAccountFab.setOnClickListener() {view: View ->
            addAccountClicked(view)
        }

        return binding.root
    }

    private fun buildFragmentInfo(
        context: Context,
        accountsRecyclerView: RecyclerView,
        totalWealthTextView: TextView
    ) {
        uiScope.launch {

            accountsList = getAccountsList()
            adapter = AccountsAdapter(context, accountsList)

            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL

            accountsRecyclerView.adapter = adapter
            accountsRecyclerView.layoutManager = layoutManager


            var total = 0.0
            for (account in accountsList) {
                total += account.balance
            }
            var intTotal: Int = 0
            if (total == total.toInt().toDouble()) {
                intTotal = total.toInt()
                totalWealthTextView.text = "\u20ac ${intTotal}"
            } else {
                totalWealthTextView.text = "\u20ac $total"
            }

        }
    }

    private fun buildRecyclerView(context: Context, accountsRecyclerView: RecyclerView) {
        uiScope.launch {
            accountsList = getAccountsList()
            adapter = AccountsAdapter(context, accountsList)

            val layoutManager = LinearLayoutManager(context)
            layoutManager.orientation = LinearLayoutManager.VERTICAL

            accountsRecyclerView.adapter = adapter
            accountsRecyclerView.layoutManager = layoutManager
        }
    }

    private fun setTotalWealth(totalWealthTextView: TextView) {
        uiScope.launch {
            accountsList = getAccountsList()
            var total = 0.0
            for (account in accountsList) {
                total += account.balance
            }
            totalWealthTextView.text = total.toString()
        }
    }



    //private fun initializeAccountsList() {
    //    uiScope.launch {
    //        accountsList = getAccountsList()
    //    }
    //}

    private suspend fun getAccountsList(): MutableList<Account> {
        return withContext(Dispatchers.IO) {
            database.getAllAccounts() ?: mutableListOf()
        }
    }

    //private fun buildAccountsList(fragment: Fragment) {
    //   uiScope.launch {
    //       withContext(Dispatchers.IO) {
    //           accountsList = database.getAllAccounts()
    //           adapter = AccountsAdapter(fragment.requireContext(), accountsList)
    //       }
    //   }
    //}



    fun addAccountClicked(view: View) {
        view.findNavController().navigate(R.id.action_accounts_fragment_to_addAccountFragment)
    }
}