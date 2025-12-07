package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.DisplayablePersonDetail
import com.allmoviedatabase.movielibrary.model.Movie
import com.allmoviedatabase.movielibrary.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import java.time.LocalDate
import java.time.Period
import java.time.format.DateTimeFormatter
import java.util.*
import javax.inject.Inject

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Int = savedStateHandle.get<Int>("personId") ?: 0

    private val _personDetail = MutableLiveData<DisplayablePersonDetail>()
    val personDetail: LiveData<DisplayablePersonDetail> = _personDetail

    private val _knownForMovies = MutableLiveData<List<Movie>>()
    val knownForMovies: LiveData<List<Movie>> = _knownForMovies

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val disposable = CompositeDisposable()

    init {
        if (personId != 0) loadPersonData()
    }

    private fun loadPersonData() {
        _isLoading.value = true

        val turkishDetails = repository.fetchPersonDetails(personId, "tr-TR").subscribeOn(Schedulers.io())
        val movieCredits = repository.fetchPersonMovieCredits(personId, "tr-TR").subscribeOn(Schedulers.io())

        disposable.add(
            turkishDetails
                .flatMap { details ->
                    if (details.biography.isNullOrBlank()) {
                        // Türkçe biyografi yoksa İngilizce'yi çek ve uyarılı model döndür
                        repository.fetchPersonDetails(personId, "en-US")
                            .subscribeOn(Schedulers.io())
                            .map { DisplayablePersonDetail(it, true) }
                    } else {
                        // Türkçe varsa direkt onu kullan
                        Single.just(DisplayablePersonDetail(details, false))
                    }
                }
                .zipWith(movieCredits) { displayablePerson, creditsResponse ->
                    // Detay ve film listesini birleştir
                    Pair(displayablePerson, creditsResponse.cast ?: emptyList())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (person, movies) ->
                    _personDetail.value = person
                    _knownForMovies.value = movies
                    _isLoading.value = false
                }, { t ->
                    _error.value = t.localizedMessage
                    _isLoading.value = false
                })
        )
    }

    // --- Helper Fonksiyonlar ---
    fun formatGender(gender: Int?): String = when (gender) {
        1 -> "Kadın"
        2 -> "Erkek"
        else -> "Belirtilmemiş"
    }

    fun formatBirthdayAndAge(birthday: String?): String {
        if (birthday.isNullOrBlank()) return "Bilinmiyor"
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val birthDate = LocalDate.parse(birthday, formatter)
            val age = Period.between(birthDate, LocalDate.now()).years
            val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
            "${birthDate.format(displayFormatter)} ($age yaşında)"
        } catch (_: Exception) {
            birthday
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}