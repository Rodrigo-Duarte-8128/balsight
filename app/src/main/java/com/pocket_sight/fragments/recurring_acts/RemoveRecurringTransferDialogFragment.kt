package com.pocket_sight.fragments.recurring_acts

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class RemoveRecurringTransferDialogFragment(val fragment: EditRecurringTransferFragment): DialogFragment() {


    interface RemoveRecurringTransferDialogListener {
        fun onRemoveRecurringTransferDialogPositiveClick(dialog: DialogFragment)
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Remove Recurring Transfer?")
            builder.setMessage("This will permanently delete this recurring transfer.")

            builder.setPositiveButton("Confirm") { _, _->
                fragment.onRemoveRecurringTransferDialogPositiveClick(this)
            }


            builder.setNegativeButton("Cancel") {dialog, _->
                dialog.dismiss()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null.")

    }
}
