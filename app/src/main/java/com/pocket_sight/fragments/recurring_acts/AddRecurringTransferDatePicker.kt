package com.pocket_sight.fragments.recurring_acts

import android.app.DatePickerDialog
import android.app.Dialog
import android.os.Bundle
import android.widget.DatePicker
import androidx.fragment.app.DialogFragment

class AddRecurringTransferDatePicker(val day: Int, val month: Int, val year: Int, val fragment: AddRecurringTransferFragment): DialogFragment(), DatePickerDialog.OnDateSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return DatePickerDialog(requireContext(), this, year, month, day)
    }

    override fun onDateSet(view: DatePicker?, year: Int, month: Int, dayOfMonth: Int) {
        fragment.setDate(year, month, dayOfMonth)
    }
}
