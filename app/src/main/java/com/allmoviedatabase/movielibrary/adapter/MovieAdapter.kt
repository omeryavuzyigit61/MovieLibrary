package com.allmoviedatabase.movielibrary.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemMovieBinding
import com.allmoviedatabase.movielibrary.model.Movie
import com.allmoviedatabase.movielibrary.util.onItemClickListener
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class MovieAdapter(private val listener: onItemClickListener) :
    ListAdapter<Movie, MovieAdapter.ListViewHolder>(MovieDiffCallback()) {

    class ListViewHolder(val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ListViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(holder: ListViewHolder, position: Int) {
        val currentItem = getItem(position)
        holder.binding.apply {
            titleNameTextView.text = currentItem.title
            val rating = currentItem.voteAverage?.times(10)

            // Güvenli null kontrolü ve renk ataması
            val color = when {
                rating != null && rating < 40 -> Color.RED
                rating != null && rating < 70 -> Color.YELLOW
                else -> Color.GREEN
            }

            // `let` kullanarak rating null değilse UI güncellemesi yap
            rating?.let {
                ratingTextView.text = "${it.toInt()}%"
                ratingProgressIndCator.setProgress(it.toInt(), true)
                ratingProgressIndCator.setIndicatorColor(color)
            }

            dateMovieTextView.text = currentItem.releaseDate
            // Hardcoded URL yerine daha güvenli bir yapı kullanılabilir (örn: BuildConfig'den)
            Glide.with(this.root)
                .load("https://media.themoviedb.org/t/p/w220_and_h330_face" + currentItem.posterPath)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(imageView)

            root.setOnClickListener {
                currentItem.id?.let { movieId ->
                    listener.omItemClick(movieId)
                }
            }
        }
    }

    /**
     * ListAdapter'ın iki liste arasındaki farkı verimli bir şekilde bulmasını sağlar.
     */
    class MovieDiffCallback : DiffUtil.ItemCallback<Movie>() {
        override fun areItemsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            // Öğelerin benzersiz ID'lerini karşılaştırır.
            // Modelinizde benzersiz bir 'id' alanı olduğunu varsayıyorum.
            return oldItem.id == newItem.id
        }

        override fun areContentsTheSame(oldItem: Movie, newItem: Movie): Boolean {
            // Öğenin içeriğinin değişip değişmediğini kontrol eder.
            // 'Movie' bir data class ise bu karşılaştırma otomatik olarak yapılır.
            return oldItem == newItem
        }
    }
}
