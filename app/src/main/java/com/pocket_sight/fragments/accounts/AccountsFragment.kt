package com.pocket_sight.fragments.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pocket_sight.databinding.FragmentAccountsBinding
import com.pocket_sight.databinding.FragmentHomeBinding
import com.pocket_sight.fragments.home.HomeAdapter
import com.pocket_sight.types.Account


class AccountsFragment: Fragment() {
    //private lateinit var binding: FragmentAccountsBinding
    private var _binding: FragmentAccountsBinding? = null
    val binding get() = _binding!!
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAccountsBinding.inflate(inflater, container, false)


        val account: Account = Account(1, "Current", 2000.0, true)

        val adapter = AccountsAdapter(this.requireContext(), listOf(account))
        val layoutManager = LinearLayoutManager(this.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        val accountsRecyclerView = binding.rvAccounts
        accountsRecyclerView.adapter = adapter
        accountsRecyclerView.layoutManager = layoutManager

        return binding.root
    }
}