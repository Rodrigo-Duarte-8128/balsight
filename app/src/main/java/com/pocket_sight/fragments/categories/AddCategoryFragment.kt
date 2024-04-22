package com.pocket_sight.fragments.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.Switch
import androidx.fragment.app.Fragment
import androidx.navigation.findNavController
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentAddAccountBinding
import com.pocket_sight.databinding.FragmentAddCategoryBinding
import com.pocket_sight.types.accounts.Account
import com.pocket_sight.types.accounts.AccountsDao
import com.pocket_sight.types.accounts.AccountsDatabase
import com.pocket_sight.types.categories.CategoriesDao
import com.pocket_sight.types.categories.CategoriesDatabase
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.math.RoundingMode


class AddCategoryFragment: Fragment() {
    private var _binding: FragmentAddCategoryBinding? = null
    val binding get() = _binding!!

    lateinit var database: CategoriesDao

    val uiScope = CoroutineScope(Dispatchers.Main + Job())

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddCategoryBinding.inflate(inflater, container, false)
        database = CategoriesDatabase.getInstance(requireNotNull(this.activity).application).categoriesDatabaseDao

        val nameEditText: EditText = binding.categoryNameEditText
        val kindSpinner: Spinner = binding.categoryKindSpinner

        ArrayAdapter.createFromResource(
            this.requireContext(),
            R.array.kinds_array,
            R.layout.category_kind_spinner
        ).also { adapter ->
            // Specify the layout to use when the list of choices appears.
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            // Apply the adapter to the spinner.
            kindSpinner.adapter = adapter
        }

        //val addAccountButton: Button = binding.addAccountButton
        //addAccountButton.setOnClickListener { view: View ->
        //    addAccount(
        //        nameEditText,
        //        balanceEditText,
        //        switch,
        //        view
        //    )
        //}


        return binding.root
    }

}
