package com.allmoviedatabase.movielibrary.view.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController // Navigasyon için gerekli
import androidx.recyclerview.widget.LinearLayoutManager
import com.allmoviedatabase.movielibrary.adapter.CrewAdapter
import com.allmoviedatabase.movielibrary.adapter.FullCastAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentFullCastBinding
import com.allmoviedatabase.movielibrary.viewmodel.FullCastViewModel
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class FullCastFragment : Fragment() {

    private var _binding: FragmentFullCastBinding? = null
    private val binding get() = _binding!!

    private val viewModel: FullCastViewModel by viewModels()

    private lateinit var castAdapter: FullCastAdapter
    private lateinit var crewAdapter: CrewAdapter

    // Bu sayfanın dizi mi film mi olduğunu tutacak değişken
    private var isTvShow: Boolean = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        // ARGÜMANI ALMA:
        arguments?.let {
            val mediaType = it.getString("mediaType") // "tv" veya "movie"
            isTvShow = mediaType == "tv"
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?): View {
        _binding = FragmentFullCastBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        setupHeaders()       // Başlıkları ayarla
        setupRecyclerViews() // Listeleri ayarla
        setupObservers()     // Verileri çek
    }

    private fun setupHeaders() {
        if (isTvShow) {
            binding.castHeaderTextView.text = "Dizi Oyuncuları"
            binding.crewHeaderTextView.text = "Dizi Ekibi"
        } else {
            binding.castHeaderTextView.text = "Oyuncu Kadrosu"
            binding.crewHeaderTextView.text = "Ekip Kadrosu"
        }
    }

    private fun setupRecyclerViews() {
        // 1. CAST ADAPTER (Tıklama ile PersonDetail'e git)
        castAdapter = FullCastAdapter(isTvShow = isTvShow) { personId ->
            navigateToPersonDetail(personId)
        }

        binding.castRecyclerView.apply {
            adapter = castAdapter
            layoutManager = LinearLayoutManager(context)
        }

        // 2. CREW ADAPTER (Tıklama ile PersonDetail'e git)
        crewAdapter = CrewAdapter { personId ->
            navigateToPersonDetail(personId)
        }

        binding.crewRecyclerView.apply {
            adapter = crewAdapter
            layoutManager = LinearLayoutManager(context)
        }
    }

    // Ortak Navigasyon Fonksiyonu
    private fun navigateToPersonDetail(personId: Int) {
        // Nav Graph'te action isminiz farklıysa burayı güncelleyin.
        // Genellikle: action_fullCastFragment_to_personDetailFragment
        val action = FullCastFragmentDirections.actionFullCastFragmentToPersonDetailFragment(personId)
        findNavController().navigate(action)
    }

    private fun setupObservers() {
        viewModel.cast.observe(viewLifecycleOwner) { castList ->
            castAdapter.submitList(castList)
        }

        viewModel.groupedCrew.observe(viewLifecycleOwner) { groupedList ->
            crewAdapter.submitList(groupedList)
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}