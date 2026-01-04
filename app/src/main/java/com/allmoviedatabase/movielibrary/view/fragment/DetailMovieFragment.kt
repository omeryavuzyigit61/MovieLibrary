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
import com.allmoviedatabase.movielibrary.R // R dosyasının doğru import edildiğinden emin ol
import com.allmoviedatabase.movielibrary.adapter.CastAdapter
import com.allmoviedatabase.movielibrary.adapter.CommentsAdapter
import com.allmoviedatabase.movielibrary.adapter.RecommendationAdapter
import com.allmoviedatabase.movielibrary.adapter.UserListSelectionAdapter // Yeni Adapter
import com.allmoviedatabase.movielibrary.adapter.VideoAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentDetailMovieBinding
import com.allmoviedatabase.movielibrary.model.Detail.MovieDetail
import com.allmoviedatabase.movielibrary.util.* // Extension importlar
import com.allmoviedatabase.movielibrary.viewmodel.MovieDetailViewModel
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import com.google.android.material.bottomsheet.BottomSheetDialog // Bottom Sheet için gerekli
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class DetailMovieFragment : Fragment() {

    private var _binding: FragmentDetailMovieBinding? = null
    private val binding get() = _binding!!

    private val args: DetailMovieFragmentArgs by navArgs()
    private val viewModel: MovieDetailViewModel by viewModels()

    private lateinit var castAdapter: CastAdapter
    private lateinit var recommendationAdapter: RecommendationAdapter
    private lateinit var videoAdapter: VideoAdapter
    private lateinit var commentsAdapter: CommentsAdapter

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

        // Mevcut Butonlar
        binding.cbLike.setOnClickListener { viewModel.toggleLike() }
        binding.cbWatchList.setOnClickListener { viewModel.toggleWatchlist() }

        // YENİ: Listeye Ekle Butonu
        binding.btnAddToList.setOnClickListener {
            viewModel.fetchUserLists() // Listeleri veritabanından çek
            showAddToListBottomSheet() // Pencereyi aç
        }

        binding.btnPostComment.setOnClickListener {
            val content = binding.etComment.text.toString()
            val isSpoiler = binding.cbSpoiler.isChecked
            viewModel.sendComment(content, isSpoiler)
        }
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

        // Video Adapter
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

        // Comments Adapter
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
        // Film Detayları
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
                    if (!ids.facebookId.isNullOrEmpty()) {
                        btnFacebook.visibility = View.VISIBLE
                        btnFacebook.setOnClickListener { requireContext().openFacebook(ids.facebookId) }
                    } else btnFacebook.visibility = View.GONE

                    if (!ids.instagramId.isNullOrEmpty()) {
                        btnInstagram.visibility = View.VISIBLE
                        btnInstagram.setOnClickListener { requireContext().openInstagram(ids.instagramId) }
                    } else btnInstagram.visibility = View.GONE

                    if (!ids.twitterId.isNullOrEmpty()) {
                        btnTwitter.visibility = View.VISIBLE
                        btnTwitter.setOnClickListener { requireContext().openTwitter(ids.twitterId) }
                    } else btnTwitter.visibility = View.GONE

                    if (!ids.imdbId.isNullOrEmpty()) {
                        btnImdb.visibility = View.VISIBLE
                        btnImdb.setOnClickListener { requireContext().openImdb(ids.imdbId) }
                    } else btnImdb.visibility = View.GONE
                }
            }
        }

        viewModel.isLiked.observe(viewLifecycleOwner) { isLiked ->
            binding.cbLike.setOnCheckedChangeListener(null)
            binding.cbLike.isChecked = isLiked
            binding.cbLike.setOnClickListener { viewModel.toggleLike() }
        }

        viewModel.isWatchlisted.observe(viewLifecycleOwner) { isSaved ->
            binding.cbWatchList.setOnCheckedChangeListener(null)
            binding.cbWatchList.isChecked = isSaved
            binding.cbWatchList.setOnClickListener { viewModel.toggleWatchlist() }
        }

        viewModel.totalLikes.observe(viewLifecycleOwner) { count ->
            binding.tvLikeCount.text = "$count Beğeni"
        }

        viewModel.comments.observe(viewLifecycleOwner) { comments ->
            commentsAdapter.submitList(comments)
        }

        viewModel.commentPostStatus.observe(viewLifecycleOwner) { status ->
            if (status.startsWith("SUCCESS") || status.startsWith("TEBRİKLER")) {
                // Eğer status SUCCESS ise normal mesaj, TEBRİKLER ile başlıyorsa rozet mesajıdır
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
                // Mesajı tekrar göstermemek için null yapılabilir ama Toast zaten kaybolur.
            }
        }
    }

    // YENİ FONKSİYON: Bottom Sheet Açma ve Yönetme
    private fun showAddToListBottomSheet() {
        val dialog = BottomSheetDialog(requireContext())
        val sheetView = LayoutInflater.from(requireContext()).inflate(R.layout.bottom_sheet_select_list, null)
        dialog.setContentView(sheetView)

        val rvLists = sheetView.findViewById<RecyclerView>(R.id.rvUserLists)
        val progressBar = sheetView.findViewById<ProgressBar>(R.id.progressBarLists)
        val tvNoList = sheetView.findViewById<TextView>(R.id.tvNoListWarning)

        // Adapter Kurulumu
        val listAdapter = UserListSelectionAdapter { selectedList ->
            // Bir listeye tıklandığında çalışacak kod:
            val currentMovie = viewModel.movieDetail.value
            if (currentMovie != null) {
                // ViewModel'deki ekleme fonksiyonunu çağır
                viewModel.addMovieToCustomList(selectedList.listId, currentMovie)
                dialog.dismiss() // Pencereyi kapat
            } else {
                Toast.makeText(context, "Film bilgisi henüz yüklenmedi.", Toast.LENGTH_SHORT).show()
            }
        }

        rvLists.layoutManager = LinearLayoutManager(requireContext())
        rvLists.adapter = listAdapter

        // Listeleri Gözlemle (Dialog açıkken veriler gelirse güncelle)
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

    private fun bindMovieDetails(movie: MovieDetail) {
        binding.apply {

            btnWatchMovie.setOnClickListener {
                val dummyVideoUrl = "https://commondatastorage.googleapis.com/gtv-videos-bucket/sample/BigBuckBunny.mp4"
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

            originalLanguageTextView.text = movie.originalLanguage.formatLanguage()
            budgetTextView.text = movie.budget.formatCurrency()
            revenueTextView.text = movie.revenue.formatCurrency()

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