package com.allmoviedatabase.movielibrary.model

import com.google.gson.annotations.SerializedName

// Multi Search'ten gelen karmaşık veriyi tutar
data class SearchResult(
    @SerializedName("id") val id: Int?,
    @SerializedName("media_type") val mediaType: String?, // "movie", "tv", "person"

    // Film alanları
    @SerializedName("title") val title: String?,
    @SerializedName("original_title") val originalTitle: String?,
    @SerializedName("release_date") val releaseDate: String?,

    // Dizi ve Kişi alanları
    @SerializedName("name") val name: String?,
    @SerializedName("first_air_date") val firstAirDate: String?,
    @SerializedName("profile_path") val profilePath: String?,
    @SerializedName("known_for_department") val knownForDepartment: String?,

    // Ortak alanlar
    @SerializedName("poster_path") val posterPath: String?,
    @SerializedName("backdrop_path") val backdropPath: String?,
    @SerializedName("vote_average") val voteAverage: Double?,
    @SerializedName("overview") val overview: String?
) {
    // Bu SearchResult'ı senin mevcut Movie modeline çevirir
    fun toMovie(): Movie {
        return Movie(
            id = id,
            title = title,
            originalTitle = originalTitle,
            posterPath = posterPath,
            backdropPath = backdropPath,
            releaseDate = releaseDate,
            voteAverage = voteAverage,
            overview = overview,
            mediaType = "movie",
            adult = false, genreIds = null, popularity = null, video = false, voteCount = null, originalLanguage = null
        )
    }

    // Bu SearchResult'ı senin mevcut TvShow modeline çevirir
    fun toTvShow(): TvShow {
        return TvShow(
            id = id,
            name = name,
            posterPath = posterPath,
            firstAirDate = firstAirDate,
            voteAverage = voteAverage
        )
    }

    // Bu SearchResult'ı senin mevcut Person modeline çevirir
    fun toPerson(): Person {
        return Person(
            id = id,
            name = name,
            profilePath = profilePath, // Kişilerde poster değil profile_path gelir
            knownForDepartment = knownForDepartment
        )
    }
}