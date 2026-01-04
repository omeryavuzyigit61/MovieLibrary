package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.allmoviedatabase.movielibrary.model.Credits.CreditsResponse
import com.allmoviedatabase.movielibrary.model.Detail.ExternalIds
import com.allmoviedatabase.movielibrary.model.Detail.MovieDetail
import com.allmoviedatabase.movielibrary.model.Movie
import com.allmoviedatabase.movielibrary.model.video.VideoResponse
import com.allmoviedatabase.movielibrary.model.video.VideoResult
import com.allmoviedatabase.movielibrary.repository.MovieRepository
import com.allmoviedatabase.movielibrary.repository.UserInteractionRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepository,
    interactionRepository: UserInteractionRepository,
    savedStateHandle: SavedStateHandle
) : BaseDetailViewModel(interactionRepository) {

    private val movieId = savedStateHandle.get<Int>("movieId") ?: 0

    private val _movieDetail = MutableLiveData<MovieDetail>()
    val movieDetail: LiveData<MovieDetail> = _movieDetail

    private val _movieCredits = MutableLiveData<CreditsResponse>()
    val movieCredits: LiveData<CreditsResponse> = _movieCredits

    private val _movieRecommendations = MutableLiveData<List<Movie>>()
    val movieRecommendations: LiveData<List<Movie>> = _movieRecommendations

    private val _ageRating = MutableLiveData<String?>()
    val ageRating: LiveData<String?> = _ageRating

    private val _videos = MutableLiveData<List<VideoResult>>()
    val videos: LiveData<List<VideoResult>> = _videos

    private val _externalIds = MutableLiveData<ExternalIds?>()
    val externalIds: LiveData<ExternalIds?> = _externalIds

    // _totalLikes BURADAN SİLİNDİ (Base'den geliyor)

    private val disposable = CompositeDisposable()

    init {
        if (movieId != 0) {
            loadAllData()
            checkInteractions(movieId.toString())
            listenToComments(movieId)

            // EKSİK OLAN BUYDU:
            listenToGlobalLikes(movieId.toString())
        }
    }

    private fun loadAllData() {
        _isLoading.value = true

        disposable.add(
            Single.zip(
                repository.fetchMovieDetails(movieId, "tr-TR"),
                repository.fetchMovieCredits(movieId, "tr-TR"),
                repository.fetchMovieRecommendations(movieId, "tr-TR"),
                repository.fetchMovieReleaseDates(movieId),
                repository.fetchMovieVideos(movieId),
                repository.fetchMovieExternalIds(movieId)
            ) { detail, credits, recs, releaseDates, videos, externals ->
                DataResult(
                    detail, credits, recs.results ?: emptyList(), releaseDates, videos, externals
                )
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    _movieDetail.value = result.detail
                    _movieCredits.value = result.credits
                    _movieRecommendations.value = result.recommendations

                    val turkeyInfo = result.releaseDatesResponse.results?.find { it.countryCode == "TR" }
                    val rating = turkeyInfo?.releaseDates?.firstOrNull { !it.certification.isNullOrEmpty() }?.certification
                    _ageRating.value = rating

                    val videoList = result.videoResponse.results?.filter { it.site == "YouTube" } ?: emptyList()
                    _videos.value = videoList
                    _externalIds.value = result.externalIds
                    _isLoading.value = false
                }, { t ->
                    _error.value = t.localizedMessage
                    _isLoading.value = false
                })
        )
    }

    fun toggleLike() {
        val detail = _movieDetail.value ?: return
        val currentStatus = _isLiked.value ?: false // Şu anki durum (Beğenilmiş mi?)

        // Yeni durum (Tıkladıktan sonra ne olacak?)
        // Eğer şu an beğenilmişse (true), yeni durum false (beğenmeme) olacak.
        val newStatus = !currentStatus

        val data: HashMap<String, Any> = hashMapOf(
            "movieId" to movieId,
            "title" to (detail.title ?: ""),
            "originalTitle" to (detail.originalTitle ?: ""),
            "posterPath" to (detail.posterPath ?: ""),
            "voteAverage" to (detail.voteAverage ?: 0.0),
            "releaseDate" to (detail.releaseDate ?: ""),
            "mediaType" to "movie",
            "addedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        viewModelScope.launch {
            // 1. Önce veritabanı işlemini yap
            val result = interactionRepository.toggleInteraction("favorites", movieId.toString(), data, newStatus)

            result.onSuccess { isNowLiked ->
                _isLiked.value = isNowLiked

                // 2. İSTATİSTİK GÜNCELLEME (Bug Fix Burası)
                // Filmin türlerini al
                val genreIds = detail.genres?.mapNotNull { it.id } ?: emptyList()

                // Eğer isNowLiked TRUE ise ekliyoruz (+1), FALSE ise çıkarıyoruz (-1)
                val userId = interactionRepository.currentUserId
                if (userId != null && genreIds.isNotEmpty()) {
                    interactionRepository.updateGenreStats(userId, genreIds, isAdding = isNowLiked)
                }
            }

            result.onFailure { _error.value = it.localizedMessage }
        }
    }

    fun toggleWatchlist() {
        val detail = _movieDetail.value ?: return
        val currentStatus = _isWatchlisted.value ?: false

        val data = hashMapOf<String, Any>(
            "movieId" to movieId,
            "title" to (detail.title ?: ""),
            "originalTitle" to (detail.originalTitle ?: ""),
            "posterPath" to (detail.posterPath ?: ""),
            "mediaType" to "movie",
            "voteAverage" to (detail.voteAverage ?: 0.0),
            "addedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        viewModelScope.launch {
            val result = interactionRepository.toggleInteraction("watchlist", movieId.toString(), data, !currentStatus)
            result.onSuccess { _isWatchlisted.value = it }
            result.onFailure { _error.value = it.localizedMessage }
        }
    }

    fun addMovieToCustomList(listId: String, movie: MovieDetail) {
        val itemData = hashMapOf<String, Any>(
            "id" to (movie.id ?: 0),
            "movieId" to (movie.id ?: 0),
            "title" to (movie.title ?: ""),
            "posterPath" to (movie.posterPath ?: ""),
            "releaseDate" to (movie.releaseDate ?: ""),
            "voteAverage" to (movie.voteAverage ?: 0.0),
            "mediaType" to "movie",
            "addedAt" to com.google.firebase.Timestamp.now()
        )

        viewModelScope.launch {
            val result = interactionRepository.addItemToCustomList(listId, itemData)
            result.onSuccess { _addToListStatus.value = "Film listeye eklendi" }
            result.onFailure { _addToListStatus.value = it.localizedMessage }
        }
    }

    fun sendComment(content: String, isSpoiler: Boolean) {
        val genreIds = _movieDetail.value?.genres?.mapNotNull { it.id } ?: emptyList()
        viewModelScope.launch {
            _isLoading.value = true
            val result = interactionRepository.sendComment(movieId.toString(), "movie", content, isSpoiler, genreIds)
            _isLoading.value = false
            result.onSuccess { _commentPostStatus.value = it }
            result.onFailure { _commentPostStatus.value = it.localizedMessage }
        }
    }

    private data class DataResult(
        val detail: MovieDetail,
        val credits: CreditsResponse,
        val recommendations: List<Movie>,
        val releaseDatesResponse: com.allmoviedatabase.movielibrary.model.Adult.ReleaseDatesResponse,
        val videoResponse: VideoResponse,
        val externalIds: ExternalIds
    )

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}