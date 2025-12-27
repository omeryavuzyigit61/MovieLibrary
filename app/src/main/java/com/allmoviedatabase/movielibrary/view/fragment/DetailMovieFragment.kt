package com.allmoviedatabase.movielibrary.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionInflater
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
import com.allmoviedatabase.movielibrary.adapter.VideoAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentDetailMovieBinding
import com.allmoviedatabase.movielibrary.model.Detail.MovieDetail
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL
import com.allmoviedatabase.movielibrary.util.formatCurrency // Extension import
import com.allmoviedatabase.movielibrary.util.formatLanguage // Extension import
import com.allmoviedatabase.movielibrary.util.openFacebook
import com.allmoviedatabase.movielibrary.util.openImdb
import com.allmoviedatabase.movielibrary.util.openInstagram
import com.allmoviedatabase.movielibrary.util.openTwitter
import com.allmoviedatabase.movielibrary.util.openYoutubeVideo // Extension import
import com.allmoviedatabase.movielibrary.util.showDescriptionDialog // Extension import
import com.allmoviedatabase.movielibrary.viewmodel.MovieDetailViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import dagger.hilt.android.AndroidEntryPoint
import kotlin.math.roundToInt

@AndroidEntryPoint
class DetailMovieFragment : Fragment() {

    private var _binding: FragmentDetailMovieBinding? = null
    private val binding get() = _binding!!

    private val args: DetailMovieFragmentArgs by navArgs()
    private val viewModel: MovieDetailViewModel by viewModels()

    private lateinit var castAdapter: CastAdapter
    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var videoAdapter: VideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailMovieBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.abuzittinImageView.transitionName = "movie_${args.movieId}"
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        // Cast Adapter
        castAdapter = CastAdapter(
            isTvShow = false,
            onCastMemberClicked = { personId ->
                val action = DetailMovieFragmentDirections.actionDetailMovieFragmentToPersonDetailFragment(personId)
                findNavController().navigate(action)
            },
            onShowMoreClicked = {
                val action = DetailMovieFragmentDirections.actionDetailMovieFragmentToFullCastFragment(args.movieId, "movie")
                findNavController().navigate(action)
            }
        )
        binding.castRecyclerView.apply {
            adapter = castAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // Video Adapter (Extension Kullanımı)
        videoAdapter = VideoAdapter { videoKey ->
            requireContext().openYoutubeVideo(videoKey)
        }
        binding.videosRecyclerView.apply {
            adapter = videoAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // Recommendation Adapter
        recommendationAdapter = RecommendationAdapter { movieId ->
            val action = DetailMovieFragmentDirections.actionDetailMovieFragmentSelf(movieId)
            findNavController().navigate(action)
        }
        binding.recommendationsRecyclerView.apply {
            adapter = recommendationAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupObservers() {
        viewModel.movieDetail.observe(viewLifecycleOwner) { movie -> bindMovieDetails(movie) }
        viewModel.movieCredits.observe(viewLifecycleOwner) { credits -> castAdapter.submitList(credits.cast) }
        viewModel.movieRecommendations.observe(viewLifecycleOwner) { movies -> recommendationAdapter.submitList(movies) }

        viewModel.ageRating.observe(viewLifecycleOwner) { rating ->
            binding.ageRatingTextView.visibility = if (!rating.isNullOrBlank()) View.VISIBLE else View.GONE
            binding.ageRatingTextView.text = rating
        }

        viewModel.videos.observe(viewLifecycleOwner) { videoList ->
            val hasVideo = !videoList.isNullOrEmpty()
            binding.videosHeaderTextView.visibility = if (hasVideo) View.VISIBLE else View.GONE
            binding.videosRecyclerView.visibility = if (hasVideo) View.VISIBLE else View.GONE
            if (hasVideo) videoAdapter.submitList(videoList)
        }

        viewModel.externalIds.observe(viewLifecycleOwner) { ids ->
            binding.apply {
                if (ids != null) {
                    // Facebook
                    if (!ids.facebookId.isNullOrEmpty()) {
                        btnFacebook.visibility = View.VISIBLE
                        btnFacebook.setOnClickListener { requireContext().openFacebook(ids.facebookId) }
                    } else btnFacebook.visibility = View.GONE

                    // Instagram
                    if (!ids.instagramId.isNullOrEmpty()) {
                        btnInstagram.visibility = View.VISIBLE
                        btnInstagram.setOnClickListener { requireContext().openInstagram(ids.instagramId) }
                    } else btnInstagram.visibility = View.GONE

                    // Twitter
                    if (!ids.twitterId.isNullOrEmpty()) {
                        btnTwitter.visibility = View.VISIBLE
                        btnTwitter.setOnClickListener { requireContext().openTwitter(ids.twitterId) }
                    } else btnTwitter.visibility = View.GONE

                    // IMDb
                    if (!ids.imdbId.isNullOrEmpty()) {
                        btnImdb.visibility = View.VISIBLE
                        btnImdb.setOnClickListener { requireContext().openImdb(ids.imdbId) }
                    } else btnImdb.visibility = View.GONE
                }
            }
        }
    }

    private fun bindMovieDetails(movie: MovieDetail) {
        binding.apply {

            btnWatchMovie.setOnClickListener {
                // Test için sabit bir URL gönderiyoruz (Big Buck Bunny)
                val dummyVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"

                // Safe Args ile PlayerFragment'a URL gönder
                val action = DetailMovieFragmentDirections.actionDetailMovieFragmentToPlayerFragment(dummyVideoUrl)
                findNavController().navigate(action)
            }

            Glide.with(this@DetailMovieFragment)
                .load("https://media.themoviedb.org/t/p/w220_and_h330_face" + movie.posterPath)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .centerCrop()
                .into(abuzittinImageView)

            val date = movie.releaseDate?.take(4) ?: ""
            val genreText = movie.genres?.joinToString(", ") { it.name ?: "" }

            // Süre Hesaplama
            val totalMinutes = movie.runtime ?: 0
            if (totalMinutes > 0) {
                val hours = totalMinutes / 60
                val minutes = totalMinutes % 60
                lenghtTextView.text = "${hours}s ${minutes}dk"
            } else {
                lenghtTextView.text = "Süre bilinmiyor"
            }

            // Puanlama
            val rating = movie.voteAverage?.times(10)
            val color = when {
                rating != null && rating < 40 -> Color.RED
                rating != null && rating < 70 -> Color.YELLOW
                else -> Color.GREEN
            }
            rating?.let {
                ratingTextView.text = "${it.toInt()}%"
                ratingProgressIndicator.setProgress(it.toInt(), true)
                ratingProgressIndicator.setIndicatorColor(color)
            }

            titleTextView.text = movie.title
            shortDateTextView.text = "($date)"
            dateTextView.text = movie.releaseDate
            genreTextView.text = genreText
            tagLineTextView.text = movie.tagline
            descriptionTextView.text = movie.overview
            originalTitleTextView.text = movie.originalTitle ?: "-"

            // Extension Kullanımları:
            originalLanguageTextView.text = movie.originalLanguage.formatLanguage()
            budgetTextView.text = movie.budget.formatCurrency()
            revenueTextView.text = movie.revenue.formatCurrency()

            // Dialog Extension Kullanımı:
            if (!movie.overview.isNullOrEmpty()) {
                readMoreHint.visibility = View.VISIBLE
                val clickListener = View.OnClickListener { requireContext().showDescriptionDialog(movie.overview) }
                descriptionTextView.setOnClickListener(clickListener)
                readMoreHint.setOnClickListener(clickListener)
            } else {
                readMoreHint.visibility = View.GONE
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}