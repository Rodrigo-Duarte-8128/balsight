package com.pocket_sight.fragments.recurring_acts

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class RecurringActsViewModel : ViewModel() {

    private val _text = MutableLiveData<String>().apply {
        value = "This is recurring acts Fragment"
    }
    val text: LiveData<String> = _text
}
