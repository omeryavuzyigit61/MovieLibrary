package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.Badge.BadgeDefinition
import com.allmoviedatabase.movielibrary.model.Badge.BadgeManager
import com.allmoviedatabase.movielibrary.model.ProfileMediaItem
import com.allmoviedatabase.movielibrary.model.User
import com.allmoviedatabase.movielibrary.model.UserList
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

    // --- KULLANICI BİLGİLERİ ---
    private val _userInfo = MutableLiveData<User?>()
    val userInfo: LiveData<User?> get() = _userInfo

    private val _userBadges = MutableLiveData<List<BadgeDefinition>>()
    val userBadges: LiveData<List<BadgeDefinition>> get() = _userBadges

    // --- FİLM/DİZİ LİSTESİ (Tab 0 ve 1 için) ---
    private val _mediaList = MutableLiveData<List<ProfileMediaItem>>()
    val mediaList: LiveData<List<ProfileMediaItem>> get() = _mediaList

    // --- KULLANICI LİSTELERİ (Tab 2 için) ---
    private val _userCreatedLists = MutableLiveData<List<UserList>>()
    val userCreatedLists: LiveData<List<UserList>> get() = _userCreatedLists

    // --- KONTROL DEĞİŞKENLERİ ---
    private var rawList: List<ProfileMediaItem> = emptyList()

    // 0: Favoriler, 1: İzleme Listesi, 2: Listelerim
    var currentTabPosition = 0
        private set

    private var currentFilterType = "all"
    private var currentSearchQuery = ""

    init {
        fetchUserProfile()
        loadMediaData() // Başlangıçta Favorileri çek
        fetchUserCreatedLists() // Başlangıçta Listeleri de çek
    }

    // 1. KULLANICI PROFİLİNİ ÇEK
    private fun fetchUserProfile() {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            firestore.collection("users").document(userId).get()
                .addOnSuccessListener { document ->
                    if (document.exists()) {
                        val user = document.toObject(User::class.java)
                        _userInfo.value = user

                        // Rozetleri eşleştir
                        val ownedBadgeIds = user?.earnedBadges ?: emptyList()
                        val matchedBadges = ownedBadgeIds.mapNotNull { id ->
                            BadgeManager.getBadgeById(id)
                        }
                        _userBadges.value = matchedBadges
                    }
                }
        }
    }

    // 2. FİLM/DİZİ VERİSİNİ ÇEK (Tab 0 ve 1)
    private fun loadMediaData() {
        val userId = auth.currentUser?.uid ?: return

        // Eğer Listelerim sekmesindeysek film çekmeye gerek yok
        if (currentTabPosition == 2) return

        val collectionName = if (currentTabPosition == 0) "favorites" else "watchlist"

        firestore.collection("users").document(userId)
            .collection(collectionName)
            .orderBy("addedAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val list = result.toObjects(ProfileMediaItem::class.java)
                rawList = list
                applyFilters()
            }
            .addOnFailureListener {
                rawList = emptyList()
                applyFilters()
            }
    }

    // 3. KULLANICI LİSTELERİNİ ÇEK (Tab 2)
    private fun fetchUserCreatedLists() {
        val userId = auth.currentUser?.uid ?: return

        firestore.collection("users").document(userId)
            .collection("created_lists")
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .get()
            .addOnSuccessListener { result ->
                val lists = result.toObjects(UserList::class.java)
                _userCreatedLists.value = lists
            }
    }

    // 4. YENİ LİSTE OLUŞTUR
    fun createNewList(listName: String) {
        val userId = auth.currentUser?.uid ?: return

        val newListRef = firestore.collection("users").document(userId)
            .collection("created_lists").document()

        val newList = UserList(
            listId = newListRef.id,
            listName = listName,
            createdAt = System.currentTimeMillis(),
            itemCount = 0
        )

        newListRef.set(newList)
            .addOnSuccessListener {
                fetchUserCreatedLists() // Listeyi güncelle
            }
    }

    // --- FİLTRELEME MANTIĞI (Sadece Filmler İçin) ---
    private fun applyFilters() {
        var filteredList = rawList

        // Tip Filtresi
        if (currentFilterType != "all") {
            filteredList = filteredList.filter { it.mediaType == currentFilterType }
        }

        // Arama Filtresi
        if (currentSearchQuery.isNotEmpty()) {
            filteredList = filteredList.filter {
                it.title.contains(currentSearchQuery, ignoreCase = true) ||
                        it.originalTitle.contains(currentSearchQuery, ignoreCase = true)
            }
        }
        _mediaList.value = filteredList
    }

    // --- UI ETKİLEŞİMLERİ ---
    fun switchTab(position: Int) {
        if (currentTabPosition == position) return
        currentTabPosition = position

        if (position == 2) {
            // "Listelerim" sekmesine geçildi
            fetchUserCreatedLists()
        } else {
            // "Favoriler" veya "İzleme Listesi"ne geçildi
            loadMediaData()
        }
    }

    fun filterList(type: String) {
        currentFilterType = type
        applyFilters()
    }

    fun searchInList(query: String) {
        currentSearchQuery = query
        applyFilters()
    }

    fun logout() {
        auth.signOut()
    }
}