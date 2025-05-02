package com.allmoviedatabase.movielibrary.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.allmoviedatabase.movielibrary.databinding.ItemMovieBinding
import com.allmoviedatabase.movielibrary.model.Movie
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy

class MovieAdapter : RecyclerView.Adapter<MovieAdapter.ListViewHolder>() {

    private var itemList = ArrayList<Movie>()

    class ListViewHolder(val binding: ItemMovieBinding) : RecyclerView.ViewHolder(binding.root)

    override fun onCreateViewHolder(
        parent: ViewGroup,
        viewType: Int
    ): ListViewHolder {
        val binding = ItemMovieBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return ListViewHolder(binding)
    }

    override fun onBindViewHolder(
        holder: ListViewHolder,
        position: Int
    ) {
        val currentItem = itemList[position]
        holder.binding.apply {
            titleNameTextView.text = currentItem.title
            val rating = currentItem.voteAverage?.times(10)
            val color = when {
                rating!! < 40 -> Color.RED
                rating < 70 -> Color.YELLOW
                else -> Color.GREEN
            }
            ratingTextView.text = "${rating.toInt()}%"
            ratingProgressIndCator.setProgress(rating.toInt(),true)
            ratingProgressIndCator.setIndicatorColor(color)
            dateMovieTextView.text = currentItem.releaseDate
            Glide.with(this.root)
                .load("https://media.themoviedb.org/t/p/w220_and_h330_face"+currentItem.posterPath)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(imageView)
        }
    }

    fun updateList(newList: List<Movie>) {
        itemList.clear()
        itemList.addAll(newList)
        notifyDataSetChanged()
    }

    override fun getItemCount(): Int {
        return itemList.size
    }
}