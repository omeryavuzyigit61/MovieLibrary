package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemVideoBinding
import com.allmoviedatabase.movielibrary.model.video.VideoResult
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class VideoAdapter(
    private val onVideoClick: (String) -> Unit
) : ListAdapter<VideoResult, VideoAdapter.VideoViewHolder>(VideoDiffCallback()) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VideoViewHolder {
        val binding = ItemVideoBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return VideoViewHolder(binding)
    }

    override fun onBindViewHolder(holder: VideoViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class VideoViewHolder(private val binding: ItemVideoBinding) : RecyclerView.ViewHolder(binding.root) {
        fun bind(video: VideoResult) {
            binding.videoTitleTextView.text = video.name ?: "Video"

            // YouTube Thumbnail URL
            val thumbnailUrl = "https://img.youtube.com/vi/${video.key}/mqdefault.jpg"

            Glide.with(itemView.context)
                .load(thumbnailUrl)
                .centerCrop()
                .placeholder(android.R.drawable.ic_menu_gallery)
                .diskCacheStrategy(DiskCacheStrategy.ALL)
                .into(binding.videoThumbnailImageView)

            itemView.setOnClickListener {
                video.key?.let { key -> onVideoClick(key) }
            }
        }
    }

    class VideoDiffCallback : DiffUtil.ItemCallback<VideoResult>() {
        override fun areItemsTheSame(oldItem: VideoResult, newItem: VideoResult): Boolean = oldItem.key == newItem.key
        override fun areContentsTheSame(oldItem: VideoResult, newItem: VideoResult): Boolean = oldItem == newItem
    }
}