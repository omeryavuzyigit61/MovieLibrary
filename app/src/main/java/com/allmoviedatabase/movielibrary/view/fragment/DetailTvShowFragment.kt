package com.allmoviedatabase.movielibrary.view.fragment

import android.graphics.Color
import android.os.Bundle
import android.transition.TransitionInflater
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.R // R dosyanı kontrol et
import com.allmoviedatabase.movielibrary.adapter.CastAdapter
import com.allmoviedatabase.movielibrary.adapter.CommentsAdapter
import com.allmoviedatabase.movielibrary.adapter.ContentAdapter
import com.allmoviedatabase.movielibrary.adapter.SeasonsAdapter
import com.allmoviedatabase.movielibrary.adapter.UserListSelectionAdapter // YENİ ADAPTER IMPORT
import com.allmoviedatabase.movielibrary.adapter.VideoAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentDetailTvShowBinding
import com.allmoviedatabase.movielibrary.model.ListItem
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL
import com.allmoviedatabase.movielibrary.util.openFacebook
import com.allmoviedatabase.movielibrary.util.openImdb
import com.allmoviedatabase.movielibrary.util.openInstagram
import com.allmoviedatabase.movielibrary.util.openTwitter
import com.allmoviedatabase.movielibrary.util.openYoutubeVideo
import com.allmoviedatabase.movielibrary.util.showDescriptionDialog
import com.allmoviedatabase.movielibrary.viewmodel.DetailTvShowViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog // YENİ IMPORT
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
    private lateinit var commentsAdapter: CommentsAdapter

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

        // --- BUTON TIKLAMA OLAYLARI ---
        binding.cbLike.setOnClickListener { viewModel.toggleLike() }
        binding.cbWatchList.setOnClickListener { viewModel.toggleWatchlist() }

        // YENİ: Listeye Ekle Butonu
        binding.btnAddToList.setOnClickListener {
            viewModel.fetchUserLists() // Listeleri çek
            showAddToListBottomSheet() // Pencereyi aç
        }

        binding.btnPostComment.setOnClickListener {
            val content = binding.etComment.text.toString()
            val isSpoiler = binding.cbSpoiler.isChecked
            viewModel.sendComment(content, isSpoiler)
        }
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

        // Video
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
                    val action = DetailTvShowFragmentDirections.actionDetailTvShowFragmentSelf(id)
                    findNavController().navigate(action)
                }
            }
        }
        binding.recommendationsRecyclerView.apply {
            adapter = recommendationsAdapter
            layoutManager = LinearLayoutManager(context, LinearLayoutManager.HORIZONTAL, false)
        }

        // Yorumlar
        commentsAdapter = CommentsAdapter()
        binding.rvComments.apply {
            adapter = commentsAdapter
            layoutManager = LinearLayoutManager(context)
            isNestedScrollingEnabled = false
            setOnTouchListener { v, event ->
                v.parent.requestDisallowInterceptTouchEvent(true)
                v.onTouchEvent(event)
                true
            }
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

        viewModel.isLiked.observe(viewLifecycleOwner) { isLiked ->
            binding.cbLike.setOnCheckedChangeListener(null)
            binding.cbLike.isChecked = isLiked
        }

        viewModel.isWatchlisted.observe(viewLifecycleOwner) { isSaved ->
            binding.cbWatchList.setOnCheckedChangeListener(null)
            binding.cbWatchList.isChecked = isSaved
        }

        viewModel.totalLikes.observe(viewLifecycleOwner) { count ->
            binding.tvLikeCount.text = "$count Beğeni"
        }

        viewModel.error.observe(viewLifecycleOwner) { errMsg ->
            if (errMsg.isNotEmpty()) Toast.makeText(requireContext(), errMsg, Toast.LENGTH_SHORT).show()
        }

        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            commentsAdapter.submitList(comments)
        }

        viewModel.commentPostStatus.observe(viewLifecycleOwner) { status ->
            if (status.startsWith("SUCCESS") || status.startsWith("TEBRİKLER")) {
                val message = if(status == "SUCCESS") "Yorumunuz gönderildi." else status
                Toast.makeText(context, message, Toast.LENGTH_LONG).show()
                binding.etComment.text?.clear()
                binding.cbSpoiler.isChecked = false
            } else if (status.isNotEmpty()) {
                Toast.makeText(context, status, Toast.LENGTH_SHORT).show()
            }
        }

        // YENİ: Listeye Ekleme Durumu Bildirimi
        viewModel.addToListStatus.observe(viewLifecycleOwner) { message ->
            if (!message.isNullOrEmpty()) {
                Toast.makeText(context, message, Toast.LENGTH_SHORT).show()
            }
        }
    }

    // YENİ: Bottom Sheet Açma
    private fun showAddToListBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_select_list, null)
        dialog.setContentView(sheetView)

        val rvLists = sheetView.findViewById<RecyclerView>(R.id.rvUserLists)
        val progressBar = sheetView.findViewById<ProgressBar>(R.id.progressBarLists)
        val tvNoList = sheetView.findViewById<TextView>(R.id.tvNoListWarning)

        // Adapter Kurulumu
        val listAdapter = UserListSelectionAdapter { selectedList ->
            // Bir listeye tıklandığında:
            val currentTvShow = viewModel.tvDetail.value
            if (currentTvShow != null) {
                // ViewModel'deki ekleme fonksiyonunu çağır (TV Show için)
                viewModel.addTvShowToCustomList(selectedList.listId, currentTvShow)
                dialog.dismiss()
            } else {
                Toast.makeText(context, "Dizi bilgisi henüz yüklenmedi.", Toast.LENGTH_SHORT).show()
            }
        }

        rvLists.layoutManager = LinearLayoutManager(requireContext())
        rvLists.adapter = listAdapter

        // Listeleri Gözlemle
        progressBar.visibility = View.VISIBLE
        viewModel.userLists.observe(viewLifecycleOwner) { lists ->
            progressBar.visibility = View.GONE
            if (lists.isNullOrEmpty()) {
                tvNoList.visibility = View.VISIBLE
                rvLists.visibility = View.GONE
            } else {
                tvNoList.visibility = View.GONE
                rvLists.visibility = View.VISIBLE
                listAdapter.submitList(lists)
            }
        }

        dialog.show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}