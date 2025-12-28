package com.allmoviedatabase.movielibrary.model

import java.util.Date

// Firestore'dan veriyi otomatik çekmek için boş constructor (varsayılan değerler) şarttır.
data class ProfileMediaItem(
    val movieId: Int = 0,    // Hem film hem dizi ID'si buraya gelecek
    val tvId: Int = 0,       // Dizi kaydederken tvId kullanmış olabiliriz, ikisini de kontrol ederiz
    val title: String = "",
    val originalTitle: String = "",
    val posterPath: String = "",
    val mediaType: String = "movie", // "movie" veya "tv"
    val voteAverage: Double = 0.0,
    val addedAt: Date? = null
) {
    // ID'yi güvenli almak için yardımcı
    fun getId(): Int {
        return if (movieId != 0) movieId else tvId
    }
}