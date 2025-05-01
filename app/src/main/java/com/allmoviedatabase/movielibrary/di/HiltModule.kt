package com.allmoviedatabase.movielibrary.di

import com.allmoviedatabase.movielibrary.data.MovieApiService
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.adapter.rxjava3.RxJava3CallAdapterFactory
import retrofit2.converter.gson.GsonConverterFactory

@Module
@InstallIn(SingletonComponent::class)
object HiltModule {

    private const val BASE_URL = "https://api.themoviedb.org/3/"
    private const val AUTH_TOKEN = "Bearer eyJhbGciOiJIUzI1NiJ9.eyJhdWQiOiJhMzUzZTVmMGEyMWFlYjQ1ODM4MWRjNzA3ZmU2MzMzOCIsIm5iZiI6MTc0NTA5MDcxNS44NTUsInN1YiI6IjY4MDNmODliYzVjODAzNWZiMDg5YTljMiIsInNjb3BlcyI6WyJhcGlfcmVhZCJdLCJ2ZXJzaW9uIjoxfQ.wItbGu3ld8F42AHS0b7MptiRVmZAzZ4qVBC0OfzVfUg"

    @Provides
    fun provideOkHttpClient(): OkHttpClient =
        OkHttpClient.Builder()
            .addInterceptor { chain ->
                val request = chain.request().newBuilder()
                    .addHeader("Authorization", AUTH_TOKEN)
                    .build()
                chain.proceed(request)
            }
            .build()

    @Provides
    fun provideRetrofit(client: OkHttpClient): Retrofit =
        Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(client)
            .addConverterFactory(GsonConverterFactory.create())
            .addCallAdapterFactory(RxJava3CallAdapterFactory.create())
            .build()

    @Provides
    fun provideTMDbService(retrofit: Retrofit): MovieApiService =
        retrofit.create(MovieApiService::class.java)
}