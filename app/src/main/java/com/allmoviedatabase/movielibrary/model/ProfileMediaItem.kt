package com.allmoviedatabase.movielibrary.model

import java.util.Date

data class ProfileMediaItem(
    val id: Int = 0,    // Bu zaten arka planda getId() oluşturuyor, o yüzden çakışıyordu.
    val movieId: Int = 0,
    val tvId: Int = 0,
    val title: String = "",
    val originalTitle: String = "",
    val posterPath: String = "",
    val mediaType: String = "movie",
    val voteAverage: Double = 0.0,
    val addedAt: Date? = null
) {
    // İSMİNİ DEĞİŞTİRDİK: getEffectiveId
    fun getEffectiveId(): Int {
        if (id != 0) return id
        if (movieId != 0) return movieId
        return tvId
    }
}