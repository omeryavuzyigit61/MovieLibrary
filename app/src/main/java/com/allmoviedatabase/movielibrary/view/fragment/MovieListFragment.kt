package com.allmoviedatabase.movielibrary.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.adapter.MovieAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentMovieListBinding
import com.allmoviedatabase.movielibrary.util.MovieCategory
import com.allmoviedatabase.movielibrary.util.onItemClickListener
import com.allmoviedatabase.movielibrary.viewmodel.MovieViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieListFragment : Fragment(), onItemClickListener {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

    private lateinit var movieAdapter: MovieAdapter

    private val viewModel: MovieViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        setupUI()
        setupListeners()
        setupClickListeners()
        return binding.root
    }

    private fun setupUI() {
        binding.apply {
            movieAdapter = MovieAdapter(this@MovieListFragment)
            movieListRecyclerView.layoutManager =
                GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
            movieListRecyclerView.adapter = movieAdapter
        }
    }

    private fun setupListeners() {
        viewModel.popularMovies.observe(viewLifecycleOwner) { movies ->
            // updateList yerine submitList kullanıyoruz.
            // ListAdapter farkları hesaplayıp listeyi verimli bir şekilde güncelleyecektir.
            movieAdapter.submitList(movies)
            binding.movieListRecyclerView.scrollToPosition(0)
        }

        viewModel.currentPage.observe(viewLifecycleOwner) { page ->
            binding.buttonPrevious.isEnabled = page > 1
            updatePageInfoText()
        }
        viewModel.totalPages.observe(viewLifecycleOwner) { totalPages ->
            binding.buttonNext.isEnabled = (viewModel.currentPage.value ?: 1) < totalPages
            updatePageInfoText()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            // Hata durumunda kullanıcıya bilgi vermek için bir UI elemanı eklenebilir.
        }
    }

    private fun setupClickListeners() {

        binding.searchRadioGroup.setOnCheckedChangeListener { _, checkedId ->
            when (checkedId) {
                R.id.popularMovieRadioButton -> viewModel.loadMoviesCategory(MovieCategory.POPULAR)
                R.id.bestMovieRadioButton -> viewModel.loadMoviesCategory(MovieCategory.TOP_RATED)
                R.id.nowPlayingMovieRadioButton -> viewModel.loadMoviesCategory(MovieCategory.NOW_PLAYING)
                R.id.upcomingMovieRadioButton -> viewModel.loadMoviesCategory(MovieCategory.UPCOMING)
            }
        }

        binding.buttonNext.setOnClickListener {
            viewModel.nextPage()
        }

        binding.buttonPrevious.setOnClickListener {
            viewModel.previousPage()
        }
    }

    private fun updatePageInfoText() {
        val currentPage = viewModel.currentPage.value ?: "-"
        val totalPages = viewModel.totalPages.value ?: "-"
        binding.textViewPageNumber.text = "$currentPage / $totalPages"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    override fun omItemClick(id: Int) {
        val action = MovieListFragmentDirections.actionMovieListFragmentToDetailMovieFragment(id)
        view?.findNavController()?.navigate(action)
    }
}
