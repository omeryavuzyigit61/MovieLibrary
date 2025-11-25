package com.allmoviedatabase.movielibrary.repository

import com.allmoviedatabase.movielibrary.data.MovieApiService
import javax.inject.Inject

class MovieRepository @Inject constructor(private val movieApiService: MovieApiService) {
    fun fetchPopularMovies(language: String, page: Int) =
        movieApiService.getPopularMovies(language, page)

    fun fetchNowPlayingMovies(language: String, page: Int) =
        movieApiService.getNowPlayingMovies(language, page)

    fun fetchTopRatedMovies(language: String, page: Int) =
        movieApiService.getTopRatedMovies(language, page)

    fun fetchUpcomingMovies(language: String, page: Int) =
        movieApiService.getUpcomingMovies(language, page)

    fun fetchMovieDetails(movieId: Int, language: String) =
        movieApiService.getMovieDetail(movieId, language)

    fun fetchMovieRecommendations(movieId: Int, language: String) =
        movieApiService.getMovieRecommendations(movieId, language)

    fun fetchMovieCredits(movieId: Int, language: String) =
        movieApiService.getMovieCredits(movieId, language)

    fun fetchMovieReleaseDates(movieId: Int) =
        movieApiService.getMovieReleaseDates(movieId)
}
