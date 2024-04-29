package com.pocket_sight.fragments.recurring_acts

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class RemoveRecurringTransactionDialogFragment(val fragment: EditRecurringTransactionFragment): DialogFragment() {


    interface RemoveRecurringTransactionDialogListener {
        fun onRemoveRecurringTransactionDialogPositiveClick(dialog: DialogFragment)
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Remove Recurring Transaction?")
            builder.setMessage("This will permanently delete this recurring transaction.")

            builder.setPositiveButton("Confirm") { _, _->
                fragment.onRemoveRecurringTransactionDialogPositiveClick(this)
            }


            builder.setNegativeButton("Cancel") {dialog, _->
                dialog.dismiss()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null.")

    }
}
