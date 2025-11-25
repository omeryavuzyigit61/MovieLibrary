package com.allmoviedatabase.movielibrary.model.Adult

import com.google.gson.annotations.SerializedName

data class CountryReleaseInfo(
    @SerializedName("iso_3166_1")
    val countryCode: String?,
    @SerializedName("release_dates")
    val releaseDates: List<ReleaseDateInfo>?
)
