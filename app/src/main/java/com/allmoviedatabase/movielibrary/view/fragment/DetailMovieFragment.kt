package com.allmoviedatabase.movielibrary.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.navigation.fragment.navArgs
import com.allmoviedatabase.movielibrary.databinding.FragmentDetailMovieBinding
import dagger.hilt.android.AndroidEntryPoint


@AndroidEntryPoint
class DetailMovieFragment : Fragment() {

    private var _binding: FragmentDetailMovieBinding? = null
    private val binding get() = _binding!!

    private val args: DetailMovieFragmentArgs by navArgs()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val movieId = args.movieId
        Toast.makeText(requireContext(), "Movie ID: $movieId", Toast.LENGTH_SHORT).show()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        _binding=FragmentDetailMovieBinding.inflate(inflater,container,false)
        setupUI()
        return binding.root
    }

    private fun setupUI() {

    }

}