package com.allmoviedatabase.movielibrary.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.lifecycle.observe
import androidx.recyclerview.widget.LinearLayoutManager
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.adapter.CrewAdapter
import com.allmoviedatabase.movielibrary.adapter.FullCastAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentFullCastBinding
import com.allmoviedatabase.movielibrary.viewmodel.FullCastViewModel
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class FullCastFragment : Fragment() {

    private var _binding: FragmentFullCastBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FullCastViewModel by viewModels()

    private lateinit var castAdapter: FullCastAdapter
    private lateinit var crewAdapter: CrewAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentFullCastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupRecyclerViews()
        setupObservers()
    }

    private fun setupRecyclerViews() {
        castAdapter = FullCastAdapter()
        binding.castRecyclerView.apply {
            adapter = castAdapter
            layoutManager = LinearLayoutManager(context)
        }

        crewAdapter = CrewAdapter()
        binding.crewRecyclerView.apply {
            adapter = crewAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    private fun setupObservers() {
        viewModel.cast.observe(viewLifecycleOwner) { castList ->
            castAdapter.submitList(castList)
        }

        viewModel.groupedCrew.observe(viewLifecycleOwner) { groupedList ->
            crewAdapter.submitList(groupedList)
        }

        // isLoading ve error observer'larını da ekleyin
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}
