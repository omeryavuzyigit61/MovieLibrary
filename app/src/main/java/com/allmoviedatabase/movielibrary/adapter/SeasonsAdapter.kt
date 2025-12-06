package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemSeasonBinding
import com.allmoviedatabase.movielibrary.model.TV.Season
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlin.math.roundToInt

class SeasonsAdapter : ListAdapter<Season, SeasonsAdapter.SeasonViewHolder>(SeasonDiffCallback()) {

    class SeasonViewHolder(private val binding: ItemSeasonBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(season: Season) {
            binding.seasonNameTextView.text = season.name
            binding.seasonOverviewTextView.text = season.overview

            // Eğer özet boşsa bilgi ver
            if (season.overview.isNullOrEmpty()) {
                binding.seasonOverviewTextView.text = "Bu sezon için özet bilgisi bulunmuyor."
            }

            // Bilgi satırı: Puan | Yıl | Bölüm Sayısı
            val year = season.airDate?.take(4) ?: "-"
            val rating = season.voteAverage?.times(10)?.roundToInt() ?: 0
            val episode = season.episodeCount ?: 0
            binding.seasonInfoTextView.text = "%$rating | $year | $episode Bölüm"

            // Poster
            Glide.with(itemView.context)
                .load(IMAGE_BASE_URL + season.posterPath)
                .placeholder(android.R.drawable.ic_menu_gallery) // Placeholder ekleyebilirsin
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.seasonPosterImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SeasonViewHolder {
        val binding = ItemSeasonBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SeasonViewHolder(binding)
    }

    override fun onBindViewHolder(holder: SeasonViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class SeasonDiffCallback : DiffUtil.ItemCallback<Season>() {
        override fun areItemsTheSame(oldItem: Season, newItem: Season): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Season, newItem: Season): Boolean = oldItem == newItem
    }
}