package com.allmoviedatabase.movielibrary.model

import com.google.gson.annotations.SerializedName

// Tek Çatı Model: Film, Dizi, Kişi veya Arama sonuçlarının hepsi bunu kullanacak.
// <T> yerine Movie, TvShow veya Person gelecek.
data class BaseResponse<T>(
    @SerializedName("page")
    val page: Int?,

    @SerializedName("results")
    val results: List<T>?,

    @SerializedName("total_pages")
    val totalPages: Int?,

    @SerializedName("total_results")
    val totalResults: Int?,

    // Sadece "Now Playing" ve "Upcoming" filmlerinde gelen tarih verisi.
    // Diğerlerinde null geleceği için sorun çıkarmaz.
    @SerializedName("dates")
    val dates: Dates?
)