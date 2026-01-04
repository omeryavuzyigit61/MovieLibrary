package com.allmoviedatabase.movielibrary.view.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.GridLayoutManager
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.adapter.ProfileMediaAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentUserListDetailBinding
import com.allmoviedatabase.movielibrary.viewmodel.UserListDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class UserListDetailFragment : Fragment(R.layout.fragment_user_list_detail) {

    private var _binding: FragmentUserListDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: UserListDetailViewModel by viewModels()
    private val args: UserListDetailFragmentArgs by navArgs()

    private lateinit var mediaAdapter: ProfileMediaAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentUserListDetailBinding.bind(view)

        setupUI()
        observeViewModel()
    }

    private fun setupUI() {
        binding.toolbarListDetail.title = args.listName
        binding.toolbarListDetail.setNavigationOnClickListener {
            findNavController().navigateUp()
        }

        mediaAdapter = ProfileMediaAdapter { id, mediaType ->
            // GÜVENLİK KONTROLÜ
            if (id == 0) {
                Toast.makeText(context, "Hata: Film ID'si alınamadı.", Toast.LENGTH_SHORT).show()
                return@ProfileMediaAdapter
            }

            if (mediaType == "tv") {
                val action = UserListDetailFragmentDirections.actionUserListDetailFragmentToDetailTvShowFragment(id)
                findNavController().navigate(action)
            } else {
                val action = UserListDetailFragmentDirections.actionUserListDetailFragmentToDetailMovieFragment(id)
                findNavController().navigate(action)
            }
        }

        binding.rvListItems.apply {
            adapter = mediaAdapter
            layoutManager = GridLayoutManager(context, 3)
        }
    }

    private fun observeViewModel() {
        viewModel.listItems.observe(viewLifecycleOwner) { items ->
            mediaAdapter.submitList(items)

            if (items.isEmpty()) {
                binding.tvEmptyList.visibility = View.VISIBLE
            } else {
                binding.tvEmptyList.visibility = View.GONE
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}