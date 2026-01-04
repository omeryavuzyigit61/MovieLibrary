package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.viewModelScope
import com.allmoviedatabase.movielibrary.model.Credits.CastMember
import com.allmoviedatabase.movielibrary.model.ListItem
import com.allmoviedatabase.movielibrary.model.TV.TvShowDetail
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
class DetailTvShowViewModel @Inject constructor(
    private val repository: MovieRepository,
    interactionRepository: UserInteractionRepository,
    savedStateHandle: SavedStateHandle
) : BaseDetailViewModel(interactionRepository) {

    private val tvId = savedStateHandle.get<Int>("tvId") ?: 0

    private val _tvDetail = MutableLiveData<TvShowDetail>()
    val tvDetail: LiveData<TvShowDetail> = _tvDetail

    private val _cast = MutableLiveData<List<CastMember>>()
    val cast: LiveData<List<CastMember>> = _cast

    private val _recommendations = MutableLiveData<List<ListItem>>()
    val recommendations: LiveData<List<ListItem>> = _recommendations

    private val _videos = MutableLiveData<List<VideoResult>>()
    val videos: LiveData<List<VideoResult>> = _videos

    // _totalLikes BURADAN SİLİNDİ (Base'den geliyor)

    private val disposable = CompositeDisposable()

    init {
        if (tvId != 0) {
            loadAllData()
            checkInteractions(tvId.toString())
            listenToComments(tvId)

            // EKSİK OLAN BUYDU:
            listenToGlobalLikes(tvId.toString())
        }
    }

    private fun loadAllData() {
        _isLoading.value = true

        disposable.add(
            Single.zip(
                repository.fetchTvShowDetails(tvId, "tr-TR"),
                repository.fetchTvShowCredits(tvId, "tr-TR"),
                repository.fetchTvShowRecommendations(tvId, "tr-TR"),
                repository.fetchTvShowVideos(tvId)
            ) { detail, creditsResponse, recsResponse, videosResponse ->

                val castList = creditsResponse.cast ?: emptyList()
                val recsList = recsResponse.results?.map { tvShow ->
                    ListItem.TvShowItem(tvShow)
                } ?: emptyList()

                val videoList = videosResponse.results?.filter { video ->
                    video.site == "YouTube"
                } ?: emptyList()

                TvDataResult(detail, castList, recsList, videoList)
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    _tvDetail.value = result.detail
                    _cast.value = result.cast
                    _recommendations.value = result.recommendations
                    _videos.value = result.videos
                    _isLoading.value = false
                }, { error ->
                    _isLoading.value = false
                    error.printStackTrace()
                })
        )
    }

    fun toggleLike() {
        val detail = _tvDetail.value ?: return
        val currentStatus = _isLiked.value ?: false
        val newStatus = !currentStatus

        val data = hashMapOf<String, Any>(
            "tvId" to tvId,
            "title" to (detail.name ?: ""),
            "originalTitle" to (detail.originalName ?: ""),
            "posterPath" to (detail.posterPath ?: ""),
            "voteAverage" to (detail.voteAverage ?: 0.0),
            "firstAirDate" to (detail.firstAirDate ?: ""),
            "mediaType" to "tv",
            "addedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        viewModelScope.launch {
            val result = interactionRepository.toggleInteraction("favorites", tvId.toString(), data, newStatus)

            result.onSuccess { isNowLiked ->
                _isLiked.value = isNowLiked

                // İSTATİSTİK GÜNCELLEME
                val genreIds = detail.genres?.mapNotNull { it.id } ?: emptyList()
                val userId = interactionRepository.currentUserId

                if (userId != null && genreIds.isNotEmpty()) {
                    // isNowLiked true ise puan artar, false ise puan düşer
                    interactionRepository.updateGenreStats(userId, genreIds, isAdding = isNowLiked)
                }
            }

            result.onFailure { _error.value = it.localizedMessage }
        }
    }

    fun toggleWatchlist() {
        val detail = _tvDetail.value ?: return
        val currentStatus = _isWatchlisted.value ?: false

        val data = hashMapOf<String, Any>(
            "tvId" to tvId,
            "title" to (detail.name ?: ""),
            "originalTitle" to (detail.originalName ?: ""),
            "posterPath" to (detail.posterPath ?: ""),
            "mediaType" to "tv",
            "voteAverage" to (detail.voteAverage ?: 0.0),
            "addedAt" to com.google.firebase.firestore.FieldValue.serverTimestamp()
        )

        viewModelScope.launch {
            val result = interactionRepository.toggleInteraction("watchlist", tvId.toString(), data, !currentStatus)
            result.onSuccess { _isWatchlisted.value = it }
            result.onFailure { _error.value = it.localizedMessage }
        }
    }

    fun addTvShowToCustomList(listId: String, detail: TvShowDetail) {
        val tvItem = hashMapOf<String, Any>(
            "id" to (detail.id ?: 0),
            "tvId" to (detail.id ?: 0),
            "title" to (detail.name ?: ""),
            "posterPath" to (detail.posterPath ?: ""),
            "releaseDate" to (detail.firstAirDate ?: ""),
            "voteAverage" to (detail.voteAverage ?: 0.0),
            "mediaType" to "tv",
            "addedAt" to com.google.firebase.Timestamp.now()
        )

        viewModelScope.launch {
            val result = interactionRepository.addItemToCustomList(listId, tvItem)
            result.onSuccess { _addToListStatus.value = "Dizi listeye eklendi" }
            result.onFailure { _addToListStatus.value = it.localizedMessage }
        }
    }

    fun sendComment(content: String, isSpoiler: Boolean) {
        val genreIds = _tvDetail.value?.genres?.mapNotNull { it.id } ?: emptyList()

        viewModelScope.launch {
            _isLoading.value = true
            val result = interactionRepository.sendComment(tvId.toString(), "tv", content, isSpoiler, genreIds)
            _isLoading.value = false

            result.onSuccess { _commentPostStatus.value = it }
            result.onFailure { _commentPostStatus.value = it.localizedMessage }
        }
    }

    private data class TvDataResult(
        val detail: TvShowDetail,
        val cast: List<CastMember>,
        val recommendations: List<ListItem>,
        val videos: List<VideoResult>
    )

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}