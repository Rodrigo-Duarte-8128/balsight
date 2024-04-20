package com.pocket_sight.fragments.accounts

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.pocket_sight.MainActivity
import com.pocket_sight.R



class EditAccountMenuProvider(private val context: Context, private val fragment: EditAccountFragment): MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.edit_account_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_remove_account -> fragment.showRemoveAccountDialog()

        }
        return fragment.onContextItemSelected(menuItem)
    }


}

