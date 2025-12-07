package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.Credits.CreditsResponse
import com.allmoviedatabase.movielibrary.model.Detail.MovieDetail
import com.allmoviedatabase.movielibrary.model.Movie
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

    // ID'yi navArgs'tan otomatik alıyoruz, böylece Fragment'ta manuel çağırmaya gerek kalmıyor
    private val movieId = savedStateHandle.get<Int>("movieId") ?: 0

    private val _movieDetail = MutableLiveData<MovieDetail>()
    val movieDetail: LiveData<MovieDetail> = _movieDetail

    private val _movieCredits = MutableLiveData<CreditsResponse>()
    val movieCredits: LiveData<CreditsResponse> = _movieCredits

    private val _movieRecommendations = MutableLiveData<List<Movie>>()
    val movieRecommendations: LiveData<List<Movie>> = _movieRecommendations

    private val _ageRating = MutableLiveData<String?>()
    val ageRating: LiveData<String?> = _ageRating

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val disposable = CompositeDisposable()

    init {
        if (movieId != 0) loadAllData()
    }

    private fun loadAllData() {
        _isLoading.value = true

        // 4 farklı API isteğini birleştiriyoruz
        disposable.add(
            Single.zip(
                repository.fetchMovieDetails(movieId, "tr-TR"),
                repository.fetchMovieCredits(movieId, "tr-TR"),
                repository.fetchMovieRecommendations(movieId, "tr-TR"),
                repository.fetchMovieReleaseDates(movieId)
            ) { detail, credits, recs, releaseDates ->
                // Verileri paketle
                DataResult(detail, credits, recs.results ?: emptyList(), releaseDates)
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    _movieDetail.value = result.detail
                    _movieCredits.value = result.credits
                    _movieRecommendations.value = result.recommendations

                    // Yaş sınırı mantığı
                    val turkeyRating = result.releaseDatesResponse.results
                        ?.find { it.countryCode == "TR" }
                        ?.releaseDates?.firstOrNull { !it.certification.isNullOrBlank() }
                        ?.certification
                    _ageRating.value = turkeyRating

                    _isLoading.value = false
                }, { t ->
                    _error.value = t.localizedMessage
                    _isLoading.value = false
                })
        )
    }

    // Zip işlemi için geçici data class
    private data class DataResult(
        val detail: MovieDetail,
        val credits: CreditsResponse,
        val recommendations: List<Movie>,
        val releaseDatesResponse: com.allmoviedatabase.movielibrary.model.Adult.ReleaseDatesResponse
        // Not: ReleaseDateResponse modelinizin tam yolunu veya adını kontrol edin
    )

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}