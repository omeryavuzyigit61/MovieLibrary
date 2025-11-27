package com.allmoviedatabase.movielibrary.viewmodel

import androidx.fragment.app.add
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.Credits.CreditsResponse
import com.allmoviedatabase.movielibrary.model.Detail.MovieDetail
import com.allmoviedatabase.movielibrary.model.Movie
import com.allmoviedatabase.movielibrary.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import jakarta.inject.Inject

@HiltViewModel
class MovieDetailViewModel@Inject constructor(private val movieRepository: MovieRepository) : ViewModel() {
    // Gözlemlenecek LiveData'lar
    private val _movieDetail = MutableLiveData<MovieDetail>()
    val movieDetail: LiveData<MovieDetail> = _movieDetail

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // RxJava aboneliklerini yönetmek için
    private val compositeDisposable = CompositeDisposable()

    // Bu fonksiyon Fragment tarafından çağrılacak
    fun loadMovieDetails(movieId: Int) {
        _isLoading.value = true
        compositeDisposable.add(
            movieRepository.fetchMovieDetails(movieId, "tr-TR") // Dili buradan belirleyebilirsiniz
                .subscribeOn(Schedulers.io()) // Ağ işlemini arka planda yap
                .observeOn(AndroidSchedulers.mainThread()) // Sonucu ana thread'de dinle
                .subscribe({ movie ->
                    // Başarılı olursa
                    _movieDetail.value = movie
                    _isLoading.value = false
                }, { throwable ->
                    // Hata olursa
                    _error.value = throwable.localizedMessage
                    _isLoading.value = false
                })
        )
    }

    private val _movieCredits = MutableLiveData<CreditsResponse>()
    val movieCredits: LiveData<CreditsResponse> = _movieCredits

    fun loadMovieCredits(movieId: Int) {
        compositeDisposable.add(
            movieRepository.fetchMovieCredits(movieId, "tr-TR") // Repository'e bu fonksiyonu ekleyeceğiz
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ credits ->
                    _movieCredits.value = credits
                }, { throwable ->
                    _error.value = "Oyuncu listesi yüklenemedi: ${throwable.localizedMessage}"
                })
        )
    }

    private val _movieRecommendations = MutableLiveData<List<Movie>>()
    val movieRecommendations: LiveData<List<Movie>> = _movieRecommendations

    fun loadMovieRecommendations(movieId: Int) {
        compositeDisposable.add(
            movieRepository.fetchMovieRecommendations(movieId, "tr-TR")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    _movieRecommendations.value = response.results ?: emptyList()
                }, { throwable ->
                    _error.value = "Öneriler yüklenemedi: ${throwable.localizedMessage}"
                })
        )
    }

    private val _ageRating = MutableLiveData<String?>()
    val ageRating: LiveData<String?> = _ageRating

    fun loadMovieReleaseDates(movieId: Int) {
        compositeDisposable.add(
            movieRepository.fetchMovieReleaseDates(movieId)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    // Yanıttan Türkiye'nin yaş sınırlandırmasını bul
                    val turkeyReleaseInfo = response.results?.find { it.countryCode == "TR" }

                    // Türkiye için olan sertifikalardan boş olmayan ilkini al
                    val rating = turkeyReleaseInfo?.releaseDates
                        ?.mapNotNull { it.certification }
                        ?.firstOrNull { it.isNotBlank() }

                    _ageRating.value = rating
                }, { throwable ->
                    // Hata olursa loglayabiliriz ama UI'da göstermeye gerek yok
                    // çünkü bu kritik bir bilgi değil.
                    _ageRating.value = null
                })
        )
    }

    // ViewModel temizlendiğinde (yok olduğunda) abonelikleri iptal et
    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}
