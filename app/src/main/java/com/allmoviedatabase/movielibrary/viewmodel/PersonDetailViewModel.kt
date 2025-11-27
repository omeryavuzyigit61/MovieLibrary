// file: com/allmoviedatabase/movielibrary/viewmodel/PersonDetailViewModel.kt
package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.*
import com.allmoviedatabase.movielibrary.model.Movie
import com.allmoviedatabase.movielibrary.model.PersonDetail.PersonDetail
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

/**
 * ViewModel'in UI'a göndereceği veriyi temsil eden özel bir sınıf.
 * Bu, verinin kendisiyle birlikte UI'ın nasıl davranması gerektiği bilgisini de taşır.
 */
data class DisplayablePersonDetail(
    val person: PersonDetail,
    val showEnglishSourceWarning: Boolean
)

@HiltViewModel
class PersonDetailViewModel @Inject constructor(
    private val repository: MovieRepository,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val personId: Int = savedStateHandle.get<Int>("personId")!!

    // LiveData'mız artık UI'a özel DisplayablePersonDetail modelini tutacak.
    private val _personDetail = MutableLiveData<DisplayablePersonDetail>()
    val personDetail: LiveData<DisplayablePersonDetail> = _personDetail

    private val _knownForMovies = MutableLiveData<List<Movie>>()
    val knownForMovies: LiveData<List<Movie>> = _knownForMovies

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    private val compositeDisposable = CompositeDisposable()

    init {
        loadPersonData()
    }

    private fun loadPersonData() {
        _isLoading.value = true

        // 1. Önce Türkçe detayları getiren bir Single oluştur.
        val turkishDetailsSingle = repository.fetchPersonDetails(personId, "tr-TR").subscribeOn(Schedulers.io())

        // 2. Oyuncunun filmlerini getiren bir Single oluştur.
        val movieCreditsSingle = repository.fetchPersonMovieCredits(personId, "tr-TR").subscribeOn(Schedulers.io())

        compositeDisposable.add(
            turkishDetailsSingle
                .flatMap { turkishDetails ->
                    // 3. Gelen Türkçe biyografi boş mu diye kontrol et.
                    if (turkishDetails.biography.isNullOrBlank()) {
                        // 4. Eğer boşsa, İngilizce detayları iste ve sonucu uyarı flag'i ile birlikte paketle.
                        repository.fetchPersonDetails(personId, "en-US").subscribeOn(Schedulers.io())
                            .map { englishDetails -> DisplayablePersonDetail(englishDetails, true) }
                    } else {
                        // 5. Eğer doluysa, mevcut Türkçe detayları kullan ve uyarı flag'ini false yap.
                        Single.just(DisplayablePersonDetail(turkishDetails, false))
                    }
                }
                // 6. Biyografi işi bittikten sonra, sonucu film kredileriyle birleştir.
                .zipWith(movieCreditsSingle) { displayableDetails, credits ->
                    Pair(displayableDetails, credits.cast ?: emptyList())
                }
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (displayableDetails, movies) ->
                    _personDetail.value = displayableDetails
                    _knownForMovies.value = movies
                    _isLoading.value = false
                }, { throwable ->
                    _error.value = "Oyuncu bilgileri yüklenemedi: ${throwable.localizedMessage}"
                    _isLoading.value = false
                })
        )
    }

    // --- Helper Fonksiyonlar ---
    fun formatGender(gender: Int?): String {
        return when (gender) {
            1 -> "Kadın"
            2 -> "Erkek"
            else -> "Belirtilmemiş"
        }
    }

    fun formatBirthdayAndAge(birthday: String?): String {
        if (birthday.isNullOrBlank()) return "Bilinmiyor"
        return try {
            val formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd")
            val birthDate = LocalDate.parse(birthday, formatter)
            val age = Period.between(birthDate, LocalDate.now()).years

            val displayFormatter = DateTimeFormatter.ofPattern("d MMMM yyyy", Locale("tr"))
            "${birthDate.format(displayFormatter)} ($age yaşında)"
        } catch (e: Exception) {
            birthday
        }
    }

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}