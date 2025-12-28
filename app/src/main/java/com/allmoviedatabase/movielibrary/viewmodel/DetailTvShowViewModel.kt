package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.Credits.CastMember
import com.allmoviedatabase.movielibrary.model.ListItem
import com.allmoviedatabase.movielibrary.model.TV.TvShowDetail
import com.allmoviedatabase.movielibrary.model.video.VideoResult
import com.allmoviedatabase.movielibrary.repository.MovieRepository
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class DetailTvShowViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val auth: FirebaseAuth,         // EKLENDİ
    private val firestore: FirebaseFirestore, // EKLENDİ
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val tvId = savedStateHandle.get<Int>("tvId") ?: 0

    // --- MEVCUT VERİLER ---
    private val _tvDetail = MutableLiveData<TvShowDetail>()
    val tvDetail: LiveData<TvShowDetail> = _tvDetail

    private val _cast = MutableLiveData<List<CastMember>>()
    val cast: LiveData<List<CastMember>> = _cast

    private val _recommendations = MutableLiveData<List<ListItem>>()
    val recommendations: LiveData<List<ListItem>> = _recommendations

    private val _videos = MutableLiveData<List<VideoResult>>()
    val videos: LiveData<List<VideoResult>> = _videos

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    // --- YENİ EKLENEN INTERACTION VERİLERİ ---
    private val _isLiked = MutableLiveData<Boolean>()
    val isLiked: LiveData<Boolean> = _isLiked

    private val _isWatchlisted = MutableLiveData<Boolean>()
    val isWatchlisted: LiveData<Boolean> = _isWatchlisted

    private val _totalLikes = MutableLiveData<Int>()
    val totalLikes: LiveData<Int> = _totalLikes

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error
    // -----------------------------------------

    private val disposable = CompositeDisposable()

    init {
        if (tvId != 0) {
            loadAllData()
            checkUserInteractions() // Kullanıcı durumu
            listenToGlobalLikes()   // Global beğeni sayısı
        }
    }

    private fun loadAllData() {
        _isLoading.value = true

        disposable.add(
            Single.zip(
                repository.fetchTvShowDetails(tvId, "tr-TR"),
                repository.fetchTvShowCredits(tvId, "tr-TR"),
                repository.fetchTvShowRecommendations(tvId, "tr-TR"),
                repository.fetchTvShowVideos(tvId)
            ) { detail, creditsResponse, recsResponse, videosResponse ->

                val castList = creditsResponse.cast ?: emptyList()
                val recsList = recsResponse.results?.map { tvShow ->
                    ListItem.TvShowItem(tvShow)
                } ?: emptyList()

                val videoList = videosResponse.results?.filter { video ->
                    video.site == "YouTube"
                } ?: emptyList()

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
                    error.printStackTrace()
                })
        )
    }

    // --- INTERACTION FONKSİYONLARI ---

    // 1. Global Beğeni Sayısını Dinle
    private fun listenToGlobalLikes() {
        val docRef = firestore.collection("movie_stats").document(tvId.toString())
        docRef.addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                val count = snapshot.getLong("likeCount")?.toInt() ?: 0
                _totalLikes.value = count
            } else {
                _totalLikes.value = 0
            }
        }
    }

    // 2. Kullanıcı Etkileşimlerini Kontrol Et
    private fun checkUserInteractions() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("favorites").document(tvId.toString())
            .get().addOnSuccessListener { doc -> _isLiked.value = doc.exists() }

        firestore.collection("users").document(userId)
            .collection("watchlist").document(tvId.toString())
            .get().addOnSuccessListener { doc -> _isWatchlisted.value = doc.exists() }
    }

    // 3. Beğenme İşlemi (Dizi Bilgileriyle)
    fun toggleLike() {
        val userId = auth.currentUser?.uid ?: return
        val detail = _tvDetail.value ?: return

        val statsRef = firestore.collection("movie_stats").document(tvId.toString())
        val userFavRef = firestore.collection("users").document(userId)
            .collection("favorites").document(tvId.toString())

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userFavRef)

            if (snapshot.exists()) {
                // Kaldır
                transaction.delete(userFavRef)
                transaction.update(statsRef, "likeCount", FieldValue.increment(-1))
                _isLiked.postValue(false)
            } else {
                // Ekle (Detaylı Dizi Bilgisi)
                val tvData = hashMapOf(
                    "tvId" to tvId,
                    "title" to detail.name,
                    "originalTitle" to (detail.originalName ?: ""),
                    "posterPath" to (detail.posterPath ?: ""),
                    "voteAverage" to detail.voteAverage,
                    "firstAirDate" to (detail.firstAirDate ?: ""),
                    "mediaType" to "tv", // ÖNEMLİ: Dizi olduğunu belirtiyoruz
                    "addedAt" to FieldValue.serverTimestamp()
                )
                transaction.set(userFavRef, tvData)

                // Global İstatistik
                val statsData = hashMapOf(
                    "tvId" to tvId,
                    "title" to detail.name,
                    "originalTitle" to (detail.originalName ?: ""),
                    "likeCount" to FieldValue.increment(1)
                )
                transaction.set(statsRef, statsData, SetOptions.merge())

                _isLiked.postValue(true)
            }
        }.addOnFailureListener {
            _error.value = "İşlem başarısız: ${it.localizedMessage}"
        }
    }

    // 4. İzleme Listesi İşlemi (Dizi Bilgileriyle)
    fun toggleWatchlist() {
        val userId = auth.currentUser?.uid ?: return
        val detail = _tvDetail.value ?: return

        val userWatchRef = firestore.collection("users").document(userId)
            .collection("watchlist").document(tvId.toString())

        userWatchRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                userWatchRef.delete()
                _isWatchlisted.value = false
            } else {
                val tvData = hashMapOf(
                    "tvId" to tvId,
                    "title" to detail.name,
                    "originalTitle" to (detail.originalName ?: ""),
                    "posterPath" to (detail.posterPath ?: ""),
                    "mediaType" to "tv", // ÖNEMLİ
                    "voteAverage" to detail.voteAverage,
                    "addedAt" to FieldValue.serverTimestamp()
                )
                userWatchRef.set(tvData)
                _isWatchlisted.value = true
            }
        }
    }

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