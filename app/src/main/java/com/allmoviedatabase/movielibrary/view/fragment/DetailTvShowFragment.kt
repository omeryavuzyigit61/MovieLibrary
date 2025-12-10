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
import com.allmoviedatabase.movielibrary.adapter.ContentAdapter
import com.allmoviedatabase.movielibrary.adapter.SeasonsAdapter
import com.allmoviedatabase.movielibrary.adapter.VideoAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentDetailTvShowBinding
import com.allmoviedatabase.movielibrary.model.ListItem
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL
import com.allmoviedatabase.movielibrary.util.openYoutubeVideo // Extension
import com.allmoviedatabase.movielibrary.util.showDescriptionDialog // Extension
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
    private lateinit var videoAdapter: VideoAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        sharedElementEnterTransition = TransitionInflater.from(context).inflateTransition(android.R.transition.move)
    }

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentDetailTvShowBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.posterImageView.transitionName = "tv_${args.tvId}"
        setupUI()
        setupObservers()
    }

    private fun setupUI() {
        // Cast
        castAdapter = CastAdapter(
            isTvShow = true,
            onCastMemberClicked = { personId ->
                val action = DetailTvShowFragmentDirections.actionDetailTvShowFragmentToPersonDetailFragment(personId)
                findNavController().navigate(action)
            },
            onShowMoreClicked = {
                val action = DetailTvShowFragmentDirections.actionDetailTvShowFragmentToFullCastFragment(args.tvId, "tv")
                findNavController().navigate(action)
            }
        )
        binding.castRecyclerView.apply {
            adapter = castAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // Video (Extension)
        videoAdapter = VideoAdapter { videoKey ->
            requireContext().openYoutubeVideo(videoKey)
        }
        binding.videosRecyclerView.apply {
            adapter = videoAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // Sezonlar
        seasonsAdapter = SeasonsAdapter { seasonNumber ->
            val action = DetailTvShowFragmentDirections.actionDetailTvShowFragmentToSeasonDetailFragment(args.tvId, seasonNumber)
            findNavController().navigate(action)
        }
        binding.seasonsRecyclerView.apply {
            adapter = seasonsAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
        }

        // Öneriler
        recommendationsAdapter = ContentAdapter(isHorizontal = true) { listItem, imageView ->
            if (listItem is ListItem.TvShowItem) {
                listItem.tvShow.id?.let { id ->
                    // Animasyon için (İsteğe bağlı, burada ImageView yoksa boş geçebilirsin)
                    // val extras = FragmentNavigatorExtras(imageView to "tv_$id")
                    val action = DetailTvShowFragmentDirections.actionDetailTvShowFragmentSelf(id)
                    findNavController().navigate(action)
                }
            }
        }
        binding.recommendationsRecyclerView.apply {
            adapter = recommendationsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupObservers() {
        viewModel.cast.observe(viewLifecycleOwner) { castList -> castAdapter.submitList(castList) }

        viewModel.videos.observe(viewLifecycleOwner) { videoList ->
            val hasVideo = !videoList.isNullOrEmpty()
            binding.videosHeaderTextView.visibility = if (hasVideo) View.VISIBLE else View.GONE
            binding.videosRecyclerView.visibility = if (hasVideo) View.VISIBLE else View.GONE
            if (hasVideo) videoAdapter.submitList(videoList)
        }

        viewModel.tvDetail.observe(viewLifecycleOwner) { tvShow ->
            binding.apply {
                titleTextView.text = tvShow.name
                val year = tvShow.firstAirDate?.take(4) ?: ""
                shortDateTextView.text = "($year)"
                dateTextView.text = tvShow.firstAirDate
                episodeCountTextView.text = "${tvShow.numberOfSeasons ?: 0} Sezon, ${tvShow.numberOfEpisodes ?: 0} Bölüm"
                genreTextView.text = tvShow.genres?.joinToString(", ") { it.name.toString() } ?: "-"

                tagLineTextView.visibility = if (!tvShow.tagline.isNullOrEmpty()) View.VISIBLE else View.GONE
                tagLineTextView.text = tvShow.tagline

                descriptionTextView.text = tvShow.overview ?: "Açıklama bulunamadı."

                // Dialog Extension Kullanımı:
                if (!tvShow.overview.isNullOrEmpty()) {
                    readMoreHint.visibility = View.VISIBLE
                    val clickListener = View.OnClickListener { requireContext().showDescriptionDialog(tvShow.overview) }
                    descriptionTextView.setOnClickListener(clickListener)
                    readMoreHint.setOnClickListener(clickListener)
                } else {
                    readMoreHint.visibility = View.GONE
                }

                val rating = tvShow.voteAverage?.times(10)
                val color = when {
                    rating != null && rating < 40 -> Color.RED
                    rating != null && rating < 70 -> Color.YELLOW
                    else -> Color.GREEN
                }
                rating?.let {
                    ratingTextView.text = "${it.roundToInt()}%"
                    ratingProgressIndicator.setProgress(it.toInt(), true)
                    ratingProgressIndicator.setIndicatorColor(color)
                }

                Glide.with(requireContext())
                    .load(IMAGE_BASE_URL + tvShow.posterPath)
                    .diskCacheStrategy(DiskCacheStrategy.ALL)
                    .into(posterImageView)

                ageRatingTextView.text = "13+"
                seasonsAdapter.submitList(tvShow.seasons)
            }
        }

        viewModel.recommendations.observe(viewLifecycleOwner) { list ->
            binding.recommendationsLabel.visibility = if (list.isNullOrEmpty()) View.GONE else View.VISIBLE
            binding.recommendationsRecyclerView.visibility = if (list.isNullOrEmpty()) View.GONE else View.VISIBLE
            recommendationsAdapter.submitList(list)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}