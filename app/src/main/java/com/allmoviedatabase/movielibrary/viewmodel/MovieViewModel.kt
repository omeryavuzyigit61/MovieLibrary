package com.allmoviedatabase.movielibrary.viewmodel

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.allmoviedatabase.movielibrary.model.ListItem
import com.allmoviedatabase.movielibrary.repository.MovieRepository
import dagger.hilt.android.lifecycle.HiltViewModel
import io.reactivex.rxjava3.android.schedulers.AndroidSchedulers
import io.reactivex.rxjava3.core.Single
import io.reactivex.rxjava3.disposables.CompositeDisposable
import io.reactivex.rxjava3.schedulers.Schedulers
import javax.inject.Inject

enum class SubCategory { POPULAR_MOVIE, TOP_RATED_MOVIE, NOW_PLAYING_MOVIE, UPCOMING_MOVIE, POPULAR_TV, TOP_RATED_TV, ON_THE_AIR_TV, AIRING_TODAY_TV, POPULAR_PERSON }
enum class SearchType { MULTI, MOVIE, TV, PERSON }

@HiltViewModel
class MovieViewModel @Inject constructor(private val movieRepository: MovieRepository) : ViewModel() {

    private val _contentList = MutableLiveData<List<ListItem>>()
    val contentList: LiveData<List<ListItem>> = _contentList

    private val _currentPage = MutableLiveData(1)
    val currentPage: LiveData<Int> = _currentPage

    private val _totalPages = MutableLiveData(1)
    val totalPages: LiveData<Int> = _totalPages

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val disposable = CompositeDisposable()

    // Mevcut durumu takip etmek için değişkenler
    private var currentSubCategory: SubCategory = SubCategory.POPULAR_MOVIE
    var currentSearchType: SearchType = SearchType.MULTI
    private var lastQuery: String = ""
    var isSearchMode: Boolean = false // Arama modunda mıyız?

    init {
        loadContentForCategory(SubCategory.POPULAR_MOVIE)
    }

    // --- ARAMA FONKSİYONU ---
    fun searchContent(query: String, type: SearchType, page: Int = 1) {
        lastQuery = query
        currentSearchType = type
        isSearchMode = true
        _currentPage.value = page
        _isLoading.value = true
        disposable.clear()

        val apiCall: Single<Pair<List<ListItem>, Int?>> = when (type) {
            SearchType.MULTI -> movieRepository.searchMulti(query, page)
                .map { response ->
                    val listItems = response.results?.mapNotNull { item ->
                        when (item.mediaType) {
                            "movie" -> ListItem.MovieItem(item.toMovie())
                            "tv" -> ListItem.TvShowItem(item.toTvShow())
                            "person" -> ListItem.PersonItem(item.toPerson())
                            else -> null
                        }
                    } ?: emptyList()
                    Pair(listItems, response.totalPages)
                }

            SearchType.MOVIE -> movieRepository.searchMovie(query, page)
                .map { Pair(it.results?.map { m -> ListItem.MovieItem(m) } ?: emptyList(), it.totalPages) }

            SearchType.TV -> movieRepository.searchTv(query, page)
                .map { Pair(it.results?.map { t -> ListItem.TvShowItem(t) } ?: emptyList(), it.totalPages) }

            SearchType.PERSON -> movieRepository.searchPerson(query, page)
                .map { Pair(it.results?.map { p -> ListItem.PersonItem(p) } ?: emptyList(), it.totalPages) }
        }

        executeApiCall(apiCall)
    }

    // --- KATEGORİ YÜKLEME FONKSİYONU ---
    fun loadContentForCategory(subCategory: SubCategory, page: Int = 1) {
        currentSubCategory = subCategory
        isSearchMode = false
        _currentPage.value = page
        _isLoading.value = true
        disposable.clear()

        val apiCall: Single<Pair<List<ListItem>, Int?>> = when (subCategory) {
            SubCategory.POPULAR_MOVIE -> movieRepository.fetchPopularMovies(page)
                .map { Pair(it.results?.map { m -> ListItem.MovieItem(m) } ?: emptyList(), it.totalPages) }
            SubCategory.TOP_RATED_MOVIE -> movieRepository.fetchTopRatedMovies(page)
                .map { Pair(it.results?.map { m -> ListItem.MovieItem(m) } ?: emptyList(), it.totalPages) }
            SubCategory.NOW_PLAYING_MOVIE -> movieRepository.fetchNowPlayingMovies(page)
                .map { Pair(it.results?.map { m -> ListItem.MovieItem(m) } ?: emptyList(), it.totalPages) }
            SubCategory.UPCOMING_MOVIE -> movieRepository.fetchUpcomingMovies(page)
                .map { Pair(it.results?.map { m -> ListItem.MovieItem(m) } ?: emptyList(), it.totalPages) }

            SubCategory.POPULAR_TV -> movieRepository.fetchPopularTvShows(page)
                .map { Pair(it.results?.map { t -> ListItem.TvShowItem(t) } ?: emptyList(), it.totalPages) }
            SubCategory.TOP_RATED_TV -> movieRepository.fetchTopRatedTvShows(page)
                .map { Pair(it.results?.map { t -> ListItem.TvShowItem(t) } ?: emptyList(), it.totalPages) }
            SubCategory.ON_THE_AIR_TV -> movieRepository.fetchOnTheAirTvShows(page)
                .map { Pair(it.results?.map { t -> ListItem.TvShowItem(t) } ?: emptyList(), it.totalPages) }
            SubCategory.AIRING_TODAY_TV -> movieRepository.fetchAiringTodayTvShows(page)
                .map { Pair(it.results?.map { t -> ListItem.TvShowItem(t) } ?: emptyList(), it.totalPages) }

            SubCategory.POPULAR_PERSON -> movieRepository.fetchPopularPeople(page)
                .map { Pair(it.results?.map { p -> ListItem.PersonItem(p) } ?: emptyList(), it.totalPages) }
        }

        executeApiCall(apiCall)
    }

    // --- ORTAK API ÇALIŞTIRMA MANTIĞI ---
    private fun executeApiCall(apiCall: Single<Pair<List<ListItem>, Int?>>) {
        disposable.add(
            apiCall
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (items, totalP) ->
                    _contentList.value = items
                    _totalPages.value = totalP ?: 1
                    _isLoading.value = false
                }, { throwable ->
                    _error.value = "Veri yüklenemedi: ${throwable.message}"
                    _isLoading.value = false
                })
        )
    }

    fun nextPage() {
        val next = (_currentPage.value ?: 1) + 1
        if (next <= (_totalPages.value ?: 1)) {
            if (isSearchMode) {
                searchContent(lastQuery, currentSearchType, next)
            } else {
                loadContentForCategory(currentSubCategory, next)
            }
        }
    }

    fun previousPage() {
        val prev = (_currentPage.value ?: 1) - 1
        if (prev >= 1) {
            if (isSearchMode) {
                searchContent(lastQuery, currentSearchType, prev)
            } else {
                loadContentForCategory(currentSubCategory, prev)
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}