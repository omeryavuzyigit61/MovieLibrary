package com.allmoviedatabase.movielibrary.view.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import com.allmoviedatabase.movielibrary.adapter.RecommendationAdapter
import com.allmoviedatabase.movielibrary.databinding.FragmentPersonDetailBinding
import com.allmoviedatabase.movielibrary.model.DisplayablePersonDetail
import com.allmoviedatabase.movielibrary.viewmodel.PersonDetailViewModel
import com.bumptech.glide.Glide
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class PersonDetailFragment : Fragment() {

    private var _binding: FragmentPersonDetailBinding? = null
    private val binding get() = _binding!!

    private val viewModel: PersonDetailViewModel by viewModels()
    private lateinit var knownForAdapter: RecommendationAdapter

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View {
        _binding = FragmentPersonDetailBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setupRecyclerView()
        setupObservers()
    }

    private fun setupRecyclerView() {
        knownForAdapter = RecommendationAdapter { movieId ->
            val action = PersonDetailFragmentDirections.actionPersonDetailFragmentToDetailMovieFragment(movieId)
            findNavController().navigate(action)
        }
        binding.knownForRecyclerView.apply {
            adapter = knownForAdapter
            layoutManager = LinearLayoutManager(requireContext(), LinearLayoutManager.HORIZONTAL, false)
        }
    }

    private fun setupObservers() {
        viewModel.personDetail.observe(viewLifecycleOwner) { displayablePerson ->
            bindPersonDetails(displayablePerson)
        }

        viewModel.knownForMovies.observe(viewLifecycleOwner) { movies ->
            binding.knownCreditsTextView.text = movies.size.toString()
            knownForAdapter.submitList(movies)
        }

        viewModel.error.observe(viewLifecycleOwner) { error ->
            if (!error.isNullOrBlank()) {
                Toast.makeText(requireContext(), error, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun bindPersonDetails(displayablePerson: DisplayablePersonDetail) {
        val person = displayablePerson.person

        binding.apply {
            val imageUrl = "https://image.tmdb.org/t/p/w500${person.profilePath}"
            Glide.with(this@PersonDetailFragment)
                .load(imageUrl)
                .into(personImageView)

            personNameTextView.text = person.name

            var biographyText = person.biography
            if (biographyText.isNullOrBlank()) {
                biographyText = "Bu kişi için biyografi bulunamadı."
            } else if (displayablePerson.showEnglishSourceWarning) {
                biographyText += "\n(Sadece İngilizce kaynak bulundu)"
            }

            biographyTextView.text = biographyText
            knownForTextView.text = person.knownForDepartment ?: "-"
            genderTextView.text = viewModel.formatGender(person.gender)
            birthdayTextView.text = viewModel.formatBirthdayAndAge(person.birthday)
            birthPlaceTextView.text = person.placeOfBirth ?: "Bilinmiyor"
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}