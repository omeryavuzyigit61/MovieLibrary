package com.allmoviedatabase.movielibrary.viewmodel

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.Credits.CreditsResponse
import com.allmoviedatabase.movielibrary.model.Detail.ExternalIds
import com.allmoviedatabase.movielibrary.model.Detail.MovieDetail
import com.allmoviedatabase.movielibrary.model.Movie
import com.allmoviedatabase.movielibrary.model.video.VideoResponse
import com.allmoviedatabase.movielibrary.model.video.VideoResult // VideoResult modelini import ettiğinden emin ol
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
class MovieDetailViewModel @Inject constructor(
    private val repository: MovieRepository,
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    private val movieId = savedStateHandle.get<Int>("movieId") ?: 0

    private val _movieDetail = MutableLiveData<MovieDetail>()
    val movieDetail: LiveData<MovieDetail> = _movieDetail

    private val _movieCredits = MutableLiveData<CreditsResponse>()
    val movieCredits: LiveData<CreditsResponse> = _movieCredits

    private val _movieRecommendations = MutableLiveData<List<Movie>>()
    val movieRecommendations: LiveData<List<Movie>> = _movieRecommendations

    private val _ageRating = MutableLiveData<String?>()
    val ageRating: LiveData<String?> = _ageRating

    // YENİ: Video Listesi
    private val _videos = MutableLiveData<List<VideoResult>>()
    val videos: LiveData<List<VideoResult>> = _videos

    private val _externalIds = MutableLiveData<ExternalIds?>()
    val externalIds: LiveData<ExternalIds?> = _externalIds

    private val _isLiked = MutableLiveData<Boolean>()
    val isLiked: LiveData<Boolean> = _isLiked

    private val _isWatchlisted = MutableLiveData<Boolean>()
    val isWatchlisted: LiveData<Boolean> = _isWatchlisted

    private val _totalLikes = MutableLiveData<Int>()
    val totalLikes: LiveData<Int> = _totalLikes

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error


    private val disposable = CompositeDisposable()

    init {
        if (movieId != 0) {
            loadAllData()
            checkUserInteractions() // Kullanıcı bu filmi beğenmiş mi?
            listenToGlobalLikes()   // Toplam beğeni sayısını dinle
        }
    }

    private fun loadAllData() {
        _isLoading.value = true

        disposable.add(
            Single.zip(
                repository.fetchMovieDetails(movieId, "tr-TR"),
                repository.fetchMovieCredits(movieId, "tr-TR"),
                repository.fetchMovieRecommendations(movieId, "tr-TR"),
                repository.fetchMovieReleaseDates(movieId),
                repository.fetchMovieVideos(movieId),
                repository.fetchMovieExternalIds(movieId)
            ) { detail, credits, recs, releaseDates, videos, externals ->
                DataResult(
                    detail,
                    credits,
                    recs.results ?: emptyList(),
                    releaseDates,
                    videos,
                    externals
                )
            }
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ result ->
                    _movieDetail.value = result.detail
                    _movieCredits.value = result.credits
                    _movieRecommendations.value = result.recommendations

                    // Yaş Sınırı
                    val turkeyInfo =
                        result.releaseDatesResponse.results?.find { it.countryCode == "TR" }
                    val rating =
                        turkeyInfo?.releaseDates?.firstOrNull { !it.certification.isNullOrEmpty() }?.certification
                    _ageRating.value = rating

                    // YENİ: Video Listesini Atama
                    // Sadece YouTube videolarını filtreleyebiliriz
                    val videoList =
                        result.videoResponse.results?.filter { it.site == "YouTube" } ?: emptyList()
                    _videos.value = videoList

                    _externalIds.value = result.externalIds

                    _isLoading.value = false
                }, { t ->
                    _error.value = t.localizedMessage
                    _isLoading.value = false
                })
        )
    }

    // 1. Global Beğeni Sayısını Dinle (Realtime)
    private fun listenToGlobalLikes() {
        // "movie_stats" koleksiyonunda movieId dökümanını dinliyoruz
        val docRef = firestore.collection("movie_stats").document(movieId.toString())
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

    // 2. Kullanıcının Kendi Durumunu Kontrol Et
    private fun checkUserInteractions() {
        val userId = auth.currentUser?.uid ?: return
        Log.i("userID", "Kullanıcı ID si: $userId")

        // Kullanıcının favorilerinde bu film var mı?
        firestore.collection("users").document(userId)
            .collection("favorites").document(movieId.toString())
            .get().addOnSuccessListener { doc ->
                _isLiked.value = doc.exists()
            }

        // Kullanıcının izleme listesinde var mı?
        firestore.collection("users").document(userId)
            .collection("watchlist").document(movieId.toString())
            .get().addOnSuccessListener { doc ->
                _isWatchlisted.value = doc.exists()
            }
    }

    // 3. Beğenme İşlemi (Transaction ile güvenli sayım)


    fun toggleLike() {
        val userId = auth.currentUser?.uid ?: return
        val detail = _movieDetail.value ?: return

        val movieStatsRef = firestore.collection("movie_stats").document(movieId.toString())

        // DİKKAT: Ana 'favorites' array'ine değil, 'favorites' adlı alt koleksiyona yazıyoruz
        val userFavRef = firestore.collection("users").document(userId)
            .collection("favorites").document(movieId.toString())

        firestore.runTransaction { transaction ->
            val snapshot = transaction.get(userFavRef)

            if (snapshot.exists()) {
                // --- BEĞENİYİ KALDIR ---
                transaction.delete(userFavRef)

                // Sayacı 1 azalt (Eğer 0'dan küçükse 0 yap mantığı eklenebilir ama Firebase bunu yönetir)
                transaction.update(movieStatsRef, "likeCount", FieldValue.increment(-1))

                // UI'ı hemen güncelle (Listener gelmesini beklemeden)
                _isLiked.postValue(false)
            } else {
                // --- BEĞENİ EKLE ---

                // 1. Kullanıcı tarafına detaylı bilgi ekle (Profil sayfası için)
                val movieData = hashMapOf(
                    "movieId" to movieId,
                    "title" to detail.title, // Türkçe Başlık
                    "originalTitle" to (detail.originalTitle ?: ""), // Orijinal Başlık (EKLENDİ)
                    "posterPath" to (detail.posterPath ?: ""),
                    "voteAverage" to detail.voteAverage,
                    "releaseDate" to (detail.releaseDate ?: ""),
                    "mediaType" to "movie", // FİLM OLDUĞUNU BELİRTİYORUZ (EKLENDİ)
                    "addedAt" to FieldValue.serverTimestamp()
                )
                transaction.set(userFavRef, movieData)

                // 2. Global istatistiklere ekle
                val statsData = hashMapOf(
                    "movieId" to movieId,
                    "title" to detail.title,
                    "originalTitle" to (detail.originalTitle ?: ""), // Buraya da ekledik
                    "likeCount" to FieldValue.increment(1)
                )
                // SetOptions.merge() sayesinde varsa günceller, yoksa oluşturur
                transaction.set(movieStatsRef, statsData, SetOptions.merge())

                _isLiked.postValue(true)
            }
        }.addOnFailureListener {
            _error.value = "İşlem hatası: ${it.localizedMessage}"
            // Hata olursa UI'ı eski haline döndür (Opsiyonel)
        }
    }

    // 4. İzleme Listesine Ekle/Çıkar
    fun toggleWatchlist() {
        val userId = auth.currentUser?.uid ?: return
        val detail = _movieDetail.value ?: return

        val userWatchRef = firestore.collection("users").document(userId)
            .collection("watchlist").document(movieId.toString())

        userWatchRef.get().addOnSuccessListener { doc ->
            if (doc.exists()) {
                userWatchRef.delete()
                _isWatchlisted.value = false
            } else {
                val movieData = hashMapOf(
                    "movieId" to movieId,
                    "title" to detail.title,
                    "originalTitle" to (detail.originalTitle ?: ""), // EKLENDİ
                    "posterPath" to (detail.posterPath ?: ""),
                    "mediaType" to "movie", // EKLENDİ
                    "voteAverage" to detail.voteAverage,
                    "addedAt" to FieldValue.serverTimestamp()
                )
                userWatchRef.set(movieData)
                _isWatchlisted.value = true
            }
        }
    }


    private data class DataResult(
        val detail: MovieDetail,
        val credits: CreditsResponse,
        val recommendations: List<Movie>,
        val releaseDatesResponse: com.allmoviedatabase.movielibrary.model.Adult.ReleaseDatesResponse,
        val videoResponse: VideoResponse,
        val externalIds: ExternalIds
    )

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}