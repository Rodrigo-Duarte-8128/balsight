
package com.pocket_sight.fragments.categories

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import com.pocket_sight.databinding.FragmentCategoriesBinding
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job


class CategoriesFragment: Fragment() {
    private var _binding: FragmentCategoriesBinding? = null
    val binding get() = _binding!!

    val uiScope = CoroutineScope(Dispatchers.Main + Job())
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {

        _binding = FragmentCategoriesBinding.inflate(inflater, container, false)

        return binding.root
    }
}
