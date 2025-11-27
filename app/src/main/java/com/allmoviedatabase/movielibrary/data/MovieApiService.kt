package com.allmoviedatabase.movielibrary.data

import com.allmoviedatabase.movielibrary.model.Adult.ReleaseDatesResponse
import com.allmoviedatabase.movielibrary.model.BaseMovie
import com.allmoviedatabase.movielibrary.model.Credits.CreditsResponse
import com.allmoviedatabase.movielibrary.model.Detail.MovieDetail
import com.allmoviedatabase.movielibrary.model.PersonDetail.PersonDetail
import com.allmoviedatabase.movielibrary.model.PersonDetail.PersonMovieCredits
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {
    @GET("movie/popular")
    fun getPopularMovies(
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<BaseMovie>

    @GET("movie/now_playing")
    fun getNowPlayingMovies(
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<BaseMovie>

    @GET("movie/top_rated")
    fun getTopRatedMovies(
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<BaseMovie>

    @GET("movie/upcoming")
    fun getUpcomingMovies(
        @Query("language") language: String,
        @Query("page") page: Int
    ): Single<BaseMovie>

    @GET("movie/{movie_id}")
    fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String
    ):Single<MovieDetail>

    @GET("movie/{movie_id}/credits")
    fun getMovieCredits(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String
    ): Single<CreditsResponse>

    @GET("movie/{movie_id}/recommendations")
    fun getMovieRecommendations(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String,
        @Query("page") page: Int = 1
    ): Single<com.allmoviedatabase.movielibrary.model.Recommendations.BaseMovie> //

    @GET("movie/{movie_id}/release_dates")
    fun getMovieReleaseDates(
        @Path("movie_id") movieId: Int
    ): Single<ReleaseDatesResponse>

    @GET("person/{person_id}")
    fun getPersonDetails(
        @Path("person_id") personId: Int,
        @Query("language") language: String
    ): Single<PersonDetail>

    @GET("person/{person_id}/movie_credits")
    fun getPersonMovieCredits(
        @Path("person_id") personId: Int,
        @Query("language") language: String
    ): Single<PersonMovieCredits>

}