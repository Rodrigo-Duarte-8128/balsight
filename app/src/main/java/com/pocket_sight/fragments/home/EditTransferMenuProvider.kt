package com.pocket_sight.fragments.home

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.pocket_sight.R


class EditTransferMenuProvider(private val context: Context, private val fragment: EditTransferFragment): MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.edit_transfer_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_remove_transfer -> fragment.showRemoveTransferDialog()
        }
        return fragment.onContextItemSelected(menuItem)
    }

}
