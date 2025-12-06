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
import com.allmoviedatabase.movielibrary.adapter.RecommendationAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentDetailMovieBinding
import com.allmoviedatabase.movielibrary.model.Detail.MovieDetail
import com.allmoviedatabase.movielibrary.viewmodel.MovieDetailViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.hilt.android.AndroidEntryPoint
import java.text.NumberFormat
import java.util.Locale
import kotlin.math.roundToInt

@AndroidEntryPoint
class DetailMovieFragment : Fragment() {

    private var _binding: FragmentDetailMovieBinding? = null
    private val binding get() = _binding!!

    private val args: DetailMovieFragmentArgs by navArgs()
    private val viewModel: MovieDetailViewModel by viewModels()

    private lateinit var castAdapter: CastAdapter
    private lateinit var recommendationAdapter: RecommendationAdapter

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Verileri Yükle
        val movieId = args.movieId
        viewModel.loadMovieDetails(movieId)
        viewModel.loadMovieCredits(movieId)
        viewModel.loadMovieRecommendations(movieId)
        viewModel.loadMovieReleaseDates(movieId)

        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        setupCastRecyclerView()
        setupRecommendationsRecyclerView()
    }

    private fun setupRecommendationsRecyclerView() {
        recommendationAdapter = RecommendationAdapter { movieId ->
            val action = DetailMovieFragmentDirections.actionDetailMovieFragmentSelf(movieId)
            findNavController().navigate(action)
        }
        binding.recommendationsRecyclerView.apply {
            adapter = recommendationAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupCastRecyclerView() {
        castAdapter = CastAdapter(
            isTvShow = false, // Film modu
            onCastMemberClicked = { personId ->
                val action = DetailMovieFragmentDirections.actionDetailMovieFragmentToPersonDetailFragment(personId)
                findNavController().navigate(action)
            },
            onShowMoreClicked = {
                val action = DetailMovieFragmentDirections.actionDetailMovieFragmentToFullCastFragment(
                    movieId = args.movieId,
                    mediaType = "movie"
                )
                findNavController().navigate(action)
            }
        )

        binding.castRecyclerView.apply {
            adapter = castAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupObservers() {
        viewModel.movieDetail.observe(viewLifecycleOwner) { movie ->
            bindMovieDetails(movie)
        }

        viewModel.movieCredits.observe(viewLifecycleOwner) { credits ->
            castAdapter.submitList(credits.cast)
        }

        viewModel.movieRecommendations.observe(viewLifecycleOwner) { movies ->
            recommendationAdapter.submitList(movies)
        }

        viewModel.ageRating.observe(viewLifecycleOwner) { rating ->
            if (!rating.isNullOrBlank()) {
                binding.ageRatingTextView.text = rating
                binding.ageRatingTextView.visibility = View.VISIBLE
            } else {
                binding.ageRatingTextView.visibility = View.GONE
            }
        }

        // isLoading ve error observer'ları buraya eklenebilir
    }

    private fun bindMovieDetails(movie: MovieDetail) {
        binding.apply {
            Glide.with(this@DetailMovieFragment)
                .load("https://media.themoviedb.org/t/p/w220_and_h330_face" + movie.posterPath)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .fitCenter()
                .into(abuzittinImageView) // ID'niz bu şekilde kalmış :)

            val fullDate = movie.releaseDate
            val date = fullDate?.take(4) ?: ""
            val genreList = movie.genres ?: emptyList()
            val genreText = genreList.joinToString(", ") { it.name ?: "" }
            val totalMinutes = movie.runtime ?: 0

            if (totalMinutes > 0) {
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                lenghtTextView.text = "${hours}s ${minutes}dk"
            } else {
                lenghtTextView.text = "Süre bilinmiyor"
            }

            val rating = movie.voteAverage?.times(10)
            val color = when {
                rating != null && rating < 40 -> Color.RED
                rating != null && rating < 70 -> Color.YELLOW
                else -> Color.GREEN
            }

            rating?.let {
                ratingTextView.text = "${it.toInt()}%"
                ratingProgressIndCator.setProgress(it.toInt(), true)
                ratingProgressIndCator.setIndicatorColor(color)
            }

            titleTextView.text = movie.title
            shortDateTextView.text = "($date)"
            dateTextView.text = fullDate
            genreTextView.text = genreText
            tagLineTextView.text = movie.tagline
            descriptionTextView.text = movie.overview
            originalTitleTextView.text = movie.originalTitle ?: "-"
            originalLanguageTextView.text = formatLanguage(movie.originalLanguage)
            budgetTextView.text = formatCurrency(movie.budget)
            revenueTextView.text = formatCurrency(movie.revenue)
        }
    }

    private fun formatCurrency(amount: Long?): String {
        if (amount == null || amount <= 0) { return "-" }
        val format = NumberFormat.getCurrencyInstance(Locale.US)
        format.maximumFractionDigits = 0
        return format.format(amount)
    }

    private fun formatLanguage(code: String?): String {
        return when (code) {
            "en" -> "İngilizce"
            "tr" -> "Türkçe"
            "ja" -> "Japonca"
            "ko" -> "Korece"
            "it" -> "İtalyanca"
            else -> code?.uppercase() ?: "-"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}