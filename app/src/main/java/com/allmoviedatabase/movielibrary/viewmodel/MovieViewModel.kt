package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.Movie
import com.allmoviedatabase.movielibrary.repository.MovieRepository
import com.allmoviedatabase.movielibrary.util.MovieCategory
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

    private val _currentPage = MutableLiveData<Int>()
    val currentPage: MutableLiveData<Int> = _currentPage

    private val _totalPages = MutableLiveData<Int>()
    val totalPages: MutableLiveData<Int> = _totalPages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var currentCategory: MovieCategory = MovieCategory.POPULAR


    init {
        loadMoviesCategory(MovieCategory.POPULAR)
    }

    fun loadMoviesCategory(category: MovieCategory) {
        currentCategory = category
        fetchMovies("tr-TR", 1)
    }

    private fun fetchMovies(language: String, page: Int) {

        val movieSingle = when (currentCategory) {
            MovieCategory.POPULAR -> movieRepository.fetchPopularMovies(language, page)
            MovieCategory.NOW_PLAYING -> movieRepository.fetchNowPlayingMovies(language, page)
            MovieCategory.TOP_RATED -> movieRepository.fetchTopRatedMovies(language, page)
            MovieCategory.UPCOMING -> movieRepository.fetchUpcomingMovies(language, page)
        }

        movieSingle
            .subscribeOn(Schedulers.io())
            .observeOn(AndroidSchedulers.mainThread())
            .doFinally {
                _isLoading.value = false
            }
            .subscribe({ baseMovie ->
                _popularMovies.value = baseMovie.results
                _totalPages.value = baseMovie.totalPages!!
                _currentPage.value = page
            }, { error ->
                _error.value = error.message
            }).let {
                compositeDisposable.add(it)
            }
    }

    fun nextPage() {
        val nextPage = (_currentPage.value ?: 1) + 1
        val total = _totalPages.value ?: nextPage
        if (nextPage <= total) {
            fetchMovies("tr-TR", page = nextPage)
        }
    }

    fun previousPage() {
        val prevPage = (_currentPage.value ?: 2) - 1
        if (prevPage >= 1) {
            fetchMovies("tr-TR", page = prevPage)
        }
    }

    /*fun nextPage() {
        _currentPage.value = _currentPage.value?.plus(1)
        val total = _totalPages.value ?: currentPage.value
        if (_currentPage.value!! < total!!) {
            fetchPopularMovies("tr-TR", currentPage.value!!)
        }
    }

    fun previousPage() {
        val currentPageValue = _currentPage.value ?: 1
        if (currentPageValue > 1) {
            _currentPage.value = currentPageValue - 1
            fetchPopularMovies("tr-TR", _currentPage.value!!)
        }
    }*/

    override fun onCleared() {
        super.onCleared()
        compositeDisposable.clear()
    }
}