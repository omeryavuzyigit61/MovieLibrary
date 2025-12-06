package com.allmoviedatabase.movielibrary.adapter

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import androidx.viewbinding.ViewBinding
import com.allmoviedatabase.movielibrary.databinding.ItemMovieBinding
import com.allmoviedatabase.movielibrary.databinding.ItemPersonBinding
import com.allmoviedatabase.movielibrary.databinding.ItemTvShowBinding
import com.allmoviedatabase.movielibrary.model.*
import com.allmoviedatabase.movielibrary.util.Constants.IMAGE_BASE_URL
import com.bumptech.glide.Glide
import com.bumptech.glide.load.engine.DiskCacheStrategy
import kotlin.math.roundToInt

// 1. Constructor'a 'isHorizontal' parametresini varsayılan olarak 'false' ile ekle
class ContentAdapter(
    private val isHorizontal: Boolean = false,
    private val onClick: (ListItem) -> Unit
) : ListAdapter<ListItem, ContentAdapter.BaseViewHolder<*>>(DiffCallback()) {

    private val MOVIE_ITEM = 1
    private val TV_SHOW_ITEM = 2
    private val PERSON_ITEM = 3

    abstract class BaseViewHolder<T>(binding: ViewBinding) : RecyclerView.ViewHolder(binding.root) {
        abstract fun bind(item: T)
    }

    // ... ViewHolder sınıfların AYNI kalıyor (MovieViewHolder, TvShowViewHolder, PersonViewHolder) ...
    // KOD TEKRARI OLMASIN DİYE BURAYI KISALTTIM, SENDEKİ MEVCUT KODLAR KALSIN

    class MovieViewHolder(private val binding: ItemMovieBinding) : BaseViewHolder<Movie>(binding) {
        override fun bind(item: Movie) {
            // ... Senin mevcut kodların ...
            binding.titleNameTextView.text = item.title
            binding.dateMovieTextView.text = item.releaseDate

            val rating = item.voteAverage?.times(10)
            val color = when {
                rating != null && rating < 40 -> Color.RED
                rating != null && rating < 70 -> Color.YELLOW
                else -> Color.GREEN
            }
            rating?.let {
                binding.ratingTextView.text = "${it.roundToInt()}%"
                binding.ratingProgressIndicator.progress = it.roundToInt()
                binding.ratingProgressIndicator.setIndicatorColor(color)
            }

            Glide.with(itemView.context)
                .load(IMAGE_BASE_URL + item.posterPath)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.imageView)
        }
    }

    class TvShowViewHolder(private val binding: ItemTvShowBinding) : BaseViewHolder<TvShow>(binding) {
        override fun bind(item: TvShow) {
            // ... Senin mevcut kodların ...
            binding.titleTextView.text = item.name // Dizi adı 'name' alanında gelir
            binding.dateTextView.text = item.firstAirDate

            val rating = item.voteAverage?.times(10)
            val color = when {
                rating != null && rating < 40 -> Color.RED
                rating != null && rating < 70 -> Color.YELLOW
                else -> Color.GREEN
            }
            rating?.let {
                binding.ratingTextView.text = "${it.roundToInt()}%"
                binding.ratingProgressIndicator.progress = it.roundToInt()
                binding.ratingProgressIndicator.setIndicatorColor(color)
            }

            Glide.with(itemView.context)
                .load(IMAGE_BASE_URL + item.posterPath)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.posterImageView)
        }
    }

    class PersonViewHolder(private val binding: ItemPersonBinding) : BaseViewHolder<Person>(binding) {
        override fun bind(item: Person) {
            // ... Senin mevcut kodların ...
            binding.personNameTextView.text = item.name
            binding.knownForTextView.text = item.knownForDepartment
            Glide.with(itemView.context)
                .load(IMAGE_BASE_URL + item.profilePath)
                .diskCacheStrategy(DiskCacheStrategy.DATA)
                .into(binding.profileImageView)
        }
    }

    override fun getItemViewType(position: Int): Int {
        return when (getItem(position)) {
            is ListItem.MovieItem -> MOVIE_ITEM
            is ListItem.TvShowItem -> TV_SHOW_ITEM
            is ListItem.PersonItem -> PERSON_ITEM
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): BaseViewHolder<*> {
        val inflater = LayoutInflater.from(parent.context)
        return when (viewType) {
            MOVIE_ITEM -> {
                val binding = ItemMovieBinding.inflate(inflater, parent, false)
                // Eğer yatay liste ise Film kartını da küçült
                if (isHorizontal) {
                    val params = binding.root.layoutParams
                    // 150dp genişlik (pixel'e çeviriyoruz)
                    params.width = (150 * parent.context.resources.displayMetrics.density).toInt()
                    binding.root.layoutParams = params
                }
                MovieViewHolder(binding)
            }
            TV_SHOW_ITEM -> {
                val binding = ItemTvShowBinding.inflate(inflater, parent, false)

                // 2. EĞER YATAY MOD İSE GENİŞLİĞİ SABİTLE (160dp)
                if (isHorizontal) {
                    val params = binding.root.layoutParams
                    // dp to pixel çevrimi: 160 * density
                    params.width = (160 * parent.context.resources.displayMetrics.density).toInt()
                    binding.root.layoutParams = params
                }

                TvShowViewHolder(binding)
            }
            PERSON_ITEM -> PersonViewHolder(ItemPersonBinding.inflate(inflater, parent, false))
            else -> throw IllegalArgumentException("Invalid view type")
        }
    }

    override fun onBindViewHolder(holder: BaseViewHolder<*>, position: Int) {
        val item = getItem(position)
        holder.itemView.setOnClickListener { onClick(item) }

        when (holder) {
            is MovieViewHolder -> holder.bind((item as ListItem.MovieItem).movie)
            is TvShowViewHolder -> holder.bind((item as ListItem.TvShowItem).tvShow)
            is PersonViewHolder -> holder.bind((item as ListItem.PersonItem).person)
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<ListItem>() {
        override fun areItemsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return when {
                oldItem is ListItem.MovieItem && newItem is ListItem.MovieItem -> oldItem.movie.id == newItem.movie.id
                oldItem is ListItem.TvShowItem && newItem is ListItem.TvShowItem -> oldItem.tvShow.id == newItem.tvShow.id
                oldItem is ListItem.PersonItem && newItem is ListItem.PersonItem -> oldItem.person.id == newItem.person.id
                else -> false
            }
        }
        override fun areContentsTheSame(oldItem: ListItem, newItem: ListItem): Boolean {
            return oldItem == newItem
        }
    }
}