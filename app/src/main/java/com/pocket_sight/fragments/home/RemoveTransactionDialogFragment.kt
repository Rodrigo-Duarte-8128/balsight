package com.pocket_sight.fragments.home


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class RemoveTransactionDialogFragment(val fragment: EditTransactionFragment): DialogFragment() {


    interface RemoveTransactionDialogListener {
        fun onRemoveTransactionDialogPositiveClick(dialog: DialogFragment)
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Remove Transaction?")
            builder.setMessage("This will permanently delete this transaction and return the value to the associated account.")

            builder.setPositiveButton("Confirm") { _, _->
                fragment.onRemoveTransactionDialogPositiveClick(this)
            }


            builder.setNegativeButton("Cancel") {dialog, _->
                dialog.dismiss()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null.")

    }
}
