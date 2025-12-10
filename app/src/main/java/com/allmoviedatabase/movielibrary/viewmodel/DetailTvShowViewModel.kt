package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.Credits.CastMember
import com.allmoviedatabase.movielibrary.model.ListItem
import com.allmoviedatabase.movielibrary.model.TV.TvShowDetail
import com.allmoviedatabase.movielibrary.model.video.VideoResult // VideoResult import edildi
import com.allmoviedatabase.movielibrary.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class DetailTvShowViewModel @Inject constructor(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tvId = savedStateHandle.get<Int>("tvId") ?: 0

    private val _tvDetail = MutableLiveData<TvShowDetail>()
    val tvDetail: LiveData<TvShowDetail> = _tvDetail

    private val _cast = MutableLiveData<List<CastMember>>()
    val cast: LiveData<List<CastMember>> = _cast

    private val _recommendations = MutableLiveData<List<ListItem>>()
    val recommendations: LiveData<List<ListItem>> = _recommendations

    // YENİ: Video Listesi
    private val _videos = MutableLiveData<List<VideoResult>>()
    val videos: LiveData<List<VideoResult>> = _videos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val disposable = CompositeDisposable()

    init {
        if (tvId != 0) loadAllData()
    }

    private fun loadAllData() {
        _isLoading.value = true

        disposable.add(
            Single.zip(
                repository.fetchTvShowDetails(tvId, "tr-TR"),
                repository.fetchTvShowCredits(tvId, "tr-TR"),
                repository.fetchTvShowRecommendations(tvId, "tr-TR"),
                repository.fetchTvShowVideos(tvId) // 4. İstek: Videolar
            ) { detail, creditsResponse, recsResponse, videosResponse ->

                // DATA İŞLEME (MAPPING) BURADA YAPILIYOR
                // Böylece DataResult içine ham response değil, işlenmiş liste gidiyor.

                // 1. Cast Listesi
                val castList = creditsResponse.cast ?: emptyList()

                // 2. Öneriler Listesi (Dizileri ListItem'a çeviriyoruz)
                // 'it' hatası almamak için açık isim veriyoruz
                val recsList = recsResponse.results?.map { tvShow ->
                    ListItem.TvShowItem(tvShow)
                } ?: emptyList()

                // 3. Video Listesi (Sadece YouTube olanlar)
                val videoList = videosResponse.results?.filter { video ->
                    video.site == "YouTube"
                } ?: emptyList()

                // Paketlenmiş sonuç
                DataResult(detail, castList, recsList, videoList)
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
                    // Hata loglama veya gösterme işlemi buraya eklenebilir
                    error.printStackTrace()
                })
        )
    }

    // Bu sınıf sadece işlenmiş (hazır) verileri taşır
    private data class DataResult(
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