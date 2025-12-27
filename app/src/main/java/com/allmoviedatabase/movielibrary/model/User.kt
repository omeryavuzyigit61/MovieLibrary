package com.allmoviedatabase.movielibrary.model

data class User(
    val userId: String = "",
    val email: String = "",
    val nickname: String = "",
    val birthDate: Long = 0L, // Yaşı string veya int tutabilirsin, edittext'ten string geliyor
    val favorites: List<Int> = emptyList(), // İleride film ID'leri buraya gelecek
    val watchList: List<Int> = emptyList()
)
