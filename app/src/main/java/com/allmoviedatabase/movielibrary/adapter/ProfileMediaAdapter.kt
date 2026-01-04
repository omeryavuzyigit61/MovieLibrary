package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemProfileMediaBinding
import com.allmoviedatabase.movielibrary.model.ProfileMediaItem
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL
import com.bumptech.glide.Glide

class ProfileMediaAdapter(
    private val onItemClick: (Int, String) -> Unit
) : ListAdapter<ProfileMediaItem, ProfileMediaAdapter.MediaViewHolder>(DiffCallback) {

    inner class MediaViewHolder(private val binding: ItemProfileMediaBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: ProfileMediaItem) {
            binding.tvMediaTitle.text = item.title

            Glide.with(binding.root.context)
                .load(IMAGE_BASE_URL + item.posterPath)
                .into(binding.ivPoster) // Senin xml'inde id ivPoster ise burası doğru

            binding.root.setOnClickListener {
                // Modelde güncellediğimiz getId() sayesinde artık 0 gelmeyecek
                val realId = item.getEffectiveId()

                if (realId != 0) {
                    onItemClick(realId, item.mediaType)
                }
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MediaViewHolder {
        val binding = ItemProfileMediaBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return MediaViewHolder(binding)
    }

    override fun onBindViewHolder(holder: MediaViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    companion object DiffCallback : DiffUtil.ItemCallback<ProfileMediaItem>() {
        override fun areItemsTheSame(oldItem: ProfileMediaItem, newItem: ProfileMediaItem): Boolean {
            return oldItem.getEffectiveId() == newItem.getEffectiveId()
        }

        override fun areContentsTheSame(oldItem: ProfileMediaItem, newItem: ProfileMediaItem): Boolean {
            return oldItem == newItem
        }
    }
}