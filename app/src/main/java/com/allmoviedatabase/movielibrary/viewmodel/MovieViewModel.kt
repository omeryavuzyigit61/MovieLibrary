package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.Movie
import com.allmoviedatabase.movielibrary.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

@HiltViewModel
class MovieViewModel @Inject constructor(private val movieRepository: MovieRepository) :
    ViewModel() {

    private val compositeDisposable = CompositeDisposable()

    private val _popularMovies = MutableLiveData<List<Movie>>()
    val popularMovies: MutableLiveData<List<Movie>> = _popularMovies

    private val _error = MutableLiveData<String>()
    val error: MutableLiveData<String> = _error

    fun fetchPopularMovies(language: String, page: Int) {
        movieRepository.fetchPopularMovies(language, page)
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .subscribe({ baseMovie ->
                _popularMovies.value = baseMovie.results
            }, { error ->
                _error.value = error.message
            }).let {
                compositeDisposable.add(it)
            }
    }
}