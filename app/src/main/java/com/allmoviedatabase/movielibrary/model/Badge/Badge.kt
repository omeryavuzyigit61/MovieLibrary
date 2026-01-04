package com.allmoviedatabase.movielibrary.model.Badge

data class Badge(
    val id: String,
    val name: String,
    val iconResId: Int // Gerçek uygulamada String URL olabilir
)
