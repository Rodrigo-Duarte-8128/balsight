package com.pocket_sight.fragments.categories

import android.content.Context
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import androidx.core.view.MenuProvider
import com.pocket_sight.R



class EditCategoryMenuProvider(private val context: Context, private val fragment: EditCategoryFragment): MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.edit_category_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_add_subcategory -> fragment.addSubcategoryClicked()
            R.id.action_remove_category-> fragment.showRemoveCategoryDialog()

        }
        return fragment.onContextItemSelected(menuItem)
    }


}
