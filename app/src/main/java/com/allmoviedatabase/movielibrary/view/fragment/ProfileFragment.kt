package com.allmoviedatabase.movielibrary.view.fragment

import android.app.AlertDialog
import android.graphics.Color
import android.graphics.drawable.ColorDrawable
import android.os.Bundle
import android.text.Editable
import android.text.TextWatcher
import android.view.LayoutInflater
import android.view.View
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.adapter.BadgeAdapter
import com.allmoviedatabase.movielibrary.adapter.ProfileMediaAdapter
import com.allmoviedatabase.movielibrary.adapter.UserListsAdapter // <--- OLUŞTURDUĞUMUZ ADAPTER
import com.allmoviedatabase.movielibrary.databinding.FragmentProfileBinding
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

    // İki farklı Adapter tanımlıyoruz
    private lateinit var mediaAdapter: ProfileMediaAdapter
    private lateinit var listsAdapter: UserListsAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        _binding = FragmentProfileBinding.bind(view)

        setupAdapters()
        setupUI()
        observeViewModel()
    }

    private fun setupAdapters() {
        // 1. Film/Dizi Adapterı (Tab 0 ve 1 için)
        mediaAdapter = ProfileMediaAdapter { id, mediaType ->
            if (mediaType == "tv") {
                val action = ProfileFragmentDirections.actionProfileFragmentToDetailTvShowFragment(id)
                findNavController().navigate(action)
            } else {
                val action = ProfileFragmentDirections.actionProfileFragmentToDetailMovieFragment(id)
                findNavController().navigate(action)
            }
        }
        binding.rvProfileList.adapter = mediaAdapter
        binding.rvProfileList.layoutManager = GridLayoutManager(requireContext(), 3)

        // 2. Kullanıcı Listeleri Adapterı (Tab 2 için)
        listsAdapter = UserListsAdapter { userList ->
            // ARTIK TOAST YOK, GERÇEK NAVİGASYON VAR
            val action = ProfileFragmentDirections.actionProfileFragmentToUserListDetailFragment(
                listId = userList.listId,
                listName = userList.listName
            )
            findNavController().navigate(action)
        }
        binding.rvUserLists.adapter = listsAdapter
        binding.rvUserLists.layoutManager = LinearLayoutManager(requireContext())
    }

    private fun setupUI() {
        // Rozet Listesi Ayarı
        binding.rvBadgeList.layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)

        // 1. Tab Layout (GÜNCELLENDİ)
        binding.tabLayoutProfile.addOnTabSelectedListener(object : TabLayout.OnTabSelectedListener {
            override fun onTabSelected(tab: TabLayout.Tab?) {
                val position = tab?.position ?: 0
                viewModel.switchTab(position) // Veriyi güncelle
                updateUiForTab(position)      // Görünümü güncelle
            }
            override fun onTabUnselected(tab: TabLayout.Tab?) {}
            override fun onTabReselected(tab: TabLayout.Tab?) {}
        })

        // 2. Chip Filtreleri
        binding.chipGroupFilter.setOnCheckedStateChangeListener { _, checkedIds ->
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
            findNavController().navigate(R.id.action_global_loginFragment)
        }

        // 5. Yeni Liste Oluştur Butonu (FAB)
        binding.fabCreateList.setOnClickListener {
            showCreateListDialog()
        }

        // Başlangıçta doğru UI'ı ayarla
        if (viewModel.currentTabPosition != 0) {
            binding.tabLayoutProfile.getTabAt(viewModel.currentTabPosition)?.select()
            updateUiForTab(viewModel.currentTabPosition)
        }
    }

    // Tablar arası geçişte UI öğelerini gizleyip açan kritik fonksiyon
    private fun updateUiForTab(position: Int) {
        if (position == 2) {
            // --- LİSTELERİM TABI ---
            binding.rvProfileList.visibility = View.GONE  // Film Izgarasını Gizle
            binding.chipGroupFilter.visibility = View.GONE // Filtreleri Gizle
            binding.etProfileSearch.visibility = View.GONE // Aramayı Gizle

            binding.rvUserLists.visibility = View.VISIBLE // Listeleri Göster
            binding.fabCreateList.visibility = View.VISIBLE // Butonu Göster
            binding.tvEmptyState.visibility = View.GONE // Resetle
        } else {
            // --- FAVORİLER / İZLEME LİSTESİ ---
            binding.rvProfileList.visibility = View.VISIBLE
            binding.chipGroupFilter.visibility = View.VISIBLE
            binding.etProfileSearch.visibility = View.VISIBLE

            binding.rvUserLists.visibility = View.GONE
            binding.fabCreateList.visibility = View.GONE
            binding.tvEmptyState.visibility = View.GONE
        }
    }

    private fun showCreateListDialog() {
        // 1. Tasarımı bağla (Layout Inflater ile)
        val dialogView = LayoutInflater.from(requireContext()).inflate(R.layout.dialog_create_list, null)

        // View içindeki elemanları bul
        val etListName = dialogView.findViewById<com.google.android.material.textfield.TextInputEditText>(R.id.etListName)
        val btnCreate = dialogView.findViewById<View>(R.id.btnCreate)
        val btnCancel = dialogView.findViewById<View>(R.id.btnCancel)

        // 2. Dialogu oluştur
        val dialog = AlertDialog.Builder(requireContext())
            .setView(dialogView)
            .create()

        // 3. Arka planı şeffaf yap (Burası çok önemli! Yoksa köşeler beyaz kare kalır)
        dialog.window?.setBackgroundDrawable(ColorDrawable(Color.TRANSPARENT))

        // 4. Buton işlemleri
        btnCreate.setOnClickListener {
            val listName = etListName.text.toString().trim()
            if (listName.isNotEmpty()) {
                viewModel.createNewList(listName) // Veritabanına kaydeder
                Toast.makeText(context, "Liste oluşturuldu!", Toast.LENGTH_SHORT).show()
                dialog.dismiss()
            } else {
                etListName.error = "İsim boş olamaz"
            }
        }

        btnCancel.setOnClickListener {
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun observeViewModel() {
        // Kullanıcı Bilgileri
        viewModel.userInfo.observe(viewLifecycleOwner) { user ->
            if (user != null) {
                binding.tvProfileName.text = user.nickname
                binding.tvProfileEmail.text = user.email
                binding.tvProfileAge.text = calculateAge(user.birthDate)
                binding.tvProfileGender.text = user.gender

                val firebasePhotoUrl = com.google.firebase.auth.FirebaseAuth.getInstance().currentUser?.photoUrl
                Glide.with(this).load(firebasePhotoUrl).circleCrop().into(binding.ivProfileAvatar)
            }
        }

        // Rozetler
        viewModel.userBadges.observe(viewLifecycleOwner) { badgeList ->
            if (badgeList.isNotEmpty()) {
                binding.rvBadgeList.visibility = View.VISIBLE
                binding.rvBadgeList.adapter = BadgeAdapter(badgeList)
            } else {
                binding.rvBadgeList.visibility = View.GONE
            }
        }

        // Medya Listesi (Tab 0 ve 1)
        viewModel.mediaList.observe(viewLifecycleOwner) { list ->
            // Sadece medya tablarındaysak güncelle
            if (viewModel.currentTabPosition != 2) {
                mediaAdapter.submitList(list)
                if (list.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvProfileList.visibility = View.GONE
                    binding.tvEmptyState.text = "Bu listede henüz film yok."
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvProfileList.visibility = View.VISIBLE
                }
            }
        }

        // Kullanıcı Listeleri (Tab 2)
        viewModel.userCreatedLists.observe(viewLifecycleOwner) { lists ->
            // Sadece listeler tabındaysak güncelle
            if (viewModel.currentTabPosition == 2) {
                listsAdapter.submitList(lists)
                if (lists.isEmpty()) {
                    binding.tvEmptyState.visibility = View.VISIBLE
                    binding.rvUserLists.visibility = View.GONE
                    binding.tvEmptyState.text = "Henüz bir liste oluşturmadınız."
                } else {
                    binding.tvEmptyState.visibility = View.GONE
                    binding.rvUserLists.visibility = View.VISIBLE
                }
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