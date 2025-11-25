package com.allmoviedatabase.movielibrary.viewmodel

import androidx.fragment.app.add
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
import jakarta.inject.Inject

// viewmodel/FullCastViewModel.kt
@HiltViewModel
class FullCastViewModel @Inject constructor(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle // Navigasyondan gelen argümanları almak için
) : ViewModel() {

    private val movieId = savedStateHandle.get<Int>("movieId")!!

    private val _cast = MutableLiveData<List<CastMember>>()
    val cast: LiveData<List<CastMember>> = _cast

    // Adapter'ın kolayca kullanabilmesi için gruplanmış liste
    private val _groupedCrew = MutableLiveData<List<Any>>()
    val groupedCrew: LiveData<List<Any>> = _groupedCrew

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val compositeDisposable = CompositeDisposable()

    init {
        loadCredits()
    }

    private fun loadCredits() {
        _isLoading.value = true
        compositeDisposable.add(
            repository.fetchMovieCredits(movieId, "tr-TR")
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ creditsResponse ->
                    _cast.value = creditsResponse.cast
                    processCrewList(creditsResponse.crew)
                    _isLoading.value = false
                }, { throwable ->
                    _error.value = "Kadro bilgileri yüklenemedi."
                    _isLoading.value = false
                })
        )
    }

    private fun processCrewList(crewList: List<CrewMember>) {
        val groupedMap = crewList.groupBy { it.department } // Departmana göre grupla

        val finalList = mutableListOf<Any>()
        groupedMap.forEach { (department, members) ->
            department?.let {
                finalList.add(it) // Başlık olarak departman adını ekle (String)
                finalList.addAll(members) // O departmandaki üyeleri ekle (CrewMember)
            }
        }
        _groupedCrew.value = finalList
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}