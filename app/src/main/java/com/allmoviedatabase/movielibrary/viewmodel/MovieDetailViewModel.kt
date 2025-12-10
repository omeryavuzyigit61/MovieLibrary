package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.Credits.CreditsResponse
import com.allmoviedatabase.movielibrary.model.Detail.ExternalIds
import com.allmoviedatabase.movielibrary.model.Detail.MovieDetail
import com.allmoviedatabase.movielibrary.model.Movie
import com.allmoviedatabase.movielibrary.model.video.VideoResponse
import com.allmoviedatabase.movielibrary.model.video.VideoResult // VideoResult modelini import ettiğinden emin ol
import com.allmoviedatabase.movielibrary.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId = savedStateHandle.get<Int>("movieId") ?: 0

    private val _movieDetail = MutableLiveData<MovieDetail>()
    val movieDetail: LiveData<MovieDetail> = _movieDetail

    private val _movieCredits = MutableLiveData<CreditsResponse>()
    val movieCredits: LiveData<CreditsResponse> = _movieCredits

    private val _movieRecommendations = MutableLiveData<List<Movie>>()
    val movieRecommendations: LiveData<List<Movie>> = _movieRecommendations

    private val _ageRating = MutableLiveData<String?>()
    val ageRating: LiveData<String?> = _ageRating

    // YENİ: Video Listesi
    private val _videos = MutableLiveData<List<VideoResult>>()
    val videos: LiveData<List<VideoResult>> = _videos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    private val _externalIds = MutableLiveData<ExternalIds?>()
    val externalIds: LiveData<ExternalIds?> = _externalIds


    private val disposable = CompositeDisposable()

    init {
        if (movieId != 0) loadAllData()
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
                    detail,
                    credits,
                    recs.results ?: emptyList(),
                    releaseDates,
                    videos,
                    externals
                )
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    _movieDetail.value = result.detail
                    _movieCredits.value = result.credits
                    _movieRecommendations.value = result.recommendations

                    // Yaş Sınırı
                    val turkeyInfo =
                        result.releaseDatesResponse.results?.find { it.countryCode == "TR" }
                    val rating =
                        turkeyInfo?.releaseDates?.firstOrNull { !it.certification.isNullOrEmpty() }?.certification
                    _ageRating.value = rating

                    // YENİ: Video Listesini Atama
                    // Sadece YouTube videolarını filtreleyebiliriz
                    val videoList =
                        result.videoResponse.results?.filter { it.site == "YouTube" } ?: emptyList()
                    _videos.value = videoList

                    _externalIds.value = result.externalIds

                    _isLoading.value = false
                }, { t ->
                    _error.value = t.localizedMessage
                    _isLoading.value = false
                })
        )
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