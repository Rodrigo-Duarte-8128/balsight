package com.pocket_sight.fragments.categories


import android.app.AlertDialog
import android.app.Dialog
import android.os.Bundle
import androidx.fragment.app.DialogFragment
import java.lang.IllegalStateException

class RemoveSubcategoriesDialogFragment(val fragment: EditCategoryFragment): DialogFragment() {


    interface RemoveSubcategoriesDialogListener{
        fun onRemoveSubcategoriesDialogPositiveClick(dialog: DialogFragment)
    }



    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return activity?.let {
            val builder = AlertDialog.Builder(it)
            builder.setTitle("Remove Subcategories?")
            builder.setMessage("By confirming you are removing some subcategories. This will also set the subcategories of associated transactions to null.")

            builder.setPositiveButton("Confirm") { _, _->
                fragment.onRemoveSubcategoriesDialogPositiveClick(this)
            }


            builder.setNegativeButton("Cancel") {dialog, _->
                dialog.dismiss()
            }

            builder.create()
        } ?: throw IllegalStateException("Activity cannot be null.")

    }
}
