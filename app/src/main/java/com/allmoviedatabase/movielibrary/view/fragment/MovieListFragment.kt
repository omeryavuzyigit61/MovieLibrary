package com.allmoviedatabase.movielibrary.view.fragment

import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.adapter.ContentAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentMovieListBinding
import com.allmoviedatabase.movielibrary.model.ListItem
import com.allmoviedatabase.movielibrary.viewmodel.MovieViewModel
import com.allmoviedatabase.movielibrary.viewmodel.SearchType
import com.allmoviedatabase.movielibrary.viewmodel.SubCategory
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MovieListFragment : Fragment() {
    private var _binding: FragmentMovieListBinding? = null
    private val binding get() = _binding!!

    private lateinit var contentAdapter: ContentAdapter
    private val viewModel: MovieViewModel by viewModels()

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentMovieListBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupUI()
        setupObservers()
        setupClickListeners()
        setupFocusListener()
        setupBackPressHandler()
    }

    private fun setupBackPressHandler() {
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                if (binding.searchFilterContainer.visibility == View.VISIBLE) {
                    toggleSearchMode(false)
                    binding.searchEditText.clearFocus()
                    hideKeyboard()
                } else {
                    isEnabled = false
                    requireActivity().onBackPressedDispatcher.onBackPressed()
                }
            }
        })
    }

    private fun setupUI() {
        contentAdapter = ContentAdapter(isHorizontal = false) { listItem ->
            when (listItem) {
                is ListItem.MovieItem -> listItem.movie.id?.let {
                    val action = MovieListFragmentDirections.actionMovieListFragmentToDetailMovieFragment(it)
                    findNavController().navigate(action)
                }
                is ListItem.TvShowItem -> listItem.tvShow.id?.let {
                    val action = MovieListFragmentDirections.actionMovieListFragmentToDetailTvShowFragment(it)
                    findNavController().navigate(action)
                }
                is ListItem.PersonItem -> listItem.person.id?.let {
                    val action = MovieListFragmentDirections.actionMovieListFragmentToPersonDetailFragment(it)
                    findNavController().navigate(action)
                }
            }
        }
        binding.movieListRecyclerView.layoutManager = GridLayoutManager(context, 2)
        binding.movieListRecyclerView.adapter = contentAdapter
    }

    private fun setupObservers() {
        viewModel.contentList.observe(viewLifecycleOwner) { list ->
            contentAdapter.submitList(list)
            binding.movieListRecyclerView.scrollToPosition(0)
        }
        viewModel.isLoading.observe(viewLifecycleOwner) {
            binding.progressBar.visibility = if (it) View.VISIBLE else View.GONE
        }
        viewModel.currentPage.observe(viewLifecycleOwner) { updatePageInfoText() }
        viewModel.totalPages.observe(viewLifecycleOwner) {
            updatePageInfoText()
            binding.buttonNext.isEnabled = (viewModel.currentPage.value ?: 1) < (it ?: 1)
        }
    }

    private fun setupFocusListener() {
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) {
                toggleSearchMode(isSearchActive = true)
            }
        }
    }

    private fun setupClickListeners() {
        binding.searchButton.setOnClickListener { performSearchAndCloseMenu() }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearchAndCloseMenu()
                true
            } else false
        }

        binding.mainCategoryChipGroup.setOnCheckedStateChangeListener { _, checkedIds ->
            if (checkedIds.isEmpty()) return@setOnCheckedStateChangeListener
            binding.searchEditText.clearFocus()
            hideKeyboard()
            val checkedId = checkedIds.first()
            updateSubCategoryVisibility(checkedId)

            when (checkedId) {
                R.id.chipMovies -> {
                    binding.movieSubCategoryChipGroup.check(R.id.chipMoviePopular)
                    viewModel.loadContentForCategory(SubCategory.POPULAR_MOVIE)
                }
                R.id.chipTvShows -> {
                    binding.tvSubCategoryChipGroup.check(R.id.chipTvPopular)
                    viewModel.loadContentForCategory(SubCategory.POPULAR_TV)
                }
                R.id.chipPeople -> {
                    binding.peopleSubCategoryChipGroup.check(R.id.chipPersonPopular)
                    viewModel.loadContentForCategory(SubCategory.POPULAR_PERSON)
                }
            }
        }

        binding.movieSubCategoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty() || !group.findViewById<View>(checkedIds.first()).isPressed) return@setOnCheckedStateChangeListener
            val subCategory = when (checkedIds.first()) {
                R.id.chipMoviePopular -> SubCategory.POPULAR_MOVIE
                R.id.chipMovieTopRated -> SubCategory.TOP_RATED_MOVIE
                R.id.chipMovieNowPlaying -> SubCategory.NOW_PLAYING_MOVIE
                R.id.chipMovieUpcoming -> SubCategory.UPCOMING_MOVIE
                else -> return@setOnCheckedStateChangeListener
            }
            viewModel.loadContentForCategory(subCategory)
        }

        binding.tvSubCategoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty() || !group.findViewById<View>(checkedIds.first()).isPressed) return@setOnCheckedStateChangeListener
            val subCategory = when (checkedIds.first()) {
                R.id.chipTvPopular -> SubCategory.POPULAR_TV
                R.id.chipTvTopRated -> SubCategory.TOP_RATED_TV
                R.id.chipTvOnTheAir -> SubCategory.ON_THE_AIR_TV
                R.id.chipTvAiringToday -> SubCategory.AIRING_TODAY_TV
                else -> return@setOnCheckedStateChangeListener
            }
            viewModel.loadContentForCategory(subCategory)
        }

        binding.peopleSubCategoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isEmpty() || !group.findViewById<View>(checkedIds.first()).isPressed) return@setOnCheckedStateChangeListener
            if (checkedIds.first() == R.id.chipPersonPopular) {
                viewModel.loadContentForCategory(SubCategory.POPULAR_PERSON)
            }
        }

        binding.buttonNext.setOnClickListener { viewModel.nextPage() }
        binding.buttonPrevious.setOnClickListener { viewModel.previousPage() }
    }

    private fun performSearchAndCloseMenu() {
        val query = binding.searchEditText.text.toString().trim()
        binding.searchEditText.clearFocus()
        hideKeyboard()

        if (query.isNotEmpty()) {
            val checkedId = binding.searchChipGroup.checkedChipId
            val searchType = when (checkedId) {
                R.id.chipSearchMulti -> SearchType.MULTI
                R.id.chipSearchMovie -> SearchType.MOVIE
                R.id.chipSearchTv -> SearchType.TV
                R.id.chipSearchPerson -> SearchType.PERSON
                else -> SearchType.MULTI
            }
            viewModel.searchContent(query, searchType)
            toggleSearchMode(isSearchActive = false)
        } else {
            toggleSearchMode(isSearchActive = false)
            binding.mainCategoryChipGroup.check(R.id.chipMovies)
            binding.movieSubCategoryChipGroup.check(R.id.chipMoviePopular)
            viewModel.loadContentForCategory(SubCategory.POPULAR_MOVIE)
            Toast.makeText(context, "Ana sayfaya dönüldü", Toast.LENGTH_SHORT).show()
        }
    }

    private fun toggleSearchMode(isSearchActive: Boolean) {
        if (isSearchActive) {
            binding.mainCategoryChipGroup.visibility = View.GONE
            binding.subCategoryContainer.visibility = View.GONE
            binding.searchFilterContainer.visibility = View.VISIBLE
        } else {
            binding.searchFilterContainer.visibility = View.GONE
            binding.mainCategoryChipGroup.visibility = View.VISIBLE
            binding.subCategoryContainer.visibility = View.VISIBLE
        }
    }

    private fun hideKeyboard() {
        val imm = requireContext().getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(binding.root.windowToken, 0)
    }

    private fun updatePageInfoText() {
        val currentPage = viewModel.currentPage.value ?: "-"
        val totalPages = viewModel.totalPages.value ?: "-"
        binding.textViewPageNumber.text = "$currentPage / $totalPages"
    }

    private fun updateSubCategoryVisibility(mainCheckedId: Int) {
        binding.movieSubCategoryChipGroup.visibility = if (mainCheckedId == R.id.chipMovies) View.VISIBLE else View.GONE
        binding.tvSubCategoryChipGroup.visibility = if (mainCheckedId == R.id.chipTvShows) View.VISIBLE else View.GONE
        binding.peopleSubCategoryChipGroup.visibility = if (mainCheckedId == R.id.chipPeople) View.VISIBLE else View.GONE
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}