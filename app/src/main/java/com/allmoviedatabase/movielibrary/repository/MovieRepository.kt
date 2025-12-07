package com.allmoviedatabase.movielibrary.repository

import com.allmoviedatabase.movielibrary.data.MovieApiService
import com.allmoviedatabase.movielibrary.model.*
import com.allmoviedatabase.movielibrary.model.SeasonDetail.SeasonDetailResponse
import io.reactivex.rxjava3.core.Single
import javax.inject.Inject

class MovieRepository @Inject constructor(private val movieApiService: MovieApiService) {

    // --- Existing Methods ---
    fun fetchPopularMovies(page: Int): Single<BaseResponse<Movie>> =
        movieApiService.getPopularMovies(page)

    fun fetchNowPlayingMovies(page: Int): Single<BaseResponse<Movie>> =
        movieApiService.getNowPlayingMovies(page)

    fun fetchTopRatedMovies(page: Int): Single<BaseResponse<Movie>> =
        movieApiService.getTopRatedMovies(page)

    fun fetchUpcomingMovies(page: Int): Single<BaseResponse<Movie>> =
        movieApiService.getUpcomingMovies(page)

    fun fetchPopularTvShows(page: Int): Single<BaseResponse<TvShow>> =
        movieApiService.getPopularTvShows(page)

    fun fetchTopRatedTvShows(page: Int): Single<BaseResponse<TvShow>> =
        movieApiService.getTopRatedTvShows(page)

    fun fetchOnTheAirTvShows(page: Int): Single<BaseResponse<TvShow>> =
        movieApiService.getOnTheAirTvShows(page)

    fun fetchAiringTodayTvShows(page: Int): Single<BaseResponse<TvShow>> =
        movieApiService.getAiringTodayTvShows(page)

    fun fetchPopularPeople(page: Int): Single<BaseResponse<Person>> =
        movieApiService.getPopularPeople(page)

    // Detail Methods
    fun fetchMovieDetails(movieId: Int, language: String) =
        movieApiService.getMovieDetail(movieId, language)

    fun fetchMovieRecommendations(movieId: Int, language: String) =
        movieApiService.getMovieRecommendations(movieId, language)

    fun fetchMovieCredits(movieId: Int, language: String) =
        movieApiService.getMovieCredits(movieId, language)

    fun fetchMovieReleaseDates(movieId: Int) = movieApiService.getMovieReleaseDates(movieId)
    fun fetchPersonDetails(personId: Int, language: String) =
        movieApiService.getPersonDetails(personId, language)

    fun fetchPersonMovieCredits(personId: Int, language: String) =
        movieApiService.getPersonMovieCredits(personId, language)

    // --- SEARCH METHODS ---
    fun searchMulti(query: String, page: Int) = movieApiService.searchMulti(query, page)
    fun searchMovie(query: String, page: Int) = movieApiService.searchMovie(query, page)
    fun searchTv(query: String, page: Int) = movieApiService.searchTv(query, page)
    fun searchPerson(query: String, page: Int) = movieApiService.searchPerson(query, page)

    fun fetchTvShowDetails(tvId: Int, language: String) =
        movieApiService.getTvShowDetails(tvId, language)

    fun fetchTvShowCredits(tvId: Int, language: String) =
        movieApiService.getTvShowCredits(tvId, language)

    fun fetchTvShowRecommendations(tvId: Int, language: String) =
        movieApiService.getTvShowRecommendations(tvId, language)

    fun getSeasonDetails(tvId: Int, seasonNumber: Int, language: String): Single<SeasonDetailResponse> {
        return movieApiService.getSeasonDetails(tvId, seasonNumber, language)
    }
}