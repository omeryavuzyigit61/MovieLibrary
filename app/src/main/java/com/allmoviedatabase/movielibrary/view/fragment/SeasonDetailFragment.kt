package com.allmoviedatabase.movielibrary.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.allmoviedatabase.movielibrary.adapter.EpisodeAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentSeasonDetailBinding
import com.allmoviedatabase.movielibrary.viewmodel.SeasonDetailViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class SeasonDetailFragment : Fragment() {

    private var _binding: FragmentSeasonDetailBinding? = null
    private val binding get() = _binding!!

    // NavArgs ile tvId ve seasonNumber alıyoruz
    private val args: SeasonDetailFragmentArgs by navArgs()

    // Yeni oluşturduğumuz ViewModel
    private val viewModel: SeasonDetailViewModel by viewModels()

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentSeasonDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val tvId = args.tvId
        val seasonNumber = args.seasonNumber

        // Verileri Yükle
        viewModel.loadSeasonDetails(tvId, seasonNumber)

        setupObservers()
    }

    private fun setupObservers() {
        // Sezon Detaylarını Gözlemle
        viewModel.seasonDetails.observe(viewLifecycleOwner) { season ->
            // Adapter'ı Kur
            val adapter = EpisodeAdapter(season.episodes)
            binding.episodesRecyclerView.layoutManager = LinearLayoutManager(context)
            binding.episodesRecyclerView.adapter = adapter
        }

        // Yüklenme Durumu
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Hata Durumu
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (!errorMessage.isNullOrEmpty()) {
                Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}