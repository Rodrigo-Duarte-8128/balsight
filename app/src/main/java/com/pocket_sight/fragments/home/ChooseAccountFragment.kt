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
import com.pocket_sight.databinding.FragmentChooseAccountBinding
import com.pocket_sight.types.accounts.Account
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.displayed.DisplayedAccount
import com.pocket_sight.types.displayed.DisplayedAccountDao
import com.pocket_sight.types.displayed.DisplayedAccountDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class ChooseAccountFragment: Fragment() {
    private var _binding: FragmentChooseAccountBinding? = null
    private val binding get() = _binding!!


    lateinit var accountsDatabase: AccountsDao
    private lateinit var displayedAccountDatabase: DisplayedAccountDao


    private lateinit var accountSpinner: Spinner
    private lateinit var accountsStringsArray: Array<String>

    lateinit var args: ChooseAccountFragmentArgs

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentChooseAccountBinding.inflate(inflater, container, false)

        args = ChooseAccountFragmentArgs.fromBundle(requireArguments())

        accountsDatabase = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao
        displayedAccountDatabase = DisplayedAccountDatabase.getInstance(this.requireContext()).displayedAccountDao

        accountSpinner = binding.chooseAccountSpinner

        buildFragmentInfo()

        val confirmChoiceButton: Button = binding.confirmChooseAccountButton
        confirmChoiceButton.setOnClickListener {view: View ->
            confirmChoice(view)
        }



        return binding.root
    }

    private fun buildFragmentInfo() {
        uiScope.launch {
            val accountsList: MutableList<Account> = withContext(Dispatchers.IO) {
                accountsDatabase.getAllAccounts()
            }
            val accountsStringsList = accountsList.map {
                "${it.number}. ${it.name}"
            }.toMutableList()
            accountsStringsList.add("None")
            this@ChooseAccountFragment.accountsStringsArray = accountsStringsList.toTypedArray()

            val arrayAdapter: ArrayAdapter<*> = ArrayAdapter<Any?>(
                this@ChooseAccountFragment.requireContext(),
                R.layout.category_kind_spinner,
                accountsStringsArray
            )
            arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            accountSpinner.adapter = arrayAdapter

            // set default selection for spinner
            var defaultSelectionPosition = 0

            val mainAccountNumber: Int? = withContext(Dispatchers.IO) {
                accountsDatabase.getMainAccountNumber()
            }

            val displayedAccountList = withContext(Dispatchers.IO) {
                displayedAccountDatabase.getAllDisplayedAccount()
            }
            if (displayedAccountList.isNotEmpty()) {
                accountsStringsList.forEachIndexed {index, accountString ->
                    if (accountString == "None") {
                        return@forEachIndexed
                    }
                    if (accountString.split(".")[0].toInt() == displayedAccountList[0].displayedAccountNumber) {
                        defaultSelectionPosition = index
                    }
                }
                accountSpinner.setSelection(defaultSelectionPosition)
            } else if (mainAccountNumber != null) {
                accountsStringsList.forEachIndexed {index, accountString ->
                    if (accountString == "None") {
                        return@forEachIndexed
                    }
                    if (accountString.split(".")[0].toInt() == mainAccountNumber) {
                        defaultSelectionPosition = index
                    }
                }
                accountSpinner.setSelection(defaultSelectionPosition)
            } else {
                accountsStringsList.forEachIndexed {index, accountString ->
                    if (accountString == "None") {
                        defaultSelectionPosition = index
                    }
                }
                accountSpinner.setSelection(defaultSelectionPosition)
            }


        }
    }

    private fun confirmChoice(view: View) {
        uiScope.launch {
            val accountChosenString = accountSpinner.selectedItem.toString()

            if (accountChosenString == "None") {
                withContext(Dispatchers.IO) {
                    displayedAccountDatabase.clear()
                }
                if (args.from == "home_fragment") {
                    view.findNavController().navigate(
                        ChooseAccountFragmentDirections.actionChooseAccountFragmentToHomeFragment()
                    )
                    return@launch
                }

                if (args.from == "stats_fragment") {
                    view.findNavController().navigate(
                        ChooseAccountFragmentDirections.actionChooseAccountFragmentToStatsFragment()
                    )
                }
            }

            val accountChosenNumber = accountChosenString.split(".")[0].toInt()
            withContext(Dispatchers.IO) {
                displayedAccountDatabase.clear()
                val displayedAccount = DisplayedAccount(
                    1,
                    accountChosenNumber
                )
                displayedAccountDatabase.insert(displayedAccount)
            }
            if (args.from == "home_fragment") {
                view.findNavController().navigate(
                    ChooseAccountFragmentDirections.actionChooseAccountFragmentToHomeFragment()
                )
                return@launch
            }

            if (args.from == "stats_fragment") {
                view.findNavController().navigate(
                    ChooseAccountFragmentDirections.actionChooseAccountFragmentToStatsFragment()
                )
            }
        }
    }

}