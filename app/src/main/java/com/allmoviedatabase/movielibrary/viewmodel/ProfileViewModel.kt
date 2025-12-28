package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.ProfileMediaItem
import com.allmoviedatabase.movielibrary.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class ProfileViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore
) : ViewModel() {

    // Kullanıcı Bilgileri
    private val _userInfo = MutableLiveData<User?>()
    val userInfo: LiveData<User?> get() = _userInfo

    // UI'da gösterilecek FİLTRELENMİŞ liste
    private val _mediaList = MutableLiveData<List<ProfileMediaItem>>()
    val mediaList: LiveData<List<ProfileMediaItem>> get() = _mediaList

    // Arka planda tutulan HAM liste (Veritabanından gelen tüm veri)
    private var rawList: List<ProfileMediaItem> = emptyList()

    // Filtreleme Durumları
    var currentTabIsFavorites = true // true: Favorites, false: Watchlist
        private set
    private var currentFilterType = "all"    // "all", "movie", "tv"
    private var currentSearchQuery = ""

    init {
        fetchUserProfile()
        loadMediaData() // Başlangıçta favorileri çek
    }

    private fun fetchUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(User::class.java)
                        _userInfo.value = user
                    }
                }
        }
    }

    // Veritabanından veriyi çeker (Tab değiştiğinde çağrılır)
    private fun loadMediaData() {
        val userId = auth.currentUser?.uid ?: return
        val collectionName = if (currentTabIsFavorites) "favorites" else "watchlist"

        firestore.collection("users").document(userId)
            .collection(collectionName)
            .orderBy("addedAt", Query.Direction.DESCENDING) // En son eklenen en üstte
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(ProfileMediaItem::class.java)
                rawList = list // Ham listeyi sakla
                applyFilters() // Filtreleri uygula ve UI'a gönder
            }
            .addOnFailureListener {
                // Hata yönetimi (log veya empty list)
                rawList = emptyList()
                applyFilters()
            }
    }

    // Tüm filtreleri (Tab + Chip + Arama) birleştirip sonucu _mediaList'e atar
    private fun applyFilters() {
        var filteredList = rawList

        // 1. Tip Filtresi (Film/Dizi)
        if (currentFilterType != "all") {
            filteredList = filteredList.filter { it.mediaType == currentFilterType }
        }

        // 2. Arama Filtresi
        if (currentSearchQuery.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.title.contains(currentSearchQuery, ignoreCase = true) ||
                        it.originalTitle.contains(currentSearchQuery, ignoreCase = true)
            }
        }

        _mediaList.value = filteredList
    }

    // --- VIEW TARAFINDAN ÇAĞRILACAK FONKSİYONLAR ---

    // Tab Değişimi
    fun switchTab(isFavorites: Boolean) {
        if (currentTabIsFavorites == isFavorites) return // Aynı taba basıldıysa işlem yapma
        currentTabIsFavorites = isFavorites
        loadMediaData() // Yeni koleksiyonu çek
    }

    // Chip Filtre Değişimi
    fun filterList(type: String) {
        currentFilterType = type
        applyFilters() // Veritabanına gitmeye gerek yok, eldeki listeyi filtrele
    }

    // Arama
    fun searchInList(query: String) {
        currentSearchQuery = query
        applyFilters() // Veritabanına gitmeye gerek yok, eldeki listeyi filtrele
    }

    fun logout() {
        auth.signOut()
    }
}