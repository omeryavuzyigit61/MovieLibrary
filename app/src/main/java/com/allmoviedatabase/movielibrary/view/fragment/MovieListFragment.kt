package com.allmoviedatabase.movielibrary.view.fragment

import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.FrameLayout
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.core.view.isVisible
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.FragmentNavigatorExtras
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.adapter.ContentAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentMovieListBinding
import com.allmoviedatabase.movielibrary.model.ListItem
import com.allmoviedatabase.movielibrary.viewmodel.MovieViewModel
import com.allmoviedatabase.movielibrary.viewmodel.SearchType
import com.allmoviedatabase.movielibrary.viewmodel.SubCategory
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
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
                if (binding.searchFilterContainer.isVisible) {
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
        contentAdapter = ContentAdapter(isHorizontal = false) { listItem, imageView ->
            val extras = FragmentNavigatorExtras(
                imageView to imageView.transitionName
            )

            when (listItem) {
                is ListItem.MovieItem -> listItem.movie.id?.let {
                    val action = MovieListFragmentDirections.actionMovieListFragmentToDetailMovieFragment(it)
                    findNavController().navigate(action, extras)
                }
                is ListItem.TvShowItem -> listItem.tvShow.id?.let {
                    val action = MovieListFragmentDirections.actionMovieListFragmentToDetailTvShowFragment(it)
                    findNavController().navigate(action, extras)
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
        // LİSTE GÖZLEMCİSİ
        viewModel.contentList.observe(viewLifecycleOwner) { list ->
            if (!list.isNullOrEmpty()) {
                contentAdapter.submitList(list) {
                    binding.movieListRecyclerView.scrollToPosition(0)
                }
                // Veri varsa listeyi göster, diğerlerini gizle
                binding.movieListRecyclerView.visibility = View.VISIBLE
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
                binding.errorLayout.visibility = View.GONE
            }
        }

        // HATA GÖZLEMCİSİ
        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage != null) {
                if (contentAdapter.currentList.isEmpty()) {
                    // Liste boşsa ve hata varsa HATA EKRANINI göster
                    binding.movieListRecyclerView.visibility = View.GONE
                    binding.shimmerViewContainer.stopShimmer()
                    binding.shimmerViewContainer.visibility = View.GONE

                    binding.errorLayout.visibility = View.VISIBLE
                    binding.errorTextView.text = "Bağlantı Sorunu\nLütfen internetinizi kontrol edin."
                } else {
                    // Liste doluysa sadece Toast göster
                    Toast.makeText(context, "Bağlantı hatası: Veri yüklenemedi", Toast.LENGTH_SHORT).show()
                }
            }
        }

        // YÜKLENİYOR (SHIMMER) GÖZLEMCİSİ
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            if (isLoading) {
                // Eğer liste boşsa (ilk açılış) Shimmer göster
                if (contentAdapter.currentList.isEmpty()) {
                    binding.errorLayout.visibility = View.GONE
                    binding.movieListRecyclerView.visibility = View.GONE
                    binding.shimmerViewContainer.visibility = View.VISIBLE
                    binding.shimmerViewContainer.startShimmer()
                }
                // Not: Sayfa geçişlerinde üstte shimmer göstermek yerine
                // mevcut listeyi tutuyoruz, kullanıcı bekliyor.
                // İstersen burada ProgressBar'ı visible yapabilirsin ama gerek yok.
            } else {
                binding.shimmerViewContainer.stopShimmer()
                binding.shimmerViewContainer.visibility = View.GONE
            }
        }

        // SAYFA GÖZLEMCİLERİ
        viewModel.currentPage.observe(viewLifecycleOwner) { page ->
            updatePageInfoText()
            binding.buttonPrevious.isEnabled = page > 1
            binding.buttonPrevious.alpha = if (page > 1) 1.0f else 0.5f
        }

        viewModel.totalPages.observe(viewLifecycleOwner) { total ->
            updatePageInfoText()
            val current = viewModel.currentPage.value ?: 1
            binding.buttonNext.isEnabled = current < total
            binding.buttonNext.alpha = if (current < total) 1.0f else 0.5f
        }
    }

    private fun setupClickListeners() {
        binding.searchButton.setOnClickListener { performSearchAndCloseMenu() }

        // TEKRAR DENE BUTONU (Hata durumunda)
        binding.btnRetry.setOnClickListener {
            binding.errorLayout.visibility = View.GONE
            binding.shimmerViewContainer.visibility = View.VISIBLE
            binding.shimmerViewContainer.startShimmer()
            viewModel.retryLastRequest()
        }

        // --- CHIP GRUPLARI (Senin mevcut kodların) ---
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
            if (checkedIds.isNotEmpty() && group.findViewById<View>(checkedIds.first()).isPressed) {
                val subCategory = when (checkedIds.first()) {
                    R.id.chipMoviePopular -> SubCategory.POPULAR_MOVIE
                    R.id.chipMovieTopRated -> SubCategory.TOP_RATED_MOVIE
                    R.id.chipMovieNowPlaying -> SubCategory.NOW_PLAYING_MOVIE
                    R.id.chipMovieUpcoming -> SubCategory.UPCOMING_MOVIE
                    else -> SubCategory.POPULAR_MOVIE
                }
                viewModel.loadContentForCategory(subCategory)
            }
        }

        binding.tvSubCategoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty() && group.findViewById<View>(checkedIds.first()).isPressed) {
                val subCategory = when (checkedIds.first()) {
                    R.id.chipTvPopular -> SubCategory.POPULAR_TV
                    R.id.chipTvTopRated -> SubCategory.TOP_RATED_TV
                    R.id.chipTvOnTheAir -> SubCategory.ON_THE_AIR_TV
                    R.id.chipTvAiringToday -> SubCategory.AIRING_TODAY_TV
                    else -> SubCategory.POPULAR_TV
                }
                viewModel.loadContentForCategory(subCategory)
            }
        }

        binding.peopleSubCategoryChipGroup.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty() && group.findViewById<View>(checkedIds.first()).isPressed) {
                viewModel.loadContentForCategory(SubCategory.POPULAR_PERSON)
            }
        }

        // Sayfalama
        binding.buttonNext.setOnClickListener { viewModel.nextPage() }
        binding.buttonPrevious.setOnClickListener { viewModel.previousPage() }

        // Yukarı Çık (FAB silindiği için bu satır da kalktı)
        // binding.fabScrollUp...

        // Modern Dialog Açma
        binding.textViewPageNumber.setOnClickListener {
            showModernPageDialog()
        }

        binding.searchEditText.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearchAndCloseMenu()
                true
            } else false
        }
    }

    // --- DÜZELTİLMİŞ DIALOG (Klavye Fix) ---
    private fun showModernPageDialog() {
        val totalPages = viewModel.totalPages.value ?: 1
        val currentPage = viewModel.currentPage.value ?: 1
        val context = requireContext()

        val paddingPixel = (24 * resources.displayMetrics.density).toInt()
        val container = FrameLayout(context).apply {
            setPadding(paddingPixel, paddingPixel / 2, paddingPixel, 0)
        }

        val textInputLayout = TextInputLayout(context).apply {
            boxBackgroundMode = TextInputLayout.BOX_BACKGROUND_OUTLINE
            hint = "Sayfa Numarası"
        }

        val editText = TextInputEditText(textInputLayout.context).apply {
            inputType = InputType.TYPE_CLASS_NUMBER
            setText(currentPage.toString())
            selectAll()
        }

        textInputLayout.addView(editText)
        container.addView(textInputLayout, FrameLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT))

        val dialog = MaterialAlertDialogBuilder(context)
            .setTitle("Sayfaya Git")
            .setView(container)
            .setPositiveButton("Git", null)
            .setNegativeButton("İptal", null)
            .create()

        dialog.setOnShowListener {
            editText.postDelayed({
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.showSoftInput(editText, InputMethodManager.SHOW_IMPLICIT)
            }, 100)

            val button = dialog.getButton(android.app.AlertDialog.BUTTON_POSITIVE)
            button.setOnClickListener {
                val pageStr = editText.text.toString()

                // 1. ODAĞI DEĞİŞTİR
                editText.clearFocus()
                binding.movieListRecyclerView.requestFocus()

                // 2. KLAVYEYİ KAPAT
                val imm = context.getSystemService(Context.INPUT_METHOD_SERVICE) as? InputMethodManager
                imm?.hideSoftInputFromWindow(binding.root.windowToken, 0)

                if (pageStr.isNotEmpty()) {
                    val page = pageStr.toIntOrNull()
                    if (page != null && page in 1..totalPages) {
                        // 3. GECİKMELİ KAPAT
                        binding.root.postDelayed({
                            dialog.dismiss()
                            viewModel.jumpToPage(page)
                            binding.movieListRecyclerView.scrollToPosition(0)
                        }, 300)
                    } else {
                        Toast.makeText(context, "Geçersiz sayfa numarası", Toast.LENGTH_SHORT).show()
                    }
                }
            }
        }
        dialog.show()
    }

    // ... (setupFocusListener, performSearchAndCloseMenu, toggleSearchMode, hideKeyboard, updatePageInfoText, updateSubCategoryVisibility AYNI) ...

    private fun setupFocusListener() {
        binding.searchEditText.setOnFocusChangeListener { _, hasFocus ->
            if (hasFocus) toggleSearchMode(isSearchActive = true)
        }
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