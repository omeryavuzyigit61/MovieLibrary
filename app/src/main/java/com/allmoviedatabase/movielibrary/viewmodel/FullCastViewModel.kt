package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.Credits.CastMember
import com.allmoviedatabase.movielibrary.model.Credits.CrewMember
import com.allmoviedatabase.movielibrary.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class FullCastViewModel @Inject constructor(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val id = savedStateHandle.get<Int>("movieId") ?: 0
    private val mediaType = savedStateHandle.get<String>("mediaType") ?: "movie"

    private val _cast = MutableLiveData<List<CastMember>>()
    val cast: LiveData<List<CastMember>> = _cast

    private val _groupedCrew = MutableLiveData<List<Any>>()
    val groupedCrew: LiveData<List<Any>> = _groupedCrew

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val disposable = CompositeDisposable()

    init {
        loadCredits()
    }

    private fun loadCredits() {
        _isLoading.value = true

        val apiCall = if (mediaType == "tv") {
            repository.fetchTvShowCredits(id, "tr-TR")
        } else {
            repository.fetchMovieCredits(id, "tr-TR")
        }

        disposable.add(
            apiCall
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ response ->
                    _cast.value = response.cast ?: emptyList()
                    processCrewList(response.crew ?: emptyList())
                    _isLoading.value = false
                }, {
                    _isLoading.value = false
                })
        )
    }

    private fun processCrewList(crewList: List<CrewMember>) {
        // Kotlin'in collection fonksiyonlarıyla daha temiz gruplama
        val finalList = mutableListOf<Any>()

        crewList.groupBy { it.department }
            .forEach { (department, members) ->
                if (!department.isNullOrBlank()) {
                    finalList.add(department)
                    finalList.addAll(members)
                }
            }

        _groupedCrew.value = finalList
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}