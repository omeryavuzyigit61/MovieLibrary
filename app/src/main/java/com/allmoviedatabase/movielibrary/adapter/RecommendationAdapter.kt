package com.allmoviedatabase.movielibrary.adapter

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemRecommendationMovieBinding
import com.allmoviedatabase.movielibrary.model.Movie
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlin.math.roundToInt

class RecommendationAdapter( private val onMovieClicked: (Int) -> Unit ) : ListAdapter<Movie, RecommendationAdapter.RecommendationViewHolder>(MovieDiffCallback()) {
    inner class RecommendationViewHolder(val binding: ItemRecommendationMovieBinding) : RecyclerView.ViewHolder(binding.root) {
    fun bind(movie: Movie) {
        binding.movieTitleTextView.text = movie.title
        val imageUrl = "https://image.tmdb.org/t/p/w300${movie.backdropPath}"
        Glide.with(itemView.context)
            .load(imageUrl)
            .diskCacheStrategy(DiskCacheStrategy.DATA)
            .fitCenter()
            .into(binding.moviePosterImageView)

        movie.voteAverage?.let { vote ->
            if (vote > 0) {
                // 8.684 -> 86.84 -> 87
                val ratingPercent = (vote * 10).roundToInt()
                binding.ratingTextView.text = "$ratingPercent%"
                binding.ratingTextView.visibility = View.VISIBLE
            } else {
                binding.ratingTextView.visibility = View.GONE
            }
        } ?: run {
            // Eğer voteAverage null ise TextView'ı gizle
            binding.ratingTextView.visibility = View.GONE
        }

        itemView.setOnClickListener {
            movie.id?.let { movieId ->
                onMovieClicked(movieId)
            }
        }
    }
}

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecommendationViewHolder {
        val binding = ItemRecommendationMovieBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return RecommendationViewHolder(binding)
    }

    override fun onBindViewHolder(holder: RecommendationViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            return oldItem == newItem
        }
    }}