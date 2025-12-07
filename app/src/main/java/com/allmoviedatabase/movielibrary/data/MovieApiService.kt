package com.allmoviedatabase.movielibrary.data

import com.allmoviedatabase.movielibrary.model.Adult.ReleaseDatesResponse
import com.allmoviedatabase.movielibrary.model.BaseResponse
import com.allmoviedatabase.movielibrary.model.Credits.CreditsResponse
import com.allmoviedatabase.movielibrary.model.Detail.MovieDetail
import com.allmoviedatabase.movielibrary.model.Movie
import com.allmoviedatabase.movielibrary.model.Person
import com.allmoviedatabase.movielibrary.model.PersonDetail.PersonDetail
import com.allmoviedatabase.movielibrary.model.PersonDetail.PersonMovieCredits
import com.allmoviedatabase.movielibrary.model.SearchResult
import com.allmoviedatabase.movielibrary.model.SeasonDetail.SeasonDetailResponse
import com.allmoviedatabase.movielibrary.model.TV.TvShowDetail
import com.allmoviedatabase.movielibrary.model.TvShow
import io.reactivex.rxjava3.core.Single
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

interface MovieApiService {

    @GET("movie/popular")
    fun getPopularMovies(
        @Query("page") page: Int,
        @Query("language") language: String = "tr-TR"
    ): Single<BaseResponse<Movie>>

    @GET("movie/now_playing")
    fun getNowPlayingMovies(
        @Query("page") page: Int,
        @Query("language") language: String = "tr-TR"
    ): Single<BaseResponse<Movie>>

    @GET("movie/top_rated")
    fun getTopRatedMovies(
        @Query("page") page: Int,
        @Query("language") language: String = "tr-TR"
    ): Single<BaseResponse<Movie>>

    @GET("movie/upcoming")
    fun getUpcomingMovies(
        @Query("page") page: Int,
        @Query("language") language: String = "tr-TR"
    ): Single<BaseResponse<Movie>>

    // --- DİZİLER (BaseTvShowResponse yerine BaseResponse<TvShow>) ---
    @GET("tv/popular")
    fun getPopularTvShows(
        @Query("page") page: Int,
        @Query("language") language: String = "tr-TR"
    ): Single<BaseResponse<TvShow>>

    @GET("tv/top_rated")
    fun getTopRatedTvShows(
        @Query("page") page: Int,
        @Query("language") language: String = "tr-TR"
    ): Single<BaseResponse<TvShow>>

    @GET("tv/on_the_air")
    fun getOnTheAirTvShows(
        @Query("page") page: Int,
        @Query("language") language: String = "tr-TR"
    ): Single<BaseResponse<TvShow>>

    @GET("tv/airing_today")
    fun getAiringTodayTvShows(
        @Query("page") page: Int,
        @Query("language") language: String = "tr-TR"
    ): Single<BaseResponse<TvShow>>

    // --- KİŞİLER (BasePersonResponse yerine BaseResponse<Person>) ---
    @GET("person/popular")
    fun getPopularPeople(
        @Query("page") page: Int,
        @Query("language") language: String = "tr-TR"
    ): Single<BaseResponse<Person>>

    // --- ARAMA ---
    // Burası zaten düzgündü ama tutarlılık için kontrol et
    @GET("search/multi")
    fun searchMulti(
        @Query("query") query: String,
        @Query("page") page: Int
    ): Single<BaseResponse<SearchResult>>

    @GET("search/movie")
    fun searchMovie(
        @Query("query") query: String,
        @Query("page") page: Int
    ): Single<BaseResponse<Movie>>

    @GET("search/tv")
    fun searchTv(
        @Query("query") query: String,
        @Query("page") page: Int
    ): Single<BaseResponse<TvShow>>

    @GET("search/person")
    fun searchPerson(
        @Query("query") query: String,
        @Query("page") page: Int
    ): Single<BaseResponse<Person>>


    @GET("movie/{movie_id}")
    fun getMovieDetail(
        @Path("movie_id") movieId: Int,
        @Query("language") language: String
    ): Single<MovieDetail>

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

    @GET("tv/{tv_id}")
    fun getTvShowDetails(
        @Path("tv_id") tvId: Int,
        @Query("language") language: String
    ): Single<TvShowDetail>

    @GET("tv/{tv_id}/aggregate_credits")
    fun getTvShowCredits(
        @Path("tv_id") tvId: Int,
        @Query("language") language: String
    ): Single<CreditsResponse>

    @GET("tv/{tv_id}/recommendations")
    fun getTvShowRecommendations(
        @Path("tv_id") tvId: Int,
        @Query("language") language: String,
        @Query("page") page: Int = 1
    ): Single<com.allmoviedatabase.movielibrary.model.BaseResponse<com.allmoviedatabase.movielibrary.model.TvShow>>

    @GET("tv/{tv_id}/season/{season_number}")
    fun getSeasonDetails(
        @Path("tv_id") tvId: Int,
        @Path("season_number") seasonNumber: Int,
        @Query("language") language: String = "tr-TR"
    ): Single<SeasonDetailResponse>

}
