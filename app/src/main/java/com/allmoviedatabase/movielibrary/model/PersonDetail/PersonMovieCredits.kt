package com.allmoviedatabase.movielibrary.model.PersonDetail

import com.allmoviedatabase.movielibrary.model.Movie
import com.google.gson.annotations.SerializedName

data class PersonMovieCredits(
    @SerializedName("cast")
    val cast: List<Movie>?
)
