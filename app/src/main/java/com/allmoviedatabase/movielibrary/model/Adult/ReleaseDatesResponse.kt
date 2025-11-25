package com.allmoviedatabase.movielibrary.model.Adult

import com.google.gson.annotations.SerializedName

data class ReleaseDatesResponse(
    @SerializedName("id")
    val id: Int?,
    @SerializedName("results")
    val results: List<CountryReleaseInfo>?
)