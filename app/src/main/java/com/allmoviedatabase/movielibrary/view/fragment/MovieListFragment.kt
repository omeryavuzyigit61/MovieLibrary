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
import com.allmoviedatabase.movielibrary.adapter.MovieAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentMovieListBinding
import com.allmoviedatabase.movielibrary.viewmodel.MovieViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieListFragment : Fragment() {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

    private val movieAdapter = MovieAdapter()

    private val viewModel: MovieViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        binding.apply {
            setupUI()
            setupListeners()
            return binding.root
        }
    }

    private fun setupUI() {
        binding.apply {
            movieListRecyclerView.layoutManager =
                GridLayoutManager(context, 2, RecyclerView.VERTICAL, false)
            movieListRecyclerView.adapter = movieAdapter
        }
    }

    private fun setupListeners() {
        viewModel.popularMovies.observe(viewLifecycleOwner) { movies ->
            Log.i("value", ""+movies)
            movieAdapter.updateList(movies)
        }
        viewModel.error.observe(viewLifecycleOwner) { error ->

        }
        viewModel.fetchPopularMovies("tr-TR", 1)
    }
}
