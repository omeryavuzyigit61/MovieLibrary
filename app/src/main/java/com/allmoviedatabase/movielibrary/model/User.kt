package com.allmoviedatabase.movielibrary.model

data class User(
    val userId: String = "",
    val email: String = "",
    val nickname: String = "",
    val gender: String = "",
    val birthDate: Long = 0L,
    val registerDate: Long = System.currentTimeMillis(), // Üyelik süresi için lazım
    val earnedBadges: List<String> = emptyList(),
    val stats: Map<String, Int> = emptyMap(),
)
