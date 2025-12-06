package com.allmoviedatabase.movielibrary.model.Credits

import com.google.gson.annotations.SerializedName

data class Role(
    @SerializedName("character")
    val character: String?,
    @SerializedName("episode_count")
    val episodeCount: Int?
)
