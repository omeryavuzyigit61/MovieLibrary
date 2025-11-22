package com.allmoviedatabase.movielibrary.view.fragment

import android.graphics.Color
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.navArgs
import com.allmoviedatabase.movielibrary.databinding.FragmentDetailMovieBinding
import com.allmoviedatabase.movielibrary.model.Detail.MovieDetail
import com.allmoviedatabase.movielibrary.viewmodel.MovieDetailViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.hilt.android.AndroidEntryPoint
import kotlin.getValue


@AndroidEntryPoint
class DetailMovieFragment : Fragment() {

    private var _binding: FragmentDetailMovieBinding? = null
    private val binding get() = _binding!!

    private val args: DetailMovieFragmentArgs by navArgs()
    private val viewModel: MovieDetailViewModel by viewModels()


    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentDetailMovieBinding.inflate(inflater, container, false)
        val movieId = args.movieId
        viewModel.loadMovieDetails(movieId)
        setupObservers()
        setupUI()
        return binding.root
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

        }

    }

}
