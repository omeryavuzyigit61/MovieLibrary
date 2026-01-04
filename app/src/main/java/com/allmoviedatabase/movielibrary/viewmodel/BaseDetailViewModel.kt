package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.allmoviedatabase.movielibrary.model.Comment
import com.allmoviedatabase.movielibrary.model.UserList
import com.allmoviedatabase.movielibrary.repository.UserInteractionRepository
import kotlinx.coroutines.launch

abstract class BaseDetailViewModel(
    protected val interactionRepository: UserInteractionRepository
) : ViewModel() {

    protected val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    protected val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    protected val _isLiked = MutableLiveData<Boolean>()
    val isLiked: LiveData<Boolean> = _isLiked

    protected val _isWatchlisted = MutableLiveData<Boolean>()
    val isWatchlisted: LiveData<Boolean> = _isWatchlisted

    protected val _userLists = MutableLiveData<List<UserList>>()
    val userLists: LiveData<List<UserList>> = _userLists

    protected val _addToListStatus = MutableLiveData<String?>()
    val addToListStatus: LiveData<String?> = _addToListStatus

    protected val _commentPostStatus = MutableLiveData<String>()
    val commentPostStatus: LiveData<String> = _commentPostStatus

    protected val _comments = MutableLiveData<List<Comment>>()
    val comments: LiveData<List<Comment>> = _comments

    // YENİ EKLENEN: Ortak Beğeni Sayısı Değişkeni
    protected val _totalLikes = MutableLiveData<Int>()
    val totalLikes: LiveData<Int> = _totalLikes

    fun fetchUserLists() {
        viewModelScope.launch {
            _userLists.value = interactionRepository.getUserLists()
        }
    }

    protected fun listenToComments(itemId: Int) {
        val currentUserId = interactionRepository.currentUserId
        interactionRepository.getCommentsQuery(itemId).addSnapshotListener { value, error ->
            if (error != null) return@addSnapshotListener
            val list = value?.mapNotNull { doc ->
                val c = doc.toObject(Comment::class.java)
                if (c.status == 1 || c.userId == currentUserId) c else null
            } ?: emptyList()
            _comments.value = list
        }
    }

    protected fun checkInteractions(itemId: String) {
        viewModelScope.launch {
            val isFav = interactionRepository.checkItemStatus("favorites", itemId)
            _isLiked.value = isFav

            val isWatch = interactionRepository.checkItemStatus("watchlist", itemId)
            _isWatchlisted.value = isWatch
        }
    }

    // YENİ EKLENEN: Ortak Beğeni Dinleme Fonksiyonu
    protected fun listenToGlobalLikes(itemId: String) {
        interactionRepository.getStatsDocument(itemId).addSnapshotListener { snapshot, e ->
            if (e != null) return@addSnapshotListener
            if (snapshot != null && snapshot.exists()) {
                val count = snapshot.getLong("likeCount")?.toInt() ?: 0
                _totalLikes.value = count
            } else {
                _totalLikes.value = 0
            }
        }
    }
}