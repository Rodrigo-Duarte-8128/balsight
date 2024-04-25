package com.pocket_sight.fragments.home

import android.app.Dialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.widget.TimePicker
import androidx.fragment.app.DialogFragment
import android.text.format.DateFormat

class MoreOptionsTimePicker(val minute: Int, val hour: Int, val fragment: MoreOptionsFragment): DialogFragment(), TimePickerDialog.OnTimeSetListener {

    override fun onCreateDialog(savedInstanceState: Bundle?): Dialog {
        return TimePickerDialog(
            activity,
            this,
            hour,
            minute,
            //DateFormat.is24HourFormat(activity),
            true,
        )
    }

    override fun onTimeSet(view: TimePicker?, hourOfDay: Int, minute: Int) {
        fragment.setTime(minute, hourOfDay)
    }
}