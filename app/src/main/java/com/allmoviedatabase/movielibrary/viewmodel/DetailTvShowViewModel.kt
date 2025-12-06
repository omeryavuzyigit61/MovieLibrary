package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.Credits.CastMember
import com.allmoviedatabase.movielibrary.model.ListItem
import com.allmoviedatabase.movielibrary.model.TV.TvShowDetail
import com.allmoviedatabase.movielibrary.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class DetailTvShowViewModel @Inject constructor(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Navigation Argument'tan gelen ID (nav_graph'ta argument name 'tvId' olmalı)
    private val tvId = savedStateHandle.get<Int>("tvId") ?: 0

    private val _tvDetail = MutableLiveData<TvShowDetail>()
    val tvDetail: LiveData<TvShowDetail> = _tvDetail

    private val _cast = MutableLiveData<List<CastMember>>()
    val cast: LiveData<List<CastMember>> = _cast

    private val _recommendations = MutableLiveData<List<ListItem>>()
    val recommendations: LiveData<List<ListItem>> = _recommendations

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val disposable = CompositeDisposable()

    init {
        if (tvId != 0) {
            loadAllData()
        }
    }

    private fun loadAllData() {
        _isLoading.value = true

        // 1. Dizi Detayları
        disposable.add(
            repository.fetchTvShowDetails(tvId, "tr-TR")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ detail ->
                    _tvDetail.value = detail
                    _isLoading.value = false
                }, {
                    // Hata yönetimi
                    _isLoading.value = false
                })
        )

        // 2. Oyuncular
        disposable.add(
            repository.fetchTvShowCredits(tvId, "tr-TR")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    _cast.value = response.cast
                }, { })
        )

        // 3. Öneriler
        disposable.add(
            repository.fetchTvShowRecommendations(tvId, "tr-TR")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    // Gelen TvShow listesini ListItem'a çeviriyoruz
                    val listItems = response.results?.map { ListItem.TvShowItem(it) } ?: emptyList()
                    _recommendations.value = listItems
                }, { })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}