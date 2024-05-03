package com.pocket_sight.fragments.categories


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class RemoveCategoryDialogFragment(val fragment: EditCategoryFragment): DialogFragment() {


    interface RemoveCategoryDialogListener {
        fun onRemoveCategoryDialogPositiveClick(dialog: DialogFragment)
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Remove Category?")
            builder.setMessage("This will remove all associated subcategories. It will also set the category of associated transactions to null.")

            builder.setPositiveButton("Confirm") { _, _->
                fragment.onRemoveCategoryDialogPositiveClick(this)
            }


            builder.setNegativeButton("Cancel") {dialog, _->
                dialog.dismiss()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null.")

    }
}
