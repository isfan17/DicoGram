package com.isfan17.dicogram.ui.home

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.fragment.app.viewModels
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.isfan17.dicogram.R
import com.isfan17.dicogram.databinding.FragmentHomeBinding
import com.isfan17.dicogram.ui.adapter.LoadingStateAdapter
import com.isfan17.dicogram.ui.adapter.StoryAdapter
import com.isfan17.dicogram.ui.viewmodels.MainViewModel
import com.isfan17.dicogram.ui.viewmodels.ViewModelFactory
import com.isfan17.dicogram.utils.Constants.Companion.USER_PREF_TOKEN_NAME

class HomeFragment : Fragment() {

    private var _binding: FragmentHomeBinding? = null
    private val binding get() = _binding!!

    private lateinit var storyAdapter: StoryAdapter
    private lateinit var mToken: String

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentHomeBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val factory: ViewModelFactory = ViewModelFactory.getInstance(requireActivity())
        val viewModel: MainViewModel by viewModels { factory }

        viewModel.getUserPreferences(USER_PREF_TOKEN_NAME).observe(viewLifecycleOwner) { token ->
            mToken = token

            viewModel.getStoriesPagingData("Bearer $token").observe(viewLifecycleOwner) {
                storyAdapter.submitData(lifecycle, it)
            }
        }

        setupRecyclerView()

        val nav = activity?.findViewById<BottomNavigationView>(R.id.bottom_nav)
        nav?.setOnItemReselectedListener { item ->
            if (item.itemId == R.id.navigation_home) {
                binding.rvStories.smoothScrollToPosition(0)
            }
        }

        binding.btnRefresh.setOnClickListener {
            binding.rvStories.scrollToPosition(0)
            storyAdapter.refresh()
        }

        binding.btnAdd.setOnClickListener {
            (activity as MainActivity).moveToStory()
        }
    }

    private fun setupRecyclerView() {
        storyAdapter = StoryAdapter()
        storyAdapter.addLoadStateListener { loadState ->
            val refreshState = loadState.mediator?.refresh
            binding.rvStories.isVisible = refreshState is LoadState.NotLoading
            binding.progressBar.isVisible = refreshState is LoadState.Loading
        }
        binding.rvStories.apply {
            layoutManager = LinearLayoutManager(context)
            setHasFixedSize(true)
            adapter = storyAdapter.withLoadStateFooter(
                footer = LoadingStateAdapter { storyAdapter.retry() }
            )
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

}