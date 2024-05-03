package com.pocket_sight.fragments.home

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.LinearLayout.LayoutParams
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.convertMonthIntToString
import com.pocket_sight.types.Act
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.SubcategoriesDatabase
import com.pocket_sight.types.transactions.Transaction
import com.pocket_sight.types.transfers.Transfer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class HomeAdapter(val context: Context, val acts: List<Act>, val displayedAccountNumber: Int?): RecyclerView.Adapter<HomeAdapter.ViewHolder>() {

    private val categoriesDatabase = CategoriesDatabase.getInstance(context).categoriesDatabaseDao
    private val subcategoriesDatabase = SubcategoriesDatabase.getInstance(context).subcategoriesDatabaseDao
    private val accountsDatabase = AccountsDatabase.getInstance(context).accountsDao
    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val dayMonthTextView: TextView = itemView.findViewById(R.id.row_act_day_month_text_view)
        val timeTextView: TextView = itemView.findViewById(R.id.row_act_time_text_view)
        val descriptionView: TextView = itemView.findViewById(R.id.row_act_category_text_view)
        val noteView: TextView = itemView.findViewById(R.id.row_act_note_text_view)
        val valueView: TextView = itemView.findViewById(R.id.row_act_value_text_view)
        val rowLayout: LinearLayout = itemView.findViewById(R.id.acts_rv_row_layout)

        init {
            rowLayout.setOnClickListener {
                val position = adapterPosition
                val act = acts[position]
                if (act is Transaction) {
                    val selectedCategoryNumber: Int = if (act.categoryNumber != null) {
                        act.categoryNumber!!
                    } else {-1}
                    val selectedSubcategoryNumber: Int = if (act.subcategory != null) {
                        act.subcategory!!
                    } else {-1}

                    itemView.findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToEditTransactionFragment(
                            act.transactionId,
                            act.transactionId,
                            act.accountNumber,
                            act.value.toString(),
                            selectedCategoryNumber,
                            selectedSubcategoryNumber,
                            act.note
                        )
                    )
                }

                if (act is Transfer) {
                    // move to EditTransferFragment
                    val accountSendingNumber: Int = if (act.accountSendingNumber != null) {
                        act.accountSendingNumber!!
                    } else {-1}

                    val accountReceivingNumber: Int = if (act.accountReceivingNumber != null) {
                        act.accountReceivingNumber!!
                    } else {-1}

                    itemView.findNavController().navigate(
                        HomeFragmentDirections.actionHomeFragmentToEditTransferFragment(
                            act.transferId,
                            act.value.toString(),
                            act.note,
                            accountSendingNumber,
                            accountReceivingNumber
                        )
                    )
                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val actView = inflater.inflate(R.layout.acts_rv_row, parent, false)
        return ViewHolder(actView)
    }

    override fun getItemCount(): Int {
        return acts.size
    }

    override fun onBindViewHolder(viewHolder: HomeAdapter.ViewHolder, position: Int) {
        val act = acts[position]

        if (act is Transaction) {
            viewHolder.dayMonthTextView.text = "${act.day} ${convertMonthIntToString(act.month)}"
            var minutesString = act.minutes.toString()
            if (minutesString.length == 1) {
                minutesString = "0$minutesString"
            }
            var hoursString = act.hour.toString()
            if (hoursString.length == 1) {
                hoursString = "0$hoursString"
            }
            viewHolder.timeTextView.text = "$hoursString:$minutesString"
            viewHolder.noteView.text = act.note

            if (act.note == "") {
                val noteParams = viewHolder.noteView.layoutParams as LayoutParams
                noteParams.weight = 0F
                viewHolder.noteView.layoutParams = noteParams

                val catParams = viewHolder.descriptionView.layoutParams as LayoutParams
                catParams.weight = 1F
                viewHolder.descriptionView.layoutParams = catParams
            }

            val value = act.value
            val valueString = if (value >= 0) {
                viewHolder.valueView.setTextColor(ContextCompat.getColor(context, R.color.green))
                if (value == value.toInt().toDouble()) {
                    "+ ${act.value.toInt()} \u20ac"
                } else {
                    "+ ${act.value} \u20ac"
                }
            } else {
                val positiveValue = -value
                viewHolder.valueView.setTextColor(ContextCompat.getColor(context, R.color.red))
                if (positiveValue == positiveValue.toInt().toDouble()) {
                    "- ${positiveValue.toInt()} \u20ac"
                } else {
                    "- ${positiveValue} \u20ac"
                }
            }
            viewHolder.valueView.text = valueString

            uiScope.launch {
                val categoryNumber = act.categoryNumber
                var categoryString = ""
                categoryString = if (categoryNumber != null) {
                    withContext(Dispatchers.IO) {
                        categoriesDatabase.get(act.categoryNumber!!).name
                    }
                } else {
                    if (act.oldCategoryName != null) {
                        act.oldCategoryName!!
                    } else {
                        "None"
                    }
                }
                val subcategoryNumber: Int? = act.subcategory
                var subcategoryName = ""
                subcategoryName = if (subcategoryNumber != null) {
                    withContext(Dispatchers.IO) {
                        subcategoriesDatabase.get(subcategoryNumber).name
                    }
                } else {
                    if (act.oldSubcategoryName != null) {
                        act.oldSubcategoryName!!
                    } else {
                        "None"
                    }
                }
                viewHolder.descriptionView.text = "$categoryString / $subcategoryName"
            }
        }


        if (act is Transfer) {
            viewHolder.dayMonthTextView.text = "${act.day} ${convertMonthIntToString(act.month)}"
            var minutesString = act.minute.toString()
            if (minutesString.length == 1) {
                minutesString = "0$minutesString"
            }
            var hoursString = act.hour.toString()
            if (hoursString.length == 1) {
                hoursString = "0$hoursString"
            }
            viewHolder.timeTextView.text = "$hoursString:$minutesString"
            viewHolder.noteView.text = act.note

            val value = act.value
            val valueString = if (displayedAccountNumber == act.accountSendingNumber) {
                viewHolder.valueView.setTextColor(ContextCompat.getColor(context, R.color.red))
                if (value == value.toInt().toDouble()) {
                    "- ${act.value.toInt()} \u20ac"
                } else {
                    "- ${act.value} \u20ac"
                }
            } else {
                viewHolder.valueView.setTextColor(ContextCompat.getColor(context, R.color.green))
                if (value == value.toInt().toDouble()) {
                    "+ ${act.value.toInt()} \u20ac"
                } else {
                    "+ ${act.value} \u20ac"
                }
            }
            viewHolder.valueView.text = valueString


            uiScope.launch {
                var accountSendingName = "Another"
                var accountReceivingName = "Another"

                if (act.accountSendingNumber != null) {
                    accountSendingName = withContext(Dispatchers.IO) {
                        accountsDatabase.getNameFromAccountNumber(act.accountSendingNumber!!)
                    }
                }

                if (act.accountReceivingNumber != null) {
                    accountReceivingName = withContext(Dispatchers.IO) {
                        accountsDatabase.getNameFromAccountNumber(act.accountReceivingNumber!!)
                    }
                }

                if (displayedAccountNumber == act.accountSendingNumber) {
                    viewHolder.descriptionView.text = "$accountSendingName -> $accountReceivingName"
                } else {
                    viewHolder.descriptionView.text = "$accountReceivingName <- $accountSendingName"
                }

            }
        }
    }
}