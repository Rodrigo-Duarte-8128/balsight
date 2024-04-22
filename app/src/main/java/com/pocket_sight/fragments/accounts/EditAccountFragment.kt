package com.pocket_sight.fragments.accounts

import android.os.Bundle
import android.util.Log
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
import com.pocket_sight.MainActivity
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentEditAccountBinding
import com.pocket_sight.fragments.home.HomeMenuProvider
import com.pocket_sight.types.Account
import com.pocket_sight.types.AccountsDao
import com.pocket_sight.types.AccountsDatabase
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

    lateinit var database: AccountsDao
    var maxNumberInDatabase: Int = 0

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        val args = EditAccountFragmentArgs.fromBundle(requireArguments())
        val accountNumber: Int = args.accountId

        _binding = FragmentEditAccountBinding.inflate(inflater, container, false)
        database = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao

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
            var main: Boolean
            withContext(Dispatchers.IO) {
                account = database.get(accountNumber)
                //accountNumberEditText.setText(accountNumber.toString())
                //nameEditText.setText(account.name)
                //balanceEditText.setText(account.balance.toString())
                //main = account.mainAccount
                //setSwitch(switch, account.mainAccount)
            }
            setInfo(
                account,
                accountNumberEditText,
                nameEditText,
                balanceEditText,
                switch
            )
            //setSwitch(switch, main)
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

    private suspend fun setSwitch(switch: Switch, bool: Boolean) {
        withContext(Dispatchers.Main) {
            switch.setChecked(bool)
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
            database.delete(account)
            val newAccount = Account(newAccountNumber, newName, newBalance, newIsMain)
            database.insert(newAccount)
        }
    }


    private suspend fun newNumberInDatabase(newAccountNumber: Int): Boolean {
        return withContext(Dispatchers.IO) {
            database.accountNumberInDatabase(newAccountNumber)
        }
    }


    private suspend fun setMainAttributesToFalse() {
        withContext(Dispatchers.IO) {
            database.setMainToFalse()
        }
    }

    fun showRemoveAccountDialog() {
        RemoveAccountDialogFragment(this).show(this.parentFragmentManager, "RemoveAccountDialog")
    }

    override fun onRemoveAccountDialogPositiveClick(dialog: DialogFragment) {
        Toast.makeText(this.context, "Account Removed. Need also to remove related transactions...", Toast.LENGTH_SHORT).show()
        uiScope.launch {
            withContext(Dispatchers.IO) {
                database.delete(account)
            }
        }
        dialog.findNavController().navigate(R.id.accounts_fragment)
        dialog.dismiss()
    }


}