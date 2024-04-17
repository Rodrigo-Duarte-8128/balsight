package com.pocket_sight.ui.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentHomeBinding

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null

    // This property is only valid between onCreateView and
    // onDestroyView.
    private val binding get() = _binding!!

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val homeViewModel =
            ViewModelProvider(this).get(HomeViewModel::class.java)

        _binding = FragmentHomeBinding.inflate(inflater, container, false)

        val menuHost: MenuHost = requireActivity()
        val menuProvider = HomeMenuProvider(this.requireContext(), this)
        menuHost.addMenuProvider(menuProvider, viewLifecycleOwner, Lifecycle.State.RESUMED)
        return binding.root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }
}



class HomeMenuProvider(private val context: Context, private val fragment: Fragment): MenuProvider {
    override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
        menuInflater.inflate(R.menu.main_menu, menu)
    }

    override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
        when (menuItem.itemId) {
            R.id.action_add_transfer -> Toast.makeText(context,"clicked add transfer!", Toast.LENGTH_SHORT).show()
            R.id.action_change_account-> Toast.makeText(context,"clicked add change account!", Toast.LENGTH_SHORT).show()
            R.id.action_custom_range-> Toast.makeText(context,"clicked custom range!", Toast.LENGTH_SHORT).show()
        }
        return fragment.onContextItemSelected(menuItem)
    }


}
