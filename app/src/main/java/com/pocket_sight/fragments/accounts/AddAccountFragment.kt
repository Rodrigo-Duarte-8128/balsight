package com.pocket_sight.fragments.accounts

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentAccountsBinding
import com.pocket_sight.types.Account

import com.pocket_sight.databinding.FragmentAddAccountBinding
import com.pocket_sight.types.AccountsDao
import com.pocket_sight.types.AccountsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode
import kotlin.math.max

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
        database = AccountsDatabase.getInstance(requireNotNull(this.activity).application).accountsDao

        val nameEditText: EditText = binding.accountNameEditText
        val balanceEditText: EditText = binding.accountBalanceEditText
        val switch: Switch = binding.mainAccountSwitch


        val addAccountButton: Button = binding.addAccountButton
        addAccountButton.setOnClickListener {view: View ->
            addAccount(nameEditText,
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
            } catch(e: Exception) {
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
//    fun addAccount(name: String, balanceString: String,switched: Boolean, view: View) {
//        // need to query the database to find the highest value for the account number
//        var balance: Double
//        try {
//            balance = balanceString.toDouble()
//            balance = balance.toBigDecimal().setScale(2, RoundingMode.HALF_UP).toDouble()
//
//        } catch(e: Exception) {
//            return
//        }
//
//        if (switched) {
//            // if switched, then we need to update all accounts to main=false
//            setMainAttributesToFalse()
//        }
//        val accountNumber = maxNumberInDatabase + 1
//        val newAccount = Account(
//            accountNumber,
//            name,
//            balance,
//            switched
//        )
//
//        insertAccountInDatabase(newAccount)
//        view.findNavController().navigate(R.id.action_addAccountFragment_to_accounts_fragment)
//
//    }



    //private fun setMainAttributesToFalse() {
    //    uiScope.launch {
    //        withContext(Dispatchers.IO) {
    //            database.setMainToFalse()
    //        }
    //    }
    //}

//    private fun insertAccountInDatabase(account: Account) {
//        uiScope.launch {
//            withContext(Dispatchers.IO) {
//                database.insert(account)
//            }
//        }
//    }

//    private fun setMaxAccountNumber() {
//        uiScope.launch {
//            withContext(Dispatchers.IO) {
//                val maxNumber = database.getMaxNumber()
//                Log.i("TAG", "Max Number: $maxNumber")
//                maxNumberInDatabase = maxNumber
//            }
//        }
//    }
}
