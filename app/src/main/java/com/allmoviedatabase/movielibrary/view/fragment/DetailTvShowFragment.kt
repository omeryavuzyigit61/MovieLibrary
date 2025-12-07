package com.allmoviedatabase.movielibrary.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import com.allmoviedatabase.movielibrary.adapter.CastAdapter
import com.allmoviedatabase.movielibrary.adapter.ContentAdapter
import com.allmoviedatabase.movielibrary.adapter.SeasonsAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentDetailTvShowBinding
import com.allmoviedatabase.movielibrary.model.ListItem
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL
import com.allmoviedatabase.movielibrary.viewmodel.DetailTvShowViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class DetailTvShowFragment : Fragment() {

    private var _binding: FragmentDetailTvShowBinding? = null
    private val binding get() = _binding!!

    private val args: DetailTvShowFragmentArgs by navArgs()
    private val viewModel: DetailTvShowViewModel by viewModels()

    private lateinit var seasonsAdapter: SeasonsAdapter
    private lateinit var recommendationsAdapter: ContentAdapter
    private lateinit var castAdapter: CastAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailTvShowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Manuel yükleme fonksiyonları SİLİNDİ.
        // ViewModel init bloğu bunu hallediyor.

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        setupCastRecyclerView()

        // 1. SEZONLAR LİSTESİ
        // Adapter'a tıklama fonksiyonunu (lambda) gönderiyoruz.
        seasonsAdapter = SeasonsAdapter { seasonNumber ->
            val action = DetailTvShowFragmentDirections.actionDetailTvShowFragmentToSeasonDetailFragment(
                tvId = args.tvId,
                seasonNumber = seasonNumber
            )
            findNavController().navigate(action)
        }

        binding.seasonsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = seasonsAdapter
            isNestedScrollingEnabled = false
        }

        // 2. ÖNERİLER LİSTESİ
        recommendationsAdapter = ContentAdapter(isHorizontal = true) { listItem ->
            if (listItem is ListItem.TvShowItem) {
                listItem.tvShow.id?.let { id ->
                    val action = DetailTvShowFragmentDirections.actionDetailTvShowFragmentSelf(id)
                    findNavController().navigate(action)
                }
            }
        }
        binding.recommendationsRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = recommendationsAdapter
        }
    }

    private fun setupCastRecyclerView() {
        castAdapter = CastAdapter(
            isTvShow = true, // Dizi modu aktif
            onCastMemberClicked = { personId ->
                val action = DetailTvShowFragmentDirections.actionDetailTvShowFragmentToPersonDetailFragment(personId)
                findNavController().navigate(action)
            },
            onShowMoreClicked = {
                val action = DetailTvShowFragmentDirections.actionDetailTvShowFragmentToFullCastFragment(
                    movieId = args.tvId,
                    mediaType = "tv"
                )
                findNavController().navigate(action)
            }
        )

        binding.castRecyclerView.apply {
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
            adapter = castAdapter
        }
    }

    private fun setupObservers() {
        viewModel.cast.observe(viewLifecycleOwner) { castList ->
            castAdapter.submitList(castList)
        }

        viewModel.tvDetail.observe(viewLifecycleOwner) { tvShow ->
            binding.apply {
                titleTextView.text = tvShow.name
                val year = tvShow.firstAirDate?.take(4) ?: ""
                shortDateTextView.text = "($year)"
                dateTextView.text = tvShow.firstAirDate
                episodeCountTextView.text = "${tvShow.numberOfSeasons ?: 0} Sezon, ${tvShow.numberOfEpisodes ?: 0} Bölüm"
                genreTextView.text = tvShow.genres?.joinToString(", ") { it.name.toString() } ?: "-"

                if (!tvShow.tagline.isNullOrEmpty()) {
                    tagLineTextView.text = tvShow.tagline
                    tagLineTextView.visibility = View.VISIBLE
                } else {
                    tagLineTextView.visibility = View.GONE
                }

                descriptionTextView.text = tvShow.overview ?: "Açıklama bulunamadı."

                val rating = tvShow.voteAverage?.times(10)
                val color = when {
                    rating != null && rating < 40 -> Color.RED
                    rating != null && rating < 70 -> Color.YELLOW
                    else -> Color.GREEN
                }
                rating?.let {
                    ratingTextView.text = "${it.roundToInt()}%"
                    ratingProgressIndicator.progress = it.roundToInt()
                    ratingProgressIndicator.setIndicatorColor(color)
                }

                Glide.with(requireContext())
                    .load(IMAGE_BASE_URL + tvShow.posterPath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .placeholder(android.R.drawable.ic_menu_gallery)
                    .error(android.R.drawable.stat_notify_error)
                    .into(posterImageView)

                ageRatingTextView.text = "13+"

                seasonsAdapter.submitList(tvShow.seasons)
            }
        }

        viewModel.recommendations.observe(viewLifecycleOwner) { list ->
            if (list.isNullOrEmpty()) {
                binding.recommendationsLabel.visibility = View.GONE
                binding.recommendationsRecyclerView.visibility = View.GONE
            } else {
                binding.recommendationsLabel.visibility = View.VISIBLE
                binding.recommendationsRecyclerView.visibility = View.VISIBLE
                recommendationsAdapter.submitList(list)
            }
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            // binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}