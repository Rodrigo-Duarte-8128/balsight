package com.pocket_sight.fragments.home

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.widget.Toast
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import com.pocket_sight.R


class HomeMenuProvider(private val context: Context, private val fragment: Fragment): MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            //R.id.action_add_transfer -> Toast.makeText(context,"clicked add transfer!", Toast.LENGTH_SHORT).show()
            //R.id.action_change_account-> Toast.makeText(context,"clicked add change account!", Toast.LENGTH_SHORT).show()
            R.id.action_custom_range-> Toast.makeText(context,"clicked custom range!", Toast.LENGTH_SHORT).show()
        }
        return fragment.onContextItemSelected(menuItem)
    }

}
