package com.allmoviedatabase.movielibrary.model.Adult

import com.google.gson.annotations.SerializedName

data class ReleaseDateInfo (
    @SerializedName("certification")
    val certification: String?,
    @SerializedName("type")
    val type: Int?
)