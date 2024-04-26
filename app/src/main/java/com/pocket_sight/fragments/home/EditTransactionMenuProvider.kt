package com.pocket_sight.fragments.home

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.pocket_sight.R


class EditTransactionMenuProvider(private val context: Context, private val fragment: EditTransactionFragment): MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.edit_transaction_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_more_options -> fragment.moreOptionsClicked()
            R.id.action_remove_transaction -> fragment.showRemoveTransactionDialog()
        }
        return fragment.onContextItemSelected(menuItem)
    }

}
