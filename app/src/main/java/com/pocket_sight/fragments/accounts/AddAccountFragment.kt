package com.pocket_sight.fragments.accounts

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import com.pocket_sight.databinding.FragmentAccountsBinding
import com.pocket_sight.types.Account

import com.pocket_sight.databinding.FragmentAddAccountBinding

class AddAccountFragment: Fragment() {
    private var _binding: FragmentAddAccountBinding? = null
    val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentAddAccountBinding.inflate(inflater, container, false)





        return binding.root
    }
}
