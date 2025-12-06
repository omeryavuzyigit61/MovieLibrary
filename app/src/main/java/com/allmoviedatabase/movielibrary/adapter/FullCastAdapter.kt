package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.R
import com.allmoviedatabase.movielibrary.databinding.ItemFullCastMemberBinding
import com.allmoviedatabase.movielibrary.model.Credits.CastMember
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

// DÜZELTME: Constructor'a 'onPersonClick' eklendi.
class FullCastAdapter(
    private val isTvShow: Boolean = false,
    private val onPersonClick: (Int) -> Unit
) : ListAdapter<CastMember, FullCastAdapter.ViewHolder>(DiffCallback()) {

    // Inner class yaparak dıştaki 'onPersonClick'e erişmesini sağladık
    inner class ViewHolder(private val binding: ItemFullCastMemberBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bind(castMember: CastMember, isTvShow: Boolean) {
            binding.actorNameTextView.text = castMember.name

            // --- KARAKTER ADINI BULMA MANTIĞI ---
            val rawCharacterName = if (!castMember.character.isNullOrEmpty()) {
                castMember.character
            } else if (!castMember.roles.isNullOrEmpty()) {
                castMember.roles[0].character
            } else {
                ""
            }

            // DİZİ KONTROLÜ VE FORMATLAMA
            if (isTvShow && castMember.totalEpisodeCount != null && castMember.totalEpisodeCount > 0) {
                binding.characterNameTextView.text = "$rawCharacterName\n(${castMember.totalEpisodeCount} Bölüm)"
            } else {
                binding.characterNameTextView.text = rawCharacterName
            }

            // TIKLAMA OLAYI
            itemView.setOnClickListener {
                castMember.id?.let { id -> onPersonClick(id) }
            }

            val imageUrl = "https://image.tmdb.org/t/p/w200${castMember.profilePath}"
            Glide.with(itemView.context)
                .load(imageUrl)
                .placeholder(R.drawable.user)
                .error(R.drawable.user)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.profileImageView)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val layoutInflater = LayoutInflater.from(parent.context)
        val binding = ItemFullCastMemberBinding.inflate(layoutInflater, parent, false)
        return ViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.bind(getItem(position), isTvShow)
    }

    class DiffCallback : DiffUtil.ItemCallback<CastMember>() {
        override fun areItemsTheSame(oldItem: CastMember, newItem: CastMember): Boolean = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CastMember, newItem: CastMember): Boolean = oldItem == newItem
    }
}