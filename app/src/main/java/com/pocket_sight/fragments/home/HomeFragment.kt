package com.pocket_sight.fragments.home

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.view.ViewGroup
import android.view.animation.Animation
import android.view.animation.AnimationUtils
import android.widget.Toast
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.Fragment
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView.LayoutManager
import com.pocket_sight.R
import com.pocket_sight.databinding.FragmentHomeBinding
import com.pocket_sight.types.Transaction

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    var fabIsExpanded = false

    private val fromBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.from_bottom_fab)
    }
    private val toBottomFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.to_bottom_fab)
    }
    private val rotateClockwiseFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.rotate_clockwise)
    }
    private val rotateAntiClockwiseFabAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.rotate_anti_clockwise)
    }
    private val fromBottomBgAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.from_bottom_anim)
    }
    private val toBottomBgAnim: Animation by lazy {
        AnimationUtils.loadAnimation(this.context, R.anim.to_bottom_anim)
    }


    // This property is only valid between onCreateView and
    // onDestroyView.
    val binding get() = _binding!!

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

        val layoutManager = LinearLayoutManager(this.context)
        layoutManager.orientation = LinearLayoutManager.VERTICAL

        val transaction1 = Transaction(
            System.currentTimeMillis(),
            "Transportation",
            100.56
        )
        val adapter = HomeAdapter(this.requireContext(), listOf(transaction1))

        val homeRecyclerView = binding.rvActs
        homeRecyclerView.adapter = adapter
        homeRecyclerView.layoutManager = layoutManager

        // handle fab
        binding.mainHomeFab.setOnClickListener {
            when (fabIsExpanded) {
                true -> shrinkFab()
                false -> expandFab()
            }
        }

        return binding.root
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null

    }

    fun shrinkFab() {
        binding.homeScreenGreyView.startAnimation(toBottomBgAnim)
        binding.mainHomeFab.startAnimation(rotateAntiClockwiseFabAnim)
        binding.addExpenseFab.startAnimation(toBottomFabAnim)
        binding.addIncomeFab.startAnimation(toBottomFabAnim)
        binding.addTransferFab.startAnimation(toBottomFabAnim)
        binding.addExpenseTextView.startAnimation(toBottomFabAnim)
        binding.addIncomeTextView.startAnimation(toBottomFabAnim)
        binding.addTransferTextView.startAnimation(toBottomFabAnim)


        fabIsExpanded = !fabIsExpanded
    }

    fun expandFab() {
        binding.homeScreenGreyView.startAnimation(fromBottomBgAnim)
        binding.mainHomeFab.startAnimation(rotateClockwiseFabAnim)
        binding.addExpenseFab.startAnimation(fromBottomFabAnim)
        binding.addIncomeFab.startAnimation(fromBottomFabAnim)
        binding.addTransferFab.startAnimation(fromBottomFabAnim)
        binding.addExpenseTextView.startAnimation(fromBottomFabAnim)
        binding.addIncomeTextView.startAnimation(fromBottomFabAnim)
        binding.addTransferTextView.startAnimation(fromBottomFabAnim)


        fabIsExpanded = !fabIsExpanded
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
