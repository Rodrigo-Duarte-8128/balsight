package com.pocket_sight.fragments.accounts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.types.accounts.Account
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


class AccountsAdapter(val context: Context, val accounts: List<Account>): RecyclerView.Adapter<AccountsAdapter.ViewHolder>() {

    val uiScope = CoroutineScope(Dispatchers.Main + Job())
    private var accountNumber = 0

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val numberView: TextView = itemView.findViewById(R.id.row_account_number_text_view)
        val accountNameView: TextView = itemView.findViewById(R.id.row_account_name_text_view)
        val accountTotalView: TextView = itemView.findViewById(R.id.row_account_total_text_view)
        val rowLayout: LinearLayout = itemView.findViewById(R.id.accounts_rv_row_layout)


        init {
            rowLayout.setOnClickListener() {
                val position = adapterPosition
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
