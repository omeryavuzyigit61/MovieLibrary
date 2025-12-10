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

    private val _isLoading = MutableLiveData<Boolean>(false)
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String?>()
    val error: LiveData<String?> = _error

    private val disposable = CompositeDisposable()

    // PUBLIC YAPILDI (Tekrar Dene butonu için)
    var currentSubCategory: SubCategory = SubCategory.POPULAR_MOVIE
    var currentSearchType: SearchType = SearchType.MULTI
    var lastQuery: String = ""
    var isSearchMode: Boolean = false

    init {
        loadContentForCategory(SubCategory.POPULAR_MOVIE)
    }

    fun searchContent(query: String, type: SearchType, page: Int = 1) {
        if (_isLoading.value == true && page != 1) return

        lastQuery = query
        currentSearchType = type
        isSearchMode = true
        _currentPage.value = page
        _isLoading.value = true
        _error.value = null // Hatayı temizle
        disposable.clear()

        val apiCall = when (type) {
            SearchType.MULTI -> movieRepository.searchMulti(query, page).map {
                it.mapToListItem { item ->
                    when (item.mediaType) {
                        "movie" -> ListItem.MovieItem(item.toMovie())
                        "tv" -> ListItem.TvShowItem(item.toTvShow())
                        "person" -> ListItem.PersonItem(item.toPerson())
                        else -> null
                    }
                }
            }
            SearchType.MOVIE -> movieRepository.searchMovie(query, page).map { it.mapToListItem { m -> ListItem.MovieItem(m) } }
            SearchType.TV -> movieRepository.searchTv(query, page).map { it.mapToListItem { t -> ListItem.TvShowItem(t) } }
            SearchType.PERSON -> movieRepository.searchPerson(query, page).map { it.mapToListItem { p -> ListItem.PersonItem(p) } }
        }
        executeApiCall(apiCall)
    }

    fun loadContentForCategory(subCategory: SubCategory, page: Int = 1) {
        if (_isLoading.value == true && page != 1) return

        currentSubCategory = subCategory
        isSearchMode = false
        _currentPage.value = page
        _isLoading.value = true
        _error.value = null // Hatayı temizle
        disposable.clear()

        val apiCall = when (subCategory) {
            SubCategory.POPULAR_MOVIE -> movieRepository.fetchPopularMovies(page).map { it.mapToListItem { m -> ListItem.MovieItem(m) } }
            SubCategory.TOP_RATED_MOVIE -> movieRepository.fetchTopRatedMovies(page).map { it.mapToListItem { m -> ListItem.MovieItem(m) } }
            SubCategory.NOW_PLAYING_MOVIE -> movieRepository.fetchNowPlayingMovies(page).map { it.mapToListItem { m -> ListItem.MovieItem(m) } }
            SubCategory.UPCOMING_MOVIE -> movieRepository.fetchUpcomingMovies(page).map { it.mapToListItem { m -> ListItem.MovieItem(m) } }
            SubCategory.POPULAR_TV -> movieRepository.fetchPopularTvShows(page).map { it.mapToListItem { t -> ListItem.TvShowItem(t) } }
            SubCategory.TOP_RATED_TV -> movieRepository.fetchTopRatedTvShows(page).map { it.mapToListItem { t -> ListItem.TvShowItem(t) } }
            SubCategory.ON_THE_AIR_TV -> movieRepository.fetchOnTheAirTvShows(page).map { it.mapToListItem { t -> ListItem.TvShowItem(t) } }
            SubCategory.AIRING_TODAY_TV -> movieRepository.fetchAiringTodayTvShows(page).map { it.mapToListItem { t -> ListItem.TvShowItem(t) } }
            SubCategory.POPULAR_PERSON -> movieRepository.fetchPopularPeople(page).map { it.mapToListItem { p -> ListItem.PersonItem(p) } }
        }
        executeApiCall(apiCall)
    }

    private fun <T> com.allmoviedatabase.movielibrary.model.BaseResponse<T>.mapToListItem(mapper: (T) -> ListItem?): Pair<List<ListItem>, Int?> {
        val list = this.results?.mapNotNull(mapper) ?: emptyList()
        return Pair(list, this.totalPages)
    }

    private fun executeApiCall(apiCall: Single<Pair<List<ListItem>, Int?>>) {
        disposable.add(
            apiCall.subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread())
                .subscribe({ (items, totalP) ->
                    _contentList.value = items
                    _totalPages.value = totalP ?: 1
                    _isLoading.value = false
                }, { t ->
                    _error.value = t.localizedMessage ?: "Bir hata oluştu"
                    _isLoading.value = false
                })
        )
    }

    // --- SAYFA KONTROLLERİ ---
    fun nextPage() {
        if (_isLoading.value == true) return
        val current = _currentPage.value ?: 1
        val max = _totalPages.value ?: 1
        if (current < max) triggerLoad(current + 1)
    }

    fun previousPage() {
        if (_isLoading.value == true) return
        val current = _currentPage.value ?: 1
        if (current > 1) triggerLoad(current - 1)
    }

    fun jumpToPage(page: Int) {
        if (_isLoading.value == true) return
        val max = _totalPages.value ?: 1
        if (page in 1..max) triggerLoad(page)
    }

    private fun triggerLoad(page: Int) {
        if (isSearchMode) searchContent(lastQuery, currentSearchType, page)
        else loadContentForCategory(currentSubCategory, page)
    }

    // Tekrar Dene için son isteği yineler
    fun retryLastRequest() {
        triggerLoad(_currentPage.value ?: 1)
    }

    override fun onCleared() {
        super.onCleared()
        disposable.clear()
    }
}