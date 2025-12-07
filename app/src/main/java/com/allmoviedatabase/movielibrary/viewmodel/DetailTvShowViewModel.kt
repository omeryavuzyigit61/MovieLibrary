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

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val disposable = CompositeDisposable()

    init {
        if (tvId != 0) loadAllData()
    }

    private fun loadAllData() {
        _isLoading.value = true

        // 3 isteği aynı anda (paralel) başlatıp sonuçları birleştiriyoruz
        disposable.add(
            Single.zip(
                repository.fetchTvShowDetails(tvId, "tr-TR"),
                repository.fetchTvShowCredits(tvId, "tr-TR"),
                repository.fetchTvShowRecommendations(tvId, "tr-TR")
            ) { detail, credits, recs ->
                // Sonuçları bir Triple (üçlü) nesne olarak paketle
                Triple(detail, credits, recs)
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (detail, creditsResponse, recsResponse) ->
                    // Tüm veriler başarıyla geldi
                    _tvDetail.value = detail
                    _cast.value = creditsResponse.cast ?: emptyList()
                    _recommendations.value = recsResponse.results?.map { ListItem.TvShowItem(it) } ?: emptyList()
                    _isLoading.value = false
                }, { error ->
                    // Herhangi birinde hata olursa buraya düşer
                    _isLoading.value = false
                    // Hata yönetimi (Opsiyonel: _error.value = error.message)
                })
        )
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}