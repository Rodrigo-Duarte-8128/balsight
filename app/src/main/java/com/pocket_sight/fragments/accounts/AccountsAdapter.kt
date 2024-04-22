package com.pocket_sight.fragments.accounts

import android.content.Context
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.types.Account
import com.pocket_sight.types.AccountsDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import kotlin.properties.Delegates


class AccountsAdapter(val context: Context, val accounts: List<Account>): RecyclerView.Adapter<AccountsAdapter.ViewHolder>() {

    private val database = AccountsDatabase.getInstance(context).accountsDao
    val uiScope = CoroutineScope(Dispatchers.Main + Job())
    private var accountNumber = 0

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val numberView: TextView = itemView.findViewById(R.id.row_account_number_text_view)
        val accountNameView: TextView = itemView.findViewById(R.id.row_account_name_text_view)
        val accountTotalView: TextView = itemView.findViewById(R.id.row_account_total_text_view)
        val rowLayout: LinearLayout = itemView.findViewById(R.id.accounts_rv_row_layout)
        //val accountNumber: Int = numberView.text.toString().toInt()

        //init {
         //   rowLayout.setOnClickListener {
                //Toast.makeText(context, "account clicked!", Toast.LENGTH_SHORT).show()
                //itemView.findNavController().navigate(
                //    AccountsFragmentDirections.actionAccountsFragmentToEditAccountFragment(accountNumber)
                //)
          //  }
        //}

        init {
            rowLayout.setOnClickListener() {
                val position = adapterPosition
                Log.i("TAG", "Position on click: $position")
                accountNumber = accounts[position].number
                itemView.findNavController().navigate(
                    AccountsFragmentDirections.actionAccountsFragmentToEditAccountFragment(accountNumber)
                )
            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val actView = inflater.inflate(R.layout.accounts_rv_row, parent, false)
        return ViewHolder(actView)
    }

    override fun getItemCount(): Int {
        return accounts.size
    }

    override fun onBindViewHolder(viewHolder: AccountsAdapter.ViewHolder, position: Int) {
        Log.i("TAG", "Accounts List: $accounts")
        Log.i("TAG", "Position: $position")
        val account = accounts[position]
        viewHolder.numberView.text = "${account.number}"
        viewHolder.accountNameView.text = account.name
        if (account.balance == account.balance.toInt().toDouble()) {
            viewHolder.accountTotalView.text = "\u20ac ${account.balance.toInt()}"
        } else {
            viewHolder.accountTotalView.text = "\u20ac ${account.balance}"
        }
    }

}
