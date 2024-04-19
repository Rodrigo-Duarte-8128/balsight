package com.pocket_sight.fragments.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pocket_sight.databinding.FragmentAccountsBinding
import com.pocket_sight.databinding.FragmentHomeBinding


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

        return binding.root
    }
}