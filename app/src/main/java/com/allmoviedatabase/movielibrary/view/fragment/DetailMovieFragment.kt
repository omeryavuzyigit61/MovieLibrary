package com.allmoviedatabase.movielibrary.view.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
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
import kotlin.getValue


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
        val movieId = args.movieId

        viewModel.loadMovieDetails(movieId)
        viewModel.loadMovieCredits(movieId)
        viewModel.loadMovieRecommendations(movieId)
        viewModel.loadMovieReleaseDates(movieId)

        setupCastRecyclerView()
        setupRecommendationsRecyclerView()
        setupObservers()
        setupUI()
        return binding.root
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
            // 1. Parametre: onCastMemberClicked
            onCastMemberClicked = { personId ->
                // Bir oyuncuya tıklandığında PersonDetailFragment'a git
                val action = DetailMovieFragmentDirections.actionDetailMovieFragmentToPersonDetailFragment(personId)
                findNavController().navigate(action)
            },
            // 2. Parametre: onShowMoreClicked
            onShowMoreClicked = {
                // "Daha Fazla Göster" tıklandığında FullCastFragment'a git
                val action = DetailMovieFragmentDirections.actionDetailMovieFragmentToFullCastFragment(args.movieId)
                findNavController().navigate(action)
            }
        )
        binding.castRecyclerView.apply {
            adapter = castAdapter
            layoutManager = LinearLayoutManager(
                context,
                LinearLayoutManager.HORIZONTAL, false
            )
        }
    }

    private fun setupObservers() {
        viewModel.movieDetail.observe(viewLifecycleOwner) { movie ->
            bindMovieDetails(movie)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            //binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            //Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
        }
        viewModel.movieCredits.observe(viewLifecycleOwner) { credits ->
            credits.cast.let { castList ->
                castAdapter.submitList(castList)
            }
        }
        viewModel.movieRecommendations.observe(viewLifecycleOwner) { movies ->
            recommendationAdapter.submitList(movies)
        }

        viewModel.ageRating.observe(viewLifecycleOwner) { rating ->
            if (rating != null && rating.isNotBlank()) {
                binding.ageRatingTextView.text = rating
                binding.ageRatingTextView.visibility = View.VISIBLE
            } else {
                binding.ageRatingTextView.visibility = View.GONE
            }
        }
    }

    private fun setupUI() {

    }

    private fun DetailMovieFragment.bindMovieDetails(movie: MovieDetail) {

        binding.apply {
            Glide.with(this@DetailMovieFragment)
                .load("https://media.themoviedb.org/t/p/w220_and_h330_face" + movie.posterPath)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .fitCenter()
                .into(abuzittinImageView)

            val fullDate = movie.releaseDate
            val date = fullDate?.substring(0, 4)
            val genreList = movie.genres ?: emptyList()
            val genreText = genreList.joinToString(", ") { it.name ?: "" }
            val totalMinutes = movie.runtime ?: 0 // Null ise 0 kabul et

            if (totalMinutes > 0) {
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60

                // Saat ve dakika bilgisini birleştirerek TextView'a ata
                lenghtTextView.text = "${hours}s ${minutes}dk"
            } else {
                // Eğer süre bilgisi yoksa veya 0 ise TextView'ı gizle veya varsayılan metin göster
                lenghtTextView.text = "Süre bilinmiyor"
                // Veya: lenghtTextView.visibility = View.GONE
            }

            val rating = movie.voteAverage?.times(10)

            // Güvenli null kontrolü ve renk ataması
            val color = when {
                rating != null && rating < 40 -> Color.RED
                rating != null && rating < 70 -> Color.YELLOW
                else -> Color.GREEN
            }

            // `let` kullanarak rating null değilse UI güncellemesi yap
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

            // Orijinal Başlık
            originalTitleTextView.text = movie.originalTitle ?: "-"

            // Orijinal Dil
            originalLanguageTextView.text = formatLanguage(movie.originalLanguage)

            // Bütçe
            budgetTextView.text = formatCurrency(movie.budget)

            // Kazanç
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
        return when(code) {
            "en" -> "İngilizce"
            "tr" -> "Türkçe"
            "ja" -> "Japonca"
            "ko" -> "Korece"
            "it" -> "İtalyanca"
            else -> {code?.uppercase() ?: "-"}
        }
    }
}

