package com.pocket_sight.fragments.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.pocket_sight.R
import com.pocket_sight.types.accounts.Account

import com.pocket_sight.databinding.FragmentAddAccountBinding
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode

class AddAccountFragment: Fragment() {
    private var _binding: FragmentAddAccountBinding? = null
    val binding get() = _binding!!

    //val database = AccountsDao()
    lateinit var database: AccountsDao
    var maxNumberInDatabase: Int = 0

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddAccountBinding.inflate(inflater, container, false)
        database =
            AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao

        val nameEditText: EditText = binding.accountNameEditText
        val balanceEditText: EditText = binding.accountBalanceEditText
        val switch: Switch = binding.mainAccountSwitch


        val addAccountButton: Button = binding.addAccountButton
        addAccountButton.setOnClickListener { view: View ->
            addAccount(
                nameEditText,
                balanceEditText,
                switch,
                view
            )
        }


        return binding.root
    }

    fun addAccount(nameEditText: EditText, balanceEditText: EditText, switch: Switch, view: View) {
        uiScope.launch {
            var balance: Double
            val balanceString = balanceEditText.text.toString()
            try {
                balance = balanceString.toDouble()
                balance = balance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
            } catch (e: Exception) {
                balanceEditText.error = "Invalid Account Balance"
                return@launch
            }



            if (switch.isChecked) {
                setMainAttributesToFalse()
            }

            val accountNumber = getMaxAccountNumber() + 1

            val newAccount = Account(
                accountNumber,
                nameEditText.text.toString(),
                balance,
                switch.isChecked
            )

            insertAccountInDatabase(newAccount)
            view.findNavController().navigate(R.id.action_addAccountFragment_to_accounts_fragment)
        }
    }


    private suspend fun setMainAttributesToFalse() {
        withContext(Dispatchers.IO) {
            database.setMainToFalse()
        }
    }

    private suspend fun getMaxAccountNumber(): Int {
        return withContext(Dispatchers.IO) {
            database.getMaxNumber()
        }
    }

    private suspend fun insertAccountInDatabase(account: Account) {
        withContext(Dispatchers.IO) {
            database.insert(account)
        }
    }
}
