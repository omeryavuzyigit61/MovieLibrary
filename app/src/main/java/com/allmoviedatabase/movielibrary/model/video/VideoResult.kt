package com.allmoviedatabase.movielibrary.model.video

import com.google.gson.annotations.SerializedName

data class VideoResult(
    @SerializedName("key") val key: String?, // YouTube Video ID'si (Örn: d96cjJhvlMA)
    @SerializedName("site") val site: String?, // "YouTube" olmalı
    @SerializedName("type") val type: String?, // "Trailer", "Teaser" vs.
    @SerializedName("name") val name: String?
)
