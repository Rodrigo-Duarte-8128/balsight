package com.pocket_sight.fragments.recurring_acts

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.navigation.findNavController
import androidx.recyclerview.widget.RecyclerView
import com.pocket_sight.R
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.categories.CategoriesDatabase
import com.pocket_sight.types.categories.SubcategoriesDatabase
import com.pocket_sight.types.recurring.RecurringAct
import com.pocket_sight.types.recurring.RecurringTransaction
import com.pocket_sight.types.recurring.RecurringTransfer
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.LocalTime
import java.time.ZoneId

class RecurringActsAdapter(val context: Context, val recurringActs: List<RecurringAct>, val displayedAccountNumber: Int?): RecyclerView.Adapter<RecurringActsAdapter.ViewHolder>() {

    private val categoriesDatabase = CategoriesDatabase.getInstance(context).categoriesDatabaseDao
    private val subcategoriesDatabase = SubcategoriesDatabase.getInstance(context).subcategoriesDatabaseDao
    private val accountsDatabase = AccountsDatabase.getInstance(context).accountsDao
    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    inner class ViewHolder(itemView: View): RecyclerView.ViewHolder(itemView){
        val nameTextView: TextView = itemView.findViewById(R.id.row_recurring_act_name_text_view)
        val descriptionView: TextView = itemView.findViewById(R.id.row_recurring_act_category_text_view)
        val noteView: TextView = itemView.findViewById(R.id.row_recurring_act_note_text_view)
        val valueView: TextView = itemView.findViewById(R.id.row_recurring_act_value_text_view)
        val rowLayout: LinearLayout = itemView.findViewById(R.id.recurring_acts_rv_row_layout)

        init {
            rowLayout.setOnClickListener {
                val position = adapterPosition
                val act = recurringActs[position]
                if (act is RecurringTransaction) {

                    val selectedCategoryNumber: Int = if (act.categoryNumber != null) {
                        act.categoryNumber!!
                    } else {-1}
                    val selectedSubcategoryNumber: Int = if (act.subcategoryNumber!= null) {
                        act.subcategoryNumber!!
                    } else {-1}

                    val dateTime = LocalDateTime.of(LocalDate.of(act.year, act.month, act.day), LocalTime.of(1, 1))
                    val startDateTimeMillis: Long = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    itemView.findNavController().navigate(
                        RecurringActsFragmentDirections.actionRecurringActsFragmentToEditRecurringTransactionFragment(
                            act.recurringTransactionId,
                            startDateTimeMillis,
                            act.accountNumber,
                            act.value.toString(),
                            selectedCategoryNumber,
                            selectedSubcategoryNumber,
                            act.note,
                            act.name,
                            act.monthDay
                        )
                    )
                }

                if (act is RecurringTransfer) {
                    // move to EditTransferFragment
                    val accountSendingNumber: Int = if (act.accountSendingNumber != null) {
                        act.accountSendingNumber!!
                    } else {-1}

                    val accountReceivingNumber: Int = if (act.accountReceivingNumber != null) {
                        act.accountReceivingNumber!!
                    } else {-1}

                    val dateTime = LocalDateTime.of(LocalDate.of(act.year, act.month, act.monthDay), LocalTime.of(1, 1))
                    val startDateTimeMillis: Long = dateTime.atZone(ZoneId.systemDefault()).toInstant().toEpochMilli()

                    itemView.findNavController().navigate(
                        RecurringActsFragmentDirections.actionRecurringActsFragmentToEditRecurringTransferFragment(
                            act.recurringTransferId,
                            startDateTimeMillis,
                            accountSendingNumber,
                            accountReceivingNumber,
                            act.name,
                            act.value.toString(),
                            act.note,
                            act.monthDay
                        )
                    )
                }

            }
        }

    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val context = parent.context
        val inflater = LayoutInflater.from(context)
        val actView = inflater.inflate(R.layout.recurring_acts_rv_row, parent, false)
        return ViewHolder(actView)
    }

    override fun getItemCount(): Int {
        return recurringActs.size
    }

    override fun onBindViewHolder(viewHolder: RecurringActsAdapter.ViewHolder, position: Int) {
        val act = recurringActs[position]

        if (act is RecurringTransaction) {
            viewHolder.nameTextView.text = act.name
            viewHolder.noteView.text = act.note

            if (act.note == "") {
                val noteParams = viewHolder.noteView.layoutParams as LinearLayout.LayoutParams
                noteParams.weight = 0F
                viewHolder.noteView.layoutParams = noteParams

                val catParams = viewHolder.descriptionView.layoutParams as LinearLayout.LayoutParams
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
                val categoryString: String = if (categoryNumber != null) {
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
                val subcategoryNumber: Int? = act.subcategoryNumber
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


        if (act is RecurringTransfer) {
            viewHolder.nameTextView.text = act.name
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