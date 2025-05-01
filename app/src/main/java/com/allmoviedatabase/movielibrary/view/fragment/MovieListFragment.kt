package com.allmoviedatabase.movielibrary.view.fragment

import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import com.allmoviedatabase.movielibrary.databinding.FragmentMovieListBinding
import com.allmoviedatabase.movielibrary.viewmodel.MovieViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieListFragment : Fragment() {

    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

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
            viewModel.fetchPopularMovies("en-US", 1)
            viewModel.popularMovies.observe(viewLifecycleOwner) { movies ->
                Log.i("value", "movies: $movies")
            }
            viewModel.error.observe(viewLifecycleOwner) { error ->

            }
            return binding.root
        }
    }
}