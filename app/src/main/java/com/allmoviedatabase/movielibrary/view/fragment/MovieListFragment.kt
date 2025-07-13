package com.allmoviedatabase.movielibrary.view.fragment

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.adapter.MovieAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentMovieListBinding
import com.allmoviedatabase.movielibrary.util.MovieCategory
import com.allmoviedatabase.movielibrary.viewmodel.MovieViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieListFragment : Fragment() {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

    private lateinit var movieAdapter: MovieAdapter

    private val viewModel: MovieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        setupUI()
        setupListeners()
        setupClickListeners()
        return binding.root
    }

    private fun setupUI() {
        binding.apply {
            movieAdapter = MovieAdapter()
            movieListRecyclerView.layoutManager =
                GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
            movieListRecyclerView.adapter = movieAdapter
        }
    }

    private fun setupListeners() {
        // ViewModel'deki LiveData'ları gözlemle
        // Bu örnekte sadece popularMovies LiveData'sını gözlemledim.
        viewModel.popularMovies.observe(viewLifecycleOwner) { movies ->
            movieAdapter.updateList(movies)
            binding.movieListRecyclerView.scrollToPosition(0)
        }
        //Mevcut sayfa ve toplam sayfa sayısını gözlemle
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

        }
    }

    private fun setupClickListeners() {

        binding.searchRadioGroup.setOnCheckedChangeListener { group, checkedId ->
            // Hangi RadioButton'ın seçildiğini ID'sine göre kontrol et
            when (checkedId) {
                R.id.popularMovieRadioButton -> viewModel.loadMoviesCategory(MovieCategory.POPULAR)
                R.id.bestMovieRadioButton -> viewModel.loadMoviesCategory(MovieCategory.TOP_RATED)
                R.id.nowPlayingMovieRadioButton -> viewModel.loadMoviesCategory(MovieCategory.NOW_PLAYING)
                R.id.upcomingMovieRadioButton -> viewModel.loadMoviesCategory(MovieCategory.UPCOMING)
            }
        }

        // Buton tıklamalarını ViewModel'daki fonksiyonlara yönlendir
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
}

