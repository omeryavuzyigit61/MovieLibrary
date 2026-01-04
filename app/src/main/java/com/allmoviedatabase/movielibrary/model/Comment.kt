package com.allmoviedatabase.movielibrary.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.PropertyName

data class Comment(
    val id: String = "",
    val movieId: Int = 0,        // Hangi içerik
    val mediaType: String = "movie", // "movie" veya "tv" (Filtreleme için kritik)
    val userId: String = "",
    val userName: String = "",
    val userAvatarUrl: String = "",
    val content: String = "",
    val timestamp: Timestamp? = null,
    val status: Int = 0,         // 0: İnceleniyor, 1: Yayında, 2: Red

    @get:PropertyName("spoiler")
    @set:PropertyName("spoiler")
    var isSpoiler: Boolean = false
)