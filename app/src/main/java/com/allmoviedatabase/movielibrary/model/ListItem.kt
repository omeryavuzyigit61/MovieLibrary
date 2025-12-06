package com.allmoviedatabase.movielibrary.model

sealed class ListItem {
    // Movie verisini sarmalayan
    data class MovieItem(val movie: Movie) : ListItem()
    data class TvShowItem(val tvShow: TvShow) :
        ListItem()

    data class PersonItem(val person: Person) : ListItem()
}