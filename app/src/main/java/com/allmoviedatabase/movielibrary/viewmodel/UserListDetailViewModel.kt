package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.ProfileMediaItem
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class UserListDetailViewModel @Inject constructor(
    private val auth: FirebaseAuth,
    private val firestore: FirebaseFirestore,
    savedStateHandle: SavedStateHandle
) : ViewModel() {

    // Navigasyon ile gelen argümanları (listId) buradan alacağız
    private val listId = savedStateHandle.get<String>("listId") ?: ""

    private val _listItems = MutableLiveData<List<ProfileMediaItem>>()
    val listItems: LiveData<List<ProfileMediaItem>> = _listItems

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    init {
        if (listId.isNotEmpty()) {
            fetchListItems()
        }
    }

    private fun fetchListItems() {
        val userId = auth.currentUser?.uid ?: return
        _isLoading.value = true

        // users -> {uid} -> created_lists -> {listId} -> items koleksiyonuna gidiyoruz
        firestore.collection("users").document(userId)
            .collection("created_lists").document(listId)
            .collection("items")
            .orderBy("addedAt", Query.Direction.DESCENDING) // En son eklenen en üstte
            .get()
            .addOnSuccessListener { result ->
                // Firebase'den gelen veriyi ProfileMediaItem modeline çeviriyoruz
                val items = result.toObjects(ProfileMediaItem::class.java)
                _listItems.value = items
                _isLoading.value = false
            }
            .addOnFailureListener {
                _isLoading.value = false
                // Hata olursa boş liste
                _listItems.value = emptyList()
            }
    }
}