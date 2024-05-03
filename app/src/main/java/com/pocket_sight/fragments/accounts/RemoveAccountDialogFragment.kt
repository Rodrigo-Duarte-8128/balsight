package com.pocket_sight.fragments.accounts

import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class RemoveAccountDialogFragment(val fragment: EditAccountFragment): DialogFragment() {


    interface RemoveAccountDialogListener {
        fun onRemoveAccountDialogPositiveClick(dialog: DialogFragment)
    }


    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Remove Account?")
            builder.setMessage("This will permanently delete all associated transactions.")

            builder.setPositiveButton("Confirm") { _, _->
                fragment.onRemoveAccountDialogPositiveClick(this)
            }


            builder.setNegativeButton("Cancel") {dialog, _->
                dialog.dismiss()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null.")

    }
}