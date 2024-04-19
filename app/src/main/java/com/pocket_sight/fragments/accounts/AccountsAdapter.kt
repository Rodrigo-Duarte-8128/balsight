package com.pocket_sight.fragments.accounts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.types.Account


class AccountsAdapter(val context: Context, val accounts: List<Account>): RecyclerView.Adapter<AccountsAdapter.ViewHolder>() {
    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val numberView: TextView = itemView.findViewById(R.id.row_account_number_text_view)
        val accountNameView: TextView = itemView.findViewById(R.id.row_account_name_text_view)
        val accountTotalView: TextView = itemView.findViewById(R.id.row_account_total_text_view)
        val rowLayout: LinearLayout = itemView.findViewById(R.id.accounts_rv_row_layout)

        init {
            rowLayout.setOnClickListener {
                Toast.makeText(context, "account clicked!", Toast.LENGTH_SHORT).show()
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
        viewHolder.accountTotalView.text = account.balance.toString()
    }
}
