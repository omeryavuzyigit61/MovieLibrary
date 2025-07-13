package com.allmoviedatabase.movielibrary.data

import com.allmoviedatabase.movielibrary.model.BaseMovie
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
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

}