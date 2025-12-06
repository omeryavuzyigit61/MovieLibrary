package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.Credits.CastMember
import com.allmoviedatabase.movielibrary.model.Credits.CrewMember
import com.allmoviedatabase.movielibrary.model.Credits.CreditsResponse
import com.allmoviedatabase.movielibrary.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import jakarta.inject.Inject

@HiltViewModel
class FullCastViewModel @Inject constructor(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle // Navigasyondan gelen argümanları otomatik alır
) : ViewModel() {

    // Navigasyondan gelen verileri alıyoruz
    // Not: NavGraph'ta ID parametresinin adı "movieId" olduğu için hem dizi hem film ID'si burada
    private val id = savedStateHandle.get<Int>("movieId") ?: 0

    // "mediaType" verisini alıyoruz. Eğer gelmezse varsayılan "movie" olsun.
    private val mediaType = savedStateHandle.get<String>("mediaType") ?: "movie"

    private val _cast = MutableLiveData<List<CastMember>>()
    val cast: LiveData<List<CastMember>> = _cast

    private val _groupedCrew = MutableLiveData<List<Any>>()
    val groupedCrew: LiveData<List<Any>> = _groupedCrew

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val compositeDisposable = CompositeDisposable()

    init {
        // ViewModel oluşur oluşmaz yüklemeyi başlat
        loadCredits()
    }

    private fun loadCredits() {
        _isLoading.value = true

        // Hangi servisi çağıracağımıza karar veriyoruz
        val apiCall: Single<CreditsResponse> = if (mediaType == "tv") {
            // DİZİ İSE:
            // Repository'de bu fonksiyonun adı fetchTvShowCredits veya getTvShowCredits olabilir.
            // Kendi Repository'indeki isme göre burayı düzeltmelisin!
            repository.fetchTvShowCredits(id, "tr-TR")
        } else {
            // FİLM İSE:
            repository.fetchMovieCredits(id, "tr-TR")
        }

        compositeDisposable.add(
            apiCall
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ creditsResponse ->
                    _cast.value = creditsResponse.cast ?: emptyList()
                    processCrewList(creditsResponse.crew ?: emptyList())
                    _isLoading.value = false
                }, { throwable ->
                    _error.value = "Kadro bilgileri yüklenemedi: ${throwable.localizedMessage}"
                    _isLoading.value = false
                    throwable.printStackTrace() // Logcat'te hatayı görmek için
                })
        )
    }

    private fun processCrewList(crewList: List<CrewMember>) {
        val groupedMap = crewList.groupBy { it.department }

        val finalList = mutableListOf<Any>()
        groupedMap.forEach { (department, members) ->
            department?.let {
                finalList.add(it)
                finalList.addAll(members)
            }
        }
        _groupedCrew.value = finalList
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}