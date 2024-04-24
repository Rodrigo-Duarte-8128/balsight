package com.pocket_sight.fragments.home

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.pocket_sight.R


class AddExpenseMenuProvider(private val context: Context, private val fragment: AddExpenseFragment): MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.add_expense_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_more_options -> fragment.moreOptionsClicked()
        }
        return fragment.onContextItemSelected(menuItem)
    }

}
