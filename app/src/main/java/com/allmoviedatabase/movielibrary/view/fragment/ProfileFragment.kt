package com.allmoviedatabase.movielibrary.view.fragment

import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.adapter.ProfileMediaAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentProfileBinding
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL
import com.allmoviedatabase.movielibrary.viewmodel.ProfileViewModel
import com.bumptech.glide.Glide
import com.google.android.material.tabs.TabLayout
import dagger.hilt.android.AndroidEntryPoint
import java.util.Calendar

@AndroidEntryPoint
class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private var _binding: FragmentProfileBinding? = null
    private val binding get() = _binding!!

    private val viewModel: ProfileViewModel by viewModels()
    private lateinit var adapter: ProfileMediaAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        setupAdapter()
        setupUI()
        observeViewModel()
    }

    private fun setupAdapter() {
        adapter = ProfileMediaAdapter { id, mediaType ->
            // Tıklama mantığı: Gelen tipe göre doğru fragmana yönlendir
            if (mediaType == "tv") {
                val action = ProfileFragmentDirections.actionProfileFragmentToDetailTvShowFragment(id)
                findNavController().navigate(action)
            } else {
                // Varsayılan olarak movie kabul ediyoruz
                val action = ProfileFragmentDirections.actionProfileFragmentToDetailMovieFragment(id)
                findNavController().navigate(action)
            }
        }
        binding.rvProfileList.adapter = adapter
        binding.rvProfileList.layoutManager = GridLayoutManager(requireContext(), 3)
    }

    private fun setupUI() {
        // 1. Tab Layout (Favoriler / İzleme Listesi)
        binding.tabLayoutProfile.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                when (tab?.position) {
                    0 -> viewModel.switchTab(isFavorites = true) // Favoriler
                    1 -> viewModel.switchTab(isFavorites = false) // İzleme Listesi
                }
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 2. Chip Filtreleri (Hepsi / Film / Dizi)
        binding.chipGroupFilter.setOnCheckedStateChangeListener { group, checkedIds ->
            if (checkedIds.isNotEmpty()) {
                when (checkedIds[0]) {
                    R.id.chipAll -> viewModel.filterList("all")
                    R.id.chipMovies -> viewModel.filterList("movie")
                    R.id.chipTvShows -> viewModel.filterList("tv")
                }
            }
        }

        // 3. Arama Çubuğu
        binding.etProfileSearch.addTextChangedListener(object : TextWatcher {
            override fun afterTextChanged(s: Editable?) {
                viewModel.searchInList(s.toString())
            }
            override fun beforeTextChanged(s: CharSequence?, start: Int, count: Int, after: Int) {}
            override fun onTextChanged(s: CharSequence?, start: Int, before: Int, count: Int) {}
        })

        // 4. Çıkış Yap Butonu
        binding.btnLogout.setOnClickListener {
            viewModel.logout()
            // Global Action ile Login'e dön
            findNavController().navigate(R.id.action_global_loginFragment)
        }
        if (!viewModel.currentTabIsFavorites) {
            val watchlistTab = binding.tabLayoutProfile.getTabAt(1)
            watchlistTab?.select()
        }
    }

    private fun observeViewModel() {
        // Kullanıcı Bilgileri
        viewModel.userInfo.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvProfileName.text = user.nickname
                binding.tvProfileEmail.text = user.email
                binding.tvProfileAge.text = calculateAge(user.birthDate)

                val firebasePhotoUrl = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.photoUrl

                // Profil resmi için placeholder veya varsa URL (Şu an placeholder varsayıyoruz)
                Glide.with(this)
                    .load(firebasePhotoUrl) // Veya user.profilePhotoUrl
                    .circleCrop()
                    .into(binding.ivProfileAvatar)
            }
        }

        // Medya Listesi (Favoriler veya Watchlist)
        viewModel.mediaList.observe(viewLifecycleOwner) { list ->
            adapter.submitList(list)

            // Liste boşsa uyarı göster
            if (list.isEmpty()) {
                binding.tvEmptyState.visibility = View.VISIBLE
                binding.rvProfileList.visibility = View.GONE
            } else {
                binding.tvEmptyState.visibility = View.GONE
                binding.rvProfileList.visibility = View.VISIBLE
            }
        }
    }

    private fun calculateAge(birthDateMillis: Long): String {
        if (birthDateMillis == 0L) return ""
        val dob = Calendar.getInstance().apply { timeInMillis = birthDateMillis }
        val today = Calendar.getInstance()

        var age = today.get(Calendar.YEAR) - dob.get(Calendar.YEAR)
        if (today.get(Calendar.DAY_OF_YEAR) < dob.get(Calendar.DAY_OF_YEAR)) {
            age--
        }
        return "$age Yaşında"
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}