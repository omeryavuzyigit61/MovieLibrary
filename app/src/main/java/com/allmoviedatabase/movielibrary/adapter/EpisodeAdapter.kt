package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemEpisodeExpandableBinding
import com.allmoviedatabase.movielibrary.model.SeasonDetail.Episode
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL_2
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class EpisodeAdapter(private val episodes: List<Episode>) : RecyclerView.Adapter<EpisodeAdapter.EpisodeViewHolder>() {

    // Hangi item'ın açık olduğunu tutan set (Birden fazla açılabilir)
    private val expandedPositions = mutableSetOf<Int>()

    inner class EpisodeViewHolder(val binding: ItemEpisodeExpandableBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(episode: Episode, position: Int) {
            // 1. TEMEL BİLGİLER
            binding.episodeTitleTextView.text = "${episode.episodeNumber} ${episode.name}"

            val rating = (episode.voteAverage ?: 0.0) * 10
            val date = episode.airDate ?: ""
            val runtime = "${episode.runtime ?: 0}m"
            binding.episodeInfoTextView.text = "${rating.toInt()}%  $date  $runtime"

            binding.episodeOverviewTextView.text = episode.overview

            Glide.with(itemView.context)
                .load(IMAGE_BASE_URL_2 + episode.stillPath)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.episodeImageView)

            // 2. GENİŞLEME MANTIĞI
            val isExpanded = expandedPositions.contains(position)
            binding.expandedLayout.visibility = if (isExpanded) View.VISIBLE else View.GONE
            binding.expandButton.text = if (isExpanded) "Küçült" else "Genişlet"
            // Özet kısmı genişleyince full görünsün, kapalıyken 3 satır
            binding.episodeOverviewTextView.maxLines = if (isExpanded) Int.MAX_VALUE else 3

            // 3. GENİŞLEYEN KISIM BİLGİLERİ (EKİP)
            val directors = episode.crew?.filter { it.job == "Director" }?.joinToString { it.name } ?: "-"
            val writers = episode.crew?.filter { it.job == "Writer" }?.joinToString { it.name } ?: "-"

            binding.crewLabelTextView.text = "Ekip Kadrosu (${episode.crew?.size ?: 0})"
            binding.crewContentTextView.text = "Yönetmen: $directors\n\nYazan: $writers"

            // 4. KONUK OYUNCULAR RECYCLERVIEW
            binding.guestStarsLabelTextView.text = "Konuk Yıldızlar (${episode.guestStars?.size ?: 0})"

            // Nested RecyclerView Ayarları
            val guestAdapter = GuestStarAdapter(episode.guestStars ?: emptyList())
            binding.guestStarsRecyclerView.apply {
                layoutManager = LinearLayoutManager(itemView.context)
                adapter = guestAdapter
                setRecycledViewPool(RecyclerView.RecycledViewPool()) // Performans için
            }

            // 5. BUTON TIKLAMA
            binding.expandButton.setOnClickListener {
                if (isExpanded) {
                    expandedPositions.remove(position)
                } else {
                    expandedPositions.add(position)
                }
                notifyItemChanged(position)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): EpisodeViewHolder {
        val binding = ItemEpisodeExpandableBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return EpisodeViewHolder(binding)
    }

    override fun onBindViewHolder(holder: EpisodeViewHolder, position: Int) {
        holder.bind(episodes[position], position)
    }

    override fun getItemCount() = episodes.size
}