package com.pocket_sight.fragments.home

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.os.Bundle
import android.view.LayoutInflater
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.MotionEvent
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

    private var touchCoordinates: Array<Float> = arrayOf(0.0f, 0.0f)


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

        // handle fab click
        binding.mainHomeFab.setOnClickListener {
            when (fabIsExpanded) {
                true -> shrinkFab()
                false -> expandFab()
            }
        }
        createSecondaryFabsListeners()
        handleTouchWhenFabExpanded()



        return binding.root
    }

    private fun createSecondaryFabsListeners() {
        binding.addExpenseFab.setOnClickListener{
            Toast.makeText(this.context, "Add Expense Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.addIncomeFab.setOnClickListener{
            Toast.makeText(this.context, "Add Income Clicked", Toast.LENGTH_SHORT).show()
        }
        binding.addTransferFab.setOnClickListener{
            Toast.makeText(this.context, "Add Transfer Clicked", Toast.LENGTH_SHORT).show()
        }
    }



    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
        fabIsExpanded = false

    }

    fun shrinkFab() {
        binding.homeScreenGreyView.visibility = View.GONE
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
        binding.homeScreenGreyView.visibility = View.VISIBLE
        binding.mainHomeFab.startAnimation(rotateClockwiseFabAnim)
        binding.addExpenseFab.startAnimation(fromBottomFabAnim)
        binding.addIncomeFab.startAnimation(fromBottomFabAnim)
        binding.addTransferFab.startAnimation(fromBottomFabAnim)
        binding.addExpenseTextView.startAnimation(fromBottomFabAnim)
        binding.addIncomeTextView.startAnimation(fromBottomFabAnim)
        binding.addTransferTextView.startAnimation(fromBottomFabAnim)


        fabIsExpanded = !fabIsExpanded
    }

    @SuppressLint("ClickableViewAccessibility")
    fun handleTouchWhenFabExpanded() {
        val view = binding.homeScreenGreyView
        val touchListener = View.OnTouchListener { v, event ->
            if (event.actionMasked == MotionEvent.ACTION_DOWN) {
                touchCoordinates[0] = event.x
                touchCoordinates[1] = event.y
            }
            return@OnTouchListener false
        }

        val clickListener = View.OnClickListener {
            if (fabIsExpanded) {
                val outRect = Rect()
                binding.fabConstraintLayout.getGlobalVisibleRect(outRect)
                if (!outRect.contains(touchCoordinates[0].toInt(), touchCoordinates[1].toInt())) {
                    shrinkFab()
                }
            }
        }

        view.setOnTouchListener(touchListener)
        view.setOnClickListener(clickListener)
    }


}



