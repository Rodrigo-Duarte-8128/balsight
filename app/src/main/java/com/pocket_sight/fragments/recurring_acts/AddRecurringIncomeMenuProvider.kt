package com.pocket_sight.fragments.recurring_acts

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.pocket_sight.R


class AddRecurringIncomeMenuProvider(private val context: Context, private val fragment: AddRecurringIncomeFragment): MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.add_recurring_income_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_more_options -> fragment.moreOptionsClicked()
        }
        return fragment.onContextItemSelected(menuItem)
    }
}
