package com.allmoviedatabase.movielibrary.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.databinding.FragmentPopularListMovieBinding

class PopularListMovieFragment : Fragment() {

    private var _binding: FragmentPopularListMovieBinding? = null
    private val binding get() = _binding!!

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // Inflate the layout for this fragment
        _binding = FragmentPopularListMovieBinding.inflate(inflater, container, false)
        binding.apply {

        }
        return binding.root
    }

}