package com.pocket_sight.fragments.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.fragment.app.DialogFragment
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.navigation.findNavController
import androidx.navigation.fragment.findNavController
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentEditAccountBinding
import com.pocket_sight.types.accounts.Account
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.transactions.TransactionsDao
import com.pocket_sight.types.transactions.TransactionsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode

class EditAccountFragment: Fragment(), RemoveAccountDialogFragment.RemoveAccountDialogListener {

    private lateinit var account: Account

    private var _binding: FragmentEditAccountBinding? = null
    val binding get() = _binding!!

    lateinit var accountsDatabase: AccountsDao
    lateinit var transactionsDatabase: TransactionsDao


    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val args = EditAccountFragmentArgs.fromBundle(requireArguments())
        val accountNumber: Int = args.accountId

        _binding = FragmentEditAccountBinding.inflate(inflater, container, false)
        accountsDatabase = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao
        transactionsDatabase = TransactionsDatabase.getInstance(requireNotNull(this.activity).application).transactionsDao

        val menuHost: MenuHost = requireActivity()
        val menuProvider = EditAccountMenuProvider(this.requireContext(), this)
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)



        val editAccountNumberEditText: EditText = binding.editAccountNumberEditText
        val editNameEditText: EditText = binding.editAccountNameEditText
        val editBalanceEditText: EditText = binding.editAccountBalanceEditText
        val editSwitch: Switch = binding.editMainAccountSwitch


        setAccountInformation(
            accountNumber,
            editAccountNumberEditText,
            editNameEditText,
            editBalanceEditText,
            editSwitch
        )


        val confirmEditAccountButton: Button = binding.confirmEditAccountButton
        confirmEditAccountButton.setOnClickListener {view: View ->
            handleConfirmChanges(
                editAccountNumberEditText,
                editNameEditText,
                editBalanceEditText,
                editSwitch,
                view
            )
        }


        return binding.root
    }

    private fun setAccountInformation(
        accountNumber: Int,
        accountNumberEditText: EditText,
        nameEditText: EditText,
        balanceEditText: EditText,
        switch: Switch
        ) {
        uiScope.launch {
            withContext(Dispatchers.IO) {
                account = accountsDatabase.get(accountNumber)
            }
            setInfo(
                account,
                accountNumberEditText,
                nameEditText,
                balanceEditText,
                switch
            )
        }
    }

    private suspend fun setInfo(
        account: Account,
        accountNumberEditText: EditText,
        nameEditText: EditText,
        balanceEditText: EditText,
        switch: Switch

    ) {
        withContext(Dispatchers.Main) {
            accountNumberEditText.setText(account.number.toString())
            nameEditText.setText(account.name)
            balanceEditText.setText(account.balance.toString())
            switch.setChecked(account.mainAccount)
        }
    }


    fun handleConfirmChanges(
        editAccountNumberEditText: EditText,
        editNameEditText: EditText,
        editBalanceEditText: EditText,
        editSwitch: Switch,
        view: View
    ) {
        uiScope.launch {

            var balance: Double
            val balanceString = editBalanceEditText.text.toString()

            try {
                balance = balanceString.toDouble()
                balance = balance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            } catch (e: Exception) {
                editBalanceEditText.error = "Invalid Account Balance"
                return@launch
            }

            val oldAccountNumber: Int = account.number
            var newAccountNumber: Int = 0
            val newAccountNumberString = editAccountNumberEditText.text.toString()

            try {
                newAccountNumber = newAccountNumberString.toInt()
            } catch (e: Exception) {
                editAccountNumberEditText.error = "Invalid Account Number"
                return@launch
            }


            if (newAccountNumber != oldAccountNumber) {
                val newNumberTaken: Boolean = newNumberInDatabase(newAccountNumber)
                if (newNumberTaken) {
                    editAccountNumberEditText.error = "Account Number Already Exists"
                    return@launch
                }

                // update all associated transactions
                withContext(Dispatchers.IO) {
                    transactionsDatabase.updateAccountNumber(oldAccountNumber, newAccountNumber)
                }
            }

            if (editSwitch.isChecked && !account.mainAccount) {
                setMainAttributesToFalse()
            }

            updateAccountInDatabase(
                newAccountNumber,
                editNameEditText.text.toString(),
                balance,
                editSwitch.isChecked
            )



            view.findNavController().navigate(R.id.action_editAccountFragment_to_accounts_fragment)


        }
    }

    private suspend fun updateAccountInDatabase(
        newAccountNumber: Int,
        newName: String,
        newBalance: Double,
        newIsMain: Boolean
        ) {
        withContext(Dispatchers.IO) {
            accountsDatabase.delete(account)
            val newAccount = Account(newAccountNumber, newName, newBalance, newIsMain)
            accountsDatabase.insert(newAccount)
        }
    }


    private suspend fun newNumberInDatabase(newAccountNumber: Int): Boolean {
        return withContext(Dispatchers.IO) {
            accountsDatabase.accountNumberInDatabase(newAccountNumber)
        }
    }


    private suspend fun setMainAttributesToFalse() {
        withContext(Dispatchers.IO) {
            accountsDatabase.setMainToFalse()
        }
    }

    fun showRemoveAccountDialog() {
        RemoveAccountDialogFragment(this).show(this.parentFragmentManager, "RemoveAccountDialog")
    }

    override fun onRemoveAccountDialogPositiveClick(dialog: DialogFragment) {
        Toast.makeText(this.context, "Account Removed. Need also to remove related transactions...", Toast.LENGTH_SHORT).show()
        uiScope.launch {
            withContext(Dispatchers.IO) {
                accountsDatabase.delete(account)
            }
        }
        dialog.findNavController().navigate(R.id.accounts_fragment)
        dialog.dismiss()
    }


}