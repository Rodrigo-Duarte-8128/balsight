package com.pocket_sight.fragments.home

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class RemoveTransferDialogFragment(val fragment: EditTransferFragment): DialogFragment() {


    interface RemoveTransferDialogListener{
        fun onRemoveTransferDialogPositiveClick(dialog: DialogFragment)
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Remove Transfer?")
            builder.setMessage("This will permanently delete this transfer and return the value to the associated accounts.")

            builder.setPositiveButton("Confirm") { _, _->
                fragment.onRemoveTransferDialogPositiveClick(this)
            }


            builder.setNegativeButton("Cancel") {dialog, _->
                dialog.dismiss()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null.")

    }
}
