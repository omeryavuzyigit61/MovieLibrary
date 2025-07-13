package com.allmoviedatabase.movielibrary.model

import com.google.gson.annotations.SerializedName

data class BaseMovie(
    @SerializedName("dates") var dates: Dates?,
    @SerializedName("page") var page: Int? = null,
    @SerializedName("results") var results: ArrayList<Movie> = arrayListOf(),
    @SerializedName("total_pages") var totalPages: Int? = null,
    @SerializedName("total_results") var totalResults: Int? = null
)
